/*
 *  Copyright (c) 2004-2021, University of Oslo
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
package org.hisp.dhis.android.core.tracker.importer.internal

import dagger.Reusable
import io.reactivex.Single
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableDataObjectStore
import org.hisp.dhis.android.core.enrollment.NewTrackerImporterEnrollment
import org.hisp.dhis.android.core.event.NewTrackerImporterEvent
import org.hisp.dhis.android.core.fileresource.FileResource
import org.hisp.dhis.android.core.fileresource.internal.FileResourceHelper
import org.hisp.dhis.android.core.fileresource.internal.FileResourcePostCall
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterTrackedEntity
import org.hisp.dhis.android.core.trackedentity.NewTrackerImporterTrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.internal.NewTrackerImporterPayload
import org.hisp.dhis.android.core.trackedentity.internal.NewTrackerImporterPayloadWrapper
import javax.inject.Inject

@Reusable
internal class TrackerImporterFileResourcesPostCall @Inject internal constructor(
    private val fileResourceStore: IdentifiableDataObjectStore<FileResource>,
    private val fileResourcePostCall: FileResourcePostCall,
    private val fileResourceHelper: FileResourceHelper
) {

    fun uploadFileResources(
        payloadWrapper: NewTrackerImporterPayloadWrapper
    ): Single<NewTrackerImporterPayloadWrapper> {
        return Single.create { emitter ->
            val fileResources = fileResourceStore.getUploadableSyncStatesIncludingError()

            if (fileResources.isEmpty()) {
                emitter.onSuccess(payloadWrapper)
            } else {
                emitter.onSuccess(
                    NewTrackerImporterPayloadWrapper(
                        deleted = uploadPayloadFileResources(payloadWrapper.deleted, fileResources),
                        updated = uploadPayloadFileResources(payloadWrapper.updated, fileResources)
                    )
                )
            }
        }
    }

    private fun uploadPayloadFileResources(
        payload: NewTrackerImporterPayload,
        fileResources: List<FileResource>
    ): NewTrackerImporterPayload {
        val uploadedAttributes = uploadAttributes(payload.trackedEntities, payload.enrollments, fileResources)
        val uploadedDataValues = uploadDataValues(payload.events, fileResources)

        return payload.copy(
            trackedEntities = uploadedAttributes.first.toMutableList(),
            enrollments = uploadedAttributes.second.toMutableList(),
            events = uploadedDataValues.first.toMutableList(),
            fileResources = (uploadedAttributes.third + uploadedDataValues.second).toMutableList()
        )
    }

    private fun uploadAttributes(
        entities: List<NewTrackerImporterTrackedEntity>,
        enrollments: List<NewTrackerImporterEnrollment>,
        fileResources: List<FileResource>
    ): Triple<List<NewTrackerImporterTrackedEntity>, List<NewTrackerImporterEnrollment>, List<FileResource>> {
        val uploadedFileResources = mutableMapOf<String, FileResource>()

        val successfulEntities: List<NewTrackerImporterTrackedEntity> = entities.mapNotNull { entity ->
            catchErrorToNull {
                val updatedAttributes = getUpdatedAttributes(
                    attributeValues = entity.trackedEntityAttributeValues(),
                    fileResources = fileResources,
                    uploadedFileResources = uploadedFileResources
                )
                entity.toBuilder().trackedEntityAttributeValues(updatedAttributes).build()
            }
        }

        val successfulEnrollments: List<NewTrackerImporterEnrollment> = enrollments.mapNotNull { enrollment ->
            catchErrorToNull {
                val updatedAttributes = getUpdatedAttributes(
                    attributeValues = enrollment.attributes(),
                    fileResources = fileResources,
                    uploadedFileResources = uploadedFileResources
                )
                enrollment.toBuilder().attributes(updatedAttributes).build()
            }
        }

        return Triple(successfulEntities, successfulEnrollments, uploadedFileResources.values.toList())
    }

    private fun getUpdatedAttributes(
        attributeValues: List<NewTrackerImporterTrackedEntityAttributeValue>?,
        fileResources: List<FileResource>,
        uploadedFileResources: MutableMap<String, FileResource>
    ): List<NewTrackerImporterTrackedEntityAttributeValue>? {
        return attributeValues?.map { attributeValue ->
            fileResourceHelper.findAttributeFileResource(attributeValue, fileResources)?.let { fileResource ->
                val uploadedFileResource = uploadedFileResources[fileResource.uid()]

                val newUid = if (uploadedFileResource != null) {
                    uploadedFileResource.uid()!!
                } else {
                    fileResourcePostCall.uploadFileResource(fileResource)?.also {
                        uploadedFileResources[fileResource.uid()!!] = fileResource.toBuilder().uid(it).build()
                    }
                }
                attributeValue.toBuilder().value(newUid).build()
            } ?: attributeValue
        }
    }

    private fun uploadDataValues(
        events: List<NewTrackerImporterEvent>,
        fileResources: List<FileResource>
    ): Pair<List<NewTrackerImporterEvent>, List<FileResource>> {
        val uploadedFileResources = mutableListOf<FileResource>()

        val successfulEvents = events.mapNotNull { event ->
            catchErrorToNull {
                val updatedDataValues = event.trackedEntityDataValues()?.map { dataValue ->
                    fileResourceHelper.findDataValueFileResource(dataValue, fileResources)?.let { fileResource ->
                        val newUid = fileResourcePostCall.uploadFileResource(fileResource)?.also {
                            uploadedFileResources.add(fileResource.toBuilder().uid(it).build())
                        }
                        dataValue.toBuilder().value(newUid).build()
                    } ?: dataValue
                }
                event.toBuilder().trackedEntityDataValues(updatedDataValues).build()
            }
        }

        return Pair(successfulEvents, uploadedFileResources)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun <T> catchErrorToNull(f: () -> T): T? {
        return try {
            f()
        } catch (e: java.lang.RuntimeException) {
            null
        } catch (e: RuntimeException) {
            null
        } catch (e: D2Error) {
            null
        }
    }
}
