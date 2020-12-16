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

package org.hisp.dhis.android.core.trackedentity.search;

import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.arch.helpers.DateUtils;
import org.hisp.dhis.android.core.common.AssignedUserMode;
import org.hisp.dhis.android.core.event.EventStatus;

import java.util.Date;
import java.util.List;

@AutoValue
abstract class TrackedEntityInstanceQueryEventFilter {

    @Nullable
    public abstract String programStage();

    @Nullable
    public abstract List<EventStatus> eventStatus();

    @Nullable
    public abstract AssignedUserMode assignedUserMode();

    @Nullable
    public abstract Date eventStartDate();

    @Nullable
    public abstract Date eventEndDate();

    public String formattedEventStartDate() {
        return formatDate(eventStartDate());
    }

    public String formattedEventEndDate() {
        return formatDate(eventEndDate());
    }

    private String formatDate(Date date) {
        return date == null ? null : DateUtils.SIMPLE_DATE_FORMAT.format(date);
    }

    abstract TrackedEntityInstanceQueryEventFilter.Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_TrackedEntityInstanceQueryEventFilter.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {

        public abstract Builder programStage(String programStage);

        public abstract Builder eventStatus(List<EventStatus> eventStatus);

        public abstract Builder eventStartDate(Date eventStartDate);

        public abstract Builder eventEndDate(Date eventEndDate);

        public abstract Builder assignedUserMode(AssignedUserMode assignedUserMode);

        public abstract TrackedEntityInstanceQueryEventFilter build();
    }
}