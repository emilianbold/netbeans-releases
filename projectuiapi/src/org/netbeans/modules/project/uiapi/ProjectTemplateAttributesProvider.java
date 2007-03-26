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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates. It delegates
 * attributes query to providers registered in project lookups.
 *
 * @author Jan Pokorsky
 */
public final class ProjectTemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    private static final String ATTR_PROJECT = "project"; // NOI18N
    private static final String ATTR_LICENSE = "license"; // NOI18N

    public Map<String, ? extends Object> attributesFor(
            DataObject template, DataFolder target, String name) {
        
        Project prj = FileOwnerQuery.getOwner(target.getPrimaryFile());
        if (prj != null) {
            Map<String, Object> all = null;
            for (CreateFromTemplateAttributesProvider attrs : prj.getLookup().lookupAll(CreateFromTemplateAttributesProvider.class)) {
                Map<String, ? extends Object> m = attrs.attributesFor(template, target, name);
                if (m != null) {
                    if (all == null) {
                        all = new HashMap<String, Object>();
                    }
                    all.putAll(m);
                }
            }
            return checkProjectLicense(all);
        }
        
        return null;
    }
    
    private static Map<String, ? extends Object> checkProjectLicense(Map<String, Object> m) {
        Object prjAttrObj = m != null? m.get(ATTR_PROJECT): null;
        Map<String, Object> newMap = null;
        if (prjAttrObj instanceof Map) {
            Map prjAttrs = (Map) prjAttrObj;
            if (prjAttrs.get(ATTR_LICENSE) != null) {
                return m;
            }
        } else if (prjAttrObj != null) {
            return m;
        } else {
            newMap = new HashMap<String, Object>();
        }
        newMap.put(ATTR_PROJECT, Collections.<String, String>singletonMap(ATTR_LICENSE, "default")); // NOI18N
        return newMap;
    }
    
}
