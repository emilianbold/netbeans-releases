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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader;
import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.openide.ErrorManager;


/** Property structure of WebAppRoot from DTD (sections that are handled
 *    by child DConfigBeans have been removed.):
 *
 *	sun-web-app : SunWebApp
 *		context-root : String?
 *		class-loader : Boolean?
 *			[attr: extra-class-path CDATA #IMPLIED ]
 *			[attr: delegate CDATA true]
 *			EMPTY : String
 *		jsp-config : JspConfig?
 *			property : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description : String?
 *		locale-charset-info : LocaleCharsetInfo?
 *			[attr: default-locale CDATA #REQUIRED ]
 *			locale-charset-map : LocaleCharsetMap[1,n]
 *				[attr: locale CDATA #REQUIRED ]
 *				[attr: agent CDATA #IMPLIED ]
 *				[attr: charset CDATA #REQUIRED ]
 *				description : String?
 *			parameter-encoding : Boolean?
 *				[attr: form-hint-field CDATA #IMPLIED ]
 *				[attr: default-charset CDATA #IMPLIED ]
 *				EMPTY : String
 *		property : WebProperty[0,n]
 *			[attr: name CDATA #REQUIRED ]
 *			[attr: value CDATA #REQUIRED ]
 *			description : String?
 *		message-destination : MessageDestination[0,n]
 *			message-destination-name : String
 *			jndi-name : String
 *		webservice-description : WebserviceDescription[0,n]
 *			webservice-description-name : String
 *			wsdl-publish-location : String?
 *
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebAppRoot extends BaseRoot implements javax.enterprise.deploy.spi.DConfigBean {
    
	public static final String SERVLET_LIST_CHANGED = "ServletListChanged"; //NOI18N
	public static final String SERVICE_REF_LIST_CHANGED = "ServiceRefListChanged"; //NOI18N
	public static final String RESOURCE_REF_LIST_CHANGED = "ResourceRefListChanged"; //NOI18N
	
	private static final String JSPCONFIG_CLASSDEBUGINFO="classdebuginfo"; //NOI18N
	private static final String JSPCONFIG_MAPPEDFILE="mappedfile"; //NOI18N
	
	public WebAppRoot() {
		setDescriptorElement(bundle.getString("BDN_WebAppRoot"));	// NOI18N	
	}	
	
	protected void init(DDBeanRoot dDBean, SunONEDeploymentConfiguration parent, DDBean ddbExtra) throws ConfigurationException {
		super.init(dDBean, parent, ddbExtra);
		
		sessionConfigBean = new SessionConfigSubBean();
		sessionConfigBean.init(this);
		
		cacheBean = new WebAppCache();
		cacheBean.init(this);
		
		loadFromPlanFile(parent);
	}
	
	/** -----------------------------------------------------------------------
	 *  Validation implementation
	 */
	
	// relative xpaths (double as field id's)
