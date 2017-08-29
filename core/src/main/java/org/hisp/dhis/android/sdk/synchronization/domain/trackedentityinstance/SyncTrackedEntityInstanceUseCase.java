package org.hisp.dhis.android.sdk.synchronization.domain.trackedentityinstance;


import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.synchronization.domain.event.IEventRepository;
import org.hisp.dhis.android.sdk.synchronization.domain.faileditem.IFailedItemRepository;

public class SyncTrackedEntityInstanceUseCase {
    ITrackedEntityInstanceRepository mTrackedEntityInstanceRepository;
    IFailedItemRepository mFailedItemRepository;
    TrackedEntityInstanceSynchronizer mTrackedEntityInstanceSynchronizer;


    public SyncTrackedEntityInstanceUseCase(ITrackedEntityInstanceRepository trackedEntityInstanceRepository, IEventRepository eventRepository,
            IFailedItemRepository failedItemRepository) {
        mTrackedEntityInstanceRepository = trackedEntityInstanceRepository;
        mFailedItemRepository = failedItemRepository;
        mTrackedEntityInstanceSynchronizer = new TrackedEntityInstanceSynchronizer(mTrackedEntityInstanceRepository, eventRepository, mFailedItemRepository);
    }

    public void execute(TrackedEntityInstance trackedEntityInstance) {
        if (trackedEntityInstance == null) {
            return;
        }

        mTrackedEntityInstanceSynchronizer.sync(trackedEntityInstance);
    }
}
