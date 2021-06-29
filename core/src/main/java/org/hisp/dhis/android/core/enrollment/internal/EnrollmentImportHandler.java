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

package org.hisp.dhis.android.core.enrollment.internal;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction;
import org.hisp.dhis.android.core.arch.helpers.internal.EnumHelper;
import org.hisp.dhis.android.core.common.DataColumns;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.internal.DataStatePropagator;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentInternalAccessor;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.internal.EventImportHandler;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.imports.internal.BaseImportSummaryHelper;
import org.hisp.dhis.android.core.imports.internal.EnrollmentImportSummary;
import org.hisp.dhis.android.core.imports.internal.EventImportSummaries;
import org.hisp.dhis.android.core.imports.internal.ImportConflict;
import org.hisp.dhis.android.core.imports.internal.TrackerImportConflictParser;
import org.hisp.dhis.android.core.imports.internal.TrackerImportConflictStore;
import org.hisp.dhis.android.core.note.Note;
import org.hisp.dhis.android.core.note.NoteTableInfo;
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.Reusable;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.getState;

@Reusable
@SuppressWarnings("PMD.ExcessiveImports")
public class EnrollmentImportHandler {
    private final EnrollmentStore enrollmentStore;
    private final TrackedEntityInstanceStore trackedEntityInstanceStore;
    private final IdentifiableObjectStore<Note> noteStore;
    private final EventImportHandler eventImportHandler;
    private final TrackerImportConflictStore trackerImportConflictStore;
    private final TrackerImportConflictParser trackerImportConflictParser;
    private final DataStatePropagator dataStatePropagator;

    @Inject
    public EnrollmentImportHandler(@NonNull EnrollmentStore enrollmentStore,
                                   @NonNull TrackedEntityInstanceStore trackedEntityInstanceStore,
                                   @NonNull IdentifiableObjectStore<Note> noteStore,
                                   @NonNull EventImportHandler eventImportHandler,
                                   @NonNull TrackerImportConflictStore trackerImportConflictStore,
                                   @NonNull TrackerImportConflictParser trackerImportConflictParser,
                                   @NonNull DataStatePropagator dataStatePropagator) {
        this.enrollmentStore = enrollmentStore;
        this.trackedEntityInstanceStore = trackedEntityInstanceStore;
        this.noteStore = noteStore;
        this.eventImportHandler = eventImportHandler;
        this.trackerImportConflictStore = trackerImportConflictStore;
        this.trackerImportConflictParser = trackerImportConflictParser;
        this.dataStatePropagator = dataStatePropagator;
    }

    public void handleEnrollmentImportSummary(List<EnrollmentImportSummary> enrollmentImportSummaries,
                                              List<Enrollment> enrollments,
                                              String teiUid) {
        State parentState = null;

        if (enrollmentImportSummaries != null) {
            for (EnrollmentImportSummary enrollmentImportSummary : enrollmentImportSummaries) {
                String enrollmentUid = enrollmentImportSummary == null ? null : enrollmentImportSummary.reference();

                if (enrollmentUid == null) {
                    break;
                }

                State state = getState(enrollmentImportSummary.status());
                trackerImportConflictStore.deleteEnrollmentConflicts(enrollmentUid);

                HandleAction handleAction = enrollmentStore.setStateOrDelete(enrollmentUid, state);

                if (state.equals(State.ERROR) || state.equals(State.WARNING)) {
                    parentState = parentState == State.ERROR ? State.ERROR : state;
                    dataStatePropagator.resetUploadingEventStates(enrollmentUid);
                }

                if (handleAction != HandleAction.Delete) {
                    storeEnrollmentImportConflicts(enrollmentImportSummary, teiUid);
                    handleNoteImportSummary(enrollmentUid, state);
                    handleEventImportSummaries(enrollmentImportSummary, enrollments, teiUid);
                }

            }
        }

        List<String> processedEnrollments = BaseImportSummaryHelper.getReferences(enrollmentImportSummaries);
        for (Enrollment enrollment : enrollments) {
            if (!processedEnrollments.contains(enrollment.uid())) {
                State state = State.TO_UPDATE;
                enrollmentStore.setStateOrDelete(enrollment.uid(), state);
                parentState = parentState == State.ERROR || parentState == State.WARNING ? parentState : state;
                dataStatePropagator.resetUploadingEventStates(enrollment.uid());

                trackerImportConflictStore.deleteEnrollmentConflicts(enrollment.uid());
            }
        }

        updateParentState(parentState, teiUid);
    }

