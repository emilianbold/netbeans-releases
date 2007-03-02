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

package org.netbeans.modules.ant.freeform;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Freeform project type.
 * @author Jesse Glick
 */
public final class FreeformProjectType implements AntBasedProjectType {

    public static final String TYPE = "org.netbeans.modules.ant.freeform";
    public static final String NS_GENERAL_1 = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    public static final String NS_GENERAL = org.netbeans.modules.ant.freeform.spi.support.Util.NAMESPACE;
    public static final String NAME_SHARED = "general-data"; // NOI18N
    private static final String NS_GENERAL_PRIVATE = "http://www.netbeans.org/ns/freeform-project-private/1"; // NOI18N
    
    /** Default constructor for lookup. */
    public FreeformProjectType() {}
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        return new FreeformProject(helper);
    }
    
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return NAME_SHARED;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        // No private.xml defined anyway.
        return shared ? /* old! for FreeformProjectGenerator */ NS_GENERAL_1 : NS_GENERAL_PRIVATE;
    }
    
    public String getType() {
        return TYPE; // NOI18N
    }
    
}
