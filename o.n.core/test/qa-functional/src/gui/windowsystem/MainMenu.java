/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.windowsystem;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
        
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.RenameAction;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.junit.NbTestSuite;



/**
 *
 * @author mmirilovic@netbeans.org
 */
public class MainMenu extends JellyTestCase {
    
    protected PrintStream err;
    protected PrintStream log;
    
    private static ProjectsTabOperator projects ;
    
    private static final String coreBundle = "org.netbeans.core.Bundle";
    private static final String openideActionsBundle = "org.openide.actions.Bundle";
    
    private static final String testProject_name="SampleProject";
    
    private static final String ideConfiguration = fromBundle(coreBundle, "UI/Services/IDEConfiguration"); // IDE Configuration
    private static final String lookAndFeel = fromBundle(coreBundle, "UI/Services/IDEConfiguration/LookAndFeel"); // Look and Feel
    private static final String menuBar = fromBundle(coreBundle, "Menu"); // Menu Bar
    private static final String toolbars = fromBundle(coreBundle, "Toolbars"); // Toolbars

    private static String lookAndFeelNodeOptions = ideConfiguration + "|" + lookAndFeel;
    private static String menuBarNodeOptionsPath = lookAndFeelNodeOptions + "|" + menuBar;
    private static String toolbarsNodeOptions = lookAndFeelNodeOptions + "|" + toolbars;
    private static String separator = fromBundle(coreBundle, "CTL_newMenuSeparator"); // Separator        
    
    private static String versioning = fromBundleNotTrimmed("org.netbeans.modules.vcscore.actions.Bundle",  "Menu/Versioning");
    private static String versioningMenuBarOptionsNodePath = menuBarNodeOptionsPath + "|" + versioning;
    
    private static final String MOVE_UP = "Move Up";
    private static final String MOVE_DOWN = "Move Down";
    private static final String SEPARATOR = "("+separator+")";
    
    private JMenuBar mainMenu;
    
    private static OptionsOperator options = null;
    
    public MainMenu(java.lang.String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        
        suite.addTest(new MainMenu("testMainMenuMnemonicsCollision"));
        suite.addTest(new MainMenu("testMainMenuShortCutCollision"));
        
        suite.addTest(new MainMenu("testMenuItemsWhenProjectSelected"));
        suite.addTest(new MainMenu("testMenuItemsWhenSourcePackagesSelected"));
        suite.addTest(new MainMenu("testMenuItemsWhenPackageSelected"));
        suite.addTest(new MainMenu("testMenuItemsWhenJavaSelected"));
        suite.addTest(new MainMenu("testMenuItemsWhenFormSelected"));
        
        suite.addTest(new MainMenu("testPopupMenuItemsWhenProjectSelected"));
        suite.addTest(new MainMenu("testPopupMenuItemsWhenSourcePackagesSelected"));
        suite.addTest(new MainMenu("testPopupMenuItemsWhenPackageSelected"));
        suite.addTest(new MainMenu("testPopupMenuItemsWhenJavaSelected"));
        suite.addTest(new MainMenu("testPopupMenuItemsWhenFormSelected"));
//
//        suite.addTest(new MainMenu("testAddingNewSeparator"));
//        suite.addTest(new MainMenu("testMoveUpDownSeparator"));
//        suite.addTest(new MainMenu("testDeletingSeparator"));
//        //suite.addTest(new MainMenu("testCutCopyPasteSeparator"));
//
//        suite.addTest(new MainMenu("testAddNewMenu"));
//        suite.addTest(new MainMenu("testChangeOrderOfMenu"));
//        //suite.addTest(new MainMenu("testCutCopyPasteMenuItem"));
//        suite.addTest(new MainMenu("testDeleteMenuItem"));
//        suite.addTest(new MainMenu("testDeleteMenu"));
//
//        suite.addTest(new MainMenu("testRenameMenu"));
//        
//        suite.addTest(new MainMenu("testCreateNewToolbar"));
//        suite.addTest(new MainMenu("testRenameToolbar"));
        
        return suite;
    }
    
