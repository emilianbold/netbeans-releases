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


import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.sqd.utils.Util;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.actions.DeleteElementAction;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.customelements.SequenceDiagramOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;




public class SQDElementsTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    
    private String lastTestCase=null;

    public SQDElementsTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SQDElementsTests.class);
        return suite;
    }
    
            
        
    /******************const section*************************/
    private String PROJECT_NAME = "SQD_umlET";      
    private String JAVA_PROJECT_NAME = "SQD_java";      
    private String EXCEPTION_DLG = "Exception";
    private String WARNING_DLG = "Warning";
    private String PKG_PATH = "Model|sqd";
    private final static String DELETE_DLG = "Delete";
    public final static String YES_BTN = "Yes";
    public final String NO_BTN = "No";
    private final String PROJECT_PATH = System.getProperty("nbjunit.workdir");   
    /********************************************************/
    
    
    
    Util util = new Util(PROJECT_NAME);
    
    private static boolean initialized = false;
    
    protected void setUp() {
        if (!initialized){                 
                Object obj = new Object();                
                //util.closeStartupException();
                //util.associateJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME);                
                Project.openProject(this.XTEST_PROJECT_DIR+File.separator+"Project-SQD");
                org.netbeans.test.umllib.Utils.createUMLProjectFromJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME, PROJECT_PATH);                
                //setting up environment
                eventTool.waitNoEvent(2000);
                initialized = true;
        }        
    }
    
    
    public void testCreateLifelineTree(){
        lastTestCase=getCurrentTestMethodName();;
        final String PATH_TO_CLASS = "Model|sqd|Customer";
        final String CLASS_NAME = "Customer";        
        final String DIAGRAM_NAME = "testCreateLifelineTree";
            Node node = util.getNode(PATH_TO_CLASS);        
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            DiagramOperator dia = new DiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.createGenericElementOnDiagram(node);
            
            //checking only required elements are present on diagram
            DiagramElementOperator line = new LifelineOperator(dia, "", CLASS_NAME);
            if (!util.diagramHasExactElements(new DiagramElementOperator[]{line}, dia)){
                fail("testCreateLifelineTree verification failed");
            }
            
    }
    
    
    public void testCreateLifelinePalette(){        
        lastTestCase=getCurrentTestMethodName();
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("startTest");
        final String LINE_NAME = "SQDLine"; 
        final String PATH_TO_CLASS = PKG_PATH + "|" + LINE_NAME;
        final String DIAGRAM_NAME = "testCreateLifelinePalette";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("afterDiagramCreate");
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            dia.waitComponentShowing(true);
            try{Thread.sleep(500);}catch(Exception ex){}
            //creating element
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
                        
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            eventTool.waitNoEvent(5000);
            
            //checking only required elements are present on diagram
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            if (!util.diagramHasExactElements(new DiagramElementOperator[]{line}, dia)){
                fail("testCreateLifelineTree verification failed");
            }
            
            //checking the node was created:
            Node newNode = util.getNode(PATH_TO_CLASS);            
            
    }
    
    
    
    public void testCreateLifelineActor(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "SQDActor"; 
        final String PATH_TO_CLASS = "Model|"+"sqd"+"|"+LINE_NAME;
        final String DIAGRAM_NAME = "testCreateLifelineActor";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            //Point p = dia.getPointForLifeline(100);
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE);
            //dia.createGenericElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE, p.x, p.y, LibProperties.getCurrentNamer(ElementTypes.LIFELINE));
                        
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //checking only required elements are present on diagram
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            if (!util.diagramHasExactElements(new DiagramElementOperator[]{line}, dia)){
                fail("testCreateLifelineActor verification failed");
            }
            
            //checking the node was created:
            Node newNode = util.getNode(PATH_TO_CLASS);            
    }
    
    
    public void testCreateMessageToSelf(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "SQDMesToSelf"; 
        final String DIAGRAM_NAME = "testCreateMessageToSelf";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            //creating message
            dia.createLinkOnDiagram(LinkTypes.MESSAGE_TO_SELF, line, line);
                        
            //checking the the links were created:
            new LinkOperator(line, line, LinkTypes.MESSAGE, 0);
            new LinkOperator(line, line, LinkTypes.MESSAGE, 1);
    }
    
    
    public void testCreateCreateMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "CMSS"; 
        final String LINE_NAME_1 = "CMST";
        final String DIAGRAM_NAME = "testCreateCreateMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.CREATE_MESSAGE, line1, line2);
                        
            //checking that the links were created:
            new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            
    }
    
    
    public void testCreateSyncMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "SMS"; 
        final String LINE_NAME_1 = "SMT";
        final String DIAGRAM_NAME = "testCreateSyncMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.SYNC_MESSAGE, line1, line2);
                        
            //checking that the links were created:
            new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            
    }
    
    
    public void testCreateASyncMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "ASMS"; 
        final String LINE_NAME_1 = "ASMT";
        final String DIAGRAM_NAME = "testCreateASyncMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.ASYNC_MESSAGE, line1, line2);
                        
            //checking that the links were created:
            new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            
    }
    
    
    
    public void testCreateCombinedFragment(){        
        lastTestCase=getCurrentTestMethodName();;
        final String DIAGRAM_NAME = "testCreateCombinedFragment";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            DiagramOperator dia = new DiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);

            eventTool.waitNoEvent(1000);
            dia = new DiagramOperator(DIAGRAM_NAME);
            
            //checking only required elements are present on diagram
            DiagramElementOperator fr = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT), 0);
            if (!util.diagramHasExactElements(new DiagramElementOperator[]{fr}, dia)){
                fail("testCreateCombinedFragment verification failed");
            }
    }
    
    
    public void testDeleteLifeLineFromDiagramByButton(){
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DLDB"; 
        final String DIAGRAM_NAME = "testDeleteLifeLineFromDiagramByButton";
        final String LINE_PATH = "Model|sqd|"+DIAGRAM_NAME+"|"+LINE_NAME; 
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //deleting element
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            line.select();
            
            new DeleteElementAction().performShortcut(line);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            chk.clickMouse();
            org.netbeans.test.umllib.util.Utils.makeScreenShot();
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1500);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteLifeLineFromDiagramByButton verification failed");
            }   
            
            if (util.nodeExists(LINE_PATH)){
                failedByBug = true;
                fail("The line node was not deleted: "+LINE_PATH);                                
            }
    }
    
    
    public void testDeleteLifeLineFromDiagramByButton2(){
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DLDB2"; 
        final String DIAGRAM_NAME = "testDeleteLifeLineFromDiagramByButton2";
        final String LINE_PATH = "Model|sqd|"+DIAGRAM_NAME+"|"+LINE_NAME; 
        final String CL_PATH = "Model|sqd|"+LINE_NAME; 
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //deleting element
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            line.select();
            
            new DeleteElementAction().performShortcut(line);
            
            eventTool.waitNoEvent(2000);
            
            //new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
            new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
            
            eventTool.waitNoEvent(2000);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteLifeLineFromDiagramByButton verification failed");
            }   
            
            if (!util.nodeExists(LINE_PATH)){                                
                fail("testDeleteLifeLineFromDiagramByButton verification failed");
            }
            
             if (!util.nodeExists(CL_PATH)){                                
                fail("testDeleteLifeLineFromDiagramByButton verification failed");
            }
    }
    
    
    
    public void testDeleteActorFromDiagramByButton(){
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DADB"; 
        final String DIAGRAM_NAME = "testDeleteActorFromDiagramByButton";
        final String LINE_PATH = "Model|sqd|"+DIAGRAM_NAME+"|"+LINE_NAME; 
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            //Point p = dia.getPointForLifeline(100);
            //dia.createGenericElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE, p.x, p.y, LibProperties.getCurrentNamer(ElementTypes.LIFELINE));                        
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //deleting element
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            line.select();
            
            new DeleteElementAction().performShortcut(line);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            if(!chk.isSelected())chk.clickMouse();
            chk.waitSelected(true);
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1500);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteActorFromDiagramByButton verification failed");
            }   
            
            if (util.nodeExists(LINE_PATH)){
                failedByBug = true;
                fail( "The node was not deleted");                
            }
    }
    
    
    
    public void testDeleteActorFromDiagramByButton2(){
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DADB2"; 
        final String DIAGRAM_NAME = "testDeleteActorFromDiagramByButton2";
        final String LINE_PATH = "Model|sqd|"+LINE_NAME; 
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            //Point p = dia.getPointForLifeline(100);
            //dia.createGenericElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE, p.x, p.y, LibProperties.getCurrentNamer(ElementTypes.LIFELINE));                        
            dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.ACTOR_LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //deleting element
            DiagramElementOperator line = new LifelineOperator(dia, LINE_NAME, LINE_NAME);
            line.select();
            
            new DeleteElementAction().performShortcut(line);
            
            eventTool.waitNoEvent(2000);
            
            //new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
            new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
                        
            eventTool.waitNoEvent(1000);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteActorFromDiagramByButton2 verification failed");
            }   
            
            if (!util.nodeExists(LINE_PATH)){                                
                fail("testDeleteActorFromDiagramByButton2 verification failed");
            }
    }
   
    
    
    public void testDeleteCombinedFragmentFromDiagramByBtn(){        
        lastTestCase=getCurrentTestMethodName();;
        final String DIAGRAM_NAME = "testDeleteCombinedFragmentFromDiagramByBtn";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            DiagramOperator dia = new DiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);

            eventTool.waitNoEvent(1000);
            dia = new DiagramOperator(DIAGRAM_NAME);
            
            //checking only required elements are present on diagram
            DiagramElementOperator fr = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT), 0);
            eventTool.waitNoEvent(1000);
            
            dia.getDrawingArea().pushKey(KeyEvent.VK_TAB);
            fr.waitSelection(true);
            
            eventTool.waitNoEvent(1000);
                        
            //new DeleteElementAction().performShortcut(fr);
            dia.getDrawingArea().pushKey(KeyEvent.VK_DELETE);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            if(!chk.isSelected())chk.clickMouse();
            chk.waitSelected(true);
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1000);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteCombinedFragmentFromDiagramByBtn verification failed");
            }
    }
    
    
    public void testDeleteCombinedFragmentFromDiagramByBtn2(){        
        lastTestCase=getCurrentTestMethodName();;
        final String DIAGRAM_NAME = "testDeleteCombinedFragmentFromDiagramByBtn2";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            DiagramOperator dia = new DiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);

            eventTool.waitNoEvent(1000);
            dia = new DiagramOperator(DIAGRAM_NAME);
            
            //checking only required elements are present on diagram
            DiagramElementOperator fr = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT), 0);
            dia.getDrawingArea().pushKey(KeyEvent.VK_TAB);
            fr.waitSelection(true);
            //new DeleteElementAction().performShortcut(fr);
            dia.getDrawingArea().pushKey(KeyEvent.VK_DELETE);
            //new JCheckBoxOperator(new JDialogOperator(DELETE_DLG)).clickMouse();
            new JButtonOperator(new JDialogOperator(DELETE_DLG), YES_BTN).push();
            
            eventTool.waitNoEvent(1000);
            
            if (!util.diagramHasExactElements(new DiagramElementOperator[0], dia)){
                fail("testDeleteCombinedFragmentFromDiagramByBtn2 verification failed");
            }
    }
    
    
    
    public void testDeleteCreateMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DCMS"; 
        final String LINE_NAME_1 = "DCMT";
        final String DIAGRAM_NAME = "testDeleteCreateMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.CREATE_MESSAGE, line1, line2);
                        
            //checking that the links were created:
            LinkOperator lnk = new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            lnk.select();
            eventTool.waitNoEvent(1000);
            
            new DeleteElementAction().performShortcut(lnk);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            if(!chk.isSelected())chk.clickMouse();
            chk.waitSelected(true);
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1000);
            
            if (line1.getLinks().size()>0){
                fail("testDeleteCreateMessage verification failed ");
            }
    }
    
    
    
    public void testDeleteSyncMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DSMS"; 
        final String LINE_NAME_1 = "DSMT";
        final String DIAGRAM_NAME = "testDeleteSyncMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.SYNC_MESSAGE, line1, line2);
            
            eventTool.waitNoEvent(1000);
            
            //checking that the links were created:
            LinkOperator lnk = new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            
            lnk.select();
            eventTool.waitNoEvent(1000);
            new DeleteElementAction().performShortcut(lnk);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            if(!chk.isSelected())chk.clickMouse();
            chk.waitSelected(true);
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1000);
            
            if (line1.getLinks().size()>0){
                fail("testDeleteSyncMessage verification failed ");
            }
     }
    
    
    public void testDeleteASyncMessage(){        
        lastTestCase=getCurrentTestMethodName();;
        final String LINE_NAME = "DASMS"; 
        final String LINE_NAME_1 = "DASMT";
        final String DIAGRAM_NAME = "testDeleteASyncMessage";
            util.addDiagram(DIAGRAM_NAME, PKG_PATH);
            eventTool.waitNoEvent(500);
            SequenceDiagramOperator dia = new SequenceDiagramOperator(DIAGRAM_NAME);            
            
            //creating element
            DiagramElementOperator line1 =  dia.putElementOnDiagram(LINE_NAME+":"+LINE_NAME, ElementTypes.LIFELINE);
            
            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            DiagramElementOperator line2 = dia.putElementOnDiagram(LINE_NAME_1+":"+LINE_NAME_1, ElementTypes.LIFELINE);

            eventTool.waitNoEvent(1000);
            dia = new SequenceDiagramOperator(DIAGRAM_NAME);
            
            //creating message
            dia.createLinkOnDiagram(LinkTypes.ASYNC_MESSAGE, line1, line2);
            eventTool.waitNoEvent(1000);            
            //checking that the links were created:
            LinkOperator lnk = new LinkOperator(line1, line2, LinkTypes.MESSAGE);            
            lnk.select();
            
            eventTool.waitNoEvent(1000);
            
            new DeleteElementAction().performShortcut(lnk);
            JDialogOperator delDlg=new JDialogOperator(DELETE_DLG);
            JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
            if(!chk.isSelected())chk.clickMouse();
            chk.waitSelected(true);
            new JButtonOperator(delDlg, YES_BTN).push();
            
            eventTool.waitNoEvent(1000);
            
            if (line1.getLinks().size()>0){
                fail("testDeleteASyncMessage verification failed ");
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
            new org.netbeans.jellytools.actions.Action(null,null,new org.netbeans.jellytools.actions.Action.Shortcut(KeyEvent.VK_ESCAPE)).performShortcut();
            new org.netbeans.jellytools.actions.Action(null,null,new org.netbeans.jellytools.actions.Action.Shortcut(KeyEvent.VK_ESCAPE)).performShortcut();
            try{Thread.sleep(100);}catch(Exception ex){}
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
