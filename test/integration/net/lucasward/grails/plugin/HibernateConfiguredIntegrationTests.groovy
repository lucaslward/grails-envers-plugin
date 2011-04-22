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

/**
 * Tests to make sure that Hibernate Configured classes still work with this plugin
 */
class HibernateConfiguredIntegrationTests {

    void testBasicModification(){
        Book.withTransaction {
            Book book = new Book(title:"Dance With Dragons", author:"George R. R. Martin")
            book.save()
        }


        Book book = Book.findByTitle("Dance With Dragons")
  //      assert book != null

//        def results = Book.findAllRevisionsById(book.id)
  //      assert results.size() == 1
    }
}
