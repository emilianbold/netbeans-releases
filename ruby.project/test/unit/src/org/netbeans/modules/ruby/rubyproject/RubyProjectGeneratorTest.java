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
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.openide.filesystems.FileObject;

public class RubyProjectGeneratorTest extends RubyProjectTestBase {

    public RubyProjectGeneratorTest(String testName) {
        super(testName);
    }

    public void testCreateProject() throws Exception {
        registerLayer();
        String appName = "RubyApp";
        String name = "script.rb";
        String expectedName = "script.rb";
        for (int i = 0; i < 2; i++) {
            File projectDir = new File(getWorkDir(), appName);
            RakeProjectHelper helper = RubyProjectGenerator.createProject(projectDir, "Ruby Application", name, RubyPlatformManager.getDefaultPlatform());
            FileObject prjDirFO = helper.getProjectDirectory();
            assertNotNull("project created", prjDirFO);

            assertNotNull("has Rakefile", prjDirFO.getFileObject("Rakefile"));
            FileObject libDirFO = prjDirFO.getFileObject("lib");
            assertNotNull("has lib", libDirFO);
            assertNotNull("has script.rb", libDirFO.getFileObject(expectedName));
            assertNull("does not have Rakefile in lib", libDirFO.getFileObject("Rakefile"));

            assertNotNull("has README", prjDirFO.getFileObject("README"));
            assertNotNull("has LICENSE", prjDirFO.getFileObject("LICENSE"));

            RubyBaseProject p = (RubyBaseProject) ProjectManager.getDefault().findProject(prjDirFO);
            assertNotNull("has project", p);
            Set<RakeTask> tasks = RakeSupport.getRakeTaskTree(p);
            assertSame("correct Rakefile", 11, tasks.size());

            // test main class without extension in the next run
            name = "another_script";
            expectedName = "another_script.rb";
            appName = "RubyApp1";
        }
    }

    public void testGeneratedSourceRoots() throws Exception {
        RubyProject project = createTestProject();
        FileObject projectDir = project.getProjectDirectory();
        FileObject[] roots = project.getSourceRoots().getRoots();
        FileObject[] testRoots = project.getTestSourceRoots().getRoots();

        assertEquals("one source root", 1, roots.length);
        assertEquals("has lib", roots[0], projectDir.getFileObject("lib"));

        assertEquals("two test roots", 2, testRoots.length);
        assertEquals("has test", testRoots[0], projectDir.getFileObject("test"));
        assertEquals("has spec", testRoots[1], projectDir.getFileObject("spec"));
    }

    public void testGeneratedRakeFile() throws Exception {
        registerLayer();
        RubyProject project = createTestProject();
        Set<RakeTask> tasks = RakeSupport.getRakeTaskTree(project);
        assertSame("correct Rakefile", 11, tasks.size());
        assertNotNull("has 'spec' task", RakeSupport.getRakeTask(project, "spec"));
        assertNotNull("has 'test' task", RakeSupport.getRakeTask(project, "test"));
    }
}
