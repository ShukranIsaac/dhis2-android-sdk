/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.trackedentity;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.BaseModel;
import org.hisp.dhis.android.core.common.ObjectWithoutUidStoreImpl;
import org.hisp.dhis.android.core.common.SQLStatementBuilder;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeReservedValueModel.Columns;

import java.util.Date;

import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeReservedValueModel.factory;

public final class TrackedEntityAttributeReservedValueStore
        extends ObjectWithoutUidStoreImpl<TrackedEntityAttributeReservedValueModel>
        implements TrackedEntityAttributeReservedValueStoreInterface {

    private TrackedEntityAttributeReservedValueStore(DatabaseAdapter databaseAdapter,
                                                     SQLiteStatement insertStatement,
                                                     SQLiteStatement updateWhereStatement,
                                                     SQLStatementBuilder builder) {
        super(databaseAdapter, insertStatement, updateWhereStatement, builder);
    }

    @Override
    public void deleteExpired(@NonNull Date serverDate) throws RuntimeException {
        super.deleteWhereClause(Columns.EXPIRY_DATE
                + " < date('" + BaseIdentifiableObject.DATE_FORMAT.format(serverDate) + "')");
    }

    @Override
    public TrackedEntityAttributeReservedValueModel popOne(@NonNull String ownerUid,
                                                           @NonNull String organisationUnitUid) {
        return popOneWhere(factory, where(ownerUid, organisationUnitUid));
    }

    @Override
    public int count(@NonNull String ownerUid, @NonNull String organisationUnitUid) {
        return countWhere(where(ownerUid, organisationUnitUid));
    }

    private String where(@NonNull String ownerUid,
                         @NonNull String organisationUnitUid) {
        return Columns.OWNER_UID + "='" + ownerUid + "' AND " +
                Columns.ORGANISATION_UNIT + "='" + organisationUnitUid + "';";
    }

    public static TrackedEntityAttributeReservedValueStoreInterface
    create(DatabaseAdapter databaseAdapter) {
        BaseModel.Columns columns = new TrackedEntityAttributeReservedValueModel.Columns();

        SQLStatementBuilder statementBuilder = new SQLStatementBuilder(
                TrackedEntityAttributeReservedValueModel.TABLE, columns);

        return new TrackedEntityAttributeReservedValueStore(
                databaseAdapter,
                databaseAdapter.compileStatement(statementBuilder.insert()),
                databaseAdapter.compileStatement(statementBuilder.updateWhere()),
                statementBuilder);
    }
}