    public void setUp() {
        //err = System.out;
        err = getLog();
        log = getRef();
        
        try {
            // set defaults
            JemmyProperties.getProperties().setOutput(new TestOut(null, new PrintWriter(err, true), new PrintWriter(err, true), null));
            mainMenu = MainWindowOperator.getDefault().getJMenuBar();
            projects = new ProjectsTabOperator();
        }catch(Exception exc) {
            failTest(exc, "Fail setUp() - maybe MainFrame hasn't menubar");
        }
        
    }

    public void testMainMenuMnemonicsCollision() {
        String collisions = MenuChecker.checkMnemonicCollision();
        assertFalse(collisions, collisions.length()>0);
    }
    

    public void testMainMenuShortCutCollision() {
        String collisions = MenuChecker.checkShortCutCollision();
        assertFalse(collisions, collisions.length()>0);
    }
    
    
    public void testMenuItemsWhenProjectSelected() {
        selectNodeAndCheckMainMenu(projects.getProjectRootNode(testProject_name));
    }
    
    public void testMenuItemsWhenSourcePackagesSelected() {
        selectNodeAndCheckMainMenu(new SourcePackagesNode(testProject_name));
    }
    
    public void testMenuItemsWhenPackageSelected() {
        selectNodeAndCheckMainMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject"));
    }
    
    public void testMenuItemsWhenJavaSelected() {
        selectNodeAndCheckMainMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject|JavaFile.java"));
    }
    
    public void testMenuItemsWhenFormSelected() {
        selectNodeAndCheckMainMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject|FormFile.java"));
    }
    
   
    public void testPopupMenuItemsWhenProjectSelected() {
        selectNodeAndCheckPopupMenu(projects.getProjectRootNode(testProject_name));
    }
    
    public void testPopupMenuItemsWhenSourcePackagesSelected() {
        selectNodeAndCheckPopupMenu(new SourcePackagesNode(testProject_name));
    }
    
    public void testPopupMenuItemsWhenPackageSelected() {
        selectNodeAndCheckPopupMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject"));
    }
    
    public void testPopupMenuItemsWhenJavaSelected() {
        selectNodeAndCheckPopupMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject|JavaFile.java"));
    }
    
    public void testPopupMenuItemsWhenFormSelected() {
        selectNodeAndCheckPopupMenu(new Node(new SourcePackagesNode(testProject_name), "sampleproject|FormFile.java"));
    }
    
    
    public void testAddingNewSeparator(){
        new EventTool().waitNoEvent(10);
        
        try {
            openOptions();
            Node versioningMenuBarOptionsNode = new Node(options.treeTable().tree(), versioningMenuBarOptionsNodePath);
            
            versioningMenuBarOptionsNode.performPopupAction(fromBundle(openideActionsBundle, "New")+"|"+separator);
            new EventTool().waitNoEvent(1000);
            
            //check
            options.selectOption(versioningMenuBarOptionsNodePath+"|"+SEPARATOR);
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
    }
    
    public void testMoveUpDownSeparator() {
        new EventTool().waitNoEvent(10);
        String initialState, finalState;
        
        try {
            openOptions();

            String fileNodePath = menuBarNodeOptionsPath + "|" + fromBundleNotTrimmed(coreBundle,  "Menu/File");
            
            initialState =  printChildrenInTreePath(options.treeTable().tree(), fileNodePath, "|");
            
            makeLog(initialState, "Initial State");
            
            Node moveNode = new Node(options.treeTable().tree(), fileNodePath + "|" + SEPARATOR);
            
            moveNode.performPopupAction(MOVE_DOWN);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_UP);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_DOWN);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_DOWN);
            new EventTool().waitNoEvent(1000);
            
