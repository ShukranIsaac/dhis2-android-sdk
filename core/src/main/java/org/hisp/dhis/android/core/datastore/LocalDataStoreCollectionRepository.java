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

package org.hisp.dhis.android.core.datastore;

import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectWithoutUidStore;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.ReadOnlyCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;

import java.util.Map;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class LocalDataStoreCollectionRepository
        extends ReadOnlyCollectionRepositoryImpl<KeyValuePair, LocalDataStoreCollectionRepository> {

    private final ObjectWithoutUidStore<KeyValuePair> store;

    @Inject
    LocalDataStoreCollectionRepository(final ObjectWithoutUidStore<KeyValuePair> store,
                                       final Map<String, ChildrenAppender<KeyValuePair>> childrenAppenders,
                                       final RepositoryScope scope) {
        super(store, childrenAppenders, scope, new FilterConnectorFactory<>(scope,
                s -> new LocalDataStoreCollectionRepository(store, childrenAppenders, s)));
        this.store = store;
    }

    public LocalDataStoreObjectRepository value(String key) {
        RepositoryScope updatedScope = byKey().eq(key).scope;
        return new LocalDataStoreObjectRepository(store, childrenAppenders, updatedScope, key);
    }

    public StringFilterConnector<LocalDataStoreCollectionRepository> byKey() {
        return cf.string(LocalDataStoreTableInfo.Columns.KEY);
    }

    public StringFilterConnector<LocalDataStoreCollectionRepository> byValue() {
        return cf.string(LocalDataStoreTableInfo.Columns.VALUE);
    }
}