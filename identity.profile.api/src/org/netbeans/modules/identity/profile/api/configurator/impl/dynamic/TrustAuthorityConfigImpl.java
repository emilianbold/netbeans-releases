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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.TrustAuthorityConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.spi.TrustAuthorityConfig;

/**
 * Dynamic implementation class for the TrustAuthorityConfig interface.
 *
 * Created on August 2, 2006, 3:35 AM
 *
 * @author ptliu
 */
public class TrustAuthorityConfigImpl implements TrustAuthorityConfig {
    private static final String AM_TRUST_AUTHORITY_CONFIG_CLASS = "com.sun.identity.wss.provider.TrustAuthorityConfig"; //NOI18N
    
    private static final String AM_DISCOVERY_TRUST_AUTHORITY_FIELD = "DISCOVERY_TRUST_AUTHORITY";   //NOI18N
    
    private static final String AM_GET_CONFIG_METHOD = "getConfig";   //NOI18N
    
    private static final String AM_SET_ENDPOINT_METHOD = "setEndpoint";    //NOI18N
    
    private static final String AM_GET_ENDPOINT_METHOD = "getEndpoint";    //NOI18N
    
    private static final String AM_SAVE_CONFIG_METHOD = "saveConfig";   //NOI18N
    
    private String name;
    private Type type;
    private ServerProperties properties;
    private Object trustAuthConfig;
    private Class trustAuthConfigClass;
    private ClassLoader classLoader;
    
    /** Creates a new instance of TrustAuthorityConfigImpl */
    public TrustAuthorityConfigImpl(String name, Type type,
            ServerProperties properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }
    
    protected ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = ClassLoaderManager.getDefault().getClassLoader(properties);
        }
        
        return classLoader;
    }
    
    protected Class getTrustAuthorityConfigClass() {
        if (trustAuthConfigClass == null) {
            try {
                trustAuthConfigClass = getClassLoader().loadClass(AM_TRUST_AUTHORITY_CONFIG_CLASS);
            } catch (Exception ex) {
                throw createConfiguratorException(ex);
            }
        }
        
        return trustAuthConfigClass;
    }
    
    protected Object getTrustAuthorityConfig() {
        if (trustAuthConfig == null) {
            try {
                Method method = getTrustAuthorityConfigClass().getMethod(AM_GET_CONFIG_METHOD,
                        String.class, String.class);
                trustAuthConfig = method.invoke(null, name, getTypeValue());
               
                //System.out.println("trustAuthConfig = " + trustAuthConfig); //NOI18N
            } catch (Exception ex) {
                throw createConfiguratorException(ex);
            }
        }
        
        return trustAuthConfig;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public ServerProperties getServerProperties() {
        return properties;
    }
    
    
    public void setEndpoint(String endpoint) {
        try {
            Method method = getTrustAuthorityConfigClass().getMethod(AM_SET_ENDPOINT_METHOD);
            method.invoke(getTrustAuthorityConfig(), endpoint);
        } catch (Exception ex) {
            throw createConfiguratorException(ex);
        }
    }
    
    public String getEndpoint() {
        try {
            Method method = getTrustAuthorityConfigClass().getMethod(AM_GET_ENDPOINT_METHOD);
            return (String) method.invoke(getTrustAuthorityConfig());
        } catch (Exception ex) {
            throw createConfiguratorException(ex);
        }
    }
    
    public void saveConfig() {
        try {
            Method method = getTrustAuthorityConfigClass().getMethod(AM_SAVE_CONFIG_METHOD,
                    getTrustAuthorityConfigClass());
            method.invoke(null, getTrustAuthorityConfig());
        } catch (Exception ex) {
            throw createConfiguratorException(ex);
        }
    }
    
    private String getTypeValue() {
        try {
            String fieldName = null;
            
            if (type == Type.DISCOVERY) {
                fieldName = AM_DISCOVERY_TRUST_AUTHORITY_FIELD;
            }
            
            Field field = getTrustAuthorityConfigClass().getField(fieldName);
            return (String) field.get(null);
        } catch (Exception ex) {
            throw createConfiguratorException(ex);
        }
    }
    
    protected ConfiguratorException createConfiguratorException(Exception ex) {
        Throwable cause = ex.getCause();
        
        return new ConfiguratorException((cause != null) ? cause : ex);
    }
}
