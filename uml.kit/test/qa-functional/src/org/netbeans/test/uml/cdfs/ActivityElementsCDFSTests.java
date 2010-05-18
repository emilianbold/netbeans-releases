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


package org.netbeans.test.uml.cdfs;



import org.netbeans.test.uml.cdfs.utils.CDFSUtil;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;





public class ActivityElementsCDFSTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    
    public ActivityElementsCDFSTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ActivityElementsCDFSTests.class);
        return suite;
    }
    
    
    /******************const section*************************/
    private String PROJECT_NAME = "CDFS_uml";
    private String EXCEPTION_DLG = "Exception";
    /********************************************************/
    
    private static boolean isNotInitialized = true;
    CDFSUtil util = new CDFSUtil(PROJECT_NAME);
    
    
    public void testCDFSComplex(){
        final String PATH_TO_OBJ1 = "Model|ActivityDiagram|InitialNode";
        final String PATH_TO_OBJ2 = "Model|ActivityDiagram|ActivityGroup";
        final String OBJ_NAME1 = "InitialNode";
        final String OBJ_NAME2 = "ActivityGroup";
        final String DIA_NAME = "Act_Cmplx";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node1 = util.getNode(PATH_TO_OBJ1);
        Node node2 = util.getNode(PATH_TO_OBJ2);
        util.createDiagram(new Node[]{node1, node2}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME1);
        DiagramElementOperator comp2 = new DiagramElementOperator(dia, OBJ_NAME2);
        LinkOperator.waitForUndirectedLink(comp, comp2, LinkTypes.ACTIVITY_EDGE);
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp, comp2}, dia)){
            fail("testCDFSComplex verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    
    public void testCDFSActivityFinalNode(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|ActivityFinalNode";
        final String OBJ_NAME = "ActivityFinalNode";
        final String DIA_NAME = "Act_ActFinalNode";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSActivityFinalNode verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateActivityDiagramFromClass verification failed");
            }
             */
        
        
    }
    
    
    public void testCDFSPartition(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|Partition";
        final String OBJ_NAME = "Partition";
        final String DIA_NAME = "Act_Partition";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSPartition verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateActivityDiagramFromClass verification failed");
            }
             */
        
        
    }
    
    
    
    
    public void testCDFSActivityGroup(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|ActivityGroup";
        final String OBJ_NAME = "ActivityGroup";
        final String DIA_NAME = "Act_ActGrp";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSActivityGroup verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateActivityGroup verification failed");
            }
             */
        
        
    }
    
    
    public void testCDFSChoice(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|Choice";
        final String OBJ_NAME = "Choice";
        final String DIA_NAME = "Act_Choice";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSChoice verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSDataStore(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|DataStore";
        final String OBJ_NAME = "DataStore";
        final String DIA_NAME = "Act_DataStore";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSDataStore verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSFlowFinalNode(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|FlowFinalNode";
        final String OBJ_NAME = "FlowFinalNode";
        final String DIA_NAME = "Act_FlowFinalNode";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSFlowFinalNode verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSInitialNode(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|InitialNode";
        final String OBJ_NAME = "InitialNode";
        final String DIA_NAME = "Act_InitNode";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSInitialNode verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSInvocation(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|Invocation";
        final String OBJ_NAME = "Invocation";
        final String DIA_NAME = "Act_Invocation";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSInvocation verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSParameterUsage(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|ParameterUsage";
        final String OBJ_NAME = "ParameterUsage";
        final String DIA_NAME = "Act_ParameterUsage";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSParameterUsage verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSSignal(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|Signal";
        final String OBJ_NAME = "Signal";
        final String DIA_NAME = "Act_Signal";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSSignal verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    public void testCDFSVerticalFork(){
        final String PATH_TO_OBJ = "Model|ActivityDiagram|VerticalFork";
        final String OBJ_NAME = "VerticalFork";
        final String DIA_NAME = "Act_VerticalFork";
        final String DIA_PATH = "Model|ActivityDiagram|"+DIA_NAME;
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OBJ);
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.ACTIVITY_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        DiagramElementOperator comp = new DiagramElementOperator(dia, OBJ_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new DiagramElementOperator[]{comp}, dia)){
            fail("testCDFSVerticalFork verification failed");
        }
        
        //checking diagram node was created
            /*
            if (!util.nodeExists(DIA_PATH)){
               eventTool.waitNoEvent(5000);
                fail("testCreateChoice verification failed");
            }
             */
        
    }
    
    
    protected void setUp() {
        if (isNotInitialized){
            Project.openProject(CDFSUtil.CDFS_XTEST_PROJECT_DIR+"/"+PROJECT_NAME);
            isNotInitialized=false;
        }
    }
    
    
    
    public void tearDown() {
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.tearDown();        
        new EventTool().waitNoEvent(1000);
        try {
            DiagramOperator d=new DiagramOperator("Act");
            d.closeAllDocuments();
        } catch (TimeoutExpiredException tee){}
        
        new EventTool().waitNoEvent(1000);
    }
    
    
    
}
