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

package gui.setup;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;



public class EJBSetupTest extends org.netbeans.junit.NbTestCase {
    
    public EJBSetupTest(java.lang.String testName) {
        super(testName);
    }

    public void testOpenEJBProject() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication/TestApplication-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication/TestApplication-war");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest/DeployTest-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest/DeployTest-war");
        ProjectSupport.waitScanFinished();
        //waitForScan();
    }
    
    public void testAddAppServer() {
        String path = System.getProperty("j2ee.appserver.path");
        if (path == null) {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0) {
                path = "E:\\space\\AppServer";
                //path =  "E:\\Sun\\AppServer-8.1_01_b04";
            } else {
                path = "/space/appserver";
            }
        }
        String username = System.getProperty("j2ee.appserver.username","admin");
        String password = System.getProperty("j2ee.appserver.password","adminadmin");
                    
        Node node = new Node(new RuntimeTabOperator().getRootNode(),"Servers");
        node.performPopupActionNoBlock("Add Server...");
        NbDialogOperator dialog = new NbDialogOperator("Add Server");
        new JComboBoxOperator(dialog).selectItem("Glassfish V2");
        new JButtonOperator(dialog,"Next").push();
        
        //"Enter the Application Server location" or "Define Application Server Instance Properties"
        if (new JLabelOperator(dialog,1).getText().equalsIgnoreCase("Enter the Application Server location")) {
            new JTextFieldOperator(dialog).setText("");
            new JTextFieldOperator(dialog).typeText(path);
            new JButtonOperator(dialog,"Next").push();
        }
        new JTextFieldOperator(dialog,0).setText("");
        new JTextFieldOperator(dialog,1).setText("");
        new JTextFieldOperator(dialog,0).typeText(username);
        new JTextFieldOperator(dialog,1).typeText(password);
        new JButtonOperator(dialog,"Finish").push();
        new ProjectsTabOperator();
    }
    
        
    public void closeAllDocuments(){
        new CloseAllDocumentsAction().perform();
    }    
    
    public void closeNavigator() {
    	new TopComponentOperator("Navigator").close();
    }
    
    public void testCloseMemoryToolbar(){
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

    
    private void waitForScan() {
        // "Scanning Project Classpaths"
        String titleScanning = Bundle.getString("org.netbeans.modules.javacore.Bundle", "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed();
    }    

}
