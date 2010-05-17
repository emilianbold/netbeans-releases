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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;

/**
 * Helper class for retrieving the low-level SecurityMechanism instances from
 * the AM client sdk.
 *
 * Created on July 12, 2006, 1:20 PM
 *
 * @author ptliu
 */
public class SecurityMechanismRetriever {
    
    private static final String AM_PROVIDER_CONFIG_CLASS = "com.sun.identity.wss.provider.ProviderConfig"; //NOI18N
    
    private static final String AM_GET_ALL_SUPPORT_SECURITY_MECH_METHOD = "getAllSupportedSecurityMech"; //NOI18N
    
    private static final String AM_GET_ALL_MESSAGE_LEVEL_SECURITY_MECH_METHOD = "getAllMessageLevelSecurityMech";   //NOI18N
    
    private static final String AM_SECURITY_MECHANISM_CLASS = "com.sun.identity.wss.security.SecurityMechanism";   //NOI18N
    
    private static final String AM_GET_LIBERTY_SECURITY_MECHANISM_URIS_METHOD =
            "getLibertySecurityMechanismURIs";  //NOI18N
    
    private static final String AM_GET_SECURITY_MECHANISM_METHOD = "getSecurityMechanism";  //NOI18N
    
    private static final String AM_LIBERTY_DS_SECURITY_FIELD = "LIBERTY_DS_SECURITY";   //NOI18N

    private ClassLoader loader;
    
    public SecurityMechanismRetriever(String id) {
        loader = ClassLoaderManager.getDefault().getClassLoader(ServerProperties.getInstance(id));
    }
    
    
    /**
     * Returns all the non-Liberty security mechanisms.
     *
     */
    public Collection<SecurityMechanism> getAllSupportedSecurityMechanisms() {
        try {
            Class clazz = loader.loadClass(AM_PROVIDER_CONFIG_CLASS);
            Method method = clazz.getMethod(AM_GET_ALL_SUPPORT_SECURITY_MECH_METHOD);
            return convertToSecurityMechProxies((List) method.invoke(null));
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    /**
     * Returns all the non-Liberty message-level security mechanisms.
     *
     */
    public Collection<SecurityMechanism> getAllMessageLevelSecurityMechanism() {
        try {
            Class clazz = loader.loadClass(AM_PROVIDER_CONFIG_CLASS);
            Method method = clazz.getMethod(AM_GET_ALL_MESSAGE_LEVEL_SECURITY_MECH_METHOD);
            
            return convertToSecurityMechProxies((List) method.invoke(null));
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    /*
     * Returns all the WSP Liberty security mechanism.
     *
     */
    public Collection<SecurityMechanism> getAllWSPLibertySecurityMechanisms() {
        try {
            Class clazz = loader.loadClass(AM_SECURITY_MECHANISM_CLASS);
            Method method = clazz.getMethod(AM_GET_LIBERTY_SECURITY_MECHANISM_URIS_METHOD);
            List uris = (List) method.invoke(null);
            
            method = clazz.getMethod(AM_GET_SECURITY_MECHANISM_METHOD, String.class);
            List secMechs = new ArrayList();
            
            for (int i = 0; i < uris.size(); i++) {
                String uri = (String) uris.get(i);
                secMechs.add(method.invoke(null, uri));
            }
            
            return convertToSecurityMechProxies(secMechs);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    /**
     *
     *
     */
    public Collection<SecurityMechanism> getAllWSCLibertySecurityMechanisms() {
        ArrayList secMechs = new ArrayList();
        
        try {
            Class clazz = loader.loadClass(AM_SECURITY_MECHANISM_CLASS);
            Field field = clazz.getField(AM_LIBERTY_DS_SECURITY_FIELD);
            secMechs.add(field.get(null));
            
            return convertToSecurityMechProxies(secMechs);
        } catch (Exception ex) {
            throw ConfiguratorException.create(ex);
        }
    }
    
    private Collection<SecurityMechanism> convertToSecurityMechProxies(List secMechs) {
        ArrayList<SecurityMechanism> secMechProxies = new ArrayList<SecurityMechanism>();
        Iterator iter = secMechs.iterator();
        
        while (iter.hasNext()) {
            SecurityMechanism proxy = new SecurityMechanismImpl(iter.next());
            secMechProxies.add(proxy);
        }
        
        return Collections.unmodifiableCollection(secMechProxies);
    }
    
    
}
