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

package org.hisp.dhis.android.core.configuration;

import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

public class ConfigurationStoreImpl implements ConfigurationStore {

    private static final long CONFIGURATION_ID = 1L;

    public static final String INSERT_STATEMENT = "INSERT INTO " + ConfigurationModel.CONFIGURATION
            + " (" + ConfigurationModel.Columns.ID + ", " +
            ConfigurationModel.Columns.SERVER_URL + ") " +
            "VALUES(?,?);";

    private static final String[] PROJECTION = {
            ConfigurationModel.Columns.ID,
            ConfigurationModel.Columns.SERVER_URL
    };

    private final DatabaseAdapter databaseAdapter;
    private final SQLiteStatement insertStatement;


    public ConfigurationStoreImpl(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        this.insertStatement = databaseAdapter.compileStatement(INSERT_STATEMENT);
    }

    @Override
    public long save(@NonNull String serverUrl) {

        delete(); // Delete all rows from table. We only allow one row.
        insertStatement.clearBindings();
        sqLiteBind(insertStatement, 1, CONFIGURATION_ID);
        sqLiteBind(insertStatement, 2, serverUrl);
        databaseAdapter.executeInsert(ConfigurationModel.CONFIGURATION, insertStatement);

        return 1;
    }

    @Nullable
    @Override
    public ConfigurationModel query() {

        String sqlQuery = SQLiteQueryBuilder.buildQueryString(false, ConfigurationModel.CONFIGURATION,
                PROJECTION, ConfigurationModel.Columns.ID + " = " + CONFIGURATION_ID,
                null, null, null, null);

        Cursor queryCursor = databaseAdapter.query(sqlQuery);

        ConfigurationModel configuration = null;

        try {
            if (queryCursor != null && queryCursor.getCount() > 0) {
                queryCursor.moveToFirst();

                configuration = ConfigurationModel.create(queryCursor);
            }
        } finally {
            if (queryCursor != null) {
                queryCursor.close();
            }
        }

        return configuration;
    }

    @Override
    public int delete() {
        return databaseAdapter.delete(ConfigurationModel.CONFIGURATION, null, null);
    }
}
