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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 * UMLValidation.java
 * Created on Jul 31, 2007, 9:49:19 PM
 */

package org.netbeans.test.uml.validation;

//import java.util.LinkedList;
import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbModuleSuite;
//import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramTypes;
//import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.util.Utils;
//import org.netbeans.test.umllib.values.Arg;
import org.netbeans.jellytools.nodes.Node;
import org.openide.util.Exceptions;
 
/**
 *
 * @author Sherry Zhou
 */


public class UMLValidationTests extends UMLTestCase {

    public static final String PROJECT_NAME_JAVA = "BankApp";
    public static final String PROJECT_NAME_UML1 = "UMLPrj1";
    public static final String PROJECT_NAME_UML2 = "UMLPrj2";
    public static final String PROJECT_NAME_UML3 = "UMLPrj3";

    public static final String DIAGRAM_CLASS_NAME1 = "JavaPlatformClassDiagram";
    public static final String DIAGRAM_CLASS_NAME2 = "PlatformIndependentClassDiagram";
    public static final String DIAGRAM_CLASS_NAME3 = "REClassDiagram";

    static String[] tests = new String[]{
            "testCreateJavaPlatformProject",
            "testCreatePlatformIndependentProject",
            "testCreateREJavaPlatformProject",
            "testUMLMainMenu"
    };

    /** Creates a new instance of DevelopingApplications */
    public UMLValidationTests(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(UMLValidationTests.class)
                .addTest(tests)
                .clusters(".*")
                .enableModules(".*")
                .gui(true)
                );
    }



    protected void setUp() {
        System.setOut(getLog());
        Utils.waitScanningClassPath();
    }


    /* 
     * Verify UML related main menus
     */
    public void testUMLMainMenu() {
        // Select main menu Edit->Find in UML model
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Find in UML Model");
        new JButtonOperator(new JDialogOperator("Find"), "Close").pushNoBlock();
        
        // Select main menu Edit->Replace in UML model
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Edit|Replace in UML Model");
        new JButtonOperator(new JDialogOperator("Replace"), "Close").pushNoBlock();
       
        // Select main menu Windows->Other->UML Documentation
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Other|UML Documentation");
        try {

            new TopComponentOperator(TopComponentOperator.findTopComponent("UML Documentation", 0));
        } catch (Exception ex) {
        }

        // Select main menu Windows->Other->UML Documentation UML Design Center
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Other|UML Design Center");
        try {

            new TopComponentOperator(TopComponentOperator.findTopComponent("UML Design Center", 0));
        } catch (Exception ex) {
        }
    }
 
    /* 
     * Create   Java-Platform Model Project
     */
    public void testCreateJavaPlatformProject() {
        UMLProject umlProject = UMLProject.createProject(PROJECT_NAME_UML1, ProjectType.UML_JAVA_PLATFORM_MODEL);
        Node umlModelNode = new Node(umlProject.getProjectNode(), "Model");

        DiagramOperator classDiagram = DiagramOperator.createDiagram(DIAGRAM_CLASS_NAME1, DiagramTypes.CLASS, umlModelNode);
        new TopComponentOperator(DIAGRAM_CLASS_NAME1);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new TopComponentOperator(DIAGRAM_CLASS_NAME1);
    }

    /* 
     * Create Platform Independent Model Project
     */
    public void testCreatePlatformIndependentProject() {
        UMLProject umlProject = UMLProject.createProject(PROJECT_NAME_UML2, ProjectType.UML_PLATFORM_INDEPENDET_MODEL);
        Node umlModelNode = new Node(umlProject.getProjectNode(), "Model");

        DiagramOperator classDiagram = DiagramOperator.createDiagram(DIAGRAM_CLASS_NAME2, DiagramTypes.CLASS, umlModelNode);
        new TopComponentOperator(DIAGRAM_CLASS_NAME2);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new TopComponentOperator(DIAGRAM_CLASS_NAME2);
    }

    /*
     * Create Reversed Engineering project
     */
    public void testCreateREJavaPlatformProject() {
        String data_dir = getDataDir().getAbsolutePath();
     //   data_dir = System.getProperty("nbjunit.workdir");
        System.err.println("==============================================");
        System.err.println(data_dir);
        System.err.println(System.getProperty("nbjunit.workdir"));
        System.out.println("==============================================");
        Project.openProject(data_dir + "/Projects-Tutorials/REJavaApplication/BankApp");

 //       Project.openProject(XTEST_PROJECT_DIR + "/Projects-Tutorials/REJavaApplication/BankApp");

        JavaProject javaProject = new JavaProject(PROJECT_NAME_JAVA, ProjectType.JAVA_APPLICATION);

        UMLProject umlProject = UMLProject.createProject(PROJECT_NAME_UML3, ProjectType.UML_JAVA_REVERSE_ENGINEERING, javaProject);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
        Node root = umlProject.getProjectNode();

        Node modelNode = new Node(umlProject.getProjectNode(), "Model");
        Node banckpackNode = new Node(modelNode, "bankpack");


        //DiagramOperator bankClassDiagram = DiagramOperator.createDiagram("BankClassDiagram", DiagramTypes.CLASS, banckpackNode);
        DiagramOperator bankClassDiagram = createDiagramFromSelectedElements(DIAGRAM_CLASS_NAME3, DiagramTypes.CLASS, banckpackNode);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new TopComponentOperator(DIAGRAM_CLASS_NAME3);
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
