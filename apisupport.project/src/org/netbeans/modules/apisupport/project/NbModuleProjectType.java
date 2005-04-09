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

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * Factory for NetBeans module projects.
 * @author Jesse Glick
 */
public final class NbModuleProjectType implements AntBasedProjectType {
    
    private static final String TYPE = "org.netbeans.modules.apisupport.project"; // NOI18N
    static final String NAME_SHARED = "data"; // NOI18N
    static final String NAMESPACE_SHARED_OLD = "http://www.netbeans.org/ns/nb-module-project/1"; // NOI18N
    static final String NAMESPACE_SHARED_NEW = "http://www.netbeans.org/ns/nb-module-project/2"; // NOI18N
    static final String[] NAMESPACES_SHARED = {
        NAMESPACE_SHARED_OLD,
        NAMESPACE_SHARED_NEW,
    };
    private static final String NAME_PRIVATE = "data"; // NOI18N
    private static final String NAMESPACE_PRIVATE = "http://www.netbeans.org/ns/nb-module-project-private/1"; // NOI18N
    
    /** Default constructor for lookup. */
    public NbModuleProjectType() {}
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        return new NbModuleProject(helper);
    }
    
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? NAME_SHARED : NAME_PRIVATE;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? NAMESPACE_SHARED_NEW : NAMESPACE_PRIVATE;
    }
    
}
