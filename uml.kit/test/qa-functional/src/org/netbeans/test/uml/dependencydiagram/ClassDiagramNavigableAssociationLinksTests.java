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
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.dependencydiagram.utils.DependencyDiagramVerifier;
import org.netbeans.test.uml.dependencydiagram.utils.DependencyUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class ClassDiagramNavigableAssociationLinksTests extends UMLTestCase {
    private final String projectName = "Depend_uml_cldNA";
    private final String diagramNamePrefix = "test_diagram";
    public static int COUNT = 0;
    //public static boolean initialized = false;
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private DiagramOperator diagram = null;
    private DependencyDiagramVerifier verifier = null;
    
    
    
    
    private EventTool eventTool = new EventTool();
    
    public ClassDiagramNavigableAssociationLinksTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ClassDiagramNavigableAssociationLinksTests.class);
        return suite;
    }
    

    // NavAssociation link tests
    public void testSrcClassClassNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.CLASS,
                    ElementTypes.CLASS,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcInterfaceInterfaceNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.INTERFACE,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcEnumEnumNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ENUMERATION,
                    ElementTypes.ENUMERATION,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcNodeNodeNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.NODE,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }

    public void testSrcDatatypeDatatypeNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.DATATYPE,
                    ElementTypes.DATATYPE,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcArtifactArtifactNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcAliasedAliasedNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ALIASED,
                    ElementTypes.ALIASED,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcUtilityUtilityNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.UTILITY_CLASS,
                    ElementTypes.UTILITY_CLASS,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    public void testSrcActorActorNavAssociation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ACTOR,
                    ElementTypes.ACTOR,
                    LinkTypes.NAVIGABLE_ASSOCIATION);



    }
    
    
    // NavComposition link tests
    public void testSrcClassClassNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.CLASS,
                    ElementTypes.CLASS,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcInterfaceInterfaceNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.INTERFACE,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcEnumEnumNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ENUMERATION,
                    ElementTypes.ENUMERATION,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcNodeNodeNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.NODE,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }

    public void testSrcDatatypeDatatypeNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.DATATYPE,
                    ElementTypes.DATATYPE,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcArtifactArtifactNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcAliasedAliasedNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ALIASED,
                    ElementTypes.ALIASED,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcUtilityUtilityNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.UTILITY_CLASS,
                    ElementTypes.UTILITY_CLASS,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testSrcActorActorNavComposition(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ACTOR,
                    ElementTypes.ACTOR,
                    LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
 
    // NavAggregation link tests
    public void testSrcClassClassNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.CLASS,
                    ElementTypes.CLASS,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcInterfaceInterfaceNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.INTERFACE,
                    ElementTypes.INTERFACE,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcEnumEnumNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ENUMERATION,
                    ElementTypes.ENUMERATION,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcNodeNodeNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.NODE,
                    ElementTypes.NODE,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }

    public void testSrcDatatypeDatatypeNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.DATATYPE,
                    ElementTypes.DATATYPE,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcArtifactArtifactNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ARTIFACT,
                    ElementTypes.ARTIFACT,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcAliasedAliasedNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ALIASED,
                    ElementTypes.ALIASED,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcUtilityUtilityNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.UTILITY_CLASS,
                    ElementTypes.UTILITY_CLASS,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    public void testSrcActorActorNavAggregation(){

            verifier.testSrcOf2LinkedElements(
                    ElementTypes.ACTOR,
                    ElementTypes.ACTOR,
                    LinkTypes.NAVIGABLE_AGGREGATION);



    }
    
    
            
    // several elements with links
    public void testMultipleLinks1(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.CLASS, 
                    ElementTypes.CLASS, LinkTypes.NAVIGABLE_AGGREGATION,
                    ElementTypes.INTERFACE, LinkTypes.NAVIGABLE_ASSOCIATION,
                    ElementTypes.ARTIFACT, LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    public void testMultipleLinks2(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.ENUMERATION, 
                    ElementTypes.CLASS, LinkTypes.COMPOSITION,
                    ElementTypes.INTERFACE, LinkTypes.ASSOCIATION,
                    ElementTypes.CLASS, LinkTypes.NAVIGABLE_COMPOSITION);



    }
    
    
    public void testMultipleLinks3(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.INTERFACE, 
                    ElementTypes.DATATYPE, LinkTypes.AGGREGATION,
                    ElementTypes.ALIASED, LinkTypes.ASSOCIATION,
                    ElementTypes.ENUMERATION, LinkTypes.ASSOCIATION);



    }
    
    public void testMultipleLinks4(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.ARTIFACT, 
                    ElementTypes.CLASS, LinkTypes.COMPOSITION,
                    ElementTypes.INTERFACE, LinkTypes.NAVIGABLE_COMPOSITION,
                    ElementTypes.PACKAGE, LinkTypes.NESTED_LINK);



    }
    
    public void testMultipleLinks5(){

            verifier.testSrcOf4LinkedElements(
                    ElementTypes.CLASS, 
                    ElementTypes.ARTIFACT, LinkTypes.NAVIGABLE_ASSOCIATION,
                    ElementTypes.ACTOR, LinkTypes.NESTED_LINK,
                    ElementTypes.INTERFACE, LinkTypes.IMPLEMENTATION);



    }
    
     protected void setUp(){
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
        //if (!initialized) { DependencyUtils.setDefaultPreferences(); initialized=true;}
        COUNT++;
        String diagramName = this.diagramNamePrefix+COUNT;
        diagram = DependencyUtils.openDiagram(projectName, diagramName, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
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
