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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test functionality of NbModuleProject.
 * @author Jesse Glick
 */
public class NbModuleProjectTest extends TestBase {
    
    public NbModuleProjectTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        FileObject dir = nbRoot().getFileObject("java.project");
        assertNotNull("have java.project checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        javaProjectProject = (NbModuleProject)p;
    }

    /** #56457 */
    // XXX no longer have editor/libsrc test case: testExternalSourceRoots

    public void testExternalModules() throws Exception {
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        PropertyEvaluator eval = actionProject.evaluator();
        String nbdestdir = eval.getProperty("netbeans.dest.dir");
        assertNotNull("defined netbeans.dest.dir", nbdestdir);
        assertEquals("right netbeans.dest.dir", file("nbbuild/netbeans"), PropertyUtils.resolveFile(FileUtil.toFile(action), nbdestdir));
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        eval = dummyProject.evaluator();
        assertEquals("right netbeans.dest.dir", resolveEEPFile("suite3/nbplatform"), PropertyUtils.resolveFile(FileUtil.toFile(dummy), eval.getProperty("netbeans.dest.dir")));
        // XXX more...
    }

    public void testGetType() throws Exception {
        assertEquals(NbModuleProvider.NETBEANS_ORG, Util.getModuleType(javaProjectProject));
        FileObject suite1 = resolveEEP("suite1");
        FileObject action = suite1.getFileObject("action-project");
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(action);
        assertEquals(NbModuleProvider.SUITE_COMPONENT, Util.getModuleType(actionProject));
        FileObject suite3 = resolveEEP("suite3");
        FileObject dummy = suite3.getFileObject("dummy-project");
        NbModuleProject dummyProject = (NbModuleProject) ProjectManager.getDefault().findProject(dummy);
        assertEquals(NbModuleProvider.STANDALONE, Util.getModuleType(dummyProject));
    }

    public void testSupportsJavadoc() throws Exception {
        assertTrue(javaProjectProject.supportsJavadoc());
        FileObject dir = nbRoot().getFileObject("beans");
        assertNotNull("have beans checked out", dir);
        Project p = ProjectManager.getDefault().findProject(dir);
        NbModuleProject beansProject = (NbModuleProject) p;
        assertFalse(beansProject.supportsJavadoc());
    }

    public void testGetNbrootFile() throws Exception {
        NbModuleProject actionProject = (NbModuleProject) ProjectManager.getDefault().findProject(resolveEEP("suite1/action-project"));
        assertEquals(file("whatever"), actionProject.getNbrootFile("whatever"));
    }

    public void testThatModuleWithOverriddenSrcDirPropertyDoesNotThrowNPE() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
        FileObject srcFO = prjFO.getFileObject("src");
        FileUtil.moveFile(srcFO, prjFO, "src2");
        ProjectManager.getDefault().findProject(prjFO);
    }

//    XXX: failing test, fix or delete
//    public void testGenericSourceGroupForExternalUnitTests() throws Exception {
//        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module1");
//        FileUtil.createData(prjFO, "../myunitsrc/a/b/c/Dummy.java");
//        FileObject propsFO = FileUtil.createData(prjFO, AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        EditableProperties ep = Util.loadProperties(propsFO);
//        ep.setProperty("test.unit.src.dir", "../myunitsrc");
//        Util.storeProperties(propsFO, ep);
//        Project module = ProjectManager.getDefault().findProject(prjFO);
//        Sources sources = ProjectUtils.getSources(module);
//        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
//        assertEquals("two generic source group", 2, sourceGroups.length); // prjFolder and unitFolder
//    }

}
