/**
 *  Copyright 2022 UnitTestBot contributors (utbot.org)
 * <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.utbot.jcdb.impl.persistence

import kotlinx.coroutines.runBlocking
import org.utbot.jcdb.api.JcClasspath
import org.utbot.jcdb.api.ext.HierarchyExtension
import org.utbot.jcdb.impl.WithRestoredDB
import org.utbot.jcdb.impl.allClasspath
import org.utbot.jcdb.impl.features.hierarchyExt
import org.utbot.jcdb.impl.tests.DatabaseEnvTest
import org.utbot.jcdb.impl.withDB

class RestoredDBTest : DatabaseEnvTest() {

    companion object : WithRestoredDB()

    override val cp: JcClasspath
        get() = runBlocking {
            val withDB = this@RestoredDBTest.javaClass.withDB
            withDB.db.classpath(allClasspath)
        }

    override val hierarchyExt: HierarchyExtension
        get() = runBlocking { cp.hierarchyExt() }


}

