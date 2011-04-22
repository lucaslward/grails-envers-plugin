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

package net.lucasward.grails.plugin.criteria

import org.hibernate.envers.query.AuditEntity
import org.hibernate.envers.query.AuditQuery

/**
 * Query by identity.  Envers treats the hibernate identity as a different criteria than a property name.
 *
 * @author Lucas Ward
 */
class IdentityCriteria implements EnversCriteria {

    @Override def addCriteria(AuditQuery query, Class clazz, String propertyName, Object argument) {
        query.add(AuditEntity.id().eq(argument))
    }

}
