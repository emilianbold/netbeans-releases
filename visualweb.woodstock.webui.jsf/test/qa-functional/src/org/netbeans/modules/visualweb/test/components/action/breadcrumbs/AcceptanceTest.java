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

package  org.netbeans.modules.visualweb.test.components.action.breadcrumbs;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;
import java.awt.*;
import java.io.IOException;

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
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * @author Sherry Zhou (sherry.zhou@sun.com)
 */
public class AcceptanceTest extends RaveTestCase {
    
    public String _bundle = ComponentUtils.getBundle();
    public String _projectServer = Bundle.getStringTrimmed(_bundle,"projectServer");
    public String _logFileLocation = Bundle.getStringTrimmed(_bundle,"logFile");
    public String _logFile = System.getProperty("xtest.workdir") + File.separator + _logFileLocation;
    public String _exception = Bundle.getStringTrimmed(_bundle,"Exception");
    public String _close = Bundle.getStringTrimmed(_bundle,"close");    
    public String _run = Bundle.getStringTrimmed(_bundle,"Run");
    public String _buildSuccess = Bundle.getStringTrimmed(_bundle,"buildSuccess");
    public String _true = Bundle.getStringTrimmed(_bundle,"true");
    
    public String _projectName = "BreadcrumbsAcceptanceTest";

    public String _page1 = "Page1";
    public String _page2 = "Page2";
    
    //Menu variables
    public String _separator = Bundle.getStringTrimmed(_bundle,"separator");
    public String _propertySheet = Bundle.getStringTrimmed(_bundle,"propertySheet");
    
    //undeployment
    public String _undeploy = Bundle.getStringTrimmed(_bundle, "undeploy");
    public String _refresh = Bundle.getStringTrimmed(_bundle, "refresh");
    public String _serverPath = Bundle.getStringTrimmed(_bundle, "serverPath");
    public String _deploymentPath = Bundle.getStringTrimmed(_bundle, "deploymentPathGlassfish");
    
    public static int xLabel=50;
    public static int yLabel=50;
    public static int xBreadcrumbs=50;
    public static int yBreadcrumbs=150; 
    
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
        suite.addTest(new AcceptanceTest("testAddPage2"));
        suite.addTest(new AcceptanceTest("testAddBreadcrumbsOnPage1"));
        suite.addTest(new AcceptanceTest("testAddBreadcrumbsOnPage2"));
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
     * Create Second Page page2.jsp
     */
    
    public void testAddPage2() {
        startTest();
        log("Add second page");
        prjNav.addWebPage(_projectName, _page2);  
        Util.saveAllAPICall();
        Util.wait(2000);
        endTest();
    }
 
     /*
      * Add label and breadcrumbs on Page1. Verify breadcrumb on outline
      */
    public void testAddBreadcrumbsOnPage1() {
        startTest();
        log("Open Page1.jsp");
        prjNav.openWebPage(_projectName, _page1);
         Util.wait(2000);
        log("Add a label component");
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());     
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicLabel"), designer, new Point(xLabel, yLabel));
        
        log("Set the label's text property");
        Util.wait(1000);
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Page 1");
        Util.wait(2000); 
        
        log("Add breadcrumbs");
        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "compositePalette"));
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "compositeBreadcrumbs"), designer, new Point(xBreadcrumbs, yBreadcrumbs));
        Util.wait(2000);

        log("Verify breadcrumbs in outlin window");
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);

        log("**Select breadcrumbs from Outline window");
        String path = "Page1|page1|html1|body1|form1|breadcrumbs1";
        
        doo.verify();
//        doo.clickOnPath(path);
        doo.selectPath(path);
        Util.wait(5000);        

        Util.saveAllAPICall(); 
        Util.wait(5000); 
        endTest();
    }
    
    /*
     * Add static text and breadcrumbs on second page
     */
    
    public void testAddBreadcrumbsOnPage2() {
        startTest();
        log("Open Page2.jsp");
        prjNav.openWebPage(_projectName, _page2);
        Util.wait(2000);
        log("Add a label");
        //designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());     
        designer = new DesignerPaneOperator(Util.getMainWindow(), 1);     

        Util.wait(2000);
        PaletteContainerOperator palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "basicPalette"));
        Util.wait(2000);
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "basicLabel"), designer, new Point(xLabel, yLabel));

        Util.wait(2000);
        log("Set the statictext's text property");
        sheet = new SheetTableOperator();
        ComponentUtils.setProperty(sheet, Bundle.getStringTrimmed(_bundle, "propertyText"), "Page 2");
        Util.wait(2000); 
        
        log("Add breadcrumbs");
        palette = new PaletteContainerOperator(Bundle.getStringTrimmed(_bundle, "compositePalette"));
        palette.addComponent(Bundle.getStringTrimmed(_bundle, "compositeBreadcrumbs"), designer, new Point(xBreadcrumbs, yBreadcrumbs));
        Util.wait(2000);

         log("Verify breadcrumbs in outlin window");
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        Util.wait(2000);

        log("**Select breadcrumbs from Outline window");
        String path = "Page2|page1|html1|body1|form1|breadcrumbs1";
 
        doo.verify();
//        doo.clickOnPath(path);
        doo.selectPath(path);
        Util.wait(5000);  
        
        Util.saveAllAPICall(); 
        Util.wait(5000); 
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

