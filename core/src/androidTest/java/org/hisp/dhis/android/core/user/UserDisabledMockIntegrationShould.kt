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

package org.hisp.dhis.android.core.user

import com.google.common.truth.Truth.assertThat
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestMetadataEnqueable
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner
import org.junit.After
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@Ignore("Test for db deletion on user disabled. Only to be executed on demand")
@RunWith(D2JunitRunner::class)
class UserDisabledMockIntegrationShould : BaseMockIntegrationTestMetadataEnqueable() {

    @After
    @Throws(D2Error::class)
    fun tearDown() {
        d2.wipeModule().wipeData()
    }

    @Test
    fun delete_database_when_user_disabled() {
        dhis2MockServer.enqueueMockResponse(401, "user/user_disabled.json")
        addDummyData()
        assertThat(d2.userModule().accountManager().getAccounts().size).isEqualTo(1)

        try {
            d2.dataValueModule().dataValues().blockingUpload()
        } catch (e: Exception) {
            val d2Error = e.cause as D2Error
            assertThat(d2Error.errorCode()).isEqualTo(D2ErrorCode.USER_ACCOUNT_DISABLED)
        }
        assertThat(d2.userModule().accountManager().getAccounts().size).isEqualTo(0)
    }

    @Test
    fun do_not_delete_database_when_user_has_bad_credentials() {
        dhis2MockServer.enqueueMockResponse(401, "user/user_unauthorized.json")
        addDummyData()
        assertThat(d2.userModule().accountManager().getAccounts().size).isEqualTo(1)

        try {
            d2.dataValueModule().dataValues().blockingUpload()
        } catch (e: Exception) {
            val d2Error = e.cause as D2Error
            assertThat(d2Error.errorCode()).isEqualTo(D2ErrorCode.API_UNSUCCESSFUL_RESPONSE)
        }
        assertThat(d2.userModule().accountManager().getAccounts().size).isEqualTo(1)
    }

    private fun addDummyData() {
        d2.dataValueModule().dataValues().value(
            "20191021",
            "DiszpKrYNg8",
            "Ok9OQpitjQr",
            "DwrQJzeChWp",
            "DwrQJzeChWp"
        ).blockingSet("30")
    }
}
