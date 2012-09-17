package net.lucasward.grails.plugin

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration
import org.hibernate.cfg.Configuration
import org.hibernate.envers.configuration.AuditConfiguration
import org.hibernate.envers.event.AuditEventListener

/**
 * {@link AuditEventListener}, that only gets initialized when called with GrailsAnnotationConfiguration
 * for default data source.
 *  
 * @author FS
 */
class AuditEventListenerForDefaultDatasource extends AuditEventListener {
	
	private boolean initialized = false
	
	@Override
	public synchronized void initialize(Configuration cfg) {
		if (initialized) return
		if (!(cfg instanceof GrailsAnnotationConfiguration)) {
			return
		} 
		if (cfg.dataSourceName != GrailsDomainClassProperty.DEFAULT_DATA_SOURCE){
			return
		}
		initialized = true
		super.initialize(cfg)
	}
}
