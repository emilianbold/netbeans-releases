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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;

/** Property structure of WebServiceEndpoint, from DTD (sun-web.xml or sun-ejb-jar.xml):
 *
 *		webservice-endpoint : WebserviceEndpoint[0,n]
 *			port-component-name : String
 *			endpoint-address-uri : String?
 *			login-config : LoginConfig?
 *				auth-method : String
 *			transport-guarantee : String?
 *			service-qname : ServiceQname?	[not used - set by server]
 *				namespaceURI : String		[not used - set by server]
 *				localpart : String			[not used - set by server]
 *			tie-class : String?				[not used - set by server]
 *			servlet-impl-class : String?	[not used - set by server]
 *
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class WebServices extends BaseRoot {
    
    /** Differentiates Servlet vs Ejb webservice support */
    private EndpointHelper helper;

    /** Hold a map, indexed by port component name, of endpoints that we found
     *  in the descriptor file, but haven't been loaded yet.  Note we find these
     *  under the host (servlet or ejb) so we don't know which web service they
     *  belong to.
     */
    private Map savedEndpoints;
    
    
    /** Creates a new instance of WebServices */
    public WebServices() {
        setDescriptorElement(bundle.getString("BDN_WebServices"));	// NOI18N
    }
    
    protected void init(DDBeanRoot dDBeanRoot, SunONEDeploymentConfiguration parent, DDBean ddbExtra) throws ConfigurationException {
        super.init(dDBeanRoot, parent, ddbExtra);
//		dDBeanRoot.addXpathListener(dDBeanRoot.getXpath(), this);
        
        BaseRoot masterRoot = parent.getMasterDCBRoot();
        if(masterRoot instanceof WebAppRoot) {
            helper = servletHelper;
        } else if(masterRoot instanceof EjbJarRoot) {
            helper = ejbHelper;
        } else {
            throw new ConfigurationException("Unexpected master DConfigBean type: " + masterRoot); // NOI18N
        }        
        
        loadFromPlanFile(parent);
    }
    
    public String generateDocType(ASDDVersion version) {
        return getMasterRootBean().generateDocType(version);
    }
    
    
    /** Getter for helpId property
     * @return Help context ID for this DConfigBean
     */
    public String getHelpId() {
        return "AS_CFG_WebServices";
    }
    
    public J2EEBaseVersion getJ2EEModuleVersion() {
        return getMasterRootBean().getJ2EEModuleVersion();
    }
    
    /** The DDBean (or one of it's children) that this DConfigBean is bound to
     *  has changed.
     *
     * @param xpathEvent
     */
//	public void notifyDDChange(XpathEvent xpathEvent) {
//		super.notifyDDChange(xpathEvent);
//        dumpNotification("notifyDDChange", xpathEvent);
//	}
//
//	public void fireXpathEvent(XpathEvent xpathEvent) {
//        super.fireXpathEvent(xpathEvent);
//        dumpNotification("fireXpathEvent", xpathEvent);
//	}
    
    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    Collection getSnippets() {
        Collection snippets = new ArrayList();
        Snippet snipOne = new DefaultSnippet() {
            public CommonDDBean getDDSnippet() {
//                // FIXME must return proper root (SunWebApp or SunEjbJar)
//				DDProvider provider = DDProvider.getDefault();
//				SunWebApp swa = (SunWebApp) provider.newGraph(SunWebApp.class);
//                return swa;
                return null;
            }
            
            public CommonDDBean mergeIntoRovingDD(CommonDDBean ddParent) {
                throw new UnsupportedOperationException();
            }
            
            public CommonDDBean mergeIntoRootDD(CommonDDBean ddRoot) {
//                if(ddRoot instanceof SunWebApp) {
//                    CommonDDBean snippet = getDDSnippet();
//                    ddRoot.merge(snippet, CommonDDBean.MERGE_UNION);
//                }
                // This bean has no data, thus nothing to save.  However, it does
                // need to be returned here, as this is the SunWebApp/SunEjbJar
                // root that will used by the children to save their data.
                return ddRoot;
            }
        };
        
        snippets.add(snipOne);
        return snippets;
    }
    
    public WebServiceDescriptor getWebServiceDescriptor(String wsName) {
        WebServiceDescriptor result = null;
        Iterator wsIter = getChildren().iterator();
        while(wsIter.hasNext()) {
            WebServiceDescriptor wsBean = (WebServiceDescriptor) wsIter.next();
            if(wsBean.getWebServiceDescriptionName().equals(wsName)) {
                result = wsBean;
                break;
            }
        }
        return result;
    }
    
