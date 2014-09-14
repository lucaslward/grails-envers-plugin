package net.lucasward.grails.plugin

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.engine.SessionImplementor
import org.hibernate.envers.AuditReader
import org.hibernate.envers.configuration.AuditConfiguration
import org.hibernate.envers.event.AuditEventListener
import org.hibernate.envers.exception.AuditException
import org.hibernate.envers.reader.AuditReaderImpl
import org.hibernate.event.PostCollectionRecreateEvent
import org.hibernate.event.PostDeleteEvent
import org.hibernate.event.PostInsertEvent
import org.hibernate.event.PostUpdateEvent
import org.hibernate.event.PreCollectionRemoveEvent
import org.hibernate.event.PreCollectionUpdateEvent

class DatasourceAwareAuditEventListener extends AuditEventListener {
  private Map dataSourceState = [:]
  List auditedDataSourceNames = [GrailsDomainClassProperty.DEFAULT_DATA_SOURCE]

  @Override
  synchronized void initialize(Configuration cfg) {
    if (!(cfg instanceof GrailsAnnotationConfiguration)) {
      return
    }

    String currentConfigDataSourceName = cfg.dataSourceName
    if (!auditedDataSourceNames.contains(currentConfigDataSourceName)) {
      return
    }

    if (dataSourceState[currentConfigDataSourceName]?.initialized) {
      return
    }

    dataSourceState[currentConfigDataSourceName] = [:]
    dataSourceState[currentConfigDataSourceName].initialized = true
    def auditEventListener = new AuditEventListener()
    auditEventListener.initialize(cfg)
    dataSourceState[currentConfigDataSourceName].auditEventListener = auditEventListener

    //noinspection GroovyAccessibility
    dataSourceState[currentConfigDataSourceName].auditedClassNames = auditEventListener.getVerCfg().getEntCfg().entitiesConfigurations.keySet()
  }

  @Override
  void onPostDelete(PostDeleteEvent postDeleteEvent) {
    String entityClassName = postDeleteEvent.getEntity().getClass().getName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPostDelete(postDeleteEvent)
    }
  }

  @Override
  void onPostInsert(PostInsertEvent postInsertEvent) {
    String entityClassName = postInsertEvent.getEntity().getClass().getName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPostInsert(postInsertEvent)
    }
  }

  @Override
  void onPostUpdate(PostUpdateEvent postUpdateEvent) {
    String entityClassName = postUpdateEvent.getEntity().getClass().getName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPostUpdate(postUpdateEvent)
    }
  }

  @Override
  void onPostRecreateCollection(PostCollectionRecreateEvent postCollectionRecreateEvent) {
    String entityClassName = postCollectionRecreateEvent.getAffectedOwnerEntityName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPostRecreateCollection(postCollectionRecreateEvent)
    }
  }

  @Override
  void onPreRemoveCollection(PreCollectionRemoveEvent preCollectionRemoveEvent) {
    String entityClassName = preCollectionRemoveEvent.getAffectedOwnerEntityName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPreRemoveCollection(preCollectionRemoveEvent)
    }
  }

  @Override
  void onPreUpdateCollection(PreCollectionUpdateEvent preCollectionUpdateEvent) {
    String entityClassName = preCollectionUpdateEvent.getAffectedOwnerEntityName()
    AuditEventListener selectedAuditEventListener = null
    for (String dataSourceName in dataSourceState.keySet()) {
      if (dataSourceState[dataSourceName].auditedClassNames.contains(entityClassName)) {
        selectedAuditEventListener = dataSourceState[dataSourceName].auditEventListener
        break;
      }
    }

    if (selectedAuditEventListener) {
      selectedAuditEventListener.onPreUpdateCollection(preCollectionUpdateEvent)
    }
  }

  AuditReader createAuditReader(Session session, String dataSourceName) throws AuditException {
    SessionImplementor sessionImpl
    if (!(session instanceof SessionImplementor)) {
      sessionImpl = (SessionImplementor) session.getSessionFactory().getCurrentSession()
    }
    else {
      sessionImpl = (SessionImplementor) session
    }

    AuditConfiguration auditConfiguration = dataSourceState[dataSourceName].auditEventListener.getVerCfg()
    AuditReaderImpl auditReaderImpl = new AuditReaderImpl(auditConfiguration, session, sessionImpl)

    return auditReaderImpl
  }

  @Override
  public AuditConfiguration getVerCfg() {
    throw new UnsupportedOperationException('getVerCfg method is not supported. Use createAuditReader() method to obtain AuditReader instance.')
  }
}
