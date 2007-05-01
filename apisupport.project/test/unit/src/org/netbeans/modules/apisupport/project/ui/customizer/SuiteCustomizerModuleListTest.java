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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/** Checks the behaviour of enabled module list.
 *
 * @author Jaroslav Tulach
 */
public class SuiteCustomizerModuleListTest extends TestBase {

    private FileObject suiteRepoFO;
    private SuiteProject suite1Prj;
    private SuiteProperties suite1Props;
    private FileObject suite1FO;
    
    private SuiteCustomizerLibraries customizer;
    
    public SuiteCustomizerModuleListTest(String testName) {
        super(testName);
    }

    @Override
    public boolean canRun() {
        // On Windows XP, under JDK 6 (but, oddly, not 5), get apparent path length limit violations:
        // java.lang.AssertionError: E:\space\test4u\builds\bindist_netbeans_Dev_daily_latest\ unit\apisupport1\org-netbeans-modules-apisupport-project\work\sys\data\example-external-projects\.\suite1\support\lib-project\test\ unit\src\org\netbeans\examples\modules\lib\LibClassTest.java
        //         at org.netbeans.modules.apisupport.project.TestBase.doCopy(TestBase.java:272)
        //         at org.netbeans.modules.apisupport.project.TestBase.doCopy(TestBase.java:269)
        //         [....]
        // See e.g.: http://beetle.czech.sun.com/automatedtests/xtest/netbeans_dev/200704301800/qa-unit_stable/qa-t4u-xp2_1/testrun_070501-125959/testbag_111/htmlresults/suites/TEST-org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizerModuleListTest.html
        return super.canRun() && !Utilities.isWindows();
    }

    protected void setUp() throws Exception {
        super.setUp();
        suiteRepoFO = FileUtil.toFileObject(copyFolder(resolveEEPFile(".")));
        suite1FO = suiteRepoFO.getFileObject("suite1");
        suite1Prj = (SuiteProject) ProjectManager.getDefault().findProject(suite1FO);
        this.suite1Props = new SuiteProperties(suite1Prj, suite1Prj.getHelper(),
                suite1Prj.getEvaluator(), SuiteUtils.getSubProjects(suite1Prj));
        
        customizer = new SuiteCustomizerLibraries(this.suite1Props, ProjectCustomizer.Category.create("x", "xx", null));
    }

    public void testDisableCluster() throws Exception {
        enableAllClusters(false);
        doDisableCluster(0, true);
    }
    
    public void testDisableCluster2() throws Exception {
        enableAllClusters(false);
        doDisableCluster(1, true);
    }
    
    public void testDisableTwoClusters() throws Exception {
        enableAllClusters(false);
        
        String c1 = doDisableCluster(1, true);
        String c2 = doDisableCluster(2, false);
        Set<String> c = new HashSet<String>();
        c.add(c1);
        c.add(c2);
        
        String[] xyz = suite1Props.getEnabledClusters();
        //assertEquals("Two clusters disabled", ???, xyz.length);
        
        Set<String> real = new HashSet<String>(Arrays.asList(xyz));
        assertFalse(real.containsAll(c));
    }
    
    private String doDisableCluster(int index, boolean doCheck) throws Exception {
        Node n = customizer.getExplorerManager().getRootContext();
        Node[] clusters = n.getChildren().getNodes();
        if (clusters.length <= index) {
            fail ("Wrong, there should be some clusters. at least: " + index + " and was: " + clusters.length);
        }
        Node[] modules = clusters[index].getChildren().getNodes();
        if (modules.length == 0) {
            fail("Expected more modules for cluster: " + clusters[index]);
        }

        setNodeEnabled(clusters[index], false);
        assertEquals("No modules in disabled clusters", 
                clusters[index].getChildren().getNodes().length, modules.length);
        
        customizer.store();
        suite1Props.storeProperties();
        
        if (doCheck) {
            String[] xyz = suite1Props.getEnabledClusters();
            //assertEquals("One cluster is disabled", ???, xyz.length);
            assertFalse("It's name is name of the node", Arrays.asList(xyz).contains(clusters[index].getName()));
        }
        
        return clusters[index].getName();
    }
    
    public void testDisableModule() throws Exception {
        enableAllClusters(true);
        
        Node n = customizer.getExplorerManager().getRootContext();
        Node[] clusters = n.getChildren().getNodes();
        if (clusters.length == 0) {
            fail("Should be at least one cluster");
        }
        Node[] modules = clusters[0].getChildren().getNodes();
        if (modules.length == 0) {
            fail("Expected at least one module in cluster: " + clusters[0]);
        }

        setNodeEnabled(modules[0], false);
        assertNodeEnabled(modules[0], Boolean.FALSE);
        
        customizer.store();
        suite1Props.storeProperties();
                
        String[] xyz = suite1Props.getDisabledModules();
        assertEquals("One module is disabled", 1, xyz.length);
        assertEquals("It's name is name of the node", modules[0].getName(), xyz[0]);
    }
    
    private static void assertNodeEnabled(Node n, Boolean value) throws Exception {
        for (Node.PropertySet ps : n.getPropertySets()) {
            for (Node.Property<?> prop : ps.getProperties()) {
                if (prop.getName().equals("enabled")) {
                    Object o = prop.getValue();
                    assertEquals("Node is correctly enabled/disabled: " + n, value, o);
                    return;
                }
            }
        }
        fail("No enabled property found: " + n);
    }
    private static void setNodeEnabled(Node n, boolean value) throws Exception {
        for (Node.PropertySet ps : n.getPropertySets()) {
            for (Node.Property<?> prop : ps.getProperties()) {
                if (prop.getName().equals("enabled")) {
                    @SuppressWarnings("unchecked") // value type is Boolean.TYPE, not Boolean.class, so Class.<T>cast will not help
                    Node.Property<Boolean> _prop = (Node.Property<Boolean>) prop;
                    _prop.setValue(value);
                    return;
                }
            }
        }
        fail("No enabled property found: " + n);
    }

    private void enableAllClusters(boolean enableModulesAsWell) throws Exception {
        Node n = customizer.getExplorerManager().getRootContext();
        for (Node cluster : n.getChildren().getNodes()) {
            setNodeEnabled(cluster, true);
            if (enableModulesAsWell) {
                for (Node module : cluster.getChildren().getNodes()) {
                    setNodeEnabled(module, true);
                }
            }
        }
    }
}
