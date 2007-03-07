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

import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;

import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;

/** This DConfigBean is a child of WebServices.
 *
 * Property structures from sun-web-app.xml or sun-ejb-jar.xml handled by this bean
 *
 *		webservice-description : WebserviceDescription
 *			webservice-description-name : String
 *			wsdl-publish-location : String?
 *			webserviceEndpoint <webservice-endpoint> : WebserviceEndpoint[0,n]
 *				portComponentName <port-component-name> : String
 *				endpointAddressUri <endpoint-address-uri> : String[0,1]
 *				(
 *				  | loginConfig <login-config> : LoginConfig
 *				  | 	authMethod <auth-method> : String
 *          // *** EJB JAR version has realm field.
 *				  | 	realm <realm> : String[0,1]
 *				  |
 *				  | messageSecurityBinding <message-security-binding> : MessageSecurityBinding
 *				  | 	[attr: auth-layer ENUM #REQUIRED ( SOAP ) ]
 *				  | 	[attr: provider-id CDATA #IMPLIED ]
 *				  | 	messageSecurity <message-security> : MessageSecurity[0,n]
 *				  | 		message <message> : Message[1,n]
 *				  | 			| javaMethod <java-method> : JavaMethod[0,1]
 *				  | 			| 	methodName <method-name> : String
 *				  | 			| 	methodParams <method-params> : MethodParams[0,1]
 *				  | 			| 		methodParam <method-param> : String[0,n]
 *				  | 			| operationName <operation-name> : String[0,1]
 *				  | 		requestProtection <request-protection> : boolean[0,1]
 *				  | 			[attr: auth-source ENUM #IMPLIED ( sender content ) ]
 *				  | 			[attr: auth-recipient ENUM #IMPLIED ( before-content after-content ) ]
 *				  | 			EMPTY : String
 *				  | 		responseProtection <response-protection> : boolean[0,1]
 *				  | 			[attr: auth-source ENUM #IMPLIED ( sender content ) ]
 *				  | 			[attr: auth-recipient ENUM #IMPLIED ( before-content after-content ) ]
 *				  | 			EMPTY : String
 *				)[0,1]
 *				transportGuarantee <transport-guarantee> : String[0,1]
 *				serviceQname <service-qname> : ServiceQname[0,1]        [not used - set by server]
 *					namespaceURI <namespaceURI> : String                [not used - set by server]
 *					localpart <localpart> : String                      [not used - set by server]
 *				tieClass <tie-class> : String[0,1]                      [not used - set by server]
 *				servletImplClass <servlet-impl-class> : String[0,1]     [not used - set by server]
 *				debuggingEnabled <debugging-enabled> : String[0,1]
 *
 *          // *** WEB APPLICATION version of property field
 *				webProperty <property> : WebProperty[0,n]
 *					[attr: name CDATA #REQUIRED ]
 *					[attr: value CDATA #REQUIRED ]
 *					description <description> : String[0,1]
 *
 *          // *** EJB JAR version of property field
 *				propertyElement <property> : PropertyElement[0,n]
 *					name <name> : String
 *					value <value> : String
 *
 * @author  Peter Williams
 */
public class WebServiceDescriptor extends Base {

	/** property event names
	 */
	public static final String WEBSERVICE_DESCRIPTION_NAME = "webserviceDescriptionName"; // NOI18N
	public static final String WEBSERVICE_ENDPOINT = "webserviceEndpoint"; // NOI18N
	public static final String ENDPOINT_SECURITY_BINDING = "endpointSecurityBinding"; // NOI18N
	public static final String COMPONENT_LINK_NAME = "componentLinkName"; // NOI18N


	/** DDBean that refers to "webservice-description-name" child of bound DDBean. */
	private DDBean webServiceDescriptionNameDD;

	/** Holds value of property wsdlPublishLocation. */
	private String wsdlPublishLocation;

    /** Hold a map of DDBean [portComponent] -> web service endpoints. */
    private Map webServiceEndpointMap;

    /** Differentiates Servlet vs Ejb webservice support */
    private EndpointHelper helper;

    /** EJB modules for J2EE 1.4 and earlier require this field.  This boolean
      * controls whether we provide a default value and keep it up to date as needed. 
      */
    private boolean requiresDefaultEndpointUri;
    
    /** Creates a new instance of WebServiceDescriptor */
	public WebServiceDescriptor() {
		setDescriptorElement(bundle.getString("BDN_WebServiceDescriptor")); // NOI18N
	}

