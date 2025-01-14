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

package org.jacodb.impl.types.signature

import org.jacodb.api.JcAccessible

interface JvmTypeParameterDeclaration {
    val symbol: String
    val owner: JcAccessible
    val bounds: List<JvmType>?
}

internal class JvmTypeParameterDeclarationImpl(
    override val symbol: String,
    override val owner: JcAccessible,
    override val bounds: List<JvmType>? = null
) : JvmTypeParameterDeclaration {


    override fun toString(): String {
        return "$symbol : ${bounds?.joinToString { it.displayName }}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JvmTypeParameterDeclarationImpl

        if (symbol != other.symbol) return false
        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }

}