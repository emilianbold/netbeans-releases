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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.performance.j2ee.setup;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;



public class J2EESetup extends PerformanceTestCase {
    
    public J2EESetup(java.lang.String testName) {
        super(testName);
    }

    public void testOpenEJBProject() {
        
        /*
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication/TestApplication-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/TestApplication/TestApplication-war");
        ProjectSupport.waitScanFinished();*/
/*        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest/DeployTest-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/perf/DeployTest/DeployTest-war");
        ProjectSupport.waitScanFinished();*/
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

    public void testJAXRPC() {
       new JButtonOperator(new NbDialogOperator("Warning")).push();
       new JButtonOperator(new NbDialogOperator("Warning")).push();
    }
    
    private void waitForScan() {
        // "Scanning Project Classpaths"
        String titleScanning = Bundle.getString("org.netbeans.modules.javacore.Bundle", "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed();
    }

    @Override
    public void prepare() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentOperator open() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
