/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.text.MessageFormat;
import java.util.Map;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.xml.sax.SAXException;

import org.openide.ErrorManager;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader;
import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;

import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/** Property structure of WebAppRoot from DTD (sections that are handled
 *    by child DConfigBeans have been removed.):
 *
 *	sun-web-app : SunWebApp
 *		[attr: error-url CDATA ]
 *		[attr: httpservlet-security-provider CDATA #IMPLIED ]
 *		contextRoot <context-root> : String[0,1]
 *		idempotentUrlPattern <idempotent-url-pattern> : boolean[0,n]
 *			[attr: url-pattern CDATA #REQUIRED ]
 *			[attr: num-of-retries CDATA -1]
 *			EMPTY : String
 *		classLoader <class-loader> : ClassLoader[0,1]
 *			[attr: extra-class-path CDATA #IMPLIED ]
 *			[attr: delegate ENUM ( yes no on off 1 0 true false ) true]
 *			[attr: dynamic-reload-interval CDATA #IMPLIED ]
 *			webProperty <property> : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description <description> : String[0,1]
 *		jspConfig <jsp-config> : JspConfig[0,1]
 *			property <property> : WebProperty[0,n]
 *				[attr: name CDATA #REQUIRED ]
 *				[attr: value CDATA #REQUIRED ]
 *				description <description> : String[0,1]
 *		localeCharsetInfo <locale-charset-info> : LocaleCharsetInfo[0,1]
 *			[attr: default-locale CDATA #IMPLIED ]
 *			localeCharsetMap <locale-charset-map> : LocaleCharsetMap[1,n]
 *				[attr: locale CDATA #REQUIRED ]
 *				[attr: agent CDATA #IMPLIED ]
 *				[attr: charset CDATA #REQUIRED ]
 *				description <description> : String[0,1]
 *			parameterEncoding <parameter-encoding> : boolean[0,1]
 *				[attr: form-hint-field CDATA #IMPLIED ]
 *				[attr: default-charset CDATA #IMPLIED ]
 *				EMPTY : String
 *		parameterEncoding <parameter-encoding> : boolean[0,1]
 *			[attr: form-hint-field CDATA #IMPLIED ]
 *			[attr: default-charset CDATA #IMPLIED ]
 *			EMPTY : String
 *		property <property> : WebProperty[0,n]
 *			[attr: name CDATA #REQUIRED ]
 *			[attr: value CDATA #REQUIRED ]
 *			description <description> : String[0,1]
 *		message-destination : MessageDestination[0,n]
 *			message-destination-name : String
 *			jndi-name : String
 *
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebAppRoot extends BaseRoot implements javax.enterprise.deploy.spi.DConfigBean {

    /** This property change event is to notify interested systems, particularly
     *  the associated customizer, that list of servlets in web.xml has changed.
     */
	public static final String SERVLET_LIST_CHANGED = "ServletListChanged"; //NOI18N
	
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
	
	public DConfigBean getDConfigBean(DDBeanRoot dDBeanRoot) {
        BaseRoot rootDCBean = null;
        
        J2EEBaseVersion moduleVersion = getJ2EEModuleVersion();
        if(moduleVersion.compareTo(ServletVersion.SERVLET_2_4) >= 0) {
            rootDCBean = createWebServicesRoot(dDBeanRoot);
        }
        
        return rootDCBean;
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
	
    /** Generate a DOCTYPE string for the specified version (which may be different
     *  than the current version of the tree
     */
    public String generateDocType(ASDDVersion version) {
        return generateDocType("sun-web-app", version.getSunWebAppPublicId(), version.getSunWebAppSystemId()); // NOI18N
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
	}	

	/** Retrieves a list of the servlet child DConfigBeans contained in this
	 *  web application.
	 */
	public List getServlets() {
		List servlets = new ArrayList();
		for(Iterator iter = getChildren().iterator(); iter.hasNext(); ) {
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
                SunWebApp swa = getConfig().getStorageFactory().createSunWebApp();
                String version = swa.getVersion().toString();
                
				if(contextRoot != null) {
					swa.setContextRoot(contextRoot);
				}
                
				if(errorUrl != null) {
					try {
						swa.setErrorUrl(errorUrl);
					} catch(VersionNotSupportedException ex) {
					}
				}

				if(httpservletSecurityProvider != null) {
					try {
						swa.setHttpservletSecurityProvider(httpservletSecurityProvider);
					} catch(VersionNotSupportedException ex) {
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
                            
                            if(Utils.notEmpty(getDynamicReloadInterval())) {
                                webClassLoader.setDynamicReloadInterval(getDynamicReloadInterval());
                            }
                            
                            WebProperty [] classLoaderProps = (WebProperty []) 
                                Utils.listToArray(getClassLoaderProperties(), WebProperty.class, version);
                            if(classLoaderProps != null) {
                                webClassLoader.setWebProperty(classLoaderProps);
                            }

                            swa.setMyClassLoader(webClassLoader);
                        } catch(VersionNotSupportedException ex) {
                            // Should not happen, but we have to catch it for now.
                        }
                    }
                }
                                
				try {
					if(Utils.notEmpty(defaultCharset) || Utils.notEmpty(formHintField)) {
						swa.setParameterEncoding(true);
						swa.setParameterEncodingDefaultCharset(defaultCharset);
						swa.setParameterEncodingFormHintField(formHintField);
					}

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
					swa.setJspConfig((JspConfig) jc.cloneVersion(version));
				}

				WebProperty [] webProps = (WebProperty []) 
					Utils.listToArray(getProperties(), WebProperty.class, version);
				if(webProps != null) {
					swa.setWebProperty(webProps);
				}
				
				MessageDestination [] msgDests = (MessageDestination []) 
					Utils.listToArray(getMessageDestinations(), MessageDestination.class, version);
				if(msgDests != null) {
					swa.setMessageDestination(msgDests);
				}
				
				if(localeInfo.sizeLocaleCharsetMap() > 0 || 
					Utils.notEmpty(localeInfo.getDefaultLocale()) ||
					Utils.notEmpty(localeInfo.getParameterEncodingDefaultCharset()) ||
					Utils.notEmpty(localeInfo.getParameterEncodingFormHintField())
					) {
					swa.setLocaleCharsetInfo((LocaleCharsetInfo) localeInfo.cloneVersion(version));
				}
				
				// SessionConfig snippet is retrieved below and added to the snippet list
				// for WebAppRoot.
				
				// Cache snippet is retrieved below and added to the snippet list
				// for WebAppRoot.
				
                /* IZ 84549 - add remaining saved role mappings here.  All entries that are represented
                 * by real DConfigBeans should have been removed by now. */
                if(savedRoleMappings != null && savedRoleMappings.size() > 0) {
                    for (Iterator iter = savedRoleMappings.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping mapping = 
                                (org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping) entry.getValue();
                        swa.addSecurityRoleMapping(
                                (org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping) mapping.cloneVersion(version));
                    }
                }                
                
				return swa;
			}
		};
		
		snippets.add(snipOne);
		snippets.addAll(sessionConfigBean.getSnippets());
		snippets.addAll(cacheBean.getSnippets());
		return snippets;
	}
   
    private class WebAppRootParser implements ConfigParser {
        public Object parse(java.io.InputStream stream) throws IOException, SAXException, DDException {
            DDProvider provider = DDProvider.getDefault();
            SunWebApp result = null;
            
            if(stream != null) {
                // Exceptions (due to bad graph or other problem) are handled by caller.
                result = provider.getWebDDRoot(new org.xml.sax.InputSource(stream));
            } else {
                // If we have a null stream, return a blank graph.
                result = (SunWebApp) provider.newGraph(SunWebApp.class,
                        getConfig().getAppServerVersion().getWebAppVersionAsString());
            }

            // First set our version to match that of this deployment descriptor.
            getConfig().internalSetAppServerVersion(ASDDVersion.getASDDVersionFromServletVersion(result.getVersion()));
            
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
	
    protected ConfigParser getParser() {
        return new WebAppRootParser();
    }

    boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		
		SunWebApp beanGraph = (SunWebApp) config.getBeans(uriText, constructFileName(),
			getParser(), new WebAppRootFinder());
		
		clearProperties();
		
		if(null != beanGraph) {
			contextRoot = beanGraph.getContextRoot();
            
			try {
				errorUrl = beanGraph.getErrorUrl();
			} catch(VersionNotSupportedException ex) {
				errorUrl = "";
			}
            
 			try {
				httpservletSecurityProvider = beanGraph.getHttpservletSecurityProvider();
			} catch(VersionNotSupportedException ex) {
				httpservletSecurityProvider = "";
			}
            
            try {
                // This block is separate from the rest because classloader exists for both 2.4.0 and 2.4.1
                MyClassLoader myClassLoader = beanGraph.getMyClassLoader();
                if(myClassLoader != null) {
                    delegate = Utils.booleanValueOf(myClassLoader.getDelegate()) ? Boolean.TRUE : Boolean.FALSE;
                    extraClassPath = myClassLoader.getExtraClassPath();
                    dynamicReloadInterval = myClassLoader.getDynamicReloadInterval();
        			classLoaderProperties = Utils.arrayToList(myClassLoader.getWebProperty());
                    if((delegate != null) || (extraClassPath != null) || (dynamicReloadInterval != null) || 
                            (classLoaderProperties != null && classLoaderProperties.size() > 0)) {
                        classLoader = Boolean.TRUE;
                    }
                }
            } catch(VersionNotSupportedException ex) {
                //Should never happen
                delegate = Boolean.TRUE;
                classLoader = Boolean.TRUE;
            }
                        
            try {
                defaultCharset = beanGraph.getParameterEncodingDefaultCharset();
                formHintField = beanGraph.getParameterEncodingFormHintField();

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
            
            // For IZ 84549 - save any security-role-mappings in graph.
            saveMappingsToCache(beanGraph.getSecurityRoleMapping());
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
		StorageBeanFactory beanFactory = getConfig().getStorageFactory();
        
		contextRoot = null;
		errorUrl = null;
        httpservletSecurityProvider = null;
		extraClassPath = null;
        dynamicReloadInterval = null;
        classLoaderProperties = null;
		defaultCharset = null;
		formHintField = null;
		idempotentUrlPattern = beanFactory.createSunWebApp();
		jspConfig = beanFactory.createJspConfig();
		properties = null;
		messageDestinations = null;
		localeInfo = beanFactory.createLocaleCharsetInfo();

		classLoader = Boolean.FALSE;
		delegate = Boolean.FALSE;
	}
	
	protected void setDefaultProperties() {
		StorageBeanFactory beanFactory = getConfig().getStorageFactory();
        
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
			
			J2EEBaseVersion moduleVersion = getJ2EEModuleVersion();
			if(moduleVersion.compareTo(ServletVersion.SERVLET_2_4) >= 0) {
//				webAppRootFactoryMap.put("message-destination", new DCBGenericFactory(MessageDestination.class)); // NOI18N
				webAppRootFactoryMap.put("service-ref", new DCBGenericFactory(ServiceRef.class));			// NOI18N

				if(moduleVersion.compareTo(ServletVersion.SERVLET_2_5) >= 0) {
					webAppRootFactoryMap.put("message-destination-ref", new DCBGenericFactory(MessageDestinationRef.class)); // NOI18N
				}
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
    
	/** Holds value of property httpservletSecurityProvider. */
	private String httpservletSecurityProvider;
    
	/** Holds value of property classLoader. */
	private Boolean classLoader;
	
	/** Holds value of property extraClassPath. */
	private String extraClassPath;
	
	/** Holds value of property delegate. */
	private Boolean delegate;

	/** Holds value of property dynamicReloadInterval. */
	private String dynamicReloadInterval;
	
	/** Holds list of WebProperty classLoaderProperties. */
	private List classLoaderProperties;
    
	/** Holds value of property parameterEncoding-defaultCharset. */
	private String defaultCharset;
        
	/** Holds value of property parameterEncoding-formHintField. */
	private String formHintField;
        
	/** Holds value of property idempotentUrlPattern */
	private SunWebApp idempotentUrlPattern;
        
	/** Holds value of property jspConfig. */
	private JspConfig jspConfig;
	
	/** Holds list of WebProperty properties. */
	private List properties;
	
	/** Holds list of MessageDestination properties. */
	private List messageDestinations;
	
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
        newContextRoot = Utils.encodeUrlField(newContextRoot);
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
        newErrorUrl = Utils.encodeUrlField(newErrorUrl);
        String oldErrorUrl = errorUrl;
        getVCS().fireVetoableChange("errorUrl", oldErrorUrl, newErrorUrl);
        errorUrl = newErrorUrl;
        getPCS().firePropertyChange("errorUrl", oldErrorUrl, errorUrl);
    }

    /** Getter for property httpservletSecurityProvider.
     * @return Value of property httpservletSecurityProvider.
     *
     */
    public String getHttpservletSecurityProvider() {
        return httpservletSecurityProvider;
    }

    /** Setter for property httpservletSecurityProvider.
     * @param newHttpservletSecurityProvider New value of property httpservletSecurityProvider.
     *
     * @throws PropertyVetoException
     *
     */
    public void setHttpservletSecurityProvider(String newHttpservletSecurityProvider) throws java.beans.PropertyVetoException {
        String oldHttpservletSecurityProvider = httpservletSecurityProvider;
        getVCS().fireVetoableChange("httpservletSecurityProvider", oldHttpservletSecurityProvider, newHttpservletSecurityProvider);
        httpservletSecurityProvider = newHttpservletSecurityProvider;
        getPCS().firePropertyChange("httpservletSecurityProvider", oldHttpservletSecurityProvider, httpservletSecurityProvider);
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
	
	/** Getter for property extraClassPath.
	 * @return Value of property extraClassPath.
	 *
	 */
	public String getExtraClassPath() {
		return extraClassPath;
	}
	
	/** Setter for property extraClassPath.
	 * @param newExtraClassPath New value of property extraClassPath.
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
	
	/** Getter for property delegate.
	 * @return Value of property delegate.
	 *
	 */
	public boolean isDelegate() {
		return delegate.booleanValue();
	}
	
	/** Setter for property delegate.
	 * @param newDelegate New value of property delegate.
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

	/** Getter for property dynamicReloadInterval.
	 * @return Value of property dynamicReloadInterval.
	 *
	 */
	public String getDynamicReloadInterval() {
		return dynamicReloadInterval;
	}
	
	/** Setter for property dynamicReloadInterval.
	 * @param newDynamicReloadInterval New value of property dynamicReloadInterval.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setDynamicReloadInterval(String newDynamicReloadInterval) throws java.beans.PropertyVetoException {
		String oldDynamicReloadInterval = dynamicReloadInterval;
		getVCS().fireVetoableChange("dynamicReloadInterval", oldDynamicReloadInterval, newDynamicReloadInterval);
		dynamicReloadInterval = newDynamicReloadInterval;
		getPCS().firePropertyChange("dynamicReloadInterval", oldDynamicReloadInterval, dynamicReloadInterval);
	}
	
	/** Getter for property classLoaderProperties.
	 * @return Value of property classLoaderProperties.
	 *
	 */
	public List getClassLoaderProperties() {
		return classLoaderProperties;
	}
	
	public WebProperty getClassLoaderProperty(int index) {
		return (WebProperty) classLoaderProperties.get(index);
	}
	
	/** Setter for property classLoaderProperties.
	 * @param newClassLoaderProperty New value of property classLoaderProperties.
	 *
	 * @throws PropertyVetoException
	 *
	 */
    public void setClassLoaderProperties(List newClassLoaderProperties) throws java.beans.PropertyVetoException {
        List oldClassLoaderProperties = classLoaderProperties;
        getVCS().fireVetoableChange("classLoaderProperties", oldClassLoaderProperties, newClassLoaderProperties);	// NOI18N
        classLoaderProperties = newClassLoaderProperties;
        getPCS().firePropertyChange("classLoaderProperties", oldClassLoaderProperties, classLoaderProperties);	// NOI18N
    }
    
	public void addClassLoaderProperty(WebProperty newClassLoaderProperty) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("classLoaderProperty", null, newClassLoaderProperty);	// NOI18N
		if(classLoaderProperties == null) {
			classLoaderProperties = new ArrayList();
		}
		classLoaderProperties.add(newClassLoaderProperty);
		getPCS().firePropertyChange("classLoaderProperty", null, newClassLoaderProperty );	// NOI18N
	}
	
	public void removeClassLoaderProperty(WebProperty oldClassLoaderProperty) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("classLoaderProperty", oldClassLoaderProperty, null);	// NOI18N
		classLoaderProperties.remove(oldClassLoaderProperty);
		getPCS().firePropertyChange("classLoaderProperty", oldClassLoaderProperty, null );	// NOI18N
	}

	/** Getter for property parameterEncodingDefaultCharset.
	 * @return Value of property parameterEncodingDefaultCharset.
	 *
	 */
	public String getDefaultCharset() {
		return defaultCharset;
	}
	
	/** Setter for property parameterEncodingDefaultCharset.
	 * @param newDefaultCharset New value of property parameterEncodingDefaultCharset.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setDefaultCharset(String newDefaultCharset) throws java.beans.PropertyVetoException {
		String oldDefaultCharset = defaultCharset;
		getVCS().fireVetoableChange("defaultCharset", oldDefaultCharset, newDefaultCharset);
		defaultCharset = newDefaultCharset;
		getPCS().firePropertyChange("defaultCharset", oldDefaultCharset, defaultCharset);
	}
	
	/** Getter for property parameterEncodingFormHintField.
	 * @return Value of property parameterEncodingFormHintField.
	 *
	 */
	public String getFormHintField() {
		return formHintField;
	}
	
	/** Setter for property parameterEncodingFormHintField.
	 * @param newFormHintField New value of property parameterEncodingFormHintField.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setFormHintField(String newFormHintField) throws java.beans.PropertyVetoException {
		String oldFormHintField = formHintField;
		getVCS().fireVetoableChange("formHintField", oldFormHintField, newFormHintField);
		formHintField = newFormHintField;
		getPCS().firePropertyChange("formHintField", oldFormHintField, formHintField);
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
