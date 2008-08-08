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
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTableOperator;
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
public class ApplyingPatterns extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String mainTreeTabName="Projects";
    private static String sourcePackGroup="Source Packages";
    private static String defPackageName="newpackage";
    private static String defClassName="NewClass";
    //common test properties
    private static String prName= "ApplyingPatternsProject";
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
    public ApplyingPatterns(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.designpatterns.ApplyingPatterns.class);

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
        java.awt.Point tmp=null,tmp2=null;
        for(int i=1;i<(allRoles.length-1);i++)
        {
            ExpandedElementTypes it=allRoles[i];
            try {
                pl.selectTool(LibProperties.getProperties().getToolName(it));
            } catch(NotFoundException ex) {
                fail("BLOCKING: Can't find '"+it+"' in paletter");
            }
            //try{Thread.sleep(4000);}catch(Exception ex){}
            tmp2=dpE.getCenterPoint();
            drA.clickMouse(tmp2.x,tmp2.y,1);
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
        for(int i=1;i<(allRoles.length-1);i++)
        {
            assertTrue("there is no 'name"+i+"' role in parameters comnpartment for design pattern", allRolesInDPStr.indexOf("name"+i)>-1);
            assertTrue("there is no 'name"+i+"' node within design pattern node",dpNode.isChildPresent("name"+i));
        }
        assertTrue("There should be "+(allRoles.length-2)+" names in parameters compartment, now:"+allRolesInDPStr.split(",").length, allRolesInDPStr.split(",").length==(allRoles.length-2));
    }
    /**
     * @caseblock Applying pattern
     * @usecase Apply design pattern to project as whole through project tree
     */
     public void testApplyDPToWholeProject() {
        lastTestCase=getCurrentTestMethodName();
        commonPreparePattern("Class Diagram");
        //close dgr
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
                    new JButtonOperator(new JDialogOperator("Save Diagram"),"Save").push();
                }
            }.start();
            d.closeAllDocuments();
        }
        catch(Exception ex)
        {
        }
        //
        String curPkg="pkg"+pkgCounter;
        Node dpNode=new Node(prTree,lastDiagramNode.getParentPath()+"|"+"DP");
        dpNode.performPopupActionNoBlock("Apply Design Pattern...");
        JDialogOperator dpWiz=new JDialogOperator("Design Pattern Apply Wizard");
        new JButtonOperator(dpWiz,"Next").pushNoBlock();
        dpWiz=new JDialogOperator("Design Pattern Wizard");
        new JButtonOperator(dpWiz,"Next").pushNoBlock();
        new EventTool().waitNoEvent(500);
        JLabelOperator nspl=new JLabelOperator(dpWiz,"Namespace:");
        nspl.waitComponentShowing(true);
        JComboBoxOperator nsCb=new JComboBoxOperator((JComboBox)(nspl.getLabelFor()));
        nsCb.selectItem(curPkg);
        nsCb.waitItemSelected(curPkg);
        new JButtonOperator(dpWiz,"Next").push();
        JLabelOperator tmpLbl=new JLabelOperator(dpWiz,"Choosing participant");
        JTableOperator tbl=new JTableOperator(dpWiz);
        tbl.waitComponentShowing(true);
        for(int i=1;i<(allRoles.length-1);i++)
        {
            tbl.clickOnCell(tbl.findCellRow("name"+i,0),tbl.findCellColumn("name"+i,0)+1,1);
            for(int j=0;j<("_applied").length();j++)
            {
                tbl.typeKey(("_applied").charAt(j));
            }
            tbl.pushKey(KeyEvent.VK_ENTER);
        }
        new JButtonOperator(dpWiz,"Next").push();
        new JCheckBoxOperator(dpWiz).clickMouse();
        try{Thread.sleep(1000);}catch(Exception ex){}
        //
        JTextFieldOperator tDgrName=new JTextFieldOperator(dpWiz,"DP"+"Diagram");
        DriverManager.setKeyDriver(new KeyEventDriver());
        tDgrName.clearText();
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));
        tDgrName.typeText("dgr"+"DP"+"Applied");
        //
        new JButtonOperator(dpWiz,"Next").push();
        new JButtonOperator(dpWiz,"Finish").pushNoBlock();
        dpWiz.waitClosed();
        //check results
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        Node newDgrNode=new Node(pkgNode,"dgr"+"DP"+"Applied");
        DiagramOperator newDgr=new DiagramOperator("dgr"+"DP"+"Applied");
        //elements
        for(int i=1;i<(allRoles.length-1);i++)
        {
            ExpandedElementTypes it=allApplied[i];
            DiagramElementOperator curEl=null;
            //
           try
            {
                curEl=new DiagramElementOperator(newDgr,"name"+i+"_applied",it,0);
            }
            catch(NotFoundException ex)
            {
                fail("Can't find named "+"name"+i+"_applied"+" on diagram");
            }
            new Node(pkgNode,"name"+i+"_applied");
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

    
}
