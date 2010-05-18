/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



package org.netbeans.test.uml.relationshipdiscovery;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.relationshipdiscovery.utils.Util;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.actions.DeleteElementAction;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.uml.relationshipdiscovery.utils.BaseRelatioshipRecoveryVerifier;
import org.netbeans.test.umllib.project.Project;


public class CollaborationDiagramRelationshipDiscoveryTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    private static String lastTestCase=null;

    private static String prName="UML-1";
    private static String prFolder="Project-RelationshipDiscovery";
    
    
    private Node lastDiagramNode;
    
    private static boolean init=false;
    
    public CollaborationDiagramRelationshipDiscoveryTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.relationshipdiscovery.CollaborationDiagramRelationshipDiscoveryTests.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String DIAGRAM_NAME = "EmptyCollaborationDiagram";  
    private String EXCEPTION_DLG = "Exception";
   
    /********************************************************/
    
    
    BaseRelatioshipRecoveryVerifier classVerifier = null;
    DiagramOperator dia  = null;
    
    String workPkg;
    String diagram;
    static int counter=0;
    
    protected void setUp() {
        if(!init)
        {
            Project.openProject(this.XTEST_PROJECT_DIR+File.separator+prFolder);
            init=true;
        }
        counter++;
        workPkg="pkg_cl_"+counter;
        diagram="dgr"+counter;
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),prName);
        
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(prName,workPkg,diagram,NewDiagramWizardOperator.COLLABORATION_DIAGRAM);
        lastDiagramNode=rt.lastDiagramNode;
        
        dia = rt.dOp;
        classVerifier = new BaseRelatioshipRecoveryVerifier(dia){
            protected DiagramElementOperator addElement(ElementTypes elementType, String elementName) throws NotFoundException{
                Point point = dia.getDrawingArea().getFreePoint(100);            
                dia.createGenericElementOnDiagram(elementName+":"+elementName, elementType, point.x, point.y);                
                DiagramElementOperator el = new DiagramElementOperator(dia, elementName);
                new EventTool().waitNoEvent(1000);
                return el;
            }
        };
    }
    
    
    public void testRecoverConnector() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){
                protected void deleteRelationship(LinkOperator lnk) throws NotFoundException{        
                        new DeleteElementAction().performShortcut(lnk);
                        JDialogOperator delDlg = new JDialogOperator(Util.DELETE_DLG);
                        new EventTool().waitNoEvent(500);
                        JCheckBoxOperator chb = new JCheckBoxOperator(delDlg, "Deletion will permanently delete elements from project");
                        chb.setSelected(false);
                        new EventTool().waitNoEvent(500);
                        new JButtonOperator(delDlg, Util.YES_BTN).push();        
                        eventTool.waitNoEvent(1000);
                }
            };
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.LIFELINE, LinkTypes.CONNECTOR);
            if (!result){
                fail("testRecoverActivityEdge failed. Reason unknown");
            }
    }
    
    
    
    public void testRecoverMultipleConnectors() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){
                
                protected LinkOperator addRelatioship(DiagramElementOperator el1, DiagramElementOperator el2, LinkTypes linkType) throws NotFoundException{
                    //creating 3 links
                    for(int i=0;i<3;i++){
                        dia.createGenericRelationshipOnDiagram(linkType, el1, el2);                    
                        eventTool.waitNoEvent(1000);
                        try{Thread.sleep(100);}catch(Exception ex){}
                    }
                    
                    new Thread(new Runnable() {
                        public void run() {
                            JDialogOperator layoutDlg = new JDialogOperator(Util.LAYOUT_DLG);
                            new JButtonOperator(layoutDlg, Util.YES_BTN).pushNoBlock();
                            layoutDlg.waitClosed();
                        }
                    }).start();
                    dia.toolbar().selectTool(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL);
                    eventTool.waitNoEvent(1000);
                    dia.getDrawingArea().dragNDrop(el2.getCenterPoint().x, el2.getCenterPoint().y, el2.getCenterPoint().x+50, el2.getCenterPoint().y);
                    eventTool.waitNoEvent(1000);
                    return null;
                }
                
                protected void deleteRelationship(LinkOperator lnk) throws NotFoundException{        
                    DiagramElementOperator el1 = new DiagramElementOperator(dia, ElementTypes.LIFELINE+"1");
                    DiagramElementOperator el2 = new DiagramElementOperator(dia, ElementTypes.LIFELINE+"2");
                    
                    //we create 3 links and have to delete them now
                    for(int i=2;i>-1;i--){
                        LinkOperator edge = new LinkOperator(el1, el2);
                        edge.select();
                        new DeleteElementAction().performShortcut(edge);
                        JDialogOperator delDlg = new JDialogOperator(Util.DELETE_DLG);
                        new EventTool().waitNoEvent(500);
                        JCheckBoxOperator chb = new JCheckBoxOperator(delDlg, "Deletion will permanently delete elements from project");
                        chb.setSelected(false);
                        new EventTool().waitNoEvent(500);
                        new JButtonOperator(delDlg, Util.YES_BTN).push();        
                        eventTool.waitNoEvent(1000);
                    }
                }
                
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                        new LinkOperator(source, destination, LinkTypes.CONNECTOR, 0);
                        try{
                            new LinkOperator(source, destination, LinkTypes.CONNECTOR, 1);
                        }
                        catch(TimeoutExpiredException ex)
                        {
                            fail(78844,"Only one connector is restored.");
                        }
                        new LinkOperator(source, destination, LinkTypes.CONNECTOR, 2);
                        return true;
                }
            };
            
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.LIFELINE, LinkTypes.CONNECTOR);
            if (!result){
                fail("verification failed");
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
            if (failedByBug){
                failedByBug = false;                        
            }
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
            Util.closeSaveDlg();
        }       
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        //
        try{Thread.sleep(100);}catch(Exception ex){}
        dia.pushKey(KeyEvent.VK_S,KeyEvent.CTRL_DOWN_MASK);
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        //
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save Diagram"),"Yes").pushNoBlock();
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        try{
            dia.closeAllDocuments();
            dia.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
    }
    
    
}

