/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;

import gui.Utilities;

import org.netbeans.performance.test.guitracker.ActionTracker;

import javax.swing.JMenuItem;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

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
    
    private TopComponentOperator editor;
    
    private static final int repeat_original = Integer.getInteger("org.netbeans.performance.repeat", 1).intValue(); // initialize original value
    
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
        testSubMenu("org.netbeans.core.Bundle","Menu/File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_RecentProjectsAction_Name");
    }
    
    //TODO open more than one project (nice to have open 10 projects)
    public void testFileSetMainProjectMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/File", "org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetMainProjectAction_Name");
    }

    public void testViewDocumentationIndicesMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/View", "org.netbeans.modules.javadoc.search.Bundle", "CTL_INDICES_MenuItem");
    }
    
    public void testViewCodeFoldsMenu(){
        editor = Utilities.openSmallJavaFile();
        waitNoEvent(5000);
        testSubMenu("org.netbeans.core.Bundle","Menu/View", "org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
    }
    
    public void testViewEditorsMenu(){
        editor = Utilities.openSmallFormFile();
        waitNoEvent(10000);
        testSubMenu("org.netbeans.core.Bundle","Menu/View", "org.netbeans.core.multiview.Bundle", "CTL_EditorsAction");
    }
    
    public void testViewToolbarsMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/View", "org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction");
    }
    
    public void testRunStackMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject", "Stack"); // this can't be localized
    }
    
    public void testRunRunFileMenu(){
        testSubMenu("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject", "org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    }
    
    public void testVersLocalHistoryMenu() {
        testSubMenu("org.netbeans.modules.versioning.Bundle","Menu/Versioning", "org.netbeans.modules.localhistory.Bundle", "CTL_MainMenuItem");
    }

    public void testVersSubversionMenu() {
        testSubMenu("org.netbeans.modules.versioning.Bundle","Menu/Versioning", "org.netbeans.modules.subversion.Bundle", "CTL_Subversion_MainMenu");
    }
        
    public void testToolsJavaDBMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Tools", " org.netbeans.modules.derby.Bundle", "LBL_DerbyDatabase");
    }
    
    public void testToolsI18nMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Tools", "org.netbeans.modules.i18n.Bundle", "LBL_I18nGroupActionName");
    }
    
    public void testWinDebuggingMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Window", "org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
    }
    
    public void testWinVersioningMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Window", "org.netbeans.modules.versioning.Bundle","Menu/Window/Versioning");
    }

    public void testWinProfilingMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Window", "org.netbeans.modules.profiler.actions.Bundle","Menu/Window/Profile");
    }
    
    public void testHelpTutorials(){
        testSubMenu("org.netbeans.core.Bundle", "Menu/Help", " org.netbeans.modules.url.Bundle", "Menu/Help/Tutorials");
    }

    public void testHelpJavadoc(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Help", "org.netbeans.modules.javadoc.search.Bundle", "CTL_INDICES_MenuItem");
    }
    
    
    private void testSubMenu(String mainMenu, String subMenu){
        mainMenuPath = mainMenu;
        subMenuPath = subMenu;
        doMeasurement();
    }
    
    private void testSubMenu(String bundle, String mainMenu, String subMenu) {
        testSubMenu(getFromBundle(bundle,mainMenu),subMenu);
    }
    
    private void testSubMenu(String bundle, String mainMenu, String bundle_2, String subMenu) {
        testSubMenu(getFromBundle(bundle,mainMenu),getFromBundle(bundle_2,subMenu));
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
    }
    
    public void shutdown() {
        if(editor != null){
            editor.close();
            editor=null;
        }
    }
    
    public void setUp () {
        super.setUp();
        repeat = 1; // only first use is interesting
    }
    
    public void tearDown() {
        super.tearDown();
        repeat = repeat_original; // initialize original value
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new MainSubMenus("testRunRunFileMenu"));
    }
    
}
