/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.identity.profile.api.configurator.impl.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBElement;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.spi.ProviderConfig;
import org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb.*;
import org.netbeans.modules.identity.profile.api.configurator.spi.TrustAuthorityConfig;

/**
 * File-based implementation of the ProviderConfig interface.
 *
 * Created on June 9, 2006, 7:59 PM
 *
 * @author Srividhya Narayanan
 * @author ptliu
 */
public class ProviderConfigImpl implements ProviderConfig {
    private String pName = null;
    private String pType = null;
    private JAXBElement<AMConfigType> amconfig = null;
    private ObjectFactory objFactory = null;
    private ProviderConfigType providerConfig = null;
    private String path;
    /**
     * Creates a new instance of ProviderConfigImpl
     *
     * @param providerName  The name of the provider used by the Access Manager
     *                      runtime to store the configuration
     * @param type          The congiurator type (WSP/WSC)
     * @param properties    The particular instance properties
     * @param path          The project root path. The cache file is created
     *                      under src/conf/amserver dir relative to this path.
     */
    public ProviderConfigImpl(String providerName, String type,
            String path) {
        this.pName = providerName;
        this.pType = type;
        this.path = path;
        
        objFactory = new ObjectFactory();
        amconfig = AMConfigManager.getDefault().getAMConfig(path);
        
        for (ProviderConfigType p : amconfig.getValue().getProviderConfig()) {
            if (p.getName().equals(providerName) && p.getType().equals(type)) {
                providerConfig = p;
                break;
            }
        }
        
        if (providerConfig == null) {
            providerConfig = objFactory.createProviderConfigType();
            amconfig.getValue().getProviderConfig().add(providerConfig);
            providerConfig.setName(providerName);
            providerConfig.setType(type);
        }
    }
    
    public boolean isResponseSignEnabled() {
        ResponseType resType = providerConfig.getResponse();
        if (resType != null)
            return resType.isSigned();
        return false;
    }
    
    public void setResponseSignEnabled(boolean flag) {
        ResponseType resType = providerConfig.getResponse();
        if (resType == null) {
            resType = objFactory.createResponseType();
            providerConfig.setResponse(resType);
        }
        resType.setSigned(flag);
    }
    
