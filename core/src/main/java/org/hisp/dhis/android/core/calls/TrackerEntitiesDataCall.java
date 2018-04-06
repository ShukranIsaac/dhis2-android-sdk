package org.hisp.dhis.android.core.calls;


import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.resource.ResourceStore;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.systeminfo.SystemInfoCall;
import org.hisp.dhis.android.core.systeminfo.SystemInfoService;
import org.hisp.dhis.android.core.systeminfo.SystemInfoStore;
import org.hisp.dhis.android.core.trackedentity.TeiQuery;
import org.hisp.dhis.android.core.trackedentity.TeisEndPointCall;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceHandler;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceService;

import java.util.Date;
import java.util.Set;

import retrofit2.Response;

@SuppressWarnings("PMD")
public class TrackerEntitiesDataCall implements Call<Response> {

    private boolean isExecuted;
    private final int teiLimitByOrgUnit;
    private final IdentifiableObjectStore<OrganisationUnitModel> organisationUnitStore;
    private final TrackedEntityInstanceService trackedEntityInstanceService;
    private final DatabaseAdapter databaseAdapter;
    private final TrackedEntityInstanceHandler trackedEntityInstanceHandler;
    private final ResourceHandler resourceHandler;
    private final ResourceStore resourceStore;
    private final SystemInfoService systemInfoService;
    private final SystemInfoStore systemInfoStore;

    public TrackerEntitiesDataCall(@NonNull IdentifiableObjectStore<OrganisationUnitModel> organisationUnitStore,
                                   @NonNull TrackedEntityInstanceService trackedEntityInstanceService,
                                   @NonNull DatabaseAdapter databaseAdapter,
                                   @NonNull TrackedEntityInstanceHandler trackedEntityInstanceHandler,
                                   @NonNull ResourceHandler resourceHandler,
                                   @NonNull ResourceStore resourceStore,
                                   @NonNull SystemInfoService systemInfoService,
                                   @NonNull SystemInfoStore systemInfoStore,
                                   int teiLimitByOrgUnit) {

        this.teiLimitByOrgUnit = teiLimitByOrgUnit;
        this.organisationUnitStore = organisationUnitStore;
        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.databaseAdapter = databaseAdapter;
        this.trackedEntityInstanceHandler =  trackedEntityInstanceHandler;
        this.resourceHandler = resourceHandler;
        this.resourceStore = resourceStore;
        this.systemInfoService = systemInfoService;
        this.systemInfoStore = systemInfoStore;
    }

    @Override
    public boolean isExecuted() {
        synchronized (this) {
            return isExecuted;
        }
    }

    @Override
    public Response call() throws Exception {
        synchronized (this) {
            if (isExecuted) {
                throw new IllegalStateException("Already executed");
            }
            isExecuted = true;
        }

        Response response = null;

        Transaction transaction = databaseAdapter.beginNewTransaction();

        try {

            response = new SystemInfoCall(
                    databaseAdapter, systemInfoStore,
                    systemInfoService, resourceStore
            ).call();

            if (!response.isSuccessful()) {
                return response;
            }

            SystemInfo systemInfo = (SystemInfo) response.body();
            Date serverDate = systemInfo.serverDate();

            response = trackerCall(serverDate);

            if (response == null || !response.isSuccessful()) {
                return response;
            }

            transaction.setSuccessful();

            return response;
        } finally {
            transaction.end();
        }
    }
    
    private Response trackerCall(Date serverDate) throws Exception {
        Response response = null;

        Set<String> organisationUnitUids = organisationUnitStore.selectUids();

        int pageSize = TeiQuery.Builder.create().build().getPageSize();

        int numPages = (int) Math.ceil((double) teiLimitByOrgUnit / pageSize);

        int pageLimit = 0;

        for (String orgUnitUid : organisationUnitUids) {

            int teisDownloaded = 0;

            for (int page = 1; page <= numPages; page++) {

                if (page == numPages && teiLimitByOrgUnit > 0) {
                    pageLimit = teiLimitByOrgUnit - teisDownloaded;
                }

                TeiQuery teiQuery = TeiQuery.
                        Builder.create()
                        .withOrgUnit(orgUnitUid)
                        .withPage(page)
                        .withPageLimit(pageLimit)
                        .build();

                response = new TeisEndPointCall(trackedEntityInstanceService, databaseAdapter,
                        teiQuery, trackedEntityInstanceHandler, resourceHandler).call();

                if (!response.isSuccessful()) {
                    return response;
                }

                teisDownloaded = teisDownloaded + teiQuery.getPageSize();
            }

        }

        if (response != null && response.isSuccessful()) {
            resourceHandler.handleResource(ResourceModel.Type.TRACKED_ENTITY_INSTANCE, serverDate);
        }

        return response;
    }


}