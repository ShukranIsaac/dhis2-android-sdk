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
package org.hisp.dhis.android.core.map.layer.internal

import com.nhaarman.mockitokotlin2.*
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore
import org.hisp.dhis.android.core.arch.handlers.internal.HandleAction
import org.hisp.dhis.android.core.arch.handlers.internal.Handler
import org.hisp.dhis.android.core.arch.handlers.internal.LinkHandler
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MapLayerHandlerShould {

    private val store: IdentifiableObjectStore<MapLayer> = mock()
    private val providerHandler: LinkHandler<MapLayerImageryProvider, MapLayerImageryProvider> = mock()
    private val mapLayer: MapLayer = mock()

    private lateinit var mapLayerHandler: Handler<MapLayer>

    private var mapLayers = listOf(mapLayer)

    @Before
    fun setUp() {
        mapLayerHandler = MapLayerHandler(store, providerHandler)

        whenever(mapLayer.uid()).doReturn("mapLayerId")
        whenever(mapLayer.imageryProviders()).doReturn(emptyList())

        whenever(store.updateOrInsert(mapLayer)).doReturn(HandleAction.Insert)
    }

    @Test
    fun update_shouldUpdateMapLayer() {
        mapLayerHandler.handleMany(mapLayers)

        // verify that update is called once
        verify(store, times(1)).updateOrInsert(mapLayer)
        verify(store, never()).delete(any())
    }

    @Test
    fun call_imagery_provider_handlers() {
        mapLayerHandler.handleMany(mapLayers)
        verify(providerHandler).handleMany(any(), any(), any())
    }
}
