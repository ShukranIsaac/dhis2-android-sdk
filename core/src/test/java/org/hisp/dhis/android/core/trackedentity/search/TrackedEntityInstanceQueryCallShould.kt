/*
 *  Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.android.core.trackedentity.search

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import java.text.ParseException
import java.util.*
import java.util.concurrent.Callable
import javax.net.ssl.HttpsURLConnection
import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor
import org.hisp.dhis.android.core.arch.api.executors.internal.RxAPICallExecutor
import org.hisp.dhis.android.core.arch.api.payload.internal.Payload
import org.hisp.dhis.android.core.arch.repositories.scope.internal.FilterItemOperator
import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryScopeFilterItem
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.common.BaseCallShould
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventInternalAccessor
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.event.internal.EventFields
import org.hisp.dhis.android.core.event.internal.EventService
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.systeminfo.DHISVersion
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceService
import org.hisp.dhis.android.core.util.simpleDateFormat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Call

@RunWith(JUnit4::class)
class TrackedEntityInstanceQueryCallShould : BaseCallShould() {

    private val trackedEntityService: TrackedEntityInstanceService = mock()
    private val eventService: EventService = mock()
    private val apiCallExecutor: APICallExecutor = mock()
    private val rxAPICallExecutor: RxAPICallExecutor = mock()
    private val mapper: SearchGridMapper = mock()
    private val dhisVersionManager: DHISVersionManager = mock()
    private val searchGrid: SearchGrid = mock()
    private val searchGridCall: Call<SearchGrid> = mock()
    private val eventCallSingle: Single<Payload<Event>> = mock()
    private val teis: List<TrackedEntityInstance> = mock()
    private val eventPayload: Payload<Event> = mock()
    private val attribute: List<RepositoryScopeFilterItem> = emptyList()

    private lateinit var query: TrackedEntityInstanceQueryOnline

    // object to test
    private lateinit var call: Callable<TrackerQueryResult>

    @Before
    override fun setUp() {
        super.setUp()
        val orgUnits = listOf("ou1", "ou2")

        query = TrackedEntityInstanceQueryOnline(
            uids = listOf("uid1", "uid2"),
            orgUnits = orgUnits,
            orgUnitMode = OrganisationUnitMode.ACCESSIBLE,
            program = "program",
            programStartDate = Date(),
            programEndDate = Date(),
            enrollmentStatus = EnrollmentStatus.ACTIVE,
            followUp = true,
            eventStatus = EventStatus.OVERDUE,
            incidentStartDate = Date(),
            incidentEndDate = Date(),
            trackedEntityType = "teiTypeStr",
            query = "queryStr",
            attributeFilter = attribute,
            includeDeleted = false,
            lastUpdatedStartDate = Date(),
            lastUpdatedEndDate = Date(),
            order = "lastupdated:desc",
            assignedUserMode = AssignedUserMode.ANY,
            paging = false,
            page = 2,
            pageSize = 33
        )

        whenServiceQuery().thenReturn(searchGridCall)
        whenEventServiceQuery().thenReturn(eventCallSingle)

        whenever(apiCallExecutor.executeObjectCallWithErrorCatcher(eq(searchGridCall), any())).doReturn(searchGrid)
        whenever(rxAPICallExecutor.wrapSingle(eq(eventCallSingle), any())).doReturn(eventCallSingle)
        whenever(eventCallSingle.blockingGet()).doReturn(eventPayload)

        whenever(mapper.transform(any())).doReturn(teis)
        whenever(dhisVersionManager.isGreaterThan(DHISVersion.V2_33)).doReturn(true)

        // Metadata call
        call = getFactory().getCall(query)
    }

    @Test
    fun succeed_when_endpoint_calls_succeed() {
        val teisResponse = call.call()
        assertThat(teisResponse.trackedEntities).isEqualTo(teis)
    }

    @Test
    fun call_mapper_with_search_grid() {
        call.call()
        verify(mapper).transform(searchGrid)
        verifyNoMoreInteractions(mapper)
    }

    @Test
    fun call_service_with_query_parameters() {
        call.call()
        verifyService(query)
        verifyNoMoreInteractions(trackedEntityService)
    }

    @Test
    fun throw_D2CallException_when_service_call_returns_failed_response() {
        whenever(apiCallExecutor.executeObjectCallWithErrorCatcher(eq(searchGridCall), any())).doThrow(d2Error)
        whenever(d2Error.errorCode()).doReturn(D2ErrorCode.MAX_TEI_COUNT_REACHED)

        try {
            call.call()
            fail("D2Error was expected but was not thrown")
        } catch (d2e: D2Error) {
            assertThat(d2e.errorCode() == D2ErrorCode.MAX_TEI_COUNT_REACHED).isTrue()
        }
    }

    @Test
    fun throw_too_many_org_units_exception_when_request_was_too_long() {
        whenever(apiCallExecutor.executeObjectCallWithErrorCatcher(eq(searchGridCall), any())).doThrow(d2Error)
        whenever(d2Error.errorCode()).doReturn(D2ErrorCode.TOO_MANY_ORG_UNITS)
        whenever(d2Error.httpErrorCode()).doReturn(HttpsURLConnection.HTTP_REQ_TOO_LONG)

        try {
            call.call()
            fail("D2Error was expected but was not thrown")
        } catch (d2e: D2Error) {
            assertThat(d2e.errorCode() == D2ErrorCode.TOO_MANY_ORG_UNITS).isTrue()
        }
    }

    @Test(expected = D2Error::class)
    fun throw_D2CallException_when_mapper_throws_exception() {
        whenever(mapper.transform(searchGrid)).thenThrow(ParseException::class.java)
        call.call()
    }

    @Test
    fun should_not_map_active_event_status_if_greater_than_2_33() {
        whenever(dhisVersionManager.isGreaterThan(DHISVersion.V2_33)).doReturn(true)
        val activeQuery = query.copy(eventStatus = EventStatus.ACTIVE)
        val activeCall = getFactory().getCall(activeQuery)

        activeCall.call()

        verifyService(activeQuery, EventStatus.ACTIVE)
    }

    @Test
    fun should_map_active_event_status_if_not_greater_than_2_33() {
        whenever(dhisVersionManager.isGreaterThan(DHISVersion.V2_33)).doReturn(false)

        val activeQuery = query.copy(eventStatus = EventStatus.ACTIVE)
        val activeCall = getFactory().getCall(activeQuery)

        activeCall.call()

        verifyService(activeQuery, EventStatus.VISITED)

        val nonActiveQuery = query.copy(eventStatus = EventStatus.SCHEDULE)
        val nonActiveCall = getFactory().getCall(nonActiveQuery)

        nonActiveCall.call()

        verifyService(activeQuery, EventStatus.SCHEDULE)
    }

    @Test
    fun should_query_events_if_data_value_filter() {
        val events = listOf(
            EventInternalAccessor.insertTrackedEntityInstance(Event.builder().uid("uid1"), "tei1").build(),
            EventInternalAccessor.insertTrackedEntityInstance(Event.builder().uid("uid2"), "tei2").build(),
        )
        whenever(eventPayload.items()).doReturn(events)

        val query = query.copy(
            dataValueFilter = listOf(
                RepositoryScopeFilterItem.builder()
                    .key("dataElement")
                    .operator(FilterItemOperator.EQ)
                    .value("2")
                    .build()
            )
        )
        val call = getFactory().getCall(query)

        call.call()

        val expectedTeiQuery = TrackedEntityInstanceQueryCallFactory.getPostEventTeiQuery(query, events)

        verifyEventService(query)
        verifyService(expectedTeiQuery)
    }

    @Test
    fun should_query_events_for_multiple_orgunits() {
        whenever(eventPayload.items()).doReturn(emptyList())

        val query = query.copy(
            dataValueFilter = listOf(
                RepositoryScopeFilterItem.builder()
                    .key("dataElement")
                    .operator(FilterItemOperator.EQ)
                    .value("2")
                    .build()
            ),
            orgUnits = listOf("orgunit1", "orgunit2")
        )
        val call = getFactory().getCall(query)

        call.call()

        verifyEventService(query)
    }

    private fun getFactory(): TrackedEntityInstanceQueryCallFactory {
        return TrackedEntityInstanceQueryCallFactory(
            trackedEntityService, eventService, mapper, apiCallExecutor, rxAPICallExecutor, dhisVersionManager
        )
    }

    private fun verifyService(
        query: TrackedEntityInstanceQueryOnline,
        expectedStatus: EventStatus? = query.eventStatus
    ) {
        verify(trackedEntityService).query(
            eq(query.uids?.joinToString(";")),
            eq(query.orgUnits.joinToString(";")),
            eq(query.orgUnitMode.toString()),
            eq(query.program),
            eq(query.programStage),
            eq(query.programStartDate.simpleDateFormat()),
            eq(query.programEndDate.simpleDateFormat()),
            eq(query.enrollmentStatus?.toString()),
            eq(query.incidentStartDate.simpleDateFormat()),
            eq(query.incidentEndDate.simpleDateFormat()),
            eq(query.followUp),
            eq(query.eventStartDate.simpleDateFormat()),
            eq(query.eventEndDate.simpleDateFormat()),
            eq(expectedStatus?.toString()),
            eq(query.trackedEntityType),
            eq(query.query),
            any(),
            eq(query.assignedUserMode?.toString()),
            eq(query.lastUpdatedStartDate.simpleDateFormat()),
            eq(query.lastUpdatedEndDate.simpleDateFormat()),
            eq(query.order),
            eq(query.paging),
            eq(query.page),
            eq(query.pageSize)
        )
    }

    private fun verifyEventService(query: TrackedEntityInstanceQueryOnline) {
        if (query.orgUnits.size <= 1) {
            verifyEventServiceForOrgunit(query, query.orgUnits.firstOrNull())
        } else {
            query.orgUnits.forEach {
                verifyEventServiceForOrgunit(query, it)
            }
        }
    }
    private fun verifyEventServiceForOrgunit(query: TrackedEntityInstanceQueryOnline, orgunit: String?) {
        verify(eventService).getEvents(
            eq(EventFields.teiQueryFields),
            eq(orgunit),
            eq(query.orgUnitMode?.toString()),
            eq(query.eventStatus?.toString()),
            eq(query.program),
            eq(query.programStage),
            eq(query.enrollmentStatus?.toString()),
            any(),
            eq(query.followUp),
            eq(query.eventStartDate.simpleDateFormat()),
            eq(query.eventEndDate.simpleDateFormat()),
            eq(query.dueStartDate.simpleDateFormat()),
            eq(query.dueEndDate.simpleDateFormat()),
            eq(query.order),
            eq(query.assignedUserMode?.toString()),
            eq(query.paging),
            eq(query.page),
            eq(query.pageSize),
            eq(query.lastUpdatedStartDate.simpleDateFormat()),
            eq(query.lastUpdatedEndDate.simpleDateFormat()),
            eq(query.includeDeleted),
            eq(null),
        )
    }

    private fun whenServiceQuery(): OngoingStubbing<Call<SearchGrid>?> {
        return whenever(
            trackedEntityService.query(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        )
    }

    private fun whenEventServiceQuery(): OngoingStubbing<Single<Payload<Event>>> {
        return whenever(
            eventService.getEvents(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            )
        )
    }
}
