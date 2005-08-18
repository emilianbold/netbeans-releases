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

package gui.setup;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.junit.ide.ProjectSupport;

import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

public class IDESetupTest extends org.netbeans.jellytools.JellyTestCase {
    
    public IDESetupTest(java.lang.String testName) {
        super(testName);
    }

    public void openDataProject() {
        ProjectSupport.openProject(System.getProperty("xtest.data")+"/PerformanceTestData");
        ProjectSupport.waitScanFinished();
    }

    public void openWebProject() {
        ProjectSupport.openProject(System.getProperty("xtest.data")+"/PerformanceTestWebApplication");
        ProjectSupport.waitScanFinished();
    }
    
    public void openFoldersProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/PerformanceTestFoldersData");
        ProjectSupport.waitScanFinished();
    }
    
    public void openNBProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/SystemProperties");
        ProjectSupport.waitScanFinished();
    }

    /** 
     * Close Welcome. 
     */
    public void closeWelcome(){
        new TopComponentOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.welcome.Bundle","LBL_Tab_Title")).close();
    }
    
    /** 
     * Close BluePrints. 
     */
    public void closeBluePrints(){
        new TopComponentOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.blueprints.Bundle","LBL_Tab_Title")).close();
    }
    
    /** 
     * Close All Documents. 
     */
    public void closeAllDocuments(){
        new CloseAllDocumentsAction().perform();
    }
    
    /** 
     * Close Memory Toolbar. 
     */
    public void closeMemoryToolbar(){
        String MENU = 
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
            org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory");
        
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(MENU,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
        
    }
    
    
}
