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

package org.netbeans.modules.visualweb.gravy;

import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import java.nio.charset.MalformedInputException;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.modules.visualweb.gravy.debugger.BuildOutputOperator;
import org.netbeans.modules.visualweb.gravy.welcome.WelcomeOperator;
import org.netbeans.modules.visualweb.gravy.actions.CloseProjectAction;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.net.URL;
import org.netbeans.modules.visualweb.gravy.properties.Property;
import org.netbeans.modules.visualweb.gravy.properties.PropertySheetTabOperator;
import org.netbeans.modules.visualweb.gravy.properties.PropertySheetOperator;
import org.netbeans.modules.visualweb.gravy.designer.*;
import javax.swing.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.drivers.*;
import org.netbeans.jemmy.drivers.input.*;
import org.netbeans.jemmy.operators.*;
////import com.meterware.httpunit.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.jellytools.OutputOperator;
import org.openide.windows.TopComponent;

/**
 * TestUtils class
 * @author Vladimir Strigun (sva@sparc.spb.su)
 */

public class TestUtils {

    public static final String KEY_STRING_AFTER_PRJ_NAME_J2EE = "src",
                               KEY_STRING_AFTER_PRJ_NAME_JSF  = "web";
    private static final String SAVE_DIALOG_TITLE = "Save";
    private static final String SAVE_ALL = "Save All";
    //public static final String SAVE_ALL = Bundle.getStringTrimmed("com.sun.rave.project.actions.Bundle", "LBL_SaveAllAction");
    private static final String CLOSE_PROJECT = "Close";
    //public static final String CLOSE_PROJECT = Bundle.getStringTrimmed("com.sun.rave.project.actions.Bundle", "LBL_CloseProjectAction");
    
    private static String delim = "|";
    private static String fSep = System.getProperty("file.separator");
    
    private static String pathLastCreatedProject;

    private static final String LBL_VWJSF = "Visual Web JavaServer Faces";
    
    /** Create new Project with given name
     *   @param projectName - Project's name
     */
    public static void createNewProject(String projectName){
        
        createNewProjectAbsoluteLocation(null,projectName);
    }
    
    /** Create new Project with name included time stamp
     *   @param projectName - Project's name
     */
    public static void createNewProjectTimeStamped(String projectName){
        createNewProject( projectName+"_"+getCurrentDateAndTimeAsString());
    }
    
    /** Create new Project with given name and relative location
     *   @param location - relative location
     *   @param projectName - Project's name
     */
    
    public static void createNewProjectLocation(String location, String projectName){
        
        createNewProjectLoc(location,projectName,false);
    }
    
    /** Create new Project with given name and absolute location
     *   @param location - absolute location
     *   @param projectName - Project's name
     */
    
    public static void createNewProjectAbsoluteLocation(String location, String projectName){
        
        createNewProjectLoc(location,projectName,true);
    }
    
