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

package org.netbeans.modules.visualweb.test.components.action.hyperlink;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
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
   
    public String _projectName = "HyperlinkAcceptanceTest";
 
    public String _page1 = "Page1";
    public String _page2 = "Page2";
    
    //Menu variables
    public String _separator = Bundle.getStringTrimmed(_bundle,"separator");
    public String _propertySheet = Bundle.getStringTrimmed(_bundle,"propertySheet");
	public String _reformatCode = Bundle.getStringTrimmed(_bundle,"reformatCode");
    
    public static int xHyperlink=50;
    public static int yHyperlink1=50;
    public static int yHyperlink2=125;
    public static int yHyperlink3=200;
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    
    public static DesignerPaneOperator designer;
    public static SheetTableOperator sheet;
    public static ProjectNavigatorOperator prjNav =  ProjectNavigatorOperator.showProjectNavigator();
    public static ServerNavigatorOperator explorer;
    
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");

     
    public AcceptanceTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite= new TestSuite();
        suite.addTest(new AcceptanceTest("testCreateProject"));
        suite.addTest(new AcceptanceTest("testAddHyperlink1"));
        suite.addTest(new AcceptanceTest("testAddHyperlink2"));
        suite.addTest(new AcceptanceTest("testAddHyperlink3"));
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
                fail(e);
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
     *  . Add 1st hyperlink component. Change its text  property sheet as "Go to Google".
     *  . Change its text color via style property.
     *  . Set its tooltip property to " Launch in a new window", and set New Window for target property
     *  . Double click the image hyperlink in designer, add code in hyperlink1_action() :
     *   hyperlink1.setUrl("http://www.google.com");
     */
    
    public void testAddHyperlink1() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        log("Add first hyperlink component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(5000);
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicHyperlink"), designer, new Point(xHyperlink, yHyperlink1));
        
        log("Set text, toolTip, target properties");
        sheet = new SheetTableOperator();
        String GO_TO_GOOGLE = "Go to Google";
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), GO_TO_GOOGLE);
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyToolTip"), "Launch browser in a new window ");
        sheet.setComboBoxValue(Bundle.getStringTrimmed(_bundle, "propertyTarget"), Bundle.getStringTrimmed(_bundle, "propertyTargetNewWindow"));
        Util.wait(1000);
        
        log("Set style property");
        setFontStyle("hyperlink1", GO_TO_GOOGLE, "serif", "36", "fuchsia");
        Util.wait(5000); //setFontStyle is not blocking so need to give time to finish.
        
        log("Add code to processValueChange");
        designer.clickMouse(xHyperlink, yHyperlink1, 2);
        TestUtils.wait(3000);
        JEditorPaneOperator editor = new JEditorPaneOperator(
                RaveWindowOperator.getDefaultRave(), "public class " + _page1 );
        
        editor.setVerification(false);
        TestUtils.wait(2000);
        editor.requestFocus();
        TestUtils.wait(2000);
        editor.pushKey(KeyEvent.VK_ENTER);
        editor.typeText("//hyperlink1.setUrl(\"http://www.google.com\");");
        editor.pushKey(KeyEvent.VK_ENTER);
        
        TestUtils.wait(200);
        editor.clickForPopup();
        new JPopupMenuOperator().pushMenu(_reformatCode);
        TestUtils.wait(200);
        // Switch to design panel
        designer.makeComponentVisible();
        TestUtils.wait(10000);
        endTest();
    }
    
    /*
     *  Add 2nd Hyperlink component.
     *  Set its text, url properties
     */
    public void testAddHyperlink2() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add second hyperlink component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(1000);
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicHyperlink"), designer, new Point(xHyperlink, yHyperlink2));
        
        log("make sure property sheet is visible"); //or it will set previous component values.
        Util.getMainMenu().pushMenu(_propertySheet,_separator);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("Set its text and url property");
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet,Bundle.getStringTrimmed(_bundle, "propertyText"), "Sun Java Studio Creator");
        // sheet.setButtonValue(Bundle.getStringTrimmed(_bundle, "propertyText"), "Sun Java Studio Creator");
        ComponentUtils.setProperty(sheet,Bundle.getStringTrimmed(_bundle, "propertyUrl"), "http://developers.sun.com/prodtech/javatools/jscreator/");
        Util.wait(2000);
        
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
    
     /*
      *  . Add 3rd hyperlink component.
      *  . Change its  text to Page2.
      *  . Create  Page2.jsp.
      *  . Link this Hyperlink to Page2.jsp
      */
    public void testAddHyperlink3() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        log("Add 3rd hyperlink component");
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicHyperlink"), designer, new Point(xHyperlink, yHyperlink3));
        
        log("make sure property sheet is visible"); //or it will set previous component values.
        Util.getMainMenu().pushMenu(_propertySheet,_separator);
        try { Thread.sleep(2000); } catch(Exception e) {}

        log("Set its text property");
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Page 2");
        Util.wait(2000); 
        
        log("Create second page Page2.jsp"); 
        Util.saveAllAPICall();//adding page fails - takes too long, so save first.
        Util.wait(5000); 
        prjNav.addWebPage(_projectName, _page2);  
        Util.wait(8000); 
        Util.wait(8000); 
        Util.wait(8000); 
/*        
        log("add label to page");
        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        palette.dndPaletteComponent(Bundle.getStringTrimmed(_bundle, "basicLabel"), designer, new Point(50, 50));

        log("Set its text property");
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Page 2");
        Util.wait(2000); 
*/
        log("Go back to first page");
        prjNav.openWebPage(_projectName, _page1);
        //TODO fix the org.netbeans.modules.visualweb.gravy.navigation.NavigatorOperator.NavigatorChooser class, then uncomment the next line
        //ComponentUtils.linkWebPages(designer, _page1, _page2, "next");
        
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
    
    
    public void setFontStyle(String componentID, String ComponentName, String style, String size, String color) {
        sheet = new SheetTableOperator();
        sheet.pushDotted(Bundle.getStringTrimmed(_bundle, "propertyStyle"));
        String title = componentID + ":" + ComponentName + " - "
                + Bundle.getStringTrimmed(_bundle, "propertyStyle");
        JDialogOperator dialog = new JDialogOperator(title);
        (new JListOperator(dialog, 0)).selectItem(Bundle.getStringTrimmed(_bundle, "style_font"));
        if (!style.equals("")) {
            (new JListOperator(dialog, 1)).selectItem(style);
        }
        if(!size.equals("")) {
            (new JListOperator(dialog, 2)).selectItem(size);
        }
        if(!color.equals("")) {
            (new JComboBoxOperator(dialog, 4)).selectItem(color);
        }
        TestUtils.wait(1000);
        (new JButtonOperator(dialog, "OK")).pushNoBlock();
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

