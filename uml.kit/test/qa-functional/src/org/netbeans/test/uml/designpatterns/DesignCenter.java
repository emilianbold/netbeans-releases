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
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Date;
import javax.swing.JTextField;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.util.Dumper;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.*;


/**
 *
 * @author psb
 * @spec uml/DesignPatterns
 */
public class DesignCenter extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String mainTreeTabName="Projects";
    private static String sourcePackGroup="Source Packages";
    private static String defPackageName="newpackage";
    private static String defClassName="NewClass";
    //common test properties
    private static String prName= "DesignCentertestHelper";
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
   //--
    private static String classDiagramName1 = "clD98";
    private static String workPkg1 = "pkg98";
    private static String  className1_1 ="class98_1";
    private static String  className1_2 ="class98_2";
    private static String  attributeName1="m"+className1_1;
    private static String propTitle1_1=className1_1+" - Properties";
    private static String propTitle1_2=className1_2+" - Properties";
    private static String setStr1="public void set"+attributeName1.substring(0,1).toUpperCase()+attributeName1.substring(1);
    private static String getStr1="public "+className1_1+" get"+attributeName1.substring(0,1).toUpperCase()+attributeName1.substring(1);
    private static String setStr1_b="public void  set"+attributeName1.substring(0,1).toUpperCase()+attributeName1.substring(1);
    private static String getStr1_b="public "+className1_1+"  get"+attributeName1.substring(0,1).toUpperCase()+attributeName1.substring(1);
    private static boolean makeScreen=false;
  
    //
    private String desCMP="Window|Other|UML Design Center";
    
    /** Need to be defined because of JUnit */
    public DesignCenter(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.designpatterns.DesignCenter.class);
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
        lastTestCase=".";
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
        if(!codeSync)
        {
            //org.netbeans.test.uml.designpatterns.utils.Utils.setDefaultPreferences();
            org.netbeans.test.uml.designpatterns.utils.Utils.prepareProjects(workdir, prName);
            //close unnecessary windows
            TopComponentOperator doc=null;
            try{doc=new TopComponentOperator("Documentation");doc.close();}catch(Exception ex){}
            codeSync=true;
        }
        new EventTool().waitNoEvent(MINWAIT);
        pto = ProjectsTabOperator.invoke();
        JTreeOperator prTree=new JTreeOperator(pto);
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
   
    /**
     * @caseblock Design Center
     * @usecase Open Design Center via Menu
     */
    public void testOpenWithMenu() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        makeScreen=false;
    }
    /**
     * @caseblock Design Center
     * @usecase Open Design Center via Shortcut
     */
    public void testOpenWithShortcut() {
        lastTestCase=getCurrentTestMethodName();
        MainWindowOperator mw=MainWindowOperator.getDefault();
        mw.pushKey(KeyEvent.VK_W, KeyEvent.SHIFT_MASK|KeyEvent.ALT_MASK);
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        makeScreen=false;
    }

    /**
     * @caseblock Design Center
     * @usecase Close Design Center with Menu
     */
   public void testCloseWithMenu() {
       lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE).pushMenuOnTab("Close Window");
        new EventTool().waitNoEvent(MINWAIT);
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
        try
        {
            new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
            fail("Design Center wasn't closed via tab menu");
        }
        catch(Exception ex)
        {
            //good
        }
        finally
        {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
        }
        makeScreen=false;
   }
    /**
     * @caseblock Design Center
     * @usecase Close Design Center with Shortcut
     */
   public void testCloseWithShortcut() {
       lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE).pushKey(KeyEvent.VK_F4, KeyEvent.CTRL_MASK);
        new EventTool().waitNoEvent(MINWAIT);
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
        try
        {
            new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
            fail("Design Center wasn't closed via tab menu");
        }
        catch(Exception ex)
        {
            //good
        }
        finally
        {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
        }
        makeScreen=false;
   }
    /**
     * @caseblock Design Center
     * @usecase Close Design Center with cross
     */
   public void testCloseWithCross() {
       lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE).close();
        new EventTool().waitNoEvent(MINWAIT);
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
        try
        {
            new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
            fail("Design Center wasn't closed via tab menu");
        }
        catch(Exception ex)
        {
            //good
        }
        finally
        {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
        }
        makeScreen=false;
   }
     /**
     * @caseblock Design Center
     * @usecase Maximize
     */
    public void testMaximize() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        int oldW=dc.getWidth();
        int oldH=dc.getHeight();
        dc.pushMenuOnTab("Maximize");
        new EventTool().waitNoEvent(MINWAIT);
        try{Thread.sleep(100);}catch(Exception ex){}
        int newW=dc.getWidth();
        int newH=dc.getHeight();
        assertFalse("Design Center wasn't maximized, new size: "+newW+","+newH+"; old: "+oldW+","+oldH, newW<=oldW && newH<=oldH);
        makeScreen=false;
    }
     /**
     * @caseblock Design Center
     * @usecase Minimize
     */
    public void testMinimize() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        int oldW=dc.getWidth();
        int oldH=dc.getHeight();
        dc.pushMenuOnTab("Maximize");
        new EventTool().waitNoEvent(MINWAIT);
        try{Thread.sleep(100);}catch(Exception ex){}
        int newW=dc.getWidth();
        int newH=dc.getHeight();
        dc.pushMenuOnTab("Restore Window");
        new EventTool().waitNoEvent(MINWAIT);
        try{Thread.sleep(100);}catch(Exception ex){}
        int newOldW=dc.getWidth();
        int newOldH=dc.getHeight();
        assertTrue("Design Center wasn't minimized, new size2: "+newOldW+","+newOldH+"; new size: "+newW+","+newH+"; old: "+oldW+","+oldH, newOldW<newW && newOldH<=newH);
        makeScreen=false;
    }
     /**
     * @caseblock Design Center
     * @usecase Base tree structure
     */
    public void testTreeStructure() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        //
        JTreeOperator tr=new JTreeOperator(dc);
        //
        assertTrue("There is no Design Center root node or invisible, current root:"+tr.getRoot(),"Design Center".equals(tr.getRoot().toString()) && tr.isRootVisible());
        tr.findPath("DesignPatternCatalog");
