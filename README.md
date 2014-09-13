# grails-envers-plugin

### General notes

For general instructions for using this plugin, please read following resources:
 * http://www.lucasward.net/2011/04/grails-envers-plugin.html
 * http://refaktor.blogspot.com/2012/08/hibernate-envers-with-grails-210.html

Plugin should be stable and reliable, since it is used on several projects in production without problems. Grails 2.3.9 was latest grails version used with this plugin.
Plugin should work on other grails releases from 2.3.x branch, but it is not testes yet with Grails 2.4.x.

### Installation
 * Add plugin dependency in `BuildConfig.groovy`:
   ```groovy
   ...
   grails.project.dependency.resolution = {
     ...
     plugins {
       ...
       compile ":envers:2.2.0"
       ...
     }
   ...
   }
   ...

   ```

### Using with multiple data sources in application
 * Configure additional data source(s). For example, in `DataSource.groovy`:
   ```
   ...
   environments {
     development {
       dataSource {
         dbCreate = "create-drop"
         url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS HISTORY"
       }

       dataSource_demo001 {
         dbCreate = "create-drop"
         url = "jdbc:h2:mem:devDemo001Db;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS HISTORY"
       }

       dataSource_demo002 {
         dbCreate = "create-drop"
         url = "jdbc:h2:mem:devDemo002Db;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS HISTORY"
       }
       ...
     }
   ...
   ```
   With above configuration, we are planning to use `HISTORY` schema for storing our auditing tables (see bellow).

 * Add domain classes which are persisted in configured data sources. For example, for `demo001` data source above corresponding domain class declaration can be:
   ```groovy
   package enversdemo

   import org.hibernate.envers.Audited

   @Audited
   class Address {
     String streetName
     String streetNumber

     static constraints = {
     }

     static mapping = {
       datasource 'demo001'
     }
   }

   ```
 * Finally, configure which data sources needs to be audited. For example, in `Config.groovy` add something like this:
   ```
   envers.auditedDataSourceNames = ['DEFAULT', 'demo001', 'demo002']
   ```
   If you omit a data source from `auditedDataSourceNames` list, corresponding domain classes will not be audited.

 * Although not related to the implementation of this plugin, it might be interesting to see how some envers properties can be configured. For example,
   by default envers will add `_AUD` suffix to its tables and will create these tables in default schema. You can change this by adding something like
   following in your `spring/resources.groovy`:
   ```groovy
   beans = {
     ...
     // Set global envers properties
     System.setProperty('org.hibernate.envers.audit_table_suffix', '_HISTORY')
     System.setProperty('org.hibernate.envers.default_schema', 'HISTORY')
   }
   ```

   If you prefer configuring envers properties on data source level, you will need to add something like this in your data source config:
   ```groovy
   environments {
     development {
       ...
       dataSource_demo001 {
         dbCreate = "create-drop"
         url = "jdbc:h2:mem:devDemo001Db;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS HISTORY"
       }

       // Note: Grails applies hibernate configuration of default data source to every other data source when they doesn't have explicit hibernate
       //       configuration defined. On the other hand, when other data sources have explicit hibernate configuration, then default hibernate config
       //       is not applied on them.
       //
       //       In the later case, if default hibernate options should be used for other data sources, default hibernate config options must be
       //       explicitly specified under hibernate_[dataSourceName] for each other data source. If hibernate configuration is set for default data
       //       source and that configuration differs for other data sources, then other data sources needs to have its own hibernate config defined
       //       with default hibernate properties at least.
       hibernate_demo001 {
         // defaults
         cache.use_second_level_cache = true
         cache.use_query_cache = false
         cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
         singleSession = true

         properties {
           // envers properties
           org.hibernate.envers.audit_table_suffix = "_HISTORY"
           org.hibernate.envers.default_schema = "HISTORY"
         }
       }
     }
   }
   ```

### Multi data source support constraints
 * Single audited entity supports only single data source. This means that for audited entity something like this is not supported:
   ```groovy
   @Audited
   class ZipCode {
     String code

     static mapping = {
       datasources(['lookup', 'DEFAULT'])  // <-- NOT SUPPORTED auditing over multiple data sources for a single audited entity
     }
   }
   ```
 * Audited datasources need to be explicitly configured (`envers.auditedDataSourceNames` in `Config.groovy`). This might be useful in some cases.
   However, by default it should not be required as audited data sources can be found from definition of domain classes. Future releases of a plugin might fix this.

### Changelog
#### 2.2.0
 * Multiple data sources support.
 * Config option `envers.auditedDataSourceNames` for specifying list of audited data sources.
 * `countAllRevisions` method added to audited domain class.
 * `getAuditReader` method added to domain classes for direct access to envers API.
 * Support for adding `@Audited` annotation exclusively on field level (without a need to place it on class level).


### Old changelog
#### 0.4.4
 * Included SpringSource Tool Suite files
 * Update to Grails 2.1.1
 * Listed developers
 * Cleanup
 * Switched to git-flow model

#### 0.4.3
 * Added support for multiple data sources (only default one, named `dataSource`, will be audited)
 * Renamed dynamically added domain class instance method `getRevisions()` to `retrieveRevisions()` to avoid invoking it on `getProperties()` call.
 * `retrieveRevisions()` (former `getRevisions()`) doesn't throw an exception if domain class is not audited, returning `null` instead.
