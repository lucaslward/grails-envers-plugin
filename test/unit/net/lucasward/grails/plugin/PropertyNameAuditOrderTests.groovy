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

import grails.test.GrailsMock
import grails.test.mixin.TestFor
import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditQuery
import org.hibernate.envers.query.criteria.AuditProperty
import org.hibernate.envers.query.order.AuditOrder
import org.junit.Before

@TestFor(Customer)
class PropertyNameAuditOrderTests {

    PropertyNameAuditOrder auditOrder
    String propertyName = "name"

    @Before
    void before() {
        auditOrder = new PropertyNameAuditOrder()
    }

    void testSortByPropertyNameDesc() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: propertyName, order: "desc"])
        }
    }

    void testSortByPropertyNameAsc() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: propertyName, order: "asc"])
        }
    }

    void testSortByAllCapsDirection() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: propertyName, order: "DESC"])
        }
    }

    void testSortByPropertyNameDefaultOrdering() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: propertyName])
        }
    }

    void testSortByPropertyNameWithNoParams() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [:])
        }
    }

    void testSortByRevisionNumberDesc() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: "revisionNumber", order: "desc"])
        }
    }

    void testSortByRevisionType() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: "revisionType", order: "desc"])
        }
    }

    void testSortByRevisionProperty() {
        withMock([:]) {query, property, order, auditEntity ->
            auditOrder.addOrder(query, [sort: "revisionProperty.userId", order: "desc"])
        }
    }

    private void withMock(Map options, Closure doIt) {
        GrailsMock queryMock = mockFor(AuditQuery, true)
        GrailsMock propertyMock = mockFor(AuditProperty, true)
        GrailsMock orderMock = mockFor(AuditOrder, true)
        GrailsMock auditEntityMock = mockFor(AuditEntity, true)

        propertyMock.demand.desc(0..100) {->
            return orderMock.createMock()
        }
        propertyMock.demand.asc(0..100) {->
            return orderMock.createMock()
        }
        auditEntityMock.demand.static.revisionProperty(0..100) {String prop ->
            return propertyMock.createMock()
        }
        auditEntityMock.demand.static.revisionNumber(0..100) {->
            return propertyMock.createMock()
        }
        auditEntityMock.demand.static.revisionType(0..100) {->
            return propertyMock.createMock()
        }
        auditEntityMock.demand.static.property(0..100) {String prop ->
            return propertyMock.createMock()
        }
        queryMock.demand.addOrder(0..100) {AuditOrder order -> }

        doIt(
                queryMock.createMock(),
                propertyMock.createMock(),
                orderMock.createMock(),
                auditEntityMock.createMock()
        )
    }
}
