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
package org.hisp.dhis.android.core.relationship

import org.hisp.dhis.android.core.arch.handlers.internal.TwoWayTransformer
import org.hisp.dhis.android.core.common.ObjectWithUid

internal object NewTrackerImporterRelationshipTransformer :
    TwoWayTransformer<Relationship, NewTrackerImporterRelationship> {
    override fun transform(o: Relationship): NewTrackerImporterRelationship {
        return NewTrackerImporterRelationship.builder()
            .id(o.id())
            .uid(o.uid())
            .relationshipType(o.relationshipType())
            .relationshipName(o.name())
            .createdAt(o.created())
            .updatedAt(o.lastUpdated())
            .from(transformRelationshipItem(o.from()))
            .to(transformRelationshipItem(o.to()))
            .deleted(o.deleted())
            .syncState(o.syncState())
            .build()
    }

    override fun deTransform(t: NewTrackerImporterRelationship): Relationship {
        return Relationship.builder()
            .id(t.id())
            .uid(t.uid())
            .relationshipType(t.relationshipType())
            .name(t.relationshipName())
            .created(t.createdAt())
            .lastUpdated(t.updatedAt())
            .from(deTransformRelationshipItem(t.from()))
            .to(deTransformRelationshipItem(t.to()))
            .deleted(t.deleted())
            .syncState(t.syncState())
            .build()
    }

    private fun transformRelationshipItem(item: RelationshipItem?): NewTrackerImporterRelationshipItem? {
        return item?.let {
            val builder = NewTrackerImporterRelationshipItem.builder()
                .relationship(item.relationship()?.uid())
                .relationshipItemType(item.relationshipItemType())

            when {
                item.hasTrackedEntityInstance() ->
                    builder.trackedEntity(
                        NewTrackerImporterRelationshipItemTrackedEntity.builder().trackedEntity(item.elementUid())
                            .build()
                    ).build()
                item.hasEnrollment() ->
                    builder.enrollment(
                        NewTrackerImporterRelationshipItemEnrollment.builder().enrollment(item.elementUid()).build()
                    ).build()
                item.hasEvent() ->
                    builder.event(
                        NewTrackerImporterRelationshipItemEvent.builder().event(item.elementUid()).build()
                    ).build()
                else -> null
            }
        }
    }

    private fun deTransformRelationshipItem(item: NewTrackerImporterRelationshipItem?): RelationshipItem? {
        return item?.let {
            val builder = RelationshipItem.builder()
                .relationship(item.relationship()?.let { ObjectWithUid.create(it) })
                .relationshipItemType(item.relationshipItemType())

            when {
                item.trackedEntity() != null ->
                    builder.trackedEntityInstance(
                        RelationshipItemTrackedEntityInstance.builder()
                            .trackedEntityInstance(item.trackedEntity()?.trackedEntity())
                            .build()
                    ).build()
                item.enrollment() != null ->
                    builder.enrollment(
                        RelationshipItemEnrollment.builder()
                            .enrollment(item.enrollment()?.enrollment())
                            .build()
                    ).build()
                item.event() != null ->
                    builder.event(
                        RelationshipItemEvent.builder()
                            .event(item.event()?.event())
                            .build()
                    ).build()
                else -> null
            }
        }
    }
}
