/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;

import org.xml.sax.SAXException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


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
//	private List webModules;
	
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
	 
    /** Generate a DOCTYPE string for the specified version (which may be different
     *  than the current version of the tree
     */
    public String generateDocType(ASDDVersion version) {
        return generateDocType("sun-application", version.getSunApplicationPublicId(), version.getSunApplicationSystemId()); // NOI18N
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
				SunApplication sa = getConfig().getStorageFactory().createSunApplication();
                String version = sa.getVersion().toString();
				
				if(passByReference != null && passByReference.length() > 0) {
					sa.setPassByReference(passByReference);
				}
				
				if(realm != null && realm.length() > 0) {
					sa.setRealm(realm);
				}
                
                /* IZ 78686, etc - add remaining saved named beans here.  All entries that are represented
                 * by real DConfigBeans should have been removed by now. */
                restoreAllNamedBeans(sa, version);
				
				return sa;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
    public class AppRootParser implements ConfigParser {
        public Object parse(java.io.InputStream stream) throws IOException, SAXException, DDException {
            DDProvider provider = DDProvider.getDefault();
            SunApplication result = null;
            
            if(stream != null) {
                // Exceptions (due to bad graph or other problem) are handled by caller.
                result = provider.getAppDDRoot(new org.xml.sax.InputSource(stream));
            } else {
                // If we have a null stream, return a blank graph.
                result = (SunApplication) provider.newGraph(SunApplication.class, 
                        getConfig().getAppServerVersion().getApplicationVersionAsString());
            }

            // First set our version to match that of this deployment descriptor.
            getConfig().internalSetAppServerVersion(ASDDVersion.getASDDVersionFromAppVersion(result.getVersion()));
            
            return result;
        }
    }
	
	public static class AppRootFinder implements ConfigFinder {
		public Object find(Object obj) {
			Object result = null;
			if(obj instanceof SunApplication) {
				result = (SunApplication) obj;
			}
			return result;
		}
	}
	
    protected ConfigParser getParser() {
        return new AppRootParser();
    }
    
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();
		SunApplication beanGraph = (SunApplication) config.getBeans(uriText, constructFileName(),
			getParser(), new AppRootFinder());
		
		clearProperties();
		
		if(beanGraph != null) {
			passByReference = beanGraph.getPassByReference();
			realm = beanGraph.getRealm();
            
            // IZ 78686, etc - cache the data for all named beans.
            saveAllNamedBeans(beanGraph);
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}

    private static Collection appRootBeanSpecs = new ArrayList();
    
    static {
        appRootBeanSpecs.add(new NamedBean(SunWebApp.SECURITY_ROLE_MAPPING, 
                org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping.ROLE_NAME));
    }
    
    protected Collection getNamedBeanSpecs() {
        return appRootBeanSpecs;
    }

	public String getHelpId() {
		return "AS_CFG_Application"; //NOI18N
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
			appRootFactoryMap.put("module/java", new DCBGenericFactory(AppClientRef.class));			// NOI18N
			appRootFactoryMap.put("security-role", new DCBGenericFactory(SecurityRoleMapping.class));	// NOI18N
		}
		
		return appRootFactoryMap;
	}
}
