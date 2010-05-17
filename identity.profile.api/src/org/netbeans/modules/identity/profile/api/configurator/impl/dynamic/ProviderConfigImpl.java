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

package org.netbeans.modules.identity.profile.api.configurator.impl.dynamic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanismHelper;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.spi.ProviderConfig;
import org.netbeans.modules.identity.profile.api.configurator.spi.TrustAuthorityConfig;

/**
 * Dynamic implementation class for the ProviderConfig interface.
 *
 * Created on June 9, 2006, 7:59 PM
 *
 * @author ptliu
 */
public class ProviderConfigImpl implements ProviderConfig {
    private static final String WSP = "WSP";    //NOI18N
    
    private static final String AM_PROVIDER_CONFIG_CLASS = "com.sun.identity.wss.provider.ProviderConfig"; //NOI18N
    
    private static final String AM_PASSWORD_CREDENTIAL_CLASS = "com.sun.identity.wss.security.PasswordCredential";  //NOI18N
    
    private static final String AM_GET_PROVIDER_METHOD = "getProvider";   //NOI18N
    
    private static final String AM_IS_RESPONSE_SIGN_ENABLED_METHOD = "isResponseSignEnabled";     //NOI18N
    
    private static final String AM_SET_RESPONSE_SIGN_ENABLED_METHOD = "setResponseSignEnabled";   //NOI18N
    
    private static final String AM_GET_KEY_ALIAS_METHOD = "getKeyAlias";      //NOI18N
    
    private static final String AM_SET_KEY_ALIAS_METHOD = "setKeyAlias";      //NOI18N
    
    private static final String AM_GET_KEY_PASSWORD_METHOD = "getKeyPassword";     //NOI18N
    
    private static final String AM_GET_KEY_STORE_FILE_METHOD = "getKeyStoreFile";     //NOI18N
    
    private static final String AM_GET_KEY_STORE_PASSWORD_METHOD = "getKeyStorePassword";    //NOI18N
    
    private static final String AM_SET_KEY_STORE_METHOD = "setKeyStore";      //NOI18N
    
    private static final String AM_GET_PROPERTY_METHOD = "getProperty";       //NOI18N
    
    private static final String AM_SET_PROPERTY_METHOD = "setProperty";       //NOI18N
    
    private static final String AM_GET_SECURITY_MECHANISMS_METHOD = "getSecurityMechanisms";   //NOI18N
    
    private static final String AM_SET_SECURITY_MECHANISMS_METHOD = "setSecurityMechanisms";   //NOI18N
    
    private static final String AM_GET_ALL_SUPPORT_SECURITY_MECH_METHOD = "getAllSupportedSecurityMech"; //NOI18N
    
    private static final String AM_GET_ALL_MESSAGE_LEVEL_SECURITY_MECH_METHOD = "getAllMessageLevelSecurityMech";   //NOI18N
    
    private static final String AM_SAVE_PROVIDER_METHOD = "saveProvider";     //NOI18N
    
    private static final String AM_DELETE_PROVIDER_METHOD = "deleteProvider"; //NOI18N
    
    private static final String AM_GET_WSP_ENDPOINT_METHOD = "getWSPEndpoint";    //NOI18N
    
    private static final String AM_SET_WSP_ENDPOINT_METHOD = "setWSPEndpoint";    //NOI18N
    
    private static final String AM_IS_PROVIDER_EXISTS_METHOD = "isProviderExists";     //NOI18N
    
    private static final String AM_GET_USERS_METHOD = "getUsers";   //NOI18N
    
    private static final String AM_SET_USERS_METHOD = "setUsers";   //NOI18N
    
    private static final String AM_GET_USERNAME_METHOD = "getUserName";    //NOI18N
    
    private static final String AM_GET_PASSWORD_METHOD = "getPassword";     //NOI18N
    
    private static final String AM_SET_SERVICE_TYPE_METHOD = "setServiceType";      //NOI18N
    
    private static final String AM_GET_SERVICE_TYPE_METHOD = "getServiceType";      //NOI18N
    
    private static final String AM_SET_DEFAULT_KEY_STORE_METHOD = "setDefaultKeyStore";     //NOI18N
    
    private static final String AM_USE_DEFAULT_KEY_STORE_METHOD = "useDefaultKeyStore";     //NOI18N
    
    private static final String AM_SET_TRUST_AUTHORITY_CONFIG_LIST_METHOD = "setTrustAuthorityConfigList";  //NOI18N
    
    private static final String DEFAULT_RELATIVE_KEYSTORE_LOCATION = "/domains/domain1/config/keystore.jks"; //NOI18N
    
    private static final String DEFAULT_KEYSTORE_PASSWORD = "adminadmin";   //NOI18N
    
    private static final String DEFAULT_KEY_ALIAS = "s1as";         //NOI18N
    
    private static final String AM_CONFIG_FILE_RELATIVE_PATH = "/addons/amserver/AMConfig.properties";  //NOI18N
    
