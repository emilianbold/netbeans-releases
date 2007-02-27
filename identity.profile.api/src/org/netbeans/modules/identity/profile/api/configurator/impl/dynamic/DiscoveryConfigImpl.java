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

package org.netbeans.modules.identity.profile.api.configurator.impl.dynamic;

import java.lang.reflect.Method;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.TrustAuthorityConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.spi.DiscoveryConfig;
import org.netbeans.modules.identity.profile.api.configurator.spi.ProviderConfig;

/**
 * Dynamic implementation class for the DiscoveryConfig interface.
 *
 * Created on August 2, 2006, 3:40 AM
 *
 * @author ptliu
 */
public class DiscoveryConfigImpl extends TrustAuthorityConfigImpl
        implements DiscoveryConfig{
    private static final String AM_DISCOVERY_CONFIG_CLASS = "com.sun.identity.wss.provider.DiscoveryConfig"; //NOI18N
    
    private static final String AM_REGISTER_PROVIDER_WITH_TA_METHOD = "registerProviderWithTA"; //NOI18N
    private static final String AM_UNREGISTER_PROVIDER_WITH_TA_METHOD = "unregisterProviderWithTA"; //NOI18N
    
    private Class discoveryConfigClass;
    
    /** Creates a new instance of DiscoveryConfigImpl */
    public DiscoveryConfigImpl(String name, Type type, ServerProperties properties) {
        super(name, type, properties);
    }

    private Class getDiscoveryConfigClass() {
        if (discoveryConfigClass == null) {
            try {
                discoveryConfigClass = getClassLoader().loadClass(AM_DISCOVERY_CONFIG_CLASS);
            } catch (Exception ex) {
                throw createConfiguratorException(ex);
            }
        }
        
        return discoveryConfigClass;
    }
    
    public void registerProvider(ProviderConfig providerConfig) {
        try {
            ProviderConfigImpl providerConfigImpl = (ProviderConfigImpl) providerConfig;
            Method method = getDiscoveryConfigClass().getMethod(
                    AM_REGISTER_PROVIDER_WITH_TA_METHOD, 
                    providerConfigImpl.getProviderConfigClass(), 
                    String.class);
            method.invoke(getTrustAuthorityConfig(), 
                    ((ProviderConfigImpl) providerConfig).getProviderConfig(),
                    providerConfig.getServiceType());            
        } catch (Exception ex) {
            throw createConfiguratorException(ex);
        }
    }
   
    public void unregisterProvider(ProviderConfig providerConfig) {
        ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        
        try {
            Method method = getDiscoveryConfigClass().getMethod(
                    AM_UNREGISTER_PROVIDER_WITH_TA_METHOD, 
                    String.class);
            method.invoke(getTrustAuthorityConfig(), 
                    providerConfig.getServiceType());            
        } catch (Exception ex) {
            System.out.println("unregister failed " + ex.getMessage()); //NOI18N
            throw createConfiguratorException(ex);
        } finally {
            Thread.currentThread().setContextClassLoader(threadContextClassLoader);
        }
    }
}
