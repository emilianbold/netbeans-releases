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


package org.netbeans.test.uml.tutorial.activity;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramLabelOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LibProperties;
import org.netbeans.test.umllib.util.Utils;




/**
 *
 * @author psb
 */
public class ActivityDiagram extends UMLTestCase {
    
    //some system properties
    private static final String contextPropItemName="Properties";
    private static final String umlPropertyWindowTitle="Project Properties";
    private static final String umlSourcePackagesLabel="Source Packages";
    private static final String umlSourcePackagesColumn="Folder Label";
    private static final String umlSourceUsageColumn="Model?";
    private static final String mainTreeTabName="Projects";
    //common test properties
    private static final String prName= "ActivityDiagramProject";
    private static final String project = prName+"|Model";
    private UMLProject pr=null;
    private static boolean codeSync=false;
    private static final String defaultNewElementName=LibProperties.getProperties().COMMON_NEW_ELEMENT_NAME;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private static final String workdir=System.getProperty("nbjunit.workdir");
    private static String lastTestCase=null;
    //preferred sized
    private static final int widthAreaPreferred=800;
    private static final int heightAreaPreferred=600;
    ///
    private static final String partitionName="Bank";
    private static final String subPartitionName1="Bank Lobby";
    private static final String subPartitionName2="Teller";
    private static final String activityGroupName="Customer";
    private static final String invocationName1="Approach Teller Counter";
    private static final String invocationName2="Enter Transaction";
    private static final String invocationName3="Receive Transaction Request";
    private static final String invocationName4="Search for Customer info";
    private static final String invocationName5="Send to Customer Service";
    private static final String invocationName6="Process Transaction";
    private static final String invocationName7="Update Account Info";
    private static final String invocationName8="Notify Customer";
    private static final String edgeName1="Initiate Cash Withdrawal";
    ///
    
