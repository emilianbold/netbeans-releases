/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.setup;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.jemmy.operators.JMenuBarOperator;

public class IDESetupTest extends org.netbeans.jellytools.JellyTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }

    public void testOpenDataProject() {
        ProjectSupport.openProject(System.getProperty("xtest.data")+"/PerformanceTestData");
        ProjectSupport.waitScanFinished();
    }

    public void testOpenWebProject() {
        ProjectSupport.openProject(System.getProperty("xtest.data")+"/PerformanceTestWebApplication");
        ProjectSupport.waitScanFinished();
    }
    
    public void testOpenFoldersProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/PerformanceTestFoldersData");
        ProjectSupport.waitScanFinished();
    }
    
    /** 
     * Close Welcome. 
     */
    public void testCloseWelcome(){
        new TopComponentOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.welcome.Bundle","LBL_Tab_Title")).close();
    }
    
    
    /** 
     * Close Memory Toolbar. 
     */
    public void testCloseMemoryToolbar(){
        String MENU = 
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory");
        
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenu(MENU,"|"); //NOI18N 
        
    }
    
    
}