	/** Override init to enable grouping support for this bean and load name
	 *  field from related DDBean.
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);

// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);

        BaseRoot masterRoot = getConfig().getMasterDCBRoot();
        if(masterRoot instanceof WebAppRoot) {
            helper = servletHelper;
            requiresDefaultEndpointUri = false;
        } else if(masterRoot instanceof EjbJarRoot) {
            helper = ejbHelper;
            requiresDefaultEndpointUri = (getJ2EEModuleVersion().compareSpecification(J2EEVersion.JAVAEE_5_0) < 0);
        } else {
            throw new ConfigurationException("Unexpected master DConfigBean type: " + masterRoot); // NOI18N
        }
        
        dDBean.addXpathListener(dDBean.getXpath(), this);
		webServiceDescriptionNameDD = getNameDD("webservice-description-name"); // NOI18N

        updateNamedBeanCache(SunWebApp.WEBSERVICE_DESCRIPTION);

		loadFromPlanFile(getConfig());
	}

	protected String getComponentName() {
		return getWebServiceDescriptionName();
	}
    
	/** -----------------------------------------------------------------------
	 *  Validation implementation
	 */
	
	// relative xpaths (double as field id's)
	public static final String FIELD_WSDL_PUBLISH_LOCATION = "wsdl-publish-location";

	protected void updateValidationFieldList() {
		super.updateValidationFieldList();
		validationFieldList.add(FIELD_WSDL_PUBLISH_LOCATION);
	}
	
