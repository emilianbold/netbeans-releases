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

package   org.netbeans.modules.visualweb.test.components.action.imagehyperlink;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.io.IOException;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.navigation.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

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

    public String _projectName = "ImageHyperlinkAcceptanceTest";
   
    
    public String imageDir=ComponentUtils.getDataDir() + File.separator + "action" + File.separator;
    String image1= imageDir + "imagehyperlink_component.png";
    String image2 = imageDir + "orchid1.JPG";
    public String _requestPrefix = "";
    public static int xImageHyperlink=50;
    public static int yImageHyperlink1=50;
    public static int yImageHyperlink2=125;
    public static int yImageHyperlink3=225;
    public String _page1 = "Page1";
    public String _page2 = "Page2";
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    public String _separator = Bundle.getStringTrimmed(_bundle, "separator");
    public String _reformatCode = Bundle.getStringTrimmed(_bundle,"reformatCode");
    
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ProjectNavigatorOperator prjNav =  ProjectNavigatorOperator.showProjectNavigator();
    public static ServerNavigatorOperator explorer;
    
    
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddImageHyperlink3"));
        suite.addTest(new AcceptanceTest("testAddImageHyperlink1"));
        suite.addTest(new AcceptanceTest("testAddImageHyperlink2")); 
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
            fail();
        }
        log("**Done");
        endTest();
    }
    
    /*
     *  . Add 1st image hyperlink component. Change its text  property sheet as "Scheduler".
     *  . Clcik ... button of its icon property. Choose SCHEDULAR_POPUP from the list.
     *  . Set its tooltip property to " Launch Machine Scheduler  in a new window", and set New Window for target property
     *  . Double click the image hyperlink in designer, add code in hyperlink1_action() :
     *   hyperlink1.setUrl("http://bsqe-giant.sfbay.sun.com/RaveHWScheduler/index.cgi");
     */
    
    public void testAddImageHyperlink1() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add first ImageHyperlink component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(5000);
//      palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink1));
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink1));
        
        log("Set text, url, toolTips, ans target properties ");
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Scheduler");
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyUrl"), "http://bsqe-giant.sfbay.sun.com/RaveHWScheduler/index.cgi");
        ComponentUtils.setProperty(sheet,Bundle.getStringTrimmed(_bundle, "propertyToolTip"), "Launch Machine Scheduler  in a new window ");
        sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), Bundle.getStringTrimmed(_bundle, "propertyTargetNewWindow"));
        
        // setIcon(); Not working so far
        Util.wait(1000);
        
        log(" Add code in init()");
        designer.clickMouse( 1,  1);
        TestUtils.wait(3000);
        designer.clickMouse( 1,  1, 2);
        TestUtils.wait(3000);
 
        JEditorPaneOperator editor = new JEditorPaneOperator(
                                        RaveWindowOperator.getDefaultRave(), "public class " + _page1);
        editor.requestFocus();
        editor.setText("log(\"Button action performed\");\n");
        
        // Open context menuitem, Reformat code
        Util.wait(1000);
        editor.clickForPopup(300,300);
        new JPopupMenuOperator().pushMenu(_reformatCode);
        TestUtils.wait(1000);
        log("Java Editor Dump:");
        log(editor.getText());
        // Switch to design panel
        designer.makeComponentVisible();
      
        Util.wait(2000);
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
    /*
     *  Add 2nd imagehyperlink component.
     *  Set its text, url and imageURL properties
     */
    public void testAddImageHyperlink2() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        log("Add second imagehyperlink property");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle,"basicPalette"));
        Util.wait(5000);
//        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink2));
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink2));
        Util.wait(1000);
        
        log("Set text, url properties");
        sheet = new SheetTableOperator();
        Util.wait(1000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "");
        sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyUrl"), "http://www.yahoo.com");        // setProperty("url", "http://www.yahoo.com");
        Util.wait(2000);
        log(image1);
        sheet.setImage("imageHyperlink3:Image Hyperlink", Bundle.getStringTrimmed(_bundle, "propertyImageURL"), image1);
        Util.wait(2000);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
     /*
      *  . Add 3rd image hyperlink component. Set its image from designer. Change its width/length 50/40.
      *  . Change its  text to Page2. Set the border property to 2 . Set its text position to left.
      *  . Create  Page2.jsp.
      *  . Link this ImageHyperlink to Page2.jsp
      */
    public void testAddImageHyperlink3() {
       
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        Util.wait(5000);
        log("Add third imagehyperlink component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(5000); 
//        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink3));
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicImageHyperlink"), designer, new Point(xImageHyperlink, yImageHyperlink3));
        Util.wait(5000);
  
        log("Set its width, height, border, text properties");
        sheet = new SheetTableOperator();
        Util.wait(5000);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Page 2");
        Util.wait(2000);
        log(image2);
        designer.setImage(xImageHyperlink+5, yImageHyperlink3+5,  image2);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyWidth"), "50");
        ComponentUtils.setProperty(sheet,Bundle.getStringTrimmed(_bundle, "propertyHeight"), "40");
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyBorder"), "2");
        sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTextPosition"), Bundle.getStringTrimmed(_bundle, "propertyTextPositionLeft"));
        Util.wait(2000);
         
//        
//        log("Create second page Page2.jsp");
//        prjNav.addWebPage(_projectName, _page2);
//        log("Open first page");
//        prjNav.openWebPage(_projectName, _page1);
//        ComponentUtils.linkWebPages(designer, _page1, _page2, "next");
//        
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
    
    
    
    public void linkWebPages() {
        designer.clickForPopup(10, 10 );
        Util.wait(500);
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed(_bundle, "Designer_Menu_PageNavigation"));
        
        NavigatorOperator navigation = new NavigatorOperator();
        Util.wait(500);
        //queueTool.waitEmpty();
        
        // navigation.staticLink(_page1, _page2, _page2);
        Util.wait(1000);
        //queueTool.waitEmpty();
        
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