    /** Need to be defined because of JUnit */
    public ActivityDiagram(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.tutorial.activity.ActivityDiagram.class);
        return suite;
    }
    

    public void testCreatingActivityDiagram() throws Exception {
        lastTestCase=getCurrentTestMethodName();
        //
        HashMap<Integer,String> issues=new HashMap<Integer,String>();
        int widthWindow=MainWindowOperator.getDefault().getWidth();
        //
        if(widthWindow<(widthAreaPreferred+200))fail("Unsupported resolution or failed to enlarge main window, size less than "+(widthAreaPreferred+200));
        //
        pr=UMLProject.createProject(prName,ProjectType.UML_PLATFORM_INDEPENDET_MODEL,workdir);
        org.netbeans.test.umllib.Utils.RetAll ret=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,"actpkg","acDTutorial",org.netbeans.test.umllib.NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        DiagramOperator dgr=ret.dOp;
        DrawingAreaOperator drA=dgr.getDrawingArea();
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        //
        int widthArea=drA.getWidth();
        int heighArea=drA.getHeight();
        //
        //extends work area
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("",250));
        if(widthArea<widthAreaPreferred)
        {
            int dx=(widthAreaPreferred-widthArea)/2;
            //
            {
                int w0=pto.getWidth()+4;
                driver.moveMouse(pto, w0, pto.getHeight()/2);
                new Timeout("",500).sleep();
                driver.pressMouse(pto, w0, pto.getHeight()/2, InputEvent.BUTTON1_MASK, 0);                
                new Timeout("",500).sleep();
                for(int x=w0;x>(w0-dx);x-=10)driver.moveMouse(pto, x, pto.getHeight()/2);
                new Timeout("",500).sleep();
                driver.releaseMouse(pto, w0/2, pto.getHeight()/2, InputEvent.BUTTON1_MASK, 0);
                new Timeout("",500).sleep();
            }
            //
            {
                int w0=pl.getWidth();
                driver.moveMouse(pl, -4, pl.getHeight()/2);
                new Timeout("",500).sleep();
                driver.pressMouse(pl, -4, pl.getHeight()/2, InputEvent.BUTTON1_MASK, 0);                
                new Timeout("",500).sleep();
                for(int x=-4;x<dx;x+=10)driver.moveMouse(pl, x, pl.getHeight()/2);
                new Timeout("",500).sleep();
                driver.releaseMouse(pl, w0/2, pl.getHeight()/2, InputEvent.BUTTON1_MASK, 0);
                new Timeout("",500).sleep();
            }
        }
        //
        pl.selectToolByType(ElementTypes.PARTITION);
        drA.clickMouse(40,40,1);
        DiagramElementOperator partition=new DiagramElementOperator(dgr,defaultNewElementName,ElementTypes.PARTITION,0);
        //
        java.awt.Point tmp=partition.getCenterPoint();
        drA.clickMouse(tmp.x,tmp.y,1,InputEvent.BUTTON3_MASK);
        drA.pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(500);
        //
        java.awt.Rectangle drAB=drA.getBounds();
        //
        java.awt.Rectangle parB=partition.getBoundingRect();
        //
        partition.setSize(Math.min(drAB.width,widthAreaPreferred)-2*parB.x-100,Math.min(drAB.height,heightAreaPreferred)-2*parB.y-20);
        //
        java.awt.Rectangle parB2=partition.getBoundingRect();
        assertTrue("partition wasn't enlarged", parB.width<parB2.width && parB.height<parB2.height);
        partition.getPopup().pushMenu("Partitions|Add Partition Column to the Right");
        //wait for size change of partition
        for(int i=0;i<20 && partition.getBoundingRect().getWidth()>(parB2.width+10);i++)
        {
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        //
        //refresh
        partition=new DiagramElementOperator(dgr,defaultNewElementName,ElementTypes.PARTITION,0);
        parB2=partition.getBoundingRect();
        //sizes
        int x0=parB2.x;
        int y0=parB2.y;
        int width0=parB2.width;
        int height0=parB2.height;
        //find line
        Point cntPnt=partition.getCenterPoint();
        Point topPnt=drA.getSolidEdge(new Point(x0+20,cntPnt.y),0,-1,null);
        int yTop=topPnt.y;
        Point sepPnt=drA.getSolidEdge(new Point(topPnt.x,topPnt.y+1),1,0,null);
        Point moveTo=new Point(x0+width0/3,cntPnt.y);
        //
        try{Thread.sleep(1000);}catch(Exception ex){}
        driver.moveMouse(drA,sepPnt.x,cntPnt.y);
        new Timeout("",500).sleep();
        driver.pressMouse(drA, sepPnt.x,cntPnt.y, InputEvent.BUTTON1_MASK, 0);                
        new Timeout("",500).sleep();
        //where separator will be located
        int xSep;
        for(xSep=sepPnt.x;xSep>=moveTo.x;xSep-=10)driver.moveMouse(drA, xSep,cntPnt.y-1);
        new Timeout("",500).sleep();
        driver.releaseMouse(drA, cntPnt.x,cntPnt.y-1, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
        //name all
        CompartmentOperator nmComp=null;
        try {
            nmComp=new CompartmentOperator(partition,CompartmentTypes.NAME_COMPARTMENT);
        } catch(Exception ex) {
            fail("Unable to find name compartment in class");
        }
        nmComp.clickOnCenter(2,InputEvent.BUTTON1_MASK);
        //try to find edit control
        org.netbeans.test.umllib.EditControlOperator ec=new org.netbeans.test.umllib.EditControlOperator();
        JTextFieldOperator tOp=ec.getTextFieldOperator();
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(tOp.getText()));
        //replace selected defaultNewClassName with name
        for(int i=0;i<partitionName.length();i++) {
            drA.typeKey(partitionName.charAt(i));
        }
        drA.pushKey(KeyEvent.VK_ENTER);
        try{Thread.sleep(100);}catch(Exception ex){}
        //refresh
        partition=new DiagramElementOperator(dgr,partitionName,ElementTypes.PARTITION,0);
        //name subpartitions
        //first subpartition
        partition.clickOn(new Point(x0+width0/6,topPnt.y+5),1,InputEvent.BUTTON1_MASK,0);
        try{Thread.sleep(500);}catch(Exception ex){}
        partition.clickOn(new Point(x0+width0/6,topPnt.y+5),2,InputEvent.BUTTON1_MASK,0);
        ec=new org.netbeans.test.umllib.EditControlOperator();
        tOp=ec.getTextFieldOperator();
        yTop+=tOp.getHeight();//shift work area
        int heightUseful=height0-(yTop-y0);
        //
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(tOp.getText()));
        //replace selected defaultNewClassName with name
        for(int i=0;i<subPartitionName1.length();i++) {
            drA.typeKey(subPartitionName1.charAt(i));
        }
        drA.pushKey(KeyEvent.VK_ENTER);
        try{Thread.sleep(100);}catch(Exception ex){}
        //workaround for blue background
        partition.clickOn(new Point(x0+width0/6,topPnt.y+5),2,InputEvent.BUTTON1_MASK,0);
        ec=new org.netbeans.test.umllib.EditControlOperator();
        drA.pushKey(KeyEvent.VK_ENTER);
        //end workaround
        //second subpartition
        partition.clickOn(new Point(x0+width0*2/3,topPnt.y+5),2,InputEvent.BUTTON1_MASK,0);
        ec=new org.netbeans.test.umllib.EditControlOperator();
        tOp=ec.getTextFieldOperator();
        assertTrue("Text \""+tOp.getText()+"\" in name compartment isn't correct, should be \""+defaultNewElementName+"\"",defaultNewElementName.equals(tOp.getText()));
        //replace selected defaultNewClassName with name
        for(int i=0;i<subPartitionName2.length();i++) {
            drA.typeKey(subPartitionName2.charAt(i));
        }
        drA.pushKey(KeyEvent.VK_ENTER);
        ec.waitComponentShowing(false);
        //workaround for blue background
        partition.clickOn(new Point(x0+width0*2/3,topPnt.y+5),2,InputEvent.BUTTON1_MASK,0);
        ec=new org.netbeans.test.umllib.EditControlOperator();
        drA.pushKey(KeyEvent.VK_ENTER);
        //end workaround
        //check in project tree
        Node bankNode=new Node(ret.lastDiagramNode,partitionName);
        Node subpartitionsNode=new Node(bankNode,"SubPartitions");
        Node subpartitionNode1=new Node(subpartitionsNode,subPartitionName1);
        Node subpartitionNode2=new Node(subpartitionsNode,subPartitionName2);
        Utils.makeScreenShotCustom("activity1_");
        //add activity group
        DiagramElementOperator activityGroup=dgr.putElementOnDiagram(activityGroupName,ExpandedElementTypes.ACTIVITY_GROUP,x0+60,yTop+60);
        //
        activityGroup.moveTo(x0+60,yTop+20);
        try{Thread.sleep(1000);}catch(Exception ex){}
        activityGroup=new DiagramElementOperator(dgr,activityGroupName,ElementTypes.ACTIVITY_GROUP,0);
        //
        java.awt.Rectangle actGB=activityGroup.getBoundingRect();
        //
        activityGroup.setSize(width0/3-(actGB.x-x0)-10,heightUseful+(yTop-actGB.y)-20);
        activityGroup=new DiagramElementOperator(dgr,activityGroupName,ElementTypes.ACTIVITY_GROUP,0);
        java.awt.Rectangle actGB2=activityGroup.getBoundingRect();
        assertTrue("activity group wasn't enlarged", actGB.width<actGB2.width && actGB.height<actGB2.height);
        //place two invocations
        DiagramElementOperator invocation1=dgr.putElementOnDiagram(invocationName1,ElementTypes.INVOCATION,actGB2.x+30,actGB2.y+actGB2.height/3+30);
        //stady invoc sized
        Rectangle invocationB0=invocation1.getBoundingRect();
        int invocation_shift_x=invocationB0.x-(actGB2.x+30);
        int invocation_shift_y=invocationB0.y-(actGB2.y+actGB2.height/3+30);
        int invocation_width0=invocationB0.width;
        int invocation_height0=invocationB0.height;
        //correct first invocation position
        invocation1.moveTo(actGB2.x+(actGB2.width-invocation_width0*1.2)/2,actGB2.y+140);
        invocation1.setSize(invocation_width0*1.2,invocation_height0);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation1=new DiagramElementOperator(dgr,invocationName1,ElementTypes.INVOCATION);
        invocationB0=invocation1.getBoundingRect();
        //2
        int tmpHFree=actGB2.y+actGB2.height-invocationB0.y-invocationB0.height;
        DiagramElementOperator invocation2=dgr.putElementOnDiagram(invocationName2,ElementTypes.INVOCATION,invocationB0.x-invocation_shift_x,invocationB0.y+invocation_height0+(tmpHFree-invocation_height0)/2-invocation_shift_y);
        invocation2.setSize(invocation_width0*1.2,invocation_height0);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation2=new DiagramElementOperator(dgr,invocationName2,ElementTypes.INVOCATION);
        Utils.makeScreenShotCustom("activity2_");
        //3, 2nd column
        DiagramElementOperator invocation3=dgr.putElementOnDiagram(invocationName3,ElementTypes.INVOCATION,xSep+width0/9-invocation_shift_x,yTop+20-invocation_shift_y);
        invocation3.setSize(4*width0/9,invocation_height0/2.5);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation3=new DiagramElementOperator(dgr,invocationName3,ElementTypes.INVOCATION);
        //4
        DiagramElementOperator invocation4=dgr.putElementOnDiagram(invocationName4,ElementTypes.INVOCATION,xSep+width0/9+20-invocation_shift_x,invocation3.getBoundingRect().y+20+invocation3.getBoundingRect().height-invocation_shift_y);
        invocation4.setSize(4*width0/9-40,invocation_height0/2.5);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation4=new DiagramElementOperator(dgr,invocationName4,ElementTypes.INVOCATION);
        //5
        DiagramElementOperator invocation5=dgr.putElementOnDiagram(invocationName5,ElementTypes.INVOCATION,xSep+width0/6-(int)(0.6*invocation_width0)-invocation_shift_x,invocation4.getBoundingRect().y+40+invocation4.getBoundingRect().height+20-invocation_shift_y);
        invocation5.setSize(invocation_width0*1.2,invocation_height0*.7);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation5=new DiagramElementOperator(dgr,invocationName5,ElementTypes.INVOCATION);
        //6
        DiagramElementOperator invocation6=dgr.putElementOnDiagram(invocationName6,ElementTypes.INVOCATION,xSep+width0/2-(int)(0.6*invocation_width0)-invocation_shift_x,invocation4.getBoundingRect().y+40+invocation4.getBoundingRect().height+20-invocation_shift_y);
        invocation6.setSize(invocation_width0*1.2,invocation_height0*.7);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation6=new DiagramElementOperator(dgr,invocationName6,ElementTypes.INVOCATION);
        //7
        DiagramElementOperator invocation7=dgr.putElementOnDiagram(invocationName7,ElementTypes.INVOCATION,xSep+width0/6-(int)(0.6*invocation_width0)-invocation_shift_x,invocation5.getBoundingRect().y+45+invocation5.getBoundingRect().height-invocation_shift_y);
        invocation7.setSize(invocation_width0*1.2,invocation_height0*.7);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation7=new DiagramElementOperator(dgr,invocationName7,ElementTypes.INVOCATION);
        //8
        DiagramElementOperator invocation8=dgr.putElementOnDiagram(invocationName8,ElementTypes.INVOCATION,xSep+width0/2-(int)(0.6*invocation_width0)-invocation_shift_x,invocation5.getBoundingRect().y+45+invocation5.getBoundingRect().height-invocation_shift_y);
        invocation8.setSize(invocation_width0*1.2,invocation_height0*.7);
        try{Thread.sleep(1000);}catch(Exception ex){}
        invocation8=new DiagramElementOperator(dgr,invocationName8,ElementTypes.INVOCATION);
        Utils.makeScreenShotCustom("activity3_");
        //end of invocations, start another
        DiagramElementOperator initialNode=dgr.putElementOnDiagram(null,ElementTypes.INITIAL_NODE,(x0+actGB2.x)/2,invocationB0.y+invocationB0.height/2);
        initialNode.moveTo((x0+actGB2.x)/2-initialNode.getBoundingRect().width/2,invocationB0.y+invocationB0.height/2-initialNode.getBoundingRect().height/2+4);
        //fork1
        DiagramElementOperator fork1=dgr.putElementOnDiagram(null,ElementTypes.HORIZONTAL_FORK,xSep+width0/9,invocation5.getBoundingRect().y+invocation5.getBoundingRect().height+15);
        fork1.setSize(width0*4/9,4);
        fork1.moveTo(xSep+width0/9,invocation5.getBoundingRect().y+invocation5.getBoundingRect().height+12-2);
        fork1=new DiagramElementOperator(dgr,null,ElementTypes.HORIZONTAL_FORK,0);
        //fork2
        DiagramElementOperator fork2=dgr.putElementOnDiagram(null,ElementTypes.HORIZONTAL_FORK,xSep+width0/9,invocation7.getBoundingRect().y+invocation7.getBoundingRect().height+15);
        fork2=new DiagramElementOperator(dgr,null,ElementTypes.HORIZONTAL_FORK,1);
        fork2.setSize(width0*4/9,4);
        fork2.moveTo(xSep+width0/9,invocation7.getBoundingRect().y+invocation7.getBoundingRect().height+40-2);
        fork2=new DiagramElementOperator(dgr,null,ElementTypes.HORIZONTAL_FORK,1);
        //final node
        DiagramElementOperator finalNode=dgr.putElementOnDiagram(null,ElementTypes.ACTIVITY_FINAL_NODE,xSep+width0/3,yTop+heightUseful-20);
        finalNode.moveTo(xSep+width0/3-finalNode.getBoundingRect().width/2+2,yTop+heightUseful-finalNode.getBoundingRect().height-10);
        //decision
        DiagramElementOperator decision=dgr.putElementOnDiagram(null,ElementTypes.DECISION,xSep+width0/3,(invocation4.getBoundingRect().y+invocation4.getBoundingRect().height+invocation5.getBoundingRect().y)/2);
        decision.moveTo(xSep+width0/3-decision.getBoundingRect().width/2+4,(invocation4.getBoundingRect().y+invocation4.getBoundingRect().height+invocation5.getBoundingRect().y)/2-decision.getBoundingRect().height/2+10);
        try{Thread.sleep(1000);}catch(Exception ex){}
        Utils.makeScreenShotCustom("activity4_");
        //========
        //Placing Edges and Dependencies
        //========
        //edge 1
        try {
            pl.selectToolByType(LinkTypes.ACTIVITY_EDGE);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find activity edge in paletter");
        }
        //
        drA.clickMouse(initialNode.getCenterPoint().x,initialNode.getCenterPoint().y,1);
        drA.clickMouse(invocation1.getCenterPoint().x,invocation1.getCenterPoint().y,1);
        //drA.pushKey(KeyEvent.VK_ESCAPE);
        dgr.toolbar().selectDefault();
        try{Thread.sleep(100);}catch(Exception ex){}
        LinkOperator edge1=null;
        for(int i=0;i<20;i++)
        {
            edge1=invocation1.getLinks().iterator().next();
            if(edge1!=null)break;
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        assertTrue("Can't find fiirst edge.",edge1!=null);
        drA.clickForPopup(edge1.getNearCenterPointWithoutOverlayCheck().x,edge1.getNearCenterPointWithoutOverlayCheck().y);
        JPopupMenuOperator pop=new JPopupMenuOperator();
        try{Thread.sleep(100);}catch(Exception ex){}
        pop.pushKey(KeyEvent.VK_LEFT);
        try{Thread.sleep(1000);}catch(Exception ex){}
        pop=new JPopupMenuOperator();
        pop.pushMenu("Labels|Show Name");
        ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.getTextFieldOperator().enterText(edgeName1);
        //
        DiagramLabelOperator lbl1=edge1.getLabel(edgeName1);
        lbl1.shift(-20,-10);
        //more edges
        try {
            pl.selectToolByType(LinkTypes.ACTIVITY_EDGE);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find activity edge in paletter");
        }
        //edge2
        drA.clickMouse(invocation1.getCenterPoint().x,invocation1.getCenterPoint().y,1);
        drA.clickMouse(invocation2.getCenterPoint().x,invocation2.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge3
        drA.clickMouse(invocation2.getCenterPoint().x,invocation2.getCenterPoint().y,1);
        drA.clickMouse(xSep+20,invocation2.getCenterPoint().y+2,1);
        drA.clickMouse(xSep+20,invocation3.getCenterPoint().y+2,1);
        drA.clickMouse(invocation3.getCenterPoint().x,invocation3.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge4
        drA.clickMouse(invocation3.getCenterPoint().x,invocation3.getCenterPoint().y,1);
        drA.clickMouse(invocation4.getCenterPoint().x,invocation4.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge5
        drA.clickMouse(invocation4.getCenterPoint().x,invocation4.getCenterPoint().y,1);
        drA.clickMouse(decision.getCenterPoint().x,decision.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge6
        drA.clickMouse(decision.getCenterPoint().x,decision.getCenterPoint().y,1);
        drA.clickMouse(invocation5.getCenterPoint().x+1,decision.getCenterPoint().y+3,1);
        drA.clickMouse(invocation5.getCenterPoint().x,invocation5.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge7
        drA.clickMouse(decision.getCenterPoint().x,decision.getCenterPoint().y,1);
        drA.clickMouse(invocation6.getCenterPoint().x+1,decision.getCenterPoint().y+3,1);
        drA.clickMouse(invocation6.getCenterPoint().x,invocation6.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge8
        drA.clickMouse(invocation6.getCenterPoint().x,invocation6.getCenterPoint().y,1);
        drA.clickMouse(fork1.getCenterPoint().x+3,invocation6.getCenterPoint().y+4,1);
        drA.clickMouse(fork1.getCenterPoint().x,fork1.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        Utils.makeScreenShotCustom("activity4_2");
        //edge9
        drA.clickMouse(fork1.getCenterPoint().x,fork1.getCenterPoint().y,1);
        drA.clickMouse(fork1.getCenterPoint().x,fork1.getCenterPoint().y+15,1);
        drA.clickMouse(invocation7.getCenterPoint().x+1,fork1.getCenterPoint().y+15,1);
        drA.clickMouse(invocation7.getCenterPoint().x,invocation7.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge10
        drA.clickMouse(fork1.getCenterPoint().x,fork1.getCenterPoint().y,1);
        drA.clickMouse(fork1.getCenterPoint().x,fork1.getCenterPoint().y+15,1);
        drA.clickMouse(invocation8.getCenterPoint().x+1,fork1.getCenterPoint().y+15,1);
        drA.clickMouse(invocation8.getCenterPoint().x,invocation8.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge11
        drA.clickMouse(invocation8.getCenterPoint().x,invocation8.getCenterPoint().y,1);
        drA.clickMouse(invocation8.getCenterPoint().x+2,fork2.getCenterPoint().y-20,1);
        drA.clickMouse(fork2.getCenterPoint().x,fork2.getCenterPoint().y-20,1);
        drA.clickMouse(fork2.getCenterPoint().x,fork2.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge12
        drA.clickMouse(invocation7.getCenterPoint().x,invocation7.getCenterPoint().y,1);
        drA.clickMouse(invocation7.getCenterPoint().x+2,fork2.getCenterPoint().y-20,1);
        drA.clickMouse(fork2.getCenterPoint().x,fork2.getCenterPoint().y-20,1);
        drA.clickMouse(fork2.getCenterPoint().x,fork2.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge13
        drA.clickMouse(fork2.getCenterPoint().x,fork2.getCenterPoint().y,1);
        drA.clickMouse(finalNode.getCenterPoint().x,finalNode.getCenterPoint().y,1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //edge 6 condition
//        drA.pushKey(KeyEvent.VK_ESCAPE);
        dgr.toolbar().selectDefault();
        try{Thread.sleep(100);}catch(Exception ex){}
        LinkOperator edge6=null;
        for(int i=0;i<20;i++)
        {
            edge6=invocation5.getLinks().iterator().next();
            if(edge6!=null)break;
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        assertTrue("Can't find 5th edge.",edge6!=null);
        drA.clickForPopup(edge6.getNearCenterPointWithoutOverlayCheck().x,edge6.getNearCenterPointWithoutOverlayCheck().y);
        pop=new JPopupMenuOperator();
        try{Thread.sleep(100);}catch(Exception ex){}
        pop.pushKey(KeyEvent.VK_LEFT);
        try{Thread.sleep(1000);}catch(Exception ex){}
        pop=new JPopupMenuOperator();
        pop.pushMenu("Labels|Show Guard Condition");
        ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.getTextFieldOperator().typeText("No Customer Info");
        ec.pushKey(KeyEvent.VK_ENTER);
        //edge 7condition
        LinkOperator edge7=null;
        for(int i=0;i<20;i++)
        {
            edge7=invocation6.getInLinks().iterator().next();
            if(edge7!=null)break;
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        assertTrue("Can't find 5th edge.",edge7!=null);
        drA.clickForPopup(edge7.getNearCenterPointWithoutOverlayCheck().x,edge7.getNearCenterPointWithoutOverlayCheck().y);
        pop=new JPopupMenuOperator();
        try{Thread.sleep(100);}catch(Exception ex){}
        pop.pushKey(KeyEvent.VK_LEFT);
        try{Thread.sleep(1000);}catch(Exception ex){}
        pop=new JPopupMenuOperator();
        pop.pushMenu("Labels|Show Guard Condition");
        ec=new org.netbeans.test.umllib.EditControlOperator();
        ec.getTextFieldOperator().typeText("Customer Info");
        ec.pushKey(KeyEvent.VK_ENTER);
        Utils.makeScreenShotCustom("activity4_3");
        //
        activityGroup.clickOn(new Point(actGB2.x+10,actGB2.y+10),1,InputEvent.BUTTON1_MASK,0);
        activityGroup.waitSelection(true);
        //
        PropertySheetOperator prS=new PropertySheetOperator(activityGroupName+" - Properties");
        Property prGK=new Property(prS,"GroupKind");
        assertTrue("Group kind is not iteration",prGK.getValue().equals("iteration"));
        prGK.setValue(1);
        try{Thread.sleep(100);}catch(Exception ex){}
        assertTrue("Group kind is not structured",prGK.getValue().equals("structured"));
        prGK.setValue(2);
        try{Thread.sleep(100);}catch(Exception ex){}
        assertTrue("Group kind is not interruptible",prGK.getValue().equals("interruptible"));
        //
        prGK.setValue(1);
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        //CompartmentOperator stC=new CompartmentOperator(activityGroup,0);
        //assertTrue("Compartment :"+stC.getName()+"::"+stC.getSource(),"<<structured>>".equals(stC.getName()));
        //
        dgr.toolbar().selectTool(DiagramToolbarOperator.FIT_TO_WINDOW_TOOL);

   }
 

 
      
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
         pto = ProjectsTabOperator.invoke();
   }
    
    public void tearDown() {
        Utils.makeScreenShot(lastTestCase);
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        long tmp2=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000); 
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 1000);
        try{
            DiagramOperator d=new DiagramOperator("acD");
            DrawingAreaOperator drAr=d.getDrawingArea();
            drAr.pushKey(KeyEvent.VK_ESCAPE);
           new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"No").push();
                }
            }).start();
            d.closeAllDocuments();
            try{Thread.sleep(500);}catch(Exception ex){}
        }catch(Exception ex){};
        closeAllModal();
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", tmp2);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp); 
        //save
        new Thread(new Runnable() {
            public void run() {
                long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 9000); 
                try
                {
                    new JButtonOperator(new JDialogOperator("Save"), "Save All").push();
                }
                catch(Exception ex)
                {
                }
                finally
                {
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp); 
                }
            }
        }).start();
   }
    
  //
   public static void setTextProperty(String pName,String pValue)
   {
        PropertySheetOperator ps=new PropertySheetOperator();
        Property nmProp=new Property(ps,pName);
        double nmPntX=ps.tblSheet().getCellRect(nmProp.getRow(),1,false).getCenterX();
        double nmPntY=ps.tblSheet().getCellRect(nmProp.getRow(),1,false).getCenterY();
        ps.clickMouse((int)nmPntX,(int)nmPntY,1);
        for(int i=0;i<pValue.length();i++)ps.typeKey(pValue.charAt(i));
        ps.pushKey(KeyEvent.VK_ENTER);
    }

}
