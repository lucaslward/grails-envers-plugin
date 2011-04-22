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

import org.hibernate.SessionFactory
import org.hibernate.envers.AuditReader
import org.hibernate.envers.AuditReaderFactory

/**
 * Query for finding a particular class file at a particular revision.
 *
 * @author Lucas Ward
 */
class FindAtRevisionQuery {

    SessionFactory sessionFactory
    Class clazz

    FindAtRevisionQuery(SessionFactory sessionFactory, Class clazz) {
        this.sessionFactory = sessionFactory
        this.clazz = clazz
    }

    def query = { id, revisionNumber ->
        AuditReader auditReader = AuditReaderFactory.get(sessionFactory.currentSession)
        return auditReader.find(clazz, id, revisionNumber)
    }
}
