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

import android.database.Cursor
import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter
import org.hisp.dhis.android.core.arch.db.stores.internal.SingleParentChildStore
import org.hisp.dhis.android.core.arch.db.stores.internal.StoreFactory.singleParentChildStore
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingList
import org.hisp.dhis.android.core.programstageworkinglist.ProgramStageWorkingListEventDataFilter

internal class ProgramStageWorkingListDataFilterChildrenAppender private constructor(
    private val childStore: SingleParentChildStore<ProgramStageWorkingList, ProgramStageWorkingListEventDataFilter>
) : ChildrenAppender<ProgramStageWorkingList>() {

    override fun appendChildren(m: ProgramStageWorkingList): ProgramStageWorkingList {
        val builder = m.toBuilder()
        val children = childStore.getChildren(m).filter { it.dataItem() != null }
        val queryCriteria = m.programStageQueryCriteria()?.toBuilder()?.dataFilters(children)?.build()
        return builder.programStageQueryCriteria(queryCriteria).build()
    }

    companion object {
        fun create(databaseAdapter: DatabaseAdapter): ChildrenAppender<ProgramStageWorkingList> {
            return ProgramStageWorkingListDataFilterChildrenAppender(
                singleParentChildStore(
                    databaseAdapter,
                    ProgramStageWorkingListEventDataFilterStore.CHILD_PROJECTION
                ) { cursor: Cursor -> ProgramStageWorkingListEventDataFilter.create(cursor) }
            )
        }
    }
}
