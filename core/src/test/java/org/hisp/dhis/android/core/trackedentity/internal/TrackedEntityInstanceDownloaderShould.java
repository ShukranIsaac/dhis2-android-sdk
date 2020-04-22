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
package org.hisp.dhis.android.core.trackedentity.internal;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.program.internal.ProgramDataDownloadParams;
import org.hisp.dhis.android.core.settings.EnrollmentScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class TrackedEntityInstanceDownloaderShould {

    @Mock
    private TrackedEntityInstanceWithLimitCallFactory callFactory;

    private ArgumentCaptor<ProgramDataDownloadParams> paramsCapture =
            ArgumentCaptor.forClass(ProgramDataDownloadParams.class);

    private TrackedEntityInstanceDownloader downloader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        downloader = new TrackedEntityInstanceDownloader(RepositoryScope.empty(), callFactory);
    }

    @Test
    public void should_create_call_with_parsed_params() {
        downloader
                .byProgramUid("program-uid")
                .limitByOrgunit(true)
                .limitByProgram(true)
                .limit(500)
                .byProgramStatus(EnrollmentScope.ONLY_ACTIVE)
                .overwrite(true)
                .download();

        verify(callFactory).download(paramsCapture.capture());
        ProgramDataDownloadParams params = paramsCapture.getValue();

        assertThat(params.program()).isEqualTo("program-uid");
        assertThat(params.limitByOrgunit()).isTrue();
        assertThat(params.limitByProgram()).isTrue();
        assertThat(params.limit()).isEqualTo(500);
        assertThat(params.programStatus()).isEqualByComparingTo(EnrollmentScope.ONLY_ACTIVE);
        assertThat(params.overwrite()).isTrue();
    }

    @Test
    public void should_parse_uid_eq_params() {
        downloader.byUid().eq("uid").download();

        verify(callFactory).download(paramsCapture.capture());
        ProgramDataDownloadParams params = paramsCapture.getValue();

        assertThat(params.uids().size()).isEqualTo(1);
        assertThat(params.uids().get(0)).isEqualTo("uid");
    }

    @Test
    public void should_parse_uid_in_params() {
        downloader.byUid().in("uid0", "uid1", "uid2").download();

        verify(callFactory).download(paramsCapture.capture());
        ProgramDataDownloadParams params = paramsCapture.getValue();

        assertThat(params.uids().size()).isEqualTo(3);
        assertThat(params.uids().get(0)).isEqualTo("uid0");
        assertThat(params.uids().get(1)).isEqualTo("uid1");
        assertThat(params.uids().get(2)).isEqualTo("uid2");
    }
}
