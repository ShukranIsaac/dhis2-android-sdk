/*
 * Copyright (c) 2016, University of Oslo
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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.data.database.DbOpenHelper;

import java.util.Date;

public class TrackedEntityStoreImpl implements TrackedEntityStore {
    private static final String INSERT_STATEMENT = "INSERT INTO " + DbOpenHelper.Tables.TRACKED_ENTITY + " (" +
            TrackedEntityContract.Columns.UID + ", " +
            TrackedEntityContract.Columns.CODE + ", " +
            TrackedEntityContract.Columns.NAME + ", " +
            TrackedEntityContract.Columns.DISPLAY_NAME + ", " +
            TrackedEntityContract.Columns.CREATED + ", " +
            TrackedEntityContract.Columns.LAST_UPDATED + ", " +
            TrackedEntityContract.Columns.SHORT_NAME + ", " +
            TrackedEntityContract.Columns.DISPLAY_SHORT_NAME + ", " +
            TrackedEntityContract.Columns.DESCRIPTION + ", " +
            TrackedEntityContract.Columns.DISPLAY_DESCRIPTION +
            ") " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final SQLiteStatement insertRowStatement;

    public TrackedEntityStoreImpl(SQLiteDatabase database) {
        this.insertRowStatement = database.compileStatement(INSERT_STATEMENT);
    }

    @Override
    public long insert(@NonNull String uid, @Nullable String code, @NonNull String name,
                       @Nullable String displayName, @NonNull Date created,
                       @NonNull Date lastUpdated, @Nullable String shortName,
                       @Nullable String displayShortName, @Nullable String description,
                       @Nullable String displayDescription) {

        insertRowStatement.clearBindings();

        insertRowStatement.bindString(1, uid);

        if (code != null) {
            insertRowStatement.bindString(2, code);
        } else {
            insertRowStatement.bindNull(2);
        }
        insertRowStatement.bindString(3, name);
        insertRowStatement.bindString(4, displayName);
        insertRowStatement.bindString(5, BaseIdentifiableObject.DATE_FORMAT.format(created));
        insertRowStatement.bindString(6, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));

        if (shortName != null) {
            insertRowStatement.bindString(7, shortName);
        } else {
            insertRowStatement.bindNull(7);
        }

        if (displayShortName != null) {
            insertRowStatement.bindString(8, displayShortName);
        } else {
            insertRowStatement.bindNull(8);
        }

        if (description != null) {
            insertRowStatement.bindString(9, description);
        } else {
            insertRowStatement.bindNull(9);
        }

        if (displayDescription != null) {
            insertRowStatement.bindString(10, displayDescription);
        } else {
            insertRowStatement.bindNull(10);
        }

        return insertRowStatement.executeInsert();
    }

    @Override
    public void close() {
        insertRowStatement.close();
    }
}
