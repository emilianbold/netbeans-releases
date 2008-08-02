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



package org.netbeans.test.uml.dependencydiagram;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.dependencydiagram.utils.DependencyDiagramVerifier;
import org.netbeans.test.uml.dependencydiagram.utils.DependencyUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class DeploymentDiagramTests extends UMLTestCase {
    private final String projectName = "Depend_uml_dpd";
    private final String diagramNamePrefix = "test_diagram";
    public static int COUNT = 0;
    //public static boolean initialized = false;
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private DiagramOperator diagram = null;
    private DependencyDiagramVerifier verifier = null;
    
    
    
    
    private EventTool eventTool = new EventTool();
    
    public DeploymentDiagramTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(DeploymentDiagramTests.class);   
        return suite;
    }
    
    
    // GEneralization link tests
    
    public void testSrcComponentComponentGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.COMPONENT,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcNodeNodeGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.NODE,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcInterfaceInterfaceGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.INTERFACE,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcArtifactArtifactGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcComponentNodeGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.NODE,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcComponentInterfaceGeneralization(){
        try{
            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.INTERFACE,
                    LinkTypes.GENERALIZATION);
         } catch (TimeoutExpiredException e){
               // It is expected to have no link created between component and interface      
              assertTrue("There should not be generalization link between component and interface",
                      e.getMessage().startsWith("Wait with link chooser: Chooser for link of type Generalization"));
          }  



    }
    
    public void testSrcComponentArtifactGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.GENERALIZATION);



    }
    
    public void testSrcArtifactInterfaceGeneralization(){
          try {
            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.INTERFACE,
                    LinkTypes.GENERALIZATION);
          } catch (TimeoutExpiredException e){
               // It is expected to have no link created between component and interface      
              assertTrue("There should not be generalization link between artifact and interface",
                      e.getMessage().startsWith("Wait with link chooser: Chooser for link of type Generalization"));
          }  


    }
    
    public void testSrcNodeArtifactGeneralization(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.ARTIFACT,
                    LinkTypes.GENERALIZATION);



    }
    
    // Implementation link tests
    public void testSrcComponentInterfaceImplementation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.INTERFACE,
                    LinkTypes.IMPLEMENTATION);



    }
    
    //Assiciation
    public void testSrcComponentComponentAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.COMPONENT,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcNodeNodeAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.NODE,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcInterfaceInterfaceAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.INTERFACE,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcArtifactArtifactAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcComponentNodeAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.NODE,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcComponentInterfaceAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.COMPONENT,
                    ElementTypes.INTERFACE,
                    LinkTypes.ASSOCIATION);



    }
    
    public void testSrcComponentArtifactAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.ARTIFACT,
                    LinkTypes.ASSOCIATION);



    }
            

    // several elements with links
    public void testMultipleLinks1(){
            try {
            verifier.testSrcOf4LinkedElements(
                    ElementTypes.NODE, 
                    ElementTypes.NODE, LinkTypes.GENERALIZATION,
                    ElementTypes.INTERFACE, LinkTypes.GENERALIZATION,
                    ElementTypes.NODE, LinkTypes.ASSOCIATION);
           } catch (TimeoutExpiredException e){
               // It is expected to have no link created between component and interface      
              assertTrue("There should not be generalization link between node and interface",
                      e.getMessage().startsWith("Wait with link chooser: Chooser for link of type Generalization"));
          }  



    }
    
    public void testMultipleLinks2(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.COMPONENT, 
                    ElementTypes.COMPONENT, LinkTypes.GENERALIZATION,
                    ElementTypes.INTERFACE, LinkTypes.IMPLEMENTATION,
                    ElementTypes.NODE, LinkTypes.GENERALIZATION);



    }
    
    
    public void testMultipleLinks3(){
         try{
            verifier.testSrcOf4LinkedElements(
                    ElementTypes.ARTIFACT, 
                    ElementTypes.NODE, LinkTypes.ASSOCIATION,
                    ElementTypes.INTERFACE, LinkTypes.GENERALIZATION,
                    ElementTypes.COMPONENT, LinkTypes.ASSOCIATION);
          } catch (TimeoutExpiredException e){
               // It is expected to have no link created between component and interface      
              assertTrue("There should not be generalization link between artifact and interface",
                      e.getMessage().startsWith("Wait with link chooser: Chooser for link of type Generalization"));
          }  


    }
    
    public void testMultipleLinks4(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.ARTIFACT, 
                    ElementTypes.NODE, LinkTypes.ASSOCIATION,
                    ElementTypes.INTERFACE, LinkTypes.ASSOCIATION,
                    ElementTypes.COMPONENT, LinkTypes.ASSOCIATION);



    }
    
    protected void setUp(){
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
        //if (!initialized) { DependencyUtils.setDefaultPreferences(); initialized=true;}
        COUNT++;
        String diagramName = this.diagramNamePrefix+COUNT;
        diagram = DependencyUtils.openDiagram(projectName, diagramName, NewDiagramWizardOperator.DEPLOYMENT_DIAGRAM, workDir);
        if (diagram == null)
            fail("Can't open diagram '" + diagramName + "', project '" + projectName + "'.");

        verifier = new DependencyDiagramVerifier(diagram, this.getLog());
    }
    
    public void tearDown() {
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.tearDown();
        new EventTool().waitNoEvent(1000);        

        //close opened diagrams
        diagram.closeAllDocuments();

        new EventTool().waitNoEvent(1000);
    }
   
    
    
    
}