//        tr.findPath("Requirements");
        tr.findPath("DesignPatternCatalog|EJB1.1");
        tr.findPath("DesignPatternCatalog|EJB2.0");
        tr.findPath("DesignPatternCatalog|GoF Design Patterns");
        makeScreen=false;
   }
     /**
     * @caseblock Design Center
     * @usecase Insert Project Into Workspace
     */
    public void testInsertProject() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        //create project
        
        org.netbeans.test.uml.designpatterns.utils.Utils.prepareProjects(workdir, "ForInsert");
        prTree=new JTreeOperator(pto);
        ProjectRootNode prRoot=new ProjectRootNode(prTree, "ForInsert|Model");
        DiagramOperator ret=org.netbeans.test.umllib.Utils.createOrOpenDiagram("ForInsert","pkg","dgr",org.netbeans.test.umllib.NewDiagramWizardOperator.CLASS_DIAGRAM).dOp;
        lastDiagramNode = new Node(prRoot,"pkg"+"|"+"dgr");
        try
        {
            ret.putElementOnDiagram("classInsert",ElementTypes.CLASS);
        }
        catch(Exception ex)
        {
            fail("can't add class to the diagram");
        }
        //
        try
        {
            dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        }
        catch(Exception ex)
        {
            fail("Design Center wasn't opened via menu");
        }
        //
        JTreeOperator tr=new JTreeOperator(dc);
        //
        tr.clickOnPath(tr.getPathForRow(0),1,InputEvent.BUTTON3_MASK);
        new JPopupMenuOperator().pushMenuNoBlock("Insert|Insert Project Into Workspace");
        JDialogOperator  id=new JDialogOperator("Insert project");
        JLabelOperator pl=new JLabelOperator(id,"File name:");
        JTextFieldOperator nt=new JTextFieldOperator((JTextField)(pl.getLabelFor()));
        nt.typeText(workdir+File.separator+"ForInsert"+File.separator+"ForInsert.etd");
        new JButtonOperator(id,"Open").pushNoBlock();
        id.waitClosed();
        try{Thread.sleep(2000);}catch(Exception ex){}
        tr.findPath("DesignPatternCatalog");
        tr.findPath("DesignPatternCatalog|ForInsert");
        tr.findPath("DesignPatternCatalog|ForInsert|pkg");
        tr.findPath("DesignPatternCatalog|ForInsert|pkg|dgr");
        tr.findPath("DesignPatternCatalog|ForInsert|pkg|classInsert");
        makeScreen=false;
   }
    /**
     * @caseblock Design Center
     * @usecase Remove Project from Design Center
     */
    public void testRemoveProject() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
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
        pnT.typeText("projectForDelete");
        try{Thread.sleep(100);}catch(Exception ex){}
        loT.clearText();
        loT.typeText(workdir);
        new JButtonOperator(nw,"OK").push();
        nw.waitClosed();
        new EventTool().waitNoEvent(500);
