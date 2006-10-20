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

package org.netbeans.modules.identity.profile.api.configurator.spi;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;

/**
 * This interface is used to provide an abstraction over the ProviderConfig
 * class from the AM client sdk. It allows us to provide both a dynamic 
 * (using the client sdk API) and a file-based (using xml) implementation. 
 *
 * Created on June 9, 2006, 7:59 PM
 *
 * @author ptliu
 */
public interface ProviderConfig {
 
    public boolean isResponseSignEnabled();
    
    public void setResponseSignEnabled(boolean flag);
    
    public String getKeyAlias();
    
    public void setKeyAlias(String keyAlias);
    
    public String getKeyStoreFile();
    
    public String getKeyStorePassword();
    
    public String getKeyPassword();
    
    public void setKeyStore(String location, String password, String keyPassword);
    
    public Object getProperty(String propName);
    
    public void setProperty(String propName, Object value);
    
    /**
     * Return a collection of security mechanism URIs.
     *
     */
    public Collection<String> getSecurityMechanisms();
    
    /**
     * Set a collection of security mechanism URIs.
     *
     */
    public void setSecurityMechanisms(Collection<String> securityMechs);
   
    public void saveProvider();
    
    public void deleteProvider();
    
    public String getWSPEndpoint();
    
    public void setWSPEndpoint(String endpoint);
    
    public boolean isProviderExists();
    
    public ServerProperties getServerProperties();
    
    public void setServerProperties(ServerProperties properties);
    
    public void setUserName(String userName);
    
    public String getUserName();
    
    public void setPassword(String password);
    
    public String getPassword();
    
    public Collection<Vector<String>> getUserNamePasswordPairs();
    
    public void setUserNamePasswordPairs(Collection<Vector<String>> pairs);
    
    /**
     *  Returns a collection of all the provider names. Note that the 
     *  provider type should be the type bound to the current ProviderConfig
     *  instance.
     *
     */
    public Collection<String> getAllProviderNames();
    
    public void setServiceType(String serviceType);
    
    public String getServiceType();
    
    public void setDefaultKeyStore(boolean flag);
    
    public boolean useDefaultKeyStore();

    public void setTrustAuthorityConfigList(List<TrustAuthorityConfig> trustAuthConfigs);
    
    
}

