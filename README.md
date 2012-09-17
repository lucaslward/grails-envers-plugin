grails-envers-plugin
====

This is not a real full-scaled README file, but a brief description of changes done to the plugin in this fork.

 * Added support for multiple data sources (only default one, named `dataSource`, will be audited)
 * Renamed dynamically added domain class instance method `getRevisions()` to `retrieveRevisions()` to avoid invoking it on `getProperty()` call.
 * `retrieveRevisions()` (former `getRevisions()`) doesn't throw an exception if domain class is not audited, returning null instead.


How to use:

 * Checkout
 * Pack via `grails package-plugin`
 * Install via `grails install-plugin -global grails-envers-plugin-PLUGINVERSIONHERE.zip`
 * Have fun!