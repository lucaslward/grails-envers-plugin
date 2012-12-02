grails-envers-plugin
====================

This is not a real full-scaled README file, but a brief changelog of this fork.

0.4.4
-------
 * Included SpringSource Tool Suite files
 * Update to Grails 2.1.1
 * Listed developers
 * Cleanup
 * Switched to git-flow model

0.4.3
-----
 * Added support for multiple data sources (only default one, named `dataSource`, will be audited)
 * Renamed dynamically added domain class instance method `getRevisions()` to `retrieveRevisions()` to avoid invoking it on `getProperties()` call.
 * `retrieveRevisions()` (former `getRevisions()`) doesn't throw an exception if domain class is not audited, returning `null` instead.

How to use:
-----------
 * Checkout
 * Pack via `grails package-plugin`
 * Install via `grails install-plugin -global grails-envers-plugin-PLUGINVERSIONHERE.zip`
 * Have fun!