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
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;

import org.netbeans.modules.j2ee.sun.share.Constants;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Cache;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping;
import org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;


/** Property structure of cache from DTD
 *
 *		cache : Cache?
 *			[attr: max-entries CDATA 4096]
 *			[attr: timeout-in-seconds CDATA 30]
 *			[attr: enabled CDATA false]  // default is true for 8.1 onward
 *			property : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description : String?
 *			default-helper : DefaultHelper?
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			cache-helper : CacheHelper[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: class-name CDATA #REQUIRED ]
 *				property : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description : String?
 *			cache-mapping : CacheMapping[0,n]
 *				| servlet-name : String
 *				| url-pattern : String
 *				| cache-helper-ref : String
 *				| dispatcher <dispatcher> : String[0,n] // 8.1 onward
 *				| timeout : String?
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.attribute]
 *				| refresh-field : Boolean?
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	EMPTY : String
 *				| http-method : String[0,n]
 *				| key-field : Boolean[0,n]
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	EMPTY : String
 *				| constraint-field : ConstraintField[0,n]
 *				| 	[attr: name CDATA #REQUIRED ]
 *				| 	[attr: scope CDATA request.parameter]
 *				| 	[attr: cache-on-match CDATA true]
 *				| 	[attr: cache-on-match-failure CDATA false]
 *				| 	constraint-field-value : String[0,n]
 *				| 		[attr: match-expr CDATA equals]
 *				| 		[attr: cache-on-match CDATA true]
 *				| 		[attr: cache-on-match-failure CDATA false]
 *
 * This object is a "wanna be" DConfigBean.  JSR-88 prevents us from having a real
 * DConfigBean for the cache block because there is no J2EE deployment descriptor
 * field to bind it to.  I am working with the server team to come up with an
 * extension that would allow this, but in the meantime, Cache needed it's own
 * JavaBean oriented object.  The implementation is very similar to a regular
 * DConfigBean but has been wired into the structure differently.
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebAppCache {
    
	/** Resource bundle 
	 */
	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.Bundle");	// NOI18N
	
	private static final String SunWebFileName = "sun-web.xml";	// NOI18N
	
	/** takes place of Base.parent, which we don't have */
	private WebAppRoot webAppRoot;
	
	/** Holds value of property cacheMaxEntries. */
	private String cacheMaxEntries;
    
	/** Holds value of property timeoutInSeconds. */
	private String timeoutInSeconds;
    
	/** Holds value of property cacheEnabled. */
	private String cacheEnabled;
    
	/** Holds List of WebProperty's. */
	private List properties;
    
	/** Holds value of property defaultHelper. */
	private DefaultHelper defaultHelper;
    
	/** Holds List of CacheHelpers. */
	private List cacheHelpers;
    
	/** Holds List of CacheMappings. */
	private List cacheMappings;
	
    /** Creates a new instance of WebAppCache */
	public WebAppCache() {
	}
	
	/** Override init to load from persistent storage if necessary
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
/*
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		loadFromPlanFile(getConfig());
	}
 */
	protected void init(WebAppRoot parent) {
		webAppRoot = parent;
		
//		loadFromPlanFile(config); // !PW handled by WebAppRoot
	}
	
	/** -----------------------------------------------------------------------
	 *  Validation support
	 */
	/** isValid represents the valid state of the bean:
	 *    null: unknown
	 *    TRUE: bean is valid
	 *    FALSE: bean has at least one invalid field.
	 */
	private Boolean isValid = null;
	
	protected List validationFieldList = new ArrayList();
	
	public static final String FIELD_CACHE_MAX_ENTRIES=":max-entries";
	public static final String FIELD_CACHE_TIMEOUT=":timeout";
	
	protected void updateValidationFieldList() {
		validationFieldList.add(FIELD_CACHE_MAX_ENTRIES);
		validationFieldList.add(FIELD_CACHE_TIMEOUT);
	}
	
	public void validationStateChanged(Boolean newState) {
		isValid = newState;
	}
	
	/** Returns previous result of validateFields() or invokes method if status is
	 *  out of date.
	 *
	 *  @return true if valid, false otherwise. 
	 */
	public boolean isValid() {
		if(isValid == null) {
			boolean tempValid = validateFields(true);
			isValid = Boolean.valueOf(tempValid);
		}
		
		return isValid.booleanValue();
	}
	
	/** Validate the fields managed by this bean.  Used by the customizers 
	 *  (and possibly incremental deployment.)
	 *
	 * @return true or false as to whether bean is valid or not.
	 */
	public boolean validateFields(boolean shortCircuit) {
		ErrorMessageDB messageDB = webAppRoot.getMessageDB();
		boolean result = true;
		
		messageDB.clearErrors();
		for(Iterator iter = validationFieldList.iterator(); iter.hasNext() && (result || !shortCircuit); ) {
			boolean fieldResult = validateField((String) iter.next());
			result = result && fieldResult;
		}
		
		return result;
	}

	/** Validate a single field managed by this bean.  Used by the customizers
	 *  (and possibly incremental deployment.)
	 *
	 * @param field Field spec (xpath to this field in DTD, should be defined
	 *   constant in bean class.)
	 * @return true or false as to whether field is valid or not.
	 */
	public boolean validateField(String fieldId) {
		ValidationError error = null;
		
		if(fieldId.equals(FIELD_CACHE_MAX_ENTRIES)) {
			String absoluteFieldXpath = getAbsoluteXpath(fieldId);
			
			if(Utils.notEmpty(cacheMaxEntries)) {
				try {
					int value = Integer.parseInt(cacheMaxEntries);
					if(value < 0) {
						Object [] args = new Object[2];
						args[0] = cacheMaxEntries;
						args[1] = "0"; // NOI18N
						String message = MessageFormat.format(bundle.getString("ERR_NumberTooLow"), args);	// NOI18N
						error = ValidationError.getValidationError(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath, message);
					}
				} catch(NumberFormatException ex) {
					Object [] args = new Object[1];
					args[0] = cacheMaxEntries; // NOI18N
					String message = MessageFormat.format(bundle.getString("ERR_NumberInvalid"), args);	// NOI18N
					error = ValidationError.getValidationError(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath, message);
				}
			}
			
			if(error == null) {
				error = ValidationError.getValidationErrorMask(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath);
			}
		} else if(fieldId.equals(FIELD_CACHE_TIMEOUT)) {
			String absoluteFieldXpath = getAbsoluteXpath(fieldId);
			
			if(Utils.notEmpty(timeoutInSeconds)) {
				try {
					int value = Integer.parseInt(timeoutInSeconds);
					if(value < -1) {
						Object [] args = new Object[2];
						args[0] = timeoutInSeconds;
						args[1] = "-1"; // NOI18N
						String message = MessageFormat.format(bundle.getString("ERR_NumberTooLow"), args);	// NOI18N
						error = ValidationError.getValidationError(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath, message);
					}
				} catch(NumberFormatException ex) {
					Object [] args = new Object[1];
					args[0] = timeoutInSeconds; // NOI18N
					String message = MessageFormat.format(bundle.getString("ERR_NumberInvalid"), args);	// NOI18N
					error = ValidationError.getValidationError(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath, message);
				}
			}
			
			if(error == null) {
				error = ValidationError.getValidationErrorMask(ValidationError.PARTITION_CACHE_GENERAL, absoluteFieldXpath);
			}
		}
		
		if(error != null) {
			webAppRoot.getMessageDB().updateError(error);
		}
		
		// return true if there was no error added
		return (error == null || !Utils.notEmpty(error.getMessage()));
	}
	
	protected String getAbsoluteXpath(String field) {
		StringBuffer buf = new StringBuffer(field.length() + 20);
		buf.append("/sun-web-app/cache/");	// NOI18N
		buf.append(field);
		return buf.toString();
	}
	
	/** -----------------------------------------------------------------------
	 */
	
	/** Getter for property cacheMaxEntries.
	 * @return Value of property cacheMaxEntries.
	 *
	 */
	public String getCacheMaxEntries() {
		return this.cacheMaxEntries;
	}
	
	/** Setter for property cacheMaxEntries.
	 * @param newCacheMaxEntries New value of property cacheMaxEntries.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCacheMaxEntries(String newCacheMaxEntries) throws java.beans.PropertyVetoException {
		if(newCacheMaxEntries == null || newCacheMaxEntries.length() == 0) { // if empty, set to null
			newCacheMaxEntries = null;
		}
		
		String oldCacheMaxEntries = cacheMaxEntries;
		getVCS().fireVetoableChange("cacheMaxEntries", oldCacheMaxEntries, newCacheMaxEntries);	// NOI18N
		cacheMaxEntries = newCacheMaxEntries;
		getPCS().firePropertyChange("cacheMaxEntries", oldCacheMaxEntries, cacheMaxEntries);	// NOI18N
	}

	/** Getter for property timeoutInSeconds.
	 * @return Value of property timeoutInSeconds.
	 *
	 */
	public String getTimeoutInSeconds() {
		return timeoutInSeconds;
	}
	
	/** Setter for property timeoutInSeconds.
	 * @param newTimeoutInSeconds New value of property timeoutInSeconds.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setTimeoutInSeconds(String newTimeoutInSeconds) throws java.beans.PropertyVetoException {
		if(newTimeoutInSeconds == null || newTimeoutInSeconds.length() == 0) { // if empty, set to null
			newTimeoutInSeconds = null;
		}
		
		String oldTimeoutInSeconds = timeoutInSeconds;
		getVCS().fireVetoableChange("timeoutInSeconds", oldTimeoutInSeconds, newTimeoutInSeconds);	// NOI18N
		timeoutInSeconds = newTimeoutInSeconds;
		getPCS().firePropertyChange("timeoutInSeconds", oldTimeoutInSeconds, timeoutInSeconds);		// NOI18N
	}

	/** Getter for property cacheEnabled.
	 * @return Value of property cacheEnabled.
	 *
	 */
	public String getCacheEnabled() {
		return cacheEnabled;
	}
	
	/** Setter for property cacheEnabled.
	 * @param newCacheEnabled New value of property cacheEnabled.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setCacheEnabled(String newCacheEnabled) throws java.beans.PropertyVetoException {
		String oldClassLoader = cacheEnabled;
		getVCS().fireVetoableChange("cacheEnabled", new Boolean(oldClassLoader), new Boolean(newCacheEnabled));	// NOI18N
		cacheEnabled = newCacheEnabled;
		getPCS().firePropertyChange("cacheEnabled", new Boolean(oldClassLoader), new Boolean(cacheEnabled));	// NOI18N
	}
	
	/** Getter for property Property.
	 * @return Value of property pPoperty.
	 *
	 */
	public List getProperties() {
		return properties;
	}
	
	public WebProperty getProperty(int index) {
		return (WebProperty) properties.get(index);
	}
	
	/** Setter for property Property.
	 * @param property New value of property Property.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setProperties(List newProperties) throws java.beans.PropertyVetoException {
        List oldProperties = properties;
        getVCS().fireVetoableChange("properties", oldProperties, newProperties);	// NOI18N
        properties = newProperties;
        getPCS().firePropertyChange("properties", oldProperties, properties);	// NOI18N
    }
    
	public void addProperty(WebProperty newProperty) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("property", null, newProperty);	// NOI18N
		if(properties == null) {
			properties = new ArrayList();
		}
		properties.add(newProperty);
		getPCS().firePropertyChange("property", null, newProperty );	// NOI18N
	}
	
	public void removeProperty(WebProperty oldProperty) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("property", oldProperty, null);	// NOI18N
		properties.remove(oldProperty);
		getPCS().firePropertyChange("property", oldProperty, null );	// NOI18N
	}
	
	/** Getter for property defaultHelper.
	 * @return Value of property defaultHelper.
	 */
	public DefaultHelper getDefaultHelper() {
		return this.defaultHelper;
	}
	
	/** Setter for property defaultHelper.
	 * @param newDefaultHelper New value of property defaultHelper.
	 *
	 * @throws PropertyVetoException
	 */
	public void setDefaultHelper(DefaultHelper newDefaultHelper) throws java.beans.PropertyVetoException {
		if(newDefaultHelper == null) {
			newDefaultHelper = StorageBeanFactory.getDefault().createDefaultHelper();
		}
		
		DefaultHelper oldDefaultHelper = this.defaultHelper;
		getVCS().fireVetoableChange("defaultHelper", oldDefaultHelper, newDefaultHelper);	// NOI18N
		this.defaultHelper = newDefaultHelper;
		getPCS().firePropertyChange("defaultHelper", oldDefaultHelper, defaultHelper);	// NOI18N
	}
	
	/** Getter for property cacheHelpers.
	 * @return Value of property cacheHelpers.
	 *
	 */
	public List getCacheHelpers() {
		return cacheHelpers;
	}
	
	public CacheHelper getCacheHelper(int index) {
		return (CacheHelper) cacheHelpers.get(index);
	}
	
	/** Setter for property cacheHelpers.
	 * @param newCacheHelpers New value of property cacheHelpers.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setCacheHelpers(List newCacheHelpers) throws java.beans.PropertyVetoException {
        List oldCacheHelpers = cacheHelpers;
        getVCS().fireVetoableChange("cacheHelpers", oldCacheHelpers, newCacheHelpers);	// NOI18N
        cacheHelpers = newCacheHelpers;
        getPCS().firePropertyChange("cacheHelpers", oldCacheHelpers, cacheHelpers);	// NOI18N
    }
    
	public void addProperty(CacheHelper newCacheHelper) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("cacheHelper", null, newCacheHelper);	// NOI18N
		if(cacheHelpers == null) {
			cacheHelpers = new ArrayList();
		}
		cacheHelpers.add(newCacheHelper);
		getPCS().firePropertyChange("cacheHelper", null, newCacheHelper);	// NOI18N
	}
	
	public void removeProperty(CacheHelper oldCacheHelper) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("cacheHelper", oldCacheHelper, null);	// NOI18N
		cacheHelpers.remove(oldCacheHelper);
		getPCS().firePropertyChange("cacheHelper", oldCacheHelper, null );	// NOI18N
	}	

	/** Getter for property cacheMappings.
	 * @return Value of property cacheMappings.
	 *
	 */
	public List getCacheMappings() {
		return cacheMappings;
	}
	
	public CacheMapping getCacheMapping(int index) {
		return (CacheMapping) cacheMappings.get(index);
	}
	
	/** Setter for property cacheMappings.
	 * @param newCacheMappings New value of property cacheMappings.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setCacheMappings(List newCacheMappings) throws java.beans.PropertyVetoException {
        List oldCacheMappings = cacheMappings;
        getVCS().fireVetoableChange("cacheMappings", oldCacheMappings, newCacheMappings);	// NOI18N
        cacheMappings = newCacheMappings;
        getPCS().firePropertyChange("cacheMappings", oldCacheMappings, cacheMappings);	// NOI18N
    }
    
	public void addCacheMapping(CacheMapping newCacheMapping) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("cacheMapping", null, newCacheMapping);	// NOI18N
		if(cacheMappings == null) {
			cacheMappings = new ArrayList();
		}
		cacheMappings.add(newCacheMapping);
		getPCS().firePropertyChange("cacheMapping", null, newCacheMapping);	// NOI18N
	}
	
	public void removeCacheMapping(CacheMapping oldCacheMapping) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("cacheMapping", oldCacheMapping, null);	// NOI18N
		cacheMappings.remove(oldCacheMapping);
		getPCS().firePropertyChange("cacheMapping", oldCacheMapping, null );	// NOI18N
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
                Cache cache = StorageBeanFactory.getDefault().createCache_NoDefaults();
                
				// simple properties
				if(cacheMaxEntries != null) {
					cache.setMaxEntries(cacheMaxEntries);
				}
				
				if(timeoutInSeconds != null) {
					cache.setTimeoutInSeconds(timeoutInSeconds);
				}
				
				if(cacheEnabled != null) {
					cache.setEnabled(cacheEnabled);
				}
				
				// cache helpers
				CacheHelper [] helpers = (CacheHelper []) 
					Utils.listToArray(getCacheHelpers(), CacheHelper.class);
				if(helpers != null) {
					cache.setCacheHelper(helpers);
				}
				
				// default helper
				DefaultHelper dh = getDefaultHelper();
				if(dh.sizeWebProperty() > 0) {
					cache.setDefaultHelper((DefaultHelper) dh.clone());
				}
				
				// properties
				WebProperty [] webProps = (WebProperty []) 
					Utils.listToArray(getProperties(), WebProperty.class);
				if(webProps != null) {
					cache.setWebProperty(webProps);
				}
				
				// cache mappings
				CacheMapping [] mappings = (CacheMapping []) 
					Utils.listToArray(getCacheMappings(), CacheMapping.class);
				if(mappings != null) {
					cache.setCacheMapping(mappings);
				}
				
				return cache;
			}

			public boolean hasDDSnippet() {
				if(getCacheMaxEntries() != null) {
					return true;
				}
				
				if(getTimeoutInSeconds() != null) {
					return true;
				}
				
				if(getCacheEnabled() != null) {
					return true;
				}
				
				List cacheHelpers = getCacheHelpers();
				if(cacheHelpers != null && cacheHelpers.size() > 0) {
					return true;
				}
				
				DefaultHelper dh = getDefaultHelper();
				if(dh.sizeWebProperty() > 0) {
					return true;
				}
				
				List properties = getProperties();
				if(properties != null && properties.size() > 0) {
					return true;
				}
				
				List cacheMappings = getCacheMappings();
				if(cacheMappings != null && cacheMappings.size() > 0) {
					return true;
				}
				
				return false;
			}
			
			public String getPropertyName() {
				return SunWebApp.CACHE;
			}

/** ---------------------------------------------------------------------------
 *  The following methods would have been inherited from DefaultSnippet if we
 *  could derive from that class.  (Another artifact of not being a real DConfigBean
 */			
			public String getFileName() {
				return SunWebFileName; // NOI18N
			}
			
			public CommonDDBean mergeIntoRootDD(CommonDDBean ddRoot) {
				Cache cache = (Cache) getDDSnippet();
				
				if(ddRoot instanceof SunWebApp) {
					SunWebApp swa = (SunWebApp) ddRoot;
					swa.setCache(cache);
				}
				
				return cache;
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
		
	private class CacheFinder implements ConfigFinder {
		public Object find(Object obj) {
			Cache result = null;
			
			if(obj instanceof SunWebApp) {
				SunWebApp swa = (SunWebApp) obj;
				result = swa.getCache();
			}
			
			return result;
		}
	}
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = webAppRoot.getUriText();
		
		Cache beanGraph = (Cache) config.getBeans(
			uriText, SunWebFileName, null, new CacheFinder());
		
		clearProperties();
		
		if(null != beanGraph) {
			cacheMaxEntries = beanGraph.getMaxEntries();
			timeoutInSeconds = beanGraph.getTimeoutInSeconds();
			cacheEnabled = beanGraph.getEnabled();

			cacheHelpers = Utils.arrayToList(beanGraph.getCacheHelper());

			DefaultHelper dh = beanGraph.getDefaultHelper();
			if(dh != null && dh.sizeWebProperty() > 0) {
				defaultHelper = (DefaultHelper) dh.clone();
			}
			
			properties = Utils.arrayToList(beanGraph.getWebProperty());
			cacheMappings = Utils.arrayToList(beanGraph.getCacheMapping());
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		cacheMaxEntries = null;
		timeoutInSeconds = null;
		cacheEnabled = null;
		cacheHelpers = null;
		defaultHelper = StorageBeanFactory.getDefault().createDefaultHelper();
		properties = null;
		cacheMappings = null;
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
