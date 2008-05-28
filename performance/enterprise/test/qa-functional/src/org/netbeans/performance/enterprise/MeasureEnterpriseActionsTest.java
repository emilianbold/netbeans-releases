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

package org.netbeans.performance.enterprise;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.performance.enterprise.actions.AddNewBpelProcess;
import org.netbeans.performance.enterprise.actions.AddNewWSDLDocument;
import org.netbeans.performance.enterprise.actions.AddNewXMLDocument;
import org.netbeans.performance.enterprise.actions.AddNewXMLSchema;
import org.netbeans.performance.enterprise.actions.ApplyDesignPattern;
import org.netbeans.performance.enterprise.actions.BuildComplexProject;
import org.netbeans.performance.enterprise.actions.CreateBPELmodule;
import org.netbeans.performance.enterprise.actions.CreateCompositeApplication;
import org.netbeans.performance.enterprise.actions.NavigatorSchemaViewMode;
import org.netbeans.performance.enterprise.actions.OpenBPELproject;
import org.netbeans.performance.enterprise.actions.OpenComplexDiagram;
import org.netbeans.performance.enterprise.actions.SchemaNavigatorDesignView;
import org.netbeans.performance.enterprise.actions.SchemaNavigatorSchemaView;
import org.netbeans.performance.enterprise.actions.StartAppserver;
import org.netbeans.performance.enterprise.actions.SwitchToDesignView;
import org.netbeans.performance.enterprise.actions.SwitchToSchemaView;
import org.netbeans.performance.enterprise.actions.ValidateSchema;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureEnterpriseActionsTest {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("UI Responsiveness Enterprise Actions suite");
        
        // EPMeasureActions1
        suite.addTest(NbModuleSuite.create(CreateBPELmodule.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(CreateCompositeApplication.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddNewWSDLDocument.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddNewXMLSchema.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddNewXMLDocument.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(AddNewBpelProcess.class, ".*", ".*")); 

        // EPMeasureActions2
        suite.addTest(NbModuleSuite.create(ValidateSchema.class, ".*", ".*"));
//Memory Leak issue 129434, moved OpenSchemaView to 4th testbag
//        suite.addTest(NbModuleSuite.create(OpenSchemaView("testOpenSchemaView", "Open Schema View")); 

//TODO it's the same as SwitchSchemaView, isn't it ?                                     suite.addTest(NbModuleSuite.create(SchemaViewSwitchTest.class, ".*", ".*"));
        
        suite.addTest(NbModuleSuite.create(BuildComplexProject.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SwitchToDesignView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SwitchToSchemaView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(SchemaNavigatorDesignView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(ApplyDesignPattern.class, ".*", ".*"));
        
        // EPMeasureActions3
        // Disabled testGCProjects check to shorten run time        
//        suite.addTest(NbModuleSuite.create(WatchProjects("testInitGCProjects"));
        suite.addTest(NbModuleSuite.create(SchemaNavigatorSchemaView.class, ".*", ".*"));
        suite.addTest(NbModuleSuite.create(NavigatorSchemaViewMode.class, ".*", ".*"));
        

//TODO there is an password dialog solve before enable to run again        suite.addTest(NbModuleSuite.create(DeployProject("measureTime","Deploy Project"));
        suite.addTest(NbModuleSuite.create(OpenComplexDiagram.class, ".*", ".*"));     
//        suite.addTest(NbModuleSuite.create(OpenComplexDiagram("testGC","Open Complex Diagram - Test GC"));         
        suite.addTest(NbModuleSuite.create(OpenBPELproject.class, ".*", ".*"));

// Disabled testGCProjects check to shorten run time        
//        suite.addTest(NbModuleSuite.create(WatchProjects("testGCProjects"));
        
        
        // EPMeasureActions4
        // Disabled testGCProjects check to shorten run time        
//        suite.addTest(NbModuleSuite.create(WatchProjects("testInitGCProjects"));
//        suite.addTest(NbModuleSuite.create(OpenSchemaView("testOpenSchemaView", "Open Schema View")); 
//        suite.addTest(NbModuleSuite.create(OpenSchemaView("testOpenComplexSchemaView", "Open Complex Schema View"));
        suite.addTest(NbModuleSuite.create(StartAppserver.class, ".*", ".*"));
// Disabled testGCProjects check to shorten run time        
//        suite.addTest(NbModuleSuite.create(WatchProjects("testGCProjects"));

        
//        // Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testInitGCProjects"));
//        suite.addTest(new CreateBPELmodule("measureTime", "Create BPEL module"));
//        suite.addTest(new CreateCompositeApplication("measureTime", "Create Composite Application"));
//        suite.addTest(new AddNewWSDLDocument("measureTime", "Add New WSDL Document"));
//        suite.addTest(new AddNewXMLSchema("measureTime", "Add New XML Schema"));
//        suite.addTest(new AddNewXMLDocument("measureTime", "Add New XML Document"));
//        suite.addTest(new AddNewBpelProcess("measureTime", "Add New Bpel Process")); 
//// Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testGCProjects"));
//
//        
//        //        suite.addTest(new WatchProjects("testInitGCProjects"));
//        suite.addTest(new ValidateSchema("measureTime","Validate Schema"));
////Memory Leak issue 129434, moved OpenSchemaView to 4th testbag
////        suite.addTest(new OpenSchemaView("testOpenSchemaView", "Open Schema View")); 
//
////TODO it's the same as SwitchSchemaView, isn't it ?                                     suite.addTest(new SchemaViewSwitchTest("measureTime", "Schema View Switch"));
//        
//        suite.addTest(new BuildComplexProject("measureTime", "Build Complex Project"));
//        
//        suite.addTest(new SwitchToDesignView("measureTime", "Schema | Switch to Design View"));
//        suite.addTest(new SwitchToSchemaView("measureTime", "Schema | Switch to Schema View"));
//        suite.addTest(new SchemaNavigatorDesignView("measureTime", "Schema Navigator Design View"));
//        suite.addTest(new ApplyDesignPattern("measureTime", "Apply Design Pattern"));
//// No objects to track reported when OpenSchemaView is disabled
////        suite.addTest(new WatchProjects("testGCProjects"));
//        
//        
//        // Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testInitGCProjects"));
//        suite.addTest(new SchemaNavigatorSchemaView("measureTime", "Schema Navigator Schema View"));
//        suite.addTest(new NavigatorSchemaViewMode("measureTime","Schema Navigator Schema View mode"));
//        
//
////TODO there is an password dialog solve before enable to run again        suite.addTest(new DeployProject("measureTime","Deploy Project"));
//        suite.addTest(new OpenComplexDiagram("measureTime","Open Complex Diagram"));         
////        suite.addTest(new OpenComplexDiagram("testGC","Open Complex Diagram - Test GC"));         
//        suite.addTest(new OpenBPELproject("measureTime","Open BPEL Project"));
//
//// Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testGCProjects"));
//        
//        
//        // Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testInitGCProjects"));
//        suite.addTest(new OpenSchemaView("testOpenSchemaView", "Open Schema View")); 
//        suite.addTest(new OpenSchemaView("testOpenComplexSchemaView", "Open Complex Schema View"));
//        suite.addTest(new StartAppserver("measureTime","Start Appserver"));
//// Disabled testGCProjects check to shorten run time        
////        suite.addTest(new WatchProjects("testGCProjects"));
//        /* TBD
//        suite.addTest(NbModuleSuite.create(AddToFavorites.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CloseAllEditors.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CloseEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CloseEditorModified.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CloseEditorTab.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CreateNBProject.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(CreateProject.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(DeleteFolder.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(ExpandNodesInComponentInspector.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(ExpandNodesProjectsView.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(JavaCompletionInEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFiles.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFilesNoCloneableEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFilesNoCloneableEditorWithOpenedEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFilesWithOpenedEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFormFile.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenFormFileWithOpenedEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenJspFile.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(OpenJspFileWithOpenedEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(PageUpPageDownInEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(PasteInEditor.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(RefactorFindUsages.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(SaveModifiedFile.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(SelectCategoriesInNewFile.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(SwitchToFile.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(SwitchView.class, ".*", ".*"));
//        suite.addTest(NbModuleSuite.create(TypingInEditor.class, ".*", ".*"));
//*/       
        return suite;
    }
    
}
