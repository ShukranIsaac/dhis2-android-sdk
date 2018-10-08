/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
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
package org.hisp.dhis.android.core.option;

import org.hisp.dhis.android.core.arch.handlers.IdentifiableSyncHandlerImpl;
import org.hisp.dhis.android.core.common.HandleAction;
import org.hisp.dhis.android.core.common.IdentifiableHandlerImpl;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.OrphanCleaner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class OptionSetHandlerShould {
    @Mock
    private IdentifiableObjectStore<OptionSetModel> optionSetStore;

    @Mock
    private OptionSet optionSet;

    @Mock
    private IdentifiableSyncHandlerImpl<Option> optionHandler;

    @Mock
    private OrphanCleaner<OptionSet, Option> optionCleaner;

    @Mock
    private Option option;

    private List<Option> options;

    // object to test
    private OptionSetHandler optionSetHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        optionSetHandler = new OptionSetHandler(optionSetStore, optionHandler, optionCleaner);
        when(optionSet.uid()).thenReturn("test_option_set_uid");
        options = Collections.singletonList(option);
        when(optionSet.options()).thenReturn(options);
    }

    @Test
    public void handle_options() {
        optionSetHandler.handle(optionSet, new OptionSetModelBuilder());
        verify(optionHandler).handleMany(options);
    }

    @Test
    public void clean_orphan_options_after_update() {
        when(optionSetStore.updateOrInsert(any(OptionSetModel.class))).thenReturn(HandleAction.Update);
        optionSetHandler.handle(optionSet, new OptionSetModelBuilder());
        verify(optionCleaner).deleteOrphan(optionSet, options);
    }

    @Test
    public void not_clean_orphan_options_after_insert() {
        when(optionSetStore.updateOrInsert(any(OptionSetModel.class))).thenReturn(HandleAction.Insert);
        optionSetHandler.handle(optionSet, new OptionSetModelBuilder());
        verify(optionCleaner, never()).deleteOrphan(optionSet, options);
    }

    @Test
    public void extend_identifiable_handler_impl() {
        IdentifiableHandlerImpl<OptionSet, OptionSetModel> genericHandler =
                new OptionSetHandler(null,null, null);
    }
}
