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
package org.hisp.dhis.android.core.imports.internal.conflicts

import org.hisp.dhis.android.core.imports.internal.ImportConflict

internal object MissingAttributeConflict : TrackerImportConflictItem {

    private val regex: Regex = Regex("Missing mandatory attribute (\\w{11})")
    private fun description(attributeName: String) = "Missing mandatory attribute: $attributeName"

    override val errorCode: String = "E1018"

    override fun matches(conflict: ImportConflict): Boolean {
        return regex.matches(conflict.value())
    }

    override fun getTrackedEntityAttribute(conflict: ImportConflict): String? {
        return regex.find(conflict.value())?.groupValues?.get(1)
    }

    override fun getDisplayDescription(
        conflict: ImportConflict,
        context: TrackerImportConflictItemContext
    ): String {

        return getTrackedEntityAttribute(conflict)?.let { attributeUid ->
            context.attributeStore.selectByUid(attributeUid)?.let { attribute ->
                val name = attribute.displayFormName() ?: attribute.displayName() ?: attributeUid
                description(name)
            }
        }
            ?: conflict.value()
    }
}
