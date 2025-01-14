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

package org.hisp.dhis.android.core.arch.api.fields.internal;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import retrofit2.Converter;

class FieldsConverter implements Converter<Fields, String> {
    FieldsConverter() {
        // explicit empty constructor
    }

    @Override
    @SuppressWarnings("unchecked")
    public String convert(Fields fields) throws IOException {
        StringBuilder builder = new StringBuilder();

        // recursive function which processes
        // properties and builds query string
        append(builder, (List<Property>) fields.fields());

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static void append(StringBuilder builder, List<Property> properties) {
        Iterator<Property> propertyIterator = properties.iterator();

        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();

            // we need to append property name first
            builder.append(property.name());

            if (property instanceof Field) {
                if (propertyIterator.hasNext()) {
                    builder.append(',');
                }
            } else if (property instanceof NestedField) {
                List<Property> children = ((NestedField) property).children();

                if (!children.isEmpty()) {
                    // open property array
                    builder.append('[');

                    // recursive call to method
                    append(builder, children);

                    // close property array
                    builder.append(']');

                    if (propertyIterator.hasNext()) {
                        builder.append(',');
                    }
                }
            } else {
                throw new IllegalArgumentException("Unsupported type of Property: " +
                        property.getClass());
            }
        }
    }
}