            moveNode.performPopupAction(MOVE_UP);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_UP);
            new EventTool().waitNoEvent(200);
            
            finalState = printChildrenInTreePath(options.treeTable().tree(), fileNodePath, "|");
            makeLog(finalState, "Final State");
            
            if(initialState.compareTo(finalState)!=0){
                throw new JemmyException("Initial State of menu {"+fileNodePath+"} isn't the same after change order items operations");
            }
            
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
        
    }
    
    public void testDeletingSeparator(){
        String confirmDeletionTitle = fromBundle("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle"); // Confirm Object Deletion
        deleteNodeInOptions(SEPARATOR, versioningMenuBarOptionsNodePath, confirmDeletionTitle);
    }
    
    
    public void testAddNewMenu(){
        new EventTool().waitNoEvent(10);
        
        try {
            openOptions();
            
            Node menuBarOptionsNode = new Node(options.treeTable().tree(), menuBarNodeOptionsPath);
            
            menuBarOptionsNode.performPopupActionNoBlock("Add Menu"); // NOI18N
            
            new NbDialogOperator(fromBundle(coreBundle, "CTL_newMenuDialog")).ok();
            new EventTool().waitNoEvent(200);
            
            //check
            options.selectOption(menuBarNodeOptionsPath + "|" + fromBundle(coreBundle, "CTL_newMenu"));
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
        
    }
    
    public void testChangeOrderOfMenu(){
        new EventTool().waitNoEvent(100);
        
        try {
            File ref = new File(this.getWorkDir(), "InfoAboutMenu.check");
            File golden = new File(this.getWorkDir(), "InfoAboutMenu.gold");
            
            openOptions();
            
            String moveNodePath = menuBarNodeOptionsPath + "|" + fromBundleNotTrimmed(coreBundle,  "Menu/File");
            Node moveNode = new Node(options.treeTable().tree(), moveNodePath);
            
            new PrintStream(new FileOutputStream(golden)).println(printChildrenInTreePath(options.treeTable().tree(), menuBarNodeOptionsPath,"|"));
            err.println("\n ======= BEFORE : \n " + printChildrenInTreePath(options.treeTable().tree(), menuBarNodeOptionsPath,"|") + " \n =============");
            
            moveNode.performPopupAction(MOVE_DOWN);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_UP);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_DOWN);
            new EventTool().waitNoEvent(200);
            
            moveNode.performPopupAction(MOVE_UP);
            new EventTool().waitNoEvent(200);

            new PrintStream(new FileOutputStream(ref)).println(printChildrenInTreePath(options.treeTable().tree(), menuBarNodeOptionsPath,"|"));
            err.println("\n ======= AFTER : \n " + printChildrenInTreePath(options.treeTable().tree(), menuBarNodeOptionsPath,"|") + " \n =============");
        
            assertFile("Menu before and after isn't the same and it should be",ref, golden, this.getWorkDir());
            
            //log(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath,options.delim));
            //log.println(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath,options.delim));
            
            //compareReferenceFiles();
            
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
        
    }

    
    
    public void testDeleteMenuItem() {
        String file = fromBundleNotTrimmed(coreBundle,  "Menu/File");
        String confirmDeletionTitle = fromBundle("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle"); // Confirm Object Deletion
        deleteNodeInOptions("Pa&ge Setup", menuBarNodeOptionsPath + "|" + file, confirmDeletionTitle); //NOI18N
    }
    
    
    public void testDeleteMenu() {
        String confirmDeletionTitle = fromBundle("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle"); // Confirm Object Deletion
        String menuXX = fromBundle(coreBundle, "CTL_newMenuName"); // Menu
        deleteNodeInOptions(menuXX, menuBarNodeOptionsPath, confirmDeletionTitle);
    }

    
    public void testRenameMenu() {
        String build = fromBundleNotTrimmed(coreBundle,  "Menu/Build"); // &Build
        renameNodeInOptions(menuBarNodeOptionsPath, build, "TestMenu");
    }
    
    public void testCreateNewToolbar() {
        new EventTool().waitNoEvent(100);
        
        try {
            openOptions();

            new Node(options.treeTable().tree(), toolbarsNodeOptions).performPopupActionNoBlock("Add Toolbar"); //NOI18N

            new NbDialogOperator("New Toolbar").ok(); //NOI18N
            new EventTool().waitNoEvent(200);
            
            //check
            options.selectOption(toolbarsNodeOptions + "|Toolbar");
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
    }
    
    public void testRenameToolbar() {
        String build = fromBundleNotTrimmed(coreBundle,  "Toolbars/Build"); // Build
        renameNodeInOptions(toolbarsNodeOptions, build, "TestBuild");
    }

    
    private void renameNodeInOptions(String parentNodeName, String oldNodeName, String newNodeName) {
            new EventTool().waitNoEvent(100);
        
        try {
            openOptions();
            
            Node renamedNode = new Node(options.treeTable().tree(), parentNodeName + "|" + oldNodeName);
            new RenameAction().performPopup(renamedNode);
            
            String rename = fromBundle(openideActionsBundle, "CTL_RenameTitle"); // Rename
            NbDialogOperator renameDialog = new NbDialogOperator(rename);
            JTextFieldOperator dialogTextField = new JTextFieldOperator(renameDialog,0);
            dialogTextField.setText(newNodeName);
            String realNewName = dialogTextField.getText();
            renameDialog.ok();
            
            log("=========================== New name is {" + realNewName + "}.");
            
            new EventTool().waitNoEvent(300);
            
            options.selectOption(parentNodeName + "|" + realNewName);            
        
        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }
    }
    
    
    private void deleteNodeInOptions(String nodeName, String parentNode, String confirmationDialogTitle) {
        boolean ariseJemmyException = false;
        String deleteNodePath = parentNode + "|" + nodeName;
        
        try {
            openOptions();
            
            Node deleteNode = new Node(options.treeTable().tree(), deleteNodePath);
            new DeleteAction().performPopup(deleteNode);
            
            closeConfirmDialog(confirmationDialogTitle);
            
            // node needn't be there, JemmyException have to arise !!!
            try {
                options.selectOption(deleteNodePath);
            } catch(JemmyException ex) {//ok
                ariseJemmyException = true;
            }
        
            if(!ariseJemmyException)
                fail("Nonexpected node {" + nodeName + "} is under node {" + deleteNode + "}.");

        } catch(Exception exc) {
            failTest(exc, "Exception: "+exc.getMessage()+" arises.");
        }

    }
    
    
    protected void selectNodeAndCheckMainMenu(Node nodeToSelect) {
        err.println("=========== try to select node ["+nodeToSelect.getPath()+"].");
        
        nodeToSelect.select();
        
        new EventTool().waitNoEvent(100);
        MenuChecker.visitMenuBar(mainMenu);

        String file = fromBundle(coreBundle, "Menu/File"); // File
        String edit = fromBundle(coreBundle, "Menu/Edit"); // Edit
        String build = fromBundle(coreBundle, "Menu/Build"); // Build
        
        MenuChecker.printMenuBarStructure(mainMenu, log, file+", "+edit+", "+build, true, true);
        
        compareReferenceFiles();
    }
    
    protected void selectNodeAndCheckPopupMenu(Node nodeToSelect) {
        err.println("=========== try to select node ["+nodeToSelect.getPath()+"].");
        
        nodeToSelect.select();
        
        JPopupMenuOperator popupOperator = nodeToSelect.callPopup();
        new EventTool().waitNoEvent(500);
        JPopupMenu popup = (JPopupMenu)popupOperator.getSource();
        
        String tools = fromBundle(coreBundle, "Menu/Tools"); // Tools
        MenuChecker.printPopupMenuStructure(popup, log, tools, true, true);
        //MenuChecker.printPopupMenuStructure(popup, System.out, "Tools", true, true);
        
        if(popupOperator!=null)
            popupOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        
        //popup.setVisible(false);
        
        compareReferenceFiles();
    }
    
    protected String printChildrenInTreePath(JTreeOperator operator, String path, String delim) {
        StringBuffer buffer = new StringBuffer("");
        
        operator.expandPath(operator.findPath(path, delim));
        javax.swing.tree.TreePath children[] = operator.getChildPaths(operator.findPath(path, delim));
        
        for(int i=0;i<children.length;i++) {
            buffer.append("\n"+children[i].toString());
        }
        
        return buffer.toString();
    }

    
    private void closeConfirmDialog(String title) {
        new NbDialogOperator(title).yes();
        new EventTool().waitNoEvent(1000);
    }

    
    private void makeLog(String logString, String logTitle) {
        log("---------- " + logTitle + " >>>>>>>");
        log(logString);
        log(">>>>>>>>>> " + logTitle + " -------");
    }

    /** Print full stack trace to log files, get message and log to test results if test fails.
     * @param exc Exception logged to description
     * @param message written to test results
     */    
    private void failTest(Exception exc, String message) {
        try{
            getWorkDir();
            org.netbeans.jemmy.util.PNGEncoder.captureScreen(getWorkDirPath()+System.getProperty("file.separator")+"IDEscreenshot.png");
        }catch(Exception ioexc){
            log("Impossible make IDE screenshot!!! \n" + ioexc.toString());
        }
        
        err.println("################################");
        exc.printStackTrace(err);
        err.println("################################");
        fail(message);
    }
    
/*
    public void testCutCopyPasteSeparator()  throws java.io.IOException {
        err.println("=========================== "+getName()+"  #######");
        Options.show();
        Options options = Options.find();
        String delim = JettyTreePropertiesWindow.delim;
 
        options.pushPopupMenu("Copy",
                              menuBarNodeOptionsPath + options.delim +"&File"+delim+SEPARATOR);
        options.pushPopupMenu("Paste|Copy",
                              menuBarNodeOptionsPath + options.delim +"&Edit");
        options.pushPopupMenu("Cut",
                              menuBarNodeOptionsPath + options.delim +"&File"+delim+SEPARATOR);
        options.pushPopupMenu("Paste",
                               menuBarNodeOptionsPath + options.delim +"&Edit");
        //capture Option
        WindowSystemSupport.makeWindowScreenshot(this, options.getComponent(), "Options");
        //print to ref
        ref("---File---");
        log(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath + options.delim +"&File", options.delim));
        ref("---Edit---");
        log(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath + options.delim +"&Edit", options.delim));
        compareReferenceFiles();
    }
 */
/*
    public void testCutCopyPasteMenuItem() throws java.io.IOException {
        err.println("=========================== "+getName()+"  #######");
        Options.show();
        Options options = Options.find();
 
        options.pushPopupMenu("Copy",new String[]{menuBarNodeOptionsPath + options.delim + "&View" + options.delim + SEPARATOR, menuBarNodeOptionsPath + options.delim + "&View" + options.delim + "&Properties"});
        options.pushPopupMenu("Paste|Copy", menuBarNodeOptionsPath + options.delim + "Menu");
        options.pushPopupMenu("Cut", menuBarNodeOptionsPath + options.delim + "&View" + options.delim + "&Explorer");
        options.pushPopupMenu("Paste", menuBarNodeOptionsPath + options.delim + "Menu");
        //capture Option
        WindowSystemSupport.makeWindowScreenshot(this, options.getComponent(), "Options");
        //print to ref
        ref("---View---");
        log(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath + options.delim + "&View", log, options.delim));
        ref("---Menu---");
        log(printChildrenInTreePath(options.getJTreeOperator(), menuBarNodeOptionsPath + options.delim + "Menu", log, options.delim));
        compareReferenceFiles();
    }
 */
    
    private static void openOptions(){
        if (options == null) {
            options = OptionsOperator.invoke();
            new EventTool().waitNoEvent(500);
        }
        
    }
    
    private static String fromBundle(String bundle, String key) {
        return org.netbeans.jellytools.Bundle.getStringTrimmed(bundle, key);
    }
    
    private static String fromBundleNotTrimmed(String bundle, String key) {
        return org.netbeans.jellytools.Bundle.getString(bundle, key);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
