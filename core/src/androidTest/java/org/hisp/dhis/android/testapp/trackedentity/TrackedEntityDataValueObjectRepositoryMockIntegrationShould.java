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

package org.hisp.dhis.android.testapp.trackedentity;

import org.hisp.dhis.android.core.data.database.SyncedDatabaseMockIntegrationShould;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueCreateProjection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class TrackedEntityDataValueObjectRepositoryMockIntegrationShould extends SyncedDatabaseMockIntegrationShould {

    @Test
    public void update_value() throws D2Error {
        String value = "new_value";

        TrackedEntityDataValueObjectRepository repository = objectRepository();

        repository.set(value);
        assertThat(repository.get().value(), is(value));

        repository.delete();
    }

    // TODO Uncomment when delete method is ready.
    // @Test
    public void return_that_a_value_exists_only_if_it_has_been_created() throws D2Error {
        assertThat(d2.trackedEntityModule().trackedEntityDataValues
                .value("event1", "ebaJjqltK5N").exists(), is(false));

        TrackedEntityDataValueObjectRepository repository = objectRepository();
        assertThat(repository.exists(), is(true));

        repository.delete();
    }

    private TrackedEntityDataValueObjectRepository objectRepository() throws D2Error {
        TrackedEntityDataValue dataValue = d2.trackedEntityModule().trackedEntityDataValues.add(
                TrackedEntityDataValueCreateProjection.create(
                        "event1", "ebaJjqltK5N", "created_value"));

        return d2.trackedEntityModule().trackedEntityDataValues.value(dataValue.event(), dataValue.dataElement());
    }
}