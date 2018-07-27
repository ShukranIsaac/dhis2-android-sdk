/*
 * Copyright (c) 2004-2018, University of Oslo
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

package org.hisp.dhis.android.core.dataelement;

import org.hisp.dhis.android.core.common.BaseNameableObject;
import org.hisp.dhis.android.core.common.BaseObjectShould;
import org.hisp.dhis.android.core.common.ObjectShould;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class DataElementOperandShould extends BaseObjectShould implements ObjectShould {

    public DataElementOperandShould() {
        super("dataelement/data_element_operand.json");
    }

    @Override
    @Test
    public void map_from_json_string() throws IOException, ParseException {

        DataElementOperand dataElementOperand = objectMapper.readValue(jsonStream, DataElementOperand.class);

        assertThat(dataElementOperand.uid()).isEqualTo("ca8lfO062zg.Prlt0C1RF0s");
        assertThat(dataElementOperand.name()).isEqualTo("Q_Vitamin A received 4-6 months ago at 12-59 dose Fixed, <1y");
        assertThat(dataElementOperand.displayName()).isEqualTo("Q_Vitamin A received 4-6 months ago at 12-59 dose Fixed, <1y");
        assertThat(dataElementOperand.created()).isEqualTo(BaseNameableObject.parseDate("2018-07-21T18:40:18.919"));
        assertThat(dataElementOperand.lastUpdated()).isEqualTo(BaseNameableObject.parseDate("2018-07-21T18:40:18.919"));
        assertThat(dataElementOperand.shortName()).isEqualTo("Q_VitA_4-6m_VitA2 Fixed, <1y");
        assertThat(dataElementOperand.displayShortName()).isEqualTo("Q_VitA_4-6m_VitA2 Fixed, <1y");
        assertThat(dataElementOperand.dataElement().uid()).isEqualTo("ca8lfO062zg");
        assertThat(dataElementOperand.categoryOptionCombo().uid()).isEqualTo("Prlt0C1RF0s");
    }
}