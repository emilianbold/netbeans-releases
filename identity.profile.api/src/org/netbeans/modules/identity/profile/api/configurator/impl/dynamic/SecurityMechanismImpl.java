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
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;

/**
 * Implementation class for the SecurityMechanism interface.
 *
 * Created on June 10, 2006, 10:48 PM
 *
 * @author ptliu
 */
class SecurityMechanismImpl implements SecurityMechanism {
    
    private static final String USERNAME_TOKEN = "UserNameToken";   //NOI18N
    
    private static final String AM_GET_URI_METHOD = "getURI";     //NOI18N
    
    private static final String AM_GET_NAME_METHOD = "getName";     //NOI18N
    
    private static final String AM_IS_TA_LOOKUP_REQUIRED_METHOD = "isTALookupRequired";     //NOI18N
    
    private static final String AM_IS_TA_REGISTRATION_REQUIRED_METHOD = "isTARegistrationRequired";     //NOI18N
    
    private static final String LIBERTY_PREFIX = "Liberty";     //NOI18N
    
    private Object proxied;
    private Class clazz;
    private String uri;
    private String name;
    private Boolean isTALookupRequired;
    private Boolean isTARegistrationRequired;
    
    
    /**
     * Creates a new instance of SecurityMechanismImpl
     */
    public SecurityMechanismImpl(Object proxied) {
        this.proxied = proxied;
        this.clazz = proxied.getClass();
    }
    
    public String getURI() {
        if (uri == null) {
            try {
                Method method = clazz.getMethod(AM_GET_URI_METHOD);
                uri = (String) method.invoke(proxied);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return uri;
    }
    
    public String getName() {
        if (name == null) {
            try {
                Method method = clazz.getMethod(AM_GET_NAME_METHOD);
                name = (String) method.invoke(proxied);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return name;
    }
    
    public boolean isTARegistrationRequired() {
        if (isTARegistrationRequired == null) {
            try {
                Method method = clazz.getMethod(AM_IS_TA_REGISTRATION_REQUIRED_METHOD);
                isTARegistrationRequired = (Boolean) method.invoke(proxied);
            } catch (Exception ex) {
                isTARegistrationRequired = Boolean.FALSE;
            }
        }
        return isTARegistrationRequired;
    }
    
    public boolean isTALookupRequired() {
        if (isTALookupRequired == null) {
            try {
                Method method = clazz.getMethod(AM_IS_TA_LOOKUP_REQUIRED_METHOD);
                isTALookupRequired = (Boolean) method.invoke(proxied);
            } catch (Exception ex) {
                isTALookupRequired = Boolean.FALSE;
            }
        }
        return isTALookupRequired;
    }
    
    public boolean isPasswordCredentialRequired() {
        return getName().equals(USERNAME_TOKEN);
    }
    
    public boolean isLiberty() {
        return getName().startsWith(LIBERTY_PREFIX);
    }
    
    public String toString() {
        return getName();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof SecurityMechanismImpl) {
            if (getURI().equals(((SecurityMechanismImpl) obj).getURI()))
                return true;
        }
        
        return false;
    }
    
    public int hashCode() {
        return getURI().hashCode();
    }
}