//CAN'T REPRODUCE IN TESTS
//        nw=new JDialogOperator("Create New Diaram");
//        new JTextFieldOperator(nw).typeText("diagram");
//        new JButtonOperator(nw,"OK").push();
        tr.moveMouse(0,0);
        tr.findPath("DesignPatternCatalog|"+"projectForDelete");
//        tr.findPath("DesignPatternCatalog|"+"projectForDelete"+"|"+"diagram");
        tr.collapsePath(tr.findPath("DesignPatternCatalog|"+"projectForDelete"));
        JButtonOperator b=null;
        for(int i=0;i<6;i++)
        {
            b=new JButtonOperator(dc,i);
            if(b.getSource().toString().indexOf("refresh")>-1)
            {
                break;
            }
        }
        b.push();
        new JPopupMenuOperator(tr.callPopupOnPath(tr.findPath("DesignPatternCatalog|"+"projectForDelete"))).pushMenuNoBlock("Remove|Remove Project From Workspace");
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(1000);}catch(Exception ex){}
        try
        {
            tr.findPath("DesignPatternCatalog|"+"projectForDelete");
            fail(114168, "project wasn't removed from design center");
        }
        catch(Exception ex)
        {
            //good
        }
        makeScreen=false;
  }
    /**
     * @caseblock Design Center
     * @usecase Add Project to Design Center
     */
    public void testAddProject() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
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
        pnT.typeText("projectForAdd");
        try{Thread.sleep(100);}catch(Exception ex){}
        loT.clearText();
        loT.typeText(workdir);
        new JButtonOperator(nw,"OK").push();
        nw.waitClosed();
        new EventTool().waitNoEvent(500);
//CAN'T REPRODUCE IN TESTS
//        nw=new JDialogOperator("Create New Diagram");
//        new JTextFieldOperator(nw).typeText("diagram");
//        new JButtonOperator(nw,"OK").push();
        tr.findPath("DesignPatternCatalog|"+"projectForAdd");
