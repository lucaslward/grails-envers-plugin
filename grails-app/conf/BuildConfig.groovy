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

grails.project.work.dir = 'target'

grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {
    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenCentral()
    }

    dependencies {
        compile ('org.hibernate:hibernate-envers:3.6.10.Final') {
            // Grails already includes all of the necessary dependencies
            transitive = false
        }
    }

    plugins {
        build(":tomcat:$grailsVersion", ":release:2.2.0", ":rest-client-builder:1.0.3") {
            export = false
        }

        compile(":hibernate:$grailsVersion") {
            export = false
        }
    }
}

// While publishing plugin, do not attempt to commit and tag source changes.
grails.release.scm.enabled = false
