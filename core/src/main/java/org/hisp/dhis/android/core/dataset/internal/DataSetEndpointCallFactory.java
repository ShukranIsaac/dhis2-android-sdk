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

package org.hisp.dhis.android.core.dataset.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.api.payload.internal.Payload;
import org.hisp.dhis.android.core.arch.call.factories.internal.UidsCallFactoryImpl;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.CallFetcher;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.UidsNoResourceCallFetcher;
import org.hisp.dhis.android.core.arch.call.internal.GenericCallData;
import org.hisp.dhis.android.core.arch.call.processors.internal.CallProcessor;
import org.hisp.dhis.android.core.arch.call.processors.internal.TransactionalResourceSyncCallProcessor;
import org.hisp.dhis.android.core.arch.call.queries.internal.UidsQuery;
import org.hisp.dhis.android.core.arch.handlers.internal.Handler;
import org.hisp.dhis.android.core.common.internal.DataAccessFields;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.resource.internal.Resource;

import java.util.Set;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
final class DataSetEndpointCallFactory extends UidsCallFactoryImpl<DataSet> {

    private static final int MAX_UID_LIST_SIZE = 50;

    private final DataSetService dataSetService;
    private final Handler<DataSet> dataSetHandler;
    private final Resource.Type resourceType = Resource.Type.DATA_SET;

    @Inject
    DataSetEndpointCallFactory(GenericCallData data,
                               APICallExecutor apiCallExecutor,
                               DataSetService dataSetService,
                               Handler<DataSet> dataSetHandler) {
        super(data, apiCallExecutor);
        this.dataSetService = dataSetService;
        this.dataSetHandler = dataSetHandler;
    }

    @Override
    protected CallFetcher<DataSet> fetcher(Set<String> uids) {

        return new UidsNoResourceCallFetcher<DataSet>(uids, MAX_UID_LIST_SIZE, apiCallExecutor) {
            String accessDataReadFilter = "access.data." + DataAccessFields.read.eq(true).generateString();

            @Override
            protected retrofit2.Call<Payload<DataSet>> getCall(UidsQuery query) {
                return dataSetService.getDataSets(
                        DataSetFields.allFields,
                        DataSetFields.uid.in(query.uids()),
                        accessDataReadFilter,
                        Boolean.FALSE);
            }
        };

    }

    @Override
    protected CallProcessor<DataSet> processor() {
        return new TransactionalResourceSyncCallProcessor<>(
                data,
                dataSetHandler,
                resourceType
        );
    }
}