    private static final String DEFAULT_APPSERVER_LOCATION = "{sjsas.root}";  //NOI18N
    
    private Class providerConfigClass;
    private Class passwordCredentialClass;
    private Object providerConfig;
    private String providerName;
    private String providerType;
    private ServerProperties properties;
    private SecurityMechanismHelper secMechHelper;
    
    /**
     * Creates a new instance of ProviderConfigImpl
     */
    public ProviderConfigImpl(String providerName, String type,
            ServerProperties properties) {
        this.providerName = providerName;
        this.providerType = type;
        this.properties = properties;
        this.secMechHelper = new SecurityMechanismHelper(properties.getProperty(ServerProperties.PROP_ID));
        
        //
        // Initialize providerConfig now so we can report
        // any exception early on.
        //
        getProviderConfig();
    }
    
    Class getProviderConfigClass() {
        if (providerConfigClass == null) {
            try {
                ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(properties);
                providerConfigClass = loader.loadClass(AM_PROVIDER_CONFIG_CLASS);
            } catch (Exception ex) {
                throw ConfiguratorException.create(ex);
            }
        }
        
        return providerConfigClass;
    }
    
    Object getProviderConfig() {
        if (providerConfig == null) {
            try {
                Method method = getProviderConfigClass().getMethod(AM_GET_PROVIDER_METHOD,
                        String.class, String.class);
                providerConfig = method.invoke(null, providerName, providerType);
                
                //System.out.println("providerConfig = " + providerConfig);
                
                //
                // For WSP profiles, we need to create it if it
                // does not exist and add the appropriate security mechanism
                // uri.
                // 
                if (providerType.equals(WSP) && !isProviderExists()) {
                    createProvider();
                }
            } catch (Exception ex) {
                ClassLoaderManager.getDefault().removeClassLoader(properties);
                throw ConfiguratorException.create(ex);
            }
        }
        
        return providerConfig;
    }
    
    private void createProvider() {
        Collection<String> names = new ArrayList<String>();
        names.add(providerName);
        Collection<String> uris = secMechHelper.getSecurityMechanismURIsFromNames(names);
        setSecurityMechanisms(uris);
        setDefaultKeyStore(true);
        
        /*
        String amConfigFile = properties.getProperty(ServerProperties.PROP_AM_CONFIG_FILE);
        
        // Convert to use forward slash.
        amConfigFile = amConfigFile.replace('\\', '/');
        
        String appServerLocation = DEFAULT_APPSERVER_LOCATION;
        
        if (properties.isDefault()) {
            int index = amConfigFile.indexOf(AM_CONFIG_FILE_RELATIVE_PATH);
            
            if (index != -1) {
                appServerLocation = amConfigFile.substring(0, index);
            }
        }
       
        
        setKeyStore(appServerLocation + DEFAULT_RELATIVE_KEYSTORE_LOCATION,
                DEFAULT_KEYSTORE_PASSWORD, null);
        setKeyAlias(DEFAULT_KEY_ALIAS);
        */
        
        saveProvider();
        
        // Need to recreate the providerConfig after saving.
        providerConfig = null;
        getProviderConfig();
    }
    
