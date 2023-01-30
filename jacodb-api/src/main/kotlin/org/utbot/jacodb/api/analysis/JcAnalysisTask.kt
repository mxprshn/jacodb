/*
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

package org.utbot.jacodb.api.analysis

import org.utbot.jacodb.api.JcClassOrInterface
import org.utbot.jacodb.api.JcClassProcessingTask
import org.utbot.jacodb.api.JcMethod
import org.utbot.jacodb.api.cfg.JcGraph

interface JcAnalysisTask : JcClassProcessingTask {

    @JvmDefault
    val transformers: List<JcGraphTransformer> get() = emptyList()

    @JvmDefault
    fun flowOf(method: JcMethod): JcGraph {
        val initial = method.flowGraph()
        return transformers.fold(initial) { value, transformer ->
            transformer.transform(value)
        }
    }

    override fun process(clazz: JcClassOrInterface) {
        clazz.declaredMethods.forEach {
            process(flowOf(it))
        }
    }

    fun process(graph: JcGraph)
}