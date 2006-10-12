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
import java.util.List;
import java.util.Map;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.DConfigBean;
import org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess;

import org.xml.sax.SAXException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;

import org.netbeans.modules.schema2beans.BaseBean;


/** Property structure of AppClientRoot from DTD (sections that are handled
 *    by child DConfigBeans have been removed.):
 *
 *  sun-application-client : SunApplicationClient
 *		javaWebStartAccess <java-web-start-access> : JavaWebStartAccess[0,1]
 *			contextRoot <context-root> : String[0,1]
 *			eligible <eligible> : String[0,1]
 *			vendor <vendor> : String[0,1]
 *		messageDestination <message-destination> : MessageDestination[0,n]
 *			messageDestinationName <message-destination-name> : String
 *			jndiName <jndi-name> : String
 *
 * @author  Peter Williams
 */
public class AppClientRoot extends BaseRoot { 

    /** Holds list of MessageDestination properties. */
    private List messageDestinations;
	
    /** Holds value of property contextRoot. */
    private String contextRoot;

    /** Holds value of property eligible. */
    private Boolean eligible;

    /** Holds value of property vendor. */
    private String vendor;
    
    /** Creates a new instance of AppClientRoot */
    public AppClientRoot() {
        setDescriptorElement(bundle.getString("BDN_AppClientRoot"));	// NOI18N
    }

    protected void init(DDBeanRoot dDBean, SunONEDeploymentConfiguration parent, DDBean ddbExtra) throws ConfigurationException {
        super.init(dDBean, parent, ddbExtra);
        loadFromPlanFile(parent);
    }

    /** Getter for helpId property
     * @return Help context ID for this DConfigBean
     */
    public String getHelpId() {
        return "AS_CFG_AppClient";
    }

    /** Get the appclient version of this module.
     *
     * @return AppClientVersion enum for the version of this module.
     */
     public J2EEBaseVersion getJ2EEModuleVersion() {
        DDBeanRoot ddbRoot = (DDBeanRoot) getDDBean();

        // From JSR-88 1.1
        String versionString = ddbRoot.getDDBeanRootVersion();
        if(versionString == null) {
            // If the above doesn't get us what we want.
            versionString = ddbRoot.getModuleDTDVersion();
        }

        J2EEBaseVersion appClientVersion = AppClientVersion.getAppClientVersion(versionString);
        if(appClientVersion == null) {
            // Default to AppClient 1.4 if we can't find out what version this is.
            appClientVersion = AppClientVersion.APP_CLIENT_1_4;
        }

        return appClientVersion;
    }	

    /** Generate a DOCTYPE string for the specified version (which may be different
     *  than the current version of the tree
     */
    public String generateDocType(ASDDVersion version) {
        return generateDocType("sun-application-client", version.getSunAppClientPublicId(), version.getSunAppClientSystemId()); // NOI18N
    }
     
    /* ------------------------------------------------------------------------
     * Property getters & setters
     */	
    /** Getter for property contextRoot.
     * @return Value of property contextRoot.
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * Setter for property newContextRoot.
     * 
     * @param newContextRoot New value of property contextRoot.
     */
    public void setContextRoot(String newContextRoot) throws java.beans.PropertyVetoException {
        newContextRoot = Utils.encodeUrlField(newContextRoot);
        String oldContextRoot = contextRoot;
        getVCS().fireVetoableChange("contextRoot", oldContextRoot, newContextRoot); // NOI18N
        contextRoot = newContextRoot;
        getPCS().firePropertyChange("contextRoot", oldContextRoot, contextRoot);    // NOI18N
    }

    /** Getter for property eligible.
     * @return Value of property eligible.
     */
	public boolean isEligible() {
		return eligible != null ? eligible.booleanValue() : false;
	}

    /**
     * Setter for property newEligible.
     * 
     * @param newEligible New value of property eligible.
     */
    public void setEligible(boolean newEligible) throws java.beans.PropertyVetoException {
		Boolean oldEligible = eligible;
		Boolean newEligibleAsBoolean = newEligible ? Boolean.TRUE : Boolean.FALSE;
		getVCS().fireVetoableChange("eligible", oldEligible, newEligibleAsBoolean);
		eligible = newEligibleAsBoolean;
		getPCS().firePropertyChange("eligible", oldEligible, eligible);		
        
    }

