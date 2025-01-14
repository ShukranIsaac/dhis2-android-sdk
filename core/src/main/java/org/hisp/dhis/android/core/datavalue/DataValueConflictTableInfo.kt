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

package org.hisp.dhis.android.core.datavalue

import org.hisp.dhis.android.core.arch.db.tableinfos.TableInfo
import org.hisp.dhis.android.core.arch.helpers.CollectionsHelper
import org.hisp.dhis.android.core.common.CoreColumns

object DataValueConflictTableInfo {

    @JvmField
    val TABLE_INFO: TableInfo = object : TableInfo() {
        override fun name(): String {
            return "DataValueConflict"
        }

        override fun columns(): CoreColumns {
            return Columns()
        }
    }

    class Columns : CoreColumns() {
        override fun all(): Array<String> {
            return CollectionsHelper.appendInNewArray(
                super.all(),
                CONFLICT,
                VALUE,
                ATTRIBUTE_OPTION_COMBO,
                CATEGORY_OPTION_COMBO,
                DATA_ELEMENT,
                PERIOD,
                ORG_UNIT,
                ERROR_CODE,
                DISPLAY_DESCRIPTION,
                STATUS,
                CREATED
            )
        }

        companion object {
            const val CONFLICT = "conflict"
            const val VALUE = "value"
            const val ATTRIBUTE_OPTION_COMBO = "attributeOptionCombo"
            const val CATEGORY_OPTION_COMBO = "categoryOptionCombo"
            const val DATA_ELEMENT = "dataElement"
            const val PERIOD = "period"
            const val ORG_UNIT = "orgUnit"
            const val ERROR_CODE = "errorCode"
            const val DISPLAY_DESCRIPTION = "displayDescription"
            const val STATUS = "status"
            const val CREATED = "created"
        }
    }
}
