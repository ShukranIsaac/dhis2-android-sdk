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

package org.hisp.dhis.android.core.relationship.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.api.payload.internal.Payload;
import org.hisp.dhis.android.core.arch.call.factories.internal.ListCallFactoryImpl;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.CallFetcher;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.PayloadResourceCallFetcher;
import org.hisp.dhis.android.core.arch.call.internal.GenericCallData;
import org.hisp.dhis.android.core.arch.call.processors.internal.CallProcessor;
import org.hisp.dhis.android.core.arch.call.processors.internal.TransactionalResourceSyncCallProcessor;
import org.hisp.dhis.android.core.arch.handlers.internal.Handler;
import org.hisp.dhis.android.core.common.DataAccess;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.resource.internal.Resource;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
final class RelationshipTypeEndpointCallFactory extends ListCallFactoryImpl<RelationshipType> {

    private final Resource.Type resourceType = Resource.Type.RELATIONSHIP_TYPE;

    private final RelationshipTypeService service;
    private final Handler<RelationshipType> handler;
    private final DHISVersionManager versionManager;

    @Inject
    RelationshipTypeEndpointCallFactory(GenericCallData data,
                                        APICallExecutor apiCallExecutor,
                                        RelationshipTypeService service,
                                        Handler<RelationshipType> handler,
                                        DHISVersionManager versionManager) {
        super(data, apiCallExecutor);
        this.service = service;
        this.handler = handler;
        this.versionManager = versionManager;
    }

    @Override
    protected CallFetcher<RelationshipType> fetcher() {

        return new PayloadResourceCallFetcher<RelationshipType>(data.resourceHandler(), resourceType,
                apiCallExecutor) {
            @Override
            protected retrofit2.Call<Payload<RelationshipType>> getCall(String lastUpdated) {
                String accessDataFilter = null;
                if (!versionManager.is2_29()) {
                    accessDataFilter = "access.data." + DataAccess.read.eq(true).generateString();
                }

                return service.getRelationshipTypes(RelationshipTypeFields.allFields,
                        RelationshipTypeFields.lastUpdated.gt(lastUpdated), accessDataFilter, Boolean.FALSE);
            }
        };
    }

    @Override
    protected CallProcessor<RelationshipType> processor() {
        return new TransactionalResourceSyncCallProcessor<>(
                data,
                handler,
                resourceType
        );
    }
}