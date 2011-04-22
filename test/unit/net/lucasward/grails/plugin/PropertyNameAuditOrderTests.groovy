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

import grails.test.GrailsUnitTestCase
import org.hibernate.envers.query.order.AuditOrder
import org.gmock.WithGMock
import org.hibernate.envers.query.AuditQuery
import org.hibernate.envers.query.criteria.AuditProperty
import org.hibernate.envers.query.AuditEntity

@WithGMock
class PropertyNameAuditOrderTests extends GrailsUnitTestCase {

    PropertyNameAuditOrder auditOrder
    def propertyName = "name"

    AuditQuery query
    AuditProperty property
    AuditOrder order
    AuditEntity auditEntity

    protected void setUp() {
        super.setUp()
        mockLogging(PropertyNameAuditOrder)
        auditOrder = new PropertyNameAuditOrder()
    }

    private def setupMocksForPropertyName() {

        setupMocks()
        auditEntity.static.property(propertyName).returns(property)

    }

    private def setupMocks() {
        query = mock(AuditQuery)
        property = mock(AuditProperty)
        order = mock(AuditOrder)
        auditEntity = mock(AuditEntity)
        query.addOrder(order)
    }

    void testSortByPropertyNameDesc() {
        setupMocksForPropertyName()
        property.desc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: propertyName, order: "desc"])
        }
    }

    void testSortByPropertyNameAsc() {
        setupMocksForPropertyName()
        property.asc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: propertyName, order: "asc"])
        }
    }

    void testSortByAllCapsDirection() {
        setupMocksForPropertyName()
        property.desc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: propertyName, order: "DESC"])
        }
    }

    void testSortByPropertyNameDefaultOrdering() {
        setupMocksForPropertyName()
        property.asc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: propertyName])
        }
    }

    void testSortByPropertyNameWithNoParams() {
        AuditQuery query = mock(AuditQuery)

        play {
            auditOrder.addOrder(query, [:])
        }
    }

    void testSortByRevisionNumberDesc() {
        setupMocks()
        auditEntity.static.revisionNumber().returns(property)
        property.desc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: "revisionNumber", order: "desc"])
        }
    }

    void testSortByRevisionType() {
        setupMocks()
        auditEntity.static.revisionType().returns(property)
        property.desc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: "revisionType", order: "desc"])
        }
    }

    void testSortByRevisionProperty() {
        setupMocks()
        auditEntity.static.revisionProperty("userId").returns(property)
        property.desc().returns(order)

        play {
            auditOrder.addOrder(query, [sort: "revisionProperty.userId", order: "desc"])
        }
    }
}
