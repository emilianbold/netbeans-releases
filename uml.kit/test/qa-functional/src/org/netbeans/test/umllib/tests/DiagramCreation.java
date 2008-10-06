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


package org.netbeans.test.umllib.tests;

import java.awt.Component;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.NewPackageWizardOperator;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.testcases.UMLTestCase;

/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public abstract class DiagramCreation extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    public static String diagramType=null;//NewDiagramWizardOperator.ACTIVITY_DIAGRAM;
    public static String childClassName=null;
    private static ElementTypes elToPlace=null;
    private static String elToPlaceName=null;
    //common test properties
    public static String prName=null;// "ActivityDiagramProjectADC";
    public static String project = null;//prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static long elCount=0;



    
    /**  */
    public DiagramCreation(String name,String type,String prNm,String childClName,ElementTypes elToPlace,String elName) {
        super(name);
        diagramType=type;
        prName=prNm;
        project = prName+"|Model";
        childClassName=childClName;
        this.elToPlace=elToPlace;
    }
    /**
     * by default all diagrams are checked with COMMENT i.e. most common element
     */
    public DiagramCreation(String name,String type,String prNm,String childClName) {
        this(name,type,prNm,childClName,ElementTypes.COMMENT,"");
    }
   /* public DiagramCreation(String name) {
        super(name);
    }*/
     /*public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.umllib.tests.DiagramCreation.class);
        return suite;
    }*/
    
     public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            if(prName==null)throw new UMLCommonException("Project is null");
            org.netbeans.test.umllib.tests.utils.Utils.commonTestsSetup(workdir, prName,"Yes");
            //
            codeSync=true;
        }
    }
   

    public void testAddDiagramModel() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),project);
        //*workaround for some fails in jelly
        root.tree().waitVisible(root.getTreePath());
        root.tree().expandPath(root.getTreePath());
        root.tree().waitExpanded(root.getTreePath());
        try{Thread.sleep(1000);}catch(Exception ex){}
        java.awt.Rectangle pth=root.tree().getPathBounds(root.getTreePath());
        root.tree().moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        new EventTool().waitNoEvent(1000);
        //*workaround finished
        root.select();
        root.tree().waitSelected(root.getTreePath());
        root.callPopup().pushMenuNoBlock("New|Diagram");
        NewDiagramWizardOperator nw=new NewDiagramWizardOperator();
        nw.setDiagramType(diagramType);
        nw.setDiagramName(diagramName);
        nw.clickFinish();
        //
        DiagramOperator ad=new DiagramOperator(diagramName);
        new Node(root,diagramName);
        root=new ProjectRootNode(pto.tree(),prName+"|Diagram");
        root.expand();
        root.waitExpanded();
        new Node(root,diagramName);
    }
    public void testAddDiagramDiagram() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),prName+"|Diagram");
        //*workaround for some fails in jelly
        root.tree().waitVisible(root.getTreePath());
        root.tree().expandPath(root.getTreePath());
        root.tree().waitExpanded(root.getTreePath());
        try{Thread.sleep(1000);}catch(Exception ex){}
        java.awt.Rectangle pth=root.tree().getPathBounds(root.getTreePath());
        root.tree().moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        new EventTool().waitNoEvent(1000);
        //*workaround finished
        root.select();
        root.tree().waitSelected(root.getTreePath());
        root.callPopup().pushMenuNoBlock("New|Diagram");
        NewDiagramWizardOperator nw=new NewDiagramWizardOperator();
        nw.setDiagramType(diagramType);
        nw.setDiagramName(diagramName);
        nw.clickFinish();
        //
        DiagramOperator ad=new DiagramOperator(diagramName);
        new Node(root,diagramName);
        root=new ProjectRootNode(pto.tree(),prName+"|Model");
        root.expand();
        root.waitExpanded();
        new Node(root,diagramName);
   }
 
    public void testAddPackageWithDiagram() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
        ProjectRootNode root = new ProjectRootNode(pto.tree(),project);
        //*workaround for some fails in jelly
        root.tree().waitVisible(root.getTreePath());
        root.tree().expandPath(root.getTreePath());
        root.tree().waitExpanded(root.getTreePath());
        try{Thread.sleep(1000);}catch(Exception ex){}
        java.awt.Rectangle pth=root.tree().getPathBounds(root.getTreePath());
        root.tree().moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
        new EventTool().waitNoEvent(1000);
        //*workaround finished
        root.select();
        root.tree().waitSelected(root.getTreePath());
        JPopupMenuOperator pop=root.callPopup();
        pop.waitComponentVisible(true);
        pop.waitComponentShowing(true);
        pop.pushMenuNoBlock("New|Package");
        NewPackageWizardOperator nw=new NewPackageWizardOperator();
        nw.setScopedDiagram(workPkg,diagramName,diagramType);
        nw.clickFinish();
        //
        DiagramOperator ad=new DiagramOperator(diagramName);
    }

    public void testOpenExistingWithDoubleClick()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.pushMenuOnTab("Close Window");
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.tree().clickOnPath(lastDiagramNode.getTreePath(),2);
        try{Thread.sleep(100);}catch(Exception ex){}
        new DiagramOperator(diagramName);
    }
    
    public void testOpenExistingWithOpen()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        new DiagramOperator(diagramName);
    }
    
    public void testOpenExistingWithEnter()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.select();
        lastDiagramNode.tree().waitSelected(lastDiagramNode.getTreePath());
        lastDiagramNode.tree().pushKey(KeyEvent.VK_ENTER);
        try{Thread.sleep(100);}catch(Exception ex){}
        new DiagramOperator(diagramName);
    }
    
    public void testChanges()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
