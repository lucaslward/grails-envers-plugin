/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lucasward.grails.plugin

import org.hibernate.envers.query.AuditQuery
import org.gmock.WithGMock
import grails.test.GrailsUnitTestCase

@WithGMock
class PaginationHandlerTests extends GrailsUnitTestCase {

    PaginationHandler handler

    protected void setUp() {
        handler = new PaginationHandler()
    }

    void testAddMax() {
        AuditQuery query = mock(AuditQuery)
        query.setMaxResults(10)

        play {
            handler.addPagination(query, [max: 10])
        }
    }

    void testCallWithoutMax() {
        AuditQuery query = mock(AuditQuery)
        play {
            handler.addPagination(query, [:])
        }
    }

    void testCallWithInvalidMax() {
        try {
            AuditQuery query = mock(AuditQuery)
            play {
                handler.addPagination(query, [max: "seven"])
                fail()
            }
        }
        catch(Exception ex){
            //expected
        }
    }

    void testAddOffset() {
        AuditQuery query = mock(AuditQuery)
        query.setFirstResult(10)

        play {
            handler.addPagination(query, [offset: 10])
        }
    }

    void testCallWithoutOffset() {
        AuditQuery query = mock(AuditQuery)
        play {
            handler.addPagination(query, [:])
        }
    }

    void testCallWithInvalidOffset() {
        try {
            AuditQuery query = mock(AuditQuery)
            play {
                handler.addPagination(query, [offset: "seven"])
                fail()
            }
        }
        catch(Exception ex){
            //expected
        }
    }

    void testAddBoth() {
        AuditQuery query = mock(AuditQuery)
        query.setFirstResult(10)
        query.setMaxResults(8)

        play {
            handler.addPagination(query, [max:8,offset: 10])
        }
    }
}
