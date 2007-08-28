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
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tor Norbye
 */
public abstract class RubyProjectTestBase extends RubyTestBase {

    public RubyProjectTestBase(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    protected Project getTestProject(String path) {
        FileObject fo = getTestFile(path);
        Project p = FileOwnerQuery.getOwner(fo);
        assertNotNull(p);

        return p;
    }
    
    protected RubyProject getRubyProject(String path) {
        Project p = getTestProject(path);
        assertNotNull(p);
        assertTrue(p instanceof RubyProject);
        
        return (RubyProject)p;
    }
    
    protected RubyProject createTestProject(String projectName, String... paths) throws Exception {
        File prjDirF = new File(getWorkDir(), projectName);
        RubyProjectGenerator.createProject(prjDirF, projectName, null, null);
        createFiles(prjDirF, paths);
        RubyProject project = (RubyProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assertNotNull(project);
        return project;
    }

    protected RubyProject createTestProject() throws Exception {
        return createTestProject("RubyProject_" + getName());
    }

}