    private void handleEventImportSummaries(EnrollmentImportSummary enrollmentImportSummary,
                                            List<Enrollment> enrollments,
                                            String teiUid) {

        if (enrollmentImportSummary.events() != null) {
            EventImportSummaries eventImportSummaries = enrollmentImportSummary.events();

            if (eventImportSummaries.importSummaries() != null) {
                eventImportHandler.handleEventImportSummaries(
                        eventImportSummaries.importSummaries(),
                        getEvents(enrollmentImportSummary.reference(), enrollments),
                        enrollmentImportSummary.reference(),
                        teiUid);
            }
        }
    }

    private void handleNoteImportSummary(String enrollmentUid, State state) {
        State newNoteState = state.equals(State.SYNCED) ? State.SYNCED : State.TO_POST;
        String whereClause = new WhereClauseBuilder()
                .appendInKeyStringValues(
                        DataColumns.STATE, EnumHelper.asStringList(State.uploadableStatesIncludingError()))
                .appendKeyStringValue(NoteTableInfo.Columns.ENROLLMENT, enrollmentUid).build();
        List<Note> notes = noteStore.selectWhere(whereClause);
        for (Note note : notes) {
            noteStore.update(note.toBuilder().state(newNoteState).build());
        }
    }

    private void storeEnrollmentImportConflicts(EnrollmentImportSummary enrollmentImportSummary,
                                                String teiUid) {
        List<TrackerImportConflict> trackerImportConflicts = new ArrayList<>();
        if (enrollmentImportSummary.description() != null) {
            trackerImportConflicts.add(getConflictBuilder(teiUid, enrollmentImportSummary)
                    .conflict(enrollmentImportSummary.description())
                    .displayDescription(enrollmentImportSummary.description())
                    .value(enrollmentImportSummary.reference())
                    .build());
        }

        if (enrollmentImportSummary.conflicts() != null) {
            for (ImportConflict importConflict : enrollmentImportSummary.conflicts()) {
                trackerImportConflicts.add(trackerImportConflictParser
                        .getEnrollmentConflict(importConflict, getConflictBuilder(teiUid, enrollmentImportSummary)));
            }
        }

        for (TrackerImportConflict trackerImportConflict : trackerImportConflicts) {
            trackerImportConflictStore.insert(trackerImportConflict);
        }
    }


    private void updateParentState(State parentState, String teiUid) {
        if (parentState != null && teiUid != null) {
            trackedEntityInstanceStore.setState(teiUid, parentState);
        }
    }

    private List<Event> getEvents(String enrollmentUid,
                                  List<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            if (enrollmentUid.equals(enrollment.uid())) {
                return EnrollmentInternalAccessor.accessEvents(enrollment);
            }
        }
        return Collections.emptyList();
    }

    private TrackerImportConflict.Builder getConflictBuilder(String trackedEntityInstanceUid,
                                                             EnrollmentImportSummary enrollmentImportSummary) {
        return TrackerImportConflict.builder()
                .trackedEntityInstance(trackedEntityInstanceUid)
                .enrollment(enrollmentImportSummary.reference())
                .tableReference(EnrollmentTableInfo.TABLE_INFO.name())
                .status(enrollmentImportSummary.status())
                .created(new Date());
    }
}