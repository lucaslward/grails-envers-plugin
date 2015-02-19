grails-envers-plugin
====

This is not a real full-scaled README file, but a brief description of changes done to the plugin in this fork.

 * Added support for multiple data sources (only default one, named `dataSource`, will be audited)
 * Renamed dynamically added method getRevisions() to `retrieveRevisions()` to avoid invoking int on `getProperty()` call.
 * `retrieveRevisions()` (former `getRevisions()`) doesn't throw an exception if domain class is not audited, returning null instead.


How to use:

 * Checkout
 * Pack via `grails package-plugin`
 * Install via `grails install-plugin -global grails-envers-plugin-PLUGINVERSIONHERE.zip`
 * Have fun

 Hibernate 4 Updates
 ----
  * Upgraded unit and integration tests so that they work with Grails 2.4.
  * Updated Grails configuration so that it is consistent with Grails 2.4 defaults, and so that grails run-app works.
  * Removed Hibernate Event Listener because it is automatically registered with Hibernate 4.
  * Moved test classes into separate package so that they are easier to exclude when packaging the plugin.
  * Suggest converting the version of the Envers plugin to match the version of Envers it implements, to maintain
    consistency with the way that the Hibernate plugins are managed.
  * Handling of Enums has changed due to underlying Envers bug, and there are new Integration tests that show a workaround.