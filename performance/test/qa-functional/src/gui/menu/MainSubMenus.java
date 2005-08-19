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

package gui.menu;

import gui.Utilities;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.performance.test.guitracker.ActionTracker;

import javax.swing.JMenuItem;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.drivers.MouseDriver;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test of application main menu.</p>
 * <p>Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 * @author Radim Kubacki, mmirilovic@netbeans.org
 */
public class MainSubMenus extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected static String mainMenuPath;
    protected static JMenuOperator testedMainMenu;
    protected static String subMenuPath;
    
    private EditorOperator editor;
    
    /** Creates a new instance of MainSubMenus */
    public MainSubMenus(String testName) {
        super(testName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
        track_mouse_event = ActionTracker.TRACK_MOUSE_MOVED;
    }
    
    
    /** Creates a new instance of MainSubMenus */
    public MainSubMenus(String testName, String performanceDataName) {
        this(testName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
        track_mouse_event = ActionTracker.TRACK_MOUSE_MOVED;
        setTestCaseName(testName, performanceDataName);
    }
    
    //TODO open more than one project (nice to have open 10 projects) and close 5 projects
    public void testFileOpenRecentProjectMenu(){
        testSubMenu("org.netbeans.core.Bundle","File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_RecentProjectsAction_Name");
    }
    
    //TODO open more than one project (nice to have open 10 projects)
    public void testFileSetMainProjectMenu(){
        testSubMenu("org.netbeans.core.Bundle","File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetMainProjectAction_Name");
    }

    //TODO open form file
    public void testViewEditorsMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.core.multiview.Bundle", "CTL_EditorsAction");
    }
    
    public void testViewCodeFoldsMenu(){
        editor = Utilities.openJavaFile();
        waitNoEvent(5000);
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
    }
    
    public void testViewDocumentationIndicesMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.modules.javadoc.search.Bundle", "CTL_INDICES_MenuItem");
    }
    
    public void testViewToolbarsMenu(){
        testSubMenu("org.netbeans.core.Bundle","View", "org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction");
    }
    
    public void testRunStackMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "RunProject", "Stack"); // this can't be localized
    }
    
    public void testRunRunOtherMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "RunProject", "org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    }
    
    public void testToolsI18nMenu(){
        testSubMenu("org.netbeans.core.Bundle","Tools", "org.netbeans.modules.i18n.Bundle", "LBL_I18nGroupActionName");
    }
    
    public void testWinGuiMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.modules.form.resources.Bundle", "Menu/Window/Form");
    }
    
    public void testWinDebuggingMenu(){
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    }
    
    public void testWinSelectDocumentNodeInMenu(){
        editor = Utilities.openJavaFile();
        waitNoEvent(5000);
        testSubMenu("org.netbeans.core.Bundle","Window", "org.netbeans.core.ui.resources.Bundle", "Menu/Window/SelectDocumentNode");
    }
    
    private void testSubMenu(String mainMenu, String subMenu){
        mainMenuPath = mainMenu;
        subMenuPath = subMenu;
        repeat = 1; // only first use is interesting
        doMeasurement();
        repeat = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue(); // initialize original value
    }
    
    private void testSubMenu(String bundle, String mainMenu, String subMenu) {
        testSubMenu(getFromBundle(bundle,"Menu/"+mainMenu),subMenu);
    }
    
    private void testSubMenu(String bundle, String mainMenu, String bundle_2, String subMenu) {
        testSubMenu(getFromBundle(bundle,"Menu/"+mainMenu),getFromBundle(bundle_2,subMenu));
    }
    
    private String getFromBundle(String bundle, String key){
        return org.netbeans.jellytools.Bundle.getStringTrimmed(bundle,key);
    }
    
    private JMenuItemOperator mio;
    private MouseDriver mdriver;
    
    public void prepare(){
        MainWindowOperator.getDefault().menuBar().pushMenu(mainMenuPath,"|");
        testedMainMenu = new JMenuOperator(MainWindowOperator.getDefault());

        JMenuItem submenu = testedMainMenu.findJMenuItem(testedMainMenu.getContainers()[0], subMenuPath, false, false);
        assertNotNull("Can not find "+subMenuPath+" menu item in "+mainMenuPath+" menu", submenu);
        mio = new JMenuItemOperator (submenu);
        
        mdriver = org.netbeans.jemmy.drivers.DriverManager.getMouseDriver(mio);
    }
    
    public ComponentOperator open(){
        mdriver.moveMouse(mio, mio.getCenterXForClick(), mio.getCenterYForClick());
//        mio.pushKey(java.awt.event.KeyEvent.VK_RIGHT);
        return mio;
    }
    
    public void close() {
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        testedComponentOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        
        if(editor != null)
            editor.close();
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new MainSubMenus("testWinSelectDocumentNodeInMenu"));
    }
    
}
