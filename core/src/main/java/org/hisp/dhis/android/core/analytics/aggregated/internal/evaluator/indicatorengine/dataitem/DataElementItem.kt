/*
 *  Copyright (c) 2004-2021, University of Oslo
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.android.core.analytics.aggregated.internal.evaluator.indicatorengine.dataitem

import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.analytics.aggregated.internal.AnalyticsServiceEvaluationItem
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.parser.internal.expression.CommonExpressionVisitor
import org.hisp.dhis.android.core.parser.internal.expression.ExpressionItem
import org.hisp.dhis.android.core.parser.internal.expression.ParserUtils
import org.hisp.dhis.parser.expression.antlr.ExpressionParser.ExprContext

@Suppress("ReturnCount")
class DataElementItem : ExpressionItem {

    override fun evaluate(ctx: ExprContext, visitor: CommonExpressionVisitor): Any? {
        val dataElementUid = ctx.uid0?.text
        val categoryOptionComboUid = ctx.uid1?.text

        val dataItem = when {
            dataElementUid != null && categoryOptionComboUid != null ->
                DimensionItem.DataItem.DataElementOperandItem(dataElementUid, categoryOptionComboUid)
            dataElementUid != null ->
                DimensionItem.DataItem.DataElementItem(dataElementUid)
            else ->
                return ParserUtils.DOUBLE_VALUE_IF_NULL
        }

        val evaluationItem = AnalyticsServiceEvaluationItem(
            dimensionItems = listOf(dataItem),
            filters = visitor.indicatorContext.evaluationItem.filters +
                visitor.indicatorContext.evaluationItem.dimensionItems.map { it as DimensionItem }
        )

        val metadataEntry = when {
            categoryOptionComboUid != null -> {
                val dataElementOperandId = "$dataElementUid.$categoryOptionComboUid"
                val dataElementOperand = DataElementOperand.builder()
                    .uid(dataElementOperandId)
                    .dataElement(ObjectWithUid.create(dataElementUid))
                    .categoryOptionCombo(ObjectWithUid.create(categoryOptionComboUid))
                    .build()

                dataElementOperandId to MetadataItem.DataElementOperandItem(dataElementOperand)
            }
            else -> {
                val dataElement =
                    visitor.indicatorContext.dataElementStore.selectByUid(dataElementUid)
                        ?: return ParserUtils.DOUBLE_VALUE_IF_NULL

                dataElementUid to MetadataItem.DataElementItem(dataElement)
            }
        }

        return visitor.indicatorContext.dataElementEvaluator.evaluate(
            evaluationItem = evaluationItem,
            metadata = visitor.indicatorContext.contextMetadata + metadataEntry
        ) ?: ParserUtils.DOUBLE_VALUE_IF_NULL
    }
}
