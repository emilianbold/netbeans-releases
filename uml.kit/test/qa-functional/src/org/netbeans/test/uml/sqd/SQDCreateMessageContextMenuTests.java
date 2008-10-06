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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.sqd.utils.GenericContextMenuVerifier;
import org.netbeans.test.uml.sqd.utils.Util;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.actions.SelectAllElementAction;
import org.netbeans.test.umllib.customelements.SequenceDiagramOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class SQDCreateMessageContextMenuTests  extends UMLTestCase {
    
    public SQDCreateMessageContextMenuTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SQDCreateMessageContextMenuTests.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String PROJECT_NAME = "SQD_umlCMCMT";      
    private String JAVA_PROJECT_NAME = "SQD_java";      
    private String EXCEPTION_DLG = "Exception";
    private String PKG_PATH = "Model|sqd";
    private String DIAGRAM = "NewSequenceDiagram";
    private String PATH_TO_DIAGRAM = "Model|sqd|"+DIAGRAM;
    private String DELETE_DLG = "Delete";
    private String YES_BTN = "Yes";
    private String OK_BTN = "Ok";
    private String CANCEL_BTN = "Cancel";
    private String CONSTRUCTOR_NAME_DLG = "Constructor Name Change";
    private final String PROJECT_PATH = System.getProperty("nbjunit.workdir");
    /********************************************************/
    
    
    
    Util util = new Util(PROJECT_NAME);
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;    
    private static boolean initialized = false;
    private static SequenceDiagramOperator dia = null;
    
    private DiagramElementOperator line1 = null;
    private DiagramElementOperator line2 = null;
    
    private String lastTestCase=null;
    
    protected void setUp() {
        eventTool.waitNoEvent(2000);                   
        if (!initialized){                 
                //util.closeStartupException();
                //associating java project
                //util.associateJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME);
                Project.openProject(this.XTEST_PROJECT_DIR+File.separator+"Project-SQD");
                org.netbeans.test.umllib.Utils.createUMLProjectFromJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME, PROJECT_PATH);
                
                eventTool.waitNoEvent(2000);
                                        
                //setting up environment
                util.addDiagram(DIAGRAM, PKG_PATH);
                dia = new SequenceDiagramOperator(DIAGRAM);
                
                initialized = true;
        }else{
            dia = new SequenceDiagramOperator(DIAGRAM);
            safeDeleteAllElements();
        }
    }
    
    
    private void safeDeleteAllElements(){
        try{
            if (dia.getDiagramElements().size()>0){
                new SelectAllElementAction().performPopup(dia.getDrawingArea());
                dia.getDrawingArea().pushKey(KeyEvent.VK_DELETE);
                new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();        
                eventTool.waitNoEvent(1000);
                Point p = dia.getDrawingArea().getFreePoint(150);
                dia.getDrawingArea().clickMouse(p.x, p.y, 1);
                eventTool.waitNoEvent(1000);
            }
        }catch(Exception e){}
    }
    
    private LinkOperator createWorkingLink(String lnName, String className) throws NotFoundException{
        Point clickPoint = dia.getDrawingArea().getFreePoint(200);
        line1 = dia.putElementOnDiagram(lnName+"1:"+className+"1", ElementTypes.LIFELINE, clickPoint.x, clickPoint.y);
        eventTool.waitNoEvent(1000);
        clickPoint = dia.getDrawingArea().getFreePoint(200);
        line2 = dia.putElementOnDiagram(lnName+"2:"+className+"2", ElementTypes.LIFELINE, clickPoint.x, clickPoint.y);
        eventTool.waitNoEvent(1000);
        return dia.createLinkOnDiagram(LinkTypes.CREATE_MESSAGE, line1, line2);
    }
    
      
   public void testOperation(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testOO";
        final String className = "OO";
        final String labelName = "public "+className+"2(  )";
        final String popupPath = "Operations|"+labelName;
        boolean enabled = true;                
            final LinkOperator lnk = createWorkingLink(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(lnk, dia){
                protected boolean checkActionResult(){
                    eventTool.waitNoEvent(1500);
                    String[] labels = lnk.getLabelsTexts();
                    for(int i=0;i<labels.length;i++){
                        if (labels[i].equals(labelName)){
                            return true;
                        }
                    }
                    return false;
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testOperation failed. Reason unknown");
            }            
   }
   
   public void testAddConstructor(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testOAC";
        final String className = "OAC";
        final String constructorName = "qwerty";
        final String popupPath = "Operations|Add Constructor";
        boolean enabled = true;                
            final LinkOperator lnk = createWorkingLink(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(lnk, dia){
                protected boolean checkActionResult(){                    
                    eventTool.waitNoEvent(1500);
                    for(int i=0;i<constructorName.length();i++) {
                        dia.getDrawingArea().typeKey(constructorName.charAt(i));
                    }                    
                    dia.getDrawingArea().typeKey('\n');
                    //new JButtonOperator(new JDialogOperator(CONSTRUCTOR_NAME_DLG), OK_BTN).push();
                    String[] labels = lnk.getLabelsTexts();
                    for(int i=0;i<labels.length;i++){
                        log(labels[i]+" and search for public qwerty(  )");
                        if (labels[i].equals("public qwerty(  )")){
                            return true;
                        }
                    }
                    return false;
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testAddConstructor failed. Reason unknown");
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
