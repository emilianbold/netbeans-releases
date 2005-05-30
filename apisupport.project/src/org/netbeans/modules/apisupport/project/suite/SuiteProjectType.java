/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Factory for NetBeans module suite projects.
 * @author Jesse Glick
 */
public final class SuiteProjectType implements AntBasedProjectType {
    
    public static final String TYPE = "org.netbeans.modules.apisupport.project.suite"; // NOI18N
    static final String NAME_SHARED = "data"; // NOI18N
    public static final String NAMESPACE_SHARED = "http://www.netbeans.org/ns/nb-module-suite-project/1"; // NOI18N
    private static final String NAME_PRIVATE = "data"; // NOI18N
    private static final String NAMESPACE_PRIVATE = "http://www.netbeans.org/ns/nb-module-suite-project-private/1"; // NOI18N
    
    /** Default constructor for lookup. */
    public SuiteProjectType() {}
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        return new SuiteProject(helper);
    }
    
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? NAME_SHARED : NAME_PRIVATE;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? NAMESPACE_SHARED : NAMESPACE_PRIVATE;
    }
    
}
