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

package gui.javahelp;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.operators.JButtonOperator;

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

import org.netbeans.junit.NbTestSuite;

/**
 * JellyTestCase test case with implemented Java Help Test support stuff
 *
 * @author  mmirilovic@netbeans.org
 */
public class JavaHelpDialogTest extends JellyTestCase {
    
    private HelpOperator helpWindow;
    
    /** Creates a new instance of JavaHelpDialogTest */
    public JavaHelpDialogTest(String testName) {
        super(testName);
    }
    
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new JavaHelpDialogTest("testHelpF1"));
        suite.addTest(new JavaHelpDialogTest("testHelpFromMenu"));
        suite.addTest(new JavaHelpDialogTest("testHelpByButtonNonModal"));
        suite.addTest(new JavaHelpDialogTest("testHelpByButtonModal"));
        suite.addTest(new JavaHelpDialogTest("testSearchInIndex"));
        suite.addTest(new JavaHelpDialogTest("testContextualSearch"));
        suite.addTest(new JavaHelpDialogTest("testHelpByButtonNestedModal"));
        return suite;
    }
    
    public void setUp() {
    }
    
    public void tearDown(){
        closeAllModal();
        
        if(helpWindow != null && helpWindow.isVisible())
            helpWindow.close();
        
        helpWindow = null;
    }
    
    public void testHelpF1(){
        MainWindowOperator.getDefault().pressKey(java.awt.event.KeyEvent.VK_F1);
        new org.netbeans.jemmy.EventTool().waitNoEvent(7000);
        helpWindow = new HelpOperator();
    }
    
    public void testHelpFromMenu(){
        new HelpAction().performMenu();
        helpWindow = new HelpOperator();
    }
    
    public void testHelpCoreFromMenu(){
        String helpMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help"); // Help
        String helpSetsMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.javahelp.resources.Bundle", "Menu/Help/HelpShortcuts");  // Help Sets
        String coreIDEHelpMenu = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.usersguide.Bundle", "Actions/Help/org-netbeans-modules-usersguide-mainpage.xml"); // Core IDE Help
        
        MainWindowOperator.getDefault().menuBar().pushMenu( helpMenu+"|"+helpSetsMenu+"|"+coreIDEHelpMenu, "|");
        helpWindow = new HelpOperator();
    }
    
    public void testHelpByButtonNonModal(){
        OptionsOperator.invoke();  //new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Tools|Options","|"); // NOI18N
        OptionsOperator options = new OptionsOperator();
        options.help();
        helpWindow = new HelpOperator();
        options.close();
    }
    
    public void testHelpByButtonModal(){
        String toolsMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // Tools
        //String setupWizardMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", "LBL_SetupWizard"); // Setup Wizard
        String javaPlatformMenu = "Java Platform Manager";
        
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu+"|"+javaPlatformMenu,"|");
        //new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "CTL_SetupWizardTitle")).help();    // Setup Wizard
        new NbDialogOperator("Java Platform Manager").help();    // Java Platform Manager
        helpWindow = new HelpOperator();
    }
    
    public void testHelpByButtonNestedModal(){
        String toolsMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Tools"); // Tools
        //String setupWizardMenu = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", "LBL_SetupWizard"); // Setup Wizard
        String javaPlatformMenu = "Java Platform Manager";
        
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu+"|"+javaPlatformMenu,"|");
        //new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "CTL_SetupWizardTitle")).help();    // Setup Wizard
        NbDialogOperator javaPlatformManager = new NbDialogOperator("Java Platform Manager");// Java Platform Manager
        
        new JButtonOperator(javaPlatformManager, "Add Platform...").pushNoBlock();
        NbDialogOperator addJavaPlatform = new NbDialogOperator("Add Java Platform");// Add Java Platform dialog
        addJavaPlatform.help();
        helpWindow = new HelpOperator();
        
        // close
        addJavaPlatform.cancel();
        javaPlatformManager.closeByButton();
    }
    
    public void testSearchInIndex(){
        new HelpAction().perform();
        helpWindow = new HelpOperator();
        helpWindow.selectPageIndex();
        helpWindow.indexFind("compile");
        try{
            Thread.sleep(5000);
        }catch(Exception exc){
            exc.printStackTrace(getLog());
        }
        
        JTreeOperator tree = helpWindow.treeIndex();
        log("Selection path="+tree.getSelectionPath());
        log("Selection count="+tree.getSelectionCount());
        
        if(tree.getSelectionCount()<1)
            fail("None founded text in the help, it isn't obvious");
    }
    
    public void testContextualSearch(){
        new HelpAction().perform();
        helpWindow = new HelpOperator();
        helpWindow.selectPageSearch();
        helpWindow.searchFind("compile");
        
        try{
            Thread.sleep(5000);
        }catch(Exception exc){
            exc.printStackTrace(getLog());
        }
        
        
        JTreeOperator tree = helpWindow.treeSearch();
        log("Selection path="+tree.getSelectionPath());
        log("Selection count="+tree.getSelectionCount());
        
        if(tree.getSelectionCount()<1)
            fail("None founded text in the help, it isn't obvious");
    }
    
    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
