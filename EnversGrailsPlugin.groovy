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

import org.hibernate.envers.event.AuditEventListener
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners
import net.lucasward.grails.plugin.EnversPluginSupport
import org.codehaus.groovy.grails.commons.GrailsClass
import org.hibernate.SessionFactory
import org.hibernate.Session
import org.hibernate.envers.AuditReaderFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.envers.query.AuditEntity
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import net.lucasward.grails.plugin.RevisionsOfEntityQueryMethod

class EnversGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.6 > *"
    // the other plugins this plugin depends on
    def observe = ['hibernate']
    def loadAfter = ['hibernate']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/net/lucasward/grails/plugin/User.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/Customer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/Address.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/OrderEntry.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/State.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/AbstractPlayer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/AmateurPlayer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/ProfessionalPlayer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/BaseballPlayer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/FootballPlayer.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/entry/AbstractPerformanceYear.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/entry/AmateurPerformanceYear.groovy",
            "grails-app/domain/net/lucasward/grails/plugin/inheritance/entry/ProfessionalPerformanceYear.groovy",
            "src/groovy/net/lucasward/grails/plugin/StubSpringSecurityService.groovy",
            "src/java/net/lucasward/grails/plugin/Book.java",
            "src/java/net/lucasward/grails/plugin/SpringSecurityRevisionListener.java",
            "src/java/net/lucasward/grails/plugin/SpringSecurityServiceHolder.java",
            "src/java/net/lucasward/grails/plugin/UserRevisionEntity.java",
            "grails-app/conf/hibernate/hibernate.cfg.xml",
            "lib/envers-1.2.2.ga-sources.jar",
            "lib/envers-1.2.2.ga.jar"
    ]

    // TODO Fill in these fields
    def author = "Lucas Ward"
    def authorEmail = ""
    def title = "Grails Envers Plugin"
    def description = '''\\
Plugin to integrate grails with Hibernate Envers

Proper documentation will be coming. Until then, this plugin describes usage: http://www.lucasward.net/2011/04/grails-envers-plugin.html
'''

    // URL to the plugin's documentation
    def documentation = "http://www.lucasward.net/2011/04/grails-envers-plugin.html"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

/**
 * Add the listeners to Grails.  By default Grails creates it's own listeners so that you can access
 * them from domain objects.  However, it provides a hook in by looking for a bean named 'hibernateEvenListeners'
 * and applying those over the top of what it already has.  In our case, we need the below listeners, as described
 * in the envers documentation.
 *
 * TODO:Make sure that an application can still define hibernate listeners with this approach.
 *
 * 	<listener class="org.hibernate.envers.event.AuditEventListener" type="post-insert"/>
 <listener class="org.hibernate.envers.event.AuditEventListener" type="post-update"/>
 <listener class="org.hibernate.envers.event.AuditEventListener" type="post-delete"/>
 <listener class="org.hibernate.envers.event.AuditEventListener" type="pre-collection-update"/>
 <listener class="org.hibernate.envers.event.AuditEventListener" type="pre-collection-remove"/>
 <listener class="org.hibernate.envers.event.AuditEventListener" type="post-collection-recreate"/>
 */

    def doWithSpring = {
        println "Configuring Envers..."
        auditEventListener(AuditEventListener)

        hibernateEventListeners(HibernateEventListeners) {
            listenerMap = ['post-insert': auditEventListener,
                    'post-update': auditEventListener,
                    'post-delete': auditEventListener,
                    'pre-collection-update': auditEventListener,
                    'pre-collection-remove': auditEventListener,
                    'post-collection-recreate': auditEventListener]
        }
    }

    def doWithDynamicMethods = { ctx ->
        for (entry in ctx.getBeansOfType(SessionFactory)) {
            SessionFactory sessionFactory = entry.value
            registerDomainMethods(application, sessionFactory)
        }
    }

    private def registerDomainMethods(GrailsApplication application, SessionFactory sessionFactory) {
        application.domainClasses.each { GrailsDomainClass gc ->
            def getAllRevisions = new RevisionsOfEntityQueryMethod(sessionFactory,gc.clazz)
            if (EnversPluginSupport.isAudited(gc)) {
                MetaClass mc = gc.getMetaClass()
                mc.static.findAllRevisions = {
                    getAllRevisions.query(null,null,[:])
                }
                mc.static.findAllRevisions = { Map parameters ->
                    getAllRevisions.query(null,null,parameters)
                }
                EnversPluginSupport.generateFindAllMethods(gc, sessionFactory)
                EnversPluginSupport.generateAuditReaderMethods(gc, sessionFactory)
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->

    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
