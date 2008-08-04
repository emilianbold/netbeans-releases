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


package org.netbeans.test.uml.activitydiagram;

import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class ActivityDiagramProperties extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectPr";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static long elCount=0;
    //--
     private static String activityDiagramName1 = "acD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Invocation";
    private static String elementName1="";
    private static ElementTypes elementType1=ElementTypes.INVOCATION;


    
    /** Need to be defined because of JUnit */
    public ActivityDiagramProperties(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramProperties.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
     public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.activitydiagram.utils.Utils.commonActivityDiagramSetup(workdir, prName);
            //
            codeSync=true;
        }
    }
   

    public void testActionProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        lastDiagramNode.select();
        lastDiagramNode.tree().waitSelected(lastDiagramNode.getTreePath());
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Activity Kind");
        assertTrue("Unexpected activity kind, expected is structured, now "+pr.getValue(),"structured".equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Visibility");
        assertTrue("Unexpected visibility, expected is public, now "+pr.getValue(),"public".equals(pr.getValue()));
        pr=new Property(ps,"Documentation");
        assertTrue("Unexpected documentation, expected empty, now "+pr.getValue(),"".equals(pr.getValue()));
        pr=new Property(ps,"Stereotypes");
        pr=new Property(ps,"Tagged Values");
        pr=new Property(ps,"Constraints");
        // pr=new Property(ps,"Final");
        pr=new Property(ps,"Template Parameters");
        pr=new Property(ps,"Transient");
        pr=new Property(ps,"Abstract");
        pr=new Property(ps,"Leaf");
        pr=new Property(ps,"Single Copy");
   }
    public void testDiagramInTreeProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        Node dgrNode=new Node(lastDiagramNode,activityDiagramName);
        dgrNode.select();
        dgrNode.tree().waitSelected(dgrNode.getTreePath());
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
   }
    public void testDiagramInDiagramViewProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        d.getDrawingArea().clickMouse();
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
   }
 
    public void testRenameViaProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        Node dgrNode=new Node(lastDiagramNode,activityDiagramName);
        dgrNode.select();
        dgrNode.tree().waitSelected(dgrNode.getTreePath());
        Node pkgNode=new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath());
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+ ", now "+pr.getValue(),activityDiagramName.equals(pr.getValue()));
        pr.setValue(activityDiagramName+"_2");
        d=new DiagramOperator(activityDiagramName+"_2");
        Node actNode=new Node(pkgNode,activityDiagramName);
        new Node(actNode,activityDiagramName+"_2");
    }
    public void testDifferentDiagramsInTreeProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d1=createOrOpenDiagram(project,workPkg, activityDiagramName+"_1");
        Node lastDiagramNode1=lastDiagramNode;
        DiagramOperator d2=createOrOpenDiagram(project,workPkg, activityDiagramName+"_2");
        Node lastDiagramNode2=lastDiagramNode;
        Node pkgNode=new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath());
        //
        Node dgrNode=new Node(lastDiagramNode1,activityDiagramName);
        dgrNode.select();
        dgrNode.tree().waitSelected(dgrNode.getTreePath());
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + "_1 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_1_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
        //
        dgrNode=new Node(lastDiagramNode2,activityDiagramName);
        dgrNode.select();
        dgrNode.tree().waitSelected(dgrNode.getTreePath());
        //
        ps=new PropertySheetOperator(activityDiagramName + "_2 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_2"+ ", now "+pr.getValue(),(activityDiagramName+"_2").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_2"+ ", now "+pr.getValue(),(activityDiagramName+"_2").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_2_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_2_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_2_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
        //
        pkgNode.select();
        ps=new PropertySheetOperator(workPkg + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        dgrNode=new Node(lastDiagramNode1,activityDiagramName);
        dgrNode.select();
        dgrNode.tree().waitSelected(dgrNode.getTreePath());
        //
        ps=new PropertySheetOperator(activityDiagramName + "_1 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_1_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
    }
    public void testDifferentDiagramsInDiagramViewProperties() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d1=createOrOpenDiagram(project,workPkg, activityDiagramName+"_1");
        Node lastDiagramNode1=lastDiagramNode;
        DiagramOperator d2=createOrOpenDiagram(project,workPkg, activityDiagramName+"_2");
        Node lastDiagramNode2=lastDiagramNode;
        Node pkgNode=new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath());
        //
        new DiagramOperator(activityDiagramName+"_1");
        //
        PropertySheetOperator ps=new PropertySheetOperator(activityDiagramName + "_1 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        Property pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_1_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
        //
        new DiagramOperator(activityDiagramName+"_2");
        //
        ps=new PropertySheetOperator(activityDiagramName + "_2 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_2"+ ", now "+pr.getValue(),(activityDiagramName+"_2").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_2"+ ", now "+pr.getValue(),(activityDiagramName+"_2").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_2_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_2_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_2_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
        //
        pkgNode.select();
        ps=new PropertySheetOperator(workPkg + " - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        new DiagramOperator(activityDiagramName+"_1");
        //
        ps=new PropertySheetOperator(activityDiagramName + "_1 - Properties");
        try{Thread.sleep(100);}catch(Exception ex){}
        pr=new Property(ps,"Diagram Kind");
        assertTrue("Unexpected diagram kind, expected is "+NewDiagramWizardOperator.ACTIVITY_DIAGRAM+", now "+pr.getValue(),NewDiagramWizardOperator.ACTIVITY_DIAGRAM.equals(pr.getValue()));
        pr=new Property(ps,"Name");
        assertTrue("Unexpected name, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Alias");
        assertTrue("Unexpected alias, expected is "+activityDiagramName+"_1"+ ", now "+pr.getValue(),(activityDiagramName+"_1").equals(pr.getValue()));
        pr=new Property(ps,"Layout Style");
        assertTrue("Unexpected layout style, expected is hierarchical, now "+pr.getValue(),"hierarchical".equals(pr.getValue()));
        pr=new Property(ps,"File Name");
        log((workdir+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld");
        assertTrue("Unexpected file name, expected to contain "+workdir+File.separator+prName+File.separator+activityDiagramName+"_1_nnnnnnn.etld, now "+pr.getValue(),pr.getValue().matches((workdir+File.separator+prName+File.separator).replaceAll("\\\\","\\\\\\\\")+activityDiagramName+"_1_\\d{13,14}\\.etld"));
        pr=new Property(ps,"Documentation");
    }

    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        try{
            DiagramOperator d=new DiagramOperator("ad");
            d.closeAllDocuments();
            d.waitClosed();
           new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
}
