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

package org.netbeans.modules.java.j2seproject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Provides attributes that can be used inside scripting templates.
 * <dl><dt><code>project.license</code></dt>
 * <dd>attribute containing license name.
 * The provider reads <code>project.license</code> property from build.properties
 * and returns it as the template attribute. In case the property is not available
 * the attribute is filled with <code>"default"</code> value.</dd>
 * </dl>
 *
 * @author Jan Pokorsky
 */
public final class J2SETemplateAttributesProvider implements CreateFromTemplateAttributesProvider {
    
    private final AntProjectHelper helper;
    
    public J2SETemplateAttributesProvider(AntProjectHelper helper) {
        this.helper = helper;
    }

    public Map<String, ? extends Object> attributesFor(
            DataObject template, DataFolder target, String name) {
        
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return addLicense(null, props);
    }
    
    private static Map<String, Object> addLicense(Map<String, Object> attrs, EditableProperties props) {
        String license = props.getProperty("project.license"); // NOI18N
        if (license == null) {
            return null;
        } else {
            license = license.trim().toLowerCase();
        }
        
        if (attrs == null) {
            // a cache might be introduced if needed here
            attrs = new HashMap<String, Object>();
        }
        attrs.put("project", // NOI18N
                Collections.<String, String>singletonMap("license", license)); // NOI18N
        return attrs;
    }
}
