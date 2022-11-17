package org.utbot.jcdb.impl.features

import org.objectweb.asm.Opcodes
import org.utbot.jcdb.api.FieldUsageMode
import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.JcField
import org.utbot.jcdb.api.JcMethod
import org.utbot.jcdb.api.ext.HierarchyExtension
import org.utbot.jcdb.api.findFieldOrNull
import org.utbot.jcdb.api.findMethodOrNull
import org.utbot.jcdb.api.isPackagePrivate
import org.utbot.jcdb.api.isPrivate
import org.utbot.jcdb.api.isStatic
import org.utbot.jcdb.api.packageName
import org.utbot.jcdb.impl.bytecode.JcClassOrInterfaceImpl

class SyncUsagesExtension(private val hierarchyExtension: HierarchyExtension, private val cp: JcClasspath) {

    /**
     * find all methods that call this method
     *
     * @param method method
     */
    fun findUsages(method: JcMethod): Sequence<JcMethod> {
        val maybeHierarchy = maybeHierarchy(method.enclosingClass, method.isPrivate) {
            it.findMethodOrNull(method.name, method.description).let {
                it == null || !it.isOverriddenBy(method)
            } // no overrides// no override
        }

        val opcodes = when (method.isStatic) {
            true -> setOf(Opcodes.INVOKESTATIC)
            else -> setOf(Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL)
        }
        return findMatches(maybeHierarchy, method = method, opcodes = opcodes)

    }

    /**
     * find all methods that directly modifies field
     *
     * @param field field
     * @param mode mode of search
     */
    fun findUsages(field: JcField, mode: FieldUsageMode): Sequence<JcMethod> {
        val maybeHierarchy = maybeHierarchy(field.enclosingClass, field.isPrivate) {
            it.findFieldOrNull(field.name).let {
                it == null || !it.isOverriddenBy(field)
            } // no overrides
        }
        val isStatic = field.isStatic
        val opcode = when {
            isStatic && mode == FieldUsageMode.WRITE -> Opcodes.PUTSTATIC
            !isStatic && mode == FieldUsageMode.WRITE -> Opcodes.PUTFIELD
            isStatic && mode == FieldUsageMode.READ -> Opcodes.GETSTATIC
            !isStatic && mode == FieldUsageMode.READ -> Opcodes.GETFIELD
            else -> return emptySequence()
        }

        return findMatches(maybeHierarchy, field = field, opcodes = listOf(opcode))
    }

    private fun maybeHierarchy(
        enclosingClass: JcClassOrInterface,
        private: Boolean,
        matcher: (JcClassOrInterface) -> Boolean
    ): Set<JcClassOrInterface> {
        return when {
            private -> hashSetOf(enclosingClass)
            else -> hierarchyExtension.findSubClasses(enclosingClass.name, true).filter(matcher)
                .toHashSet() + enclosingClass
        }
    }


    private fun findMatches(
        hierarchy: Set<JcClassOrInterface>,
        method: JcMethod? = null,
        field: JcField? = null,
        opcodes: Collection<Int>
    ): Sequence<JcMethod> {
        val list = hierarchy.map {
            Usages.syncQuery(
                cp, UsageFeatureRequest(
                    methodName = method?.name,
                    description = method?.description,
                    field = field?.name,
                    opcodes = opcodes,
                    className = it.name
                )
            ).flatMap {
                JcClassOrInterfaceImpl(
                    cp,
                    it.source
                ).declaredMethods.slice(it.offsets.map { it.toInt() })
            }
        }

        return sequence {
            list.forEach {
                yieldAll(it)
            }
        }
    }

    private fun JcMethod.isOverriddenBy(method: JcMethod): Boolean {
        if (name == method.name && description == method.description) {
            return when {
                isPrivate -> false
                isPackagePrivate -> enclosingClass.packageName == method.enclosingClass.packageName
                else -> true
            }
        }
        return false
    }

    private fun JcField.isOverriddenBy(field: JcField): Boolean {
        if (name == field.name) {
            return when {
                isPrivate -> false
                isPackagePrivate -> enclosingClass.packageName == field.enclosingClass.packageName
                else -> true
            }
        }
        return false
    }
}


suspend fun JcClasspath.usagesExtension(): SyncUsagesExtension {
    return SyncUsagesExtension(hierarchyExt(), this)
}

suspend fun JcClasspath.findUsages(method: JcMethod) = usagesExtension().findUsages(method)
suspend fun JcClasspath.findUsages(field: JcField, mode: FieldUsageMode) = usagesExtension().findUsages(field, mode)
