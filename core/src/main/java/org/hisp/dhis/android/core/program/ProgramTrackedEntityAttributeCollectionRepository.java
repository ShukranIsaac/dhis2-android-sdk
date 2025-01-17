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

package org.hisp.dhis.android.core.program;

import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.ReadOnlyNameableCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.BooleanFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.IntegerFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeTableInfo.Columns;
import org.hisp.dhis.android.core.program.internal.ProgramTrackedEntityAttributeFields;

import java.util.Map;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public final class ProgramTrackedEntityAttributeCollectionRepository
        extends ReadOnlyNameableCollectionRepositoryImpl
        <ProgramTrackedEntityAttribute, ProgramTrackedEntityAttributeCollectionRepository> {

    @Inject
    ProgramTrackedEntityAttributeCollectionRepository(
            final IdentifiableObjectStore<ProgramTrackedEntityAttribute> store,
            final Map<String, ChildrenAppender<ProgramTrackedEntityAttribute>> childrenAppenders,
            final RepositoryScope scope) {
        super(store, childrenAppenders, scope, new FilterConnectorFactory<>(scope,
                s -> new ProgramTrackedEntityAttributeCollectionRepository(store, childrenAppenders, s)));
    }

    public BooleanFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> byMandatory() {
        return cf.bool(Columns.MANDATORY);
    }

    public StringFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> byTrackedEntityAttribute() {
        return cf.string(Columns.TRACKED_ENTITY_ATTRIBUTE);
    }

    public BooleanFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> byAllowFutureDate() {
        return cf.bool(Columns.ALLOW_FUTURE_DATE);
    }

    public BooleanFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> byDisplayInList() {
        return cf.bool(Columns.DISPLAY_IN_LIST);
    }

    public StringFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> byProgram() {
        return cf.string(Columns.PROGRAM);
    }

    public IntegerFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> bySortOrder() {
        return cf.integer(Columns.SORT_ORDER);
    }

    public BooleanFilterConnector<ProgramTrackedEntityAttributeCollectionRepository> bySearchable() {
        return cf.bool(Columns.SEARCHABLE);
    }

    public ProgramTrackedEntityAttributeCollectionRepository withRenderType() {
        return cf.withChild(ProgramTrackedEntityAttributeFields.RENDER_TYPE);
    }

    public ProgramTrackedEntityAttributeCollectionRepository orderBySortOrder(
            RepositoryScope.OrderByDirection direction) {
        return cf.withOrderBy(Columns.SORT_ORDER, direction);
    }
}
