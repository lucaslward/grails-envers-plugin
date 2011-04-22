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

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.commons.DefaultGrailsClass
import org.hibernate.envers.DefaultRevisionEntity
import org.hibernate.envers.RevisionType
import org.gmock.WithGMock
import org.hibernate.SessionFactory
import grails.test.GrailsUnitTestCase
import org.hibernate.envers.Audited
import org.codehaus.groovy.grails.orm.hibernate.GrailsHibernateDomainClass

@WithGMock
class EnversPluginSupportTests extends GrailsUnitTestCase {

    void testIsAudited(){
        Address address = new Address()
        def gc = new DefaultGrailsDomainClass(Address)
        assert EnversPluginSupport.isAudited(gc) == true
    }

    void testIsNotAudited(){
        def gc = new DefaultGrailsDomainClass(State)
        assert EnversPluginSupport.isAudited(gc) == false
    }

    void testIsAuditedAtFieldLevelOnly(){
        def gc = new DefaultGrailsDomainClass(User)
        assert EnversPluginSupport.isAudited(gc) == true
    }

    void testCollapseRevisions(){
        Customer user = new Customer(name:'collapseTest');
        DefaultRevisionEntity revisionEntity = new DefaultRevisionEntity(id:1)
        RevisionType revType = RevisionType.ADD
        def revision = [user,revisionEntity,revType]
        def collapsed = EnversPluginSupport.collapseRevision(revision)
        assert collapsed instanceof Customer
        assert collapsed.name == user.name
        assert collapsed.revisionEntity == revisionEntity
        assert collapsed.revisionType == RevisionType.ADD
    }

    void testCollapseRevisionsWithTooSmallArray(){
        try{
            EnversPluginSupport.collapseRevision([])
            fail()
        }
        catch(Exception ex){
            //we should get an exception
        }
    }

    void testCollapseRevisionsWithTooLargeArray(){
        try{
            EnversPluginSupport.collapseRevision([1,2,3,4])
            fail()
        }
        catch(Exception ex){
            //we should get an exception
        }
    }

    void testGenerateFindAllMethods(){
        mockDomain Customer
        SessionFactory sessionFactory = mock(SessionFactory)
        GrailsDomainClass gdc = new DefaultGrailsDomainClass(Customer.class)
        EnversPluginSupport.generateFindAllMethods(gdc,sessionFactory)
        assert Customer.metaClass.getStaticMetaMethod("findAllRevisionsByEmail", ["Email"]) != null
        assert Customer.metaClass.getStaticMetaMethod("findAllRevisionsByName", ["Email"]) != null
        assert Customer.metaClass.getStaticMetaMethod("findAllRevisionsByAddress", ["Email"]) != null
        assert Customer.metaClass.getStaticMetaMethod("findAllRevisionsById", ["Email"]) != null
    }

    void testGenerateAuditReaderMethods(){
        mockDomain Customer
        SessionFactory sessionFactory = mock(SessionFactory)
        GrailsDomainClass gdc = new DefaultGrailsDomainClass(Customer.class)
        EnversPluginSupport.generateAuditReaderMethods(gdc,sessionFactory)
        assert Customer.metaClass.getStaticMetaMethod("getCurrentRevision", []) != null
        assert Customer.metaClass.getMetaMethod("getRevisions", []) != null
        assert Customer.metaClass.getMetaMethod("findAtRevision", [3]) != null
    }

}
