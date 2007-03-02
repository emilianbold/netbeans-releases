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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;

import gui.Utilities;

import org.netbeans.performance.test.guitracker.ActionTracker;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.EditorOperator;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * Performance test of application main menu.</p>
 * <p>Each test method reads the label of tested menu and pushes it (using mouse).
 * The menu is then close using escape key.
 * @author mmirilovic@netbeans.org
 */
public class MainMenu extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    protected static String menuPath;
    
    private JMenuBarOperator menuBar;
    
    private JMenuOperator testedMenu;
    
    private EditorOperator editor;

    /** Creates a new instance of MainMenu */
    public MainMenu(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }
    
    
    /** Creates a new instance of MainMenu */
    public MainMenu(String testName, String performanceDataName) {
        this(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 100;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
        setTestCaseName(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MainMenu("testFileMenu", "File main menu"));
        suite.addTest(new MainMenu("testEditMenu", "Edit main menu"));
        suite.addTest(new MainMenu("testViewMenu", "View main menu"));
        suite.addTest(new MainMenu("testNavigateMenu", "Navigate main menu"));
        suite.addTest(new MainMenu("testSourceMenu", "Source main menu"));
        suite.addTest(new MainMenu("testBuildMenu", "Build main menu"));
        suite.addTest(new MainMenu("testRunMenu", "Run main menu"));
        suite.addTest(new MainMenu("testRefactorMenu", "Refactor main menu"));
        suite.addTest(new MainMenu("testVersioningMenu", "CVS main menu"));
        suite.addTest(new MainMenu("testWindowMenu", "Window main menu"));
        suite.addTest(new MainMenu("testHelpMenu", "Help main menu"));
        return suite;
    }
    
    public void testFileMenu(){
        WAIT_AFTER_PREPARE = 1000;
        testMenu("org.netbeans.core.Bundle","Menu/File");
    }
    
    public void testEditMenu(){
        testMenuWithJava("org.netbeans.core.Bundle","Menu/Edit");
    }
    
    public void testViewMenu(){
        testMenuWithJava("org.netbeans.core.Bundle","Menu/View");
    }
    
    public void testNavigateMenu(){
        testMenuWithJava("org.netbeans.core.Bundle","Menu/GoTo");
    }
    
    public void testSourceMenu(){
        testMenuWithJava("org.netbeans.core.Bundle","Menu/Source");
    }
    
    public void testBuildMenu(){
        testMenuWithJava("org.netbeans.modules.project.ui.Bundle","Menu/BuildProject");
    }
    
    public void testRunMenu(){
        testMenuWithJava("org.netbeans.modules.project.ui.Bundle","Menu/RunProject");
    }
    
    public void testRefactoringMenu(){
        testMenuWithJava("org.netbeans.modules.refactoring.spi.impl.Bundle","Menu/Refactoring");
    }
    
    public void testVersioningMenu(){
        testMenuWithJava("org.netbeans.modules.versioning.Bundle","Menu/Versioning");
    }
    
    public void testProfileMenu(){
        testMenuWithJava("org.netbeans.modules.profiler.actions.Bundle","Menu/Profile");
    }
    
    public void testWindowMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/Window");
    }
    
    public void testHelpMenu(){
        testMenu("org.netbeans.core.Bundle","Menu/Help");
    }
    
    
    protected void testMenu(String menu){
        menuPath = menu;
        doMeasurement();
    }
    
    protected void testMenuWithJava(String bundle, String menu) {
        if(editor == null) {
            editor = Utilities.openJavaFile();
            waitNoEvent(5000);
        }
        testMenu(bundle, menu);
    }
    
    protected void testMenu(String bundle, String menu) {
        menuPath = org.netbeans.jellytools.Bundle.getStringTrimmed(bundle,menu);
        doMeasurement();
    }
    
    public void prepare(){
    }
    
    public ComponentOperator open(){
        menuBar.pushMenu(menuPath,"|");
        return testedMenu;
    }

    public void close() {
        super.close();
        
        if(editor != null)
            editor.close();
    }
    
    /**
     * Prepare method have to contain everything that needs to be done prior to
     * repeated invocation of test.
     * Default implementation is empty.
     */
    protected void initialize() {
        menuBar = MainWindowOperator.getDefault().menuBar();
        testedMenu = new JMenuOperator(MainWindowOperator.getDefault());
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new MainMenu("testGoToMenu"));
        junit.textui.TestRunner.run(new MainMenu("testSourceMenu"));
    }
    
}
