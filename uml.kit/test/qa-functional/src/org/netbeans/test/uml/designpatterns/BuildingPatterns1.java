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

import org.netbeans.jemmy.operators.JButtonOperator;

import org.netbeans.jemmy.operators.JDialogOperator;

import org.netbeans.jemmy.operators.JTreeOperator;





import org.netbeans.junit.NbTestSuite;

//import org.netbeans.test.umllib.UMLClassOperator;

import org.netbeans.test.umllib.CompartmentOperator;

import org.netbeans.test.umllib.CompartmentTypes;

import org.netbeans.test.umllib.DiagramElementOperator;

import org.netbeans.test.umllib.DiagramOperator;

import org.netbeans.test.umllib.DrawingAreaOperator;

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

public class BuildingPatterns1 extends UMLTestCase {

    

    //some system properties

    private static String contextPropItemName="Properties";

    private static String umlPropertyWindowTitle="Project Properties";

    private static String mainTreeTabName="Projects";

    private static String sourcePackGroup="Source Packages";

    private static String defPackageName="newpackage";

    private static String defClassName="NewClass";

    //common test properties

    private static String prName= "BuildingPatternsProject1";

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

    public BuildingPatterns1(String name) {

        super(name);

    }

     public static NbTestSuite suite() {

         NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.designpatterns.BuildingPatterns1.class);

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

     * @caseblock Building Patterns

     * @usecase Create and binding in the same time roles and pattern (Class Diagram)

     */

    public void testCreateAndBindAtOnceClD() {

        lastTestCase=getCurrentTestMethodName();

        commonCreateAndBindAtOnce("Class Diagram");  

        makeScreen=false;

    }

    /**

     * @caseblock Building Patterns

     * @usecase Create and binding in the same time roles and pattern (Component Diagram)

     */

    public void testCreateAndBindAtOnceCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonCreateAndBindAtOnce("Component Diagram");

