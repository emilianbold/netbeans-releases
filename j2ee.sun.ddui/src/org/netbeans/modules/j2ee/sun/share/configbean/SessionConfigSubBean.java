/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager;
import org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties;


/** Property structure of session-config from DTD
 *
 *		session-config : SessionConfig?
 *			session-manager : SessionManager?
 *				[attr: persistence-type CDATA memory]
 *				manager-properties : ManagerProperties?
 *					property : WebProperty[0,n]
 *						[attr: name CDATA #REQUIRED ]
 *						[attr: value CDATA #REQUIRED ]
 *						description : String?
 *				store-properties : StoreProperties?
 *					property : WebProperty[0,n]
 *						[attr: name CDATA #REQUIRED ]
 *						[attr: value CDATA #REQUIRED ]
 *						description : String?
 *			session-properties : SessionProperties?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			cookie-properties : CookieProperties?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 */
/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class SessionConfigSubBean {
    
	private static final String SunWebFileName = "sun-web.xml";	// NOI18N
	
	private static final String SESSION_CONFIG = "session-config"; // NOI18N
//	private static final String DEFAULT_PERSISTENCE_TYPE = "memory"; // NOI18N
	
	/** takes place of Base.parent, which we don't have */
	private WebAppRoot webAppRoot;
	
	/** Holds value of property persistenceType. */
	private String persistenceType;
	
	/** Holds value of property managerProperties. */
	private ManagerProperties managerProperties;
	
	/** Holds value of property storeProperties. */
	private StoreProperties storeProperties;
	
	/** Holds value of property sessionProperties. */
	private SessionProperties sessionProperties;
	
	/** Holds value of property cookieProperties. */
	private CookieProperties cookieProperties;
	
    /** Creates a new instance of SessionConfiguration */
	public SessionConfigSubBean() {
	}
	
	/** Override init to load from persistent storage if necessary
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(WebAppRoot parent) {
		webAppRoot = parent;

//		loadFromPlanFile(getConfig());	// !PW handled by WebAppRoot
	}
	
	/** Getter for property persistenceType.
	 * @return Value of property persistenceType.
	 *
	 */
	public String getPersistenceType() {
		return this.persistenceType;
	}
	
	/** Setter for property persistenceType.
	 * @param newPersistenceType New value of property persistenceType.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPersistenceType(String newPersistenceType) throws java.beans.PropertyVetoException {
		if(newPersistenceType == null || newPersistenceType.length() == 0) { // if empty, set to null
			newPersistenceType = null;
		}
		
		String oldPersistenceType = this.persistenceType;
		getVCS().fireVetoableChange("persistenceType", oldPersistenceType, newPersistenceType);	// NOI18N
		this.persistenceType = newPersistenceType;
		getPCS().firePropertyChange("persistenceType", oldPersistenceType, persistenceType);	// NOI18N
	}

	/** Getter for property managerProperties.
	 * @return Value of property managerProperties.
	 *
	 */
	public ManagerProperties getManagerProperties() {
		return this.managerProperties;
	}
	
	/** Setter for property managerProperties.
	 * @param managerProperties New value of property managerProperties.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setManagerProperties(ManagerProperties newManagerProperties) throws java.beans.PropertyVetoException {
		if(newManagerProperties == null) {
			newManagerProperties = StorageBeanFactory.getDefault().createManagerProperties();
		}
		
		ManagerProperties oldManagerProperties = this.managerProperties;
		getVCS().fireVetoableChange("managerProperties", oldManagerProperties, newManagerProperties);	// NOI18N
		this.managerProperties = newManagerProperties;
		getPCS().firePropertyChange("managerProperties", oldManagerProperties, managerProperties);	// NOI18N
	}
	
	/** Getter for property storeProperties.
	 * @return Value of property storeProperties.
	 *
	 */
	public StoreProperties getStoreProperties() {
		return this.storeProperties;
	}
	
	/** Setter for property storeProperties.
	 * @param storeProperties New value of property storeProperties.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setStoreProperties(StoreProperties newStoreProperties) throws java.beans.PropertyVetoException {
		if(newStoreProperties == null) {
			newStoreProperties = StorageBeanFactory.getDefault().createStoreProperties();
		}
		
		StoreProperties oldStoreProperties = this.storeProperties;
		getVCS().fireVetoableChange("storeProperties", oldStoreProperties, newStoreProperties);	// NOI18N
		this.storeProperties = newStoreProperties;
		getPCS().firePropertyChange("storeProperties", oldStoreProperties, storeProperties);	// NOI18N
	}
	
	/** Getter for property sessionProperties.
	 * @return Value of property sessionProperties.
	 *
	 */
	public SessionProperties getSessionProperties() {
		return this.sessionProperties;
	}
	
	/** Setter for property sessionProperties.
	 * @param sessionProperties New value of property sessionProperties.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setSessionProperties(SessionProperties newSessionProperties) throws java.beans.PropertyVetoException {
		if(newSessionProperties == null) {
			newSessionProperties = StorageBeanFactory.getDefault().createSessionProperties();
		}
		
		SessionProperties oldSessionProperties = this.sessionProperties;
		getVCS().fireVetoableChange("sessionProperties", oldSessionProperties, newSessionProperties);	// NOI18N
		this.sessionProperties = newSessionProperties;
		getPCS().firePropertyChange("sessionProperties", oldSessionProperties, sessionProperties);	// NOI18N
	}
	
	/** Getter for property cookieProperties.
	 * @return Value of property cookieProperties.
	 *
	 */
	public CookieProperties getCookieProperties() {
		return this.cookieProperties;
	}
	
	/** Setter for property cookieProperties.
	 * @param cookieProperties New value of property cookieProperties.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCookieProperties(CookieProperties newCookieProperties) throws java.beans.PropertyVetoException {
		if(newCookieProperties == null) {
			newCookieProperties = StorageBeanFactory.getDefault().createCookieProperties();
		}
		
		CookieProperties oldCookieProperties = this.cookieProperties;
		getVCS().fireVetoableChange("cookieProperties", oldCookieProperties, newCookieProperties);	// NOI18N
		this.cookieProperties = newCookieProperties;
		getPCS().firePropertyChange("cookieProperties", oldCookieProperties, cookieProperties);	// NOI18N
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new Snippet() {
			
            public org.netbeans.modules.schema2beans.BaseBean getCmpDDSnippet() {
                return null;
            }

			public CommonDDBean getDDSnippet() {
				SessionConfig sessionConfig = StorageBeanFactory.getDefault().createSessionConfig();
				SessionManager sessionManager = sessionConfig.newSessionManager();
				boolean hasSessionManager = false;
				
				// Set each setting only if present and set by the user.
				String persistenceType = getPersistenceType();
				if(Utils.notEmpty(persistenceType)) {
					sessionManager.setPersistenceType(getPersistenceType());
					hasSessionManager = true;
				} else {
					sessionManager.setPersistenceType(null);
				}

				ManagerProperties mp = getManagerProperties();
				if(mp.sizeWebProperty() > 0) {
					sessionManager.setManagerProperties((ManagerProperties) mp.clone());
					hasSessionManager = true;
				}

				StoreProperties sp = getStoreProperties();
				if(sp.sizeWebProperty() > 0) {
					sessionManager.setStoreProperties((StoreProperties) sp.clone());
					hasSessionManager = true;
				}

				if(hasSessionManager) {
					sessionConfig.setSessionManager(sessionManager);
				}

				SessionProperties ssp = getSessionProperties();
				if(ssp.sizeWebProperty() > 0) {
					sessionConfig.setSessionProperties((SessionProperties) ssp.clone());
				}

				CookieProperties cp = getCookieProperties();
				if(cp.sizeWebProperty() > 0) {
					sessionConfig.setCookieProperties((CookieProperties) cp.clone());
				}
				
				return sessionConfig;
			}

			public boolean hasDDSnippet() {
				if(getPersistenceType() != null) {
					return true;
				}
				
				ManagerProperties mp = getManagerProperties();
				if(mp.sizeWebProperty() > 0) {
					return true;
				}
				
				StoreProperties sp = getStoreProperties();
				if(sp.sizeWebProperty() > 0) {
					return true;
				}
				
				SessionProperties ssp = getSessionProperties();
				if(ssp.sizeWebProperty() > 0) {
					return true;
				}
				
				CookieProperties cp = getCookieProperties();
				if(cp.sizeWebProperty() > 0) {
					return true;
				}	
				
				return false;
			}
			
			public String getPropertyName() {
				return SunWebApp.SESSION_CONFIG;
			}
			
/** ---------------------------------------------------------------------------
 *  The following methods would have been inherited from DefaultSnippet if we
 *  could derive from that class.  (Another artifact of not being a real DConfigBean
 */			
			public String getFileName() {
				return SunWebFileName; // NOI18N
			}
			
			public CommonDDBean mergeIntoRootDD(CommonDDBean ddRoot) {
				SessionConfig sessionConfig = (SessionConfig) getDDSnippet();
				
				if(ddRoot instanceof SunWebApp) {
					SunWebApp swa = (SunWebApp) ddRoot;
					swa.setSessionConfig(sessionConfig);
				}
				
				return sessionConfig;
			}

			public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
				// !PW I don't think this can ever be called, but if so, it should
				// be called with ddParent being an instance of SunWebApp and so
				// mergeIntoRootDD() performs the correct action.
				//
				return mergeIntoRootDD(ddParent);
			}	
