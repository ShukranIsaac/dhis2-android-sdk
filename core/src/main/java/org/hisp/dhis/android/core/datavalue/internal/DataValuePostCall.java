/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
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

package org.hisp.dhis.android.core.datavalue.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.imports.internal.DataValueImportSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;

@Reusable
public final class DataValuePostCall implements Callable<DataValueImportSummary> {

    private final DataValueService dataValueService;
    private final DataValueStore dataValueStore;
    private final APICallExecutor apiCallExecutor;

    @Inject
    DataValuePostCall(@NonNull DataValueService dataValueService,
                      @NonNull DataValueStore dataValueSetStore,
                      APICallExecutor apiCallExecutor) {

        this.dataValueService = dataValueService;
        this.dataValueStore = dataValueSetStore;
        this.apiCallExecutor = apiCallExecutor;
    }

    @Override
    public DataValueImportSummary call() throws Exception {
        Collection<DataValue> toPostDataValues = new ArrayList<>();

        appendPostableDataValues(toPostDataValues);
        appendUpdatableDataValues(toPostDataValues);

        if (toPostDataValues.isEmpty()) {
            return DataValueImportSummary.EMPTY;
        }

        DataValueSet dataValueSet = new DataValueSet(toPostDataValues);

        DataValueImportSummary dataValueImportSummary = apiCallExecutor.executeObjectCall(
                dataValueService.postDataValues(dataValueSet));

        handleImportSummary(dataValueSet, dataValueImportSummary);

        return dataValueImportSummary;
    }

    private void appendPostableDataValues(Collection<DataValue> dataValues) {
        dataValues.addAll(dataValueStore.getDataValuesWithState(State.TO_POST));
    }

    private void appendUpdatableDataValues(Collection<DataValue> dataValues) {
        dataValues.addAll(dataValueStore.getDataValuesWithState(State.TO_UPDATE));
    }

    private void handleImportSummary(DataValueSet dataValueSet, DataValueImportSummary dataValueImportSummary) {

        DataValueImportHandler dataValueImportHandler =
                new DataValueImportHandler(dataValueStore);

        dataValueImportHandler.handleImportSummary(dataValueSet, dataValueImportSummary);
    }
}