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

package org.jacodb.analysis.analyzers

import org.jacodb.analysis.DumpableAnalysisResult
import org.jacodb.analysis.VulnerabilityInstance
import org.jacodb.analysis.engine.Analyzer
import org.jacodb.analysis.engine.DomainFact
import org.jacodb.analysis.engine.FlowFunctionInstance
import org.jacodb.analysis.engine.FlowFunctionsSpace
import org.jacodb.analysis.engine.IFDSResult
import org.jacodb.analysis.engine.SpaceId
import org.jacodb.analysis.engine.ZEROFact
import org.jacodb.analysis.paths.AccessPath
import org.jacodb.analysis.paths.toPath
import org.jacodb.analysis.paths.toPathOrNull
import org.jacodb.api.JcClasspath
import org.jacodb.api.JcMethod
import org.jacodb.api.analysis.JcApplicationGraph
import org.jacodb.api.cfg.JcArgument
import org.jacodb.api.cfg.JcAssignInst
import org.jacodb.api.cfg.JcExpr
import org.jacodb.api.cfg.JcInst
import org.jacodb.api.cfg.JcInstanceCallExpr
import org.jacodb.api.cfg.JcLocal
import org.jacodb.api.cfg.JcSpecialCallExpr
import org.jacodb.api.cfg.JcStaticCallExpr
import org.jacodb.api.cfg.values
import org.jacodb.api.ext.cfg.callExpr


class UnusedVariableAnalyzer(
    val graph: JcApplicationGraph
) : Analyzer {
    override val flowFunctions: FlowFunctionsSpace = UnusedVariableForwardFunctions(graph.classpath)
    override val backward: Analyzer
        get() = error("No backward analysis for Unused variable")

    companion object : SpaceId {
        override val value: String = "unused variable analysis"
    }

    private fun AccessPath.isUsedAt(expr: JcExpr): Boolean {
        return this in expr.values.map { it.toPathOrNull() }
    }

    private fun AccessPath.isUsedAt(inst: JcInst): Boolean {
        val callExpr = inst.callExpr

        // TODO: currently we use here that `this` is not in `operands` of JcSpecialCallExpr, this may be wrong
        if (callExpr != null) {
            if (graph.callees(inst).none()) {
                return isUsedAt(callExpr)
            }

            if (callExpr is JcInstanceCallExpr) {
                return isUsedAt(callExpr.instance)
            }
            return false
        }
        if (inst is JcAssignInst) {
            return isUsedAt(inst.rhv) && (inst.lhv !is JcLocal || inst.rhv !is JcLocal)
        }
        return false
    }

    override fun calculateSources(ifdsResult: IFDSResult): DumpableAnalysisResult {
        val used: MutableMap<JcInst, Boolean> = mutableMapOf()
        ifdsResult.resultFacts.forEach { (inst, facts) ->
            facts.filterIsInstance<UnusedVariableNode>().forEach { fact ->
                if (fact.initStatement !in used) {
                    used[fact.initStatement] = false
                }

                if (fact.variable.isUsedAt(inst)) {
                    used[fact.initStatement] = true
                }
            }
        }
        val vulnerabilities = used.filterValues { !it }.keys.map {
            VulnerabilityInstance(value, listOf(it.toString()), it.toString(), emptyList())
        }
        return DumpableAnalysisResult(vulnerabilities)
    }
}

private class UnusedVariableForwardFunctions(
    val classpath: JcClasspath
) : FlowFunctionsSpace {

    override val inIds: List<SpaceId> get() = listOf(UnusedVariableAnalyzer, ZEROFact.id)

    override fun obtainStartFacts(startStatement: JcInst): Collection<DomainFact> {
        return listOf(ZEROFact)
    }

    override fun obtainSequentFlowFunction(current: JcInst, next: JcInst) = object : FlowFunctionInstance {
        override val inIds = this@UnusedVariableForwardFunctions.inIds

        override fun compute(fact: DomainFact): Collection<DomainFact> {
            if (current !is JcAssignInst) {
                return listOf(fact)
            }

            if (fact == ZEROFact) {
                val toPath = current.lhv.toPathOrNull() ?: return listOf(ZEROFact)
                return listOf(ZEROFact, UnusedVariableNode(toPath, current))
            }

            if (fact !is UnusedVariableNode) {
                return emptyList()
            }

            val default = if (fact.variable == current.lhv.toPathOrNull()) emptyList() else listOf(fact)
            val fromPath = current.rhv.toPathOrNull() ?: return default
            val toPath = current.lhv.toPathOrNull() ?: return default

            if (fromPath.isOnHeap || toPath.isOnHeap) {
                return default
            }

            if (fromPath == fact.variable) {
                return default.plus(UnusedVariableNode(toPath, fact.initStatement))
            }
            return default
        }

    }

    override fun obtainCallToStartFlowFunction(callStatement: JcInst, callee: JcMethod) = object : FlowFunctionInstance {
        override val inIds = this@UnusedVariableForwardFunctions.inIds

        override fun compute(fact: DomainFact): Collection<DomainFact> {
            val callExpr = callStatement.callExpr ?: error("Call expr is expected to be not-null")
            val formalParams = callee.parameters.map {
                JcArgument.of(it.index, it.name, classpath.findTypeOrNull(it.type.typeName)!!)
            }

            if (fact == ZEROFact) {
                // We don't show unused parameters for virtual calls
                if (callExpr !is JcStaticCallExpr && callExpr !is JcSpecialCallExpr) {
                    return listOf(ZEROFact)
                }
                return formalParams.map { UnusedVariableNode(it.toPath(), callStatement) }.plus(ZEROFact)
            }

            if (fact !is UnusedVariableNode) {
                return emptyList()
            }

            return formalParams.zip(callExpr.args)
                .filter { (_, actual) -> actual.toPathOrNull() == fact.variable }
                .map { UnusedVariableNode(it.first.toPath(), fact.initStatement) }
        }

    }

    override fun obtainCallToReturnFlowFunction(callStatement: JcInst, returnSite: JcInst) =
        obtainSequentFlowFunction(callStatement, returnSite)

    override fun obtainExitToReturnSiteFlowFunction(
        callStatement: JcInst,
        returnSite: JcInst,
        exitStatement: JcInst
    ) = object : FlowFunctionInstance {
        override val inIds = this@UnusedVariableForwardFunctions.inIds

        override fun compute(fact: DomainFact): Collection<DomainFact> {
            return if (fact == ZEROFact) listOf(ZEROFact) else emptyList()
        }

    }

    override val backward: FlowFunctionsSpace
        get() = error("No backward FF for unused variable")
}