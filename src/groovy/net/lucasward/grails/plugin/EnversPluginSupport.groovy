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

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import java.util.concurrent.ConcurrentHashMap
import org.hibernate.Session
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.Audited

import org.springframework.core.annotation.AnnotationUtils
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import net.lucasward.grails.plugin.criteria.IdentityCriteria
import net.lucasward.grails.plugin.criteria.PropertyNameCriteria

/**
 * Support classes for the plugin.  it's easier to test some of these methods separately than if they were in the main plugin file.  Most of the
 * dynamic methods are added in this class.
 *
 * @author Lucas Ward
 */
class EnversPluginSupport {

    static def getAllRevisions(Class clazz, SessionFactory sessionFactory) {
        Session session = sessionFactory.currentSession
        return AuditReaderFactory.get(sessionFactory.currentSession).createQuery().forRevisionsOfEntity(clazz, false, true).resultList
    }

    private static DOMAIN_INITIALIZERS = new ConcurrentHashMap()

    static initializeDomain(Class c) {
        synchronized (c) {
            // enhance domain class only once, initializer is removed after calling
            DOMAIN_INITIALIZERS.remove(c)?.call()
        }
    }

    /**
     * For right now, only the presence of the @Audited annotation on the class will mean it's annotated
     */
    static def isAudited = { GrailsDomainClass gc ->
        return AnnotationUtils.findAnnotation(gc.clazz, Audited) != null
    }

    /**
     * Depending upon how you structure your envers query, if you pass in the boolean indicating you want revision info, you
     * will get an array with three elements, which contain: the entity at a particular revision, the revisionEntity
     * (which contains things like the revision date and id), and the revision type (i.e. ADD, MOD, DEL)  This method
     * will collapse them down into one object, which is easy and somewhat idiomatic in groovy, but impossible in
     * java.
     */
    static collapseRevision = { revision ->
        if (revision.size() != 3) {
            def msg = "Invalid revision while attempting to collapse: [${revision}]"
            log.error(msg)
            throw new IllegalArgumentException(msg)
        }

        //The entity is always in the first position in the array.  I'm somewhat trusting Envers here.  I could
        //check for the audited annotation, but it seems a bit paranoid...
        def entity = revision[0]
        entity.metaClass.revisionEntity = revision[1]
        entity.metaClass.revisionType = revision[2]
        return entity
    }

    static generateFindAllMethods(GrailsDomainClass gc, SessionFactory sessionFactory) {
        def findAllRevisionsBy = new RevisionsOfEntityQueryMethod(sessionFactory, gc.clazz, new PropertyNameCriteria())
        MetaClass mc = gc.getMetaClass()
        gc.persistantProperties.each { GrailsDomainClassProperty prop ->
            generateFindAllMethod(prop, mc, findAllRevisionsBy)
        }
        generateFindAllMethod(gc.identifier, mc, new RevisionsOfEntityQueryMethod(sessionFactory, gc.clazz, new IdentityCriteria()))
    }

    //Generate the methods that work on just 'AuditReader', and not and AuditQuery
    static generateAuditReaderMethods(GrailsDomainClass gc, SessionFactory sessionFactory) {
        def getCurrentRevision = new GetCurrentRevisionQuery(sessionFactory, gc.clazz)
        def getRevisions = new GetRevisionsQuery(sessionFactory, gc.clazz)
        def findAtRevision = new FindAtRevisionQuery(sessionFactory, gc.clazz)
        MetaClass mc = gc.getMetaClass()
        mc.static.getCurrentRevision = {
            getCurrentRevision.query()
        }
        mc.retrieveRevisions = {
			try {
				return getRevisions.query(delegate.id)
			} catch (org.hibernate.envers.exception.NotAuditedException ex) {
				// This indicates call to entity.revisions or entity.getProperties()
				// In second case, we shouldn't throwing an exception clearly is unexpected behavior
				return null;
			}
        }
        mc.findAtRevision = { revisionNumber ->
            findAtRevision.query(delegate.id, revisionNumber)
        }
    }

    private static def generateFindAllMethod(GrailsDomainClassProperty prop, MetaClass mc, method) {
        def propertyName = prop.name
        def methodName = "findAllRevisionsBy${propertyName.capitalize()}"
        mc.static."$methodName" = { argument ->
            method.query(propertyName, argument, [:])
        }

        mc.static."$methodName" = { argument, Map parameters ->
            method.query(propertyName, argument, parameters)
        }
    }
}