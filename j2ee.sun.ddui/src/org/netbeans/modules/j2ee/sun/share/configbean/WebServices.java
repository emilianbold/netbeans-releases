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
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
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

	/** Creates a new instance of WebServices */
	public WebServices() {
		setDescriptorElement(bundle.getString("BDN_WebServices"));	// NOI18N
	}

	protected void init(DDBeanRoot dDBeanRoot, SunONEDeploymentConfiguration parent, DDBean ddbExtra) throws ConfigurationException {
		super.init(dDBeanRoot, parent, ddbExtra);
//		dDBeanRoot.addXpathListener(dDBeanRoot.getXpath(), this);
		
		loadFromPlanFile(parent);		
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_WebServices";
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
    
//	/** Calculate what the parent S2B bean should be for this child and return
//	 *  that bean.
//	 */
//	protected CommonDDBean processParentBean(CommonDDBean bean, DConfigBean child) {
//		// If these services are in an ejb-jar, then we need to move the pointer
//        // to enterprise beans.
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
        return getConfig().getMasterDCBRoot().getParser();
    }
    
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		RootInterface beanGraph = (RootInterface) config.getBeans(uriText, 
			constructFileName(), getParser(), new RootFinder());
		
		clearProperties();
		
		if(beanGraph != null) {
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
	}
	
	protected void setDefaultProperties() {
	}

    protected String constructFileName() {
        // Delegate to master DConfigBean which could be either WebAppRoot or EjbJarRoot.
        return getConfig().getMasterDCBRoot().constructFileName();
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
    
    // No properties for this bean.
}
