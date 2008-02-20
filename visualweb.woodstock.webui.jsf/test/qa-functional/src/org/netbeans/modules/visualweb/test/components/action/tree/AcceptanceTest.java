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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package  org.netbeans.modules.visualweb.test.components.action.tree;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.io.IOException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;

import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;


/**
 * @author Sherry Zhou (sherry.zhou@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    public String _bundle = "org.netbeans.modules.visualweb.test.components.Component";
    public String _projectServer = Bundle.getStringTrimmed(_bundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_bundle,"close");
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");
    public String _projectName = "TreeAcceptanceTest";
    public String dataDir=ComponentUtils.getDataDir() +  "action" + File.separator;
    String pdfFile = dataDir + "catalog.pdf";
    String imageFile = dataDir + "orchid1.JPG";
    public String _requestPrefix = "";
    public static int xTree=50;
    public static int yTree=50;
    public static int xTreeNode=70;
    public static int yTreeNode=70;
    public static int xLabel=100;
    public static int yLabel=50;
    
    public String _page1 = "Page1";
    public String _page2 = "Page2";
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
        
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ProjectNavigatorOperator prjNav;
    public static PaletteContainerOperator palette;
    public static ServerNavigatorOperator explorer;
    
    
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddTree"));
        suite.addTest(new AcceptanceTest("testConfigureTreeNodes"));
        suite.addTest(new AcceptanceTest("testAddSecondPage"));
        suite.addTest(new AcceptanceTest("testCreatePageNavigation"));
        suite.addTest(new AcceptanceTest("testDeploy"));
        suite.addTest(new AcceptanceTest("testCloseProject"));
        suite.addTest(new AcceptanceTest("testUndeploy"));
        suite.addTest(new AcceptanceTest("testCheckIDELog"));
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
        System.out.println("########  "+getName()+" Finished #######");
    }
    
    /*
     * Start PE. Delete PointBase travel resource
     */
    private void testStartup() {
        //Start PE is it is not started yet
        ServerNavigatorOperator se = new ServerNavigatorOperator();
        // Skip next 2 steps if running on Mac as Jemmy's call popup issue
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            //Start PE is it is not started yet
            try {
                se.startServer("J2EE");
            } catch (Exception e) {
            }
            // Delete pb travel resource if it exists
            se.deleteResource("jdbc/Travel");
        }
    }
    
    /*
     *   Create new project
     *   And add property val to SessionBean1.java
     */
    public void testCreateProject() {
        startTest();
        log("**Creating Project");
        //Create Project
        try {
            ComponentUtils.createNewProject(_projectName);
        } catch(Exception e) {
            log(">> Project Creation Failed");
            e.printStackTrace();
            log(e.toString());
            fail(e);
        }
        log("**Done");
        endTest();
    }
    
    /*
     *  Add tree component. Set its text via property sheet
     */
    
    public void testAddTree() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add Tree component");
        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(5000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicTree"), designer, new Point(xTree, yTree));
        
        log("Set tree's text property via property sheet ");
        sheet = new SheetTableOperator();
        Util.wait(2000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Documents");
        //sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyText"), "Documents");
        
        log("Set Tree Node 1's text property");
        Util.wait(1000);
        
        log("**Select Tree Node 1 from Outline window");
        selectOutlineNode("tree1:Documents|treeNode1:Tree Node 1");
        
        sheet = new SheetTableOperator();
        Util.wait(5000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Creator Tutorial");
        Util.wait(1000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyUrl"), "http://java.sun.com/docs/books/tutorial" );
        Util.wait(1000);
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    /*
     *  Add 3  tree node components.
     *  Configure their properties
     */
    public void testConfigureTreeNodes() {
        startTest();
        log("Setup");
//Lines Fail!!  See Lark's Temp fix
//        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
//        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle,"basicPalette"));
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        sheet = new SheetTableOperator();
        Util.wait(1000);
//Larks temp fix
/*        log("larks temp workaround");
        palette = new PaletteContainerOperator(null); //Seems to work, every other?
            log("drop Tree Node2");
//            palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicTreeNode"), designer, new Point(xTreeNode, yTreeNode));
            palette.addComponent("Tree Node", designer, new Point(70, 70));
            Util.wait(1000);
            log("set properties");
            sheet = new SheetTableOperator();
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "JSP Page" );
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), "Open Page2" );
            Util.wait(1000);
            sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), "New window (_blank)" );
            Util.wait(1000);
            log("Node2 added");
 
            log("drop Tree Node3");
            designer.clickMouse("treeNode1", 1); //This is REALLY SLOW
            palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
//            palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicTreeNode"), designer, new Point(xTreeNode, yTreeNode));
            palette.dndPaletteComponent("Tree Node", designer, new Point(70, 70));
            Util.wait(1000);
            log("set properties");
            sheet = new SheetTableOperator();
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Image File" );
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), "Show an image" );
            Util.wait(1000);
            sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), "New window (_blank)" );
            Util.wait(1000);
            log("Node3 added");
 
            log("drop Tree Node4");
            designer.clickMouse("treeNode1", 1); //This is REALLY SLOW
            palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
//            palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicTreeNode"), designer, new Point(xTreeNode, yTreeNode));
            palette.dndPaletteComponent("Tree Node", designer, new Point(70, 70));
            Util.wait(1000);
            log("set properties");
            sheet = new SheetTableOperator();
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "PDF File" );
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), "Open a PDF File" );
            Util.wait(1000);
            sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), "New window (_blank)" );
            Util.wait(1000);
            log("Node4 added");
 
            log("end larks stuff");
//End Larks temp fix
 */
        
        String[] treeNodeName = { "JSP Page", "Image File", "PDF File"};
        String[] treeNodeToolTip = { "Open Page2", "Show an image", "Open a PDF File"};
        String[] treeNodeTarget = {Bundle.getStringTrimmed(_bundle, "propertyTargetNewWindow"), "", Bundle.getStringTrimmed(_bundle, "propertyTargetNewWindow")};
        
        int index=1;
        
        String path;
        int tmpnode;
		
        log("Add 3 more tree nodes to Documents. Set the text, tooltip, target properties via property sheet ");
        for (int i=0; i<3; i++) {
            
            log("Click Tree root node in outline");
            selectOutlineNode("tree1:Documents");
            
            log("drop Tree Node"+ i);
            designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
            palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
            palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicTreeNode"), designer, new Point(xTreeNode+5, yTreeNode+5));
            Util.wait(1000);
			
			log("select new node in outline");
			tmpnode = i + 2;
			selectOutlineNode("tree1:Documents|treeNode" + tmpnode + ":Tree Node " + tmpnode);
			Util.wait(1000);
			
            log("set properties");
            sheet = new SheetTableOperator();
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), treeNodeName[i] );
            Util.wait(1000);
            ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), treeNodeToolTip[i] );
            Util.wait(1000);
            if (treeNodeTarget[i]!=null)
                sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), treeNodeTarget[i] );
            Util.wait(1000);
            log("Node added");
        }
        
        log("Set Image File Node's url property");
        selectOutlineNode("tree1:Documents|treeNode3:Image File");
        sheet = new SheetTableOperator();
        sheet.setImage("treeNode3", Bundle.getStringTrimmed(_bundle, "propertyUrl"), imageFile);
        Util.wait(1000);
        
        log("Set FPD File Node's url property");
        selectOutlineNode("tree1:Documents|treeNode4:PDF File");
        sheet = new SheetTableOperator();
        sheet.setImage("treeNode4", Bundle.getStringTrimmed(_bundle, "propertyUrl"), pdfFile);
        Util.wait(1000);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    /*
     * Create second page
     */
    public void testAddSecondPage() {
        startTest();
        log("Create Page2");
        prjNav =  ProjectNavigatorOperator.showProjectNavigator();
        prjNav.addWebPage(_projectName, _page2);
        Util.wait(1000);
        log("Add a label component. Set its text property");
        designer = new DesignerPaneOperator(Util.getMainWindow(), 1);
        //designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        log(designer.toString());
        Util.wait(1000);
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicLabel"), designer, new Point(xLabel, yLabel));
        Util.wait(5000);
        sheet = new SheetTableOperator();
        Util.wait(1000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), _page2 );
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
     /*
      *  Create page navigation from JSP Page tree node to Page2
      */
    public void testCreatePageNavigation() {
        startTest();
        log("Go to Page1");
        new TopComponentOperator(_page1).getFocus();
        // designer = new DesignerPaneOperator(Util.getMainWindow());
       
//        log("Select treeNode2 and  add action event");
//        Util.wait(2000);
//        selectOutlineNode("tree1:Documents|treeNode2:JSP Page"); // Always call Tree root context menu
//        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
//        Util.wait(2000);
//        designer.clickMouse("treeNode2", 2);
//        log("Set outcome to page2 in Java editor");
//        JEditorPaneOperator editor = new JEditorPaneOperator(
//                RaveWindowOperator.getDefaultRave(), "public class " + "Page1");
//        
//        editor.setVerification(false);
//        TestUtils.wait(2000);
//        editor.requestFocus();
//        TestUtils.wait(2000);
//        editor.selectText("return null;");
//        editor.pushKey(KeyEvent.VK_DELETE);
//        editor.typeText("return \"page2\";\n");
//        TestUtils.wait(200);
//        editor.clickForPopup();
//        new JPopupMenuOperator().pushMenu("Reformat Code");
//        TestUtils.wait(200);
//        
//        log("Switch to designer");
//        designer.makeComponentVisible();
//        TestUtils.wait(10000);
        // TODO fix the ComponentUtils.linkWebPages(), then uncommet next lines
        //log("Create link between page1 to page2, named 'page1'");
        //ComponentUtils.linkWebPages(designer, _page1, _page2, "page2");
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
   /*
    * Deploy application
    */
    public void testDeploy() {
        startTest();
        //need to wait responce
        Waiter deploymentWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object output) {
                String text = ((OutputOperator)output).getText();
                if (text.indexOf(_buildSuccess)!=-1)
                    return _true;
                return null;
                
            }
            public String getDescription() {
                return("Waiting Project Deployed");
            }
        });
        log("Deploy from menu");
        ProjectNavigatorOperator.pressPopupItemOnNode(_projectName, _run);
        TestUtils.wait(2000);
        OutputOperator outputWindow = new OutputOperator();
        deploymentWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 240000);
        log("wait until " + _buildSuccess);
        try {
            deploymentWaiter.waitAction(outputWindow);
        } catch(InterruptedException e) {
            log(outputWindow.getText());
            e.printStackTrace();
            fail("Deployment error: "+e);
        }
        log("Deployment complete");
        endTest();
    }
    
    
    public void testCloseProject() {
        startTest();
        Util.saveAllAPICall();
        new ProjectNavigatorOperator().pressPopupItemOnNode(_projectName, Bundle.getStringTrimmed(_bundle,
                "CloseProjectPopupItem"));
        //TestUtils.closeCurrentProject();
        TestUtils.wait(5000);
        endTest();
    }
    
    private void selectOutlineNode(String path) {
        DocumentOutlineOperator doo ;
        String fullPath;
        
        doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);
        fullPath = "Page1|page1|html1|body1|form1|"+ path;
        doo.verify();
        doo.expandPath(fullPath);
        doo.selectPath(fullPath);
        Util.wait(5000);
    }
    
    
    private void callForActionHandlerPopup(String node) {
        DocumentOutlineOperator doo ;
        String fullPath;
        
        doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);
        fullPath = "Page1|page1|html1|body1|form1|"+ node;
        doo.verify();
        doo.clickOnPath(fullPath);
        doo.selectPath(fullPath);
        Util.wait(5000);
        doo.clickForPopup();
        new JPopupMenuOperator().pushMenuNoBlock("Edit action Event Handler");
        TestUtils.wait(2000);
        
    }

    /* Need to undeploy project to finish tests correctly */
    public void testUndeploy() {
        startTest();
        log("Initialize");
        explorer = ServerNavigatorOperator.showNavigatorOperator();
        String serverPath = _serverPath + _projectServer;  //Current deployment server
        String deploymentPath = serverPath + _deploymentPath; //glassfish specific
        String applicationPath = deploymentPath + _separator + _projectName; //project name
        
        // Select the Server Navigator and set the JTreeOperator
        log("get explorer");
        new QueueTool().waitEmpty(100); //??
        explorer.requestFocus();
        JTreeOperator tree = explorer.getTree();
        try { Thread.sleep(4000); } catch(Exception e) {} // Sleep 4 secs to make sure Server Navigator is in focus

        // Need to refresh J2EE AppServer node
        log("refresh");
        explorer.pushPopup(tree, serverPath, _refresh);
        TestUtils.wait(1000);
        
        log("refresh deployment path: " + deploymentPath);
        TestUtils.wait(1000);
        explorer.selectPath(deploymentPath);
        explorer.getTree().expandPath(explorer.getTree().findPath(deploymentPath));
        explorer.pushPopup(tree, deploymentPath, _refresh);
        TestUtils.wait(1000);

        log("undeploy Path: " + applicationPath);
        explorer.selectPath(applicationPath);
        TestUtils.wait(1000);
        
        log("Push Menu Undeploy...");
        explorer.pushPopup(explorer.getTree(), applicationPath, _undeploy);
        TestUtils.wait(5000);
        endTest();
    }
    
    public void testCheckIDELog() {
        startTest();
        try {
            String err = ComponentUtils.hasUnexpectedException();
            String str = "";
            if (!(err.equals(""))) {
                assertTrue("Unexpected  exceptions found in message.log: " + err, str.equals(""));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to open message.log : " + ioe);
        }
        endTest();
    }    
}