    public boolean isResponseSignEnabled() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_IS_RESPONSE_SIGN_ENABLED_METHOD);
            return ((Boolean) method.invoke(getProviderConfig())).booleanValue();
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setResponseSignEnabled(boolean flag) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_RESPONSE_SIGN_ENABLED_METHOD,
                    Boolean.TYPE);
            method.invoke(getProviderConfig(), flag);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getKeyAlias() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_KEY_ALIAS_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setKeyAlias(String keyAlias) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_KEY_ALIAS_METHOD,
                    String.class);
            method.invoke(getProviderConfig(), keyAlias);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getKeyPassword() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_KEY_PASSWORD_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getKeyStoreFile() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_KEY_STORE_FILE_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getKeyStorePassword() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_KEY_STORE_PASSWORD_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setKeyStore(String location, String password, String keyPassword) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_KEY_STORE_METHOD,
                    String.class, String.class, String.class);
            method.invoke(getProviderConfig(), location, password, keyPassword);
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw ConfiguratorException.create(ex);
        }
    }
    
    public Object getProperty(String propName) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_PROPERTY_METHOD,
                    String.class);
            return method.invoke(getProviderConfig(), propName);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setProperty(String propName, Object value) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_PROPERTY_METHOD,
                    String.class, Object.class);
            method.invoke(getProviderConfig(), propName, value);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public Collection<String> getSecurityMechanisms() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_SECURITY_MECHANISMS_METHOD);
            return (Collection<String>) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setSecurityMechanisms(Collection<String> securityMechs) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_SECURITY_MECHANISMS_METHOD,
                    List.class);
            method.invoke(getProviderConfig(), new ArrayList(securityMechs));
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void saveProvider() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SAVE_PROVIDER_METHOD,
                    getProviderConfigClass());
            method.invoke(null, getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void deleteProvider() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_DELETE_PROVIDER_METHOD ,
                    String.class);
            method.invoke(null, providerName);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getWSPEndpoint() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_WSP_ENDPOINT_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setWSPEndpoint(String endpoint) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_WSP_ENDPOINT_METHOD,
                    String.class);
            method.invoke(getProviderConfig(), endpoint);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public boolean isProviderExists() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_IS_PROVIDER_EXISTS_METHOD,
                    String.class, String.class);
            return (Boolean) method.invoke(null, providerName, providerType);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
        
    }
    
    public ServerProperties getServerProperties(String id) {
        return properties;
    }
    
    public void setServerProperties(ServerProperties properties) {
        // TODO: need to reinitialize everything
    }
    
    public String getUserName() {
        return null;
    }
    
    public void setUserName(String userName) {
    }
    
    public String getPassword() {
        return null;
    }
    
    public void setPassword(String password) {
    }
    
    public void setUserNamePasswordPairs(Collection<Vector<String>> pairs) {
        List credentials = convertToPasswordCredentials(pairs);
        
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_USERS_METHOD,
                    List.class);
            method.invoke(getProviderConfig(), credentials);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public Collection<Vector<String>> getUserNamePasswordPairs() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_USERS_METHOD);
            List credentials = (List) method.invoke(getProviderConfig());
            
            return convertToUserNamePasswordPairs(credentials);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    private Class getPasswordCredentialClass() {
        if (passwordCredentialClass == null) {
            try {
                ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(properties);
                passwordCredentialClass = loader.loadClass(AM_PASSWORD_CREDENTIAL_CLASS);
            } catch (Exception ex) {
                throw ConfiguratorException.create(ex);
            }
        }
        return passwordCredentialClass;
    }
    
    private List convertToPasswordCredentials(Collection<Vector<String>> pairs) {
        List credentials = new ArrayList();
        
        for (Vector<String> pair : pairs) {
            credentials.add(convertToPasswordCredential(pair));
        }
        
        return credentials;
    }
    
    private Object convertToPasswordCredential(Vector<String> pair) {
        try {
            Constructor constructor =
                    getPasswordCredentialClass().getConstructor(String.class, String.class);
            
            String userName = pair.get(0);
            
            //
            // Empty user name exposes a security leak on the UserNameToken
            // profile in the AM authentication provider.  Appending an empty
            // space to plug the hole.
            //
            if (userName == null || userName.length() == 0) {
                userName = " ";    //NOI18N 
            }
            
            return constructor.newInstance(userName, pair.get(1));
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    private Collection<Vector<String>> convertToUserNamePasswordPairs(List credentials) {
        Collection pairs = new Vector<Vector<String>>();
        
        if (credentials != null) {
            Iterator iter = credentials.iterator();
            
            while (iter.hasNext()) {
                pairs.add(convertToUserNamePasswordPair(iter.next()));
            }
        }
        
        return pairs;
    }
    
    private Vector<String> convertToUserNamePasswordPair(Object credential) {
        try {
            Method method = getPasswordCredentialClass().getMethod(AM_GET_USERNAME_METHOD);
            String userName = (String) method.invoke(credential);
            method = getPasswordCredentialClass().getMethod(AM_GET_PASSWORD_METHOD);
            String password = (String) method.invoke(credential);
            
            Vector<String> pair = new Vector<String>();
            pair.add(userName);
            pair.add(password);
            
            return pair;
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    /**
     * This is currently not support by amclientsdk.
     *
     */
    public Collection<String> getAllProviderNames() {
        // simply return an empty list.
        return Collections.emptyList();
    }
    
    public void setServiceType(String serviceType) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_SERVICE_TYPE_METHOD,
                    String.class);
            method.invoke(getProviderConfig(), serviceType);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public String getServiceType() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_GET_SERVICE_TYPE_METHOD);
            return (String) method.invoke(getProviderConfig());
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void setDefaultKeyStore(boolean flag) {
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_DEFAULT_KEY_STORE_METHOD,
                    Boolean.TYPE);
            method.invoke(getProviderConfig(), flag);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
        
    }
    
    public boolean useDefaultKeyStore() {
        try {
            Method method = getProviderConfigClass().getMethod(AM_USE_DEFAULT_KEY_STORE_METHOD);
            return ((Boolean) method.invoke(getProviderConfig())).booleanValue();
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
 
    public void setTrustAuthorityConfigList(List<TrustAuthorityConfig> trustAuthConfigs) {
        List list = new ArrayList();
        
        for (TrustAuthorityConfig config : trustAuthConfigs) {
            TrustAuthorityConfigImpl configImpl = (TrustAuthorityConfigImpl) config;
            list.add(configImpl.getTrustAuthorityConfig());
        }
        
        try {
            Method method = getProviderConfigClass().getMethod(AM_SET_TRUST_AUTHORITY_CONFIG_LIST_METHOD);
            method.invoke(getProviderConfig(), list);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    public void close() {
        
    }
}
