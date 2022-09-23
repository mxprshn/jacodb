package org.utbot.jcdb.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.utbot.jcdb.JCDBSettings
import org.utbot.jcdb.api.JCDB
import org.utbot.jcdb.api.JCDBPersistence
import org.utbot.jcdb.api.JcByteCodeLocation
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.RegisteredLocation
import org.utbot.jcdb.impl.fs.JavaRuntime
import org.utbot.jcdb.impl.fs.asByteCodeLocation
import org.utbot.jcdb.impl.fs.filterExisted
import org.utbot.jcdb.impl.fs.load
import org.utbot.jcdb.impl.storage.PersistentLocationRegistry
import org.utbot.jcdb.impl.storage.SQLitePersistenceImpl
import org.utbot.jcdb.impl.vfs.GlobalClassesVfs
import org.utbot.jcdb.impl.vfs.RemoveLocationsVisitor
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class JCDBImpl(
    override val persistence: JCDBPersistence,
    val featureRegistry: FeaturesRegistry,
    private val settings: JCDBSettings
) : JCDB {

    private val classesVfs = GlobalClassesVfs()
    internal val javaRuntime = JavaRuntime(settings.jre)
    private val hooks = settings.hooks.map { it(this) }

    internal val locationsRegistry: LocationsRegistry
    private val backgroundJobs = ConcurrentHashMap<Int, Job>()

    private val isClosed = AtomicBoolean()
    private val jobId = AtomicInteger()

    init {
        featureRegistry.bind(this)
        // todo rewrite in more elegant way
        locationsRegistry = PersistentLocationRegistry(persistence as SQLitePersistenceImpl, featureRegistry)
    }

    override val locations: List<JcByteCodeLocation>
        get() = locationsRegistry.actualLocations.map { it.jcLocation }

    suspend fun restore() {
        persistence.setup()
        locationsRegistry.cleanup()
        val runtime = JavaRuntime(settings.jre).allLocations
        locationsRegistry.setup(runtime).new.process()
        locationsRegistry.registerIfNeeded(settings.predefinedDirOrJars.filter { it.exists() }
            .map { it.asByteCodeLocation(isRuntime = false) }
        ).new.process()
    }

    override suspend fun classpath(dirOrJars: List<File>): JcClasspath {
        assertNotClosed()
        val existedLocations = dirOrJars.filterExisted().map { it.asByteCodeLocation() }
        val processed = locationsRegistry.registerIfNeeded(existedLocations.toList())
            .also { it.new.process() }.registered + locationsRegistry.runtimeLocations
        return JcClasspathImpl(
            locationsRegistry.newSnapshot(processed),
            featureRegistry,
            this,
            classesVfs
        )
    }

    fun new(cp: JcClasspathImpl): JcClasspath {
        assertNotClosed()
        return JcClasspathImpl(
            locationsRegistry.newSnapshot(cp.registeredLocations),
            featureRegistry,
            cp.db,
            classesVfs
        )
    }

    override suspend fun load(dirOrJar: File) = apply {
        assertNotClosed()
        load(listOf(dirOrJar))
    }

    override suspend fun load(dirOrJars: List<File>) = apply {
        assertNotClosed()
        loadLocations(dirOrJars.filterExisted().map { it.asByteCodeLocation() })
    }

    override suspend fun loadLocations(locations: List<JcByteCodeLocation>) = apply {
        assertNotClosed()
        locationsRegistry.registerIfNeeded(locations).new.process()
    }

    private suspend fun List<RegisteredLocation>.process(): List<RegisteredLocation> {
        val actions = ConcurrentLinkedQueue<RegisteredLocation>()

        val libraryTrees = withContext(Dispatchers.IO) {
            map { location ->
                async {
                    // here something may go wrong
                    val libraryTree = location.load()
                    actions.add(location)
                    libraryTree
                }
            }
        }.awaitAll()
        val locationClasses = libraryTrees.associate {
            it.location to it.pushInto(classesVfs).values
        }
        val backgroundJobId = jobId.incrementAndGet()
        backgroundJobs[backgroundJobId] = BackgroundScope.launch {
            val parentScope = this
            actions.map { location ->
                async {
                    if (parentScope.isActive) {
                        val addedClasses = locationClasses[location]
                        if (addedClasses != null) {
                            if (parentScope.isActive) {
                                persistence.persist(location, addedClasses.toList())
                                featureRegistry.index(location, addedClasses)
                            }
                        }
                    }
                }
            }.joinAll()
            locationsRegistry.afterProcessing(this@process)
            backgroundJobs.remove(backgroundJobId)
        }
        return this
    }

    override suspend fun refresh() {
        awaitBackgroundJobs()
        locationsRegistry.refresh().new.process()
        val result = locationsRegistry.cleanup()
        classesVfs.visit(RemoveLocationsVisitor(result.outdated))
    }

    override suspend fun rebuildFeatures() {
        awaitBackgroundJobs()
        // todo implement me
    }

    override fun watchFileSystemChanges(): JCDB {
        val delay = settings.watchFileSystemChanges?.delay
        if (delay != null) { // just paranoid check
            BackgroundScope.launch {
                while (true) {
                    delay(delay)
                    refresh()
                }
            }
        }
        return this
    }

    override suspend fun awaitBackgroundJobs() {
        backgroundJobs.values.toList().joinAll()
    }

    fun afterStart() {
        hooks.forEach { it.afterStart() }
    }

    override fun close() {
        isClosed.set(true)
        locationsRegistry.close()
        backgroundJobs.values.forEach {
            it.cancel()
        }
        backgroundJobs.clear()
        persistence.close()
        hooks.forEach { it.afterStop() }
    }

    private fun assertNotClosed() {
        if (isClosed.get()) {
            throw IllegalStateException("Database is already closed")
        }
    }

}