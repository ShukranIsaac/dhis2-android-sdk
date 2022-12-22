/*
 *  Copyright (c) 2004-2022, University of Oslo
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

package org.hisp.dhis.android.core.analytics.aggregated.internal.evaluator

import org.hisp.dhis.android.core.arch.helpers.DateUtils
import org.hisp.dhis.android.core.parser.internal.expression.ParserUtils
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.period.internal.CalendarProviderFactory
import org.hisp.dhis.android.core.period.internal.ParentPeriodGeneratorImpl

object AnalyticsPeriodHelper {

    private val periodGenerator = ParentPeriodGeneratorImpl.create(CalendarProviderFactory.calendarProvider)

    fun shiftPeriod(period: Period, offset: Int): Period {
        return periodGenerator.generatePeriod(period.periodType()!!, period.startDate()!!, offset)!!
    }

    fun shiftPeriods(periods: List<Period>, offset: Int): List<Period> {
        return periods.map { shiftPeriod(it, offset) }
    }

    fun countWeeksOrBiWeeksInYear(periodType: PeriodType, year: Int): Int {
        // The period containing this date is the last period in the year
        val lastDate = DateUtils.SIMPLE_DATE_FORMAT.parse("$year-12-28")
        val lastPeriod = periodGenerator.generatePeriod(periodType, lastDate, 0)!!

        return ParserUtils.getTrailingDigits(lastPeriod.periodId()!!)!!
    }

    fun getPeriodsToDate(period: Period): List<Period> {
        val periodInYear = ParserUtils.getPeriodInYear(period)

        val periodsExcludingEnd = (1 until periodInYear).map {
            periodGenerator.generatePeriod(period.periodType()!!, period.startDate()!!, -it)!!
        }

        return periodsExcludingEnd + period
    }
}
