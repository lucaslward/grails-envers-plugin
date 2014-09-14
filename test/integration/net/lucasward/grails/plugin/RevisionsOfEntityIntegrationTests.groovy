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

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.envers.AuditReader
import org.hibernate.envers.RevisionType
import org.hibernate.envers.query.AuditEntity
import static net.lucasward.grails.plugin.TestData.getCreate2OrderEntriesWith1Modification
import static net.lucasward.grails.plugin.TestData.getCreateGormCustomerWith2Modifications
import static net.lucasward.grails.plugin.TestData.getCreateHibernateCustomerWith1Modification
import static net.lucasward.grails.plugin.TestData.getDeleteAuditTables

class RevisionsOfEntityIntegrationTests extends GroovyTestCase {
    def transactional = false

    SessionFactory sessionFactory
    Session session
    AuditReader reader
    DatasourceAwareAuditEventListener datasourceAwareAuditEventListener

    User currentUser

    protected void setUp() {
        super.setUp()
        session = sessionFactory.currentSession
        reader = datasourceAwareAuditEventListener.createAuditReader(sessionFactory.currentSession, GrailsDomainClassProperty.DEFAULT_DATA_SOURCE)
        currentUser = new User(userName: 'foo', realName: 'Bar').save(flush:  true, failOnError: true)
        SpringSecurityServiceHolder.springSecurityService.currentUser = currentUser
    }

    protected void tearDown() {
        super.tearDown()
        deleteAuditTables(session)
    }