//	public static final String FIELD_CONTEXT_ROOT="context-root";
	public static final String FIELD_FORM_HINT="locale-charset-info/parameter-encoding/form-hint-field";

	protected void updateValidationFieldList() {
		super.updateValidationFieldList();
//		validationFieldList.add(FIELD_CONTEXT_ROOT);
		validationFieldList.add(FIELD_FORM_HINT);
	}
	
	public boolean validateField(String fieldId) {
		ValidationError error = null;
		
		// !PW use visitor pattern to get rid of switch/if statement for validation
		//     field -- data member mapping.
		//
/* According to DTD, context root can be empty.
		if(fieldId.equals(FIELD_CONTEXT_ROOT)) {
			// validation version will be:
			//   expand relative field id to full xpath id based on current context
			//   lookup validator for this field in field validator DB
			//   execute validator
			//   add any validation errors to database
			//
			String absoluteFieldXpath = getAbsoluteXpath(fieldId);
			
			if(!Utils.notEmpty(contextRoot)) {
				error = ValidationError.getValidationError(ValidationError.PARTITION_WEB_GENERAL, absoluteFieldXpath, "Context root cannot be empty.");
			} else {
				error = ValidationError.getValidationErrorMask(ValidationError.PARTITION_WEB_GENERAL, absoluteFieldXpath);
			}
		} else 
*/
		if(fieldId.equals(FIELD_FORM_HINT)) {
			String absoluteFieldXpath = getAbsoluteXpath(fieldId);
			
			if(localeInfo != null) {
				String formHint = localeInfo.getParameterEncodingFormHintField();
				if(Utils.notEmpty(formHint)) {
					if(!Utils.isJavaIdentifier(formHint)) {
						Object [] args = new Object[1];
						args[0] = "form-hint-field"; // NOI18N
						String message = MessageFormat.format(bundle.getString("ERR_NotValidIdentifier"), args); // NOI18N
						error = ValidationError.getValidationError(ValidationError.PARTITION_WEB_LOCALE, absoluteFieldXpath, message);
					}
				}
			}
			
			if(error == null) {
				error = ValidationError.getValidationErrorMask(ValidationError.PARTITION_WEB_LOCALE, absoluteFieldXpath);
			}
		}
		
		if(error != null) {
			getMessageDB().updateError(error);
		}
		
		// return true if there was no error added
		return (error == null || !Utils.notEmpty(error.getMessage()));
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_WebAppGeneral";
	}
	
	/** Get the servlet version of this module.
	 *
	 * @return ServletVersion enum for the version of this module.
	 */
	public J2EEBaseVersion getJ2EEModuleVersion() {
		DDBeanRoot ddbRoot = (DDBeanRoot) getDDBean();
		
		// From JSR-88 1.1
		String versionString = ddbRoot.getDDBeanRootVersion();
		if(versionString == null) {
			// If the above doesn't get us what we want.
			versionString = ddbRoot.getModuleDTDVersion();
		}
		
		J2EEBaseVersion servletVersion = ServletVersion.getServletVersion(versionString);
		if(servletVersion == null) {
			// Default to Servlet 2.4 if we can't find out what version this is.
			servletVersion = ServletVersion.SERVLET_2_4;
		}
		
		return servletVersion;
	}
	
	/** !PW FIXME Workaround for broken XpathEvent.BEAN_ADDED not being sent.
	 *  Override this method (see WebAppRoot) to be notified if a child bean
	 *  is created.  See IZ 41214
	 */
	protected void beanAdded(String xpath) {
		super.beanAdded(xpath);
		
		if("/web-app/servlet".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVLET_LIST_CHANGED, false, true);
		}

		if("/web-app/service-ref".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVICE_REF_LIST_CHANGED, false, true);
		}

		if("/web-app/resource-ref".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVICE_REF_LIST_CHANGED, false, true);
		}
	}
	
	/** !PW FIXME Workaround for broken XpathEvent.BEAN_REMOVED not being sent.
	 *  Override this method (see WebAppRoot) to be notified if a child bean
	 *  is destroyed.  See IZ 41214
	 */
	protected void beanRemoved(String xpath) {
		super.beanRemoved(xpath);
		
		if("/web-app/servlet".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVLET_LIST_CHANGED, false, true);
		}

		if("/web-app/service-ref".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVICE_REF_LIST_CHANGED, false, true);
		}

		if("/web-app/resource-ref".equals(xpath)) {	// NOI18N
			getPCS().firePropertyChange(SERVICE_REF_LIST_CHANGED, false, true);
		}
	}	
	
	/** Retrieves a list of the servlet child DConfigBeans contained in this
	 *  web application.
	 */
	public List getServlets() {
		List servlets = new ArrayList();
		Collection collection = getChildren();
		for(Iterator iter = collection.iterator(); iter.hasNext(); ) {
			Object child = iter.next();
			if(child instanceof ServletRef) {
				servlets.add(child);
			}
		}
		return servlets;
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
				DDProvider provider = DDProvider.getDefault();
				SunWebApp swa = (SunWebApp) provider.newGraph(SunWebApp.class);

//				ServletVersion servletVersion = (ServletVersion) getJ2EEModuleVersion();
//				swa.graphManager().setDoctype(servletVersion.getSunPublicId(), servletVersion.getSunSystemId());
				
				if(contextRoot != null) {
					swa.setContextRoot(contextRoot);
				}
                
				if(errorUrl != null) {
					try {
						swa.setErrorUrl(errorUrl);
					} catch(VersionNotSupportedException ex) {
						// should not happen w/ 8.1 files.
					}
				}

                                if(classLoader != null) {
                                    if(classLoader.toString().equals("true")){       //NOI18N
                                        try {
                                            MyClassLoader webClassLoader = swa.newMyClassLoader();

                                            if(delegate != null) {
                                                webClassLoader.setDelegate(Boolean.toString(isDelegate()));
                                            } else {
                                                webClassLoader.setDelegate(Boolean.TRUE.toString());
                                            }

                                            if(Utils.notEmpty(getExtraClassPath())) {
                                                webClassLoader.setExtraClassPath(getExtraClassPath());
                                            }

                                            swa.setMyClassLoader(webClassLoader);
                                        } catch(VersionNotSupportedException ex) {
                                            // Should not happen, but we have to catch it for now.
                                        }
                                    }
                                }
                                
				try {
					int numPatterns = idempotentUrlPattern.sizeIdempotentUrlPattern();
					if(numPatterns > 0) {
						swa.setIdempotentUrlPattern(new boolean[numPatterns]);
						for(int i = 0; i < numPatterns; i++) {
							swa.setIdempotentUrlPatternUrlPattern(i, idempotentUrlPattern.getIdempotentUrlPatternUrlPattern(i));
							swa.setIdempotentUrlPatternNumOfRetries(i, idempotentUrlPattern.getIdempotentUrlPatternNumOfRetries(i));
						}
					}
				} catch(VersionNotSupportedException ex) {
					//Should never happen
				}

				JspConfig jc = getJspConfig();
				if(jc.sizeWebProperty() > 0) {
					swa.setJspConfig((JspConfig) jc.clone());
				}

				WebProperty [] webProps = (WebProperty []) 
					Utils.listToArray(getProperties(), WebProperty.class);
				if(webProps != null) {
					swa.setWebProperty(webProps);
				}
				
				MessageDestination [] msgDests = (MessageDestination []) 
					Utils.listToArray(getMessageDestinations(), MessageDestination.class);
				if(msgDests != null) {
					swa.setMessageDestination(msgDests);
				}
				
				WebserviceDescription [] wsDescrs = (WebserviceDescription []) 
					Utils.listToArray(getWebServiceDescriptions(), WebserviceDescription.class);
				if(wsDescrs != null) {
					swa.setWebserviceDescription(wsDescrs);
				}
				
				if(localeInfo.sizeLocaleCharsetMap() > 0 || 
					Utils.notEmpty(localeInfo.getDefaultLocale()) ||
					Utils.notEmpty(localeInfo.getParameterEncodingDefaultCharset()) ||
					Utils.notEmpty(localeInfo.getParameterEncodingFormHintField())
					) {
					swa.setLocaleCharsetInfo((LocaleCharsetInfo) localeInfo.clone());
				}
				
				// SessionConfig snippet is retrieved below and added to the snippet list
				// for WebAppRoot.
				
				// Cache snippet is retrieved below and added to the snippet list
				// for WebAppRoot.
				
				return swa;
			}
		};
		
		snippets.add(snipOne);
		snippets.addAll(sessionConfigBean.getSnippets());
		snippets.addAll(cacheBean.getSnippets());
		return snippets;
	}
   
	private class WebAppRootParser implements ConfigParser {
		public Object parse(java.io.InputStream stream) {
            DDProvider provider = DDProvider.getDefault();
			SunWebApp result = null;
            
            if(null != stream) {
                try {
                    result = provider.getWebDDRoot(new org.xml.sax.InputSource(stream));
                } catch (Exception ex) {
                    jsr88Logger.severe("invalid stream for SunWebApp"); // FIXME
                }
            }
            
            // If we have a null stream or there is a problem reading the graph,
            // return a blank graph.
            if(result == null) {
                result = (SunWebApp) provider.newGraph(SunWebApp.class);
            }
            
            return result;
		}
	}
	
	private class WebAppRootFinder implements ConfigFinder {
		public Object find(Object obj) {
			SunWebApp result = null;
			
			if(obj instanceof SunWebApp) {
				result = (SunWebApp) obj;
			}
			
			return result;
		}
	}
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		
		SunWebApp beanGraph = (SunWebApp) config.getBeans(uriText, constructFileName(),
			new WebAppRootParser(), new WebAppRootFinder());
		
		clearProperties();
		
		if(null != beanGraph) {
			contextRoot = beanGraph.getContextRoot();
            
			try {
				errorUrl = beanGraph.getErrorUrl();
			} catch(VersionNotSupportedException ex) {
				errorUrl = "";
			}
            
                        try {
                            MyClassLoader myClassLoader = beanGraph.getMyClassLoader();
                            if(myClassLoader != null) {
                                delegate = Utils.booleanValueOf(myClassLoader.getDelegate()) ? Boolean.TRUE : Boolean.FALSE;
                                extraClassPath = myClassLoader.getExtraClassPath();
                                if((delegate != null) || (extraClassPath != null)) {
                                    classLoader = Boolean.TRUE;
                                }
                            }
                        } catch(VersionNotSupportedException ex) {
                            //Should never happen
                            delegate = Boolean.TRUE;
                            classLoader = Boolean.TRUE;
                        }
                        
                        try {
                            int numPatterns = beanGraph.sizeIdempotentUrlPattern();
                            if(numPatterns > 0) {
                                idempotentUrlPattern.setIdempotentUrlPattern(new boolean[numPatterns]);
                                for(int i = 0; i < numPatterns; i++) {
                                    idempotentUrlPattern.setIdempotentUrlPatternUrlPattern(i, beanGraph.getIdempotentUrlPatternUrlPattern(i));
                                    idempotentUrlPattern.setIdempotentUrlPatternNumOfRetries(i, beanGraph.getIdempotentUrlPatternNumOfRetries(i));
                                }
                            }
                        } catch(VersionNotSupportedException ex) {
                            //Should never happen
                        }
                        
			JspConfig jc = beanGraph.getJspConfig();
			if(jc != null && jc.sizeWebProperty() > 0) {
				jspConfig = (JspConfig) jc.clone();
			}
			
			properties = Utils.arrayToList(beanGraph.getWebProperty());
			messageDestinations = Utils.arrayToList(beanGraph.getMessageDestination());
			webServiceDescriptions = Utils.arrayToList(beanGraph.getWebserviceDescription());
			
			// Save any portion of the locale that has been specifie, regardless
			// of whether it is DTD valid or not (e.g. we could save default locale
			// without any entries in the locale-charset-mapping table.)
			//
			LocaleCharsetInfo info = beanGraph.getLocaleCharsetInfo();
			if(info != null &&
				(info.sizeLocaleCharsetMap() > 0 || 
				 Utils.notEmpty(info.getDefaultLocale()) ||
				 Utils.notEmpty(info.getParameterEncodingDefaultCharset()) ||
				 Utils.notEmpty(info.getParameterEncodingFormHintField())
					)) {
				localeInfo = (LocaleCharsetInfo) info.clone();
			}
		} else {
			setDefaultProperties();
		}

		// Now load session configuration pseudo DConfigBean
		sessionConfigBean.loadFromPlanFile(config);
		
		// Now load cache pseudo DConfigBean
		cacheBean.loadFromPlanFile(config);
		
		return (beanGraph != null);
	}	
	
	protected void clearProperties() {
		DDProvider provider = DDProvider.getDefault();
		StorageBeanFactory beanFactory = StorageBeanFactory.getDefault();
        
		contextRoot = null;
		errorUrl = null;
		extraClassPath = null;
		idempotentUrlPattern = (SunWebApp) provider.newGraph(SunWebApp.class);
		jspConfig = beanFactory.createJspConfig();
		properties = null;
		messageDestinations = null;
		webServiceDescriptions = null;
		localeInfo = beanFactory.createLocaleCharsetInfo();

                classLoader = Boolean.FALSE;
                delegate = Boolean.FALSE;
	}
	
	protected void setDefaultProperties() {
		StorageBeanFactory beanFactory = StorageBeanFactory.getDefault();
        
		// Add two properties to make developing and debugging JSP's easier by
		// by default for new web applications.]
		WebProperty classDebugInfoProperty = beanFactory.createWebProperty();
		classDebugInfoProperty.setName(JSPCONFIG_CLASSDEBUGINFO);
		classDebugInfoProperty.setValue("true");	 // NOI18N
		classDebugInfoProperty.setDescription(bundle.getString("DESC_ClassDebugInfo"));	// NOI18N
		jspConfig.addWebProperty(classDebugInfoProperty);

		WebProperty mappedFileProperty = beanFactory.createWebProperty();
		mappedFileProperty.setName(JSPCONFIG_MAPPEDFILE);
		mappedFileProperty.setValue("true");		// NOI18N
		mappedFileProperty.setDescription(bundle.getString("DESC_MappedFile"));	// NOI18N
		jspConfig.addWebProperty(mappedFileProperty);

                classLoader = Boolean.TRUE;
                delegate = Boolean.TRUE;

		// errorUrl is required for SJSAS 8.1
		errorUrl = "";
	}	
	
	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
	private HashMap webAppRootFactoryMap;
	
    /** Retrieve the XPathToFactory map for this DConfigBean.  For AppRoot,
	 *  this maps application xpaths to factories for other contained root
	 *  objects plus a SecurityRoleModel factory
     * @return
     */  
	protected java.util.Map getXPathToFactoryMap() {
		if(webAppRootFactoryMap == null) {
			webAppRootFactoryMap = new HashMap(17);

			webAppRootFactoryMap.put("ejb-ref", new DCBGenericFactory(EjbRef.class));						// NOI18N
			webAppRootFactoryMap.put("resource-env-ref", new DCBGenericFactory(ResourceEnvRef.class));		// NOI18N
			webAppRootFactoryMap.put("resource-ref", new DCBGenericFactory(ResourceRef.class));				// NOI18N
			webAppRootFactoryMap.put("security-role", new DCBGenericFactory(SecurityRoleMapping.class));	// NOI18N
			webAppRootFactoryMap.put("servlet", new DCBGenericFactory(ServletRef.class));					// NOI18N
			
// Removed as genuine DConfigBean due to issues with NetBeans DDEditor & web.xml			
//			webAppRootFactoryMap.put("session-config", new DCBGenericFactory(SessionConfiguration.class));	// NOI18N
			
			if(getJ2EEModuleVersion().compareTo(ServletVersion.SERVLET_2_4) >= 0) {
				webAppRootFactoryMap.put("service-ref", new DCBGenericFactory(ServiceRef.class));			// NOI18N
			}
 		}
		
		return webAppRootFactoryMap;
	}	

	/* ------------------------------------------------------------------------
	 * Property support
	 */
	
	/** Holds value of property contextRoot. */
	private String contextRoot;
	
	/** Holds value of property errorUrl. */
	private String errorUrl;
    
	/** Holds value of property classLoader. */
	private Boolean classLoader;
	
	/** Holds value of property extraClassPath. */
	private String extraClassPath;
	
	/** Holds value of property delegate. */
	private Boolean delegate;

    /** Holds value of property idempotentUrlPattern */
    private SunWebApp idempotentUrlPattern;
        
	/** Holds value of property jspConfig. */
	private JspConfig jspConfig;
	
	/** Holds list of WebProperty properties. */
	private List properties;
	
	/** Holds list of MessageDestination properties. */
	private List messageDestinations;
	
	/** Holds list of WebServiceDescription properties. */
	private List webServiceDescriptions;
	
	/* Holds value of property LocaleCharsetInfo. */
	private LocaleCharsetInfo localeInfo;
	
	/* Holds value of property SessionConfig. */
	private SessionConfigSubBean sessionConfigBean;
	
	/* Holds value of property Cache. */
	private WebAppCache cacheBean;
	
	/** Getter for property contextRoot.
	 * @return Value of property contextRoot.
	 *
	 */
	public String getContextRoot() {
		return contextRoot;
	}
	
	/** Setter for property contextRoot.
	 * @param contextRoot New value of property contextRoot.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setContextRoot(String newContextRoot) throws java.beans.PropertyVetoException {
                if (newContextRoot!=null){
                    newContextRoot = newContextRoot.replace (' ', '_'); //NOI18N
                }
                if (newContextRoot!=null){ //see bug 56280
                    try{
                        String result="";
                        String s[] = newContextRoot.split("/");
                        for (int i=0;i<s.length;i++){
                            result=result+java.net.URLEncoder.encode(s[i], "UTF-8");
                            if (i!=s.length -1)
                                result=result+"/";
                        }
                        newContextRoot= result;
                    }
                    catch (Exception e){
                        
                    }
                }
		String oldContextRoot = contextRoot;
		getVCS().fireVetoableChange("contextRoot", oldContextRoot, newContextRoot);
		contextRoot = newContextRoot;
		getPCS().firePropertyChange("contextRoot", oldContextRoot, contextRoot);
	}
	
    /** Getter for property errorUrl.
     * @return Value of property errorUrl.
     *
     */
    public String getErrorUrl() {
        return errorUrl;
    }

    /** Setter for property errorUrl.
     * @param newErrorUrl New value of property errorUrl.
     *
     * @throws PropertyVetoException
     *
     */
    public void setErrorUrl(String newErrorUrl) throws java.beans.PropertyVetoException {
        String oldErrorUrl = errorUrl;
        getVCS().fireVetoableChange("errorUrl", oldErrorUrl, newErrorUrl);
        errorUrl = newErrorUrl;
        getPCS().firePropertyChange("errorUrl", oldErrorUrl, errorUrl);
    }

	/** Getter for property classLoader.
	 * @return Value of property classLoader.
	 *
	 */
	public boolean isClassLoader() {
		return classLoader.booleanValue();
	}
	
	/** Setter for property classLoader.
	 * @param classLoader New value of property classLoader.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setClassLoader(boolean newClassLoader) throws java.beans.PropertyVetoException {
		Boolean oldClassLoader = classLoader;
		Boolean newClassLoaderAsBoolean = newClassLoader ? Boolean.TRUE : Boolean.FALSE;
		getVCS().fireVetoableChange("classLoader", oldClassLoader, newClassLoaderAsBoolean);
		classLoader = newClassLoaderAsBoolean;
		getPCS().firePropertyChange("classLoader", oldClassLoader, classLoader);
	}
	
	/** Getter for property classLoaderExtraClassPath.
	 * @return Value of property classLoaderExtraClassPath.
	 *
	 */
	public String getExtraClassPath() {
		return extraClassPath;
	}
	
	/** Setter for property classLoaderExtraClassPath.
	 * @param classLoaderExtraClassPath New value of property classLoaderExtraClassPath.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setExtraClassPath(String newExtraClassPath) throws java.beans.PropertyVetoException {
		String oldExtraClassPath = extraClassPath;
		getVCS().fireVetoableChange("extraClassPath", oldExtraClassPath, newExtraClassPath);
		extraClassPath = newExtraClassPath;
		getPCS().firePropertyChange("extraClassPath", oldExtraClassPath, extraClassPath);
	}
	
	/** Getter for property classLoaderDelegate.
	 * @return Value of property classLoaderDelegate.
	 *
	 */
	public boolean isDelegate() {
		return delegate.booleanValue();
	}
	
	/** Setter for property classLoaderDelegate.
	 * @param classLoaderDelegate New value of property classLoaderDelegate.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setDelegate(boolean newDelegate) throws java.beans.PropertyVetoException {
		Boolean oldDelegate = delegate;
		Boolean newDelegateAsBoolean = newDelegate ? Boolean.TRUE : Boolean.FALSE;
		getVCS().fireVetoableChange("delegate", oldDelegate, newDelegateAsBoolean);
		delegate = newDelegateAsBoolean;
		getPCS().firePropertyChange("delegate", oldDelegate, delegate);		
	}
        
    /** Getter for property idempotentUrlPattern.
     * @return SunWebApp instance that only holds the list of idempotentUrlPatterns
     *  that have been set.
     */
    public SunWebApp getIdempotentUrlPattern() {
        return idempotentUrlPattern;
    }
    
    /** Setter for property idempotentUrlPattern.
     * @param iup New value of property idempotentUrlPattern
     *
     * @throws PropertyVetoException
     */
    public void setIdempotentUrlPattern(SunWebApp newIdempotentUrlPattern) throws java.beans.PropertyVetoException {
        SunWebApp oldIdempotentUrlPattern = idempotentUrlPattern;
        getVCS().fireVetoableChange("idempotentUrlPatterns", oldIdempotentUrlPattern, newIdempotentUrlPattern);
        idempotentUrlPattern = newIdempotentUrlPattern;
        getPCS().firePropertyChange("idempotentUrlPatterns", oldIdempotentUrlPattern, idempotentUrlPattern);
    }
    
