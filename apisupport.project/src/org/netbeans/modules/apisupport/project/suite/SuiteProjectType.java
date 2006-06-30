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
