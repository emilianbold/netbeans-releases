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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    /** 
     * Returns all the non-Liberty security mechanisms.
     *
     */
    public static Collection<SecurityMechanism> getAllSupportedSecurityMechanisms() {
        try {
            ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(new ServerProperties());
            Class clazz = loader.loadClass(AM_PROVIDER_CONFIG_CLASS);
            Method method = clazz.getMethod(AM_GET_ALL_SUPPORT_SECURITY_MECH_METHOD);
            return convertToSecurityMechProxies((List) method.invoke(null));
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Returns all the non-Liberty message-level security mechanisms.
     *
     */
    public static Collection<SecurityMechanism> getAllMessageLevelSecurityMechanism() {
        try {
            ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(new ServerProperties());
            Class clazz = loader.loadClass(AM_PROVIDER_CONFIG_CLASS);
            Method method = clazz.getMethod(AM_GET_ALL_MESSAGE_LEVEL_SECURITY_MECH_METHOD);
            
            return convertToSecurityMechProxies((List) method.invoke(null));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return Collections.emptyList();
    }
    
    /*
     * Returns all the WSP Liberty security mechanism.
     *
     */
    public static Collection<SecurityMechanism> getAllWSPLibertySecurityMechanisms() {
        try {
            ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(new ServerProperties());
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
            ex.printStackTrace();
        }
        
        return Collections.emptyList();
        
    }
    
    /**
     *
     *
     */
    public static Collection<SecurityMechanism> getAllWSCLibertySecurityMechanisms() {
        ArrayList secMechs = new ArrayList();
        
        try {
            ClassLoader loader = ClassLoaderManager.getDefault().getClassLoader(new ServerProperties());
            Class clazz = loader.loadClass(AM_SECURITY_MECHANISM_CLASS);
            Field field = clazz.getField(AM_LIBERTY_DS_SECURITY_FIELD);
            secMechs.add(field.get(null));
            
            return convertToSecurityMechProxies(secMechs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return Collections.emptyList();
    }
    
    private static Collection<SecurityMechanism> convertToSecurityMechProxies(List secMechs) {
        ArrayList<SecurityMechanism> secMechProxies = new ArrayList<SecurityMechanism>();
        Iterator iter = secMechs.iterator();
        
        while (iter.hasNext()) {
            secMechProxies.add(new SecurityMechanismImpl(iter.next()));
        }
        
        return Collections.unmodifiableCollection(secMechProxies);
    }
    
    
}
