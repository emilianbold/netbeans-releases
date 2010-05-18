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


import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
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
import org.netbeans.test.umllib.exceptions.ElementVerificationException;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LibProperties;
import org.netbeans.test.uml.relationshipdiscovery.utils.BaseRelatioshipRecoveryVerifier;
import org.netbeans.test.umllib.exceptions.KnownBugException;
import org.netbeans.test.umllib.project.Project;




public class ClassDiagramRelationshipRecoveryTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    private static String lastTestCase=null;

    private ElementTypes lastType1;

    private ElementTypes lastType2;

    private static String prName="UML-1";
    private static String prFolder="Project-RelationshipDiscovery";
    
    
    private Node lastDiagramNode;
    
    private static boolean init=false;
    
    public ClassDiagramRelationshipRecoveryTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.relationshipdiscovery.ClassDiagramRelationshipRecoveryTests.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String DIAGRAM_NAME = "EmptyClassDiagram";  
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
        workPkg="pkg_c_"+counter;
        diagram="dgr"+counter;
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),prName);
        
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(prName,workPkg,diagram,NewDiagramWizardOperator.CLASS_DIAGRAM);
        lastDiagramNode=rt.lastDiagramNode;
        
        dia = rt.dOp;
        classVerifier = new BaseRelatioshipRecoveryVerifier(dia);
    }
   
    
    
    public void testRecoverAssociation() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.ASSOCIATION);
            if (!result){
                fail("testRecoverAssociation failed. Reason unknown");
            }
    }
    
    
    
    public void testRecoverNavigableAssociation() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){                               
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                    return super.checkRelationshipOnDiagram(source, destination, LinkTypes.ASSOCIATION);
                }                
            };
            
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.NAVIGABLE_ASSOCIATION);
            if (!result){
                fail("testRecoverNavigableAssociation failed. Reason unknown");
            }
   }
    
    
    
    
    public void testRecoverComposition() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.COMPOSITION);
            if (!result){
                fail(78484);
            }
    }
    
    
    
    
    public void testRecoverNavigableComposition() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){                               
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                    return super.checkRelationshipOnDiagram(source, destination, LinkTypes.COMPOSITION);
                }                
            };
            
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.NAVIGABLE_COMPOSITION);
            if (!result){
                fail(78484);
            }
    }
    
    
    
    
    public void testRecoverAggregation() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.AGGREGATION);
            if (!result){
                fail("testRecoverAggregation failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverNavigableAggregation() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){                               
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                    return super.checkRelationshipOnDiagram(source, destination, LinkTypes.AGGREGATION);
                }                
            };            
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.NAVIGABLE_AGGREGATION);
            if (!result){
                fail("testRecoverNavigableAggregation failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverAssociationClass() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){
                protected LinkOperator addRelatioship(DiagramElementOperator el1, DiagramElementOperator el2, LinkTypes linkType) throws NotFoundException{
                    dia.createGenericRelationshipOnDiagram(linkType, el1, el2);                    
                    eventTool.waitNoEvent(1000);
                    try{Thread.sleep(100);}catch(Exception ex){}
                    new Thread(new Runnable() {
                        public void run() {
                            JDialogOperator layoutDlg = new JDialogOperator(Util.LAYOUT_DLG);
                            new JButtonOperator(layoutDlg, Util.YES_BTN).pushNoBlock();
                            layoutDlg.waitClosed();
                        }
                    }).start();
                    dia.toolbar().selectTool(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL);
                                        
                    eventTool.waitNoEvent(1000);
                    DiagramElementOperator assClass = new DiagramElementOperator(dia, "Unnamed", 1);
                    assClass.select();                    
                    LibProperties.getCurrentNamer(ElementTypes.CLASS).setName(dia.getDrawingArea(),assClass.getCenterPoint().x, assClass.getCenterPoint().y, "AssClass");
                    eventTool.waitNoEvent(1000);
                    return null;
                }
                
                protected void deleteRelationship(LinkOperator lnk) throws NotFoundException{        
                    DiagramElementOperator assConnector = new DiagramElementOperator(dia, "AssClass",1);
                    //new DeleteElementAction().performShortcut(assConnector);
                    assConnector.getPopup().pushMenuNoBlock("Edit|Delete");
                    JDialogOperator delDlg = new JDialogOperator(Util.DELETE_DLG);
                    new EventTool().waitNoEvent(1000);
                    //JCheckBoxOperator chb = new JCheckBoxOperator(delDlg, 0);
                    //chb.clickMouse();
                    new EventTool().waitNoEvent(1000);
                    new JButtonOperator(delDlg, Util.YES_BTN).push();        
                    eventTool.waitNoEvent(1000);
                }
                
                protected void recoverRelatioship(DiagramElementOperator[] elements) throws NotFoundException{
                    eventTool.waitNoEvent(1000);
                    dia.toolbar().selectTool(DiagramToolbarOperator.RELATIONSHIP_DISCOVERY_TOOL);
                    eventTool.waitNoEvent(1000);        
                }
                
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                        DiagramElementOperator assConnector = null;
                        try
                        {
                            assConnector =new DiagramElementOperator(dia, "AssClass");
                        }
                        catch(Exception ex)
                        {
                            throw new KnownBugException(78701,"relationship discovery doesn't work fro assocuiation class");
                        }
                        DiagramElementOperator assClass = null;
                        assClass =new DiagramElementOperator(dia, "AssClass", 1);                        
                        new LinkOperator(source, assConnector);
                        new LinkOperator(assConnector, destination);                        
                        return true;
                }
                
            };
            
            boolean result = false;
            try
            {
                result=custom.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.ASSOCIATION_CLASS);
            }
            catch(KnownBugException ex)
            {
                fail(ex.getBugId(),ex.getMessage());
            }
            
            long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
            try{                        
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
                new JDialogOperator(EXCEPTION_DLG);
                //failedByBug = true;
                fail("Unknown exception");
            }catch(Exception excp){
            }finally{
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
            }
            
            if (!result){
                fail("testRecoverAssociationClass failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverGeneralization() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.GENERALIZATION);
            if (!result){
                fail("testRecoverGeneralization failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverImplementation() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, ElementTypes.INTERFACE, LinkTypes.IMPLEMENTATION);
            if (!result){
                fail("testRecoverImplementation failed. Reason unknown");
            }
    }
    
    
    
    public void testRecoverDependency() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.DEPENDENCY);
            if (!result){
                fail("testRecoverDependency failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverRealization() throws NotFoundException {
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.REALIZE);
            if (!result){
                fail("testRecoverRealization failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverUsage() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.USAGE);
            if (!result){
                fail("testRecoverUsage failed. Reason unknown");
            }
    }
     
    
    
    
    public void testRecoverPermission() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.PERMISSION);
            if (!result){
                fail("testRecoverPermission failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverAbstraction() throws NotFoundException {        
         lastTestCase=getCurrentTestMethodName();
            boolean result = classVerifier.verifyElementsRelationRecovery(ElementTypes.CLASS, LinkTypes.ABSTRACTION);
            if (!result){
                fail("testRecoverAbstraction failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverNestedLink() throws NotFoundException {
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){
                                
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                    return super.checkRelationshipOnDiagram(source, destination, linkType);
                }
                
            };
            
            try
            {
                boolean result = custom.verifyElementsRelationRecovery(ElementTypes.CLASS, ElementTypes.PACKAGE, LinkTypes.PACKAGE);
                if (!result)
                {
                    if(custom.checkRelationshipOnDiagram(ElementTypes.CLASS, ElementTypes.PACKAGE,LinkTypes.CLASS))fail(86394,"Link Discovery Tool changes link type for Nested Links");
                    fail("Verification failed");
                }
            }
            catch(ElementVerificationException ex)
            {
                //try
                {
                    
                    if(custom.checkRelationshipOnDiagram(ElementTypes.CLASS, ElementTypes.PACKAGE,LinkTypes.NESTED_LINK.NESTED_LINK("Class")))fail(86394,"Link Discovery Tool changes link type for Nested Links");
                }
                //catch(Exception rr)
                //{
                //   fail(ex.getMessage()+"//"+ex.getId());
                //}
                fail(ex.getMessage()+"//"+ex.getId());
            }
            
    }
    
    
    
    
    public void testRecoverDerivationEdge() throws NotFoundException {
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){
                protected void deleteRelationship(LinkOperator lnk) throws NotFoundException{                
                    new DeleteElementAction().performShortcut();
                    JDialogOperator delDlg = new JDialogOperator(Util.DELETE_DLG);
                    new EventTool().waitNoEvent(1000);
                    //JCheckBoxOperator chb = new JCheckBoxOperator(delDlg, 0);
                    //chb.clickMouse();
                    new EventTool().waitNoEvent(1000);
                    new JButtonOperator(delDlg, Util.YES_BTN).push();        
                    eventTool.waitNoEvent(1000);
                }
                
            };
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.DERIVATION_CLASSIFIER, ElementTypes.TEMPLATE_CLASS, LinkTypes.DERIVATION_EDGE);
            if (!result){
                fail("testRecoverDerivationEdge failed. Reason unknown");
            }
    }
    
    
    
    
    public void testRecoverRoleBinding() throws NotFoundException {
         lastTestCase=getCurrentTestMethodName();
            BaseRelatioshipRecoveryVerifier custom = new BaseRelatioshipRecoveryVerifier(classVerifier){                               
                protected boolean checkRelationshipOnDiagram(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
                    return super.checkRelationshipOnDiagram(source, destination, LinkTypes.PART_FACADE);
                }                
            };
            
            boolean result = custom.verifyElementsRelationRecovery(ElementTypes.DESIGN_PATTERN, ElementTypes.ROLE, LinkTypes.ROLE);
            
            long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
            try{                        
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
                new JDialogOperator(EXCEPTION_DLG);
                fail("Unknown exception");
            }catch(Exception excp){
            }finally{
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
            }
            
            if (!result){
                fail("testRecoverDerivationEdge failed. Reason unknown");
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
