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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

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

    @Override
    protected boolean runInEQ() {
        return false;
    }

    protected RubyProject getRubyProject(String relativePath) throws Exception {
        FileObject projectDir = getTestFile(relativePath);
        assertNotNull(projectDir);
        Project project = ProjectManager.getDefault().findProject(projectDir);
        assertNotNull(project);
        return (RubyProject) project;
    }

    protected RubyProject createTestProject(String projectName, String... paths) throws Exception {
        File prjDirF = new File(getWorkDir(), projectName);
        RubyPlatform plaf = RubyPlatformManager.getDefaultPlatform();
        RubyProjectGenerator.createProject(prjDirF, projectName, null, plaf);
        createFiles(prjDirF, paths);
        RubyProject project = (RubyProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        assertNotNull(project);
        return project;
    }

    protected RubyProject createTestProject(final boolean open) throws Exception {
        RubyProject project = createTestProject("RubyProject_" + getName());
        if (open) {
            project.open();
        }
        return project;
        
    }
    protected RubyProject createTestProject() throws Exception {
        return createTestProject(false);
    }

    /**
     * <strong>Note:</strong> Copy-pasted from APISupport
     * <p>
     * Convenience method for loading {@link EditableProperties} from a {@link
     * FileObject}. New items will alphabetized by key.
     *
     * @param propsFO file representing properties file
     * @exception FileNotFoundException if the file represented by the given
     *            FileObject does not exists, is a folder rather than a regular
     *            file or is invalid. i.e. as it is thrown by {@link
     *            FileObject#getInputStream()}.
     */
    public static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream propsIS = propsFO.getInputStream();
        EditableProperties props = new EditableProperties(true);
        try {
            props.load(propsIS);
        } finally {
            propsIS.close();
        }
        return props;
    }

    /**
     * <strong>Note:</strong> Copy-pasted from APISupport
     * <p>
     * Convenience method for storing {@link EditableProperties} into a {@link
     * FileObject}.
     *
     * @param propsFO file representing where properties will be stored
     * @param props properties to be stored
     * @exception IOException if properties cannot be written to the file
     */
    public static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    protected void dumpRakefile(final String rakefileContent, final RubyProject project) throws IOException {
        FileObject script = FileUtil.createData(project.getProjectDirectory(), "Rakefile");
        PrintWriter pw = new PrintWriter(script.getOutputStream());
        try {
            pw.print(rakefileContent);
        } finally {
            pw.close();
        }
    }

}
