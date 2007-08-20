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
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;


/**
 *
 * @author Peter Williams
 */
public class ResourceRefMetadataReader extends CommonBeanReader {

    private String parentName;
    
    public ResourceRefMetadataReader(final String parentName) {
        super(DDBinding.PROP_RESOURCE_REF);
        this.parentName = parentName;
    }
    
    /** For normalizing data structures within /ejb-jar graph.
     *    /ejb-jar -> -> /ejb-jar/enterprise-beans/session[ejb-name="xxx"]
     * (finds message-driven and entity as well)
     * 
     * TODO This mechanism will probably need optimization and caching to perform
     * for larger files.
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parentName != null && parent instanceof EjbJar) {
            parent = findEjbByName((EjbJar) parent, parentName);
        }
        return parent;
    }
    
    /** Maps interesting fields from resource-ref descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof ResourceRef []) {
            ResourceRef [] resourceRefs = (ResourceRef []) beans;
            for(ResourceRef resourceRef: resourceRefs) {
                String resourceRefName = resourceRef.getResRefName();
                if(Utils.notEmpty(resourceRefName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> resourceRefMap = new HashMap<String, Object>();
                    result.put(resourceRefName, resourceRefMap);
                    resourceRefMap.put(DDBinding.PROP_NAME, resourceRefName);
                }
            }
        }
        return result;
    }
}
