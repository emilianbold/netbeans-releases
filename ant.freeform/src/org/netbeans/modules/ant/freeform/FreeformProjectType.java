/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    public static final String NS_GENERAL = "http://www.netbeans.org/ns/freeform-project/1"; // NOI18N
    private static final String NS_GENERAL_PRIVATE = "http://www.netbeans.org/ns/freeform-project-private/1"; // NOI18N
    
    /** Default constructor for lookup. */
    public FreeformProjectType() {}
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        return new FreeformProject(helper);
    }
    
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return "general-data"; // NOI18N
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        // No private.xml defined anyway.
        return shared ? NS_GENERAL : NS_GENERAL_PRIVATE;
    }
    
    public String getType() {
        return TYPE; // NOI18N
    }
    
}
