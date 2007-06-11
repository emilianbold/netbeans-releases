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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class SecurityRoleMetadataReader extends CommonBeanReader {

    public SecurityRoleMetadataReader() {
        super(DDBinding.PROP_SECURITY_ROLE);
    }
    
    @Override
    public Map<String, Object> readDescriptor(CommonDDBean commonDD) {
        // This method is passed the root of the standard descriptor.  For ejb-jar,
        // the security-role elements are under the assembly-descriptor element, if any.
        if(commonDD instanceof EjbJar) {
            commonDD = ((EjbJar) commonDD).getSingleAssemblyDescriptor();
        }

        return super.readDescriptor(commonDD);
    }
    
    /** Maps interesting fields from security-role descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof SecurityRole []) {
            SecurityRole [] roles = (SecurityRole []) beans;
            for(SecurityRole securityRole: roles) {
                String securityRoleName = securityRole.getRoleName();
                if(Utils.notEmpty(securityRoleName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, String> securityRoleMap = new HashMap<String, String>();
                    result.put(securityRoleName, securityRoleMap);
                    securityRoleMap.put(DDBinding.PROP_NAME, securityRoleName);
                }
            }
        }
        return result;
    }
}