    public static String createNewPortletProjectLoc(String location, String projectName,
            boolean absoluteLocation, String projectCategory, String projectType) {
        NewProjectWizardOperator po=NewProjectWizardOperator.invoke();
        
        //need to wait for a list containing project
        Waiter projectListWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object po) {
                JList projList =
                        (JList)((ContainerOperator)po).
                        findSubComponent(new JListOperator.JListFinder(), 1);
                JListOperator projListOper = new JListOperator(projList);
                projListOper.copyEnvironment((ComponentOperator)po);
                /*return((projListOper.findItemIndex(Bundle.getString(
                        "com.sun.rave.project.nbbridge.Bundle",
                        "Templates/Project/Web/raveform.xml")) == -1) ? null : "");*/
                //return((projListOper.findItemIndex("Application") == -1) ? null : "");
                if ((projListOper.findItemIndex("Application") == -1)&&(projListOper.findItemIndex("Travel") == -1))
                    return null;
                else return "";
            }
            public String getDescription() {
                return("Project list to be displayed");
            }
        });
        wait(1000);
        
        /* select the categroy */
        po.selectCategory(projectCategory);
        projectListWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 180000);
        try {
            projectListWaiter.waitAction(po);
        } catch(InterruptedException e) {}
        /* Select project type */
        //po.selectProject(Bundle.getString("com.sun.rave.project.nbbridge.Bundle", "Templates/Project/Web/raveform.xml"));
        po.selectProject(projectType);
        wait(1000);
        po.next();
        wait(2000);
        if (projectName != null) {
            new JTextFieldOperator(po).setText(projectName);
        } else {
            projectName = new JTextFieldOperator(po).getText();
        }
        if (location != null) {
            if (!absoluteLocation){
                location=new JTextFieldOperator(po, 1).getText() + "/" + location;
            }
            new JTextFieldOperator(po,1).setText(location);
        } else {
            location = new JTextFieldOperator(po, 1).getText();
        }
        pathLastCreatedProject = location;
        
        wait(1000);
        po.next();
        wait(2000);
        
        po.finish();
        
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long timeoutValue = JemmyProperties.getCurrentTimeout(timeoutName);
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, 8000);
        try {
            new JButtonOperator(new JDialogOperator("Question"), "Yes").pushNoBlock();
        } catch(Exception e) {
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName,
                    timeoutValue);
        }
        
        Util.getMainWindow().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 60000);//project creation time
        //disableBrowser(projectName, true);
        
        disableBrowser(projectName, location, true);
        return projectName;
    }
    @Deprecated
    public static String createNewJSFProject() {
        return createNewJSFProject(null); // use default name
    }
    @Deprecated
    public static String createNewJSFProject(String projectName) {
        return createNewProjectLoc(null, projectName,  true,
                "Web"/*Bundle.getStringTrimmed("com.sun.rave.jsf.project.Bundle",
                "OpenIDE-Module-Display-Category")*/,
                "Web Application"/*Bundle.getStringTrimmed("com.sun.rave.jsf.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyJsf.xml")*/);
    }
    
    /**
     * Creates project with J2EE 1.4 and default source structure
     * @param location Project's location, if null then default location used
     * @param projectName Project's name, if null then default name used
     * @param absoluteLocation absolute or relative location
     * @param projectCategory Category of the project (General, Web, etc.)
     * @param projectType Type of the project of the given category
     * @return Name of the Created project
     *
     */
    public static String createNewProjectLoc(String location, String projectName,
            boolean absoluteLocation, String projectCategory, String projectType) {
        projectName = createNewProject(location, projectName, absoluteLocation, projectCategory, projectType, null, "J2EE 1.4");
        return projectName;
    }
    
    /**
     * Creates JAVA EE 5 project with default source level
     * @param location Project's location, if null then default location used
     * @param projectName Project's name, if null then default name used
     * @param absoluteLocation absolute or relative location
     * @param projectCategory Category of the project (General, Web, etc.)
     * @param projectType Type of the project of the given category
     * @return Name of the Created project
     */
    public static String createJavaEE5ProjectLoc(String location, String projectName,
            boolean absoluteLocation, String projectCategory, String projectType) {
        projectName = createNewProject(location, projectName, absoluteLocation, projectCategory, projectType, null, "Java EE 5");
        return projectName;
    }
    
    /**
     * Creating project
     * @param location Project's location, if null then default location used
     * @param projectName Project's name, if null then default name used
     * @param absoluteLocation absolute or relative location
     * @param projectCategory Category of the project (General, Web, etc.)
     * @param projectType Type of the project of the given category, i.e. Web Application
     * @param sourceStructure source structure (Java BluePrints, Jakarta), if null
     * then currently selected used
     * @param J2EELevel (Java EE 5, J2EE 1.4, J2EE 1.3), if null then currently selected used
     * @return Name of the Created project
     */
    public static String createNewProject(
            String location,
            String projectName,
            boolean absoluteLocation,
            String projectCategory,
            String projectType,
            String sourceStructure,
            String J2EELevel) {
        projectName = createNewProject(location, projectName, absoluteLocation,
                projectCategory, projectType, sourceStructure, J2EELevel, null);
        return projectName;
    }
    
    /**
     * Creating project
     * @param location Project's location, if null then default location used
     * @param projectName Project's name, if null then default name used
     * @param absoluteLocation absolute or relative location
     * @param projectCategory Category of the project (General, Web, etc.)
     * @param projectType Type of the project of the given category, i.e. Web Application
     * @param sourceStructure source structure (Java BluePrints, Jakarta), if null
     * then currently selected used
     * @param J2EELevel (Java EE 5, J2EE 1.4, J2EE 1.3), if null then currently selected used
     * @param server deployment target, if null then default application server used
     * @return Name of the Created project
     */
    public static String createNewProject(
            String location,
            String projectName,
            boolean absoluteLocation,
            String projectCategory,
            String projectType,
            String sourceStructure,
            String J2EELevel,
            String server) {
        new Operator.DefaultStringComparator(true, true);
        NewProjectWizardOperator po=NewProjectWizardOperator.invoke();
        
        //need to wait for a list containing project
        Waiter projectListWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object po) {
                JList projList =
                        (JList)((ContainerOperator)po).
                        findSubComponent(new JListOperator.JListFinder(), 1);
                JListOperator projListOper = new JListOperator(projList);
                projListOper.copyEnvironment((ComponentOperator)po);
                /*return((projListOper.findItemIndex(Bundle.getString(
                        "com.sun.rave.project.nbbridge.Bundle",
                        "Templates/Project/Web/raveform.xml")) == -1) ? null : "");*/
                //return((projListOper.findItemIndex("Application") == -1) ? null : "");
                if ((projListOper.findItemIndex("Application") == -1)&&(projListOper.findItemIndex("Travel") == -1))
                    return null;
                else return "";
            }
            public String getDescription() {
                return("Project list to be displayed");
            }
        });
        wait(1000);
        
        /* select Web categroy */
        po.selectCategory(projectCategory);
        projectListWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 180000);
        try {
            projectListWaiter.waitAction(po);
        } catch(InterruptedException e) {}
        /* Select project type */
        //po.selectProject(Bundle.getString("com.sun.rave.project.nbbridge.Bundle", "Templates/Project/Web/raveform.xml"));
        Operator.StringComparator comparator = po.getComparator();
        Operator.StringComparator stringComparator = new Operator.DefaultStringComparator(true, true);
        po.setComparator(stringComparator);
        wait(1000);
        po.selectProject(projectType);
        wait(2000);
        po.setComparator(comparator);
        wait(1000);
        
        po.next();
        wait(2000);
        if (projectName != null) {
            new JTextFieldOperator(po, 0).typeText(projectName);
        } else {
            projectName = new JTextFieldOperator(po, 0).getText();
        }
        if (location != null) {
            if (!absoluteLocation){
                location=new JTextFieldOperator(po, 2).getText() + "/" + location;
            }
            new JTextFieldOperator(po, 2).setText(location);
        } else {
            location = new JTextFieldOperator(po, 2).getText();
        }
        pathLastCreatedProject = location;
        
        //Setting source structure (Blue Prints or Jakarta)
        //if (sourceStructure!=null) {
        //   new JComboBoxOperator(po, 0).selectItem(sourceStructure);
        //}

        po.next();
        wait(2000);
        //Setting Server
        if (server != null) {
            new JComboBoxOperator(po, 0).selectItem(server);
        }
        
        wait(1000);
        //Setting level of J2EE
        if (J2EELevel != null) {
            new JComboBoxOperator(po, 1).selectItem(J2EELevel);
        }
        wait(1000);
        if (new JButtonOperator(po, "Next").isEnabled()) {
            po.next();
            wait(1000);
            //Setting "Visual Web JavaServer Faces" framework
            JTableOperator tlbFrameworks = new JTableOperator(po);
            Point VWcell = tlbFrameworks.findCell(LBL_VWJSF, 1);
            tlbFrameworks.selectCell(VWcell.y, VWcell.x);
            wait(2000);
        }
        po.finish();
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long timeoutValue = JemmyProperties.getCurrentTimeout(timeoutName);
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, 10000);
        Waiter waitProjectPropertiesDialog = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                try {
                    new JButtonOperator(new JDialogOperator("Edit Project Properties"), "Regenerate").push();
                    return null;
                }catch(TimeoutExpiredException e) {
                    return true;
                }
                
            }
            
            public String getDescription() {
                return "Wait all dialogs";
            }
        });
        waitProjectPropertiesDialog.getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        try {
            waitProjectPropertiesDialog.waitAction(null);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName,
                    timeoutValue);
        }
        /*
        try {
            for (int i = 0 ;i<5; i++) {
                new JButtonOperator(new JDialogOperator("Edit Project Properties"), "Cancel").pushNoBlock();
            }
        } catch(Exception e) {
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName,
                    timeoutValue);
        }
         */
        //wait(20000);
        //Util.getMainWindow().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 60000);//project creation time
        
        
        //new TopComponentOperator("Page1");
        disableBrowser(projectName, location, true);
        
        Waiter projectCreationWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object po) {
                try {
                    int i = 0;
                    PageTopComponentOperator page;
                    while (((String) (page = new PageTopComponentOperator("Page1", i++)).getDump().get("Tooltip text")).indexOf("web" + File.separator + "Page1.jsp") == -1);
                    new DesignerPaneOperator(page);
                    return "";
                }catch(TimeoutExpiredException e) {
                    return null;
                }
            }
            public String getDescription() {
                return("Project list to be displayed");
            }
        });
        wait(1000);
        projectCreationWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 600000);
        System.out.println("TRACE: Start waiting: "+ getCurrentDateAndTimeAsString());
        try {
            projectCreationWaiter.waitAction(null);
        }catch (Exception e) {
            //            e.printStackTrace();
            throw new JemmyException("Exception when waiting Page1 opened", e);
        }
        System.out.println("TRACE: Stop waiting: "+ getCurrentDateAndTimeAsString());
        return projectName;
    }
    
    
    public static String getPathLastCreatedProject() {
        return pathLastCreatedProject;
    }
    
    public static void openProject(String projectPath, boolean welcomeOperator){
        if(welcomeOperator)    WelcomeOperator.getWelcomeButton(Bundle.getStringTrimmed("com.sun.rave.welcome.Bundle", "LBL_OpenProject")).pushNoBlock();
        else Util.getMainMenu().pushMenuNoBlock(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File") + "|"
                + Bundle.getStringTrimmed("com.sun.rave.project.actions.Bundle", "LBL_OpenProjectAction"));
        //        String workDir = System.getProperty("scratch.dir").replace('/',File.separatorChar);
        //        workDir+=File.separatorChar+"Creator"+File.separatorChar+projectPath;
        Util.wait(3000);
        //        (new JTextFieldOperator(new JDialogOperator("Open"))).typeText(workDir);
        //        (new JButtonOperator(new JDialogOperator("Open"), "Open")).push();
        (new JTextFieldOperator(new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_PrjChooser_Title")), 1)).typeText(projectPath);
        (new JButtonOperator(new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_PrjChooser_Title")), Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "BTN_PrjChooser_ApproveButtonText"))).push();
        //        DesignerPaneOperator des = new DesignerPaneOperator(Util.getMainTab());
        //        DesignerPaneOperator des = new DesignerPaneOperator();
        Util.wait(3000);
    }
    
    public static void openProject(String projectPath){
        /*
        Util.getMainMenu().pushMenuNoBlock(Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/File")+"|"
                +Bundle.getStringTrimmed("com.sun.rave.project.actions.Bundle",
                "LBL_OpenProjectAction"));
         */
        Util.getMainMenu().pushMenuNoBlock(Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                "Menu/File")+"|"
                +"Open Project");
        
        Util.wait(3000);
        //(new JTextFieldOperator(new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_PrjChooser_Title")), 1)).typeText(projectPath);
        JDialogOperator openPrjDialog = new JDialogOperator("Open Project");
//        if (System.getProperty("os.name").equals("Mac OS X")) {
//            String[] splittedPath = projectPath.substring(1, projectPath.length()).split(File.separator);
//            TestUtils.wait(1000);
//            JComboBoxOperator cbRoot = new JComboBoxOperator(openPrjDialog);
//            for (int i = 0; i < cbRoot.getItemCount(); i++) {
//                System.out.println("cbRoot.getItemAt(i).toString()=" + cbRoot.getItemAt(i).toString());
//                TestUtils.wait(500);
//                if (cbRoot.getItemAt(i).toString().equals(File.separator)) {
//                    cbRoot.setSelectedIndex(i);
//                    break;
//                }
//            }
//            String toCompare = "";
//            for (int i = 0; i < splittedPath.length; i++) {
//                JTableOperator jtoPath = new JTableOperator(openPrjDialog);
//                toCompare += "/" + splittedPath[i];
//                System.out.println("toComapre="+toCompare);
//                TestUtils.wait(500);
//                Point cell = jtoPath.findCell(toCompare, new Operator.DefaultStringComparator(true, true), 0);
//                jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX());
//                TestUtils.wait(500);
//                if (i != splittedPath.length - 1) jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(), 2);
//                else new JButtonOperator(openPrjDialog, "Open Project Folder").pushNoBlock();
//                TestUtils.wait(500);
//            }
//        } else {
        new JTextFieldOperator(openPrjDialog, 1).setText(projectPath);
        new JButtonOperator(openPrjDialog, "Open Project").push();
//        }
        //        (new JButtonOperator(new JDialogOperator("Open"), "Open")).push();
        //(new JButtonOperator(new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_PrjChooser_Title")), Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "BTN_PrjChooser_ApproveButtonText"))).push();
        //(new JButtonOperator(openPrjDialog, "Open Project Folder")).push();
        //        DesignerPaneOperator des = new DesignerPaneOperator(Util.getMainTab());
        //        DesignerPaneOperator des = new DesignerPaneOperator();
        Util.wait(3000);
    }
    
    public static void openWebFormJavaFile(String projectName){
        //        Util.getMainMenu().pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View")+delim+Bundle.getStringTrimmed("com.sun.rave.project.Bundle", "LBL_Projects"));
        Util.getMainMenu().pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View")+delim+Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Projects"));
        final JTreeOperator tree = new JTreeOperator(new ProjectNavigatorOperator());
        //        TreePath treePath = tree.findPath(projectName, "|");
        //        tree.expandPath(treePath);
        String path = projectName + "|Java Sources|"+projectName.toLowerCase()+"|Page1.java";
        TreePath treePath = tree.findPath(path, "|");
        (new JPopupMenuOperator(tree.callPopupOnPath(treePath))).pushMenu("Open");
    }
    
    public static void closeCurrentProject(){
        try{
            Util.getMainMenu().pushMenu(
                    Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                    "Menu/File")
                    +delim
                    +CLOSE_PROJECT);
        }catch(TimeoutExpiredException e){
            System.out.println("Can't close current project. May be project doesn't exist");
            e.printStackTrace();
        }
    }
    
    public static void closeCurrentProjectSaveAll(){
        try{
            try {
                /*
                Util.getMainMenu().pushMenu(
                        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                                "Menu/File")
                        +delim
                        + CLOSE_PROJECT);
                 */
                new CloseProjectAction().perform();
            } catch (TimeoutExpiredException e) {}
            JDialogOperator saveAll = new JDialogOperator(SAVE_DIALOG_TITLE);
            new JButtonOperator(saveAll,
                    SAVE_ALL).pushNoBlock();
        }catch(TimeoutExpiredException e){
            System.out.println("Can't close current project. May be project doesn't exist");
            e.printStackTrace();
        }
    }
    
    /**
     *  Closes project <code>prjName</code> if it is currently selected
     *  @param prjName Project Name
     *  @deprecated Use closeCurrentProject or closeProjectByName instead
     */
    public static void closeProject(String prjName) {
        try{
            try {
                Util.getMainMenu().pushMenu(
                        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                        "Menu/File")
                        +delim
                        +CLOSE_PROJECT
                        +" \""+prjName+"\"");
            } catch (TimeoutExpiredException e) {}
            JDialogOperator saveAll = new JDialogOperator(SAVE_DIALOG_TITLE);
            new JButtonOperator(saveAll, SAVE_ALL).pushNoBlock();
        }catch(org.netbeans.jemmy.TimeoutExpiredException e){
            System.out.println("Can't close current project. May be project doesn't exist");
            e.printStackTrace();
        }
    }
    
    /**
     *  Closes project <code>prjName</code>. Saves all if Save Dialog is shown
     *  @param prjName Project Name
     */
    public static void closeProjectByName(String prjName) {
        
        try {
            ProjectNavigatorOperator pno = new ProjectNavigatorOperator().switchToProjects();
            pno.pressPopupItemOnNode(prjName, "Close");
        } catch (TimeoutExpiredException e) {
            throw new RuntimeException("Failed to close project", e);
        }
        
        JDialogOperator saveAll = null;
        
        try {
            saveAll = new JDialogOperator(SAVE_DIALOG_TITLE);
            new JButtonOperator(saveAll, SAVE_ALL).pushNoBlock();
        } catch (org.netbeans.jemmy.TimeoutExpiredException e) {
            if (saveAll == null) System.out.println("No Save Dialog is shown");
            else {
                System.out.println("Failed to save all");
                e.printStackTrace();
            }
        }
    }
    
    /**
     *  Checks whether project <code>prjName</code> is opened
     *  @param prjName Project Name to check
     *  @return Returns true if project name is shown in Projects tree
     */
    public static boolean isProjectOpened(String prjName) {
        ProjectNavigatorOperator pno = null;
        try {
            pno = new ProjectNavigatorOperator().switchToProjects();
            JTreeOperator jto = pno.tree();
            Timeouts timeouts = jto.getTimeouts().cloneThis();
            jto.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 3000);
            try {
                jto.findPath(prjName);
            } finally {
                jto.setTimeouts(timeouts);
            }
        } catch (TimeoutExpiredException e) {
            if (pno == null) {
                //                    e.printStackTrace();
                throw new RuntimeException("Failed to open Project Navigator", e);
            } else {
                return false;
            }
        }
        return true;
    }
    
    public static void closeProjectSaveAll(String prjName){
        try{
            try {
                /*
                Util.getMainMenu().pushMenu(
                        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                        "Menu/File") + delim
                        + CLOSE_PROJECT
                        + " \"" + prjName + "\"");
                 */
                new CloseProjectAction().perform();
            } catch (TimeoutExpiredException e) {}
            JDialogOperator saveAll = new JDialogOperator(SAVE_DIALOG_TITLE);
            new JButtonOperator(saveAll, SAVE_ALL).push();
        }catch(org.netbeans.jemmy.TimeoutExpiredException e){
            System.out.println("Can't close current project. May be project doesn't exist");
            e.printStackTrace();
        }
    }
    
    public static void closeProjectAfterSaveAll(String prjName) {
        try{
            Util.getMainMenu().pushMenu(
                    Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File") + delim
                    + SAVE_ALL);
            Util.getMainMenu().pushMenu(
                    Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File") + delim
                    +  CLOSE_PROJECT
                    +  " \"" + prjName + "\"");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void pushPopupMenuItemOnMainDesignerTab(
            DesignerPaneOperator designerOperator, String menuItem) {
        clickMainDesignerTabForPopup(designerOperator);
        JPopupMenuOperator popupMenu = new JPopupMenuOperator();
        Util.wait(1000);
        popupMenu.setComparator(new ComponentOperator.DefaultStringComparator(
                false, false));
        popupMenu.pushMenu(menuItem);
        Util.wait(2000);
    }
    
    public static void createNewProjectLoc(String location, String projectName, boolean absoluteLocation){
        
        createNewProjectLoc(location, projectName, absoluteLocation, "Web", "Visual Web Application");
    }
    
    private static void clickMainDesignerTabForPopup(
            DesignerPaneOperator designerOperator) {
        TabbedAdapter tabbedAdapter = getTabbedAdapter(designerOperator);
        
        int selectedIndex = tabbedAdapter.getSelectionModel().getSelectedIndex();
        Rectangle tabRect = tabbedAdapter.getTabBounds(selectedIndex);
        int clickX = tabRect.x + (tabRect.width / 2),
                clickY = tabRect.y + (tabRect.height / 2);
        
        ComponentOperator componentOperator = new ComponentOperator((Component)tabbedAdapter);
        MouseDriver mouseDriver = new MouseRobotDriver(new Timeout("", 300));
        mouseDriver.clickMouse(componentOperator, clickX, clickY, 1,
                InputEvent.BUTTON3_MASK, 0, new Timeout("", 10));
        Util.wait(1000);
    }
    
    public static String getCurrentProjectName() {
        // JSF-project is used by default
        return getCurrentProjectName(KEY_STRING_AFTER_PRJ_NAME_JSF);
    }
    
    public static String getCurrentProjectName(String keyStringAfterPrjName) {
        DesignerPaneOperator designerOperator = new DesignerPaneOperator();
        TopComponent topComponent = getSelectedTopComponent(designerOperator);
        String toolTip = topComponent.getToolTipText();
        
        System.out.println();
        System.out.println("+++ TopComconent's tooltip = [" + toolTip + "]");
        
        String prjName = toolTip.replace(File.separatorChar, '/');
        int substrIndex = prjName.lastIndexOf("/" + keyStringAfterPrjName);
        prjName = prjName.substring(0, substrIndex);
        
        substrIndex = prjName.lastIndexOf("/");
        prjName = prjName.substring(substrIndex + 1);
        
        System.out.println("+++ Current project name = " + prjName);
        System.out.println();
        
        return prjName;
    }
    
    public static boolean isMainTabOfDesignerSelected(
            DesignerPaneOperator designerOperator, String tabTitle) {
        TopComponent topComponent = getSelectedTopComponent(designerOperator);
        String name = getTopComponentName(topComponent).trim();
        
        System.out.println("+++ Tab Title = [" + tabTitle + "]");
        System.out.println("+++ TopComconent name = [" + name + "]");
        System.out.println();
        
        return (name.equals(tabTitle.trim()));
    }
    
    private static TabbedAdapter getTabbedAdapter(ContainerOperator containerOperator) {
        if (containerOperator == null) return null;
        Container container = containerOperator.getParent();
        while (container != null) {
            if (container instanceof TabbedAdapter) {
                System.out.println();
                System.out.println("+++ Tabbed Adapter found = " + container);
                System.out.println();
                return ((TabbedAdapter) container);
            }
            container = container.getParent();
        }
        return null;
    }
    
    public static TopComponent[] getTabTopComponents(ContainerOperator containerOperator) {
        TabbedAdapter tabbedAdapter = getTabbedAdapter(containerOperator);
        if (tabbedAdapter == null) {
            return null;
        }
        TopComponent[] topComponents = tabbedAdapter.getTopComponents();
        return topComponents;
    }
    
    public static TopComponent getTabSelectedTopComponent(ContainerOperator containerOperator) {
        TabbedAdapter tabbedAdapter = getTabbedAdapter(containerOperator);
        if (tabbedAdapter == null) {
            return null;
        }
        int selectedIndex = tabbedAdapter.getSelectionModel().getSelectedIndex();
        TopComponent topComponent = tabbedAdapter.getSelectedTopComponent();
        return topComponent;
    }
    
    private static TopComponent getSelectedTopComponent(DesignerPaneOperator designerOperator) {
        TopComponent topComponent = getTabSelectedTopComponent(designerOperator);
        
        System.out.println();
        System.out.println("+++ TopComconent found = " + topComponent);
        System.out.println();
        return topComponent;
    }
    
    public static String getTopComponentName(TopComponent topComponent) {
        String name = topComponent.getDisplayName();
        if ((name == null) || (name.length() < 1)) {
            name = topComponent.getName();
        }
        return name;
    }
    
    public static boolean checkProjectBuilded(){
        BuildOutputOperator console = new BuildOutputOperator();
        if((console.getTextOutput()).indexOf("BUILD SUCCESSFUL")!=-1) return true;
        return false;
    }
    
    public static void wait(int millisec){
        Util.wait(millisec);
        //System.out.println("sleep to "+millisec/1000+" seconds");
        //try { Thread.sleep(millisec); } catch(Exception e) {}
    }
    
    public static void waitStatusText(String line) {
        String[] variants = {line};
        StatusWaitable csw = new StatusWaitable(variants, false);
        String result = waitStatus(csw);
        if(result!=null){
            System.out.println("Error while waiting status text: "+result);
        }
    }
    
    public static String waitStatus(StatusWaitable csw){
        Waiter w = new Waiter(csw);
        try {
            w.waitAction(csw);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return("Exception while waiting status text:" + e);
        } catch (TimeoutExpiredException e1) {
            e1.printStackTrace();
            return("Exception while waiting status text:" + e1);
        }
        return null;
    }
    
    public static void waitDeployed(OutputOperator output, int maxTimeout) throws InterruptedException {
        Waiter deploymentWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object output) {
                String text = ((OutputOperator)output).getText();
                if (text.indexOf("BUILD FAILED")!=-1)
                    throw new RuntimeException(new InterruptedException("BUILD FAILED String found in Output Window"));
                if (text.indexOf("BUILD SUCCESSFUL")!=-1)
                    return "true";
                return null;
                
            }
            public String getDescription() {
                return("Waiting Project Deployed");
            }
        });
        //Waiter deploymentWaiter = new Waiter(new WaitDeploymentCompleted());
        deploymentWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", maxTimeout);
        deploymentWaiter.waitAction(output);
        output.close();
    }
    
    /** getting coords of selected component from "style" property
     *
     *
     */
    
    public static Point getComponentCoords() {
        PropertySheetOperator pso = new PropertySheetOperator(Util.getMainWindow());
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
        psto.setComparator(new Operator.DefaultStringComparator(true, true));
        Property pr = new Property(psto, "style");
        String propValue = pr.getValue();
        
        
        int xLoc = new Integer(propValue.substring(propValue.indexOf("left: ") + 6, propValue.indexOf("px", propValue.indexOf("left: ")))).intValue();
        int yLoc = new Integer(propValue.substring(propValue.indexOf("top: ") + 5, propValue.indexOf("px", propValue.indexOf("top: ")))).intValue();
        
        return new Point(xLoc, yLoc);
    }
    
    /** getting current Date and Time as one string ( YYYYMMDDHHMMSS )
     *
     *
     */
    public static String getCurrentDateAndTimeAsString(){
        return (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
    }
    
    /** export WAR file
     *
     *
     */
    public static void exportWAR(String projectName, String pathToWAR, boolean platform){
        //        ProjectNavigatorOperator.pressPopupItemOnNode("ROOT", "Export WAR File");
        ProjectNavigatorOperator.pressPopupItemOnNode(projectName, "Export WAR File");
        wait(2000);
        JDialogOperator newWF = new JDialogOperator("Export WAR");
        new JTextFieldOperator(newWF, 0).setText(pathToWAR+fSep+projectName+".war");
        wait(1000);
        new JRadioButtonOperator(newWF, platform?0:1).setSelected(true);
        wait(1000);
        new JButtonOperator(newWF, "OK").pushNoBlock();
        newWF.waitClosed();
        wait(2000);
        long oldWaitTime = Util.getMainWindow().getTimeouts().getTimeout("DialogWaiter.WaitDialogTimeout");
        try {
            Util.getMainWindow().getTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", 5000);
            JDialogOperator owdo = new JDialogOperator("Question");
            new JButtonOperator(owdo, "Yes").pushNoBlock();
            Util.getMainWindow().getTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", oldWaitTime);
        } catch(Exception e) {
            Util.getMainWindow().getTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", oldWaitTime);
        }
        System.out.println("WAR File exported in "+pathToWAR+fSep+projectName+".war");
        while (new JLabelOperator(Util.getMainWindow(), 0).getText().indexOf("Finished") == -1 &&
                new JLabelOperator(Util.getMainWindow(), 0).getText().indexOf("(export.war)") == -1) {}
        wait(2000);
    }
    
    public static void exportWAR(String projectName, String pathToWAR){
        exportWAR(projectName, pathToWAR, true);
    }
    
    /**
     * Prints list of all child components of container
     * @param op - container
     */
    public static void printComponentList(ComponentOperator op) {
        Component cmp=op.getSource();
        if (cmp instanceof Container){
            printComponentList((Container)cmp);
        }else {
            System.out.println("Not a container");
        }
    }
    
    public static void printComponentList(Container container) {
        printComponentList(container, 0);
    }
    
    private static void printComponentList(Container container, int tabCount) {
        printComponentList(System.out, container, tabCount);
    }
    
    public static void printComponentList(String fileName, Container container) {
        PrintStream printOut = null;
        try {
            printOut = new PrintStream(new FileOutputStream(fileName, true));
            printComponentList(printOut, container, 0);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                printOut.flush();
                printOut.close();
            } catch(Exception e) {e.printStackTrace();}
        }
    }
    
    private static void printComponentList(PrintStream out, Container container,
            int tabCount) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            
            out.println();
            out.println();
            for (int j = 0; j < tabCount; j++) {
                out.print("\t");
            }
            out.print("+++ COMPONENT = " + component);
            out.println();
            // print next level
            if (component instanceof Container) {
                printComponentList(out, (Container) component, tabCount + 1);
            }
        }
    }
    
    private static final String
            CREATOR_SYSTEM_DIR = "visualweb1" + File.separator + "config",
            CREATOR_INSTALL_PROPERTIES_FILE_NAME = "com-sun-rave-install.properties";
    //            CREATOR_SYSTEM_DIR = "system",
    //            CREATOR_INSTALL_PROPERTIES_FILE_NAME = "install.properties";
    
    private static final String
            HTTP_PROTOCOL = "http",
            WEB_PORT_PROPERTY_NAME = "webPort",
            ADMIN_PORT_PROPERTY_NAME = "adminPort",
            LOCALHOST="localhost";
    
    public static URL getJ2EEApplicationURL(String projectName) {
        String webPort = getWebPortFromProperties(),
                unsensitiveProjectName = projectName.toLowerCase();
        try {
            return (new URL(HTTP_PROTOCOL, LOCALHOST,
                    Integer.parseInt(webPort), File.separator + unsensitiveProjectName));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getWebPortFromProperties() {
        Properties installProperties = getCreatorInstallProperties();
        return ((installProperties == null) ? null :
            installProperties.getProperty(WEB_PORT_PROPERTY_NAME));
    }
    
    public static String getAdminPortFromProperties() {
        Properties installProperties = getCreatorInstallProperties();
        return ((installProperties == null) ? null :
            installProperties.getProperty(ADMIN_PORT_PROPERTY_NAME));
    }
    
    public static Properties getCreatorInstallProperties() {
        // ak153254: Fixed the name of installation properties file due to the fact
        // that netbeans.dest.dir property doesn't exist any more
        String installPropertiesFileName = System.getProperty("netbeans.home") +
                File.separator + ".." + File.separator + CREATOR_SYSTEM_DIR +
                File.separator + CREATOR_INSTALL_PROPERTIES_FILE_NAME;
        try {
            Properties installProperties = new Properties();
            installProperties.load(new FileInputStream(installPropertiesFileName));
            return installProperties;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void outMsg(String msg) {
        outMsg(msg, true, true);
    }
    
    public static void outMsg(String msg, boolean spaceBefore, boolean spaceAfter) {
        outMsg(System.out, msg, spaceBefore, spaceAfter);
    }
    
    public static void outMsg(String fileName, String msg) {
        outMsg(fileName, msg, true, true);
    }
    
    public static void outMsg(String fileName, String msg, boolean spaceBefore,
            boolean spaceAfter) {
        PrintStream printOut = null;
        try {
            printOut = new PrintStream(new FileOutputStream(fileName, true));
            outMsg(printOut, msg, spaceBefore, spaceAfter);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                printOut.flush();
                printOut.close();
            } catch(Exception e) {e.printStackTrace();}
        }
    }
    
    private static void outMsg(PrintStream out, String msg, boolean spaceBefore,
            boolean spaceAfter) {
        if (spaceBefore) {
            out.println();
        }
        out.println(msg);
        if (spaceAfter) {
            out.println();
        }
    }
    
//    public static boolean isServerStarted() {
//        String adminPort = "24848";//getAdminPortFromProperties();
//        System.out.println("adminPort = " + adminPort);
//        return isServerStarted(LOCALHOST, adminPort);
//    }
//    
//    public static boolean isServerStarted(String host, String port) {
//        return isServerStarted(host, port, null, null);
//    }
//    
//    public static boolean isServerStarted(String host, String port,
//            String userName, String userPassword) {
//        try {
//            WebConversation conversation = new WebConversation();
//            HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
//            HttpUnitOptions.setExceptionsThrownOnScriptError(false);
//            if ((userName != null) && (userPassword != null)) {
//                conversation.setAuthorization(userName, userPassword);
//            }
//            String url = HTTP_PROTOCOL + "://" + host + ":" + port;
//            try {
//                WebResponse response = conversation.getResponse(url);
//            } catch(java.io.EOFException e) {
//                // workaround: handle an exception from Web Server 6.1
//                outMsg("+++ Method [isServerStarted(...)]: an exception [" + e +
//                        "] after attempt to receive a response " +
//                        "from server [" + url + "]");
//                e.printStackTrace();
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//            System.out.println("Exception in HTTP check : " + t);
//            return false;
//        }
//        return true;
//    }
    
    public static String getRequestPrefix(long timeout) {
        Waiter waiter = new Waiter(new Waitable() {
            public Object actionProduced(Object p) {
                String _requestPrefix = "";
                _requestPrefix = System.getProperty("test.netbeans.j2eeserver.clienturl");
                if(_requestPrefix != null &&
                        _requestPrefix.length() > 0) {
                    return(_requestPrefix);
                } else {
                    return(null);
                }
            }
            public String getDescription() {
                return("URL to be set");
            }
        });
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
        try {
            return((String)waiter.waitAction(null));
        } catch(java.lang.InterruptedException e) {
            return("");
        }
    }
    
    public static void disableBrowser(String prjName, boolean dis) {
        ProjectNavigatorOperator.pressPopupItemOnNode(prjName, "Properties");
        JDialogOperator propertiesDialog = new JDialogOperator("Project Properties - "+prjName);
        wait(1000);
        JTreeOperator tree = new JTreeOperator(propertiesDialog);
        tree.selectPath(tree.findPath("Run"));
        wait(2000);
        JCheckBoxOperator checkBox = new JCheckBoxOperator(propertiesDialog);
        checkBox.setSelected(!dis);
        wait(1000);
        new JButtonOperator(propertiesDialog, "OK").pushNoBlock();
        wait(1000);
    }
    
    public static void disableBrowser(String prjName, String prjLoc, boolean dis) {
        final String propFileStr = prjLoc+File.separator + prjName + File.separator+"nbproject"
                +File.separator+"project.properties";
        FileInputStream propIn = null;
        
        //need to wait property file to be created
        Waiter propertyFileWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object path) {
                File file = new File((String)path);
                if (file.exists()) {
                    return file;
                } else return null;
            }
            public String getDescription() {
                return("\"" + propFileStr + "\" property file to be created");
            }
        });
        
        propertyFileWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 20000);
        File propFile = null;
        try {
            propFile  = (File)propertyFileWaiter.waitAction(propFileStr);
        } catch(InterruptedException e) {}
        
        
        wait(1000);
        try {
            Properties prop = new Properties();
            wait(1000);
            propIn = new FileInputStream(propFile);
            wait(1000);
            prop.load(propIn);
            //wait(2000);
            Boolean newVal = new Boolean(!dis);
            prop.setProperty("display.browser", newVal.toString());
            //System.out.println("TRACE        Property set : display.browser = "+prop.getProperty("display.browser"));
            //wait(2000);
            prop.store(new FileOutputStream(propFileStr), null);
            //wait(5000);
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occured "+ e);
        }
    }
    
    public static JMenuItem findPopupMenuItemByLabel(JPopupMenuOperator menuOperator,
            String menuItemLabel) {
        return findPopupMenuItemByLabel(menuOperator, menuItemLabel, true, false);
    }
    
    public static JMenuItem findPopupMenuItemByLabel(JPopupMenuOperator menuOperator,
            String menuItemLabel, boolean equalsMode, boolean ignoreCaseMode) {
        String labelText = (ignoreCaseMode ? menuItemLabel.toUpperCase() : menuItemLabel);
        
        MenuElement[] menuItems = menuOperator.getSubElements();
        for (int i = 0; i < menuItems.length; i++) {
            JMenuItem menuItem = (JMenuItem) menuItems[i];
            String menuItemText = (ignoreCaseMode ?
                menuItem.getText().toUpperCase() : menuItem.getText());
            if (equalsMode) {
                if (menuItemText.equals(labelText)) {
                    return menuItem;
                }
            } else {
                if (menuItemText.indexOf(labelText) > -1) {
                    return menuItem;
                }
            }
        }
        return null;
    }
    /**
     * @param destinationUrl - url with zip file
     * @param srcFile - file in which downloaded file will be saved
     */
    
    public static void downloadFile(String destinationUrl, String srcFile)
            throws MalformedInputException, IOException {
        
        // try {
        // Create an URL instance
        URL url = new URL(destinationUrl);
        
        // Get an input stream for reading
        InputStream in = url.openStream();
        
        // Create a buffered input stream for efficency
        BufferedInputStream bufIn = new BufferedInputStream(in);
        OutputStream outStr = new FileOutputStream(srcFile);
        
        // Repeat until end of file
        for (;;) {
            int data = bufIn.read();
            
            // Check for EOF
            if (data == -1)
                break;
            else
                outStr.write(data);
            //System.out.print ( (char) data);
        }
        outStr.close();
        /*
        } catch(MalformedURLException mue) {
            System.err.println("Invalid URL");
            System.out.println("Invalid URL :" + mue);
            fail("Invalid URL");
        } catch (IOException ioe) {
            System.err.println("I/O Error - " + ioe);
            fail("I/O Error - " + ioe);
        }
         */
    }
    
    public static String unzip(String file) throws IOException {
        String projectName;
        Enumeration entries;
        ZipFile zipFile;
        //try {
        zipFile = new ZipFile(file);
        entries = zipFile.entries();
        System.out.println("Extracting from: " + zipFile.getName());
        //TODO:
        String firstEntry = ((ZipEntry)zipFile.entries().nextElement()).getName();
        System.out.println("FIRST ENTRY = " + firstEntry);
        String startDir = firstEntry;
        if (firstEntry.indexOf('/') != -1) {
            startDir = firstEntry.substring(0, firstEntry.indexOf('/'));
            System.out.println("dir = " + startDir);
            (new File(startDir)).mkdir();
        }
        projectName = startDir;
        //_projectPath = (new File(startDir)).getAbsolutePath();
        while(entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            
            
            if(entry.isDirectory()) {
                // Assume directories are stored parents first then children.
                System.out.println("Extracting directory: " + entry.getName());
                // This is not robust, just for demonstration purposes.
                (new File(entry.getName())).mkdir();
                //continue;
            } else {
                System.out.println("Extracting file: " + entry.getName());
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(entry.getName())));
            }
            
        }
        zipFile.close();
        return projectName;
            /*
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            fail();
        }
             */
    }
    protected static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        
        while((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        
        in.close();
        out.close();
    }
    
    /**
     * Parse logs for exceptions
     *
     */
    public static String parseLogs(String pathToFile, String strToFind) throws IOException {
        String lineSep = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(new FileReader(pathToFile));
        String nextLine = "";
        StringBuffer sb = new StringBuffer();
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.indexOf(strToFind)!=-1) {
                sb.append(nextLine);
                sb.append(lineSep);
            }
        }
        return sb.toString();
    }
    
    public static void setHTTPProxy(String proxyHost, String proxyPort) {
        OptionsOperator opOper = OptionsOperator.invoke();
        opOper.selectOption("General Settings|System Settings");
        SheetTableOperator props = new SheetTableOperator(opOper);
        props.setComboBoxValue("Type of proxy configuration", "Use HTTP Proxy");
        props.setTextValue("Proxy Host", proxyHost);
        props.setTextValue("Proxy Port", proxyPort);
        opOper.close();
    }
}
