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
package org.hisp.dhis.android.core.calls;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryComboEndpointCall;
import org.hisp.dhis.android.core.category.CategoryEndpointCall;
import org.hisp.dhis.android.core.common.BasicCallFactory;
import org.hisp.dhis.android.core.common.D2CallExecutor;
import org.hisp.dhis.android.core.common.GenericCallData;
import org.hisp.dhis.android.core.common.GenericCallFactory;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.common.SimpleCallFactory;
import org.hisp.dhis.android.core.common.SyncCall;
import org.hisp.dhis.android.core.common.UidsHelper;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.dataset.DataSetParentCall;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCall;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramParentCall;
import org.hisp.dhis.android.core.settings.SystemSetting;
import org.hisp.dhis.android.core.settings.SystemSettingCall;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.SystemInfoCall;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.UserCall;

import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity",
        "PMD.StdCyclomaticComplexity", "PMD.ExcessiveImports", "PMD.PrematureDeclaration"})
public class MetadataCall extends SyncCall<Response> {

    private final DatabaseAdapter databaseAdapter;
    private final Retrofit retrofit;

    private final BasicCallFactory<SystemInfo> systemInfoCallFactory;
    private final SimpleCallFactory<SystemSetting> systemSettingCallFactory;
    private final GenericCallFactory<User> userCallFactory;
    private final SimpleCallFactory<Payload<Category>> categoryCallFactory;
    private final SimpleCallFactory<Payload<CategoryCombo>> categoryComboCallFactory;
    private final SimpleCallFactory<Payload<Program>> programParentCallFactory;
    private final OrganisationUnitCall.Factory organisationUnitCallFactory;
    private final DataSetParentCall.Factory dataSetParentCallFactory;

    public MetadataCall(@NonNull DatabaseAdapter databaseAdapter,
                        @NonNull Retrofit retrofit,
                        @NonNull BasicCallFactory<SystemInfo> systemInfoCallFactory,
                        @NonNull SimpleCallFactory<SystemSetting> systemSettingCallFactory,
                        @NonNull GenericCallFactory<User> userCallFactory,
                        @NonNull SimpleCallFactory<Payload<Category>> categoryCallFactory,
                        @NonNull SimpleCallFactory<Payload<CategoryCombo>> categoryComboCallFactory,
                        @NonNull SimpleCallFactory<Payload<Program>> programParentCallFactory,
                        @NonNull OrganisationUnitCall.Factory organisationUnitCallFactory,
                        @NonNull DataSetParentCall.Factory dataSetParentCallFactory) {
        this.databaseAdapter = databaseAdapter;
        this.retrofit = retrofit;

        this.systemInfoCallFactory = systemInfoCallFactory;
        this.systemSettingCallFactory = systemSettingCallFactory;
        this.userCallFactory = userCallFactory;
        this.categoryCallFactory = categoryCallFactory;
        this.categoryComboCallFactory = categoryComboCallFactory;
        this.programParentCallFactory = programParentCallFactory;
        this.organisationUnitCallFactory = organisationUnitCallFactory;
        this.dataSetParentCallFactory = dataSetParentCallFactory;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    public Response call() throws Exception {
        super.setExecuted();

        Transaction transaction = databaseAdapter.beginNewTransaction();
        try {

            SystemInfo systemInfo = new D2CallExecutor().executeD2Call(
                    systemInfoCallFactory.create(databaseAdapter, retrofit));

            GenericCallData genericCallData = GenericCallData.create(databaseAdapter, retrofit,
                    systemInfo.serverDate());

            Response<SystemSetting> systemSettingResponse = systemSettingCallFactory.create(genericCallData).call();
            if (!systemSettingResponse.isSuccessful()) {
                return systemSettingResponse;
            }

            User user = userCallFactory.create(genericCallData).call();

            Response<Payload<Category>> categoryResponse = categoryCallFactory.create(genericCallData).call();
            if (!categoryResponse.isSuccessful()) {
                return categoryResponse;
            }

            Response<Payload<CategoryCombo>> categoryComboResponse
                    = categoryComboCallFactory.create(genericCallData).call();
            if (!categoryComboResponse.isSuccessful()) {
                return categoryComboResponse;
            }

            Response<Payload<Program>> programResponse = programParentCallFactory.create(genericCallData).call();
            if (!programResponse.isSuccessful()) {
                return programResponse;
            }

            Response<Payload<OrganisationUnit>> organisationUnitResponse =
                    organisationUnitCallFactory.create(genericCallData, user,
                            UidsHelper.getUids(programResponse.body().items())).call();

            if (!organisationUnitResponse.isSuccessful()) {
                return organisationUnitResponse;
            }

            List<OrganisationUnit> organisationUnits = organisationUnitResponse.body().items();
            Response dataSetParentCallResponse =
                    dataSetParentCallFactory.create(user, genericCallData, organisationUnits).call();

            if (dataSetParentCallResponse.isSuccessful()) {
                transaction.setSuccessful();
            }

            return dataSetParentCallResponse;
        } finally {
            transaction.end();
        }
    }

    public static MetadataCall create(DatabaseAdapter databaseAdapter, Retrofit retrofit) {
        return new MetadataCall(
                databaseAdapter,
                retrofit,
                SystemInfoCall.FACTORY,
                SystemSettingCall.FACTORY,
                UserCall.FACTORY,
                CategoryEndpointCall.FACTORY,
                CategoryComboEndpointCall.FACTORY,
                ProgramParentCall.FACTORY,
                OrganisationUnitCall.FACTORY,
                DataSetParentCall.FACTORY
        );
    }
}
