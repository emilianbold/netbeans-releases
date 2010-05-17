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


/*
 * CLD_Modeling_Basics.java
 *
 * Created on April 20, 2005, 11:15 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.uml.classdiagram;
import java.awt.Component;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.actions.RenameAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.classdiagram.utils.CLDUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.actions.AddElementAction;
import org.netbeans.test.umllib.actions.AddPackageAction;
import org.netbeans.test.umllib.actions.CloseDiagramAction;
import org.netbeans.test.umllib.actions.NavigateToSourceAction;
import org.netbeans.test.umllib.actions.SourceControlRefreshStatusAction;
import org.netbeans.test.umllib.customelements.ClassOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.project.Project;



/**
 *
 * @author VijayaBabu Mummaneni
 */
public class CLDModelingBasics extends ClassDiagramTestCase {
    private String PROJECT_NAME = "UMLProject-1" ;
    
    String sketchpad = System.getProperty("xtest.sketchpad") ;
    String projLocation = sketchpad ;
//    String projLocation = "I:/UML_TEMP" ;
    private String EXCEPTION_DLG = "Exception";
    private boolean failedByBug = false;
    private static boolean isNotInitialized = true;
    
    /** Creates a new instance of CLD_Modeling_Basics */
    public CLDModelingBasics(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.classdiagram.CLDModelingBasics.class);
        return suite;
    }
    
    public void testCreateNewClassDiagram(){
        String NEW_PROJECT_NAME = "UMLProject-new" ;
        String classDiagramName = "CLDName";
        try {
            ProjectRootNode root = CLDUtils.createNewUMLProject(NEW_PROJECT_NAME, projLocation);
            assert(root!=null);
            Node cldNode = CLDUtils.createNewClassDiagram(classDiagramName, NEW_PROJECT_NAME);     
            assert(cldNode!=null);
            new Timeout("", 5000).sleep();
            DiagramOperator dop = new DiagramOperator(classDiagramName);
            assertTrue(dop!=null);
            UMLPaletteOperator tco = new UMLPaletteOperator();
            assertTrue(tco!=null);
        } catch(Exception e){
            e.printStackTrace();
            fail("Test Failed with Exception :" + e.toString());
        }
    }
    
    
    
    public void testVerifyElementsInPaletteForAnExistingCLD(){
        String waysToOpen = "pop-up" ;
//        String classDiagramName = "CLDNameToTestPltte";
       
        
        //There are bugs to open diagram, so use the diagram "CLDName"
        // that created in test testCreateNewClassDiagram
        String classDiagramName = "CLDName";
        Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToOpen);
      
        
        //new EventTool().waitNoEvent(1000);
        new Timeout("", 5000).sleep();
        DiagramOperator dop = new DiagramOperator(classDiagramName);
        assertTrue(dop!=null);
        UMLPaletteOperator tco = new UMLPaletteOperator();
        assertTrue(tco!=null);
        
        ElementTypes[] paletteElementTypes = CLDUtils.getExpectedCLDPaletteElementTypes();
        for(ElementTypes et : paletteElementTypes ){
            try {
                tco.selectToolByType(et);
            } catch(NotFoundException e){
                e.printStackTrace();
                fail("Element " + et + " does not exist in the palette" + e.toString());
            }
        }
