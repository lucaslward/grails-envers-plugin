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
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile ('org.hibernate:hibernate-envers:3.6.10.Final') {
			excludes 'ant', 'cglib', 'commons-logging', 'commons-logging-api', 'h2',
			         'hibernate-commons-annotations', 'hibernate-core', 'hibernate-entitymanager',
			         'hibernate-jpa-2.0-api', 'hibernate-testing', 'hibernate-tools', 'javassist',
			         'jcl-over-slf4j', 'junit', 'mysql-connector-java', 'slf4j-api', 'slf4j-log4j12',
			         'testng'
		}
	}

	plugins {
		build(":release:2.2.0", ":rest-client-builder:1.0.3") {
			export = false
		}

		compile(":hibernate:$grailsVersion")
	}
}
