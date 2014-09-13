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

import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditQuery
import org.hibernate.envers.query.criteria.AuditProperty
import org.hibernate.envers.query.order.AuditOrder
import org.hibernate.envers.query.property.OriginalIdPropertyName

/**
 * Because envers can only be queried with a criteria like interface, if a user wants to do something like
 * order by a particular column, they must add an 'AuditOrder' on that column name.  An example is below:
 *
 * <pre>
 * auditQuery.forRevisionsOfEntity(Customer.class,false,true).addOrder(AuditEntity.revisionNumber().desc()).resultList
 * </pre>
 *
 * In the case of this plugin, we'll use something similar to grails:
 * <pre>
 * Domain.findAllRevisionsByName("name", [sort:"date",order:"desc"])
 * <pre>
 *
 * The one oddity is that sorting off a property named 'revision' will assume that the user means to sort by
 * the revision number, which is available on all audited objects
 *
 * @author Lucas Ward
 */
class PropertyNameAuditOrder {

    void addOrder(AuditQuery query, Map parameters) {

        //we'll only add sort if it's requested, otherwise use envers default (i.e. AuditOrder isn't required)
        if (!parameters.sort) {
            return
        }

        String propertyName = parameters.sort
        String order = parameters.order

        AuditProperty auditProperty = getPropertyByName(propertyName)
        AuditOrder auditOrder
        if (order?.toLowerCase() == "desc") {
            auditOrder = auditProperty.desc()
        } else {
            auditOrder = auditProperty.asc()
        }
        query.addOrder(auditOrder)

    }

    //If the name is revisionNumber, we'll use that, otherwise assume it is a property
    private AuditProperty getPropertyByName(String propertyName) {
        String revisionProperty = getRevisionProperty(propertyName)
        if (revisionProperty != null) {
            return AuditEntity.revisionProperty(revisionProperty)
        }

        if (propertyName == 'revisionNumber') {
            return AuditEntity.revisionNumber()
        }

        if (propertyName == 'revisionType') {
            return AuditEntity.revisionType()
        }

        if (propertyName == 'id') {
            return new AuditProperty(new OriginalIdPropertyName(propertyName));
        }

        return AuditEntity.property(propertyName)
    }

    //if the propertyName starts with revisionProperty. then it's a revision property
    private String getRevisionProperty(String propertyName) {
        if (propertyName.startsWith('revisionProperty.')) {
            return propertyName.split('revisionProperty.')[1]
        }

        return null
    }
}
