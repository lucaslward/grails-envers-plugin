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

import org.hibernate.Session

class TestData {

    //delete all the data from these tables in between tests.  Because Envers only writes out on transaction
    //commit, we can't test it without committing the transaction, so we have to clean up afterwards
    def static deleteAuditTables = { Session session ->
        session.createSQLQuery("delete from order_entry").executeUpdate()
        session.createSQLQuery("delete from order_entry_aud").executeUpdate()
        session.createSQLQuery("delete from customer").executeUpdate()
        session.createSQLQuery("delete from customer_aud").executeUpdate()
        session.createSQLQuery("delete from address").executeUpdate()
        session.createSQLQuery("delete from address_aud").executeUpdate()
        session.createSQLQuery("delete from user").executeUpdate()
        session.createSQLQuery("delete from user_aud").executeUpdate()
        session.createSQLQuery("delete from revinfo").executeUpdate()
    }

    def static create2CustomersInOneTransaction = {

        def customers = []
        Customer.withTransaction {
            def address = new Address(city: "Chicago", zip: "60640")
            address.save()
            def customer = new Customer(name: "Envers1", email: "tester@envers.org", address: address)
            customer.save(flush: true)
            customers << customer
            address = new Address(city: "New York", zip: "60654")
            address.save()
            customer = new Customer(name: "Envers2", email: "tester@envers.org", address: address)
            customer.save()
            customers << customer
        }

        Customer.withTransaction {
            Customer customer
            def address = new Address(city: "Chicago", zip: "60640")
            address.save()
            customer = new Customer(name: "Envers3", email: "tester@envers.org", address: address)
            customer.save(flush: true)
            customers << customer
        }

        return customers
    }

    static def createGormCustomerWith2Modifications = {
        Customer customer
        Customer.withTransaction {
            def address = new Address(city: "Chicago", zip: "60640")
            address.save()
            customer = new Customer(name: "PureGorm", email: "tester@gorm.org", address: address)
            customer.save(flush: true)
        }

        Customer.withTransaction {
            customer = Customer.findByName("PureGorm")
            customer.email = "tester2@gorm.org"
            customer.address.city = "New York"
            customer.save(flush: true)
        }

        Customer.withTransaction {
            customer = Customer.findByName("PureGorm")
            customer.email = "tester3@gorm.org"
            customer.address.zip = "10003"
            customer.save(flush: true)
        }
        return customer
    }

    static def createHibernateCustomerWith1Modification = {
        Customer customer
        Customer.withTransaction {
            def address = new Address(city: "Boston", zip: "02109")
            address.save()
            customer = new Customer(name: "Hibernate", email: "tester@hibernate.org", address: address)
            customer.save(flush: true)
        }

        Customer.withTransaction {
            customer = Customer.findByName("Hibernate")
            customer.email = "tester2@Hibernate.org"
            customer.address.zip = "02108"
            customer.save(flush: true)
        }
        return customer
    }

    static def create2OrderEntriesWith1Modification = { Customer customer, Date time ->
        Customer.withTransaction {
            OrderEntry order = new OrderEntry(
                    date: time - 1,
                    amount: 5.3,
                    numberOfItems: 2,
                    customer: customer)
            order.save()
            customer.orders << order
            customer.save(flush:true)
        }

        Customer.withTransaction {
            OrderEntry order = new OrderEntry(
                    date: time,
                    amount: 5.3,
                    numberOfItems: 2,
                    customer: customer)
            order.save()
            customer.orders << order
            customer.save(flush:true)
        }

        Customer.withTransaction {
            OrderEntry order = customer.orders.first()
            order.amount = 22.53
            order.numberOfItems = 3
            order.save()
        }
    }
}
