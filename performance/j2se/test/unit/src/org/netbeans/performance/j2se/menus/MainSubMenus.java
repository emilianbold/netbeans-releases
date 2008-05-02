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

package org.netbeans.performance.j2se.menus;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
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
public class MainSubMenus extends PerformanceTestCase {
    
    protected static String mainMenuPath;
    protected static JMenuOperator testedMainMenu;
    protected static String subMenuPath;
    
    private TopComponentOperator editor;
    
    private JMenuItemOperator mio;
    private MouseDriver mdriver;
    
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
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance", "Main20kB.java", true);
        waitNoEvent(5000);
        testSubMenu("org.netbeans.core.Bundle","Menu/View", "org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
    }
    
    public void testViewEditorsMenu(){
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance", "Main20kB.java", true);
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
        
    public void testToolsPaletteMenu(){
        testSubMenu("org.netbeans.core.Bundle","Menu/Tools", "org.netbeans.modules.palette.Bundle", "CTL_PaletteAction");
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
        testSubMenu("org.netbeans.core.Bundle", "Menu/Help", "org.netbeans.modules.url.Bundle", "Menu/Help/Tutorials");
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
