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
 * REJavaApplication.java
 *
 * Created on January 19, 2006, 1:55 PM
 *
 */
package org.netbeans.test.uml.tutorial.reverseeng;

import java.awt.Point;
import java.awt.event.InputEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.DiagramTypes;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.PopupConstants;
import org.netbeans.test.umllib.util.Utils;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.util.JPopupByPointChooser;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class REJavaApplication extends UMLTestCase {

    public static final String PROJECT_NAME_UML = "UMLProjectTutorialRE";
    public static final String PROJECT_NAME_JAVA = "BankApp";
    public static final String DIAGRAM_NAME_BANK_CLASS = "BankClassDiagram";
    public static final String DIAGRAM_NAME_BANK_ACCOUNT = "BankAccountDependencies";
    public static final String DIAGRAM_NAME_SEQUENCE = "withdrawSD";
    ProjectsTabOperator pto;
    JTreeOperator prTree;
    ProjectRootNode root;
    Node modelNode;
    Node banckpackNode;
    Node bankAccountClassNode;

    /** Creates a new instance of REJavaApplication */
    public REJavaApplication(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
//        System.out.println("Set Up!!!");
//        return new NbTestSuite(REJavaApplication.class);
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new REJavaApplication("testOpenJavaApplication"));
        suite.addTest(new REJavaApplication("testREJavaApplication"));
       //TODO: DependencyDiagram is not implemented yet
       // suite.addTest(new REJavaApplication("testClassDependencyDiagram"));
      //  suite.addTest(new REJavaApplication("testNodeDependencyDiagram"));
        suite.addTest(new REJavaApplication("testSequenceDiagram"));
        suite.addTest(new REJavaApplication("testAttributes"));
        suite.addTest(new REJavaApplication("testOperations"));
        suite.addTest(new REJavaApplication("testRelationships"));
        return suite;
    }

    protected void setUp() {
        Utils.waitScanningClassPath();
        //Debug.showLog(this);
    }

    public void testOpenJavaApplication() {
        Project.openProject(XTEST_PROJECT_DIR + "/Projects-Tutorials/REJavaApplication/BankApp");
        new JavaProject("BankApp").build();
    }

    public void testREJavaApplication() {
        System.out.println();
        System.out.println("Start: Test Reverse Engineering Java Application");
        System.out.println();


        JavaProject javaProject = new JavaProject(PROJECT_NAME_JAVA, ProjectType.JAVA_APPLICATION);

        UMLProject umlProject = UMLProject.createProject(PROJECT_NAME_UML, ProjectType.UML_JAVA_REVERSE_ENGINEERING, javaProject);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        Node root = umlProject.getProjectNode();

        Node modelNode = new Node(root, "Model");
        Node banckpackNode = new Node(modelNode, "bankpack");

        //DiagramOperator bankClassDiagram = DiagramOperator.createDiagram("BankClassDiagram", DiagramTypes.CLASS, banckpackNode);
        DiagramOperator bankClassDiagram = createDiagramFromSelectedElements(DIAGRAM_NAME_BANK_CLASS, DiagramTypes.CLASS, banckpackNode);


        new DiagramElementOperator(bankClassDiagram, "Account", ElementTypes.INTERFACE);
        new DiagramElementOperator(bankClassDiagram, "BankAccount", ElementTypes.CLASS);
        new DiagramElementOperator(bankClassDiagram, "History", ElementTypes.CLASS);
        new DiagramElementOperator(bankClassDiagram, "Platinum", ElementTypes.CLASS);

        bankClassDiagram.maximize();
        DiagramToolbarOperator toolbar = bankClassDiagram.toolbar();


//        TODO:toolbar is not fully implemented
//        new JButtonOperator(toolbar.getButtonByTooltip(DiagramToolbarOperator.FIT_TO_WINDOW_TOOL)).push();
//
//         JToggleButtonOperator(toolbar.getToggleButtonByTooltip(DiagramToolbarOperator.SYMMETRIC_LAYOUT_TOOL)).push();

//        JDialogOperator layoutDialog = new JDialogOperator("Layout");
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//
//        new JCheckBoxOperator(layoutDialog).setSelected(true);
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//
//        new JButtonOperator(layoutDialog, "Yes").push();
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }

//        new JToggleButtonOperator(toolbar.getToggleButtonByTooltip(DiagramToolbarOperator.HIERARCHICAL_LAYOUT_TOOL)).push();

//        try {
//            new JButtonOperator(new JDialogOperator("Layout"), "Yes").push();
//        } catch (Exception e) {
//        }
//        new Timeout("", 2000);


//        new JToggleButtonOperator(toolbar.getToggleButtonByTooltip(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL)).push();
//        try {
//            new JButtonOperator(new JDialogOperator("Layout"), "Yes").push();
//        } catch (Exception e) {
//        }
//
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }


//
//        new JComboBoxOperator(bankClassDiagram).selectItem("50%");
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//
//
//        new JButtonOperator(toolbar.getButtonByTooltip(DiagramToolbarOperator.OVERVIEW_WINDOW_TOOL)).push();
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//
//        JDialogOperator overviewDialog = new JDialogOperator("Overview");
//
//        int x = overviewDialog.getCenterX();
//        int y = overviewDialog.getCenterY();
//
//        int d = 10;
//        //overviewDialog.clickMouse( x - d, y - d, 1);
//        overviewDialog.moveMouse(x - d, y - d);
//        overviewDialog.dragMouse(x + d, y + d);
//
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//        }
//        overviewDialog.close();
//
//        System.out.println("End  : Test Reverse Engineering Java Application");
//        System.out.println("");
    }

    public void testClassDependencyDiagram() {
        init();

        //DiagramOperator bankClassDiagram  = DiagramOperator(new modelNode );
        DiagramOperator bankClassDiagram = new DiagramOperator(DIAGRAM_NAME_BANK_CLASS);

        DiagramElementOperator bankAccountClass = bankClassDiagram.getDiagramElement("BankAccount");

        //Call Bankacount class element's popup meun   
        Point loc = bankAccountClass.getBoundingRect().getLocation();
        loc.translate(10, 5);  
        bankAccountClass.clickOn(loc, 1, InputEvent.BUTTON3_MASK, 0);
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
        JPopupMenuOperator popup = new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container) (MainWindowOperator.getDefault().getSource()), new JPopupByPointChooser(loc, bankClassDiagram.getDrawingArea().getSource(), 0)));

       // It does not work, it selects one of operator and invoke different popup
       // JPopupMenuOperator popup = bankAccountClass.getPopup();

        boolean isBug = false;
        try {
            // known bug on Unix OSs
            popup.pushMenu(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM);
        } catch (TimeoutExpiredException e) {
            isBug = true;
        }

        if (isBug) {
            // try to open popup menu again
            bankAccountClass.select();
            popup = bankAccountClass.getPopup();
            popup.pushMenu(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM);
        }

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        boolean isDependencyDiagram = bankAccountClassNode.isChildPresent(DIAGRAM_NAME_BANK_ACCOUNT);
        assertTrue(6377386, " " + DIAGRAM_NAME_BANK_ACCOUNT + " Diagram is not created.", isDependencyDiagram);

        assertFalse(6324226, "Solaris10-Sparc:Contextual menu items of class element missing in class diagram.", isBug);
    }

    public void testNodeDependencyDiagram() {
        init();

        //Node bankAccountNode = new Node();
        bankAccountClassNode.callPopup().pushMenu(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        boolean isDependencyDiagram = bankAccountClassNode.isChildPresent(DIAGRAM_NAME_BANK_ACCOUNT);
        assertTrue(DIAGRAM_NAME_BANK_ACCOUNT + " Diagram is not created.", isDependencyDiagram);

        DiagramOperator bankAccountClassDiagram = new DiagramOperator(DIAGRAM_NAME_BANK_ACCOUNT);

        new DiagramElementOperator(bankAccountClassDiagram, "Account", ElementTypes.INTERFACE);
        new DiagramElementOperator(bankAccountClassDiagram, "History", ElementTypes.CLASS);

        new DiagramElementOperator(bankAccountClassDiagram, "Object", ElementTypes.DATATYPE, 0);
        new DiagramElementOperator(bankAccountClassDiagram, "String", ElementTypes.DATATYPE, 0);

        DiagramElementOperator bankAccountElement = new DiagramElementOperator(bankAccountClassDiagram, "BankAccount", ElementTypes.CLASS);

        //HashSet<LinkOperator> links = bankAccountElement.getLinks();
        System.out.println("   set size = " + bankAccountElement.getLinks().size());
        for (LinkOperator link : bankAccountElement.getLinks()) {
            System.out.println("   link = \"" + link + "\"");
        }



        int linkSize = bankAccountElement.getLinks().size();

        assertTrue("BankAccount class should have 2 links instead of " + linkSize + ".", linkSize == 2);
    }

    public void testSequenceDiagram() {
        init();

        Node operationsNode = new Node(bankAccountClassNode, "Operations");


        //for(String name : operationsNode.getChildren()){
        //    System.out.println("  child = \"" + name + "\"");
        //}

        Node withdrawOperationNode = new Node(operationsNode, "public void  withdraw( double val )");
        System.out.println("Node = \"" + withdrawOperationNode + "\"");
        withdrawOperationNode.callPopup().pushMenuNoBlock(PopupConstants.REVERSE_ENGINEER_OPERATION);


        NewDiagramWizardOperator wiz = new NewDiagramWizardOperator();
        wiz.setDiagramType(DiagramTypes.SEQUENCE.toString());
        wiz.setDiagramName(DIAGRAM_NAME_SEQUENCE);
        wiz.clickFinish();


        DiagramOperator sequenceDiagram = new DiagramOperator(DIAGRAM_NAME_SEQUENCE);

        //for(DiagramElementOperator elem : sequenceDiagram.getDiagramElements()){
        //    System.out.println("   elem name = \"" + elem.getSubjectVNs().get(0) + "\"");
        //}
        new DiagramElementOperator(sequenceDiagram, "val", ElementTypes.LIFELINE);
        new DiagramElementOperator(sequenceDiagram, "self", ElementTypes.LIFELINE);
        new DiagramElementOperator(sequenceDiagram, "balance", ElementTypes.LIFELINE);
        new DiagramElementOperator(sequenceDiagram, "mHistory", ElementTypes.LIFELINE);
    }

    public void testAttributes() {
        init();


        // =============   Check Attributes ==================================
        Node bankAccountAttribute = new Node(bankAccountClassNode, "Attributes");

        //        for(String str : bankAccountAttribute.getChildren() ){
        //            System.out.println("***  attribute child = \"" + str + "\"");
        //        }
        String[] attribute = bankAccountAttribute.getChildren();

        assertTrue("There should be 3 attributes instead of " + attribute.length + ".", attribute.length == 3);
        assertTrue("There should be \"private double balance\" attribute.", "private double balance".equals(attribute[0]));
        assertTrue("There should be \"private double interestRate\" attribute.", "private double interestRate".equals(attribute[1]));
        assertTrue("There should be \"private String accountNumber\" attribute.", "private String accountNumber".equals(attribute[2]));
    }

    public void testOperations() {
        init();

        // =============   Check Operations  ==================================
        Node bankAccountOperation = new Node(bankAccountClassNode, "Operations");
        String[] operation = bankAccountOperation.getChildren();

        assertTrue("There should be 18 operations instead of " + operation.length + ".", operation.length == 18);
        assertTrue("There should be \"private void  noAvailableFunds(  )\" operation.", "private void  noAvailableFunds(  )".equals(operation[0]));
    }

    public void testRelationships() {
        init();

        Node bankAccountRelationships = new Node(bankAccountClassNode, "Relationships");

        String[] relationship = bankAccountRelationships.getChildren();
        assertTrue("There should be 3 relationships instead of " + relationship.length + ".", relationship.length == 3);
        assertTrue("There should be \"Specializations\" relationship.", "Specializations".equals(relationship[0]));
        assertTrue("There should be \"Aggregation\" relationship.", "Aggregation".equals(relationship[1]));
        assertTrue("There should be \"Implementation\" relationship.", "Implementation".equals(relationship[2]));


        // Implementation -> Account
        Node bankAccountImplementation = new Node(bankAccountRelationships, "Implementation");

        //Debug.showNode(bankAccountImplementation);
        String[] implementation = bankAccountImplementation.getChildren();

        assertTrue("There should be 1 class instead of " + implementation.length + ".", implementation.length == 1);
        assertTrue("There should be \"Account\" implementation class.", "Account".equals(implementation[0]));


        // Generalization -> Checking
        //Node bankAccountGeneralizations = new Node(bankAccountRelationships, "Specializations|Generalization");
        Node bankAccountGeneralizations = new Node(new Node(bankAccountRelationships, "Specializations"), 1);

        //Debug.showNode(bankAccountGeneralizations);
        String[] generalization = bankAccountGeneralizations.getChildren();


        assertTrue("There should be 1 class instead of " + generalization.length + ".", generalization.length == 1);
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
        } 
        //There are should be 3 generation nodes. Saving, Checking and Platinum. 
        // The order may be different on different platform.
        assertTrue("The first generation should be either \"Saving\", or \"Checking\" or \"Platinum\" generalization class.", 
                   "Saving".equals(generalization[0])||"Checking".equals(generalization[0])||"Platinum".equals(generalization[0]));
    }

    /*
     */
    protected void init() {
        pto = ProjectsTabOperator.invoke();
        prTree = new JTreeOperator(pto);
        root = new ProjectRootNode(pto.tree(), PROJECT_NAME_UML);

        modelNode = new Node(root, "Model");
        banckpackNode = new Node(modelNode, "bankpack");

        bankAccountClassNode = new Node(banckpackNode, "BankAccount");
    }

    protected static DiagramOperator createDiagramFromSelectedElements(String diagramName, Node projectNode) {
        return createDiagramFromSelectedElements(diagramName, DiagramTypes.CLASS, projectNode);
    }

    protected static DiagramOperator createDiagramFromSelectedElements(String diagramName, DiagramTypes diagramType, Node projectNode) {


        projectNode.callPopup().pushMenuNoBlock("Create Diagram From Selected Elements...");
        NewDiagramWizardOperator dw = new NewDiagramWizardOperator();
        dw.setDiagramType(diagramType.toString());
        dw.setDiagramName(diagramName);
        dw.clickFinish();

        JDialogOperator createFromSelected = new JDialogOperator("Create Diagram From Selected");
        new JButtonOperator(createFromSelected, "Yes").push();

        return new DiagramOperator(diagramName);
    }
}
