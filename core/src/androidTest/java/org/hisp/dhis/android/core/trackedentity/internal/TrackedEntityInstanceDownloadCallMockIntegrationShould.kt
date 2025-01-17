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
package org.hisp.dhis.android.core.trackedentity.internal

import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestMetadataDispatcher
import org.junit.Test

class TrackedEntityInstanceDownloadCallMockIntegrationShould : BaseMockIntegrationTestMetadataDispatcher() {

    @Test
    fun emit_progress() {
        val testObserver = d2.trackedEntityModule().trackedEntityInstanceDownloader().download().test()

        testObserver.assertValueCount(5)

        testObserver.assertValueAt(0) { v: TrackerD2Progress ->
            !v.isComplete &&
                v.doneCalls().size == 1 &&
                v.programs().all { (_, progress) -> !progress.isComplete && progress.syncStatus == null }
        }
        testObserver.assertValueAt(1) { v ->
            !v.isComplete && v.doneCalls().size == 2 && programSucceeded(v.programs(), "IpHINAT79UW")
        }
        testObserver.assertValueAt(2) { v ->
            !v.isComplete && v.doneCalls().size == 3 && allProgramsSucceeded(v.programs())
        }
        testObserver.assertValueAt(3) { v ->
            !v.isComplete && v.doneCalls().size == 4 && allProgramsSucceeded(v.programs())
        }
        testObserver.assertValueAt(4) { v ->
            v.isComplete && v.doneCalls().size == 4 && allProgramsSucceeded(v.programs())
        }

        testObserver.dispose()
    }

    private fun allProgramsSucceeded(programs: Map<String, D2ProgressStatus>): Boolean {
        return programs.all { (_, progress) ->
            progress.isComplete && progress.syncStatus == D2ProgressSyncStatus.SUCCESS
        }
    }

    private fun programSucceeded(programs: Map<String, D2ProgressStatus>, program: String): Boolean {
        return programs.all { (programsProgram, progress) ->
            if (programsProgram == program) {
                progress.isComplete && progress.syncStatus == D2ProgressSyncStatus.SUCCESS
            } else {
                !progress.isComplete
            }
        }
    }
}
