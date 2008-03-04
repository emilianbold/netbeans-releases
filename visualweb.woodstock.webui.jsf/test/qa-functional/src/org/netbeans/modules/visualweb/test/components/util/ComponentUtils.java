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

/*
 * ComponentUtils.java
 *
 * Created on June 20, 2006, 9:57 AM
 *
 * Common code used in component testcases.
 */

package  org.netbeans.modules.visualweb.test.components.util;


import java.io.ByteArrayInputStream;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.modules.visualweb.gravy.DocumentOutlineOperator;
import org.netbeans.modules.visualweb.gravy.ProjectNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.navigation.NavigatorOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.gravy.model.IDE;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.jemmy.drivers.text.SwingTextKeyboardDriver;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import javax.swing.JList;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.visualweb.gravy.actions.ActionNoBlock;
import org.netbeans.jellytools.WizardOperator;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.tree.TreePath;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.modules.visualweb.gravy.Bundle;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.gravy.NbDialogOperator;
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;

/**
 *
 * @author Sherry Zhou
 * @author Lark Fitzgerald
 */
public class ComponentUtils {
    
    public static String _bundle = getBundle();
    
    public static String _projects = Bundle.getStringTrimmed(_bundle,"projects");
    public static String _projectType = Bundle.getStringTrimmed(_bundle,"projectType");
    public static String _projectServer = Bundle.getStringTrimmed(_bundle,"projectServer");
    public static String _projectServerLocation = Bundle.getStringTrimmed(_bundle,"projectServerLocation");
    public static String _projectServerPassword = Bundle.getStringTrimmed(_bundle,"projectServerPassword");
    public static String _projectSourceLevel = Bundle.getStringTrimmed(_bundle,"projectSourceLevel");
    public static String _projectJ2EEVersion = Bundle.getStringTrimmed(_bundle,"projectJ2EEVersion");
//    public static String _projectSourceStructure = Bundle.getStringTrimmed(_bundle,"projectSourceStructure");
    public static String _projectCategory = System.getProperty("xtest.project.category");
    
    public static String _dataBindingMenu = Bundle.getStringTrimmed(_bundle, "Designer_Menu_DataBinding");
    public static String _dataBindingDialog = Bundle.getStringTrimmed(_bundle, "Dialog_DataBinding");
    public static String _dataProviderTab = Bundle.getStringTrimmed(_bundle, "DataProviderTab");
    public static String _objectTab = Bundle.getStringTrimmed(_bundle, "ObjectTab");
    public static String _okButton = Bundle.getStringTrimmed(_bundle, "Button_OK");
    public static String _applyButton = Bundle.getStringTrimmed(_bundle, "Button_Apply");
    public static String _closeButton = Bundle.getStringTrimmed(_bundle, "Button_Close");
    public static ActionNoBlock srv;
    private static String pathLastCreatedProject;
    
    /** Creates a new instance of Util */
    public ComponentUtils() {
    }
    
    /**
     * Types multi-line text into a JTextComponent.
     * @param text the lines
     * @param comp the component operator
     */
    public static void typeLines(String text, JTextComponentOperator comp) {
        String[] lines = text.split("\n", -1);
        for (String line : lines) {
            if (!"".equals(line)) {
                comp.typeText(line);
            }
            comp.pressKey(KeyEvent.VK_ENTER);
        }
    }
    
    /**
     * Selects a component in the Navigator view specified by string <code>path</code>
     * @param path string path delimited by |
     */
    public static void selectComponentByPath(String path) {
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        JTreeOperator jto = doo.getStructTreeOperator();
        new Node(jto, path).select();
     }

    /**
     * Selects a component in the Navigator view specified by relative path to
     * <code>Page1|page1|html1|body1|form1|</code> component.
     * @param a relative path delimited by |
     */
    public static void selectForm1Component(String name) {
        /*TODO should be replaced by selectComponent(String name) which find the component
          by it's name somwhere in the tree */
        DocumentOutlineOperator doo = new DocumentOutlineOperator(Util.getMainWindow());
        JTreeOperator jto = doo.getStructTreeOperator();
        selectComponentByPath("Page1|page1|html1|body1|form1|"+name);
     }
    
