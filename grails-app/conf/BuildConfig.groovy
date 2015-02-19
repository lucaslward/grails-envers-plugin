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

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
      grailsPlugins()
      grailsHome()
      grailsCentral()

      mavenLocal()
      mavenCentral()
    }

    dependencies {
        compile ('org.hibernate:hibernate-envers:4.3.6.Final')
        test "org.grails:grails-datastore-test-support:1.0.2-grails-2.4"
    }

    plugins {
        build(":tomcat:7.0.55") {
            export = false
        }
        build ':release:3.0.1', ':rest-client-builder:2.0.1', {
            export = false
        }

        // To use the Envers plugin, you will also need to add the current Hibernate4 plugin
        // as a dependency.
        compile(":hibernate4:4.3.6.1") {
            export = false
        }
    }
}
