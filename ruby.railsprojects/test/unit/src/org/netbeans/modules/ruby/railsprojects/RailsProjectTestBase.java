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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.RubyTestBase;
import org.openide.filesystems.FileUtil;

public class RailsProjectTestBase extends RubyTestBase {

    public RailsProjectTestBase(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    protected RailsProject createTestProject() throws Exception {
        return createTestProject("RubyProject_" + getName());
    }
    
    protected RailsProject createTestProject(String projectName, String... paths) throws Exception {
        File prjDirF = new File(getWorkDir(), projectName);
        RailsProjectGenerator.createProject(prjDirF, projectName, false, null, false, false);
        RubyTestBase.createFiles(prjDirF, paths);
        RailsProject project = (RailsProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assertNotNull(project);
        return project;
    }

}