// nb6.5 not close/reopen diagram. Workaround for bug
//        rt.dOp.close();
//        rt.dOp.waitClosed();
//        try{Thread.sleep(100);}catch(Exception ex){}
//        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        sa.waitState(new ChooseEnabledState(true));
    }
    public void testSaveChangesSaveAll()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        lastDiagramNode.expand();lastDiagramNode.waitExpanded();
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        sa.push();
        try{Thread.sleep(100);}catch(Exception ex){}
        sa.waitState(new ChooseEnabledState(false));
        assertFalse("Save all toolbar button enabled after save ",sa.isEnabled());
        dr.close();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertFalse("Save all toolbar button enabled",sa.isEnabled());
    }
    public void testSaveChangesSaveDocument()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        dr.callMenuOnTab().pushMenu("Save Document");
        try{Thread.sleep(100);}catch(Exception ex){}
        //sa.waitState(new ChooseEnabledState(false));
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        try{Thread.sleep(100);}catch(Exception ex){}
        //assertFalse("Save all toolbar button enabled after save ",sa.isEnabled());
        dr.close();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled when only diagram was saved",sa.isEnabled());
    }
    public void testSaveNewWithSaveDocument()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        dr.callMenuOnTab().pushMenu("Save Document");
        try{Thread.sleep(100);}catch(Exception ex){}
        //sa.waitState(new ChooseEnabledState(false));
        //assertFalse("Save all toolbar button enabled after save ",sa.isEnabled());
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.close();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button enabled when only diagram was saved",sa.isEnabled());
    }
    public void testSaveChangesAfterCloseDocument()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.close();//I hope should be similar to cross pressing
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Save").push();
            }
        }.start();
        dr.waitClosed();
        sa.waitState(new ChooseEnabledState(true));
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testSaveChangesAfterCloseAll()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close All Documents");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Save").push();
            }
        }.start();
        dr.waitClosed();
        sa.waitState(new ChooseEnabledState(true));
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testSaveChangesAfterCloseWindow()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close Window");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Save").push();
            }
        }.start();
        dr.waitClosed();
        sa.waitState(new ChooseEnabledState(true));
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testSaveNewAfterCloseWindow()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close Window");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Save").push();
            }
        }.start();
        dr.waitClosed();
        sa.waitState(new ChooseEnabledState(true));
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testDontSaveNewAfterCloseWindow()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close Window");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Discard").push();
            }
        }.start();
        dr.waitClosed();
        sa.waitState(new ChooseEnabledState(true));
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr2=new DiagramOperator(diagramName);
        try
        {
            new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
            fail("Element is found on diagram after No to save");
        }
        catch(Exception ex)
        {
            //good
        }
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testSaveNewWithCtrlS()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        pm.waitComponentShowing(false);
        try{Thread.sleep(100);}catch(Exception ex){}
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        dr.pushKey(KeyEvent.VK_S,KeyEvent.CTRL_MASK);
        try{Thread.sleep(100);}catch(Exception ex){}
        //sa.waitState(new ChooseEnabledState(false));
        //assertFalse("Save all toolbar button enabled after save ",sa.isEnabled());
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.close();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button enabled when only diagram was saved",sa.isEnabled());
    }
    
    public void testCancelClose()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close Window");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        new Thread()
        {
            public void run()
            {
                new JButtonOperator(new JDialogOperator("Save"),"Cancel").push();
            }
        }.start();
        try
        {
            dr.waitClosed();
            fail("Diagram was closed after cancel in save dialog");
        }
        catch(Exception ex)
        {
            //good
        }
        //
        sa.waitState(new ChooseEnabledState(true));
        //
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    public void testCrossInSaveOnClose()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        final DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        new Thread()
        {
            public void run()
            {
                dr.pushMenuOnTab("Close Window");
            }
        }.start();
        try{Thread.sleep(100);}catch(Exception ex){}
        JDialogOperator dlg=new JDialogOperator("Save");
        dlg.close();//is it the same as cross?
        dlg.waitClosed();
        try
        {
            dr.waitClosed();
            fail("Diagram was closed after cancel in save dialog");
        }
        catch(Exception ex)
        {
            //good
        }
        //
        sa.waitState(new ChooseEnabledState(true));
        //
        DiagramOperator dr2=new DiagramOperator(diagramName);
        new DiagramElementOperator(dr2,elToPlaceName,elToPlace);
        pm=dr2.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
    }
    //
    public void testSaveNewSeveralDiagramsSaveAll()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_1",diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName+"_1");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //2nd
        org.netbeans.test.umllib.Utils.RetAll rt2=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_2",diagramType);
        dr=new DiagramOperator(diagramName+"_2");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //
        sa.push();
        try{Thread.sleep(100);}catch(Exception ex){}
        sa.waitState(new ChooseEnabledState(false));
        assertFalse("Save all toolbar button enabled after save ",sa.isEnabled());
        dr.closeAllDocuments();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_1");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertFalse("Save all toolbar button enabled",sa.isEnabled());
        rt2.lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_2");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertFalse("Save all toolbar button enabled",sa.isEnabled());
    }
    //
    public void testSaveByOneAfterCloseAll()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_1",diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName+"_1");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //2nd
        org.netbeans.test.umllib.Utils.RetAll rt2=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_2",diagramType);
        dr=new DiagramOperator(diagramName+"_2");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //
        final DiagramOperator drCl=dr;
        new Thread()
        {
            public void run()
            {
                drCl.closeAllDocuments();
            }
        }.start();
        JDialogOperator dlg=new JDialogOperator("Save");
        JButtonOperator btn=new JButtonOperator(dlg,"Save");
        btn.pushNoBlock();
        dlg.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        dlg=new JDialogOperator("Save");
        btn=new JButtonOperator(dlg,"Save");
        btn.push();
        dlg.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_1");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        rt2.lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_2");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
    }
    public void testDeleteEmptyNewDiagram()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        DiagramOperator dr=new DiagramOperator(diagramName);
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        final Node lastDiagramNode2=lastDiagramNode;
        new Thread()
        {
            public void run()
            {
                JPopupMenuOperator pop=lastDiagramNode2.callPopup();
                org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(childClassName,lastTestCase,"beforeClick");
                pop.pushMenu("Delete");
            }
        }.start();
        new JButtonOperator(new JDialogOperator("Confirm Object Deletion"),"Yes").push();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        lastDiagramNode.waitNotPresent();
        //
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
    }
    public void testDeleteModifiedDiagram()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        rt.dOp.close();
        rt.dOp.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName);
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        final Node lastDiagramNode2=lastDiagramNode;
        new Thread()
        {
            public void run()
            {
                JPopupMenuOperator pop=lastDiagramNode2.callPopup();
                org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(childClassName,lastTestCase,"beforeClick");
                pop.pushMenu("Delete");
            }
        }.start();
        new JButtonOperator(new JDialogOperator("Confirm Object Deletion"),"Yes").push();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        lastDiagramNode.waitNotPresent();
        //
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
    }
    //
    /**
     * It's good if it will be last for saving checks because preferences are changed by pressing Save Always
     */
    public void testSaveAllAfterCloseAll()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_1",diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        try{Thread.sleep(100);}catch(Exception ex){}
        DiagramOperator dr=new DiagramOperator(diagramName+"_1");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        JPopupMenuOperator pm=dr.callMenuOnTab();
        JMenuItemOperator mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //2nd
        org.netbeans.test.umllib.Utils.RetAll rt2=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName+"_2",diagramType);
        dr=new DiagramOperator(diagramName+"_2");
        dr.putElementOnDiagram(elToPlaceName,elToPlace);
        try{Thread.sleep(100);}catch(Exception ex){}
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertTrue("Save disabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        //
        final DiagramOperator drCl=dr;
        new Thread()
        {
            public void run()
            {
                drCl.closeAllDocuments();
            }
        }.start();
        //Close first Save Diagram Dialog
        JDialogOperator dlg=new JDialogOperator("Save Diagram");
        JButtonOperator btn=new JButtonOperator(dlg,"Save");
        btn.pushNoBlock();
        dlg.waitClosed();
        //Close second Save Diagram dialog
        dlg=new JDialogOperator("Save Diagram");
        btn=new JButtonOperator(dlg,"Save");
        btn.pushNoBlock();
        dlg.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        dr.waitClosed();
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_1");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
        rt2.lastDiagramNode.performPopupAction("Open");
        try{Thread.sleep(100);}catch(Exception ex){}
        dr=new DiagramOperator(diagramName+"_2");
        new DiagramElementOperator(dr,elToPlaceName,elToPlace);
        pm=dr.callMenuOnTab();
        mi=new JMenuItemOperator(pm,"Save Document");
        assertFalse("Save enabled",mi.isEnabled());
        pm.pushKey(KeyEvent.VK_ESCAPE);
        tlb=MainWindowOperator.getDefault().getToolbar("File");
        sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        assertTrue("Save all toolbar button disabled",sa.isEnabled());
    }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(childClassName,lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        if(lastDiagramNode!=null)
        {
            if(lastDiagramNode.isPresent())
            {
                lastDiagramNode.collapse();
                new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
            }
        }
        //save all
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        if(sa.isEnabled())
        {
            sa.push();
            sa.waitState(new ChooseEnabledState(false));
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        //
        DiagramOperator d=null;
        try{
            d=new DiagramOperator("testD");
        }
        catch(Exception e)
        {
        }
        //
        if(d!=null)
        {
            try{
                final DiagramOperator d2=d;
                new Thread()
                {
                    public void run()
                    {
                         d2.closeAllDocuments();
                    }
                }.start();

                d.waitClosed();
               new EventTool().waitNoEvent(1000);
            }catch(Exception ex){};
        }
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
    
    public class ChooseEnabledState implements ComponentChooser
    {
        private boolean enabled=false;
        
        ChooseEnabledState(boolean enabled)
        {
            this.enabled=enabled;
        }
        
        public boolean checkComponent(Component component) {
            return (component.isEnabled() && enabled) || (!enabled && !component.isEnabled());
        }

        public String getDescription() {
            return "choose component if it's enabled: "+enabled;
        }
        
    }
}
