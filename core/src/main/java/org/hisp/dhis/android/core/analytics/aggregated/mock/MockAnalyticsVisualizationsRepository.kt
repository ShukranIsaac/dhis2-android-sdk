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

package org.hisp.dhis.android.core.analytics.aggregated.mock

import dagger.Reusable
import io.reactivex.Single
import org.hisp.dhis.android.core.analytics.aggregated.AnalyticsVisualizationsRepository
import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse
import org.hisp.dhis.android.core.analytics.aggregated.internal.AnalyticsException
import org.hisp.dhis.android.core.arch.helpers.Result
import javax.inject.Inject

@Reusable
class MockAnalyticsVisualizationsRepository @Inject constructor() : AnalyticsVisualizationsRepository {

    override fun withVisualization(visualization: String): AnalyticsVisualizationsRepository {
        return MockAnalyticsVisualizationsRepository()
    }

    override fun withPeriods(periods: List<DimensionItem.PeriodItem>): AnalyticsVisualizationsRepository {
        return MockAnalyticsVisualizationsRepository()
    }

    override fun withOrganisationUnits(
        orgUnits: List<DimensionItem.OrganisationUnitItem>
    ): AnalyticsVisualizationsRepository {
        return MockAnalyticsVisualizationsRepository()
    }

    override fun evaluate(): Single<Result<GridAnalyticsResponse, AnalyticsException>> {
        return Single.fromCallable { blockingEvaluate() }
    }

    override fun blockingEvaluate(): Result<GridAnalyticsResponse, AnalyticsException> =
        Result.Success(GridAnalyticsResponseSamples.sample1)
}
