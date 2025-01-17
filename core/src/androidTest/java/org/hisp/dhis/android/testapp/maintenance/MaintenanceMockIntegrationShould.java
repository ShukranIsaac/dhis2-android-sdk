/*
 *  Copyright (c) 2004-2022, University of Oslo
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

package org.hisp.dhis.android.testapp.maintenance;

import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation;
import org.hisp.dhis.android.core.option.OptionSetTableInfo;
import org.hisp.dhis.android.core.option.OptionTableInfo;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(D2JunitRunner.class)
public class MaintenanceMockIntegrationShould extends BaseMockIntegrationTestFullDispatcher {

    @Test
    public void allow_access_to_foreign_key_violations() {
        List<ForeignKeyViolation> violations = d2.maintenanceModule().foreignKeyViolations().blockingGet();
        assertThat(violations.size()).isEqualTo(3);

        ForeignKeyViolation optionViolation = ForeignKeyViolation.builder()
                .toTable(OptionSetTableInfo.TABLE_INFO.name())
                .toColumn(IdentifiableColumns.UID)
                .fromTable(OptionTableInfo.TABLE_INFO.name())
                .fromColumn(OptionTableInfo.Columns.OPTION_SET)
                .notFoundValue("non_existent_option_set_uid")
                .fromObjectUid("non_existent_option_uid")
                .build();

        List<ForeignKeyViolation> violationsToCompare = new ArrayList<>();
        for (ForeignKeyViolation violation : violations) {
            violationsToCompare.add(violation.toBuilder().id(null).created(null).fromObjectRow(null).build());
        }

        assertThat(violationsToCompare.contains(optionViolation)).isTrue();
    }

    @Test
    public void get_no_vulnerabilities_for_high_threshold() {
        assertThat(d2.maintenanceModule().getPerformanceHintsService(100,
                100).areThereVulnerabilities()).isFalse();
    }

    @Test
    public void get_vulnerabilities_for_low_threshold() {
        assertThat(d2.maintenanceModule().getPerformanceHintsService(1,
                1).areThereVulnerabilities()).isTrue();
    }

    @Test
    public void allow_access_to_d2_errors() {
        List<D2Error> d2Errors = d2.maintenanceModule().d2Errors().blockingGet();
        assertThat(d2Errors.size()).isEqualTo(2);
    }
}