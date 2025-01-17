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
package org.hisp.dhis.android.core.arch.repositories.collection;

import org.hisp.dhis.android.core.common.CoreObject;
import org.hisp.dhis.android.core.common.ObjectWithUidInterface;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.maintenance.D2Error;

import io.reactivex.Single;

public interface ReadWriteWithUidCollectionRepository<M extends CoreObject & ObjectWithUidInterface, C>
        extends ReadOnlyWithUidCollectionRepository<M> {

    /**
     * Adds a new object to the given collection in an asynchronous way based on the provided CreateProjection.
     * It returns a {@code Single<String>} with the generated UID, which is completed when the object is added to the
     * database. It adds an object with a {@link State#TO_POST}, which will be uploaded to the server in the next
     * upload.
     * @param c the CreateProjection of the object to add
     * @return the Single with the UID
     */
    Single<String> add(C c);

    /**
     * Adds a new object to the given collection in a synchronous way based on the provided CreateProjection.
     * It blocks the current thread and returns the generated UID.
     * It adds an object with a {@link State#TO_POST}, which will be uploaded to the server in the next
     * upload. Important: this is a blocking method and it should not be executed in the main thread. Consider the
     * asynchronous version {@link #add}.
     * @param c the CreateProjection of the object to add
     * @return the UID
     */
    String blockingAdd(C c) throws D2Error;
}