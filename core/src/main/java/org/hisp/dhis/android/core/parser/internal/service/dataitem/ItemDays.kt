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
package org.hisp.dhis.android.core.parser.internal.service.dataitem

import org.hisp.dhis.android.core.parser.internal.expression.CommonExpressionVisitor
import org.hisp.dhis.android.core.parser.internal.expression.ExpressionItem
import org.hisp.dhis.android.core.parser.internal.expression.ParserUtils
import org.hisp.dhis.parser.expression.antlr.ExpressionParser.ExprContext

/**
 * Parsed expression item as handled by the expression service.
 *
 *
 * When getting item id and org unit group, just return default values
 * (because not every item implements these, only those that need to.)
 *
 * @author Jim Grace
 */
internal class ItemDays : ExpressionItem {

    override fun getDescription(ctx: ExprContext, visitor: CommonExpressionVisitor): Any {
        visitor.itemDescriptions[ctx.text] = "[Number of days]"
        return ParserUtils.DOUBLE_VALUE_IF_NULL
    }

    override fun evaluate(ctx: ExprContext, visitor: CommonExpressionVisitor): Any? {
        return visitor.days
    }

    override fun regenerate(ctx: ExprContext, visitor: CommonExpressionVisitor): Any? {
        return visitor.days?.toString()
    }

    override fun getSql(ctx: ExprContext, visitor: CommonExpressionVisitor): Any {
        return visitor.days?.toString() ?: ParserUtils.DOUBLE_VALUE_IF_NULL
    }
}