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
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class EjbRefMetadataReader extends CommonBeanReader {

    public EjbRefMetadataReader() {
        super(DDBinding.PROP_EJB_REF);
    }
    
    /** Maps interesting fields from ejb-ref descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof EjbRef []) {
            EjbRef [] ejbRefs = (EjbRef []) beans;
            for(EjbRef ejbRef: ejbRefs) {
                String ejbRefName = ejbRef.getEjbRefName();
                if(Utils.notEmpty(ejbRefName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, String> ejbRefMap = new HashMap<String, String>();
                    result.put(ejbRefName, ejbRefMap);
                    ejbRefMap.put(DDBinding.PROP_NAME, ejbRefName);
                }
            }
        }
        return result;
    }
}
