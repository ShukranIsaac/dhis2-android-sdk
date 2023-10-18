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
package org.hisp.dhis.android.core.imports

import dagger.Reusable
import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppenderGetter
import org.hisp.dhis.android.core.arch.repositories.collection.internal.ReadOnlyCollectionRepositoryImpl
import org.hisp.dhis.android.core.arch.repositories.filters.internal.DateFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.EnumFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.imports.internal.TrackerImportConflictStore
import javax.inject.Inject

@Reusable
class TrackerImportConflictCollectionRepository @Inject internal constructor(
    store: TrackerImportConflictStore,
    databaseAdapter: DatabaseAdapter,
    scope: RepositoryScope,
) : ReadOnlyCollectionRepositoryImpl<TrackerImportConflict, TrackerImportConflictCollectionRepository>(
    store,
    databaseAdapter,
    childrenAppenders,
    scope,
    FilterConnectorFactory(
        scope,
    ) { s: RepositoryScope ->
        TrackerImportConflictCollectionRepository(
            store,
            databaseAdapter,
            s,
        )
    },
) {
    fun byConflict(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.CONFLICT)
    }

    fun byValue(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.VALUE)
    }

    fun byTrackedEntityInstanceUid(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.TRACKED_ENTITY_INSTANCE)
    }

    fun byEnrollmentUid(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.ENROLLMENT)
    }

    fun byEventUid(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.EVENT)
    }

    fun byTableReference(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.TABLE_REFERENCE)
    }

    fun byErrorCode(): StringFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.string(TrackerImportConflictTableInfo.Columns.ERROR_CODE)
    }

    fun byStatus(): EnumFilterConnector<TrackerImportConflictCollectionRepository, ImportStatus> {
        return cf.enumC(TrackerImportConflictTableInfo.Columns.STATUS)
    }

    fun byCreated(): DateFilterConnector<TrackerImportConflictCollectionRepository> {
        return cf.date(TrackerImportConflictTableInfo.Columns.CREATED)
    }

    internal companion object {
        val childrenAppenders: ChildrenAppenderGetter<TrackerImportConflict> = emptyMap()
    }
}