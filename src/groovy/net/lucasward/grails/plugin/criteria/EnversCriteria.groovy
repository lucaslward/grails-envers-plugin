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

import org.hibernate.envers.query.AuditQuery

/**
 * Interface for envers criteria.  It's necessary because there are multiple different types of criteria to add.  For example, querying
 * based off of an identity is very different from querying by a property name.  And in some cases it's best to add no criteria, and it's
 * easier to have a no-op implementation.
 *
 * @author Lucas Ward
 */
interface EnversCriteria {

    public addCriteria(AuditQuery query, Class clazz, String propertyName, argument)
}
