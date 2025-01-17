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

package org.jacodb.impl.types.substition

import org.jacodb.impl.types.signature.JvmArrayType
import org.jacodb.impl.types.signature.JvmBoundWildcard.JvmLowerBoundWildcard
import org.jacodb.impl.types.signature.JvmBoundWildcard.JvmUpperBoundWildcard
import org.jacodb.impl.types.signature.JvmClassRefType
import org.jacodb.impl.types.signature.JvmParameterizedType
import org.jacodb.impl.types.signature.JvmType
import org.jacodb.impl.types.signature.JvmTypeParameterDeclaration
import org.jacodb.impl.types.signature.JvmTypeParameterDeclarationImpl
import org.jacodb.impl.types.signature.JvmTypeVariable
import org.jacodb.impl.types.signature.JvmTypeVisitor

internal class VisitorContext(private val processed: HashSet<Any> = HashSet()) {

    fun makeProcessed(type: Any): Boolean {
        return processed.add(type)
    }


    fun isProcessed(type: Any): Boolean {
        return processed.contains(type)
    }
}

internal interface RecursiveJvmTypeVisitor : JvmTypeVisitor<VisitorContext> {
    fun visitType(type: JvmType): JvmType {
        return visitType(type, VisitorContext())
    }

    override fun visitUpperBound(type: JvmUpperBoundWildcard, context: VisitorContext): JvmType {
        return JvmUpperBoundWildcard(visitType(type.bound, context))
    }

    override fun visitLowerBound(type: JvmLowerBoundWildcard, context: VisitorContext): JvmType {
        return JvmLowerBoundWildcard(visitType(type.bound, context))
    }

    override fun visitArrayType(type: JvmArrayType, context: VisitorContext): JvmType {
        return JvmArrayType(visitType(type.elementType, context), type.isNullable, type.annotations)
    }

    override fun visitTypeVariable(type: JvmTypeVariable, context: VisitorContext): JvmType {
        if (context.isProcessed(type)) {
            return type
        }
        val result = visitUnprocessedTypeVariable(type, context)
        context.makeProcessed(type)
        return result
    }

    fun visitUnprocessedTypeVariable(type: JvmTypeVariable, context: VisitorContext): JvmType {
        return type
    }

    override fun visitClassRef(type: JvmClassRefType, context: VisitorContext): JvmType {
        return type
    }

    override fun visitNested(type: JvmParameterizedType.JvmNestedType, context: VisitorContext): JvmType {
        return JvmParameterizedType.JvmNestedType(
            type.name,
            type.parameterTypes.map { visitType(it, context) },
            visitType(type.ownerType, context),
            type.isNullable,
            type.annotations
        )
    }

    override fun visitParameterizedType(type: JvmParameterizedType, context: VisitorContext): JvmType {
        return JvmParameterizedType(type.name, type.parameterTypes.map { visitType(it, context) }, type.isNullable, type.annotations)
    }

    fun visitDeclaration(
        declaration: JvmTypeParameterDeclaration,
        context: VisitorContext = VisitorContext()
    ): JvmTypeParameterDeclaration {
        if (context.isProcessed(declaration)) {
            return declaration
        }
        context.makeProcessed(declaration)
        return JvmTypeParameterDeclarationImpl(
            declaration.symbol,
            declaration.owner,
            declaration.bounds?.map { visitType(it, context) }
        )
    }
}


internal val Map<String, JvmTypeParameterDeclaration>.fixDeclarationVisitor: RecursiveJvmTypeVisitor
    get() {
        val declarations = this
        return object : RecursiveJvmTypeVisitor {

            override fun visitTypeVariable(type: JvmTypeVariable, context: VisitorContext): JvmType {
                type.declaration = declarations[type.symbol]!!
                return type
            }
        }
    }
