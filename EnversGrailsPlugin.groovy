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

import net.lucasward.grails.plugin.DatasourceAwareAuditEventListener
import net.lucasward.grails.plugin.EnversPluginSupport
import net.lucasward.grails.plugin.RevisionsOfEntityQueryMethod

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.SessionFactory
import org.springframework.context.ApplicationContext

class EnversGrailsPlugin {
    def version = "2.2.0.BUILD-SNAPSHOT"
    def grailsVersion = "2.1.0 > *"
    def loadAfter = ['hibernate']

    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**",
            "src/groovy/net/lucasward/grails/plugin/StubSpringSecurityService.groovy",
            "src/groovy/net/lucasward/grails/plugin/SpringSecurityRevisionListener.groovy",
            "src/groovy/net/lucasward/grails/plugin/SpringSecurityServiceHolder.groovy",
            "src/java/net/lucasward/grails/plugin/Book.java",
            "src/java/net/lucasward/grails/plugin/UserRevisionEntity.java",
            "grails-app/conf/hibernate/hibernate.cfg.xml",
            "web-app/**"
    ]

    def author = "Lucas Ward"
    def authorEmail = "lucaslward@gmail.com"
    def title = "Grails Envers Plugin"
    def description = 'Plugin to integrate grails with Hibernate Envers'
    def documentation = "http://grails.org/plugin/envers"

    def license = "APACHE"
    def developers = [
         [name: 'Jay Hogan', email: 'jay_hogan@sra.com'],
         [name: 'Damir Murat', email: 'damir.murat.git@gmail.com'],
         [name: 'Matija Folnović', email: 'mfolnovic@gmail.com'],
         [name: 'Alex Abdugafarov', email: 'fswork90@gmail.com'],
         [name: 'Burt Beckwith', email: 'burt@burtbeckwith.com']
    ]

    def issueManagement = [ system: "github", url: "https://github.com/lucaslward/grails-envers-plugin/issues" ]
    def scm = [url: 'https://github.com/lucaslward/grails-envers-plugin']

    def doWithSpring = {
        def configuredAuditedDataSourceNames = application.config?.envers?.auditedDataSourceNames
        datasourceAwareAuditEventListener(DatasourceAwareAuditEventListener) {
          if (configuredAuditedDataSourceNames) {
            auditedDataSourceNames = configuredAuditedDataSourceNames
          }
        }

        hibernateEventListeners(HibernateEventListeners) {
            listenerMap = [
                'post-insert': datasourceAwareAuditEventListener,
                'post-update': datasourceAwareAuditEventListener,
                'post-delete': datasourceAwareAuditEventListener,
                'pre-collection-update': datasourceAwareAuditEventListener,
                'pre-collection-remove': datasourceAwareAuditEventListener,
                'post-collection-recreate': datasourceAwareAuditEventListener
            ]
        }
    }

    def doWithDynamicMethods = { ctx ->
        def datasourceNames = []
        if (ctx.containsBean('dataSource')) {
            datasourceNames << GrailsDomainClassProperty.DEFAULT_DATA_SOURCE
        }

        for (name in application.config.keySet()) {
            if (name.startsWith('dataSource_')) {
                datasourceNames << name - 'dataSource_'
            }
        }

        for (String datasourceName in datasourceNames) {
            boolean isDefault = datasourceName == GrailsDomainClassProperty.DEFAULT_DATA_SOURCE
            String suffix = isDefault ? '' : '_' + datasourceName
            SessionFactory dataSourceSessionFactory = ctx.getBean("sessionFactory$suffix") as SessionFactory
            registerDomainMethods(application, dataSourceSessionFactory, datasourceName, ctx)
        }
    }

    private static void registerDomainMethods(
        GrailsApplication application, SessionFactory sessionFactory, String dataSourceName, ApplicationContext applicationContext)
    {
        DatasourceAwareAuditEventListener datasourceAwareAuditEventListener =
          applicationContext.getBean('datasourceAwareAuditEventListener') as DatasourceAwareAuditEventListener

        application.domainClasses.each { GrailsDomainClass gc ->
            if (EnversPluginSupport.isAudited(gc) && GrailsHibernateUtil.usesDatasource(gc, dataSourceName)) {
                def getAllRevisions = new RevisionsOfEntityQueryMethod(sessionFactory, gc.clazz)
                MetaClass mc = gc.getMetaClass()

                mc.static.findAllRevisions = {
                    getAllRevisions.query(dataSourceName, datasourceAwareAuditEventListener, null, null, [:])
                }

                mc.static.findAllRevisions = { Map parameters ->
                    getAllRevisions.query(dataSourceName, datasourceAwareAuditEventListener, null, null, parameters)
                }

                mc.static.countAllRevisions = {
                    getAllRevisions.count(dataSourceName, datasourceAwareAuditEventListener)
                }

                mc.static.getAuditReader = {
                    getAllRevisions.getAuditReader(dataSourceName, datasourceAwareAuditEventListener)
                }

                EnversPluginSupport.generateFindAllMethods(dataSourceName, datasourceAwareAuditEventListener, gc, sessionFactory)
                EnversPluginSupport.generateAuditReaderMethods(dataSourceName, datasourceAwareAuditEventListener, gc, sessionFactory)
            }
        }
    }
}
