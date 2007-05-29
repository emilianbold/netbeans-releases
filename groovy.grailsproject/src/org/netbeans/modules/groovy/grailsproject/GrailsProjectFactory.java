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

package org.netbeans.modules.groovy.grailsproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class GrailsProjectFactory implements ProjectFactory {

    public static final String GRAILS_APP_DIR = "grails-app";

    public GrailsProjectFactory() {
    }

    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(GRAILS_APP_DIR) != null;
    }

    public Project loadProject(FileObject projectDirectory, ProjectState projectState) throws IOException {
        return isProject(projectDirectory) ? new GrailsProject(projectDirectory, projectState) : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
