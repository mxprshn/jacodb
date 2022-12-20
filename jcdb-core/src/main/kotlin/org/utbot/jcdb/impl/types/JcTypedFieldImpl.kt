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

package org.utbot.jcdb.impl.types

import org.utbot.jcdb.api.JcField
import org.utbot.jcdb.api.JcRefType
import org.utbot.jcdb.api.JcType
import org.utbot.jcdb.api.JcTypedField
import org.utbot.jcdb.api.ext.isNullable
import org.utbot.jcdb.api.throwClassNotFound
import org.utbot.jcdb.impl.types.signature.FieldResolutionImpl
import org.utbot.jcdb.impl.types.signature.FieldSignature
import org.utbot.jcdb.impl.types.substition.JcSubstitutor

class JcTypedFieldImpl(
    override val enclosingType: JcRefType,
    override val field: JcField,
    private val substitutor: JcSubstitutor
) : JcTypedField {

    private val classpath = field.enclosingClass.classpath
    private val resolvedType by lazy(LazyThreadSafetyMode.NONE) {
        val resolution = FieldSignature.of(field) as? FieldResolutionImpl
        resolution?.fieldType
    }

    override val name: String get() = this.field.name

    override val fieldType: JcType by lazy {
        val typeName = field.type.typeName
        val type = resolvedType?.let {
            classpath.typeOf(substitutor.substitute(it))
        } ?: classpath.findTypeOrNull(field.type.typeName) ?: typeName.throwClassNotFound()

        field.isNullable?.let {
            (type as? JcRefType)?.copyWithNullability(it)
        } ?: type
    }


}