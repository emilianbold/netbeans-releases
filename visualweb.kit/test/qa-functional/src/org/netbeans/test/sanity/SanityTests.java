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

package org.netbeans.test.sanity;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jellytools.OutputTabOperator;

import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.model.IDE;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.navigation.NavigatorOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * @author Alexey Butenko (alexey.butenko@sun.com)
 */
public class SanityTests extends RaveTestCase {
    
    public static String _projectName = "SanityProject";
    public static String _projectPath = System.getProperty("xtest.workdir") +
            File.separator + "projects" +
            File.separator +
            _projectName;
    public static String bundle="org.netbeans.test.sanity.Bundle";
    public static String ravePaletteBundle=Bundle.getStringTrimmed(bundle,"RavePaletteBundle");
    public static String _projectType = Bundle.getStringTrimmed(bundle, "ProjectType");
    /*
    public static String _projectCategory = Bundle.getStringTrimmed(
                                                Bundle.getStringTrimmed(bundle,"ProjectWizardBundle"),
                                                Bundle.getStringTrimmed(bundle, "ProjectCategory"));
     */
    public static String _projectCategory = Bundle.getStringTrimmed(bundle, "ProjectCategory");
    public static String _requestPrefix;
    public static String _pluginName = "Visual Web JSF Backwards Compatibility Kit";
    public static DesignerPaneOperator designer;
    public static String paletteGroup = null;
    public static int xButtonLoc;
    public static int yButtonLoc;

    static final String [] tests = {
                "testAddPlugin",
                "testAddApplicationServer",
                "testCreateProject",
                "testAddButton",
                "testBackingFile",
                "testCloseProject",
                "testCreateJavaEE5Project",
                "testAddStandardComponents",
                "testExecution",
                "testCheckIDELog"
    };
    
    public SanityTests(String testName) {
        super(testName);
    }
    