    public String getKeyAlias() {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType != null)
            return certType.getKeyAlias();
        return null;
    }
    
    public void setKeyAlias(String keyAlias) {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType == null) {
            certType = objFactory.createCertificateSettingsType();
            providerConfig.setCertificateSettings(certType);
        }
        certType.setKeyAlias(keyAlias);
    }
    
    public String getKeyStoreFile() {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        
        if (certType != null)
            return certType.getKeystoreLocation();
        return null;
    }
    
    public String getKeyStorePassword() {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType != null)
            return certType.getKeystorePassword();
        return null;
    }
    
    public void setKeyStore(String location, String password, String keyPassword) {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType == null) {
            certType = objFactory.createCertificateSettingsType();
            providerConfig.setCertificateSettings(certType);
        }
        certType.setKeystoreLocation(location);
        certType.setKeystorePassword(password);
        certType.setKeyPassword(keyPassword);
    }
    
    /**
     * Not impl. as it is assumed not needed
     */
    public Object getProperty(String propName) {
        return null;
    }
    
    /**
     * Not impl. as it is assumed not needed
     */
    public void setProperty(String propName, Object value) {
    }
    
    public Collection<String> getSecurityMechanisms() {
        Collection<String> l = new ArrayList<String>();
        SecurityMechanismType secmechType = providerConfig.getSecurityMechanism();
        if (secmechType != null) {
            l.add(secmechType.getUri());
        }
        return l;
    }
    
    public void setSecurityMechanisms(Collection<String> securityMechs) {
        if (securityMechs.size() == 0) {
            providerConfig.setSecurityMechanism(null);
        } else if (securityMechs.size() == 1) {
            SecurityMechanismType secmechType = providerConfig.getSecurityMechanism();
            if (secmechType == null) {
                secmechType = objFactory.createSecurityMechanismType();
                providerConfig.setSecurityMechanism(secmechType);
            }
            String uri = securityMechs.iterator().next();
            secmechType.setUri(uri);
        } else {
            //TODO: need to support multiple instances of security mechs
        }
    }
    
    public void saveProvider() {
        //
        // RESOLVE:  We are writing out the amconfig.xml each time
        // this method is called on the ProviderConfigImpl.  An optimization
        // would be to only write it out if the last referencing
        // ProviderConfigImpl calls the save.
        //
        AMConfigManager.getDefault().saveAMConfig(amconfig, path);
    }
    
    public void deleteProvider() {
        List<ProviderConfigType> tmp = new ArrayList();
        AMConfigType amconfigType = amconfig.getValue();
        List<ProviderConfigType> pConfig = amconfigType.getProviderConfig();
        for (ProviderConfigType p : pConfig) {
            if (p.getName().equals(pName) && p.getType().equals(pType)) {
                // Found the provider
                providerConfig = p;
                break;
            } else {
                tmp.add(p);
            }
        }
        pConfig.clear();
        for (ProviderConfigType p : tmp) {
            pConfig.add(p);
        }
        saveProvider();
    }
    
    /**
     * Not impl. as it is assumed not needed
     */
    public String getWSPEndpoint() {
        return null;
    }
    
    /**
     * Not impl. as it is assumed not needed
     */
    public void setWSPEndpoint(String endpoint) {
    }
    
    public boolean isProviderExists() {
        return true;
    }
    
    public ServerProperties getServerProperties(String id) {
        ServerConfigType serverConfigType = amconfig.getValue().getServerConfig();
        ServerProperties properties = ServerProperties.getInstance(id);
        
        if (serverConfigType != null) {
            properties.setProperty(ServerProperties.PROP_ID, serverConfigType.getName());
            properties.setProperty(ServerProperties.PROP_HOST, serverConfigType.getHost());
            properties.setProperty(ServerProperties.PROP_PORT, serverConfigType.getPort());
            properties.setProperty(ServerProperties.PROP_USERNAME, serverConfigType.getUsername());
            properties.setProperty(ServerProperties.PROP_PASSWORD, serverConfigType.getPassword());
        }
        
        return properties;
    }
    
    public void setServerProperties(ServerProperties properties) {
        ServerConfigType serverConfigType = amconfig.getValue().getServerConfig();
        
        if (serverConfigType == null) {
            serverConfigType = objFactory.createServerConfigType();
            amconfig.getValue().setServerConfig(serverConfigType);
        }
        
        serverConfigType.setName(properties.getProperty(ServerProperties.PROP_ID));
        serverConfigType.setHost(properties.getProperty(ServerProperties.PROP_HOST));
        serverConfigType.setPort(properties.getProperty(ServerProperties.PROP_PORT));
        serverConfigType.setUsername(properties.getProperty(ServerProperties.PROP_USERNAME));
        serverConfigType.setPassword(properties.getProperty(ServerProperties.PROP_PASSWORD));
        
    }
    
    public String getUserName() {
        UserPassSettingsType type = getUserPassSettings();
        
        return type.getUsername();
    }
    
    public void setUserName(String userName) {
        UserPassSettingsType type = getUserPassSettings();
        
        type.setUsername(userName);
    }
    
    public String getPassword() {
        UserPassSettingsType type = getUserPassSettings();
        
        return type.getPassword();
    }
    
    public void setPassword(String password) {
        UserPassSettingsType type = getUserPassSettings();
        
        type.setPassword(password);
    }
    
    private UserPassSettingsType getUserPassSettings() {
        UserPassSettingsType type = providerConfig.getUserPassSettings();
        
        if (type == null) {
            type = (UserPassSettingsType) objFactory.createUserPassSettingsType();
            providerConfig.setUserPassSettings(type);
        }
        
        return type;
    }
    
    public void setUserNamePasswordPairs(Collection<Vector<String>> pairs) {
        // not implemented
    }
    
    public Collection<Vector<String>> getUserNamePasswordPairs() {
        String userName = getUserName();
        String password = getPassword();
        Collection<Vector<String>> pairs = new Vector<Vector<String>>();
        
        // Only create the pair if either or both userName and password
        // are not null.
        if (userName != null || password != null) {
            Vector<String> pair = new Vector<String>();
            pair.add(userName);
            pair.add(password);
            pairs.add(pair);
        }
        
        return pairs;
    }
    
    private boolean isServerPropExist(ServerProperties properties) {
        AMConfigType aType = amconfig.getValue();
        if (aType != null) {
            return aType.getServerConfig().getName().equals(
                    properties.getProperty(ServerProperties.PROP_ID));
        }
        return false;
    }
    
    public Collection<String> getAllProviderNames() {
        Collection<ProviderConfigType> providerConfigs = amconfig.getValue().getProviderConfig();
        Collection<String> providerNames = new ArrayList<String>();
        
        for (ProviderConfigType providerConfig : providerConfigs) {
            if (pType.equals(providerConfig.getType())) {
                providerNames.add(providerConfig.getName());
            }
        }
        
        return providerNames;
    }
    
    public void setServiceType(String serviceType) {
        providerConfig.setServiceType(serviceType);
    }
    
    public void setDefaultKeyStore(boolean flag) {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType == null) {
            certType = objFactory.createCertificateSettingsType();
            providerConfig.setCertificateSettings(certType);
        }
        certType.setDefaultKeystore(flag);
    }
    
    public boolean useDefaultKeyStore() {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        
        if (certType != null) {
            Boolean value = certType.isDefaultKeystore();
            return (value != null) ? value : true;
        }
        
        return true;
    }
    
    public String getServiceType() {
        return providerConfig.getServiceType();
    }
    
    public String getKeyPassword() {
        CertificateSettingsType certType = providerConfig.getCertificateSettings();
        if (certType != null)
            return certType.getKeyPassword();
        return null;
    }
    
    public void setTrustAuthorityConfigList(List<TrustAuthorityConfig> trustAuthConfigs) {
        //noop
    }
    
    public void close() {
        AMConfigManager.getDefault().removeAMConfig(path);
    }
}
