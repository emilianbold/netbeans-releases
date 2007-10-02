/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