    void testFindAllRevisions() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisions()
        assertGormCustomerRevisions(results)
    }

    //Test that we can view the latest changes first, not the first change
    void testSortFindAllRevisions() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisions(sort: "email", order: "asc")
        assert results.size() == 3
        assert results[0].email == "tester2@gorm.org"
        UserRevisionEntity entity = results[0].revisionEntity
        assert entity.getUserId() == currentUser.id
        assert results[0].revisionEntity.getUserId() == currentUser.id
        assert results[1].email == "tester3@gorm.org"
        assert results[2].email == "tester@gorm.org"
    }

    //Test that we can view the latest changes first, not the first change
    void testFindAllRevisionsWithMaxParameter() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisions(max: 1)
        assert results.size() == 1
    }

    //Test that we can paginate
    void testPaginateFindAllRevisions() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisions(max: 1)
        assert results.size() == 1
        assert results[0].email == "tester@gorm.org"

        results = Customer.findAllRevisions(max: 1, offset: 1)

        assert results[0].email == "tester2@gorm.org"

        results = Customer.findAllRevisions(max: 1, offset: 2)

        assert results[0].email == "tester3@gorm.org"
    }

    void testFindRevisionsByPrimitiveProperty() {
        createGormCustomerWith2Modifications()
        def revisions = Customer.findAllRevisionsByName("PureGorm")
        assertGormCustomerRevisions(revisions)
    }

    void testRetrievingChangesForASpecificUser() {
        createGormCustomerWith2Modifications()
        createHibernateCustomerWith1Modification()

        Customer gormUser = Customer.findByName("PureGorm")
        def revisions = reader.createQuery().forRevisionsOfEntity(Customer.class, false, true).add(AuditEntity.id().eq(gormUser.id)).resultList.collect { EnversPluginSupport.collapseRevision(it) }
        assertGormCustomerRevisions(revisions)
    }

    //Id has to be handled differently, so we should test it separately
    void testFindRevisionsById() {
        Customer customer = createGormCustomerWith2Modifications()
        def revisions = Customer.findAllRevisionsById(customer.id)
        assertGormCustomerRevisions(revisions)
    }

    //if I search by a class, such as address, does it work?
    void testFindByAssociatedDomainClass() {
        Customer customer = createGormCustomerWith2Modifications()
        def revisions = Customer.findAllRevisionsByAddress(customer.address)
        assertGormCustomerRevisions(revisions)
    }

    //Test that we can view the latest changes first, not the first change
    void testOrderByDateDesc() {
        createGormCustomerWith2Modifications()
        def revisions = reader.createQuery().forRevisionsOfEntity(Customer.class, false, true).addOrder(AuditEntity.revisionNumber().desc()).resultList
        assert revisions[0][2] == RevisionType.MOD
    }

    //Test that we can view the latest changes first, not the first change
    void testOrderByDateDescending() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisionsByName("PureGorm", [sort: "email", order: "asc"])
        assert results.size() == 3
        assert results[0].email == "tester2@gorm.org"
        assert results[1].email == "tester3@gorm.org"
        assert results[2].email == "tester@gorm.org"
    }

    void testFindAllWithSortAndMax() {
        createGormCustomerWith2Modifications()
        def results = Customer.findAllRevisionsByName("PureGorm", [sort: "email", order: "asc", max: 2])
        assert results.size() == 2
        assert results[0].email == "tester2@gorm.org"
        assert results[1].email == "tester3@gorm.org"
    }


    void testRetrieveCustomersCreatedInTheSameTransaction() {
        TestData.create2CustomersInOneTransaction()
        def results = Customer.findAllRevisionsByEmail("tester@envers.org")
        assert results.size() == 3
        assert results[0].revisionEntity == results[1].revisionEntity
    }

    void testRevisionsWithCollection() {
        Customer customer = createHibernateCustomerWith1Modification()
        create2OrderEntriesWith1Modification(customer, new Date())

        def revisions = Customer.findAllRevisionsById(customer.id)
        assert revisions.size() == 2

        revisions = OrderEntry.findAllRevisionsByCustomer(customer)
        assert revisions.size() == 3
    }

    void testFieldLevelInclusiveAudit() {
        Long id = null
        UserInclusivePartiallyAudited.withTransaction() {
            UserInclusivePartiallyAudited user = new UserInclusivePartiallyAudited(userName: "field", realName: "Annotated")
            user.save(flush: true)
            id = user.id
        }

        UserInclusivePartiallyAudited.withTransaction() {
            UserInclusivePartiallyAudited user = UserInclusivePartiallyAudited.get(id)
            user.userName = "newField"
            user.save(flush: true)
        }

        UserInclusivePartiallyAudited.withTransaction() {
            UserInclusivePartiallyAudited user = UserInclusivePartiallyAudited.get(id)
            user.realName = "Field Annotated"
            user.save(flush: true)
        }

        def results = reader.createQuery().forRevisionsOfEntity(UserInclusivePartiallyAudited.class, false, true).resultList
        assert results.size() == 2

        UserInclusivePartiallyAudited user = UserInclusivePartiallyAudited.get(id)
        def revisions = user.retrieveRevisions()

        def userRevision = user.findAtRevision(revisions[0])
        assert userRevision.realName == "Annotated"
        assert userRevision.userName == null

        userRevision = user.findAtRevision(revisions[1])
        assert userRevision.realName == "Field Annotated"
        assert userRevision.userName == null
    }

    void testFieldLevelExclusiveAudit() {
        Long id = null
        UserExclusivePartiallyAudited.withTransaction() {
            UserExclusivePartiallyAudited user = new UserExclusivePartiallyAudited(userName: "field", realName: "Annotated")
            user.save(flush: true)
            id = user.id
        }

        UserExclusivePartiallyAudited.withTransaction() {
            UserExclusivePartiallyAudited user = UserExclusivePartiallyAudited.get(id)
            user.userName = "newField"
            user.save(flush: true)
        }

        UserExclusivePartiallyAudited.withTransaction() {
            UserExclusivePartiallyAudited user = UserExclusivePartiallyAudited.get(id)
            user.realName = "Field Annotated"
            user.save(flush: true)
        }

        def results = reader.createQuery().forRevisionsOfEntity(UserExclusivePartiallyAudited.class, false, true).resultList
        assert results.size() == 3

        UserExclusivePartiallyAudited user = UserExclusivePartiallyAudited.get(id)
        def revisions = user.retrieveRevisions()

        def userRevision = user.findAtRevision(revisions[0])
        assert userRevision.realName == null
        assert userRevision.userName == "field"

        userRevision = user.findAtRevision(revisions[1])
        assert userRevision.realName == null
        assert userRevision.userName == "newField"

        userRevision = user.findAtRevision(revisions[2])
        assert userRevision.realName == null
        assert userRevision.userName == "newField"
    }

    void testSaveCustomerWithoutAddress() {
        Customer.withTransaction {
            Customer customer = new Customer(name: "PureGorm", email: "tester@gorm.org")
            customer.save(flush: true)
        }
    }

    void testGetCurrentRevision() {
        UserRevisionEntity revisionEntity = Customer.getCurrentRevision()
        assert revisionEntity != null
        assert revisionEntity.id != null
    }

    void testRetrieveRevisions() {
        Customer customer = createGormCustomerWith2Modifications()
        List<Number> revisions = customer.retrieveRevisions()
        assert revisions != null
        assert revisions.size() == 3

        Customer oldCustomer = customer.findAtRevision(revisions[1])
        assert oldCustomer.email == "tester2@gorm.org"
        assert oldCustomer.address.city == "New York"
    }

    //this doesn't work, but I'm not sure I care.  Searching by the address object itself makes more sense to me...
    //    void testFindByAssociatedDomainClassId(){
    //        User user = createGormCustomerWith2Modifications()
    //        def revisions = User.findAllRevisionsByAddress(user.address.id)
    //        assertGormCustomerRevisions(revisions)
    //    }

    private static def assertGormCustomerRevisions(List results) {
        assert results.size() == 3
        def r = results[0]
        assert r.name == "PureGorm"
        assert r.email == "tester@gorm.org"
        assert r.address.city == "Chicago"
        assert r.address.zip == "60640"
        assert r.revisionType == RevisionType.ADD
        r = results[1]
        assert r.email == "tester2@gorm.org"
        assert r.address.city == "New York"
        assert r.revisionType == RevisionType.MOD
        r = results[2]
        assert r.email == "tester3@gorm.org"
        assert r.address.zip == "10003"
        assert r.revisionType == RevisionType.MOD
    }
}
