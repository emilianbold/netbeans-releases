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
package org.netbeans.modules.identity.ant;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Configurable;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.DiscoveryConfigurator;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.profile.api.configurator.TrustAuthorityConfigurator;

/**
 * Ant task for deploying configuration data to the AM server.
 *
 * Created on June 26, 2006, 11:00 PM
 * 
 * @author ptliu
 */
public class AMDeploy extends Task {
    private static final String PROP_AM_CONFIG_FILE = "AM_CONFIG_FILE"; //NOI18N
    
//    // This is temporarily until we have the appserver fix for FCS
//    private static final String WSC = "wsc";    //NOI18N
    
    private static final String LOCAL_DISCO = "LocalDisco";     //NOI18N
    
    private String amConfigFile;
    private String amConfigXmlDir;
    private DiscoveryConfigurator discoveryConfig;
    
    private static final Configurable[] wscConfigurables = {
        Configurable.SECURITY_MECH,
        Configurable.SIGN_RESPONSE,
        Configurable.USERNAME_PASSWORD_PAIRS,
        Configurable.USE_DEFAULT_KEYSTORE,
        Configurable.SERVICE_TYPE
    };
    
    private static final Configurable[] wspConfigurables = {
        Configurable.SERVICE_TYPE
    };
    
    private static final Configurable[] keystoreConfigurables = {
        Configurable.KEYSTORE_LOCATION,
        Configurable.KEYSTORE_PASSWORD,
        Configurable.KEY_ALIAS,
        Configurable.KEY_PASSWORD
    };
    
    /** Creates a new instance of AMDeploy */
    public AMDeploy() {
    }
    
    public void setAmconfigfile(String amConfigFile) {
        this.amConfigFile = amConfigFile;
        log("amConfigFile: " + amConfigFile);          //NOI18N
        if (System.getProperty(PROP_AM_CONFIG_FILE) == null) {
            System.setProperty(PROP_AM_CONFIG_FILE, amConfigFile);
        }
    }
    
    public void setAmconfigxmldir(String amConfigXmlDir) {
        this.amConfigXmlDir = amConfigXmlDir;
        log("amConfigXmlDir: " + amConfigXmlDir);      //NOI18N
    }
    
    public void execute() throws BuildException {
        try {
            deployWSCProviders();
            deployWSPProviders();
        } catch (ConfiguratorException ex) {
            //ex.printStackTrace();
            ex.getCause().printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void deployWSCProviders() {
        Collection<ProviderConfigurator> fileConfigs =
                ProviderConfigurator.getAllConfigurators(Type.WSC,
                AccessMethod.FILE, amConfigXmlDir);
        
        for (ProviderConfigurator fileConfig : fileConfigs) {
            log("Deploying wsc provider " + fileConfig.getProviderName()); //NOI18N
            log("ServerProperties: " + 
                    fileConfig.getValue(Configurable.SERVER_PROPERTIES)); //NOI18N
            
            ProviderConfigurator dynamicConfig =
                    ProviderConfigurator.getConfigurator(fileConfig.getProviderName(),
                    fileConfig.getType(), AccessMethod.DYNAMIC,
                    (ServerProperties) fileConfig.getValue(Configurable.SERVER_PROPERTIES));
            
            //
            // This is temporary until we have the appserver fix for FCS.  For
            // now we use "wsc" as the provider name.
            //
//            ProviderConfigurator dynamicConfig =
//                    ProviderConfigurator.getConfigurator(WSC,
//                    fileConfig.getType(), AccessMethod.DYNAMIC,
//                    (ServerProperties) fileConfig.getValue(Configurable.SERVER_PROPERTIES));
            
            transferConfigurationData(fileConfig, dynamicConfig, wscConfigurables);
            
            Boolean value = (Boolean) fileConfig.getValue(Configurable.USE_DEFAULT_KEYSTORE);
            if (value == Boolean.FALSE) {
                transferConfigurationData(fileConfig, dynamicConfig, keystoreConfigurables);
            }
            
            if (isLiberty(fileConfig)) {
                ArrayList list = new ArrayList();
                list.add(getDiscoveryConfigurator((ServerProperties) fileConfig.getValue(Configurable.SERVER_PROPERTIES)));
                dynamicConfig.setValue(Configurable.TRUST_AUTHORITY_CONFIG_LIST, list);
            }
            
            dynamicConfig.save();
        }
        
    }
    
    private void deployWSPProviders() {
        Collection<ProviderConfigurator> fileConfigs =
                ProviderConfigurator.getAllConfigurators(Type.WSP,
                AccessMethod.FILE, amConfigXmlDir);
        
        for (ProviderConfigurator fileConfig : fileConfigs) {
            log("Deploying wsp provider " + fileConfig.getProviderName()); //NOI18N
            log("ServerProperties: " + 
                    fileConfig.getValue(Configurable.SERVER_PROPERTIES)); //NOI18N
            SecurityMechanism secMech =
                    (SecurityMechanism) fileConfig.getValue(Configurable.SECURITY_MECH);
            
            ProviderConfigurator dynamicConfig =
                    ProviderConfigurator.getConfigurator(secMech.getName(),
                    Type.WSP, AccessMethod.DYNAMIC,
                    (ServerProperties) fileConfig.getValue(Configurable.SERVER_PROPERTIES));

            transferConfigurationData(fileConfig, dynamicConfig,
                    wspConfigurables);
           
          
            if (isLiberty(fileConfig)) {
                //
                // Need to reset the security mechanism for the preconfigured
                // liberty providers which are invalid.
                //
                dynamicConfig.setValue(Configurable.SECURITY_MECH, secMech);
                
                // wsp endpoint cannot be null and cannot be empty
                dynamicConfig.setValue(Configurable.WSP_ENDPOINT, "http://wsc.com");   //NOI18N
                
                dynamicConfig.save();
              
                DiscoveryConfigurator discoveryConfig = getDiscoveryConfigurator(
                        (ServerProperties) fileConfig.getValue(Configurable.SERVER_PROPERTIES));
        
                log("Registering provider with Discovery Service"); //NOI18N
                discoveryConfig.unregisterProvider(dynamicConfig);
                discoveryConfig.registerProvider(dynamicConfig);
            } else {
                dynamicConfig.save();
            }
        }
    }
    
    private void transferConfigurationData(ProviderConfigurator fromConfig,
            ProviderConfigurator toConfig, Configurable[] configurables) {
        for (Configurable configurable : configurables) {
            log(configurable + ": " + fromConfig.getValue(configurable)); //NOI18N
            toConfig.setValue(configurable, fromConfig.getValue(configurable));
        }
    }
    
    private boolean isLiberty(ProviderConfigurator config) {
        SecurityMechanism secMech = (SecurityMechanism) config.getValue(Configurable.SECURITY_MECH);
        
        return secMech.isLiberty();
    }
    
    private DiscoveryConfigurator getDiscoveryConfigurator(ServerProperties properties) {
        if (discoveryConfig == null) {
            discoveryConfig = (DiscoveryConfigurator) TrustAuthorityConfigurator.getConfigurator(
                    LOCAL_DISCO, TrustAuthorityConfigurator.Type.DISCOVERY,
                    AccessMethod.DYNAMIC, properties);
            
            discoveryConfig.setValue(TrustAuthorityConfigurator.Configurable.ENDPOINT,
                    properties.getProperty(ServerProperties.PROP_LIBERTY_DISCO_SERVICE_URL));
            discoveryConfig.save();
        }
        
        return discoveryConfig;
    }
}
