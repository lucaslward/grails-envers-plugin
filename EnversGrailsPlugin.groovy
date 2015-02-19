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

import net.lucasward.grails.plugin.EnversPluginSupport
import net.lucasward.grails.plugin.RevisionsOfEntityQueryMethod
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.hibernate.SessionFactory

class EnversGrailsPlugin {
    def version = "4.3.6.Final-SNAPSHOT"
    def grailsVersion = "2.4.0 > *"

    def observe = ['hibernate4']
    def loadAfter = ['hibernate4']

    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**",
            "src/groovy/net/lucasward/grails/plugin/test/**",
            "src/java/net/lucasward/grails/plugin/test/**",
            "grails-app/conf/hibernate/hibernate.cfg.xml",
            "web-app/**"
    ]

    def author = "Lucas Ward, Jay Hogan"
    def authorEmail = ""
    def title = "Grails Envers Plugin"
    def description = 'Plugin to integrate grails with Hibernate Envers'
    def documentation = "http://grails.org/plugin/envers"

    def doWithDynamicMethods = { ctx ->
        for (entry in ctx.getBeansOfType(SessionFactory)) {
            SessionFactory sessionFactory = entry.value
            registerDomainMethods(application, sessionFactory)
        }
    }

    private def registerDomainMethods(GrailsApplication application, SessionFactory sessionFactory) {
        application.domainClasses.each { GrailsDomainClass gc ->
            def getAllRevisions = new RevisionsOfEntityQueryMethod(sessionFactory, gc.clazz)
            if (EnversPluginSupport.isAudited(gc)) {
                MetaClass mc = gc.getMetaClass()

                mc.static.findAllRevisions = {
                    getAllRevisions.query(null, null, [:])
                }

                mc.static.findAllRevisions = { Map parameters ->
                    getAllRevisions.query(null, null, parameters)
                }

                EnversPluginSupport.generateFindAllMethods(gc, sessionFactory)
                EnversPluginSupport.generateAuditReaderMethods(gc, sessionFactory)
            }
        }
    }
}