    /*public static Test suite() {
        TestSuite suite= new NbTestSuite();
        suite.addTest(new SanityTests("testAddPlugin"));
        suite.addTest(new SanityTests("testAddApplicationServer"));
        suite.addTest(new SanityTests("testCreateProject"));
        suite.addTest(new SanityTests("testAddButton"));
        // Comment out as PageNavigation is not supposed to work for M7
        //suite.addTest(new SanityTests("testPageNavigation"));
        //TODO: commented because creating of component's Event Habdler method not implemented yet
        suite.addTest(new SanityTests("testBackingFile"));
        suite.addTest(new SanityTests("testCloseProject"));
        suite.addTest(new SanityTests("testCreateJavaEE5Project"));
        //suite.addTest(new SanityTests("testAddComponents"));
        suite.addTest(new SanityTests("testAddStandardComponents"));
        suite.addTest(new SanityTests("testExecution"));
        suite.addTest(new SanityTests("testCheckIDELog"));
        return suite;
    }*/

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SanityTests.class)
                .addTest(tests)
                .clusters(".*")
                .enableModules(".*")
                .gui(true)
                );
    }

    /** method called before each testcase
     */
    protected void setUp() {
        Util.saveAllAPICall();
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
        //Util.saveAllAPICall();
        System.out.println("########  "+getName()+" Finished #######");
    }
    
    public void testAddPlugin() {
        startTest();
        PluginsOperator.getInstance().installAvailablePlugins(_pluginName);
        endTest();
    }
    
    public void testAddApplicationServer() {
        startTest();
       // JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        IDE.getIDE();
        endTest();
    }
    
    /*
     *   Create new project
     *   And add property val to SessionBean1.java
     */
    public void testCreateProject() {
        startTest();
        // Create Project
        try {
            log("project type = " + _projectType + ", project category = " + _projectCategory);
            TestUtils.createNewProjectLoc(_projectPath, _projectName, true, _projectType, _projectCategory);
        } catch(Exception e) {
            e.printStackTrace();
            log("Exception during project creation occured " + e);
            fail();
        }
        endTest();
    }
    public void testAddButton() {
        startTest();
        new org.netbeans.jellytools.TopComponentOperator(RaveWindowOperator.getDefaultRave(), Bundle.getStringTrimmed(bundle,"Page1"));
        TestUtils.wait(50000);
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        
        //Bring up palette. Workaround for issue http://www.netbeans.org/issues/show_bug.cgi?id=105200
        PaletteContainerOperator.showPalette();
        TestUtils.wait(1000);
        
        // Add Button component
        paletteGroup = Bundle.getStringTrimmed(ravePaletteBundle, Bundle.getStringTrimmed(bundle,"PaletteGroup"));
        log("Palette Group = " +paletteGroup);
        PaletteContainerOperator palette = new PaletteContainerOperator(paletteGroup);
        
        // Wait for the Portfolio to appear
        try { Thread.sleep(5000); } catch(Exception e) {}
        log("Rave Palette Bundle = "+ravePaletteBundle);
        //log("Button text = "+ Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"ButtonComponent")));
        // TODO :Need to fine appropriate bundle
        //palette.addComponent(Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"ButtonComponent")), designer, new Point(168, 168));
        palette.addComponent(Bundle.getStringTrimmed(bundle,"ButtonComponent"), designer, new Point(168, 168));
        TestUtils.wait(7000);
        try {
            //Waiting until button's Properties window appeared, otherwise will fail.
            new org.netbeans.jellytools.TopComponentOperator("button1:");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Probably button wasn't aded on designer: "+ e );
        }
        SheetTableOperator sheet = new SheetTableOperator();
        TestUtils.wait(2000);
        String componentValue=//Bundle.getStringTrimmed(
                //Bundle.getStringTrimmed(bundle,"PropertiesBundle"),
                Bundle.getStringTrimmed(bundle,"ComponentValue");//);
        log("componentValue = "+componentValue);
        sheet.clickForEdit(sheet.findCell(componentValue, 2).y, 1);
        TestUtils.wait(2000);
        new JTextComponentOperator(sheet).enterText(Bundle.getStringTrimmed(bundle,"TCButtonValue"));
        TestUtils.wait(1000);
        
        
        try {
            Point btnLoc =  designer.getComponentCenter("button1");
        } catch(Exception e) {
            e.printStackTrace();
            fail("Button disappeared: " + e);
        }
        //Util.saveAllAPICall();
        designer.switchToJSPSource();
        //This string should present in JSP editor
        String verStr = "text=\""+
                Bundle.getStringTrimmed(bundle, "TCButtonValue")+"\"";
        String Page1= Bundle.getStringTrimmed(bundle, "Page1");
        assertFalse("There is no \""+ verStr+ "\" string in jsp editor",
                new org.netbeans.jellytools.EditorOperator(Page1).getText().indexOf(verStr)==-1);
        endTest();
    }
    
    public void testPageNavigation() {
        startTest();
        ProjectNavigatorOperator prjNav = new ProjectNavigatorOperator();
        prjNav.requestFocus();
        
        TestUtils.wait(1000);
        String bundleName = Bundle.getStringTrimmed(bundle,"ProjectsBundle");
        String pageNavigation=Bundle.getStringTrimmed(bundleName,Bundle.getStringTrimmed(bundle,"PageNavigation"));
        log("pageNavigation = "+ pageNavigation);
        prjNav.pressPopupItemOnNode(_projectName+"|"+
                pageNavigation,
                Bundle.getStringTrimmed(bundle,"OpenPopupItem"));
        NavigatorOperator no = new NavigatorOperator();
        TestUtils.wait(1500);
        String addPage = Bundle.getStringTrimmed(
                Bundle.getStringTrimmed(bundle,"RaveNavigationGraphBundle"),
                Bundle.getStringTrimmed(bundle,"AddPagePopupItem"));
        log("Add Page Popup Item = "+addPage);
        no.pushPopup(addPage);
        TestUtils.wait(5000);
        bundleName = Bundle.getStringTrimmed(bundle,"RaveNavigationBundle");
        String newFormTitle=Bundle.getStringTrimmed(bundleName,Bundle.getStringTrimmed(bundle,"NewFormTitle"));
        log("newFormTitle = "+newFormTitle);
        new JButtonOperator(new JDialogOperator(newFormTitle),
                "OK").pushNoBlock();
        
        // Create link between page1 to page2, named 'go'
        no.link(Bundle.getStringTrimmed(bundle,"Page1")+".jsp",
                Bundle.getStringTrimmed(bundle,"Page2")+".jsp", "go");
        TestUtils.wait(500);
        endTest();
    }
    
    /*
     *  Test for java file editing
     */
    public void testBackingFile() {
        startTest();
        designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.makeComponentVisible();
        TestUtils.wait(2000);
        // Double click at button to open Jave Editor
        try {
            Point btnLoc =  designer.getComponentCenter("button1");
            xButtonLoc = (int)btnLoc.getX();
            yButtonLoc = (int)btnLoc.getY();
        } catch(Exception e) {
            e.printStackTrace();
            fail("There is no button1 was found on designer: "+ e);
        }
        
        TestUtils.wait(2000);
        log("(x,y)=(" + xButtonLoc +"," + yButtonLoc+")");
        
        designer.clickMouse(xButtonLoc, yButtonLoc, 2);
        org.netbeans.jellytools.TopComponentOperator tc = 
                new org.netbeans.jellytools.TopComponentOperator(Bundle.getStringTrimmed(bundle, "Page1"));
        org.netbeans.jellytools.EditorOperator editor = 
                new org.netbeans.jellytools.EditorOperator((JComponent)tc.waitSubComponent(
                    new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().endsWith("JavaEditorTopComponent");
            }
            public String getDescription() {
                return "JavaEditorTopComponent";
            }
        }));
        editor.replace("return null;", "return \"go\";\n");
        new Action(null, "Format").perform(editor);
        // Switch to design panel
        designer.makeComponentVisible();
        endTest();
    }
    
    public void testCloseProject() {
        startTest();
        Util.saveAllAPICall();
        String closeProjectPopupItem = //Bundle.getStringTrimmed(
                // Bundle.getStringTrimmed(bundle,"ProjectsCustomizerBundle"),
                Bundle.getStringTrimmed(bundle,"CloseProjectPopupItem");//);
        log("closeProjectPopupItem = "+ closeProjectPopupItem);
        new ProjectNavigatorOperator().pressPopupItemOnNode(_projectName,closeProjectPopupItem);
        //TestUtils.closeCurrentProject();
        TestUtils.wait(5000);
        endTest();
    }
    
    public void testCreateJavaEE5Project() {
        _projectName = "SanityJavaEE5Project";
        startTest();
        try {
            TestUtils.createJavaEE5ProjectLoc(_projectPath, _projectName,true, _projectType, _projectCategory);
        } catch(Exception e) {
            e.printStackTrace();
            log("Exception during project creation occured " + e);
            fail("Exception during project creation: ");
        }
        endTest();
    }
    
    public void testAddComponents() {
        startTest();
        new org.netbeans.jellytools.TopComponentOperator(RaveWindowOperator.getDefaultRave(), Bundle.getStringTrimmed(bundle,"Page1"));
        //TestUtils.wait(20000);
        org.netbeans.jemmy.QueueTool theTool = new org.netbeans.jemmy.QueueTool();
        theTool.getTimeouts().setTimeout("QueueTool.WaitQueueEmptyTimeout", 60000);
        theTool.waitEmpty(100);
        designer = new DesignerPaneOperator(Util.getMainWindow());
        
        //Bring up palette. Workaround for issue http://www.netbeans.org/issues/show_bug.cgi?id=105200
        PaletteContainerOperator.showPalette();
        TestUtils.wait(1000);
        
        paletteGroup = Bundle.getStringTrimmed(ravePaletteBundle, Bundle.getStringTrimmed(bundle,"PaletteGroup"));
        PaletteContainerOperator palette = new PaletteContainerOperator(paletteGroup);
        TestUtils.wait(2000);
        //TODO: Find appropriate Rave bundle
        //String label = Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"LabelComponent"));
        String label = Bundle.getStringTrimmed(bundle,"LabelComponent");
        palette.addComponent(label, designer, new Point(5,5));
        TestUtils.wait(10000);
        String componentValue=//Bundle.getStringTrimmed(
                //Bundle.getStringTrimmed(bundle,"PropertiesBundle"),
                Bundle.getStringTrimmed(bundle,"ComponentValue");//);
        log("componentValue = "+componentValue);
        new SheetTableOperator().setTextValue(componentValue, "Java EE 5 Project");
        TestUtils.wait(2000);
        //Getting button name from Rave bundle
        //by getting its bundle name and bundle location from local byndle
        //TODO Find appropriate Rave Bundle
        //String button = Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"ButtonComponent"));
        String button = Bundle.getStringTrimmed(bundle,"ButtonComponent");
        palette.addComponent(button, designer, new Point(100,80));
        TestUtils.wait(5000);
        try {
            //Waiting until button's Properties window appeared, otherwise will fail.
            new org.netbeans.jellytools.TopComponentOperator("button1:");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Probably button wasn't aded on designer: "+ e );
        }
        
        
        new SheetTableOperator().setButtonValue("text", "Push me");
        TestUtils.wait(2000);
        Util.saveAllAPICall();
        designer.clickMouse("button1", 2);
        EditorOperator editor = new EditorOperator(Util.getMainWindow(),Bundle.getStringTrimmed(bundle,"Page1"));
        //Because of #123536, it will be necessary to add binding explicitly, therefore requires an additional
        //step in sanity. Commenting out the following to avoid sanity failures
//        TestUtils.wait(2000);
//        editor.txtEditorPane().typeText("label1.setValue(\"Clicked on button\"); \n");
        TestUtils.wait(2000);
        designer.switchToDesignerPane();
        TestUtils.wait(2000);
        Util.getMainWindow().saveAll();
        endTest();
    }
    
    public void testAddStandardComponents() {
        startTest();
        new org.netbeans.jellytools.TopComponentOperator(RaveWindowOperator.getDefaultRave(), Bundle.getStringTrimmed(bundle,"Page1"));
        TestUtils.wait(20000);
        designer = new DesignerPaneOperator(Util.getMainWindow());
        
        paletteGroup = Bundle.getStringTrimmed(bundle, "StandardPalette");
        PaletteContainerOperator palette = new PaletteContainerOperator(paletteGroup);
        TestUtils.wait(2000);
        //TODO: Find appropriate Rave bundle
        //String label = Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"LabelComponent"));
         String label = Bundle.getStringTrimmed(bundle,"StandardOutputTextComponent");
        palette.addComponent(label, designer, new Point(5,5));
        TestUtils.wait(5000);
        String componentValue=//Bundle.getStringTrimmed(
                //Bundle.getStringTrimmed(bundle,"PropertiesBundle"),
                Bundle.getStringTrimmed(bundle,"ComponentValue");//);
        log("componentValue = "+componentValue);
        new SheetTableOperator().setTextValue("value", "Java EE 5 Project");
        TestUtils.wait(2000);
        //Getting button name from Rave bundle
        //by getting its bundle name and bundle location from local byndle
        //TODO Find appropriate Rave Bundle
        //String button = Bundle.getStringTrimmed(ravePaletteBundle,Bundle.getStringTrimmed(bundle,"ButtonComponent"));
        String button = Bundle.getStringTrimmed(bundle,"StandardButtonComponent");
        palette.addComponent(button, designer, new Point(100,80));
        TestUtils.wait(5000);
        try {
            //Waiting until button's Properties window appeared, otherwise will fail.
            new org.netbeans.jellytools.TopComponentOperator("button1:");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Probably button wasn't aded on designer: "+ e );
        }
        
        new SheetTableOperator().setTextValue("value", "Push me");
        TestUtils.wait(2000);
        Util.saveAllAPICall();
        designer.clickMouse("button1", 2);
        EditorOperator editor = new EditorOperator(Util.getMainWindow(),Bundle.getStringTrimmed(bundle,"Page1"));
        TestUtils.wait(2000);
        //Because of #123536, it will be necessary to add binding explicitly, therefore requires an additional
        //step in sanity. Commenting out the following to avoid sanity failures        
//        editor.txtEditorPane().typeText("outputText1.setValue(\"Clicked on button\"); \n");
//        TestUtils.wait(2000);
        designer.switchToDesignerPane();
        TestUtils.wait(2000);
        Util.getMainWindow().saveAll();
        endTest();
    }
    
    
    public void testExecution() {     
        startTest();
        String runProjectPopupItem = Bundle.getStringTrimmed(
                Bundle.getStringTrimmed(bundle,"ProjectsBundle"),
                Bundle.getStringTrimmed(bundle,"RunProjectPopupItem"));
        log("runProjectPopupItem = " + runProjectPopupItem);
        ProjectNavigatorOperator.pressPopupItemOnNode(_projectName, runProjectPopupItem);
        TestUtils.wait(2000);
        OutputTabOperator oto = new OutputTabOperator(_projectName);
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 240000);
        try {
            oto.waitText("BUILD SUCCESSFUL");
        } catch (TimeoutExpiredException e) {
            log(oto.getText());
            try {
                // log glassfish message
                log("glassfish.txt", new OutputTabOperator("GlassFish").getText());
            } catch (Exception ex) {
                // ignore
            } 
            e.printStackTrace();
            fail("Deployment error: "+e);
        }
        
        try {
/*            WebConversation conversation = new WebConversation();
            WebResponse response = null;
            //HttpUnitOptions.setExceptionsThrownOnScriptError(false);
            HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
            HttpUnitOptions.setScriptingEnabled(false);
            
            // Get deployment target prefix from test/data/DefaultDepploymentTargets.propertes.
            // By default, it is http://localhost:8080
*/            ApplicationServer  as = (ApplicationServer) IDE.getIDE().getDeploymentTargets().get(0);
/*            _requestPrefix=as.requestPrefix;
            log("prefix="+as.requestPrefix);
            
            response = conversation.getResponse(_requestPrefix+_projectName);
            TestUtils.wait(5000);
            log("Response text before form1 submit: " + response.getText());
            WebForm addContainer = response.getForms()[0];
            SubmitButton btn = addContainer.getSubmitButtons()[0];
            response = addContainer.submit(btn);
            TestUtils.wait(5000);
            log("Response text after form1 submit: " + response.getText());
            //TODO action handler commented in testAddComponents()
        //Because of #123536, it will be necessary to add binding explicitly, therefore requires an additional
        //step in sanity. Commenting out the following to avoid sanity failures. Should uncomment once additional
        //step is added
//            assertFalse("Wrong response after form submit", response.getText().indexOf("Clicked on button")==-1);
//            //assertFalse("Wrong response after form submit", response.getText().indexOf("Push me")==-1);
*/            TestUtils.wait(2000);
            ServerNavigatorOperator.showNavigatorOperator().pushPopup(ServerNavigatorOperator.STR_SERVERS_PATH + 
                    as.web_applications_path + "|" + as.app_pref + _projectName, as.APPLICATION_UNDEPLOY);
            TestUtils.wait(1000);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception during run : "+e);
        }
        endTest();
    }
    
    
    public void testCheckIDELog() {
        startTest();
        try {
            assertTrue("Unexpected  exceptions found in message.log", !hasUnexpectedException());
        }catch(IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to open message.log : " + ioe);
        }
        endTest();
    }
    
    public boolean hasUnexpectedException() throws IOException {
        String[] knownException={"import javax.faces.FacesException",
                                  "java.lang.IllegalStateException",
                                  "java.lang.IllegalArgumentException: Expected scheme-specific part at index", 
                                  "java.lang.IllegalArgumentException: Cannot get BASE revision,", 
                                  "java.net.UnknownHostException: www.netbeans.org",
                                  "java.io.IOException"};
 
        
        String logFile = System.getProperty("xtest.workdir") +
                File.separator +
                Bundle.getStringTrimmed(bundle,"LogFile");
        String lineSep = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        String nextLine = "";
        boolean isUnexpectedException;
        
        //Get lines that has word "Exceptions" from message.log
        String exceptions=TestUtils.parseLogs(logFile, "Exception");
        if (exceptions.equals("")) {
            log("There are no any execptions found");
            return false;
        } else {
            log("Exceptions found in message.log:\n"+exceptions);         
            // Compile the pattern
            String patternStr = "^(.*)$";
            Pattern pattern = Pattern.compile(patternStr, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(exceptions);
            
            // Read exceptions line by line to determine if it is unexpected
            while (matcher.find()) {
                isUnexpectedException=true;
                nextLine = matcher.group(1);
                for (int i=0; i<knownException.length; i++ ){
                    if (nextLine.indexOf(knownException[i])!=-1)
                        isUnexpectedException=false;
                }
                if (isUnexpectedException){
                    sb.append(nextLine);
                    sb.append(lineSep);
                }
            }
            if (!sb.toString().equals("")) {
                log("Unexpected exceptions: \n"+sb.toString());
                return true;
            } else
                return false;
        }
    }
}
