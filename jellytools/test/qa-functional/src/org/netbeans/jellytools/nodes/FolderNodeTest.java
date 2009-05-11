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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.testutils.JavaNodeUtils;

/** Test of org.netbeans.jellytools.nodes.FolderNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class FolderNodeTest extends org.netbeans.jellytools.JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FolderNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        /*
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        // Cannot test because folder at different view has different items. 
        // suite.addTest(new FolderNodeTest("testVerifyPopup"));
        // Explore from here is used on web services node but to create such
        // a node you need application server installed. For now we skip this test.
        //suite.addTest(new FolderNodeTest("testExploreFromHere"));
        suite.addTest(new FolderNodeTest("testFind"));
        suite.addTest(new FolderNodeTest("testCompile"));
        suite.addTest(new FolderNodeTest("testCut"));
        suite.addTest(new FolderNodeTest("testCopy"));
        suite.addTest(new FolderNodeTest("testPaste"));
        suite.addTest(new FolderNodeTest("testDelete"));
        suite.addTest(new FolderNodeTest("testRename"));
        suite.addTest(new FolderNodeTest("testProperties"));
        suite.addTest(new FolderNodeTest("testNewFile"));
        return suite;
         */
        return createModuleTest(FolderNodeTest.class, 
        "testFind",
        "testCompile",
        "testCut",
        "testCopy",
        "testPaste",
        "testDelete",
        "testRename",
        "testProperties",
        "testNewFile");
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test case setup. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
    }
    
    /** Test verifyPopup method.
     * Currently folder at differetn view has different items. */
    public void testVerifyPopup() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.verifyPopup();
    }
    
    /** Test find method. */
    public void testFind() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.find();
        new FindInFilesOperator().close();
    }
    
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "SampleWebProject";  //NOI18N
    // name of sample web service
    private static final String SAMPLE_WEB_SERVICE_NAME = "SampleWebService";  //NOI18N

    /** Test exploreFromHere. */
    public void testExploreFromHere() throws Exception {
        // create new web application project
        
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Web"
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        npwo.selectCategory(webLabel);
        // "Web Application"
        String webApplicationLabel = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        npwo.selectProject(webApplicationLabel);
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_WEB_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.finish();
        // wait project appear in projects view
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // wait index.jsp is opened in editor
        EditorOperator editor = new EditorOperator("index.jsp"); // NOI18N
        // wait classpath scanning finished
        try {
            Class.forName("org.netbeans.api.java.source.SourceUtils", true, Thread.currentThread().getContextClassLoader()).
                    getMethod("waitScanFinished").invoke(null);
        } catch (ClassNotFoundException x) {
            System.err.println("Warning: org.netbeans.api.java.source.SourceUtils could not be found, will not wait for scan to finish");
        }
        
        // create a web service

        // "Web Services"
        String webServicesLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices");
        // "Web Service"
        String webServiceLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebService.java");
        NewFileWizardOperator.invoke(projectRootNode, webServicesLabel, webServiceLabel);
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setPackage("dummy"); // NOI18N
        nameStepOper.setObjectName(SAMPLE_WEB_SERVICE_NAME);
        nameStepOper.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator(SAMPLE_WEB_SERVICE_NAME).closeAllDocuments();

        // "Web Services"
        String webServicesNodeLabel = Bundle.getString(
                "org.netbeans.modules.websvc.core.Bundle", "LBL_WebServices");
        FolderNode wsNode = new FolderNode(projectRootNode, webServicesNodeLabel+"|"+SAMPLE_WEB_SERVICE_NAME);
        wsNode.exploreFromHere();
        new TopComponentOperator(SAMPLE_WEB_SERVICE_NAME).close();  // NOI18N
    }

    /** Test compile. */
    public void testCompile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        folderNode.compile();
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test paste. */
    public void testPaste() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        FolderNode sample2Node = new FolderNode(sample1Node, "sample2"); // NOI18N
        sample2Node.copy();
        sample1Node.paste();
        FolderNode sample21Node = new FolderNode(sample1Node, "sample2_1");  // NOI18N
        JavaNodeUtils.performSafeDelete(sample21Node);
    }
    
    /** Test cut. */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.cut();
        JavaNodeUtils.testClipboard(clipboard1);
    }
    
    /** Test copy. */
    public void testCopy() {
        final Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.copy();
        JavaNodeUtils.testClipboard(clipboard1);
    }
    
    /** Test delete. */
    public void testDelete() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.delete();
        JavaNodeUtils.closeSafeDeleteDialog();
    }
    
    /** Test rename */
    public void testRename() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "nbproject"); // NOI18N
        sample1Node.rename();
        JavaNodeUtils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        FolderNode sample1Node = new FolderNode(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1"); // NOI18N
        sample1Node.properties();
        JavaNodeUtils.closeProperties("sample1"); //NOI18N
    }
    
    /** Test newFile */
    public void testNewFile() {
        FolderNode folderNode = new FolderNode(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        folderNode.newFile();
        new NewFileWizardOperator().close();
    }
}
