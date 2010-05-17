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
