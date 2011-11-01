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

import net.lucasward.grails.plugin.criteria.DoNothingCriteria
import net.lucasward.grails.plugin.criteria.EnversCriteria
import org.hibernate.SessionFactory
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.query.AuditQuery

/**
 * @author Lucas Ward
 */
class RevisionsOfEntityQueryMethod {

    SessionFactory sessionFactory
    Class clazz
    EnversCriteria criteria
    PropertyNameAuditOrder auditOrder = new PropertyNameAuditOrder()
    PaginationHandler paginationHandler = new PaginationHandler()

    RevisionsOfEntityQueryMethod(SessionFactory sessionFactory, Class clazz, EnversCriteria criteria) {
        this.sessionFactory = sessionFactory
        this.clazz = clazz
        this.criteria = criteria
    }

    RevisionsOfEntityQueryMethod(SessionFactory sessionFactory, Class clazz) {
        this(sessionFactory, clazz, new DoNothingCriteria())
    }

    public query(String propertyName, argument, Map parameters) {
        def auditQueryCreator = AuditReaderFactory.get(sessionFactory.currentSession).createQuery()
        AuditQuery query = auditQueryCreator.forRevisionsOfEntity(clazz, false, true)
        criteria.addCriteria(query, clazz, propertyName, argument)
        auditOrder.addOrder(query, parameters)
        paginationHandler.addPagination(query, parameters)

        return query.resultList.collect { EnversPluginSupport.collapseRevision(it) }
    }
}