// meteroa: Links are moved from palette        
//        LinkTypes[] paletteLinkTypes = CLDUtils.getExpectedCLDPaletteLinkTypes();
//        for(LinkTypes et : paletteLinkTypes ){
//            try {
//                tco.selectToolByType(et);
//            } catch(NotFoundException e){
//                e.printStackTrace();
//                fail("Link " + et + " does not exist in the palette" + e.toString());
//            }
//        }
// meteroa
        /*
        Enum[] paletteElementTypes = CLDUtils.getExpectedCLDPaletteElements();
        for(Enum et : paletteElementTypes ){
            try {
                tco.selectToolByType(et);
            } catch(NotFoundException e){
                e.printStackTrace();
                fail("Element " + et + " does not exist in the palette" + e.toString());
            }
        }
         */
        /* String[] groupNames = {CLD_PALETTE_GROUP_BASIC, CLD_PALETTE_GROUP_ROBUSTNESS, CLD_PALETTE_GROUP_DEPENDENCIES, CLD_PALETTE_GROUP_TEMPLATES, CLD_PALETTE_GROUP_ASSOCIATION, CLD_PALETTE_GROUP_DESIGNPATTERN, CLD_PALETTE_GROUP_COMMENTS};
                for(String groupName : groupNames ){
                    try {
                        new UMLPaletteOperator().expandGroup(groupName);
                        new UMLPaletteOperator().collapseGroup(groupName);
                    } catch(Exception e){
                        e.printStackTrace();
                        fail("Exception while expanding/collapsing group : " + groupName + e.toString());
                    }
                }*/
        dop.close();
    }
    
    
    public void testOpenAnExistingClassDiagram(){
        String[] waysToOpen = {"pop-up", "double-click"} ;
        String classDiagramName = "CLDNameToOpen";
        
        for(int i=0; i<waysToOpen.length; i++) {
            Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToOpen[i]);       
            new Timeout("", 5000).sleep();
            DiagramOperator dop = new DiagramOperator(classDiagramName);
            assertTrue(dop!=null);
            UMLPaletteOperator tco = new UMLPaletteOperator();
            assertTrue(tco!=null);
            
            dop.close();
        }
        
    }
    
    public void testCloseAnExistingClassDiagram(){
        String[] waysToClose = {"pop-up"} ;
        String classDiagramName = "CLDNameToClose";
        
        for(int i=0; i<waysToClose.length; i++) {
            Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToClose[i]);
            new Timeout("", 5000).sleep();
            DiagramOperator dop = new DiagramOperator(classDiagramName);
            assertTrue(dop!=null);
            UMLPaletteOperator tco = new UMLPaletteOperator();
            assertTrue(tco!=null);
            
            root = CLDUtils.closeDiagram(root, waysToClose[i], classDiagramName);
            //assertTrue(! new CloseDiagramAction().isEnabled(root));
            
            new Timeout("",2000);
            Component comp = DiagramOperator.findDiagram( classDiagramName, new Operator.DefaultStringComparator(true, true) );
            assertTrue("Unexpected Exception. Class Diagram is not closed using "+waysToClose[i]+"!", comp==null);
            
            
            //            try {
            //                dop = new DiagramOperator(classDiagramName);
            //            }catch(TimeoutExpiredException e){
            //               endTest(); // Sets the test status to 'finished' state (test passed)
            //            }catch(Exception e){
            //                fail("Unexpected Exception. We expected  TimeoutExpiredException!!!");
            //            }
        }
    }
    
    
    
    public void testRenameAnExistingClassDiagram(){
        String[] waysToRename = {"pop-up"} ;
        String classDiagramName = "CLDNameToRename";
        
        for(int i=0; i<waysToRename.length; i++) {
            try{
                String classDiagramNameNew = "CLDName_New" + i;
                Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToRename[i]);
                new Timeout("", 5000).sleep();
                DiagramOperator dop = new DiagramOperator(classDiagramName);
                assertTrue(dop!=null);
                UMLPaletteOperator tco = new UMLPaletteOperator();
                assertTrue(tco!=null);
                
                CLDUtils.renameNode(root, classDiagramNameNew, waysToRename[i]);
                assertTrue(root.getText().equals(classDiagramNameNew));
                try{
                    dop = new DiagramOperator(classDiagramNameNew);
                }catch(TimeoutExpiredException e1){
                    fail("Not found diagram with name '" + classDiagramNameNew + "'");
                }
//                assertTrue(dop!=null);
                
                dop.close();
            }catch(Exception e){
                e.printStackTrace(getLog());
                fail("Test failed with exception message: " + e.getMessage() + "(Details in log)");
            }
        }
    }
    
    public void testDeleteAnExistingClassDiagram(){
        String[] waysToDelete = {"pop-up"} ;
        String classDiagramName = "CLDNameToDelete";
        
        for(int i=0; i<waysToDelete.length; i++) {
            Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToDelete[i]);
            new Timeout("", 5000).sleep();
            DiagramOperator dop = new DiagramOperator(classDiagramName);
            assertTrue(dop!=null);
            UMLPaletteOperator tco = new UMLPaletteOperator();
            assertTrue(tco!=null);
            
            new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Confirm Object Deletion"), "Yes").push();
                }
            }).start();
            
            CLDUtils.deleteNode(root, waysToDelete[i]);
            new EventTool().waitNoEvent(2000);
            assertTrue(!root.isPresent());
            try{
                dop = new DiagramOperator(classDiagramName);
            }catch(TimeoutExpiredException e){
                endTest(); // Sets the test status to 'finished' state (test passed)
            }catch(Exception e){
                fail("Unexpected Exception. We expected  TimeoutExpiredException!!!");
            }
            
        }
    }
    
    
    //TBD
    /*
    public void testCopyClassesBetweenDiagrams(){
        String[] waysToDelete = {"pop-up"} ;
        String classDiagramName1 = "CLD1";
        String classDiagramName2 = "CLD2";
     
        for(int i=0; i<waysToDelete.length; i++) {
            Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, waysToDelete[i]);
            new Timeout("", 5000).sleep();
            DiagramOperator dop = new DiagramOperator(classDiagramName);
            assertTrue(dop!=null);
            UMLPaletteOperator tco = new UMLPaletteOperator();
            assertTrue(tco!=null);
     
            CLDUtils.deleteNode(root, waysToDelete[i]);
            assertTrue(!root.isPresent());
            dop = new DiagramOperator(classDiagramName);
            assertTrue(dop==null);
     
        }
    }
     **/
    
    public void verifyClassDiagramNodeContextualMenuPresence(){
        // String classDiagramName = "ClassDiagramToTestConetxtualmenu";
        String classDiagramName = "Class Diagram";
        Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, "pop-up");
        Action[] actions = {new OpenAction(), new AddPackageAction(), new AddElementAction(), new SaveAction(), new CloseDiagramAction(), new DeleteAction(), new RenameAction(), new NavigateToSourceAction(), new SourceControlRefreshStatusAction(), new PropertiesAction()};
        try {
            root.verifyPopup(actions);
        }catch(Exception e){
            e.printStackTrace();
            fail("Unexpected Exception !!!");
        }
    }
    
    public void testInsertAttribute(){
        //String PROJECT_NAME = "UMLProject-2" ;
        String classDiagramName = "CLDEmptyClassDiagram";
        Node root = CLDUtils.openDiagram(classDiagramName, PROJECT_NAME, "pop-up");
        new Timeout("", 5000).sleep();
        DiagramOperator dop = new DiagramOperator(classDiagramName);
        
        try {
            ClassOperator co = new ClassOperator(dop, "myClass");
            co.insertAttribute("private", "Vector", "myVector", null, true);
        } catch(Exception e){
            e.printStackTrace();
            fail("Unexpected Exception !!!");
        }
    }
    
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        
        if (isNotInitialized){
            Project.openProject(CLDUtils.CDFS_XTEST_PROJECT_DIR+"/"+PROJECT_NAME);
            isNotInitialized=false;
        }

        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 5000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
    }
    
    
    public void tearDown() {
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
        }
        org.netbeans.test.umllib.util.Utils.tearDown();
    }
    
}
