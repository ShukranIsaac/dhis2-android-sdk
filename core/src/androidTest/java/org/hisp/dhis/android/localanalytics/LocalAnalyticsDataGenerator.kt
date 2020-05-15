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
package org.hisp.dhis.android.localanalytics

import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.data.category.CategoryComboSamples
import org.hisp.dhis.android.core.data.organisationunit.OrganisationUnitSamples
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class LocalAnalyticsParams(val organisationUnitChildrenCount: Int)

class LocalAnalyticsDataGenerator(private val params: LocalAnalyticsParams) {

    fun getOrganisationUnits(): List<OrganisationUnit> {
        val root = OrganisationUnitSamples.getForValues("OU", 1, null)
        val children = getChildren(root)
        val grandchildren = children.flatMap { ch -> getChildren(ch) }
        return listOf(root) + children + grandchildren
    }

    private fun getChildren(parent: OrganisationUnit): List<OrganisationUnit> {
        return (1..params.organisationUnitChildrenCount).map { i ->
            OrganisationUnitSamples.getForValues("${parent.name()} $i", parent.level()!! + 1,
                    ObjectWithUid.create(parent.uid()))
        }
    }

    fun getCategoryCombos(): List<CategoryCombo> {
        val default = CategoryComboSamples.getCategoryCombo("Default", true)
        val cc2 = CategoryComboSamples.getCategoryCombo("CC2", false)
        val cc3 = CategoryComboSamples.getCategoryCombo("CC3", false)
        return listOf(default, cc2, cc3)
    }
}