        makeScreen=false;

    }

    public void commonCreateThenBind(String diagramType) {

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

            tmp=drA.getFreePoint(80);

            drA.clickMouse(tmp.x,tmp.y,1);

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

        int numChild2=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;

        assertTrue("There should be "+allRoles.length+" role nodes within package in project tree, now: "+(numChild2-numChild-1), allRoles.length==(numChild2-numChild-1));

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

            tmp=drA.getFreePoint(80);

            drA.clickMouse(tmp.x,tmp.y,1);

           try

            {

                curRole=new DiagramElementOperator(dgr,"name"+i,it,0);

            }

            catch(NotFoundException ex)

            {

                fail("Can't find named "+it+" on diagram");

            }

            new Node(new Node(prTree,lastDiagramNode.getParentPath()),"name"+i);

        }

        //bind nodes

        try

        {

            pl.selectTool(LibProperties.getProperties().getToolName(LinkTypes.ROLE));

        }

        catch(NotFoundException ex)

        {

            fail("Can't find role binding link in palette");

        }

        for(int i=0;i<allRoles.length;i++)

        {

            DiagramElementOperator tmpE=null;

            try

            {

                    tmpE=new DiagramElementOperator(dgr,"name"+i);

            }

            catch(Exception ex)

            {

                fail("can't find 'name"+i+"' element on diagram");

            }

            tmpE.clickOnCenter();

            dpE.clickOnCenter();

        }

        tmp=drA.getFreePoint(10);

        drA.clickMouse(tmp.x,tmp.y,1,InputEvent.BUTTON3_MASK);

        drA.pushKey(KeyEvent.VK_ESCAPE);

        new EventTool().waitNoEvent(500);

        assertTrue("There should be "+allRoles.length+" binding links from design pattern to roles, now: "+dpE.getLinks().size(), allRoles.length==dpE.getLinks().size());

        assertTrue("There should be "+allRoles.length+" roles within design pattern node in project tree, now: "+dpNode.getChildren().length, allRoles.length==dpNode.getChildren().length);

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

     * @caseblock Building Patterns

     * @usecase Create and binding in the same time roles and pattern (Class Diagram)

     */

    public void testCreateThenBindClD() {

        lastTestCase=getCurrentTestMethodName();

        commonCreateThenBind("Class Diagram");

        makeScreen=false;

    }

    /**

     * @caseblock Building Patterns

     * @usecase Create and binding in the same time roles and pattern (Component Diagram)

     */

    public void testCreateThenBindCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonCreateThenBind("Component Diagram");

        makeScreen=false;

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

            else if (link.equals(LinkTypes.NAVIGABLE_ASSOCIATION))

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

     * @caseblock Building Patterns

     * @usecase Draw Generalization for roles (Class-Class) (Class Diagram)

     */

    public void testDrawGeneralizationClToClClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.GENERALIZATION,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Generalization for roles (Class-Class) (Class Diagram)

     */

    public void testDrawGeneralizationInToInClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.INTERFACE_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.GENERALIZATION,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Implementation for roles (Class-Interface) (Class Diagram)

     */

    public void testDrawImplementationClToInClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.IMPLEMENTATION,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Nested Link for roles (Class-Class) (Class Diagram)

     */

    public void testDrawNestedLinkClToClClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NESTED_LINK,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Dependency for roles (Class-Role) (Class Diagram)

     */

    public void testDrawDependancyClToRoClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Realize for roles (Actor-Role) (Class Diagram)

     */

    public void testDrawRealizeAcToRoClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.ACTOR_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Permission for roles (Use case-Class) (Class Diagram)

     */

    public void testDrawPermissionUsToClClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.USE_CASE_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.PERMISSION,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Navigable Association for roles (Class-Class) (Class Diagram)

     */

    public void testDrawNavigableAssocClToClClD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NAVIGABLE_ASSOCIATION,"Class Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Generalization for roles (Class-Class) (Class Diagram)

     */

    public void testDrawGeneralizationClToClCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.GENERALIZATION,"Component Diagram");

        makeScreen=false;

    }

   /**

    * 

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Generalization for roles (Class-Class) (Class Diagram)

     */

    public void testDrawGeneralizationInToInCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.INTERFACE_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.GENERALIZATION,"Component Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * not really class diagram, but very similar

    * @usecase Draw Implementation for roles (Class-Interface) (Class Diagram)

     */

    public void testDrawImplementationClToInCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.INTERFACE_ROLE,LinkTypes.IMPLEMENTATION,"Component Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Nested Link for roles (Class-Class) (Class Diagram)

     */

    public void testDrawNestedLinkClToClCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NESTED_LINK,"Component Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Dependency for roles (Class-Role) (Class Diagram)

     */

    public void testDrawDependancyClToRoCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Component Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Realize for roles (Actor-Role) (Class Diagram)

     */

    public void testDrawRealizeAcToRoCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.ACTOR_ROLE,ExpandedElementTypes.ROLE,LinkTypes.DEPENDENCY,"Component Diagram");

        makeScreen=false;

    }

   /**

     * @caseblock Building Patterns

     * @usecase Draw Permission for roles (Use case-Class) (Class Diagram)

     */

    public void testDrawPermissionUsToClCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.USE_CASE_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.PERMISSION,"Component Diagram");

        makeScreen=false;

        javax.swing.JDialog f;

    }

   /**

     * @caseblock Building Patterns

    * not really class diagram, but very similar

     * @usecase Draw Navigable Association for roles (Class-Class) (Class Diagram)

     */

    public void testDrawNavigableAssocClToClCoD() {

        lastTestCase=getCurrentTestMethodName();

        commonDrawLink(ExpandedElementTypes.CLASS_ROLE,ExpandedElementTypes.CLASS_ROLE,LinkTypes.NAVIGABLE_ASSOCIATION,"Component Diagram");

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

            d.closeAllDocuments();

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