/** ------------------------------------------------------------------------- */			
			
		};
		
		snippets.add(snipOne);
		return snippets;	
	}
		
	private class SessionConfigFinder implements ConfigFinder {
		public Object find(Object obj) {
			SessionConfig result = null;
			
			if(obj instanceof SunWebApp) {
				SunWebApp swa = (SunWebApp) obj;
				
				result = swa.getSessionConfig();
			}
			
			return result;
		}
	}
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = webAppRoot.getUriText();
		
		SessionConfig beanGraph = (SessionConfig) config.getBeans(
			uriText, SunWebFileName, null, new SessionConfigFinder());
		
		clearProperties();
		
		if(null != beanGraph) {
			SessionManager sm = beanGraph.getSessionManager();
			if(sm != null) {
				persistenceType = sm.getPersistenceType();
				
				ManagerProperties mp = sm.getManagerProperties();
				if(mp != null && mp.sizeWebProperty() > 0) {
					managerProperties = (ManagerProperties) mp.clone();
				}

				StoreProperties sp = sm.getStoreProperties();
				if(sp != null && sp.sizeWebProperty() > 0) {
					storeProperties = (StoreProperties) sp.clone();
				}
			}
			
			SessionProperties ssp = beanGraph.getSessionProperties();
			if(ssp != null && ssp.sizeWebProperty() > 0) {
				sessionProperties = (SessionProperties) ssp.clone();
			}

			CookieProperties cp = beanGraph.getCookieProperties();
			if(cp != null && cp.sizeWebProperty() > 0) {
				cookieProperties = (CookieProperties) cp.clone();
			}
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
        StorageBeanFactory beanFactory = StorageBeanFactory.getDefault();
        
		persistenceType = null;
		managerProperties = beanFactory.createManagerProperties();
		storeProperties = beanFactory.createStoreProperties();
		sessionProperties = beanFactory.createSessionProperties();
		cookieProperties = beanFactory.createCookieProperties();
	}
	
	protected void setDefaultProperties() {
		// no defaults
	}
	
	/** -----------------------------------------------------------------------
	 *  JavaBean support that would have been inherited from Base if this was a 
	 *  true DConfigBean.  
	 *  
	 *  !PW I amended this code to forward property notifications
	 *  to the webAppRoot parent DConfigBean.
	 */
	/**
	 * @return PropertyChangeSupport object used by this bean.
	 */    
	protected java.beans.PropertyChangeSupport getPCS() {
		return webAppRoot.getPCS();
	}

	/**
	 * @return VetoableChangeSupport object used by this bean.
	 */    
	protected java.beans.VetoableChangeSupport getVCS() {
		return webAppRoot.getVCS();
	}	
}
