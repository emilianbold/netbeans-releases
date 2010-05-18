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


package org.netbeans.test.uml.re.operation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramTypes;
import org.netbeans.test.umllib.UMLWidgetOperator;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.Utils;

/**
 *
 * @author Alexandr Scherbatiy
 */



public class REOperationTestCase extends UMLTestCase{
    
    protected static JavaProject javaProject = null;
    protected static UMLProject  umlProject   = null;
    
    protected String projectDir = Utils.WORK_DIR + "/user/data";
    
    
    /** Creates a new instance of RETestCase */
    public REOperationTestCase(String name) {
        super(name);
        System.setOut(getLog());
    }
    
    
    public static NbTestSuite suite() {
        return new NbTestSuite(REOperationTestCase.class);
    }
    
    protected void delay(int millis){
        try{ Thread.sleep(millis); } catch(Exception e){ e.printStackTrace();}
    }
    
    
    public void openProject(String umlProjectName, String javaProjectName){ 
        if(umlProject == null || !umlProject.getName().equals(umlProjectName)){
            //openProject(projectDir + "/" + javaProjectName);
            Project.openProject(XTEST_PROJECT_DIR + "/Projects-RE/REOperation/" + javaProjectName);
            delay(2000);
            javaProject = new JavaProject(javaProjectName, ProjectType.JAVA_APPLICATION);
            //umlProject = UMLProject.createProject(umlProjectName, ProjectType.UML_JAVA_REVERSE_ENGINEERING, javaProject);
            delay(2000);
            umlProject = javaProject.reverseEngineer(umlProjectName);  

        }
    }
    
    //    public void openProject(String projectPath){
    //        JMenuBarOperator menuBar = new JMenuBarOperator(MainWindowOperator.getDefault());
    //
    //        menuBar.pushMenuNoBlock("File|Open Project...");
    //
    //        JDialogOperator dialog = new JDialogOperator("Open Project");
    //
    //        JTextFieldOperator textField = new JTextFieldOperator(dialog, 1);
    //        textField.setText(projectPath);
    //
    //        JButtonOperator button = new JButtonOperator(dialog, "Open Project Folder");
    //        button.pushNoBlock();
    //
    //        Utils.waitScanningClassPath();
    //
    //    }
    
    public void showDiagramElements(DiagramOperator diagramOperator){
        System.out.println(" ------ Show Diagram Elements ------ ");
        
        System.out.println("diagram name = " + diagramOperator.getName() + "\n");
        
        
        ArrayList<DiagramElementOperator>  diagramElements = diagramOperator.getAllDiagramElements();
        
        for(DiagramElementOperator diagramElement: diagramElements){
            System.out.println("   ---  ------------  ---  ");
            showDiagramElement(diagramElement);
            
        }
        
        System.out.println(" ----------------------------------- ");
    }
    
    
    public void showDiagramElement(DiagramElementOperator diagramElement) {

        System.out.println("name = " + diagramElement.getName());
        System.out.println("type = " + diagramElement.getType());
        Widget widget = diagramElement.getGraphObject();

        //TODO:  Will a long list.find what should be list here
        List<Widget> compList = widget.getChildren();
        for (Widget child : compList) {
            System.out.println(" compartment name = " + (new UMLWidgetOperator(child)).getName());
        }
        if (widget instanceof UMLEdgeWidget) {
            Widget edgeWidget = (Widget) widget;
            List<Widget> list = edgeWidget.getChildren();
            for (Widget child : list) {
                if (child instanceof UMLLabelWidget) {
                    System.out.println("  label name = \"" + ((UMLLabelWidget)child).getLabel() + "\"");
                }
            }
        }
    }

    public void createDiagram(String node, String diagramName) {

        try{
            umlProject.reverseEngineerOperation(node, diagramName, DiagramTypes.SEQUENCE);
        }catch(TimeoutExpiredException  e){
            failByBug(90734, "RE Operation: Performance regression issue");
        }
        delay(6000);
        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.tearDown();
    }
    
    public void assertNull(int bugNumber, String description, Object obj){
        if(obj != null){
            failByBug(bugNumber, description);
        }
    }
    
    
}
