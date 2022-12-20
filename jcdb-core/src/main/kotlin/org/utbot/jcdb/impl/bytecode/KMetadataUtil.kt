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

package org.utbot.jcdb.impl.bytecode

import kotlinx.metadata.Flag
import kotlinx.metadata.InconsistentKotlinMetadataException
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeParameter
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.fieldSignature
import kotlinx.metadata.jvm.signature
import mu.KLogging
import org.utbot.jcdb.api.JcClassOrInterface
import org.utbot.jcdb.api.JcField
import org.utbot.jcdb.api.JcMethod
import org.utbot.jcdb.api.JcParameter

val logger = object : KLogging() {}.logger

/**
 * Returns [KotlinClassMetadata] instance for the class if it was generated by Kotlin.
 * See docs for [Metadata] for more info on how it is represented in bytecode and what it contains.
 */
@Suppress("UNCHECKED_CAST")
private val JcClassOrInterface.kMetadata: KotlinClassMetadata?
    get() {
        val kmParameters = annotations.firstOrNull { it.matches("kotlin.Metadata") }?.values ?: return null
        val kmHeader = KotlinClassHeader(
            kmParameters["k"] as? Int,
            (kmParameters["mv"] as? List<Int>)?.toIntArray(),
            (kmParameters["d1"] as? List<String>)?.toTypedArray(),
            (kmParameters["d2"] as? List<String>)?.toTypedArray(),
            kmParameters["xs"] as? String,
            kmParameters["pn"] as? String,
            kmParameters["xi"] as? Int,
        )
        return try {
            KotlinClassMetadata.read(kmHeader)
        } catch (e: InconsistentKotlinMetadataException) {
            logger.warn {
                "Can't parse Kotlin metadata annotation found on class $name, the class may be damaged"
            }
            null
        }
    }

val JcClassOrInterface.kmTypeParameters: List<KmTypeParameter>?
    get() =
        (kMetadata as? KotlinClassMetadata.Class)?.toKmClass()?.typeParameters

val JcMethod.kmFunction: KmFunction?
    get() =
        enclosingClass.kMetadata?.functions?.firstOrNull { it.signature?.name == name && it.signature?.desc == description }

val JcMethod.kmConstructor: KmConstructor?
    get() =
        enclosingClass.kMetadata?.constructors?.firstOrNull { it.signature?.name == name && it.signature?.desc == description }

val JcParameter.kmParameter: KmValueParameter?
    get() {
        method.kmFunction?.let {
            // Shift needed to properly handle extension functions
            val shift = if (it.receiverParameterType != null) 1 else 0

            // index - shift could be out of bounds if generated JVM parameter is fictive
            // E.g., see how extension functions and coroutines are compiled
            return it.valueParameters.getOrNull(index - shift)
        }

        return method.kmConstructor?.valueParameters?.get(index)
    }

// If parameter is a receiver parameter, it doesn't have KmValueParameter instance, but we still can get KmType for it
val JcParameter.kmType: KmType?
    get() =
        kmParameter?.type ?: run {
            if (index == 0)
                method.kmFunction?.receiverParameterType
            else
                null
        }

val JcField.kmType: KmType?
    get() =
        enclosingClass.kMetadata?.properties?.let { property ->
            // TODO: maybe we need to check desc here as well
            property.firstOrNull { it.fieldSignature?.name == name }?.returnType
        }

val JcMethod.kmReturnType: KmType?
    get() =
        kmFunction?.returnType

private val KotlinClassMetadata.functions: List<KmFunction>
    get() =
        when(this) {
            is KotlinClassMetadata.Class -> toKmClass().functions
            is KotlinClassMetadata.FileFacade -> toKmPackage().functions
            is KotlinClassMetadata.MultiFileClassPart -> toKmPackage().functions
            else -> listOf()
        }

private val KotlinClassMetadata.constructors: List<KmConstructor>
    get() = (this as? KotlinClassMetadata.Class)?.toKmClass()?.constructors ?: emptyList()

private val KotlinClassMetadata.properties: List<KmProperty>
    get() =
        when(this) {
            is KotlinClassMetadata.Class -> toKmClass().properties
            is KotlinClassMetadata.FileFacade -> toKmPackage().properties
            is KotlinClassMetadata.MultiFileClassPart -> toKmPackage().properties
            else -> listOf()
        }

val KmType.isNullable: Boolean
    get() = Flag.Type.IS_NULLABLE(flags)