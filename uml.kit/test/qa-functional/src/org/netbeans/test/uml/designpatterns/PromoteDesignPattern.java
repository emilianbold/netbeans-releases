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
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
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
public class PromoteDesignPattern extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String mainTreeTabName="Projects";
    private static String sourcePackGroup="Source Packages";
    private static String defPackageName="newpackage";
    private static String defClassName="NewClass";
    //common test properties
    private static String prName= "PromotePatternsProject";
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
    ExpandedElementTypes allApplied[]={ExpandedElementTypes.CLASS,ExpandedElementTypes.INTERFACE,ExpandedElementTypes.ACTOR,ExpandedElementTypes.CLASS,ExpandedElementTypes.USE_CASE};
    LinkTypes allLinks[]={LinkTypes.DEPENDENCY};
    private static boolean makeScreen=false;
  
    
    /** Need to be defined because of JUnit */
    public PromoteDesignPattern(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.designpatterns.PromoteDesignPattern.class);

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
            addProject();
            //close unnecessary windows
            TopComponentOperator doc=null;
            try{doc=new TopComponentOperator("Documentation");doc.close();}catch(Exception ex){}
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
    public void commonPreparePattern(String diagramType) {
        pkgCounter++;
        String curPkg="pkg"+pkgCounter;
        DiagramOperator dgr=org.netbeans.test.umllib.Utils.createOrOpenDiagram(prName,curPkg,"dgr"+pkgCounter,diagramType).dOp;
        lastDiagramNode = new Node(prRoot,curPkg+"|"+"dgr"+pkgCounter);
        DrawingAreaOperator drA=dgr.getDrawingArea();
        int numChild=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        DiagramElementOperator dpE=null;
        try
        {
            dpE=dgr.putElementOnDiagram("DP"+pkgCounter,ExpandedElementTypes.DESIGN_PATTERN);
        }
        catch(NotFoundException ex)
        {
            fail("Can't find design pattern: "+ex);
        }
        //
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP"+pkgCounter);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
        java.awt.Point tmp=null;
        for(int i=1;i<(allRoles.length-1);i++)
        {
            ExpandedElementTypes it=allRoles[i];
            try {
                pl.selectTool(LibProperties.getProperties().getToolName(it));
            } catch(NotFoundException ex) {
                fail("BLOCKING: Can't find '"+it+"' in paletter");
            }
            drA.clickMouse(dpE.getCenterPoint().x,dpE.getCenterPoint().y,1);
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
        assertTrue("There should be "+(allRoles.length-2)+" binding links from design pattern to roles, now: "+dpE.getLinks().size(), (allRoles.length-2)==dpE.getLinks().size());
        assertTrue("There should be "+(allRoles.length-2)+" roles within design pattern node in project tree, now: "+dpNode.getChildren().length, (allRoles.length-2)==dpNode.getChildren().length);
        //name roles
        for(int i=1;i<(allRoles.length-1);i++)
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
            for(int j=0;j<("name"+pkgCounter+"_"+i).length();j++)
            {
                drA.typeKey(("name"+pkgCounter+"_"+i).charAt(j));
            }
            tmp=drA.getFreePoint();
            drA.clickMouse(tmp.x,tmp.y,1);
           try
            {
                curRole=new DiagramElementOperator(dgr,"name"+pkgCounter+"_"+i,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find named "+it+" on diagram");
            }
            new Node(dpNode,"name"+pkgCounter+"_"+i);
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
        for(int i=1;i<(allRoles.length-1);i++)
        {
            assertTrue("there is no 'name"+pkgCounter+"_"+i+"' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("name"+pkgCounter+"_"+i)>-1);
            assertTrue("there is no 'name"+pkgCounter+"_"+i+"' node within design pattern node",dpNode.isChildPresent("name"+pkgCounter+"_"+i));
        }
        assertTrue("There should be "+(allRoles.length-2)+" names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==(allRoles.length-2));
    }
    /**
     * @caseblock Promote design pattern to Design Center
     * @usecase Promote a pattern, leaving in local project
     */
     public void testPromoteWORemove() {
        lastTestCase=getCurrentTestMethodName();
        commonPreparePattern("Component Diagram");
        //
        String curPkg="pkg"+pkgCounter;
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        dpNode.performPopupActionNoBlock("Promote Design Pattern...");
        JDialogOperator prD=new JDialogOperator("Promote Pattern to Design Center");
        JLabelOperator prL=new JLabelOperator(prD,"Project:");
        JComboBoxOperator prC=new JComboBoxOperator((JComboBox) (prL.getLabelFor()));
        prC.selectItem("projectForPromotions");
        //JLabelOperator nspl=new JLabelOperator(prD,"Namespace:");
        //nspl.waitComponentShowing(true);
        //JComboBoxOperator nsCb=new JComboBoxOperator((JComboBox)(nspl.getLabelFor()));
        //nsCb.selectItem();
        //nsCb.waitItemSelected(curPkg);
        new JButtonOperator(prD,"OK").pushNoBlock();
        prD.waitClosed();
        //check results
        //remains in model
        DiagramOperator dgr=new DiagramOperator("dgr"+pkgCounter);
        DrawingAreaOperator drA=dgr.getDrawingArea();
        DiagramElementOperator dpE=null;
        try
        {
            dpE=new DiagramElementOperator(dgr,"DP"+pkgCounter);
        }
        catch(Exception ex)
        {
            fail("Can't find design pattern on diagram after promotion.");
        }
        assertTrue("There should be "+(allRoles.length-2)+" binding links from design pattern to roles, now: "+dpE.getLinks().size(), (allRoles.length-2)==dpE.getLinks().size());
        assertTrue("There should be "+(allRoles.length-2)+" roles within design pattern node in project tree, now: "+dpNode.getChildren().length, (allRoles.length-2)==dpNode.getChildren().length);
        //roles
        for(int i=1;i<(allRoles.length-1);i++)
        {
            ExpandedElementTypes it=allRoles[i];
            DiagramElementOperator curRole=null;
            //
            try
            {
                curRole=new DiagramElementOperator(dgr,"name"+pkgCounter+"_"+i,it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find "+it+" on diagram");
            }
            new Node(dpNode,"name"+pkgCounter+"_"+i);
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
        for(int i=1;i<(allRoles.length-1);i++)
        {
            assertTrue("there is no 'name"+pkgCounter+"_"+i+"' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("name"+pkgCounter+"_"+i)>-1);
            assertTrue("there is no 'name"+pkgCounter+"_"+i+"' node within design pattern node",dpNode.isChildPresent("name"+pkgCounter+"_"+i));
        }
        assertTrue("There should be "+(allRoles.length-2)+" names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==(allRoles.length-2));
        //appears in design center
        Action open=new Action("Window|Other|UML Design Center",null);
        open.performMenu();
        TopComponentOperator dc=null;
        try
        {
            dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        }
        catch(Exception ex)
        {
            fail("Design Center wasn't opend via menu");
        }
        JTreeOperator tr=new JTreeOperator(dc);
        TreePath tmpP=null;
        tmpP=tr.findPath("DesignPatternCatalog|"+"projectForPromotions");
        tr.expandPath(tmpP);
        tr.waitExpanded(tmpP);
        tmpP=tr.findPath("DesignPatternCatalog|"+"projectForPromotions|"+"DP"+pkgCounter);
        tr.expandPath(tmpP);
        tr.waitExpanded(tmpP);
        //roles
        for(int i=1;i<(allRoles.length-1);i++)
        {
            tr.findPath("DesignPatternCatalog|"+"projectForPromotions|"+"DP"+pkgCounter+"|"+"name"+pkgCounter+"_"+i);
        }
        
        
        makeScreen=false;
    }
   
    //
    public void tearDown() {
       if(makeScreen) Utils.makeScreenShot(lastTestCase);
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
                    try{Thread.sleep(2000);}catch(Exception ex){}
                    new JButtonOperator(new JDialogOperator("Save Diagram"),"Discard").push();
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

    public void addProject() {
        Action open=new Action("Window|Other|UML Design Center",null);
        open.performMenu();
        TopComponentOperator dc=null;
        try
        {
            dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        }
        catch(Exception ex)
        {
            fail("Design Center wasn't opend via menu");
        }
        //
        JTreeOperator tr=new JTreeOperator(dc);
        //
        assertTrue("There is no Design Center root node or invisible, current root:"+tr.getRoot(),"Design Center".equals(tr.getRoot().toString()) && tr.isRootVisible());
        tr.findPath("DesignPatternCatalog");
        new JPopupMenuOperator(tr.callPopupOnPath(tr.findPath("DesignPatternCatalog"))).pushMenuNoBlock("New|Project...");
        JDialogOperator nw=new JDialogOperator(org.netbeans.test.uml.designpatterns.utils.Utils.createDesignCenterProjectTitle);
        JLabelOperator pn=new JLabelOperator(nw,org.netbeans.test.uml.designpatterns.utils.Utils.createDesignCenterProjectNameLabel);
        JLabelOperator lo=new JLabelOperator(nw,org.netbeans.test.uml.designpatterns.utils.Utils.createDesignCenterProjectLocationLabel);
        JTextFieldOperator pnT=new JTextFieldOperator((JTextField)(pn.getLabelFor()));
        JTextFieldOperator loT=new JTextFieldOperator((JTextField)(lo.getLabelFor()));
        pnT.typeText("projectForPromotions");
        loT.clearText();
        loT.typeText(workdir);
        new JButtonOperator(nw,"OK").push();
        nw.waitClosed();
        new EventTool().waitNoEvent(500);
//CAN'T REPRODUCE IN TESTS
//        nw=new JDialogOperator("Create New Diagram");
//        new JTextFieldOperator(nw).typeText("diagram");
//        new JButtonOperator(nw,"OK").push();
        tr.findPath("DesignPatternCatalog|"+"projectForPromotions");
//        tr.findPath("DesignPatternCatalog|"+"projectForDelete"+"|"+"diagram");
  }
    
}
