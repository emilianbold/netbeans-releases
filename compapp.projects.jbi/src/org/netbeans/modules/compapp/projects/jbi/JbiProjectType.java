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

package org.netbeans.modules.compapp.projects.jbi;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.openide.util.NbBundle;

import java.io.IOException;


/**
 * Factory for EJB Module projects
 *
 * @author Chris Webster
 */
public final class JbiProjectType implements AntBasedProjectType {
    /**
     * DOCUMENT ME!
     */
    public static final String TYPE = "org.netbeans.modules.compapp.projects.jbi"; // NOI18N
    private static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-jbi/1"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-jbi-private/1"; // NOI18N

    /**
     * Creates a new JbiProjectType object.
     */
    public JbiProjectType() {
        int i = 0;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getType() {
        return TYPE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param helper DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public Project createProject(AntProjectHelper helper)
        throws IOException {
        if (null == helper) {
            throw new IllegalArgumentException(NbBundle.getMessage(JbiProjectType.class, "MSG_helper")); // NOI18N
        }

        return new JbiProject(helper, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param shared DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }

    /**
     * DOCUMENT ME!
     *
     * @param shared DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }
}
