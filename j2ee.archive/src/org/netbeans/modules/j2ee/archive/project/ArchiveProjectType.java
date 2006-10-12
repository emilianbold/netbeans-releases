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

package org.netbeans.modules.j2ee.archive.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;

public class ArchiveProjectType implements AntBasedProjectType {
    
    private static final String PROJECT_CONFIGURATION_NAME ="data";            //NOI18N
    public static final String PROJECT_CONFIGURATION_NS ="http://www.netbeans.org/ns/archive-project/1";                     //NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data";            //NOI18N
    private static final String PRIVATE_CONFIGURATION_NS = "http://www.netbeans.org/ns/archive-project-private/1";             //NOI18N

    public static final String TYPE = "org.netbeans.modules.j2ee.archive.project";                        //NOI18N

    /** Creates a new instance of DeployableArchive */
    public ArchiveProjectType() {
    }

    public String getType() {
        return TYPE;
    }

    public Project createProject(AntProjectHelper helper) throws IOException {
        return new ArchiveProject(helper);
    }

    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NS : PRIVATE_CONFIGURATION_NS;
    }
}
