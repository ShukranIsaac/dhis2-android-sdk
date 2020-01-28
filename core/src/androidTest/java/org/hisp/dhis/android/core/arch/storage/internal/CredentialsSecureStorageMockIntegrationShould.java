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

package org.hisp.dhis.android.core.arch.storage.internal;

import androidx.test.InstrumentationRegistry;

import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;

@RunWith(D2JunitRunner.class)
public class CredentialsSecureStorageMockIntegrationShould {

    private CredentialsSecureStore credentialsSecureStore;

    @Before
    public void setUp() {
        credentialsSecureStore =
                new CredentialsSecureStoreImpl(InstrumentationRegistry.getContext().getApplicationContext());
    }

    @Test
    public void credentials_are_correctly_stored() {
        Credentials credentials = Credentials.create("username", "password");
        credentialsSecureStore.setCredentials(credentials);

        Credentials retrievedCredentials = credentialsSecureStore.getCredentials();

        assertThat(retrievedCredentials.username()).isEqualTo(credentials.username());
        assertThat(retrievedCredentials.password()).isEqualTo(credentials.password());
    }

    @Test
    public void credentials_are_correctly_removed() {
        Credentials credentials = Credentials.create("username", "password");
        credentialsSecureStore.setCredentials(credentials);

        credentialsSecureStore.removeCredentials();

        Credentials retrievedCredentials = credentialsSecureStore.getCredentials();

        assertThat(retrievedCredentials).isNull();
    }

}