	public boolean validateField(String fieldId) {
		ValidationError error = null;
		
		// !PW use visitor pattern to get rid of switch/if statement for validation
		//     field -- data member mapping.
		//
		if(fieldId.equals(FIELD_WSDL_PUBLISH_LOCATION)) {
			String absoluteFieldXpath = getAbsoluteXpath(fieldId);
			
			if(Utils.notEmpty(wsdlPublishLocation)) {
                try {
                    URL testUrl = new URL(wsdlPublishLocation);
                } catch(MalformedURLException ex) {
                    Object [] args = new Object[1];
                    args[0] = wsdlPublishLocation; // NOI18N
                    String message = MessageFormat.format(bundle.getString("ERR_InvalidUrl"), args); // NOI18N
                    error = ValidationError.getValidationError(ValidationError.PARTITION_GLOBAL, absoluteFieldXpath, message);
                }
			}
			
			if(error == null) {
				error = ValidationError.getValidationErrorMask(ValidationError.PARTITION_GLOBAL, absoluteFieldXpath);
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
		return "AS_CFG_WebServiceDescriptor";
	}

	/** Getter for property webservice-description-name.
	 * @return Value of property webservice-description-name.
	 *
	 */
	public String getWebServiceDescriptionName() {
		return cleanDDBeanText(webServiceDescriptionNameDD);
	}
    
    /** For the customizer to determine the host type of the endpoint.
     */
    public boolean isWarModule() {
        // FIXME need a better implementation but this will do for now.
        return helper instanceof ServletHelper;
    }
    
    public boolean isEjbModule() {
        // FIXME need a better implementation but this will do for now.
        return helper instanceof EjbHelper;
    }

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    Collection getSnippets() {
        Collection snippets = new ArrayList();

        Snippet snipOne = new DefaultSnippet() {
            public CommonDDBean getDDSnippet() {
                // Add web service description entry.
                WebserviceDescription wsDesc = getConfig().getStorageFactory().createWebserviceDescription();
                wsDesc.setWebserviceDescriptionName(getWebServiceDescriptionName());
                wsDesc.setWsdlPublishLocation(getWsdlPublishLocation());
                return wsDesc;
            }

            public boolean hasDDSnippet() {
				if(wsdlPublishLocation != null && wsdlPublishLocation.length() > 0) {
					return true;
				}

				return false;
            }

            public String getPropertyName() {
                return SunWebApp.WEBSERVICE_DESCRIPTION;
            }

            public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
                CommonDDBean newBean = getDDSnippet();
                if(newBean != null) {
                    if(ddParent != null) {
                        helper.addWebServiceDescription(ddParent, newBean);
                    }
                }
                return newBean;
            }

        };

        Snippet snipTwo = new DefaultSnippet() {
            public CommonDDBean getDDSnippet() {
                // The contract for getDDSnippet() is to NEVER return null.  In this
                // instance however, a true snippet is not created.  Instead, the
                // implementation of mergeIntoRovingDD() inserts the various endpoint
                // fragments into the correct places directly.  In fact, it is quite
                // possible this method is never called.  The result certainly should
                // never be used.
                return null;
            }

            public boolean hasDDSnippet() {
				if(webServiceEndpointMap != null && webServiceEndpointMap.size() > 0) {
					return true;
				}

				return false;
            }

            public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
                // For each endpoint, locate the host (servlet or ejb) it is bound
                // to and add it to that host's endpoint table.
                RootInterface root = (RootInterface) ddParent;
                String version = root.getVersion().toString();
                Iterator iter = webServiceEndpointMap.entrySet().iterator();

                while(iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    DDBean portComponent = (DDBean) entry.getKey();
                    String linkName = getComponentLinkName(portComponent);
                    WebserviceEndpoint endpoint = (WebserviceEndpoint) entry.getValue();

                    // We only add the endpoint data if there is something to save.
                    if(hasContent(endpoint)) {
                        boolean endpointAdded = false;
                        CommonDDBean [] hosts = helper.getEndpointHosts(root);
                        for(int i = 0; i < hosts.length; i++) {
                            String hostName = (String) hosts[i].getValue(helper.getHostNameProperty());
                            if(hostName != null && hostName.equals(linkName)) {
                                hosts[i].addValue(helper.getEndpointProperty(), endpoint.cloneVersion(version));
                                endpointAdded = true;
                                break;
                            }
                        }

                        if(!endpointAdded) {
                            CommonDDBean newBean = helper.createNewHost();
                            newBean.setValue(helper.getHostNameProperty(), linkName);
                            newBean.addValue(helper.getEndpointProperty(), endpoint.cloneVersion(version));
                            helper.addEndpointHost(root, newBean);
                        }
                    }
                }

                return ddParent;
            }
            
            public String getPropertyName() {
                return helper.getEndpointProperty();
            }
            
            private boolean hasContent(WebserviceEndpoint endpoint) {
                if(Utils.notEmpty(endpoint.getEndpointAddressUri())) {
                    return true;
                }
                
                if(Utils.notEmpty(endpoint.getTransportGuarantee())) {
                    return true;
                }
                
                if(endpoint.getLoginConfig() != null) {
                    return true;
                }
                
                try {
                    if(Utils.notEmpty(endpoint.getDebuggingEnabled())) {
                        return true;
                    }
                } catch (VersionNotSupportedException ex) {
                    // ignore.
                }
                
                try {
                    if(endpoint.getMessageSecurityBinding() != null) {
                        return true;
                    }
                } catch(VersionNotSupportedException ex) {
                    // ignore.
                }
                
                return false;
            }            
        };

        snippets.add(snipOne);
        snippets.add(snipTwo);
        return snippets;
	}

	protected class WebServiceDescriptorFinder implements ConfigFinder {

		private String wsDescName;

		public WebServiceDescriptorFinder(String beanName) {
			this.wsDescName = beanName;
		}

		public Object find(Object obj) {
			Object result = null;
			CommonDDBean root = (CommonDDBean) obj;
            CommonDDBean [] descriptions = helper.getWebServiceDescriptions(root);

			for(int i = 0; i < descriptions.length; i++) {
                String name = (String) descriptions[i].getValue(WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME);
                if(wsDescName.equals(name)) {
					result = descriptions[i];
					break;
				}
			}

			return result;
		}
	}

	private class RootFinder implements ConfigFinder {
		public Object find(Object obj) {
			RootInterface result = null;

			if(obj instanceof SunWebApp || obj instanceof SunEjbJar) {
				result = (RootInterface) obj;
			}

			return result;
		}
	}
    
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

        RootInterface root = (RootInterface) config.getBeans(uriText,
            constructFileName(), getParser(), new RootFinder());

		WebserviceDescription descGraph = (WebserviceDescription) config.getBeans(uriText,
            constructFileName(), getParser(), new WebServiceDescriptorFinder(getWebServiceDescriptionName()));

        clearProperties();

        // Default endpoint URI's are set in this call.
        Map tmpEndpointMap = getEndpointMap(requiresDefaultEndpointUri);

		if(descGraph != null) {
            wsdlPublishLocation = descGraph.getWsdlPublishLocation();
        }

