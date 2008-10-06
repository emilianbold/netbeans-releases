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


package org.netbeans.test.uml.designpatterns;

//import com.embarcadero.uml.ui.controls.projecttree.IProjectTreeItem;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.*;

/**
 *
 * @author psb
 * @spec uml/DesignPatterns
 */
public class BuildingPatterns2 extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String mainTreeTabName="Projects";
    private static String sourcePackGroup="Source Packages";
    private static String defPackageName="newpackage";
    private static String defClassName="NewClass";
    //common test properties
    private static String prName= "BuildingPatternsProject2";
    private static String project = prName+"|Model";
    private static boolean codeSync=false;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static String defaultNewElementName=org.netbeans.test.uml.designpatterns.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.designpatterns.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.designpatterns.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.designpatterns.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.designpatterns.utils.Utils.defaultOperationVisibility;
    private static String defaultClassVisibility=org.netbeans.test.uml.designpatterns.utils.Utils.defaultClassVisibility;
    ProjectsTabOperator pto=null;
    JTreeOperator prTree=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private final long MINWAIT=500;
    ProjectRootNode prRoot=null;
    private static int pkgCounter=0;
    //
    ExpandedElementTypes allRoles[]={ExpandedElementTypes.ROLE,ExpandedElementTypes.INTERFACE_ROLE,ExpandedElementTypes.ACTOR_ROLE,ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.USE_CASE_ROLE};
    LinkTypes allLinks[]={LinkTypes.DEPENDENCY};
    private static boolean makeScreen=false;
  
    
    /** Need to be defined because of JUnit */
    public BuildingPatterns2(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
         NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.designpatterns.BuildingPatterns2.class);
         return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,org.netbeans.test.umllib.NewDiagramWizardOperator.CLASS_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    
   public void setUp() {
        makeScreen=true;
        System.out.println("########  "+getName()+"  #######");
        long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000); 
        try
        {
            JDialogOperator svD=new JDialogOperator("Exception");
            new JButtonOperator(svD, "OK").push();
        }
        catch(Exception ex)
        {
        }
        closeAllModal();
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp); 
        pto = ProjectsTabOperator.invoke();
        prTree=new JTreeOperator(pto);
        if(!codeSync)
        {
            //org.netbeans.test.uml.designpatterns.utils.Utils.setDefaultPreferences();
            org.netbeans.test.uml.designpatterns.utils.Utils.prepareProjects(workdir, prName);
            codeSync=true;
        }
        prRoot=new ProjectRootNode(prTree, project);
        //close design center
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",3000);
        try
        {
            new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE).closeWindow();
        }
        catch(Exception ex)
        {
            
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);

    }
   
    public void commonCreateAndBindAtOnce(String diagramType) {
        pkgCounter++;
        String curPkg="pkg"+pkgCounter;
        DiagramOperator dgr=org.netbeans.test.umllib.Utils.createOrOpenDiagram(prName,curPkg,"dgr"+pkgCounter,diagramType).dOp;
        lastDiagramNode = new Node(prRoot,curPkg+"|"+"dgr"+pkgCounter);
        DrawingAreaOperator drA=dgr.getDrawingArea();
        int numChild=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        DiagramElementOperator dpE=null;
        try
        {
            dpE=dgr.putElementOnDiagram("DP",ExpandedElementTypes.DESIGN_PATTERN);
        }
        catch(NotFoundException ex)
        {
            fail("Can't find design pattern: "+ex);
        }
        //
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
        java.awt.Point tmp=null;
        for(int i=0;i<allRoles.length;i++)
        {
            ExpandedElementTypes it=allRoles[i];
            try {
                pl.selectTool(LibProperties.getProperties().getToolName(it));
            } catch(NotFoundException ex) {
                fail("BLOCKING: Can't find '"+it+"' in paletter");
            }
           // try{Thread.sleep(4000);}catch(Exception ex){}
            drA.clickMouse(dpE.getCenterPoint().x,dpE.getCenterPoint().y,1);
            //try{Thread.sleep(4000);}catch(Exception ex){}
            tmp=drA.getFreePoint(80);
            drA.clickMouse(tmp.x,tmp.y,1);
            //try{Thread.sleep(4000);}catch(Exception ex){}
            //
            try
            {
                new DiagramElementOperator(dgr,defaultNewElementName,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Role "+it+" wasn't added to diagram");
            }
        }
        
        tmp=drA.getFreePoint(10);
        drA.clickMouse(tmp.x,tmp.y,1,InputEvent.BUTTON3_MASK);
        drA.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(500);
        assertTrue("There should be "+allRoles.length+" binding links from design pattern to roles, now: "+dpE.getLinks().size(), allRoles.length==dpE.getLinks().size());
        assertTrue("There should be "+allRoles.length+" roles within design pattern node in project tree, now: "+dpNode.getChildren().length, allRoles.length==dpNode.getChildren().length);
        //name roles
        for(int i=0;i<allRoles.length;i++)
        {
            ExpandedElementTypes it=allRoles[i];
            DiagramElementOperator curRole=null;
            //
            try
            {
                curRole=new DiagramElementOperator(dgr,defaultNewElementName,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find "+it+" on diagram");
            }
            curRole.select(true);
            curRole.waitSelection(true);
            for(int j=0;j<("name"+i).length();j++)
            {
                drA.typeKey(("name"+i).charAt(j));
            }
            tmp=drA.getFreePoint();
            drA.clickMouse(tmp.x,tmp.y,1);
           try
            {
                curRole=new DiagramElementOperator(dgr,"name"+i,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find named "+it+" on diagram");
            }
            new Node(dpNode,"name"+i);
        }
        CompartmentOperator dpPC=null;
        try
        {
            dpPC=new CompartmentOperator(dpE,CompartmentTypes.TEMPLATE_PARAMETERS_COMPARTMENT);
        }
        catch(Exception ex)
        {
            fail("can't find parameters comparment in design pattern");
        }
        String allRolesInDPStr=dpPC.getName();
        for(int i=0;i<allRoles.length;i++)
        {
            assertTrue("there is no 'name"+i+"' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("name"+i)>-1);
            assertTrue("there is no 'name"+i+"' node within design pattern node",dpNode.isChildPresent("name"+i));
        }
        assertTrue("There should be "+allRoles.length+" names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==allRoles.length);
    }

    /**
     * Create diagram in package and add pattern with two roles
     * draw link between roles and check results
     * @param fromEl - type for role from wich link is drawn
     * @param toEl - link to
     * @param link - type of link to be drawn
     * @param diagramType - type name for created diagram
     */
     public void commonDrawLink(ExpandedElementTypes fromEl,ExpandedElementTypes toEl,LinkTypes link,String diagramType)
     {
        pkgCounter++;
        String curPkg="pkg"+pkgCounter;
        DiagramOperator dgr=org.netbeans.test.umllib.Utils.createOrOpenDiagram(prName,curPkg,"dgr"+pkgCounter,diagramType).dOp;
        lastDiagramNode = new Node(prRoot,curPkg+"|"+"dgr"+pkgCounter);
        DrawingAreaOperator drA=dgr.getDrawingArea();
        int numChild=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        DiagramElementOperator dpE=null;

        dpE=dgr.putElementOnDiagram("DP",ExpandedElementTypes.DESIGN_PATTERN);

        new DiagramElementOperator(dgr,"DP",ExpandedElementTypes.DESIGN_PATTERN,0);

        //
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
        java.awt.Point tmp=null;

            pl.selectTool(LibProperties.getProperties().getToolName(toEl));
            tmp=drA.getFreePoint(80);
            drA.clickMouse(dpE.getCenterPoint().x,dpE.getCenterPoint().y,1);
            drA.clickMouse(tmp.x,tmp.y,1);
            //
            DiagramElementOperator toE=null;
            toE=new DiagramElementOperator(dgr,defaultNewElementName,toEl,0);
            toE.waitSelection(true);
            //---
            for(int j=0;j<"toRole".length();j++)
            {
                drA.typeKey("toRole".charAt(j));
            }
            //
            if(!fromEl.equals(toEl))pl.selectTool(LibProperties.getProperties().getToolName(fromEl));
            tmp=drA.getFreePoint(80);
            drA.clickMouse(dpE.getCenterPoint().x,dpE.getCenterPoint().y,1);
            drA.clickMouse(tmp.x,tmp.y,1);
            new EventTool().waitNoEvent(500);
            //
            toE=new DiagramElementOperator(dgr,"toRole",toEl,0);//first should be here
            //
            DiagramElementOperator fromE=null;
            fromE=new DiagramElementOperator(dgr,defaultNewElementName,fromEl,0);
            fromE.waitSelection(true);
            for(int j=0;j<"fromRole".length();j++)
            {
                drA.typeKey("fromRole".charAt(j));
            }
        try{Thread.sleep(100);}catch(Exception ex){}            
        tmp=drA.getFreePoint(10);
        drA.clickMouse(tmp.x,tmp.y,1,InputEvent.BUTTON3_MASK);
        drA.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(100);}catch(Exception ex){}            
        //
            try
            {
                fromE=new DiagramElementOperator(dgr,"fromRole",fromEl,0);
            }
            catch(Exception ex)
            {
                try
                {
                    fail("Role "+fromEl+" wasn't added to diagram but with type: "+new DiagramElementOperator(dgr,"fromRole").getType());
                }catch(Exception eee){}
                fail("Role "+fromEl+" wasn't added to diagram");
            }
        
        assertTrue("There should be 2 binding links from design pattern to roles, now: "+dpE.getLinks().size(), 2==dpE.getLinks().size());
        assertTrue("There should be 2 roles within design pattern node in project tree, now: "+dpNode.getChildren().length, 2==dpNode.getChildren().length);
           
        CompartmentOperator dpPC=null;
        dpPC=new CompartmentOperator(dpE,CompartmentTypes.TEMPLATE_PARAMETERS_COMPARTMENT);
        String allRolesInDPStr=dpPC.getName();
        assertTrue("there is no 'fromRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("fromRole")>-1);
        assertTrue("there is no 'toRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("toRole")>-1);
        assertTrue("there is no 'fromRole' node within design pattern node",dpNode.isChildPresent("fromRole"));
        assertTrue("there is no 'toRole' node within design pattern node",dpNode.isChildPresent("toRole"));
        assertTrue("There should be 2 names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==2);
        //link
        pl.selectTool(LibProperties.getProperties().getToolName(link));
         new EventTool().waitNoEvent(500);
            drA.clickMouse(fromE.getCenterPoint().x,fromE.getCenterPoint().y,1);
             new EventTool().waitNoEvent(500);
            drA.clickMouse(toE.getCenterPoint().x,toE.getCenterPoint().y,1);
            new EventTool().waitNoEvent(500);
        tmp=drA.getFreePoint(10);
        drA.clickMouse(tmp.x,tmp.y,1,InputEvent.BUTTON3_MASK);
        drA.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(500);
        //checks
        //STANDART
        assertTrue("There should be 2 binding links from design pattern to roles, now: "+dpE.getLinks().size(), 2==dpE.getLinks().size());
        assertTrue("there is no 'fromRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("fromRole")>-1);
        assertTrue("there is no 'toRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("toRole")>-1);
        //
        try
        {
            if(link.equals(LinkTypes.NESTED_LINK))
            {
                new LinkOperator(fromE,toE,LinkTypes.valueOf(toEl.name()));
            }
            else if(link.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
                  new LinkOperator(fromE,toE,LinkTypes.ASSOCIATION);
            else {
                new LinkOperator(fromE,toE,link);
            }
        }
        catch(NotFoundException ex)
        {
            fail("there should be '"+link+"' between roles: "+ex);
        }
        //CASES
        if(link.equals(LinkTypes.NESTED_LINK))
        {
            //1
            assertTrue("there is no 'toRole' node within design pattern node",dpNode.isChildPresent("toRole"));
            assertTrue("there is no 'toRole|fromRole' node within design pattern node",new Node(dpNode,"toRole").isChildPresent("fromRole"));
            assertTrue("There should be 1 roles within design pattern node in project tree, now: "+dpNode.getChildren().length, 1==dpNode.getChildren().length);
        }
        else
        {
            //2
            assertTrue("there is no 'fromRole' node within design pattern node",dpNode.isChildPresent("fromRole"));
            assertTrue("there is no 'toRole' node within design pattern node",dpNode.isChildPresent("toRole"));
            assertTrue("There should be 2 roles within design pattern node in project tree, now: "+dpNode.getChildren().length, 2==dpNode.getChildren().length);
            Node toNode=new Node(dpNode,"toRole");
            Node fromNode=new Node(dpNode,"fromRole");
            Node toRelNode=new Node(toNode,"Relationships");
            Node fromRelNode=new Node(fromNode,"Relationships");
            //
            String nodeName=null;
            //CASES
            if(true)
            {
                //most cases??
                if(link.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
                    nodeName=LinkTypes.ASSOCIATION.toString();
                else
                    nodeName=link.toString();
            }
            Node toRelGenNode=new Node(toRelNode,nodeName);
            Node fromRelGenNode=new Node(fromRelNode,nodeName);
            assertTrue("there is no 'Relationships|"+nodeName+"|toRole' node within 'fromRole' node",fromRelGenNode.isChildPresent("toRole"));
            assertTrue("there is no 'Relationships|"+nodeName+"|fromRole' node within 'toRole' node",toRelGenNode.isChildPresent("fromRole"));
            assertTrue("there are extra nodes within 'Relationships|"+nodeName+"' node within 'fromRole' node",fromRelGenNode.getChildren().length==1);
            assertTrue("there are extra nodes within 'Relationships|"+nodeName+"' node within 'toRole' node",toRelGenNode.getChildren().length==1);
            //CASES
            if(link.equals(LinkTypes.NAVIGABLE_AGGREGATION) || link.equals(LinkTypes.NAVIGABLE_ASSOCIATION) || link.equals(LinkTypes.NAVIGABLE_COMPOSITION))
            {
                CompartmentOperator opCmp=null;
                try
                {
                    opCmp=new CompartmentOperator(fromE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
                }
                catch(NotFoundException ex)
                {
                    //there is no operation compartment
                }
                if(opCmp!=null)
                {
                    assertTrue("There should be accessors in from element on the diagram",opCmp.getCompartments().size()>0);
                    boolean setter=false,getter=false;
                    for(int i=0;i<opCmp.getCompartments().size();i++)
                    {
                        CompartmentOperator cur=opCmp.getCompartments().get(i);
                        if(cur.getName().equals("public toRole  getMtoRole(  )"))getter=true;
                        else if(cur.getName().equals("public void  setMtoRole( toRole val )"))setter=true;
                    }
                    assertTrue("there is no getter in operations compartment", getter);
                    assertTrue("there is no setter in operations compartment", setter);
                    //
                    Node opNode=fromNode;
                    if(fromNode.isChildPresent("Operations"))opNode=new Node(opNode,"Operations");
                    assertTrue("there is no setter in fromRole node in project tree", opNode.isChildPresent("public void  setMtoRole( toRole val )"));
                    assertTrue("there is no getter in fromRole node in project tree", opNode.isChildPresent("public toRole  getMtoRole(  )"));
                }
            }
        }
     }

    /**
     * Create diagram in package and add pattern with two roles
     * draw link between roles and check results
     * @param fromEl - type for role from wich link is drawn
     * @param toEl - link to
     * @param link - type of link to be drawn
     * @param diagramType - type name for created diagram
     */
     public void commonDeleteLink(ExpandedElementTypes fromEl,ExpandedElementTypes toEl,LinkTypes link,String diagramType)
     {
        commonDrawLink(fromEl, toEl, link, diagramType);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        DiagramOperator dgr=null;
        try
        {
            dgr=new DiagramOperator("dgr"+pkgCounter);
        }
        catch(Exception ex)
        {
            fail("can't find diagram");
        }
        //
        DiagramElementOperator fromE=null,toE=null,dpE=null;
            try
            {
                dpE=new DiagramElementOperator(dgr,"DP",ElementTypes.DESIGN_PATTERN,0);
             }
            catch(NotFoundException ex)
            {
                fail("Can't find design patterb on diagram");
            }
            try
            {
                fromE=new DiagramElementOperator(dgr,"fromRole",fromEl,0);
            }
            catch(NotFoundException ex)
            {
                fail("Role "+fromEl+" wasn't added to diagram");
            }
            try
            {
                toE=new DiagramElementOperator(dgr,"toRole",toEl,0);
            }
            catch(NotFoundException ex)
            {
                fail("Role "+toEl+" wasn't added to diagram");
            }
        LinkOperator lnk=null;
        try
        {
            if(link.equals(LinkTypes.NESTED_LINK))
            {
                lnk=new LinkOperator(fromE,toE,LinkTypes.valueOf(toEl.name()));
            }
            else if(link.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
                 lnk=new LinkOperator(fromE,toE, LinkTypes.ASSOCIATION);
            else {
                lnk=new LinkOperator(fromE,toE,link);
            }
        }
        catch(NotFoundException ex)
        {
            fail("there should be '"+link+"' between roles: "+ex);
        }
        //delete
        JPopupMenuOperator pop=lnk.getPopup();
        pop.waitComponentShowing(true);
        new EventTool().waitNoEvent(1000);
        //
        try
        {
        pop.showMenuItem("Edit|Delete").pushNoBlock();
        }catch(Exception ex)
        {
            log("Pop:"+pop.getSource()+":::"+pop.getParent()+":::ex:"+ex);
            fail("Pop:"+pop.getSource()+":::"+pop.getParent()+":::ex:"+ex);
        }
        //
        JDialogOperator delDlg=new JDialogOperator("Delete");
        try{Thread.sleep(1000);}catch(Exception ex){}
        delDlg.waitComponentShowing(true);
        JCheckBoxOperator chk=new JCheckBoxOperator(delDlg);
        chk.waitComponentShowing(true);
        if(!chk.isSelected())chk.clickMouse();
        chk.waitSelected(true);
        new JButtonOperator(delDlg,"Yes").pushNoBlock();
        delDlg.waitClosed();
        //check if there is a reason to delete accessors
        if(link.equals(LinkTypes.NAVIGABLE_AGGREGATION) || link.equals(LinkTypes.NAVIGABLE_ASSOCIATION) || link.equals(LinkTypes.NAVIGABLE_COMPOSITION))
        {
            log("navigable, i.e. need accessors removal");
            //yes to delete accessors
            JDialogOperator dld=new JDialogOperator("Delete");
            new JButtonOperator(dld,"Yes").push();
            dld.waitClosed();
        }
        new EventTool().waitNoEvent(500);
        //
        assertTrue("Links remains between roles after deletion (on diagram).",LinkOperator.findLink(fromE, toE, new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0)==null);
        //
        assertTrue("There should be 2 binding links from design pattern to roles, now: "+dpE.getLinks().size(), 2==dpE.getLinks().size());
        //
        CompartmentOperator dpPC=null;
        try
        {
            dpPC=new CompartmentOperator(dpE,CompartmentTypes.TEMPLATE_PARAMETERS_COMPARTMENT);
        }
        catch(Exception ex)
        {
            fail("can't find parameters comparment in design pattern");
        }
        String allRolesInDPStr=dpPC.getName();
        assertTrue("there is no 'fromRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("fromRole")>-1);
        assertTrue("there is no 'toRole' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("toRole")>-1);
        assertTrue("There should be 2 names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==2);
        //CASES
        if(link.equals(LinkTypes.NESTED_LINK))
        {
            //1
            assertTrue("there is no 'toRole' node within design pattern node",dpNode.isChildPresent("toRole"));
            assertFalse(86020, "there is no 'fromRole' node within design pattern node, but 'fromNode' is on pattern node level",!dpNode.isChildPresent("fromRole") && pkgNode.isChildPresent("fromRole"));
            assertTrue("there is no 'fromRole' node within design pattern node",dpNode.isChildPresent("fromRole"));
            assertFalse("there is 'toRole|fromRole' node within design pattern node",new Node(dpNode,"toRole").isChildPresent("fromRole"));
            assertTrue("There should be 2 roles within design pattern node in project tree, now: "+dpNode.getChildren().length, 2==dpNode.getChildren().length);
        }
        else
        {
            //2
            assertTrue("there is no 'fromRole' node within design pattern node",dpNode.isChildPresent("fromRole"));
            assertTrue("there is no 'toRole' node within design pattern node",dpNode.isChildPresent("toRole"));
            assertTrue("There should be 2 roles within design pattern node in project tree, now: "+dpNode.getChildren().length, 2==dpNode.getChildren().length);
            Node toNode=new Node(dpNode,"toRole");
            Node fromNode=new Node(dpNode,"fromRole");
            Node toRelNode=null;
            if(toNode.isChildPresent("Relationships"))toRelNode=new Node(toNode,"Relationships");
            Node fromRelNode=null;
            if(fromNode.isChildPresent("Relationships"))fromRelNode=new Node(fromNode,"Relationships");
            //
            String nodeName=null;
            //CASES
            if(true)
            {
                //most cases??
                nodeName=link.toString();
            }
            assertTrue("there should be no Relationships or empty Relationships node within 'fromRole' node",fromRelNode==null || fromRelNode.getChildren().length==0);
            assertTrue("there should be no Relationships or empty Relationships node within 'toRole' node",toRelNode==null || toRelNode.getChildren().length==0);
            //CASES
            if(link.equals(LinkTypes.NAVIGABLE_AGGREGATION) || link.equals(LinkTypes.NAVIGABLE_ASSOCIATION) || link.equals(LinkTypes.NAVIGABLE_COMPOSITION))
            {
                new EventTool().waitNoEvent(500);
                //
                CompartmentOperator opCmp=null;
                try
                {
                    opCmp=new CompartmentOperator(fromE,CompartmentTypes.OPERATION_LIST_COMPARTMENT);
                }
                catch(NotFoundException ex)
                {
                    //there is no operation compartment
                }
                if(opCmp!=null)
                {
                    int setter=0,getter=0;
                    for(int count=1;count<=10;count++)
                    {
                        try{Thread.sleep(500);}catch(Exception ex){}
                        for(int i=0;i<opCmp.getCompartments().size();i++)
                        {
                            CompartmentOperator cur=opCmp.getCompartments().get(i);
                            if(cur.getName().equals("public toRole  getMtoRole(  )"))getter++;
                            else if(cur.getName().equals("public void  setMtoRole( toRole val )"))setter++;
                        }
                        if(getter<count && setter<count)break;
                    }
                   assertFalse("there is getter in operations compartment", getter>=10);
                    assertFalse("there is setter in operations compartment", setter>=10);
                    //
                    Node opNode=fromNode;
                    if(fromNode.isChildPresent("Operations"))opNode=new Node(opNode,"Operations");
                    assertFalse("there is setter in fromRole node in project tree", opNode.isChildPresent("public void  setMtoRole( toRole val )"));
                    assertFalse("there is getter in fromRole node in project tree", opNode.isChildPresent("public toRole  getMtoRole(  )"));
                }
            }
        }
     }
   /**
     * @caseblock Building Patterns
      */
    public void testDeleteGeneralizationClToClClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.GENERALIZATION,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteGeneralizationInToInClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.INTERFACE_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.GENERALIZATION,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteImplementationClToInClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.IMPLEMENTATION,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteNestedLinkClToClClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NESTED_LINK,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteDependancyClToRoClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Class Diagram");
         makeScreen=false;
   }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteRealizeAcToRoClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.ACTOR_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeletePermissionUsToClClD() {
        lastTestCase=getCurrentTestMethodName();
       commonDeleteLink(ExpandedElementTypes.USE_CASE_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.PERMISSION,"Class Diagram");
        makeScreen=false;
    }
   /**
     * @caseblock Building Patterns
     */
    public void testDeleteNavigableAssocClToClClD() {
        lastTestCase=getCurrentTestMethodName();
        commonDeleteLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NAVIGABLE_ASSOCIATION,"Class Diagram");
        makeScreen=false;
    }
    /**
     *
     *
     */
    public void commonAddAttribute(boolean inTree,String diagramType)
    {
        commonCreateAndBindAtOnce(diagramType);
        //vars restore
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        DiagramOperator dgr=null;
        try
        {
            dgr=new DiagramOperator("dgr"+pkgCounter);
        }
        catch(Exception ex)
        {
            fail("can't find diagram");
        }
        if(!inTree)new JComboBoxOperator(dgr).enterText("100%");
         new EventTool().waitNoEvent(500);
        Node allRolesN[]=new Node[allRoles.length];
        DiagramElementOperator allRolesE[]=new DiagramElementOperator[allRoles.length];
        //name roles
         for(int i=0;i<allRoles.length;i++)
        {
            ExpandedElementTypes it=allRoles[i];
           try
            {
                allRolesE[i]=new DiagramElementOperator(dgr,"name"+i,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find named "+it+" on diagram");
            }
            allRolesN[i]=new Node(dpNode,"name"+i);
        }
        //attribute addition
        if(inTree)
        {
            for(int i=0;i<allRoles.length;i++)
            {
                allRolesN[i].performPopupAction("New|Attribute");
                Node atrNode=new Node(allRolesN[i],defaultAttributeVisibility+" "+defaultAttributeType+" "+defaultNewElementName);
                try
                {
                    //even manually sometimes selection do not work from first time
                    prTree.clickForEdit(atrNode.getTreePath());
                }
                catch(Exception ex)
                {
                    prTree.clickForEdit(atrNode.getTreePath());
                }
                for(int j=0;j<("attribute"+i).length();j++)
                {
                    prTree.typeKey(("attribute"+i).charAt(j));
                }
                prTree.pushKey(KeyEvent.VK_ENTER);
            }
        }
        else
        {
           for(int i=0;i<allRoles.length;i++)
            {
                CompartmentOperator cmp=null;
                try
                {
                    cmp=new CompartmentOperator(allRolesE[i],CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
                }
                catch(NotFoundException ex)
                {
                         
                    //there is no attribute compartment
                }
                catch(TimeoutExpiredException ex)
                {
                    //there is no attribute compartment
                }
                if(cmp!=null)
                {
                    cmp.clickleftForPopup();
                     try{Thread.sleep(1000);}catch(Exception ex){}
                    new JPopupMenuOperator().pushKey(KeyEvent.VK_LEFT);
                    try{Thread.sleep(3000);}catch(Exception ex){}
                    new JPopupMenuOperator().pushMenu("Insert Attribute");
                    
                    org.netbeans.test.uml.designpatterns.utils.Utils.attributeNaturalWayNaming(null,null,"attribute"+i);
                    new EditControlOperator().getTextFieldOperator().pushKey(KeyEvent.VK_ENTER);
               }
            }
        }
        new EventTool().waitNoEvent(1000);
        //checks
        for(int i=0;i<allRoles.length;i++)
        {
            //on diagram
            CompartmentOperator cmpA=null;
            try
            {
                cmpA=new CompartmentOperator(allRolesE[i],CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
            }
            catch(TimeoutExpiredException ex)
            {
                //there is no attribute compartment
            }
            catch(NotFoundException ex)
            {
                //there is no attribute compartment
            }
            //
            if(cmpA!=null)
            {
                  assertTrue(i+": there should be "+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?"public":defaultAttributeVisibility)+" "+defaultAttributeType+" attribute"+i+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?" = 0":"")+" attribute in "+allRoles[i]+" on diagram ("+allRolesE[i].getSubjectVNs().get(0)+"), cmp: "+cmpA, cmpA.getCompartments().get(0).getName().equals((allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?"public":defaultAttributeVisibility)+" "+defaultAttributeType+" attribute"+i+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?" = 0":"")));
            }
            CompartmentOperator cmpO=null;
            try
            {
                cmpO=new CompartmentOperator(allRolesE[i],CompartmentTypes.OPERATION_LIST_COMPARTMENT);
            }
            catch(TimeoutExpiredException ex)
            {
                //there is no operation compartment
                if(cmpA!=null && cmpO==null)fail("There is attribute compartments but no operations compartment");
            }
            catch(NotFoundException ex)
            {
               //there is no operation compartment
                if(cmpA!=null && cmpO==null)fail("There is attribute compartments but no operations compartment");
            }
            if(cmpO!=null && !allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE))
            {
                    assertTrue("There should be accessors in from element on the diagram",cmpO.getCompartments().size()>0);
                    boolean setter=false,getter=false;
                    for(int j=0;j<cmpO.getCompartments().size();j++)
                    {
                        CompartmentOperator cur=cmpO.getCompartments().get(j);
                        if(cur.getName().equals("public "+defaultAttributeType+"  getAttribute"+i+"(  )"))getter=true;
                        else if(cur.getName().equals("public void  setAttribute"+i+"( "+defaultAttributeType+" val )"))setter=true;
                    }
                    assertTrue("there is no getter in operations compartment", getter);
                    assertTrue("there is no setter in operations compartment", setter);
            }
            //in tree
             if(inTree || cmpA!=null)
            {
                assertTrue("there is no attribute within "+allRolesN[i].getTreePath(),allRolesN[i].isChildPresent((allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?"public":defaultAttributeVisibility)+" "+defaultAttributeType+" attribute"+i+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?" = 0":"")));
                Node opNode=allRolesN[i];
                if(opNode.isChildPresent("Operations"))opNode=new Node(opNode,"Operations");
                assertTrue("there is "+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?"":"no")+" setter in "+opNode.getTreePath()+" node in project tree", allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)^opNode.isChildPresent("public void  setAttribute"+i+"( "+defaultAttributeType+" val )"));
                assertTrue("there is "+(allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)?"":"no")+" getter in "+opNode.getTreePath()+" node in project tree", allRoles[i].equals(ExpandedElementTypes.INTERFACE_ROLE)^opNode.isChildPresent("public "+defaultAttributeType+"  getAttribute"+i+"(  )"));
            }
        }
    }
    /**
     * @caseblock Building Patterns
     * @usecase Add attribute trough model tree (Class Diagram)
     */
    public void testAddAttributeInTreeClD()
    {
        lastTestCase=getCurrentTestMethodName();
        commonAddAttribute(true,"Class Diagram");
        makeScreen=false;
    }
    /**
     * @caseblock Building Patterns
     * @usecase Add attribute through model tree (Component Diagram)
     */
    public void testAddAttributeInTreeCoD()
    {
        lastTestCase=getCurrentTestMethodName();
        commonAddAttribute(true,"Component Diagram");
        makeScreen=false;
    }
    /**
     * @caseblock Building Patterns
     * @usecase Add attribute through diagram (Class Diagram)
     */
    public void testAddAttributeOnDiagramClD()
    {
        lastTestCase=getCurrentTestMethodName();
        commonAddAttribute(false,"Class Diagram");
        makeScreen=false;
    }
    /**
     * @caseblock Building Patterns
     * @usecase Add attribute through diagram (Component Diagram)
     */
    public void testAddAttributeOnDiagramCoD()
    {
        lastTestCase=getCurrentTestMethodName();
        commonAddAttribute(false,"Component Diagram");
        makeScreen=false;
    }
    //
    public void tearDown() {
        if(makeScreen)Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        //
       closeAllModal();
       org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)new Node(prTree,lastDiagramNode.getParentPath()).collapse();
        //
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
        try
        {
            DiagramOperator d=new DiagramOperator("dgr");
            DrawingAreaOperator drAr=d.getDrawingArea();
            drAr.pushKey(KeyEvent.VK_ESCAPE);
            java.awt.Point a=drAr.getFreePoint();
            drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
            drAr.pushKey(KeyEvent.VK_ESCAPE);
            new Thread(){
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"No").push();
                }
            }.start();
            try{Thread.sleep(100);}catch(Exception ex){}//short sleep to allow others threads to go
            d.closeAllDocuments();
            try{Thread.sleep(100);}catch(Exception ex){}//short sleep to allow others threads to go
        }
        catch(Exception ex)
        {
            
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
         //save
         org.netbeans.test.umllib.util.Utils.tearDown();
  }
    
   public boolean compareWithoutExtraSpaceChars(String s1,String s2)
   {
       return org.netbeans.test.uml.designpatterns.utils.Utils.compareWithoutExtraSpaceChars(s1,s2);
   }

    
}