// !PW May not need these.    
//    public void addIdempotentUrlPattern(String urlPattern, String numRetries) throws java.beans.PropertyVetoException {
//        getVCS().fireVetoableChange("idempotentUrlPattern", null, urlPattern);	// NOI18N
//        if(idempotentUrlPattern == null) {
//            DDProvider provider = DDProvider.getDefault();
//            idempotentUrlPattern = (SunWebApp) provider.newGraph(SunWebApp.class);
//        }
//
//        try {
//            int index = idempotentUrlPattern.addIdempotentUrlPattern(true);
//            idempotentUrlPattern.setIdempotentUrlPatternUrlPattern(index, urlPattern);
//            idempotentUrlPattern.setIdempotentUrlPatternNumOfRetries(index, numRetries);
//        } catch (VersionNotSupportedException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        getPCS().firePropertyChange("idempotentUrlPattern", null, urlPattern );	// NOI18N
//    }
//
//    public void removeIdempotentUrlPattern(String urlPattern) throws java.beans.PropertyVetoException {
//        getVCS().fireVetoableChange("idempotentUrlPattern", oldProperty, null);	// NOI18N
//        FIXME unfinished.
//        properties.remove(oldProperty);
//        getPCS().firePropertyChange("idempotentUrlPattern", oldProperty, null );	// NOI18N
//    }
        
	/** Getter for property jspConfig.
	 * @return Value of property jspConfig.
	 *
	 */
	public JspConfig getJspConfig() {
		return jspConfig;
	}
	
	/** Setter for property jspConfig.
	 * @param jspConfig New value of property jspConfig.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setJspConfig(JspConfig newJspConfig) throws java.beans.PropertyVetoException {
		JspConfig oldJspConfig = jspConfig;
		getVCS().fireVetoableChange("jspConfig", oldJspConfig, newJspConfig);
		jspConfig = newJspConfig;
		getPCS().firePropertyChange("jspConfig", oldJspConfig, jspConfig);
	}
	
	/** Getter for property property.
	 * @return Value of property property.
	 *
	 */
	public List getProperties() {
		return properties;
	}
	
	public WebProperty getProperty(int index) {
		return (WebProperty) properties.get(index);
	}
	
	/** Setter for property property.
	 * @param property New value of property property.
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

	/** Getter for property messageDestinations.
	 * @return Value of property messageDestinations.
	 *
	 */
	public List getMessageDestinations() {
		return messageDestinations;
	}
	
	public MessageDestination getMessageDestination(int index) {
		return (MessageDestination) messageDestinations.get(index);
	}
	
	/** Setter for property messageDestinations.
	 * @param messageDestinations New value of property messageDestinations.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setMessageDestinations(List newMessageDestinations) throws java.beans.PropertyVetoException {
        List oldMessageDestinations = messageDestinations;
        getVCS().fireVetoableChange("messageDestinations", oldMessageDestinations, newMessageDestinations);	// NOI18N
        messageDestinations = newMessageDestinations;
        getPCS().firePropertyChange("messageDestinations", oldMessageDestinations, messageDestinations);	// NOI18N
    }
    
	public void addMessageDestination(MessageDestination newMessageDestination) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("messageDestination", null, newMessageDestination);	// NOI18N
		if(messageDestinations == null) {
			messageDestinations = new ArrayList();
		}		
		messageDestinations.add(newMessageDestination);
		getPCS().firePropertyChange("messageDestination", null, newMessageDestination );	// NOI18N
	}
	
	public void removeMessageDestination(MessageDestination oldMessageDestination) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("messageDestination", oldMessageDestination, null);	// NOI18N
		messageDestinations.remove(oldMessageDestination);
		getPCS().firePropertyChange("messageDestination", oldMessageDestination, null );	// NOI18N
	}
	
	/** Getter for property webserviceDescription.
	 * @return Value of property webserviceDescription.
	 *
	 */
	public List getWebServiceDescriptions() {
		return webServiceDescriptions;
	}
	
	public WebserviceDescription getWebServiceDescription(int index) {
		return (WebserviceDescription) webServiceDescriptions.get(index);
	}
	
	/** Setter for property webserviceDescription.
	 * @param webserviceDescription New value of property webserviceDescription.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setWebServiceDescriptions(List newWebServiceDescriptions) throws java.beans.PropertyVetoException {
        List oldWebServiceDescriptions = webServiceDescriptions;
        getVCS().fireVetoableChange("webServiceDescriptions", oldWebServiceDescriptions, newWebServiceDescriptions);	// NOI18N
        webServiceDescriptions = newWebServiceDescriptions;
        getPCS().firePropertyChange("webServiceDescriptions", oldWebServiceDescriptions, webServiceDescriptions);	// NOI18N
    }
    
	public void addWebServiceDescription(WebserviceDescription newWebServiceDescription) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("webServiceDescription", null, newWebServiceDescription);	// NOI18N
		if(webServiceDescriptions == null) {
			webServiceDescriptions = new ArrayList();
		}		
		webServiceDescriptions.add(newWebServiceDescription);
		getPCS().firePropertyChange("webServiceDescription", null, newWebServiceDescription );	// NOI18N
	}
	
	public void removeWebServiceDescription(WebserviceDescription oldWebServiceDescription) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("webServiceDescription", oldWebServiceDescription, null);	// NOI18N
		webServiceDescriptions.remove(oldWebServiceDescription);
		getPCS().firePropertyChange("webServiceDescription", oldWebServiceDescription, null );	// NOI18N
	}
	
	/** Getter for property localeInfo.
	 * @return Value of property localeInfo.
	 *
	 */
	public LocaleCharsetInfo getLocaleCharsetInfo() {
		return localeInfo;
	}
	
	/** Setter for property localeInfo.
	 * @param localeInfo New value of property localeInfo.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setLocaleCharsetInfo(LocaleCharsetInfo newLocaleInfo) throws java.beans.PropertyVetoException {
		LocaleCharsetInfo oldLocaleInfo = localeInfo;
		getVCS().fireVetoableChange("localeInfo", oldLocaleInfo, newLocaleInfo);
		localeInfo = newLocaleInfo;
		getPCS().firePropertyChange("localeInfo", oldLocaleInfo, localeInfo);
	}	
	
	/** Retrieve the java bean that handles the web-app session-config settings
	 * @return Value of sessionConfigBean.
	 *
	 */
	public SessionConfigSubBean getSessionConfigBean() {
		return sessionConfigBean;
	}
	
	/** Retrieve the java bean that handles the web-app cache settings
	 * @return Value of cacheBean.
	 *
	 */
	public WebAppCache getCacheBean() {
		return cacheBean;
	}
}