    /**
     * Reads a property list (key and element pairs) from the String.
     * @param src properties string in format described at
     * <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/Properties.html#load(java.io.InputStream
     * Properties.load(java.io.InputStream)</a> method.
     * @return the <code>Properties</code> object holding the properties represented by the string argument
     */
    public static Properties parseProperties(String src) {
        Properties properties =  new Properties();
        try {
            properties.load(new ByteArrayInputStream(src.getBytes("ISO-8859-1")));
        } catch (IOException ex) {
            new JemmyException("Failed to parse properties:\n"+src, ex);
        }
        return properties;
    }
    
    /*
     * Get bundle property file based on commandline option j2ee.version
     * if j2ee.version=Java EE 5, return ComponentJEE5.properties
     * otherwise return Component.properties
     */
    public static String getBundle() {
        /*String j2eeVersion=System.getProperty("j2ee.version");
         
        if (j2eeVersion.equals("JEE5"))
            return "org.netbeans.modules.visualweb.test.components.ComponentJEE5";
        else */
        return "org.netbeans.modules.visualweb.test.components.Component";
        
    }
    
    
     /*
      *  get project path
      */
    public static String getProjectPath(String projectName){
        String basePath = System.getProperty("xtest.workdir") ;
        //String basePath = System.getProperty("xtest.sketchpad") ;
        String projectPath = basePath + File.separator + "projects";
        File folder = new File(projectPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return projectPath;
    }
    
    
     /*
      * set component's property via property sheet
      */
    public static void setProperty(SheetTableOperator sheet, String propertyName, String propertyValue){
        int row = sheet.findCellRow(propertyName);
        sheet.clickForEdit(row, 1);
        sheet.clickForEdit(row, 1);
        new JTextComponentOperator(sheet).enterText(propertyValue);
//        sheet.clickForEdit(sheet.findCell(propertyName, 2).y, 1);
//        sheet.clickForEdit(sheet.findCell(propertyName, 2).y, 1);
//        new JTextComponentOperator(sheet).enterText(propertyValue);
    }
    
    /*
     * return runtime data directory
     */
    public static String getDataDir() {
        String path = System.getProperty("xtest.workdir") + File.separator +
                "sys" + File.separator + "tests" + File.separator +
                "components" + File.separator + "classes" + File.separator +
                "com" + File.separator + "sun" + File.separator +
                "rave" + File.separator + "components" + File.separator + "data" + File.separator ;
        return path;
    }
    
    /*
     * Add bean property from project window
     * @objName Application Bean, Session Bean, or Request Bean
     * @propertyName Property Name
     * @propertyType Property type
     * @propertyMode Property mode
     *
     */
    
    public static void addObjectProperty(String objectPath,  String propertyName, String propertyType, String propertyMode) {
        ProjectNavigatorOperator prjNav =  ProjectNavigatorOperator.showProjectNavigator();
        
        String  popupMenuItem = "Add|Property";
        prjNav.selectNode(objectPath);
        Util.wait(2000);
        prjNav.pressPopupItemOnNode(objectPath, popupMenuItem);
        prjNav.selectNode(objectPath);
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed(_bundle, "Dialog_BeanPattern_Title"));
        Util.wait(500);
        JTextFieldOperator tf_Name = new JTextFieldOperator(dialog, 0);
        tf_Name.setText(propertyName);
        Util.wait(500);
        JTextFieldOperator tf_Type = new JTextFieldOperator(dialog, 1);
        tf_Type.setText(propertyType);
//        JComboBoxOperator cmbx_Type = new JComboBoxOperator(dialog, 0);
//        cmbx_Type.setSelectedItem(propertyType);
        Util.wait(1000);
        JComboBoxOperator cmbx_Mode = new JComboBoxOperator(dialog, 1);
        cmbx_Mode.setSelectedItem(propertyMode);
        Util.wait(1000);
        
        new JButtonOperator(dialog, _okButton).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
        Util.saveAllAPICall();
    }
    
