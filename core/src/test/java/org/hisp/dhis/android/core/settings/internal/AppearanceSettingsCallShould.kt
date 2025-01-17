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

package org.hisp.dhis.android.core.settings.internal

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.hisp.dhis.android.core.arch.api.executors.internal.RxAPICallExecutor
import org.hisp.dhis.android.core.arch.handlers.internal.Handler
import org.hisp.dhis.android.core.maintenance.D2ErrorSamples
import org.hisp.dhis.android.core.settings.AppearanceSettings
import org.hisp.dhis.android.core.settings.FilterSetting
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AppearanceSettingsCallShould {

    private val filterSettingHandler: Handler<FilterSetting> = mock()
    private val programConfigurationHandler: Handler<ProgramConfigurationSetting> = mock()
    private val service: SettingAppService = mock()
    private val apiCallExecutor: RxAPICallExecutor = mock()
    private val appVersionManager: SettingsAppInfoManager = mock()

    private val appearanceSettings: AppearanceSettings = mock()
    private val appearanceSettingsSingle: Single<AppearanceSettings> = Single.just(appearanceSettings)

    private lateinit var appearanceSettingsCall: AppearanceSettingCall

    @Before
    fun setUp() {
        whenever(service.appearanceSettings(any())) doReturn appearanceSettingsSingle

        appearanceSettingsCall = AppearanceSettingCall(
            filterSettingHandler,
            programConfigurationHandler,
            service,
            apiCallExecutor,
            appVersionManager
        )
    }

    @Test
    fun call_appearances_endpoint_if_version_1() {
        whenever(appVersionManager.getDataStoreVersion()) doReturn Single.just(SettingsAppDataStoreVersion.V1_1)

        appearanceSettingsCall.getCompletable(false).blockingAwait()

        verify(service, never()).appearanceSettings(any())
    }

    @Test
    fun call_appearances_endpoint_if_version_2() {
        whenever(apiCallExecutor.wrapSingle(appearanceSettingsSingle, false)) doReturn appearanceSettingsSingle
        whenever(appVersionManager.getDataStoreVersion()) doReturn Single.just(SettingsAppDataStoreVersion.V2_0)

        appearanceSettingsCall.getCompletable(false).blockingAwait()

        verify(service).appearanceSettings(any())
    }

    @Test
    fun default_to_empty_collection_if_not_found() {
        whenever(appVersionManager.getDataStoreVersion()) doReturn Single.just(SettingsAppDataStoreVersion.V2_0)
        whenever(apiCallExecutor.wrapSingle(appearanceSettingsSingle, false)) doReturn
            Single.error(D2ErrorSamples.notFound())

        appearanceSettingsCall.getCompletable(false).blockingAwait()

        verify(filterSettingHandler).handleMany(emptyList())
        verifyNoMoreInteractions(filterSettingHandler)
        verify(programConfigurationHandler).handleMany(emptyList())
        verifyNoMoreInteractions(programConfigurationHandler)
    }
}
