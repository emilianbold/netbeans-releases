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
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.J2eeApplicationObject;
import javax.enterprise.deploy.model.DeployableObject;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.app.Web;


/** This bean represents the content of the sun-application deployment 
 *  descriptor.
 *
 *  It can be a parent to beans that describe the content of the sun-ejb-jar,
 *  sun-web-app, sun-connector, and sun-application-client deployment 
 *  descriptors.
 *
 *  The bean that represents the content of the sun-web-app has the "alternate"
 *  context-root data that is optional in this descriptor.
 *
 *	sun-application : SunApplication
 *		web : Web[0,n]
 *			web-uri : String
 *			context-root : String
 *		pass-by-reference : String?
 *		realm : String?
 *
 *  @author vkraemer
 */
public class AppRoot extends BaseRoot {

	/** Holds value of property passByReference. */
	private String passByReference;
	
	/** Holds value of property realm. */
	private String realm;
	
	/** Holds list of web properties. */
	private List webModules;
	
	/** Creates a new instance of AppRoot
	 *  @param dDBean The root of the application.xml file
	 *  @param parent The bean's DeploymentConfiguration parent
	 */
	public AppRoot() {
		setDescriptorElement(bundle.getString("BDN_AppRoot"));	// NOI18N	
	}
	
	protected void init(DDBeanRoot dDBean, SunONEDeploymentConfiguration parent, DDBean ddbExtra) throws ConfigurationException {
		super.init(dDBean, parent, ddbExtra);
		
//		initWebModuleList(dDBean.getChildBean("module/web"));
		
		loadFromPlanFile(parent);
	}
	
/*
	private void initWebModuleList(DDBean [] webDDBeans) {
	if(webDDBeans != null) {
			webModules = new ArrayList(webDDBeans.length);
			Web webEntry;
			
			for(int i = 0; i < webDDBeans.length; i++) {
				webEntry = new Web();
				webEntry.setWebUri(webDDBeans[i].getText("web-uri")[0]);
				webEntry.setContextRoot(webDDBeans[i].getText("context-root")[0]);
			}
		}
	}
*/
	
	public String getUriText() {
		// FIXME !PW What can we put here?  Name of EAR file?
		return "EAR"; // NOI18N
	}		
	
	/** Get the application root version of this module.
	 *
	 * @return ApplicationVersion enum for the version of this module.
	 */
	 public J2EEBaseVersion getJ2EEModuleVersion() {
		DDBeanRoot ddbRoot = (DDBeanRoot) getDDBean();
		
		// From JSR-88 1.1
		String versionString = ddbRoot.getDDBeanRootVersion();
		if(versionString == null) {
			// If the above doesn't get us what we want.
			versionString = ddbRoot.getModuleDTDVersion();
		}
		
		J2EEBaseVersion applicationVersion = ApplicationVersion.getApplicationVersion(versionString);
		if(applicationVersion == null) {
			// Default to Application 1.4 if we can't find out what version this is.
			applicationVersion = ApplicationVersion.APPLICATION_1_4;
		}
		
		return applicationVersion;
	}
	 
	/* ------------------------------------------------------------------------
	 * Property getters/setters.
	 */
	
	/** Getter for property passByReference.
	 * @return Value of property passByReference.
	 */
	public String getPassByReference() {
		return passByReference;
	}
	
	/** Setter for property passByReference.
	 * @param newPassByReference New value of property passByReference.
	 * @throws PropertyVetoException if the property change is vetoed
	 */
	public void setPassByReference(String newPassByReference) throws java.beans.PropertyVetoException {
		String oldPassByReference = passByReference;
		getVCS().fireVetoableChange("passByReference", oldPassByReference, newPassByReference);
		passByReference = newPassByReference;
		getPCS().firePropertyChange("passByReference", oldPassByReference, passByReference);
	}
	
	/** Getter for property realm.
	 * @return Value of property realm.
	 */
	public String getRealm() {
		return realm;
	}
	
	/** Setter for property realm.
	 * @param newRealm New value of property realm.
	 * @throws PropertyVetoException if the property change is vetoed
	 */
	public void setRealm(String newRealm) throws java.beans.PropertyVetoException {
		String oldRealm = realm;
		getVCS().fireVetoableChange("realm", oldRealm, newRealm);
		realm = newRealm;
		getPCS().firePropertyChange("realm", oldRealm, realm);
	}
	
	
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			
			public CommonDDBean getDDSnippet() {
				SunApplication sa = (SunApplication) DDProvider.getDefault().newGraph(SunApplication.class);
				
				if(passByReference != null && passByReference.length() > 0) {
					sa.setPassByReference(passByReference);
				}
				
				if(realm != null && realm.length() > 0) {
					sa.setRealm(realm);
				}
				
				return sa;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
	public class AppRootParser implements ConfigParser {
		public Object parse(java.io.InputStream stream) {
            DDProvider provider = DDProvider.getDefault();
			SunApplication result = null;
            
            if(null != stream) {
                try {
                    result = provider.getAppDDRoot(new org.xml.sax.InputSource(stream));
                } catch (Exception ex) {
                    jsr88Logger.severe("invalid stream for SunWebApp"); // FIXME
                }
            }
            
            // If we have a null stream or there is a problem reading the graph,
            // return a blank graph.
            if(result == null) {
                result = (SunApplication) provider.newGraph(SunApplication.class);
            }
            
            return result;
            
		}
	}
	
	public class AppRootFinder implements ConfigFinder {
		public Object find(Object obj) {
			Object result = null;
			if(obj instanceof SunApplication) {
				result = (SunApplication) obj;
			}
			return result;
		}
	}
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		SunApplication beanGraph = (SunApplication) config.getBeans(uriText, constructFileName(),
			(ConfigParser) new AppRootParser(), new AppRootFinder());
		
		clearProperties();
		
		if(beanGraph != null) {
			passByReference = beanGraph.getPassByReference();
			realm = beanGraph.getRealm();
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}


        public String getHelpId() {
            return "AS_CFG_Application";                                //NOI18N
        }

    
	protected void clearProperties() {
		passByReference = null;
		realm = null;
	}

	protected void setDefaultProperties() {
	}
	
	/* ------------------------------------------------------------------------
	 * XPath to Factory mapping support
	 */
	private HashMap appRootFactoryMap;
	
	/** Retrieve the XPathToFactory map for this DConfigBean.  For AppRoot,
	 *  this maps application xpaths to factories for other contained root
	 *  objects plus a SecurityRoleModel factory
	 * @return
	 */
	protected Map getXPathToFactoryMap() {
		if(appRootFactoryMap == null) {
			appRootFactoryMap = new HashMap(17);
			
			appRootFactoryMap.put("module/ejb", new DCBGenericFactory(EjbJarRef.class));				// NOI18N
			appRootFactoryMap.put("module/web", new DCBGenericFactory(WebAppRef.class));				// NOI18N
//			appRootFactoryMap.put("module/connector", new DCBGenericFactory(ConnectorRef.class));		// NOI18N
//			appRootFactoryMap.put("module/java", new DCBGenericFactory(AppClientRef.class));			// NOI18N
			appRootFactoryMap.put("security-role", new DCBGenericFactory(SecurityRoleMapping.class));	// NOI18N
		}
		
		return appRootFactoryMap;
	}
}