    /** Getter for property vendor.
     * @return Value of property vendor.
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Setter for property newVendor.
     * 
     * @param newVendor New value of property vendor.
     */
    public void setVendor(String newVendor) throws java.beans.PropertyVetoException {
        String oldVendor = vendor;
        getVCS().fireVetoableChange("vendor", oldVendor, newVendor);    // NOI18N
        vendor = newVendor;
        getPCS().firePropertyChange("vendor", oldVendor, vendor);       // NOI18N
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

    /* ------------------------------------------------------------------------
     * Persistence support.  Loads DConfigBeans from previously saved Deployment
     * plan file.
     */
    Collection getSnippets() {
        Collection snippets = new ArrayList();
        Snippet snipOne = new DefaultSnippet() {

            public CommonDDBean getDDSnippet() {
                SunApplicationClient sac = getConfig().getStorageFactory().createSunApplicationClient();
                String version = sac.getVersion().toString();

                try {
                    JavaWebStartAccess jwsa = sac.newJavaWebStartAccess();
                    if(jwsa != null) {
                        boolean hasContent = false;
                        if(Utils.notEmpty(contextRoot)) {
                            jwsa.setContextRoot(contextRoot);
                            hasContent = true;
                        }

                        if(eligible != null) {
                            jwsa.setEligible(Boolean.toString(isEligible()));
                            hasContent = true;
                        }

                        if(Utils.notEmpty(vendor)) {
                            jwsa.setVendor(vendor);
                            hasContent = true;
                        }

                        if(hasContent) {
                            sac.setJavaWebStartAccess(jwsa);
                        }
                    }
                } catch(VersionNotSupportedException ex) {
                }
                
                MessageDestination [] msgDests = (MessageDestination []) 
                        Utils.listToArray(getMessageDestinations(), MessageDestination.class, version);
                if(msgDests != null) {
                        sac.setMessageDestination(msgDests);
                }

                return sac;
            }
        };

        snippets.add(snipOne);
        return snippets;
    }

    public class AppClientRootParser implements ConfigParser {
        public Object parse(java.io.InputStream stream) throws IOException, SAXException, DDException {
            DDProvider provider = DDProvider.getDefault();
            SunApplicationClient result = null;
            
            if(stream != null) {
                // Exceptions (due to bad graph or other problem) are handled by caller.
                result = provider.getAppClientDDRoot(new org.xml.sax.InputSource(stream));
            } else {
                // If we have a null stream, return a blank graph.
                result = (SunApplicationClient) provider.newGraph(SunApplicationClient.class,
                        getConfig().getAppServerVersion().getAppClientVersionAsString());
            }

            // First set our version to match that of this deployment descriptor.
            getConfig().internalSetAppServerVersion(ASDDVersion.getASDDVersionFromAppClientVersion(result.getVersion()));
            
            return result;
        }
    }

    public class AppClientRootFinder implements ConfigFinder {
        public Object find(Object obj) {
            SunApplicationClient result = null;
            if(obj instanceof SunApplicationClient) {
                result = (SunApplicationClient) obj;
            }
            return result;
        }
    }

    protected ConfigParser getParser() {
        return new AppClientRootParser();
    }

    boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
        String uriText = getUriText();
        SunApplicationClient beanGraph = (SunApplicationClient) config.getBeans(uriText, constructFileName(),
            getParser(), new AppClientRootFinder());

        clearProperties();

        if(null != beanGraph) {
            try {
                JavaWebStartAccess jwsa = beanGraph.getJavaWebStartAccess();
                if(jwsa != null) {
                    contextRoot = jwsa.getContextRoot();
                    vendor = jwsa.getVendor();
                    
                    String eligibleAsString = jwsa.getEligible();
                    if(eligibleAsString != null) {
                        eligible = Utils.booleanValueOf(eligibleAsString) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } catch (VersionNotSupportedException ex) {
            }
            
            messageDestinations = Utils.arrayToList(beanGraph.getMessageDestination());
        } else {
            setDefaultProperties();
        }

        return (beanGraph != null);
    }

    protected void clearProperties() {
        contextRoot = null;
        eligible = null;
        vendor = null;
        messageDestinations = null;
    }

    protected void setDefaultProperties() {
    }

    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
    private HashMap appClientRootMap;

    /** Retrieve the XPathToFactory map for this DConfigBean.  For AppRoot,
     *  this maps application xpaths to factories for other contained root
     *  objects plus a SecurityRoleModel factory
     * @return
     */
    protected Map getXPathToFactoryMap() {
        if(appClientRootMap == null) {
            appClientRootMap = new HashMap(17);

            appClientRootMap.put("ejb-ref", new DCBGenericFactory(EjbRef.class));                   // NOI18N
            appClientRootMap.put("resource-ref", new DCBGenericFactory(ResourceRef.class));         // NOI18N
            appClientRootMap.put("resource-env-ref", new DCBGenericFactory(ResourceEnvRef.class));  // NOI18N

            J2EEBaseVersion moduleVersion = getJ2EEModuleVersion();
            if(moduleVersion.compareTo(AppClientVersion.APP_CLIENT_1_4) >= 0) {
//                appClientRootMap.put("message-destination", new DCBGenericFactory(MessageDestination.class)); // NOI18N
                appClientRootMap.put("service-ref", new DCBGenericFactory(ServiceRef.class));       // NOI18N
                
                if(moduleVersion.compareTo(AppClientVersion.APP_CLIENT_5_0) >= 0) {
                    appClientRootMap.put("message-destination-ref", new DCBGenericFactory(MessageDestinationRef.class));// NOI18N
                }
            }
        }

        return appClientRootMap;
    }	
}