//	/** Calculate what the parent S2B bean should be for this child and return
//	 *  that bean.
//	 */
//	protected CommonDDBean processParentBean(CommonDDBean bean, DConfigBean child) {
//		// If these services are in an ejb-jar, then we need to move the pointer
//		// to enterprise beans.
//		if(bean instanceof SunEjbJar) {
//			return ((SunEjbJar) bean).getEnterpriseBeans();
//		}
//
//		// All other children require no translation.
//		return bean;
//	}
    
    private class RootFinder implements ConfigFinder {
        public Object find(Object obj) {
            RootInterface result = null;
            
            if(obj instanceof RootInterface) {
                result = (RootInterface) obj;
            }
            
            return result;
        }
    }
    
    protected ConfigParser getParser() {
        return getMasterRootBean().getParser();
    }
    
    boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
        String uriText = getUriText();
        
        RootInterface beanGraph = (RootInterface) config.getBeans(uriText,
                constructFileName(), getParser(), new RootFinder());
        
        clearProperties();
        
        if(beanGraph != null) {
            // Load all endpoints that already have defined values and save them.
            // This facilitates lazy loading of endpoint data when the port-component
            // DDBeans are not available at the time the web-service-description
            // DDBean is created.
            CommonDDBean [] hosts = helper.getEndpointHosts(beanGraph);
            if(hosts != null) {
                for(int i = 0; i < hosts.length; i++) {
                    WebserviceEndpoint [] definedEndpoints = 
                            (WebserviceEndpoint []) hosts[i].getValues(helper.getEndpointProperty());
                    if(definedEndpoints != null && definedEndpoints.length > 0) {
                        String hostName = (String) hosts[i].getValue(helper.getHostNameProperty());
                        for(int j = 0; j < definedEndpoints.length; j++) {
                            saveEndpoint(hostName, definedEndpoints[j]);
                        }
                    }
                }
            }
        } else {
            setDefaultProperties();
        }
        
        return (beanGraph != null);
    }
    
    protected void clearProperties() {
        savedEndpoints = new HashMap();
    }
    
    protected void setDefaultProperties() {
    }
    
    protected String constructFileName() {
        // Delegate to master DConfigBean which could be either WebAppRoot or EjbJarRoot.
        return getMasterRootBean().constructFileName();
    }
    
    private BaseRoot getMasterRootBean() {
        return getConfig().getMasterDCBRoot();
    }
    
    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
    private HashMap webServicesRootFactoryMap;
    
    /** Retrieve the XPathToFactory map for this DConfigBean.  For AppRoot,
     *  this maps application xpaths to factories for other contained root
     *  objects plus a SecurityRoleModel factory
     * @return
     */
    protected java.util.Map getXPathToFactoryMap() {
        if(webServicesRootFactoryMap == null) {
            webServicesRootFactoryMap = new HashMap(3);
            webServicesRootFactoryMap.put("webservice-description", new DCBGenericFactory(WebServiceDescriptor.class)); // NOI18N
        }
        
        return webServicesRootFactoryMap;
    }
    
    /* ------------------------------------------------------------------------
     * Property getter/setter support
     */
    
    /** Saves the data in this particular endpoint in a cache in case the related
     *  port-component DDBean is created after the associated WebServiceDescriptor
     *  has already been constructed and loaded.
     *
     *  Added to support new event model used by merged annotation-dd provider which
     *  creates and sends events for empty webservice-description entries and then
     *  creates the individual port-components, after we've already loaded the bean
     *  data.  Saving the endpoint data for unrecognized ports here allows us to
     *  restore their data later, when the port-component events are received.
     *
     *  Presumes port-component names are unique per module.  Might have to save
     *  the servlet or ejb binding along with this.
     */
    public void saveEndpoint(final String hostName, final WebserviceEndpoint endpoint) {
        String key = hostName + endpoint.getPortComponentName();
        if(Utils.notEmpty(key) && savedEndpoints.get(key) == null) {
            savedEndpoints.put(key, endpoint);
        }
    }
    
    public WebserviceEndpoint removeEndpoint(final String linkName, final String portName) {
        WebserviceEndpoint result = null;
        String key = linkName + portName;
        if(savedEndpoints != null && savedEndpoints.size() > 0 && Utils.notEmpty(key)) {
            result = (WebserviceEndpoint) savedEndpoints.remove(key);
        }
        return result;
    }
    
    
    /** !PW FIXME In favor of least entropy during high resistence mode, partial
     *  versions of the helper classes have been copied here from WebServiceDescriptor.
     *
     *  These actually should be moved here in full and instances of WebServiceDescriptor
     *  should use their parent's helper instance.  That change can wait until 6.0
     */
    private final EndpointHelper servletHelper = new ServletHelper();
    private final EndpointHelper ejbHelper = new EjbHelper();
    
    private abstract class EndpointHelper {
        
        private final String hostNameProperty;
        private final String endpointProperty;
        
        public EndpointHelper(String hnp, String epp) {
            hostNameProperty = hnp;
            endpointProperty = epp;
        }
        
        public String getHostNameProperty() {
            return hostNameProperty;
        }
        
        public String getEndpointProperty() {
            return endpointProperty;
        }
        
        public abstract CommonDDBean [] getEndpointHosts(RootInterface root);
        
    }
    
    private class ServletHelper extends EndpointHelper {
        public ServletHelper() {
            super(Servlet.SERVLET_NAME, Servlet.WEBSERVICE_ENDPOINT);
        }
        
        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = (CommonDDBean []) root.getValues(SunWebApp.SERVLET);
            return result;
        }
    }
    
    private class EjbHelper extends EndpointHelper {
        public EjbHelper() {
            super(Ejb.EJB_NAME, Ejb.WEBSERVICE_ENDPOINT);
        }
        
        public CommonDDBean [] getEndpointHosts(RootInterface root) {
            CommonDDBean [] result = null;
            CommonDDBean enterpriseBeans = (CommonDDBean) root.getValue(SunEjbJar.ENTERPRISE_BEANS);
            if(enterpriseBeans != null) {
                result = (CommonDDBean []) enterpriseBeans.getValues(EnterpriseBeans.EJB);
            }
            return result;
        }
    }
}