    /*
     * Connect sample database
     */
    public static void connectDB(String dbName, String passwd) {
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree =  explorer.getTree();
        tree.callPopupOnPath(tree.findPath(dbName));
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed(_bundle, "Database_Menu_Connect"));
//        JDialogOperator dialog = new JDialogOperator("Connect");
//        Util.wait(1000);
//        JTextFieldOperator tf_Name = new JTextFieldOperator(dialog, 1);
//        tf_Name.setText(passwd);
//        Util.wait(1000);
//        new JButtonOperator(dialog, "OK").pushNoBlock();
//        dialog.waitClosed();
        TestUtils.wait(15000);
    }
    
    /*
     * Bind component to specific data provider
     */
    public static void bindToDataProvider(int x, int y, String dbTable, String dataProviderName, String displayField, String valueField) {
        //D&D database table to component
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree =  explorer.getTree();
        TreePath path=tree.findPath(dbTable);
        TestUtils.wait(5000);
        tree.clickOnPath(path);
        TestUtils.wait(1000);
        tree.selectPath(path);
        TestUtils.wait(1000);
        
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(x , y , 1);
        TestUtils.wait(10000);
        
        designer.requestFocus();
        designer.clickForPopup(x , y);
        
        //Bring up Data binding dialog. Setup data provider binding
        JPopupMenuOperator popup = null;
        popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock(_dataBindingMenu);
        
        JDialogOperator dataBind = new JDialogOperator(_dataBindingDialog);
        // new JTabbedPaneOperator(dataBind).selectPage(_dataProviderTab);
        TestUtils.wait(8000);
        // Place cursor to PERSON.NAME in value field
        new JTabbedPaneOperator(dataBind).selectPage(_dataProviderTab);
        TestUtils.wait(1000);
        
        //Select data provider
        JComboBoxOperator comb = new JComboBoxOperator(dataBind, 0);
        comb.setSelectedItem(dataProviderName);
        Util.wait(1000);
        
        //Set Display field and Value field
        
        if (valueField != null) {
            JListOperator valueList = new JListOperator(dataBind, 0);
            TestUtils.wait(1000);
            valueList.selectItem(valueField);
            TestUtils.wait(1000);
        }
        
        if (displayField != null) {
            JListOperator displayList = new JListOperator(dataBind, 1);
            TestUtils.wait(1000);
            displayList.selectItem(displayField);
            TestUtils.wait(1000);
        }
        
        new JButtonOperator(dataBind, _okButton).pushNoBlock();
    }
    
    /*
     * Bind component to specific data provider 
     * Can be used for Table component where Name and Value fields do not need to be specified.
     * This will bind the component to the DB table with default settings.
     */
    public static void bindToDataProvider(int x, int y, String dbTable) {
        //D&D database table to component
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        JTreeOperator tree =  explorer.getTree();
        TreePath path=tree.findPath(dbTable);
        TestUtils.wait(5000);
        tree.clickOnPath(path);
        TestUtils.wait(1000);
        tree.selectPath(path);
        TestUtils.wait(1000);
        
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(x , y , 1);
        TestUtils.wait(20000);
        
    }
    
     /*
      * Bind component to an object property
      */
    public static void bindToObject(int x, int y, String propertyPath) {
        //Open Bind to Data dialog vai context menu
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickMouse(x , y , 1);
        TestUtils.wait(1000);
        designer.requestFocus();
        designer.clickForPopup(x , y);
        JPopupMenuOperator popup = null;
        popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock(_dataBindingMenu);
        Util.wait(1000);
        JDialogOperator dialog = new JDialogOperator(_dataBindingDialog);
        Util.wait(1000);
        
        // Select object to bind
        new JTabbedPaneOperator(dialog).selectPage(_objectTab);
        TestUtils.wait(1000);
        TestUtils.printComponentList(dialog);
        JTreeOperator tree = new JTreeOperator(dialog);
        TestUtils.printComponentList(tree);
        tree.selectPath(tree.findPath(propertyPath));
        Util.wait(1000);
        
        new JButtonOperator(dialog, _applyButton).pushNoBlock();
        Util.wait(1000);
        
        new JButtonOperator(dialog, _okButton).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
    }
    
    
    
   /* public static void bindToObject(int x, int y,  String[] treeNodeTexts) {
    
        DesignerPaneOperator designer = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        designer.clickForPopup(x, y);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    
        JPopupMenuOperator popupMenu = new JPopupMenuOperator();
        popupMenu.pushMenuNoBlock(_dataBindingMenu);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    
        JDialogOperator dialog = new JDialogOperator(_dataBindingDialog);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    
        JTreeOperator treeOperator = new JTreeOperator(dialog);
        JTree tree = (JTree) treeOperator.getSource();
        TreeModel treeModel = tree.getModel();
        TreeNode rootNode = (TreeNode) treeModel.getRoot();
    
        Object[] treeNodePaths = new Object[treeNodeTexts.length + 1];
        treeNodePaths[0] = rootNode;
        TreeNode parentNode = rootNode;
        for (int i = 0; i < treeNodeTexts.length; ++i) {
            TestUtils.outMsg("+++ tree level = " + i);
            TreeNode treeNode = findBindingTreeNode(parentNode, treeNodeTexts[i]);
            treeNodePaths[i + 1] = treeNode;
            parentNode = treeNode;
        }
        List treeNodeList = new ArrayList();
        for (int i = 0; i < treeNodePaths.length; ++i) {
            Object treeNode = treeNodePaths[i];
            treeNodeList.add(treeNode);
            TreePath treePath = new TreePath(treeNodeList.toArray());
            tree.expandPath(treePath);
            Util.wait(500);
        }
        tree.getSelectionModel().setSelectionPath(new TreePath(treeNodePaths));
        Util.wait(1000);
        new QueueTool().waitEmpty();
    
        new JButtonOperator(dialog, _applyButton).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    
        new JButtonOperator(dialog, _okButton).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static TreeNode findBindingTreeNode(TreeNode parentNode, String treeNodeText) {
        TreeNode treeNode = null;
        int childCount = parentNode.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            treeNode = (TreeNode) parentNode.getChildAt(i);
            TestUtils.outMsg("+++ tree node class = " + treeNode.getClass().getName());
            try {
                // find and invoke the method public String getBindingExpressionPart() on a tree node
                Method method = treeNode.getClass().getMethod("getBindingExpressionPart", null);
                Object methodInvocationResult = method.invoke(treeNode, new Object[] {});
                String text = methodInvocationResult.toString();
                TestUtils.outMsg("+++ treeNodeText = " + text);
                if (treeNodeText.equals(text)) {
                    return treeNode;
                }
            } catch(Throwable t) {
                JemmyException e = new JemmyException("Problem with a method of a tree node class", t);
                e.printStackTrace();
                if ((t instanceof NoSuchMethodException) &&
                        ((treeNode.getClass().getName().equals("com.sun.rave.propertyeditors.binding.nodes.ContextTargetNode"))
                        ||
                        (treeNode.getClass().getName().equals("com.sun.rave.propertyeditors.binding.nodes.PropertyTargetNode")))) {
                    throw e;
                }
            }
        }
        return null;
    }*/
    
    /*
     * set component's property via property bindling dialog
     * @designer
     * @component Component ID
     * @x The component xcoordinate
     * @y The component y coordinate
     * @property Property name
     * @value Prtoperty value
     */
    public static void setPropertyBinding(DesignerPaneOperator designer,  int x, int y, String property, String value) {
        designer.clickForPopup(x, y);
        Util.wait(500);
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed(_bundle, "Designer_Menu_PropertyBindings"));
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed(_bundle, "Dialog_PropertyBindings" ));
        JListOperator list = new JListOperator(dialog, 0);
        list.selectItem(property);
        Util.wait(1000);
        JTextFieldOperator tf_Name = new JTextFieldOperator(dialog, 1);
        tf_Name.setText(value);
        Util.wait(1000);
        new JButtonOperator(dialog, _applyButton).pushNoBlock();
        Util.wait(1000);
        new JButtonOperator(dialog, _closeButton).pushNoBlock();
        dialog.waitClosed();
        Util.wait(1000);
    }
    
    public static void loadImageFile(String imageFile) {
        // Select menu File>Add existing items>image
        String menuItem=Bundle.getStringTrimmed(_bundle, "Menu_File") + "|"
                + Bundle.getStringTrimmed(_bundle, "Menu_AddExistingItem") + "|"
                + Bundle.getStringTrimmed(_bundle, "Menu_ImageFile");
        
        Util.getMainMenu().pushMenuNoBlock(menuItem);
        JDialogOperator dialog = new JDialogOperator(Bundle.getStringTrimmed(_bundle, "Dialog_AddExistingItem"));
        (new JTextFieldOperator(dialog, 0)).typeText(imageFile);
        TestUtils.wait(1000);
        (new JButtonOperator(dialog, Bundle.getStringTrimmed(_bundle, "Button_Add"))).pushNoBlock();
    }
    
    public static void setButtonTextInLine(DesignerPaneOperator designer, int x, int y, String text) {
        // click component to get inline mode
        TestUtils.printComponentList(new RaveWindowOperator());
        designer.clickMouse(x , y , 1);
        TestUtils.wait(1500);
        designer.clickMouse(x , y , 1);
        TestUtils.wait(1500);
        //Enter the text
        JTextFieldOperator field = new JTextFieldOperator(designer);
        SwingTextKeyboardDriver keyboard = new SwingTextKeyboardDriver();
        keyboard.enterText(field, text);
        TestUtils.wait(100);
    }
    
    /*
     * Create navigation link
     */
    public static void linkWebPages(DesignerPaneOperator designer, String startPage, String endPage, String linkName) {
        // Start Page Navigation Editor via designer context menu
        designer.clickForPopup(1, 1 );
        Util.wait(500);
        new JPopupMenuOperator().pushMenuNoBlock(Bundle.getStringTrimmed(_bundle, "Designer_Menu_PageNavigation"));
        
        NavigatorOperator navigation = new NavigatorOperator();
        Util.wait(500);
        navigation.linkUsingXmlSource(startPage+".jsp", endPage+".jsp", linkName);
        TestUtils.wait(500);
    }
    
    public static void insertJavaCode(EditorOperator editor, String[] javaCodeLines) {
        for (int i = 0; i < javaCodeLines.length; ++i) {
            editor.insert(javaCodeLines[i]);
            Util.wait(1000);
            
            if (i < javaCodeLines.length - 1) {
                editor.pushKey(KeyEvent.VK_ENTER);
                Util.wait(1000);
                
            }
        }
    }
    
    /*
     * Create New Project
     */
    public static void createNewProject(String _projectName) {
        String _projectPath=getProjectPath(_projectName);
      //  createNewProjectLoc(_projectPath, _projectName, true, _projectType, _projectCategory, _projectServer, _projectServerLocation, _projectServerPassword, _projectSourceLevel, _projectJ2EEVersion, _projectSourceStructure);
         createProject(_projectName);
       

        // Wait until designer appear
        TestUtils.wait(50000);
    }
    
    /* 
     * Use the method in gravy. similar to sanity tests
     */
    public static void createProject(String _projectName) {
        // Add appserver
         IDE.getIDE();
         
         // Create a project
         String _projectPath=getProjectPath(_projectName);
         if (_projectJ2EEVersion.equals("Java EE 5")) {
            // Create Java EE 5 project
            TestUtils.createJavaEE5ProjectLoc(_projectPath, _projectName, true, _projectType, _projectCategory);
        } else {
            // Create J2EE 1.4 project
            TestUtils.createNewProjectLoc(_projectPath, _projectName, true, _projectType, _projectCategory);
        }
         
        //Bring up palette. Workaround for issue http://www.netbeans.org/issues/show_bug.cgi?id=105200
        PaletteContainerOperator.showPalette();
        TestUtils.wait(1000);
    }
    
    /*
     * Create New Project & Configure Web Server
     * This method should be temporary until alexey fixes the
     * original utilities.
     */
    public static String createNewProjectLoc(String location, String projectName,
            boolean absoluteLocation, String projectCategory, String projectType,
            String projectServer, String projectServerLocation, String projectServerPassword,
            String projectSourceLevel, String projectJ2EEVersion, String projectSourceStructure) {
        NewProjectWizardOperator po=NewProjectWizardOperator.invoke();
        
        //need to wait for a list containing project
        Waiter projectListWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object po) {
                JList projList =
                        (JList)((ContainerOperator)po).
                        findSubComponent(new JListOperator.JListFinder(), 1);
                JListOperator projListOper = new JListOperator(projList);
                projListOper.copyEnvironment((ComponentOperator)po);
                /*return((projListOper.findItemIndex(_bundle.getString(
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
        TestUtils.wait(1000);
        
        /* select Web categroy */
        po.selectCategory(projectCategory);
        projectListWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", 180000);
        try {
            projectListWaiter.waitAction(po);
        } catch(InterruptedException e) {}
        
        /* Select project type */
        //po.selectProject(_bundle.getString("com.sun.rave.project.nbbridge.Bundle", "Templates/Project/Web/raveform.xml"));
        po.selectProject(projectType);
        TestUtils.wait(1000);
        po.next();
        TestUtils.wait(2000);
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
        
        //Configure projectServer if not default (Tomcat (5.5.17)
//        if (projectServer == "Sun Java System Application Server") {
        JButtonOperator manage = new JButtonOperator(po, "Manage...");
        manage.press();
        manage.release();
        configureSunServer(projectServerLocation, projectServerPassword);
/*
            //Set project Source Structure
            JLabelOperator sourceStructure = new JLabelOperator(po, "Source Structure:");
            java.awt.Component c = sourceStructure.getLabelFor();
            JComboBox cb = (JComboBox)c;
            JComboBoxOperator structure = new JComboBoxOperator(cb);
            structure.selectItem(projectSourceStructure);
 */
        JLabelOperator server = new JLabelOperator(po, "Server:");
        java.awt.Component c = server.getLabelFor();
        JComboBox cb = (JComboBox)c;
        JComboBoxOperator sunServer = new JComboBoxOperator(cb);
        sunServer.selectItem(projectServer);
//        }
        
        //Set J2EE Version
        JLabelOperator j2eeVersion = new JLabelOperator(po, "Java EE Version:");
        c = j2eeVersion.getLabelFor();
        cb = (JComboBox)c;
        JComboBoxOperator version = new JComboBoxOperator(cb);
        version.selectItem(projectJ2EEVersion);
        
        //Set Source Level to 1.4
        
        //Set as Main Project
        
        if (new JButtonOperator(po, "Next").isEnabled()) {
            po.next();
            //TODO clicked on Down button and then Brake Space pressed to select needed checkbox
            po.pushKey(KeyEvent.VK_DOWN);
            TestUtils.wait(2000);
            po.pushKey(KeyEvent.VK_SPACE);
            TestUtils.wait(2000);
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

        //wait(20000);
        Util.getMainWindow().getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 60000);//project creation time
//        new TopComponentOperator("Page1");
//        TestUtils.disableBrowser(projectName, location, true);
        return projectName;
    }
    
    /*
     * Configure Sun One Application Server
     */
    public static void configureSunServer(String projectServerLocation, String projectServerPassword) {
        NbDialogOperator serverManager = new NbDialogOperator("Servers");
        JButtonOperator addServer = new JButtonOperator(serverManager, "Add Server...");
        addServer.pushNoBlock();
        
        WizardOperator serverInstance = new WizardOperator("Add Server Instance");
        serverInstance.next();
        JButtonOperator browse = new JButtonOperator(serverInstance, "Browse...");
        browse.press();
        browse.release();
        
        NbDialogOperator installLocation = new NbDialogOperator("Choose Application Server's Install Location");
        //There's only 1 textField on page so this works.
        JTextFieldOperator fileName = new JTextFieldOperator(installLocation);
        fileName.enterText(projectServerLocation);
        JButtonOperator choose = new JButtonOperator(installLocation, "Choose");
        choose.pushNoBlock();
        try { Thread.sleep(2000); } catch(Exception e) {}
        
        serverInstance.next();
        try { Thread.sleep(2000); } catch(Exception e) {}
        JLabelOperator la = new JLabelOperator(serverInstance, "Admin Password:");
        java.awt.Component c = la.getLabelFor();
        JTextField jtf = (JTextField)c;
        JTextFieldOperator adminPassword = new JTextFieldOperator(jtf);
        adminPassword.setText(projectServerPassword);
        serverInstance.finish();
        
        JButtonOperator close = new JButtonOperator(serverManager, "Close");
        close.pushNoBlock();
    }
    
    /*
     * Used to check the ide log file for exceptions.
     * This method was taken from the sanity test and modified to return a string.
     */
        public static String hasUnexpectedException() throws IOException {
        String[] knownException={"import javax.faces.FacesException",
                                  "java.lang.IllegalStateException",
                                  "java.lang.IllegalArgumentException: Expected scheme-specific part at index", 
                                  "java.lang.IllegalArgumentException: Cannot get BASE revision,", 
                                  "java.net.UnknownHostException: www.netbeans.org",
                                  "org.netbeans.modules.uihandler.exceptionreporter"};
 
        
        String logFile = System.getProperty("xtest.workdir") +
                File.separator +
                Bundle.getStringTrimmed(_bundle,"LogFile");
        String lineSep = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        String nextLine = "";
        boolean isUnexpectedException;
        
        //Get lines that has word "Exceptions" from message.log
        String exceptions=TestUtils.parseLogs(logFile, "Exception");
        if (exceptions.equals("")) {
            return ""; //No exceptions found
        } else {
//            log("Exceptions found in message.log:\n"+exceptions);         
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
                return "Unexpected exceptions: \n"+sb.toString();
            } else
                return "";
        }
    }
}


