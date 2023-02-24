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
package org.hisp.dhis.android.core.trackedentity.internal

import io.reactivex.Single
import java.util.concurrent.Callable
import org.hisp.dhis.android.core.arch.api.payload.internal.Payload
import org.hisp.dhis.android.core.arch.helpers.CollectionsHelper
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryOnline
import org.hisp.dhis.android.core.tracker.exporter.TrackerAPIQuery

internal abstract class TrackedEntityEndpointCallFactory {

    abstract fun getCollectionCall(query: TrackerAPIQuery): Single<Payload<TrackedEntityInstance>>

    abstract suspend fun getEntityCall(uid: String, query: TrackerAPIQuery): TrackedEntityInstance

    abstract fun getRelationshipEntityCall(uid: String): Single<Payload<TrackedEntityInstance>>

    abstract fun getQueryCall(query: TrackedEntityInstanceQueryOnline): Callable<List<TrackedEntityInstance>>

    protected fun getUidStr(query: TrackerAPIQuery): String? {
        return if (query.uids.isEmpty()) null else CollectionsHelper.joinCollectionWithSeparator(query.uids, ";")
    }

    protected fun getProgramStatus(query: TrackerAPIQuery): String? {
        return when {
            query.commonParams.program != null -> query.programStatus?.toString()
            else -> null
        }
    }

    protected fun getProgramStartDate(query: TrackerAPIQuery): String? {
        return when {
            query.commonParams.program != null -> query.commonParams.startDate
            else -> null
        }
    }
}