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
