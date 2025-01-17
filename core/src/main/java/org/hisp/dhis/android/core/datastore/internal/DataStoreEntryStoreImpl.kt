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
package org.hisp.dhis.android.core.datastore.internal

import android.content.ContentValues
import android.database.Cursor
import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.SQLStatementBuilder
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.SQLStatementBuilderImpl
import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementWrapper
import org.hisp.dhis.android.core.arch.db.stores.binders.internal.WhereStatementBinder
import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectWithoutUidStoreImpl
import org.hisp.dhis.android.core.common.DataColumns
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.datastore.DataStoreEntry
import org.hisp.dhis.android.core.datastore.DataStoreEntryTableInfo

@Suppress("MagicNumber")
internal class DataStoreEntryStoreImpl(
    databaseAdapter: DatabaseAdapter,
    builder: SQLStatementBuilder,
    binder: StatementBinder<DataStoreEntry>,
    whereUpdateBinder: WhereStatementBinder<DataStoreEntry>,
    whereDeleteBinder: WhereStatementBinder<DataStoreEntry>,
    objectFactory: (Cursor) -> DataStoreEntry
) : ObjectWithoutUidStoreImpl<DataStoreEntry>(
    databaseAdapter,
    builder,
    binder,
    whereUpdateBinder,
    whereDeleteBinder,
    objectFactory
),
    DataStoreEntryStore {

    override fun setState(entry: DataStoreEntry, state: State) {
        val updates = ContentValues()
        updates.put(DataColumns.SYNC_STATE, state.toString())
        val whereClause = WhereClauseBuilder()
            .appendKeyStringValue(DataStoreEntryTableInfo.Columns.NAMESPACE, entry.namespace())
            .appendKeyStringValue(DataStoreEntryTableInfo.Columns.KEY, entry.key())
            .build()

        updateWhere(updates, whereClause)
    }

    override fun setStateIfUploading(entry: DataStoreEntry, state: State) {
        val updates = ContentValues()
        updates.put(DataColumns.SYNC_STATE, state.toString())
        val whereClause = WhereClauseBuilder()
            .appendKeyStringValue(DataStoreEntryTableInfo.Columns.NAMESPACE, entry.namespace())
            .appendKeyStringValue(DataStoreEntryTableInfo.Columns.KEY, entry.key())
            .appendKeyStringValue(DataColumns.SYNC_STATE, State.UPLOADING.toString())
            .build()

        updateWhere(updates, whereClause)
    }

    companion object {
        private val BINDER: StatementBinder<DataStoreEntry> = StatementBinder { o, w ->
            w.bind(1, o.namespace())
            w.bind(2, o.key())
            w.bind(3, o.value())
            w.bind(4, o.syncState())
            w.bind(5, o.deleted())
        }

        private val WHERE_UPDATE_BINDER = WhereStatementBinder { o: DataStoreEntry, w: StatementWrapper ->
            w.bind(6, o.namespace())
            w.bind(7, o.key())
        }

        private val WHERE_DELETE_BINDER = WhereStatementBinder { o: DataStoreEntry, w: StatementWrapper ->
            w.bind(1, o.namespace())
            w.bind(2, o.key())
        }

        @JvmStatic
        fun create(databaseAdapter: DatabaseAdapter): DataStoreEntryStore {
            val tableInfo = DataStoreEntryTableInfo.TABLE_INFO
            val statementBuilder: SQLStatementBuilder = SQLStatementBuilderImpl(
                tableInfo.name(), tableInfo.columns().all(),
                tableInfo.columns().whereUpdate()
            )
            return DataStoreEntryStoreImpl(
                databaseAdapter,
                statementBuilder,
                BINDER,
                WHERE_UPDATE_BINDER,
                WHERE_DELETE_BINDER
            ) { cursor: Cursor -> DataStoreEntry.create(cursor) }
        }
    }
}
