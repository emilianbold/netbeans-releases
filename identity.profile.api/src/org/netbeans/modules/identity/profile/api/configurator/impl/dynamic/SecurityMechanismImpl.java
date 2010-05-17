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
        return getName().startsWith(USERNAME_TOKEN);
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
