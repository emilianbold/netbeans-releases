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

package org.netbeans.modules.apisupport.project.queries;

import java.util.Collections;
import java.util.Map;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Specifies license header for module and suite projects.
 */
public class TemplateAttributesProvider implements CreateFromTemplateAttributesProvider {

    private final AntProjectHelper helper;
    private final boolean netBeansOrg;

    public TemplateAttributesProvider(AntProjectHelper helper, boolean netBeansOrg) {
        this.helper = helper;
        this.netBeansOrg = netBeansOrg;
    }

    public Map<String,?> attributesFor(DataObject template, DataFolder target, String name) {
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String license = props.getProperty("project.license"); // NOI18N
        if (license == null && netBeansOrg) {
            license = "cddl"; // NOI18N
        }
        if (license == null) {
            return null;
        } else {
            return Collections.singletonMap("project", Collections.singletonMap("license", license)); // NOI18N
        }
    }

}
