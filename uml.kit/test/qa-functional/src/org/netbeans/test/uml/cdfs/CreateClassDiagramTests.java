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



package org.netbeans.test.uml.cdfs;



import org.netbeans.test.uml.cdfs.utils.CDFSUtil;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.project.JavaProject;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;





public class CreateClassDiagramTests extends UMLTestCase {
    
    private EventTool eventTool = new EventTool();
    
    public CreateClassDiagramTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CreateClassDiagramTests.class);
        return suite;
    }
    
    
    /******************const section*************************/
    private String PROJECT_NAME = "CDFS_uml_Class";
    private String JAVA_PROJECT_NAME = "CDFS_java";
    private final static String PROJECT_PATH = System.getProperty("nbjunit.workdir");
    
    /********************************************************/
    
    
    private static boolean isNotInitialized = true;
    CDFSUtil util = new CDFSUtil(PROJECT_NAME);
    
    public void testCreateDiagramFromClass(){
        final String PATH_TO_CLASS = "Model|cdfs|Customer";
        final String CLASS_NAME = "Customer";
        final String DIA_NAME = "DGCD_1";
        util = new CDFSUtil(PROJECT_NAME, this);
        Node node = util.getNode(PATH_TO_CLASS);
        
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new String[]{CLASS_NAME}, dia)){
            fail("testCreateDiagramFromClass verification failed");
        }
        
        //checking a node was created
        //TODO: add later
        
    }
    
    
    public void testCreateDiagramFromManyClasses(){
        final String PATH_TO_CLASSES = "Model|cdfs|";
        final String[] classNames = new String[]{"Address", "Customer", "CustomerAccount", "GovermentCustomer"};
        final String DIA_NAME = "DGAllCLD";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        
        Node[] nodes = new Node[classNames.length];
        for(int i=0; i<classNames.length; i++){
            nodes[i] = util.getNode(PATH_TO_CLASSES+classNames[i]);
        }
        
        util.createDiagram(nodes, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(classNames, dia)){
            fail("testCreateDiagramFromManyClasses verification failed");
        }
        
        //checking links were created on the diagram
        DiagramElementOperator customer = new DiagramElementOperator(dia, "Customer");
        DiagramElementOperator account = new DiagramElementOperator(dia, "CustomerAccount");
        DiagramElementOperator govCustomer = new DiagramElementOperator(dia, "GovermentCustomer");
        DiagramElementOperator address = new DiagramElementOperator(dia, "Address");
        new LinkOperator(govCustomer, customer, LinkTypes.GENERALIZATION);
        LinkOperator.waitForUndirectedLink(customer, account, LinkTypes.AGGREGATION);
        LinkOperator.waitForUndirectedLink(customer, address, LinkTypes.AGGREGATION);
        
        //checking a node was created
        //TODO: add later
        
    }
    
    
    
    
    
    public void testCreateDiagramForInnerClasses(){
        final String PATH_TO_CLASS = "Model|cdfs|";
        final String CLASS_NAME = "Order";
        final String DIA_NAME = "DG_CD2";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_CLASS+CLASS_NAME);
        
        new Thread(new Runnable() {
            public void run() {
                JDialogOperator confDlg = new JDialogOperator(util.CDFS_COMPLEX_OBJ_TTL);
                new JButtonOperator(confDlg, util.YES_BTN).pushNoBlock();
            }
        }).start();
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new String[]{CLASS_NAME, "IDGenerator", "OrderItem"}, dia)){
            fail("testCreateDiagramForInnerClasses verification failed");
        }
        //checking liks exist
        DiagramElementOperator idGen = new DiagramElementOperator(dia, "IDGenerator");
        DiagramElementOperator item = new DiagramElementOperator(dia, "OrderItem");
        DiagramElementOperator order = new DiagramElementOperator(dia, CLASS_NAME);
        new LinkOperator(idGen, order, LinkTypes.CLASS);
        new LinkOperator(item, order, LinkTypes.CLASS);
        
        
        //checking a node was created
        //TODO: add later
        
        
    }
    
    
    
    public void testCreateDiagramForInnerClassesNotDisplayed(){
        final String PATH_TO_CLASS = "Model|cdfs|";
        final String CLASS_NAME = "Order";
        final String DIA_NAME = "DGNOInner";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_CLASS+CLASS_NAME);
        
        new Thread(new Runnable() {
            public void run() {
                JDialogOperator confDlg = new JDialogOperator(util.CDFS_COMPLEX_OBJ_TTL);
                new JButtonOperator(confDlg, util.NO_BTN).pushNoBlock();
            }
        }).start();
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new String[]{CLASS_NAME}, dia)){
            fail("testCreateDiagramForInnerClassesNotDisplayed verification failed");
        }
        
        //checking a node was created
        //TODO: add later
        
        
    }
    
    
    
    
    public void testCreateDiagramFromPackage(){
        final String PATH_TO_CLASSES = "Model|cdfs|";
        final String PATH_TO_PACKAGE = "Model|cdfs";
        final String[] packageElements = new String[]{"Address", "Customer", "CustomerAccount", "GovermentCustomer", "Order", "employees"};
        //final String[] packageElements = new String[]{"Address", "Customer", "CustomerAccount", "GovermentCustomer", "Order", "employees","int"};
        final String DIA_NAME = "DGPckgCLD";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        
        
        Node node = util.getNode(PATH_TO_PACKAGE);
        
        new Thread(new Runnable() {
            public void run() {
                JDialogOperator confDlg = new JDialogOperator(util.CDFS_COMPLEX_OBJ_TTL);
                new JButtonOperator(confDlg, util.YES_BTN).pushNoBlock();
            }
        }).start();
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(packageElements, dia)){
            fail("testCreateDiagramFromManyClasses verification failed");
        }
        
        //checking links were created on the diagram
        DiagramElementOperator customer = new DiagramElementOperator(dia, "Customer");
        DiagramElementOperator account = new DiagramElementOperator(dia, "CustomerAccount");
        DiagramElementOperator govCustomer = new DiagramElementOperator(dia, "GovermentCustomer");
        DiagramElementOperator address = new DiagramElementOperator(dia, "Address");
        DiagramElementOperator order = new DiagramElementOperator(dia, "Order");
        new LinkOperator(govCustomer, customer, LinkTypes.GENERALIZATION);
        LinkOperator.waitForUndirectedLink(customer, account, LinkTypes.AGGREGATION);
        LinkOperator.waitForUndirectedLink(customer, address, LinkTypes.AGGREGATION);
        //LinkOperator.waitForUndirectedLink(customer, order, LinkTypes.ASSOCIATION);
        
        //checking a node was created
        //TODO: add later
        
        
    }
    
    
    
    public void testCreateDiagramForPackageScopedElementsNotDisplayed(){
        final String PATH_TO_PKG = "Model|cdfs";
        final String PKG_NAME = "cdfs";
        final String DIA_NAME = "DGPkgNoScoped";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_PKG);
        
        new Thread(new Runnable() {
            public void run() {
                JDialogOperator confDlg = new JDialogOperator(util.CDFS_COMPLEX_OBJ_TTL);
                new JButtonOperator(confDlg, util.NO_BTN).pushNoBlock();
            }
        }).start();
        
        util.createDiagram(new Node[]{node}, NewDiagramWizardOperator.CLASS_DIAGRAM, DIA_NAME);
        
        //checking diagram was opened
        DiagramOperator dia = new DiagramOperator(DIA_NAME);
        
        //checking only required elements are present on diagram
        if (!util.diagramHasExactElements(new String[]{PKG_NAME}, dia)){
            fail("testCreateDiagramForPackageScopedElementsNotDisplayed verification failed");
        }
        
        //checking a node was created
        //TODO: add later
        
        
    }
    
    
    
    public void testCreateClassDiagramFromAggregation(){
        final String PATH_TO_ASSOC = "Model|cdfs|Customer|Relationships|Associations|Aggregation";
        final String DIA_NAME = "DGASSOC";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_ASSOC);
        JPopupMenuOperator jPopup= node.callPopup();
        if (util.isPopupMenuItemExist(jPopup, util.CDFS_MENU))
            fail("Menu Item "+util.CDFS_MENU+" should not exist ");
        jPopup.pushKey(KeyEvent.VK_ESCAPE);
    }
    
    
    public void testCreateClassDiagramFromGeneralization(){
        final String PATH_TO_GEN = "Model|cdfs|Customer|Relationships|Generalization";
        final String DIA_NAME = "DGGEN";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_GEN);
        JPopupMenuOperator jPopup= node.callPopup();
        if (util.isPopupMenuItemExist(jPopup, util.CDFS_MENU))
            fail("Menu Item "+util.CDFS_MENU+" should not exist ");
        jPopup.pushKey(KeyEvent.VK_ESCAPE);
        
        
    }
    
    
    public void testCreateClassDiagramFromOperation(){
        final String PATH_TO_OPER = "Model|cdfs|Customer|Operations|getAddresses";
        final String DIA_NAME = "DGOper";
        util = new CDFSUtil(PROJECT_NAME, this);
        
        Node node = util.getNode(PATH_TO_OPER);
        node.performPopupActionNoBlock(util.CDFS_MENU);
        
        NewDiagramWizardOperator wizard = new NewDiagramWizardOperator();
        if (wizard.isDiagramAllowed(NewDiagramWizardOperator.CLASS_DIAGRAM)){
            fail("testCreateClassDiagramFromOperation failed because of KNOWN BUG: Class diagram should not be allowed to be created from Associations");
        }
        
    }
    
    protected void setUp() {
        if (isNotInitialized){
            Project.openProject(CDFSUtil.CDFS_XTEST_PROJECT_DIR+"/"+JAVA_PROJECT_NAME);
            UMLProject.createProject( PROJECT_NAME, ProjectType.UML_JAVA_REVERSE_ENGINEERING, new JavaProject(JAVA_PROJECT_NAME));
            isNotInitialized=false;
        }
    }
    
    
    
    public void tearDown() {
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.tearDown();
        new EventTool().waitNoEvent(1000);
        
        try {
            DiagramOperator d=new DiagramOperator("DG");
            d.closeAllDocuments();
        } catch (TimeoutExpiredException tee){}
        
        new EventTool().waitNoEvent(1000);
    }
    
    
    
    
}