        if(root != null) {
            // Load all endpoints that already have defined values.
            CommonDDBean [] hosts = helper.getEndpointHosts(root);
            if(hosts != null) {
                for(int i = 0; i < hosts.length; i++) {
                    String hostName = (String) hosts[i].getValue(helper.getHostNameProperty());
                    WebserviceEndpoint [] definedEndpoints = (WebserviceEndpoint []) hosts[i].getValues(helper.getEndpointProperty());

                    for(int j = 0; j < definedEndpoints.length; j++) {
                        String portComponentName = definedEndpoints[j].getPortComponentName();
                        DDBean key = findEndpointInMap(hostName, portComponentName, tmpEndpointMap);
                        if(key != null) {
                            // This end point is still valid and has data that has been previously saved.
                            tmpEndpointMap.remove(key);
                            webServiceEndpointMap.put(key, definedEndpoints[j].clone());
                            
                            // Remove this endpoint from the saved endpoint list managed by
                            // the WebServices root DConfigBean.
                            ((WebServices) getParent()).removeEndpoint(hostName, portComponentName);
                            
                            // If the host data is still cached, remove this endpoint from the cache entry 
                            // for the host so that it is not saved twice if modified.  See IZ 94865
                            BaseRoot masterRoot = config.getMasterDCBRoot();
                            masterRoot.removeCachedEndpoint(helper.getHostProperty(), helper.getHostNameProperty(), 
                                    hostName, helper.getEndpointProperty(), portComponentName);
                        }
                    }
                }
            }
        }

        // This is section is the equivalent of setDefaultProperties() in other
        // DConfigBean implementations.
        // Default URI's for any new, or otherwise undefined endpoints were initially
        // set above in getEndpointMap() and are transferred to the storage map here.
        //
        if(tmpEndpointMap.size() > 0) {
            // handle defaults for the remaining tmp endpoints.
            Iterator iter = tmpEndpointMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                webServiceEndpointMap.put(entry.getKey(), entry.getValue());
            }
            
            // Mark master DConfigBean as dirty so we force a save of the new default
            // values, if any were generated.  We can't simply mark this bean dirty
            // because it is still being constructed and doesn't have any listeners 
            // attached yet.
            //
            if(requiresDefaultEndpointUri) {
                config.getMasterDCBRoot().setDirty();
            }
        }
        
        // Do we want to do anything special for default values, or simply fill in
        // the entries + port component name all the time?

		return (descGraph != null || root != null);
	}

    /** Finds the first endpoint in the map, if any, with the given port name
     *  and servlet-link or ejb-link and returns the key that will allow this
     *  entry to be retrieved.  If this proves to be a performance bottleneck,
     *  we may have to have an additional index into the map.
     */
    private DDBean findEndpointInMap(String linkName, String portComponentName, Map endpointMap) {
        DDBean key = null;

        if(Utils.notEmpty(linkName) && Utils.notEmpty(portComponentName)) {
            Iterator iter = endpointMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                DDBean portComponent = (DDBean) entry.getKey();

                if(portComponentName.equals(getPortComponentName(portComponent)) &&
                        linkName.equals(getComponentLinkName(portComponent))) {
                    key = portComponent;
                    break;
                }
            }
        }

        return key;
    }

    /** Returns a map between servlet/port pairs and endpoints, with the port
     *  name in each endpoint pre-filled.
     */
    private Map getEndpointMap(boolean requiresDefaultEndpointUri) {
        HashMap endpointMap = new HashMap();

        // The list of ports in this service
        DDBean [] portComponents = getDDBean().getChildBean("port-component"); // NOI18N
        for(int i = 0; i < portComponents.length; i++) {
            String portComponentName = getPortComponentName(portComponents[i]);
            WebserviceEndpoint endpoint = helper.createWebServiceEndpoint(getConfig().getStorageFactory());

            if(Utils.notEmpty(portComponentName)) {
                endpoint.setPortComponentName(portComponentName);
                // This where default endpoints get set, if required for this service & module type
                if(requiresDefaultEndpointUri) {
                    endpoint.setEndpointAddressUri(helper.getUriPrefix() + portComponentName);
                }
            }

            endpointMap.put(portComponents[i], endpoint);
        }

        return endpointMap;
    }

    private String getPortComponentName(DDBean portComponent) {
        return getChildBeanText(portComponent, "port-component-name");
    }

    private String getComponentLinkName(DDBean portComponent) {
        return getChildBeanText(portComponent, "service-impl-bean/" + helper.getLinkXpath());
    }

    protected String getChildBeanText(DDBean parent, String childXpath) {
        String childText = null;

		DDBean[] beans = parent.getChildBean(childXpath);
		DDBean childDD = null;
		if(beans.length >= 1) {
			// Found the DDBean we want.
			childDD = beans[0];
		}

        if(childDD != null) {
            childText = childDD.getText();
            if(childText != null) {
                childText = childText.trim();
            }
        }

		return childText;
    }

	protected void clearProperties() {
		wsdlPublishLocation = null;
		webServiceEndpointMap = new HashMap();
	}

    protected String constructFileName() {
        // Delegate to parent, which in turn delegates to master DCB root.
        return getParent().constructFileName();
    }

	/* ------------------------------------------------------------------------
	 * Event handling support
	 */

	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);
