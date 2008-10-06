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
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class ActivityDiagramPlaceDeleteLinksByContext extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectPDLC";
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
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private String lastTestCase=null;
    //
    private static ElementTypes fromType= ElementTypes.INVOCATION;
    private static ElementTypes toType= ElementTypes.INVOCATION;
    //--
    private static String activityDiagramName1 = "acD15";
    private static String workPkg1 = "pkg15";
    private static String element1="Activity Edge";
    private static String treeNode1=element1;
    private static String elementName1="";
    private static LinkTypes elementType1=LinkTypes.ACTIVITY_EDGE;
    private static boolean eIO1=true;
    //--
    private static String activityDiagramName2 = "acD16";
    private static String workPkg2 = "pkg16";
    private static String element2="Dependency";
    private static String treeNode2=element2;
    private static String elementName2="";
    private static LinkTypes elementType2=LinkTypes.DEPENDENCY;
    private static boolean eIO2=false;
    //--
    private static String activityDiagramName3 = "acD17";
    private static String workPkg3 = "pkg17";
    private static String element3="Realize";
    private static String treeNode3="Realization";
    private static String elementName3="";
    private static LinkTypes elementType3=LinkTypes.REALIZE;
    private static boolean eIO3=false;
    //--
    private static String activityDiagramName4 = "acD18";
    private static String workPkg4 = "pkg18";
    private static String element4="Usage";
    private static String treeNode4=element4;
    private static String elementName4="";
    private static LinkTypes elementType4=LinkTypes.USAGE;
    private static boolean eIO4=false;
    //--
    private static String activityDiagramName5 = "acD5";
    private static String workPkg5 = "pkg5";
    private static String element5="Permission";
    private static String treeNode5=element5;
    private static String elementName5="";
    private static LinkTypes elementType5=LinkTypes.PERMISSION;
    private static boolean eIO5=false;
    //--
    private static String activityDiagramName6 = "acD20";
    private static String workPkg6 = "pkg20";
    private static String element6="Abstraction";
    private static String treeNode6=element6;
    private static String elementName6="";
    private static LinkTypes elementType6=LinkTypes.ABSTRACTION;
    private static boolean eIO6=false;


    
    /** Need to be defined because of JUnit */
    public ActivityDiagramPlaceDeleteLinksByContext(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramPlaceDeleteLinksByContext.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    


  /*public void testDeleteLinkByContext1() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg1,activityDiagramName1, fromType, toType,element1,elementName1,elementType1,treeNode1,eIO1);
    }*/
  public void testDeleteLinkByContext2() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg2,activityDiagramName2, fromType, toType,element2,elementName2,elementType2,treeNode2,eIO2);
    }
    public void testDeleteLinkByContext3() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg3,activityDiagramName3, fromType, toType,element3,elementName3,elementType3,treeNode3,eIO3);
    }
  public void testDeleteLinkByContext4() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg4,activityDiagramName4, fromType, toType,element4,elementName4,elementType4,treeNode4,eIO4);
    }
  public void testDeleteLinkByContext5() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg5,activityDiagramName5, fromType, toType,element5,elementName5,elementType5,treeNode5,eIO5);
    }
  public void testDeleteLinkByContext6() {
        lastTestCase=getCurrentTestMethodName();
        testLinkPlaceDelete(workPkg6,activityDiagramName6, fromType, toType,element6,elementName6,elementType6,treeNode6,eIO6);
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
            DiagramOperator d=new DiagramOperator("acD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    

    
   private void testLinkPlaceDelete(String workPkg,String diagramName,ElementTypes fromType,ElementTypes toType,String element,String elementName,LinkTypes elementType,String treeNode,boolean useIO)
   {
       String localElName1="El1";
       String localElName2="El2";
       String newName="LinkName";
       //
        DiagramOperator d = createOrOpenDiagram(project,workPkg, diagramName);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=lastDiagramNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
       DrawingAreaOperator drAr=d.getDrawingArea();
       //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
        try
        {
            dE1=d.putElementOnDiagram(localElName1,fromType,a.x,a.y);
            a=drAr.getFreePoint(150);
            dE2=d.putElementOnDiagram(localElName2,toType,a.x,a.y);
        }
        catch(Exception ex)
        {
        
        }
        //
        try {
            pl.selectTool(element);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find '"+element+"' in paletter");
        }
        //
        drAr.clickMouse(dE1.getCenterPoint().x,dE1.getCenterPoint().y,1);
        drAr.clickMouse(dE2.getCenterPoint().x,dE2.getCenterPoint().y,1);
        //
        new EventTool().waitNoEvent(2000);
         //drAr.pushKey(KeyEvent.VK_ESCAPE);
        d.toolbar().selectDefault();
      //
        PropertySheetOperator ps=null;
        try
        {
            ps=new PropertySheetOperator(elementType+" - Properties");
        }
        catch(org.netbeans.jemmy.TimeoutExpiredException ex)
        {
            ps=new PropertySheetOperator();
            throw ex;
        }
        org.netbeans.test.uml.activitydiagram.utils.Utils.setTextProperty("Name", newName);
        new EventTool().waitNoEvent(500);
        LinkOperator testedlink=null;
        try
        {
            testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
        }
        catch(Exception ex)
        {
            fail(element+" of type "+elementType+" wasn't added to diagram:"+ex);
        }
        if(testedlink==null)
        {
            LinkOperator altLink=null;
            try
            {
                altLink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
            }
            catch(Exception ex)
            {
                fail("any link find failed.");
            }
            if(altLink!=null)fail("Can't find "+elementType+" link between elements, but the is "+altLink.getType()+" link.");
       }
        d.makeComponentVisible();
        //
        JPopupMenuOperator popM=testedlink.getPopup();
        popM.waitComponentShowing(true);
        popM.waitComponentVisible(true);
        try{Thread.sleep(3000);}catch(Exception ex){}
        popM.pushKey(KeyEvent.VK_LEFT);
        popM=new JPopupMenuOperator();
//        try{Thread.sleep(5000);}catch(Exception ex){}
        popM.pushMenuNoBlock("Edit|Delete");
        JDialogOperator delDlg=new JDialogOperator("Delete");
        try{Thread.sleep(1000);}catch(Exception ex){}
        delDlg.waitComponentShowing(true);
        JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
        chk.waitComponentShowing(true);
        if(!chk.isSelected())chk.clickMouse();
        chk.waitSelected(true);
        new JButtonOperator(delDlg,"Yes").pushNoBlock();
        //
        new DiagramElementOperator(d,localElName1);
        new DiagramElementOperator(d,localElName2);
        //
        testedlink=null;
        try
        {
            for(int i=0;i<10;i++)
            {
                testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
                if(testedlink==null)break;
                Thread.sleep(100);
            }
        }
        catch(Exception ex)
        {
            
        }
        assertFalse("Link remains on diagram after deletion",testedlink!=null);
         //
         Node n1=new Node(lastDiagramNode,localElName1);
         Node n2=new Node(lastDiagramNode,localElName2);
         Node rel1=null;
         if(n1.isChildPresent("Relationships"))rel1=new Node(n1,"Relationships");
         Node rel2=null;
         if(n2.isChildPresent("Relationships"))rel2=new Node(n2,"Relationships");
         new EventTool().waitNoEvent(500);
         if(useIO)
         {
             assertFalse("There is Outgoing Edges node remains after link deletion",rel1!=null && rel1.isChildPresent("Outgoing Edges"));
             assertFalse("There is Incoming Edges node remains after link deletion",rel2!=null && rel2.isChildPresent("Incoming Edges"));
         }
         else
         {
             assertFalse("There should be no "+newName+" node within Relationships node of "+localElName1,rel1!=null && rel1.isChildPresent(newName));
             assertFalse("There should be no "+newName+" node within Relationships node of "+localElName2,rel2!=null && rel2.isChildPresent(newName));
         }
   }
}
