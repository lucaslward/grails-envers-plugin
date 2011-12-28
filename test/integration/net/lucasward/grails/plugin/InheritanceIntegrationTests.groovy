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

import net.lucasward.grails.plugin.inheritance.BaseballPlayer
import net.lucasward.grails.plugin.inheritance.entry.ProfessionalPerformanceYear
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.envers.AuditReader
import org.hibernate.envers.AuditReaderFactory

/**
 * These tests are specifically designed to test how well envers as configured in this plugin can handle
 * complex inheritance and collections configurations using gorm
 */
class InheritanceIntegrationTests extends GroovyTestCase {

    def transactional = false

    SessionFactory sessionFactory
    Session session
    AuditReader reader

    User currentUser
    
    protected void setUp() {
        super.setUp()
        session = sessionFactory.currentSession
        reader = AuditReaderFactory.get(sessionFactory.currentSession)

        currentUser = new User(userName: 'foo', realName: 'Bar').save(flush: true, failOnError: true)
        assert currentUser.id
        SpringSecurityServiceHolder.springSecurityService.currentUser = currentUser
    }

    protected void tearDown() {
        super.tearDown()

        //delete all the data from these tables in between tests.  Because Envers only writes out on transaction
        //commit, we can't test it without committing the transaction, so we have to clean up afterwards
        session.createSQLQuery("delete from professional_performance_year").executeUpdate()
        session.createSQLQuery("delete from professional_performance_year_aud").executeUpdate()
        session.createSQLQuery("delete from amateur_performance_year").executeUpdate()
        session.createSQLQuery("delete from abstract_player").executeUpdate()
        session.createSQLQuery("delete from abstract_player_aud").executeUpdate()
        session.createSQLQuery("delete from abstract_performance_year").executeUpdate()
        session.createSQLQuery("delete from abstract_performance_year_aud").executeUpdate()
        session.createSQLQuery("delete from user").executeUpdate()
        session.createSQLQuery("delete from user_aud").executeUpdate()
        session.createSQLQuery("delete from revinfo").executeUpdate()
    }

    void testStoringEntry() {

        Date today = new Date()

        BaseballPlayer player
        ProfessionalPerformanceYear.withTransaction {
            player = new BaseballPlayer(name: "Albert Pujols")
            player.save(flush: true)
            ProfessionalPerformanceYear py = new ProfessionalPerformanceYear(date: today, salary: 1.2, player: player)
            py.save(flush: true)
        }

        def results = ProfessionalPerformanceYear.findAllRevisions()
        assert results.size() == 1
    }
}