//        dumpNotification("notifyDDChange", xpathEvent);

		DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

		if(eventBean == webServiceDescriptionNameDD) {
			// name changed...
			getPCS().firePropertyChange(WEBSERVICE_DESCRIPTION_NAME, GenericOldValue, getWebServiceDescriptionName());
			getPCS().firePropertyChange(DISPLAY_NAME, GenericOldValue, getDisplayName());

            updateNamedBeanCache(SunWebApp.WEBSERVICE_DESCRIPTION);
		} else if(xpath.endsWith("port-component-name")) {
            // port-component-name changed
            DDBean [] parents = eventBean.getChildBean("..");
            if(parents != null && parents.length == 1) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) webServiceEndpointMap.get(parents[0]);
                if(endpoint != null) {
                    updateEndpoint(endpoint, eventBean);
                    getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                }
            }
        } else if(xpath.endsWith(helper.getLinkXpath())) {
            DDBean [] parents = eventBean.getChildBean("../..");
            if(parents != null && parents.length == 1) {
                if(webServiceEndpointMap.get(parents[0]) != null) {
                    getPCS().firePropertyChange(COMPONENT_LINK_NAME, GenericOldValue, parents[0]);
                }
            }
        }
	}

    private void updateEndpoint(WebserviceEndpoint endpoint, DDBean portComponentNameDD) {
        String oldPortComponentName = endpoint.getPortComponentName();
        String newPortComponentName = portComponentNameDD.getText();
        if(newPortComponentName != null) {
            newPortComponentName = newPortComponentName.trim();
        }

        endpoint.setPortComponentName(newPortComponentName);

        if(requiresDefaultEndpointUri) {
            String oldEndpointUri = endpoint.getEndpointAddressUri();
            if((oldPortComponentName != null && oldPortComponentName.equals(oldEndpointUri)) ||
                    (oldPortComponentName == null && oldEndpointUri == null)) {
                endpoint.setEndpointAddressUri(newPortComponentName);
            }
        }
    }

    /** A DDBean has been added or removed (or changed, but we handle change events
     *  in notifyDDChange()).
     */
    public void fireXpathEvent(XpathEvent xpathEvent) {
        super.fireXpathEvent(xpathEvent);
//        dumpNotification("fireXpathEvent", xpathEvent);

        DDBean eventBean = xpathEvent.getBean();
        String xpath = eventBean.getXpath();

        if(xpath.endsWith("port-component")) {
            if(xpathEvent.isAddEvent()) {
                try {
                    addWebServiceEndpoint(eventBean, true);
                } catch(java.beans.PropertyVetoException ex) {
                    // suppress for now.
                }
            } else if(xpathEvent.isRemoveEvent()) {
                try {
                    removeWebServiceEndpoint(eventBean);
                } catch(java.beans.PropertyVetoException ex) {
                    // suppress for now.
                }
            }
        } else if(xpath.endsWith("port-component-name")) {
            DDBean [] parents = eventBean.getChildBean("..");
            if(parents != null && parents.length == 1) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) webServiceEndpointMap.get(parents[0]);
                if(endpoint != null) {
                    if(xpathEvent.isAddEvent()) {
                        updateEndpoint(endpoint, eventBean);
                        getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                    } else if(xpathEvent.isRemoveEvent()) {
                        endpoint.setPortComponentName("");
                        endpoint.setEndpointAddressUri("");
                        getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, GenericOldValue, parents[0]);
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------------------
     * Property support
     */

    /** Getter for property wsdlPublishLocation.
     * @return Value of property wsdlPublishLocation.
     */
    public String getWsdlPublishLocation() {
        return this.wsdlPublishLocation;
    }

    /** Setter for property wsdlPublishLocation.
     * @param newWsdlPublishLocation New value of property wsdlPublishLocation.
     *
     * @throws PropertyVetoException
     */
    public void setWsdlPublishLocation(String newWsdlPublishLocation) throws java.beans.PropertyVetoException {
        String oldWsdlPublishLocation = this.wsdlPublishLocation;
        getVCS().fireVetoableChange("wsdlPublishLocation", oldWsdlPublishLocation, newWsdlPublishLocation);
        this.wsdlPublishLocation = newWsdlPublishLocation;
        getPCS().firePropertyChange("wsdlPublishLocation", oldWsdlPublishLocation, wsdlPublishLocation);
    }

    /** Getter for property webServiceEndpoint.
     * @return Value of property webServiceEndpoint.
     *
     */
    public List getWebServiceEndpoints() {
        List result = new ArrayList(webServiceEndpointMap.size());
        Iterator iter = webServiceEndpointMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.add(entry.getValue());
        }
        return result;
    }

    public WebserviceEndpoint getWebServiceEndpoint(int index) {
        List endpoints = getWebServiceEndpoints();
        return (WebserviceEndpoint) endpoints.get(index);
    }

    public WebserviceEndpoint getWebServiceEndpoint(String portName) {
        WebserviceEndpoint result = null;
        DDBean key = createKey(portName);
        if(key != null) {
            result = (WebserviceEndpoint) webServiceEndpointMap.get(key);
        }
        return result;
    }

    /** Setter for property webServiceEndpoint.
     * @param webServiceEndpoint New value of property webServiceEndpoint.
     *
     * @throws PropertyVetoException
     *
     */
    public void setWebServiceEndpoints(List newWebServiceEndpoints) throws java.beans.PropertyVetoException {
        Map oldWebServiceEndpointMap = webServiceEndpointMap;
        getVCS().fireVetoableChange("webServiceEndpointMap", oldWebServiceEndpointMap, newWebServiceEndpoints);	// NOI18N
        webServiceEndpointMap = new HashMap();
        if(newWebServiceEndpoints != null) {
            Iterator iter = newWebServiceEndpoints.iterator();
            while(iter.hasNext()) {
                WebserviceEndpoint endpoint = (WebserviceEndpoint) iter.next();
                if(endpoint != null) {
                    DDBean key = createKey(endpoint.getPortComponentName());
                    if(key != null) {
                        webServiceEndpointMap.put(key, endpoint);
                    }
                }
            }
        }
        getPCS().firePropertyChange("webServiceEndpointMap", oldWebServiceEndpointMap, webServiceEndpointMap);	// NOI18N
    }

    public void addWebServiceEndpoint(WebserviceEndpoint newWebServiceEndpoint) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, null, newWebServiceEndpoint);
        if(webServiceEndpointMap == null) {
            webServiceEndpointMap = new HashMap();
        }
        DDBean key = createKey(newWebServiceEndpoint.getPortComponentName());
        if(key != null) {
            webServiceEndpointMap.put(key, newWebServiceEndpoint);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, null, newWebServiceEndpoint );
        }
    }

    public void removeWebServiceEndpoint(WebserviceEndpoint oldWebServiceEndpoint) throws java.beans.PropertyVetoException {
        getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, oldWebServiceEndpoint, null);
        DDBean key = createKey(oldWebServiceEndpoint.getPortComponentName());
        if(key != null && webServiceEndpointMap.get(key) != null) {
            webServiceEndpointMap.remove(key);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, oldWebServiceEndpoint, null );
        }
    }

    public void addWebServiceEndpoint(DDBean portComponentDD, boolean load) throws java.beans.PropertyVetoException {
        WebserviceEndpoint newWebserviceEndpoint = helper.createWebServiceEndpoint(getConfig().getStorageFactory());
        String portComponentName = getChildBeanText(portComponentDD, "port-component-name"); // NOI81N
        newWebserviceEndpoint.setPortComponentName(portComponentName);
        
        // If specified, try to load any saved mappings this endpoint might have
        if(load) {
            Base parent = getParent();
            if(parent instanceof WebServices) {
                WebServices wsParent = (WebServices) parent;
                String linkName = getChildBeanText(portComponentDD, "service-impl-bean/" + helper.getLinkXpath());
                WebserviceEndpoint savedEndpoint = wsParent.removeEndpoint(linkName, portComponentName);
                
                // Clone this data now, before possibly trimming it from the tree, below.
                if(savedEndpoint != null) {
                    newWebserviceEndpoint = (WebserviceEndpoint) savedEndpoint.clone();
                } else {
                    // set load to false so that we create a default endpoint uri, if required.
                    load = false;
                }
                
                // If the host data is still cached, remove this endpoint from the cache entry 
                // for the host so that it is not saved twice if modified.  See IZ 94865
                BaseRoot masterRoot = getConfig().getMasterDCBRoot();
                masterRoot.removeCachedEndpoint(helper.getHostProperty(), helper.getHostNameProperty(), 
                        linkName, helper.getEndpointProperty(), portComponentName);
            }
        }
        
        // Only fire vetoable/property changes if we set a default value on this endpoint.
        if(!load && requiresDefaultEndpointUri) {
            newWebserviceEndpoint.setEndpointAddressUri(portComponentName);
            getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, null, newWebserviceEndpoint);
        }

        if(webServiceEndpointMap == null) {
            webServiceEndpointMap = new HashMap();
        }
        webServiceEndpointMap.put(portComponentDD, newWebserviceEndpoint);
        
        if(!load && requiresDefaultEndpointUri) {
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, null, newWebserviceEndpoint);
        }
    }

    public void removeWebServiceEndpoint(DDBean portComponentDD) throws java.beans.PropertyVetoException {
        WebserviceEndpoint oldWebserviceEndpoint = (WebserviceEndpoint) webServiceEndpointMap.get(portComponentDD);
        if(oldWebserviceEndpoint != null) {
            getVCS().fireVetoableChange(WEBSERVICE_ENDPOINT, oldWebserviceEndpoint, null);
            webServiceEndpointMap.remove(portComponentDD);
            getPCS().firePropertyChange(WEBSERVICE_ENDPOINT, oldWebserviceEndpoint, null );
        }
    }

    /** Setter for message security binding for specific endpoint.
     *
     * @param portName port to locate binding for.
     * @param binding instance of MessageSecurityBinding to set on this port
     */
    public void setMessageSecurityBinding(String portName, MessageSecurityBinding newBinding) throws java.beans.PropertyVetoException, VersionNotSupportedException {
        WebserviceEndpoint endpoint = getWebServiceEndpoint(portName);
        if(endpoint != null) {
            MessageSecurityBinding oldBinding = endpoint.getMessageSecurityBinding();
            getVCS().fireVetoableChange(ENDPOINT_SECURITY_BINDING, oldBinding, newBinding);
            endpoint.setMessageSecurityBinding((newBinding != null) ? (MessageSecurityBinding) newBinding.clone() : null);
            getPCS().firePropertyChange(ENDPOINT_SECURITY_BINDING, oldBinding, newBinding);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(bundle.getString("ERR_InvalidPortName"), new Object [] { portName }));
        }
    }

    /** This method is the backbone of an attempt to interface the new Map based storage
     *  for endpoints with the existing List based API, which the existing UI depends
     *  on, even though the List provides less information.  This algorithm is dependent
     *  on each port component name specified for a servlet being unique, regardless
     *  of which servlet or ejb implements it.  This will generally be true (in fact
     *  it may be required.)
     */
    private DDBean createKey(String targetPortComponentName) {
        DDBean key = null;
        DDBean [] portComponentBeans = getDDBean().getChildBean("port-component"); // NOI18N
        for(int i = 0; i < portComponentBeans.length; i++) {
            String portComponentName = getPortComponentName(portComponentBeans[i]);
            if(Utils.notEmpty(portComponentName) && portComponentName.equals(targetPortComponentName)) {
                key = portComponentBeans[i];
                break;
            }
        }

        return key;
    }


    private static final String WEB_URI_PREFIX = ""; // NOI18N
    private static final String EJB_URI_PREFIX = "webservice/"; // NOI18N

    private final EndpointHelper servletHelper = new ServletHelper();
    private final EndpointHelper ejbHelper = new EjbHelper();

    private abstract class EndpointHelper {

        private final String linkXpath;
        private final String hostProperty;
        private final String hostNameProperty;
        private final String endpointProperty;
        private final String blueprintsUriPrefix;

        public EndpointHelper(String xpath, String hp, String hnp, String epp, String uriPrefix) {
            linkXpath = xpath;
            hostProperty = hp;
            hostNameProperty = hnp;
            endpointProperty = epp;
            blueprintsUriPrefix = uriPrefix;
        }

        public String getLinkXpath() {
            return linkXpath;
        }

        private String getHostProperty() {
            return hostProperty;
        }

        public String getHostNameProperty() {
            return hostNameProperty;
        }

        public String getEndpointProperty() {
            return endpointProperty;
        }

        public String getUriPrefix() {
            return blueprintsUriPrefix;
        }

        public abstract CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent);

        public abstract void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean);

        public abstract WebserviceEndpoint createWebServiceEndpoint(StorageBeanFactory factory);

        public abstract CommonDDBean [] getEndpointHosts(RootInterface root);

        public abstract void addEndpointHost(RootInterface root, CommonDDBean bean);

        public abstract CommonDDBean createNewHost();
    }

    private class ServletHelper extends EndpointHelper {
        public ServletHelper() {
            super("servlet-link", SunWebApp.SERVLET, Servlet.SERVLET_NAME, Servlet.WEBSERVICE_ENDPOINT, WEB_URI_PREFIX);
        }

        public CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent) {
            CommonDDBean [] result = (CommonDDBean []) ddParent.getValues(SunWebApp.WEBSERVICE_DESCRIPTION);
            return result;
        }

        public void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean) {
            ddParent.addValue(SunWebApp.WEBSERVICE_DESCRIPTION, wsDescBean);
        }

        public WebserviceEndpoint createWebServiceEndpoint(StorageBeanFactory factory) {
            return factory.createWebHostedWebserviceEndpoint();
        }

        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = (CommonDDBean []) root.getValues(SunWebApp.SERVLET);
            return result;
        }

        public void addEndpointHost(RootInterface root, CommonDDBean bean) {
            root.addValue(SunWebApp.SERVLET, bean);
        }

        public CommonDDBean createNewHost() {
            return getConfig().getStorageFactory().createServlet();
        }
    }

    private class EjbHelper extends EndpointHelper {
        public EjbHelper() {
            super("ejb-link", EnterpriseBeans.EJB, Ejb.EJB_NAME, Ejb.WEBSERVICE_ENDPOINT, EJB_URI_PREFIX);
        }

        public CommonDDBean [] getWebServiceDescriptions(CommonDDBean ddParent) {
            CommonDDBean [] result = null;
            CommonDDBean enterpriseBeans = (CommonDDBean) ddParent.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans != null) {
                result = (CommonDDBean []) enterpriseBeans.getValues(EnterpriseBeans.WEBSERVICE_DESCRIPTION);
            }
            return result;
        }

        public void addWebServiceDescription(CommonDDBean ddParent, CommonDDBean wsDescBean) {
            CommonDDBean enterpriseBeans = getEnterpriseBeans(ddParent);
            enterpriseBeans.addValue(EnterpriseBeans.WEBSERVICE_DESCRIPTION, wsDescBean);
        }
        
        public WebserviceEndpoint createWebServiceEndpoint(StorageBeanFactory factory) {
            return factory.createEjbHostedWebserviceEndpoint();
        }

        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = null;
            CommonDDBean enterpriseBeans = (CommonDDBean) root.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans != null) {
                result = (CommonDDBean []) enterpriseBeans.getValues(EnterpriseBeans.EJB);
            }
            return result;
        }

        public void addEndpointHost(RootInterface root, CommonDDBean bean) {
            CommonDDBean enterpriseBeans = getEnterpriseBeans(root);
            enterpriseBeans.addValue(EnterpriseBeans.EJB, bean);
        }

        public CommonDDBean createNewHost() {
            return getConfig().getStorageFactory().createEjb();
        }

        private CommonDDBean getEnterpriseBeans(CommonDDBean ddParent) {
            assert ddParent instanceof SunEjbJar;
            CommonDDBean enterpriseBeans = (CommonDDBean) ddParent.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans == null) {
                enterpriseBeans = getConfig().getStorageFactory().createEnterpriseBeans();
                ddParent.addValue(SunEjbJar.ENTERPRISE_BEANS, enterpriseBeans);
            }
            return enterpriseBeans;
        }
    }
}