//        tr.findPath("DesignPatternCatalog|"+"projectForDelete"+"|"+"diagram");
        makeScreen=false;
  }
    /**
     * @caseblock Design Center
     * @usecase Rename Project in Design Center with Shortcut
     */ 
    
    /* F2 does not work in 6.0
    public void testRenameProjectWShortcut() {
        lastTestCase=getCurrentTestMethodName();
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
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
        pnT.typeText("projectForRenameS");
         try{Thread.sleep(100);}catch(Exception ex){}
        loT.clearText();
        loT.typeText(workdir);
        new JButtonOperator(nw,"OK").push();
        nw.waitClosed();
        new EventTool().waitNoEvent(500);        
//CAN'T REPRODUCE IN TESTS
//        nw=new JDialogOperator("Create New Diagram");
//        new JTextFieldOperator(nw).typeText("diagram");
//        new JButtonOperator(nw,"OK").push();
        tr.findPath("DesignPatternCatalog|"+"projectForRenameS");
//        tr.findPath("DesignPatternCatalog|"+"projectForDelete"+"|"+"diagram");
        tr.selectPath(tr.findPath("DesignPatternCatalog|"+"projectForRenameS"));
        //setup separate logs
        String OUT_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_out.txt";
        String ERR_LOG_FILE=workdir+"/user/"+this.getClass().getName()+"/"+lastTestCase+"/psb_err.txt";
        TestOut defTestOut=JemmyProperties.getCurrentOutput();
        try
        {
            PrintStream myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            PrintStream myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        }
        catch(Exception ex)
        {
            
        }
       //
        tr.pushKey(KeyEvent.VK_F2);
        //try dialogs
         long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try
        {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
            JDialogOperator ex=new JDialogOperator("Unexpected Exception");
            assertFalse("NPE on attempt to rename project in design center(known but isn't filed yet)",new JLabelOperator(ex).getText().indexOf("java.lang.NullPointerException")>-1);
            fail("Unexpected exception on attempt to rename project in design center");
        }
        catch(Exception ex)
        {
        }
        finally
        {
             JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
        }
        //restore default logs
         JemmyProperties.setCurrentOutput(defTestOut);
        //try jemmy logs
        try
        {
            BufferedReader myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("java.lang.NullPointerException")>-1){
                    fail(78872, "NPE on attempt to rename project in design center, isPathEditable="+tr.isPathEditable(tr.getSelectionPath()));
                }
                else if (line!=null && line.indexOf("Exception")>-1)
                {
                    fail("Unexpected exception on attempt to rename project in design center");
                }
            } while (line != null);
        }
        catch(Exception ex)
        {
            
        }
        try {
            //
            Dumper.dumpAll(workdir+"/user/" + getClass().getName()+"/fulldump_"+new Date().getTime()+".xml");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        //
        fail("tests incomplete (there was blocking Defect)");
        //
        makeScreen=false;
  }
  */
    
    
    /**
     * @caseblock Design Center
     * @usecase Rename Project in Design Center with Shortcut
     */
    public void testRenameProjectWMenu() {
        lastTestCase=getCurrentTestNamesWithCheck()[1];
        Action open=new Action(desCMP,null);
        open.performMenu();
        TopComponentOperator dc=null;
        dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
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
        pnT.typeText("projectForRenameM");
         try{Thread.sleep(100);}catch(Exception ex){}
        loT.clearText();
        loT.typeText(workdir);
        new JButtonOperator(nw,"OK").push();
        nw.waitClosed();
        new EventTool().waitNoEvent(500);        
//CAN'T REPRODUCE IN TESTS
//        nw=new JDialogOperator("Create New Diagram");
//        new JTextFieldOperator(nw).typeText("diagram");
//        new JButtonOperator(nw,"OK").push();
        tr.findPath("DesignPatternCatalog|"+"projectForRenameM");
//        tr.findPath("DesignPatternCatalog|"+"projectForDelete"+"|"+"diagram");
        tr.selectPath(tr.findPath("DesignPatternCatalog|"+"projectForRenameM"));
        new JPopupMenuOperator(tr.callPopupOnPath(tr.findPath("DesignPatternCatalog|"+"projectForRenameM"))).pushMenu("Rename");
        new EventTool().waitNoEvent(500);
        assertTrue(78873,"Edition wasn't enabled for 'Rename' of project in design center", tr.getEditingPath()!=null);
        //
        fail("tests incomplete (there is/was blocking Defect seee)");
        //
        makeScreen=false;
   }
  
    public void tearDown() {
        if(makeScreen)Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        org.netbeans.test.umllib.util.Utils.saveAll();
        //close design center
        long oldwait=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",5000);
        TopComponentOperator dc=null;
        try
        {
            dc=new TopComponentOperator(LabelsAndTitles.DESIGN_CENTER_TITLE);
        }
        catch(Exception ex)
        {
            
        }
        if(dc!=null)
        {
            Dimension tmp_old=dc.getSize();
            dc.closeWindow();
            //wait 5 seconds, there was issue with waitClosed for top component, if will be resolved next code can be replaced
            for(long i=0;i<5000;i+=100)
            {
                if(dc.isVisible())
                {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            }
            if(dc.isShowing())
            {
                Dimension tmp_new=dc.getSize();
                Utils.makeScreenShotCustom(lastTestCase,"beforeCrossClose_");
                dc.close();
                assertFalse(102328,"Close/cross restore maximized top component instead of closing",tmp_new.width<tmp_old.width || tmp_new.height<tmp_old.height);
                fail("Design Center wasn't closed");
            }
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",oldwait);
        //
        closeAllModal();
         //save
        org.netbeans.test.umllib.util.Utils.tearDown();
  }
    
   public boolean compareWithoutExtraSpaceChars(String s1,String s2)
   {
       return org.netbeans.test.uml.designpatterns.utils.Utils.compareWithoutExtraSpaceChars(s1,s2);
   }

    
}
