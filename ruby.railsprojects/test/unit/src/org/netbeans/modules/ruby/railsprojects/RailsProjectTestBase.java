/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.railsprojects.database.RailsAdapterFactory;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConfiguration;
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
        RailsDatabaseConfiguration dbConf = RailsAdapterFactory.getDefaultAdapter();
        RailsProjectCreateData data = new RailsProjectCreateData(RubyPlatformManager.getDefaultPlatform(), prjDirF, projectName, false, dbConf, false, "WEBRICK", null);
        touch(prjDirF, "Rakefile");
        RailsProjectGenerator.createProject(data);
        RubyTestBase.createFiles(prjDirF, paths);
        RailsProject project = (RailsProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assertNotNull(project);
        return project;
    }

    protected RailsProject createTestPlainProject() throws Exception {
        return createTestProjectFromDataFile("testfiles/plain_rails.txt");
    }
    
    private RailsProject createTestProjectFromDataFile(final String dataFile) throws Exception {
        RailsProject project = createTestProject("RubyProject");
        createFilesFromDesc(project.getProjectDirectory(), dataFile);
        return project;
    }

}
