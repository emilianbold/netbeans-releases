/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.apisupport.project.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.api.ManifestManager.PackageExport;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.UnitTestLibrariesNode.RemoveDependencyAction;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

/**
 * @author Tomas Musil
 */
public class UnitTestLibrariesNodeTest extends TestBase {
    private static final String DEP_CNB = "org.openide.filesystems";
    private static int nc = 0;             //says if junit or nbjunit is present
    private NbModuleProject p;
    private UnitTestLibrariesNode libsNode;
    public UnitTestLibrariesNodeTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
        p = generateStandaloneModule("module");
        libsNode = new UnitTestLibrariesNode(TestModuleDependency.UNIT, p);
    }


//    XXX: failing test, fix or delete
    //this tests if node draws subnodes    
//    public void testLibrariesNodeDrawingDeps() throws Exception {
//        Lookup.getDefault().lookup(ModuleInfo.class);
//        //initial check
//        NbModuleProject p = generateStandaloneModule("module");
//
//        Node libs = new UnitTestLibrariesNode(TestModuleDependency.UNIT, p);
//        assertNotNull("have the Libraries node", libs);
//        assertEquals("nc node", nc, libs.getChildren().getNodes(true).length);
//
//        //add tests dependecy
//        ProjectXMLManager pxm = new ProjectXMLManager(p);
//        addTestDependency(p);
//        ModuleList ml = p.getModuleList();
//        Set unitDeps = pxm.getTestDependencies(ml).get(TestModuleDependency.UNIT);
//        assertNotNull("Have unit deps now", unitDeps);
//        assertEquals("one dep now", 1,  unitDeps.size());
//        assertEquals("nc+1 nodes now", nc+1, libs.getChildren().getNodes().length);
//
//        //remove test dependency
//        pxm.removeTestDependency(TestModuleDependency.UNIT, DEP_CNB);
//        ProjectManager.getDefault().saveProject(p);
//        assertEquals("nc nodes now", nc, libs.getChildren().getNodes().length);
//    }
    
    public void testActions() throws Exception{
        assertNotNull("have the Libraries node", libsNode);
        //test removedep action
        addTestDependency(p);
        String depName = p.getModuleList().getEntry(DEP_CNB).getCodeNameBase();
        forceChildrenUpdate(libsNode);
        Node depNode = libsNode.getChildren().findChild(depName);
        assertNotNull("have a node with dependency", depNode);
        Action[] act = depNode.getActions(false);
        assertEquals("have three actions", 3, act.length);
        RemoveDependencyAction removeAct = (RemoveDependencyAction) act[2];
        assertEquals("nc+1 nodes now", nc+1, libsNode.getChildren().getNodes().length);
        removeAct.performAction(new Node[] {depNode});
        forceChildrenUpdate(libsNode);
        assertEquals("nc nodes now, dep removed", nc, libsNode.getChildren().getNodes().length);
    }

    /*
     * Simulates creation of ActionFilterNode without DataObject, see
     * UnitTestLibrariesNode.LibrariesChildren.createNodes and ActionFilterNode.create.
     */
    private final class MockModuleEntry implements ModuleEntry {
        private File jar;

        public String getNetBeansOrgPath() {
            return null;
        }

        public File getSourceLocation() {
            return null;
        }

        public String getCodeNameBase() {
            return "org.test.module";
        }

        public File getClusterDirectory() {
            return null;
        }

        public File getJarLocation() {
            try {
                if (jar == null) {
                    Logger.getLogger(UnitTestLibrariesNodeTest.class.getName())
                            .log(Level.INFO, "getJarLocation, creating jar");
                    jar = new File(getWorkDir(), "org-module-test.jar");
                    JarOutputStream jos = new JarOutputStream(FileUtil.createData(jar).getOutputStream(), new Manifest());
                    jos.flush();
                    jos.close();
                    assertTrue(jar.exists());
                    Logger.getLogger(UnitTestLibrariesNodeTest.class.getName())
                            .log(Level.INFO, "getJarLocation, jar created");
                }
                return jar;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getClassPathExtensions() {
            return null;
        }

        public String getReleaseVersion() {
            return null;
        }

        public String getSpecificationVersion() {
            return null;
        }

        public String[] getProvidedTokens() {
            return null;
        }

        public String getLocalizedName() {
            if (jar != null && jar.exists()) {
                Logger.getLogger(UnitTestLibrariesNodeTest.class.getName())
                        .log(Level.INFO, "getLocalizedName, deleting jar");
                try {
                    Thread.sleep(500);  // must wait until jar closes
                    // simulate deletion of module jar
                    FileUtil.toFileObject(jar).delete();
                    jar = null;
                    Logger.getLogger(UnitTestLibrariesNodeTest.class.getName())
                            .log(Level.INFO, "getLocalizedName, successfully deleted");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex2) {
                    throw new RuntimeException(ex2);
                }
            }
            return "Test Module";
        }

        public String getCategory() {
            return null;
        }

        public String getLongDescription() {
            return null;
        }

        public String getShortDescription() {
            return null;
        }

        public PackageExport[] getPublicPackages() {
            return null;
        }

        public URL getJavadoc(NbPlatform platform) {
            return null;
        }

        public Set<String> getAllPackageNames() {
            return null;
        }

        public boolean isDeclaredAsFriend(String cnb) {
            return false;
        }

        public Set<String> getPublicClassNames() {
            return null;
        }

        public boolean isDeprecated() {
            return false;
        }

        public String[] getRunDependencies() {
            return null;
        }

        public int compareTo(ModuleEntry o) {
            return 0;
        }

    }
    public void testNoDataObjectInLookup169568() throws Exception {
        TestModuleDependency tmd = new TestModuleDependency(new MockModuleEntry(), true, true, true);
        Children ch = libsNode.getChildren();
        Method m = ch.getClass().getDeclaredMethod("createNodes", Object.class);
        m.setAccessible(true);
        Node[] nodes = (Node[]) m.invoke(ch, tmd);
        assertNotNull(nodes);
        assertTrue(nodes.length == 1);
    }

    //TODO add more tests, try to invoke all actions on nodes, etc
    
    private void addTestDependency(NbModuleProject project) throws Exception{
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        ModuleList ml = project.getModuleList();
        ModuleEntry me = ml.getEntry(DEP_CNB);
        assertNotNull("me exist", me);
        TestModuleDependency tmd = new TestModuleDependency(me, true, true, true);
        pxm.addTestDependency(TestModuleDependency.UNIT, tmd);
        ProjectManager.getDefault().saveProject(project);
    }

    private void forceChildrenUpdate(Node node) {
        node.getChildren().getNodesCount(); // so that refreshKeys() gets called
        waitForNodesUpdate();
        waitForNodesUpdate();
    }
    
}
