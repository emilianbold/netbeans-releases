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



package org.netbeans.test.uml.sqd;


import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.sqd.utils.Util;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;




public class SQDDiagramTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    private String lastTestCase=null;
    
    public SQDDiagramTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SQDDiagramTests.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String PROJECT_NAME = "SQD_umlDT";      
    private String JAVA_PROJECT_NAME = "SQD_java";      
    private String EXCEPTION_DLG = "Exception";
    private String PKG_PATH = "Model|sqd";
    private String DIAGRAM = "EmptySequenceDiagram";
    final String PATH_TO_DIAGRAM = "Model|sqd|"+DIAGRAM;
    public final String OPEN_DIAGRAM = "Open";
    public final String DELETE_DIAGRAM = "Delete";
    
    private final String CONFIRM_DELETION_DLG_TTL = "Confirm Object Deletion";
    private final String YES_BTN = "Yes";
    private final String PROJECT_PATH = System.getProperty("nbjunit.workdir");
   
    /********************************************************/
    
    
    
    Util util = new Util(PROJECT_NAME);
    
    private static boolean initialized = false;
    
    protected void setUp() {
        if (!initialized){
            //util.closeStartupException();
                Project.openProject(this.XTEST_PROJECT_DIR+File.separator+"Project-SQD");
            org.netbeans.test.umllib.Utils.createUMLProjectFromJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME, PROJECT_PATH);                
            eventTool.waitNoEvent(2000);
            //setting up environment
            util.addDiagram(DIAGRAM, PKG_PATH);
            eventTool.waitNoEvent(2000);            
            initialized = true;
            java.awt.Robot rbt;
            try {
                rbt = new java.awt.Robot();
                rbt.keyRelease(KeyEvent.VK_SHIFT);
                rbt.keyRelease(KeyEvent.VK_CONTROL);
                rbt.keyRelease(KeyEvent.VK_ALT);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        }        
        eventTool.waitNoEvent(2000);           
    }
    
    
    public void testCreateDiagram(){
        lastTestCase=getCurrentTestMethodName();;
        final String DIAGRAM_NAME = "testCreateDiagram";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);  
            DiagramOperator dia = new DiagramOperator(DIAGRAM_NAME);
    }
    
    
    public void testOpenDiagramByContextMenu(){                                
        lastTestCase=getCurrentTestMethodName();;
            Node diaNode = util.getNode(PATH_TO_DIAGRAM);
            diaNode.performPopupActionNoBlock(OPEN_DIAGRAM);
            new DiagramOperator(DIAGRAM);                        
    }
    
    
    
    public void testOpenDiagramByDoubleClick(){        
        lastTestCase=getCurrentTestMethodName();;
            Node diaNode = util.getNode(PATH_TO_DIAGRAM);
            diaNode.tree().clickOnPath(diaNode.getTreePath(), 2);
            new DiagramOperator(DIAGRAM);                        
    }
    
    
    public void testDeleteDiagramByButton(){        
        lastTestCase=getCurrentTestMethodName();;
            Node diaNode = util.getNode(PATH_TO_DIAGRAM);
            diaNode.select();
            
            diaNode.tree().pushKey(KeyEvent.VK_DELETE);
            
            new JButtonOperator(new JDialogOperator(CONFIRM_DELETION_DLG_TTL), YES_BTN).push();
            eventTool.waitNoEvent(1000);            
            
            if (util.nodeExists(PATH_TO_DIAGRAM)){
                fail("Node exists");
            }
    }
    
    
    public void testDeleteDiagramByContextMenu(){                                
        lastTestCase=getCurrentTestMethodName();;
        final String DIAGRAM_NAME = "testDeleteDiagramByContextMenu";
            
            //creating diagram first
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(1000);
            new DiagramOperator(DIAGRAM_NAME);
            Node diaNode = util.getNode(PKG_PATH+"|"+DIAGRAM_NAME);
            diaNode.performPopupActionNoBlock(DELETE_DIAGRAM);
            
            //deleting it
            new JButtonOperator(new JDialogOperator(CONFIRM_DELETION_DLG_TTL), YES_BTN).push();
            eventTool.waitNoEvent(1000);
            
            if (util.nodeExists(PKG_PATH+"|"+DIAGRAM_NAME)){
                fail("testDeleteDiagramByContextMenu verification failed");
            }
    }
    
    
    
    
    
    
    public void tearDown() {
         org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
       long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
            new JDialogOperator(EXCEPTION_DLG).close();
            if (!failedByBug){
                fail("Unexpected Exception dialog was found");
            }            
        }catch(Exception excp){
        }finally{
            org.netbeans.test.umllib.util.Utils.saveAll();
            closeAllModal();
            if (failedByBug){
                failedByBug = false;                        
            }                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
            //TODO: should be removed later
            util.closeSaveDlg();
        }        
    }
    
}
