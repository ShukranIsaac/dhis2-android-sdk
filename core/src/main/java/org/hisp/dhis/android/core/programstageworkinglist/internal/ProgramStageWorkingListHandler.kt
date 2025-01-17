/*
 *  Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.android.core.programstageworkinglist.internal

import dagger.Reusable
import javax.inject.Inject
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore
import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction
import org.hisp.dhis.android.core.arch.handlers.internal.HandlerWithTransformer
import org.hisp.dhis.android.core.arch.handlers.internal.IdentifiableHandlerImpl
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingList
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingListAttributeValueFilter
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingListEventDataFilter

@Reusable
internal class ProgramStageWorkingListHandler @Inject constructor(
    store: IdentifiableObjectStore<ProgramStageWorkingList>,
    private val eventDataFilterHandler: HandlerWithTransformer<ProgramStageWorkingListEventDataFilter>,
    private val attributeValueFilterHandler: HandlerWithTransformer<ProgramStageWorkingListAttributeValueFilter>
) : IdentifiableHandlerImpl<ProgramStageWorkingList>(store) {

    override fun beforeCollectionHandled(
        oCollection: Collection<ProgramStageWorkingList>
    ): Collection<ProgramStageWorkingList> {
        store.delete()
        return super.beforeCollectionHandled(oCollection)
    }

    override fun afterObjectHandled(o: ProgramStageWorkingList, action: HandleAction) {
        if (action !== HandleAction.Delete && o.programStageQueryCriteria() != null) {
            o.programStageQueryCriteria()?.let { criteria ->
                eventDataFilterHandler.handleMany(
                    criteria.dataFilters()
                ) { edf -> edf.toBuilder().programStageWorkingList(o.uid()).build() }
                attributeValueFilterHandler.handleMany(
                    criteria.attributeValueFilters()
                ) { avf -> avf.toBuilder().programStageWorkingList(o.uid()).build() }
            }
        }
    }
}
