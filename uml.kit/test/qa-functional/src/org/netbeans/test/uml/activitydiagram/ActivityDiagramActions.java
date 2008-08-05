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

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import org.netbeans.test.umllib.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class ActivityDiagramActions extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectDA";
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
    public ActivityDiagramActions(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramActions.class);
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
   

    public void testZoomInContext() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          a=drAr.getFreePoint(150);
          drAr.getPopup().pushMenu("Zoom In");
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "110%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match approximately 110% (+-1%), current: "+change+" ( "+oldd+" vs "+newd+" )", change>=109 && change<=111);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match approximately 110%, current: "+change,change>=108 && change<=111);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match approximately 110%, current: "+change,change>=109 && change<=111);
   }
 
   public void testZoomOutContext() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          a=drAr.getFreePoint(150);
          drAr.getPopup().pushMenu("Zoom Out");
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "90.91%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match 90-92%, current: "+change+" ( "+oldd+" vs "+newd+" )", change<=92 && change>=90);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
    }
    public void testZoomInToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.toolbar().selectTool(DiagramToolbarOperator.ZOOM_IN_TOOL);
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "110%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match approximately 110% (+-1%), current: "+change+" ( "+oldd+" vs "+newd+" )", change>=109 && change<=111);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match approximately 110%, current: "+change,change>=108 && change<=111);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match approximately 110%, current: "+change,change>=109 && change<=111);
   }
 
   public void testZoomOutToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.toolbar().selectTool(DiagramToolbarOperator.ZOOM_OUT_TOOL);
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "90.91%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match 90-92%, current: "+change+" ( "+oldd+" vs "+newd+" )", change<=92 && change>=90);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
    }
    public void testZoomInPlus() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          try{Thread.sleep(100);}catch(Exception ex){}
          java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            throw new UMLCommonException("Can't create awt robot object");
        }
          rbt.setAutoDelay(200);
          rbt.keyPress(KeyEvent.VK_ADD);
          rbt.keyRelease(KeyEvent.VK_ADD);
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "110%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match approximately 110% (+-1%), current: "+change+" ( "+oldd+" vs "+newd+" )", change>=109 && change<=111);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match approximately 110%, current: "+change,change>=108 && change<=111);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match approximately 110%, current: "+change,change>=109 && change<=111);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match approximately 110%, current: "+change,change>=109 && change<=111);
   }
 
   public void testZoomOutMinus() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.pushKey(KeyEvent.VK_MINUS);
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "90.91%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match 90-92%, current: "+change+" ( "+oldd+" vs "+newd+" )", change<=92 && change>=90);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match 90-93%, current: "+change,change<=93 && change>=90);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match 90-93%, current: "+change,change<=93 && change>=90);
    }

    public void testZoomCustomContext() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          a=drAr.getFreePoint(150);
          drAr.getPopup().pushMenuNoBlock("Zoom...");
          JDialogOperator zmdlg=new JDialogOperator("Zoom");
          JComboBoxOperator zmindlg=new JComboBoxOperator(zmdlg);
          zmindlg.getTextField().enterText("200");
          try{Thread.sleep(100);}catch(Exception ex){}
          //org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("zoomDialog");
          //new JButtonOperator(zmdlg,"OK").push();
          zmdlg.waitClosed();
          try{Thread.sleep(100);}catch(Exception ex){}
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "200%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match 200%, current: "+change+" ( "+oldd+" vs "+newd+" )", change==200);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match 200%, current: "+change,change==200);
   }
 
    public void testZoomCustomToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));

          zoom.enterText("200%");
          
          try{Thread.sleep(100);}catch(Exception ex){}
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "200%".equals(zoom.getTextField().getText()));
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
          //
          long change=Math.round((100.0*newd)/oldd);
          assertTrue("Distance change do not match 200%, current: "+change+" ( "+oldd+" vs "+newd+" )", change==200);
          change=Math.round(100.0*new1.width/old1.width);
          assertTrue("Width of first element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new2.width/old2.width);
          assertTrue("Width of second element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new1.height/old1.height);
          assertTrue("Height of first element do not match 200%, current: "+change,change==200);
          change=Math.round(100.0*new2.height/old2.height);
          assertTrue("Height of second element do not match 200%, current: "+change,change==200);
   }
    
    public void testZoomWithMarqueToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.toolbar().selectTool(DiagramToolbarOperator.ZOOM_WITH_MARQUEE_TOOL);
          //
          java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            throw new UMLCommonException("Can't create awt robot object");
        }
          rbt.setAutoDelay(200);
          rbt.mouseMove(d.getDrawingArea().getLocationOnScreen().x+old1.x,d.getDrawingArea().getLocationOnScreen().y+old1.y);
          rbt.mousePress(MouseEvent.BUTTON1_MASK);
          rbt.mouseMove(d.getDrawingArea().getLocationOnScreen().x+old1.x+old1.width,d.getDrawingArea().getLocationOnScreen().y+old1.y+old1.height);
          rbt.mouseRelease(MouseEvent.BUTTON1_MASK);
          try{}catch(Exception ex){}
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          double newzoom=Double.parseDouble(zoom.getTextField().getText().substring(0,zoom.getTextField().getText().length()-1));
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), newzoom>100);
          dE1=new DiagramElementOperator(d,"El_1");
          Rectangle new1=dE1.getBoundingRect();
          //
          assertTrue("Position of first element should be center to center in diagram area now: "+new1,Math.abs(new1.x+new1.width/2-drAr.getWidth()/2)<50 && Math.abs(new1.y+new1.height/2-drAr.getHeight()/2)<50);
          //
          assertTrue("Width or height of first element should be close to diagram area width or zoom should be 400","400%".equals(zoom.getTextField().getText()) || Math.abs(new1.width-drAr.getWidth())<50 || Math.abs(new1.height-drAr.getHeight())<50);
    }
    public void testZoomInteractivelyToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.toolbar().selectTool(DiagramToolbarOperator.ZOOM_INTERACTIVELY_TOOL);
          //
          java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            throw new UMLCommonException("Can't create awt robot object");
        }
          rbt.setAutoDelay(200);
          rbt.mouseMove(d.getDrawingArea().getLocationOnScreen().x+old1.x,d.getDrawingArea().getLocationOnScreen().y+old1.y);
          rbt.mousePress(MouseEvent.BUTTON1_MASK);
          rbt.mouseMove(d.getDrawingArea().getLocationOnScreen().x+old1.x,d.getDrawingArea().getLocationOnScreen().y+old1.y+50);
          rbt.mouseRelease(MouseEvent.BUTTON1_MASK);
          try{}catch(Exception ex){}
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          double newzoom=Double.parseDouble(zoom.getTextField().getText().substring(0,zoom.getTextField().getText().length()-1));
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), newzoom>100);
          dE1=new DiagramElementOperator(d,"El_1");
          Rectangle new1=dE1.getBoundingRect();
          dE2=new DiagramElementOperator(d,"El_1");
          Rectangle new2=dE2.getBoundingRect();
          //
          assertTrue("Width of first element do not match new zoom",Math.abs(new1.width-old1.width*newzoom/100)<20);
    }
    public void testFitToWindowToolbar() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          d.toolbar().selectTool(DiagramToolbarOperator.FIT_TO_WINDOW_TOOL);
          //
          for(int i=0;i<10;i++)
          {
              assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
              try{Thread.sleep(400);}catch(Exception ex){}
          }
          //
          double newzoom=Double.parseDouble(zoom.getTextField().getText().substring(0,zoom.getTextField().getText().length()-1));
          assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), newzoom>100);
          dE1=new DiagramElementOperator(d,"El_1");
          dE2=new DiagramElementOperator(d,"El_2");
          Rectangle new1=dE1.getBoundingRect();
          Rectangle new2=dE2.getBoundingRect();
          //suppose first element above/and or left to second
          assertTrue("Position of first element should be close to border "+new1,Math.abs(new1.x)<60 || Math.abs(new1.y)<60);
          assertTrue("Position of second element should be close to border "+new2,Math.abs(new2.x+new2.width-drAr.getWidth())<60 || Math.abs(new2.y+new2.height-drAr.getHeight())<60);
    }
      
   public void testSelectAllContext() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
          a=drAr.getFreePoint(150);
          drAr.getPopup().pushMenu("Edit|Select All");
          //
          dE1.waitSelection(true);
          dE2.waitSelection(true);
    }
   public void testSelectAllShortcut() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
                dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
               a=drAr.getFreePoint(150);
               dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
          //store sizes, positions
          Rectangle old1=dE1.getBoundingRect();
          Rectangle old2=dE2.getBoundingRect();
          long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
          JComboBoxOperator zoom=new JComboBoxOperator(d);
          String oldZoom=zoom.getTextField().getText();
          a=drAr.getFreePoint(150);
          d.pushKey(KeyEvent.VK_A,KeyEvent.CTRL_MASK);
          //
          dE1.waitSelection(true);
          dE2.waitSelection(true);
    }
   public void testSelectSimilarContext() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null,dE3=null;
        //
            dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE3=d.putElementOnDiagram("El_3",ElementTypes.INVOCATION,a.x,a.y);
          //
          dE3.getPopup().pushMenu("Edit|Select All Similar Elements");
          //
          dE1.waitSelection(true);
          dE3.waitSelection(true);
          try{Thread.sleep(100);}catch(Exception ex){}
          dE2.waitSelection(false);
    }
   public void testSelectSimilarShortcut() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null,dE3=null;
        //
            dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE3=d.putElementOnDiagram("El_3",ElementTypes.INVOCATION,a.x,a.y);
          //
          dE3.select();
          drAr.pushKey(KeyEvent.VK_A,KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK);
          //
          try
          {
            dE1.waitSelection(true);
          }
          catch(UMLCommonException ex)
          {
              fail(103639,"Strl-shift-a doesn't select similar elements");
          }
          dE3.waitSelection(true);
          try{Thread.sleep(100);}catch(Exception ex){}
          dE2.waitSelection(false);
    }
   public void testSelectWithDragOver() {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String activityDiagramName="ad"+elCount;
        String workPkg="pkg"+elCount;
       //
        DiagramOperator d=createOrOpenDiagram(project,workPkg, activityDiagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null,dE3=null;
        //
            dE1=d.putElementOnDiagram("El_1",ElementTypes.INVOCATION,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE2=d.putElementOnDiagram("El_2",ElementTypes.SIGNAL,a.x,a.y);
           a=drAr.getFreePoint(150);
           dE3=d.putElementOnDiagram("El_3",ElementTypes.INVOCATION,a.x,a.y);
           //
          Rectangle old1=dE1.getBoundingRect();
          try{Thread.sleep(5000);}catch(Exception ex){}
          java.awt.Robot rbt=null;
            try {
                rbt=new java.awt.Robot();
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
          rbt.setAutoDelay(200);
          rbt.mouseMove(drAr.getLocationOnScreen().x+old1.x+old1.width+30,drAr.getLocationOnScreen().y+old1.y+old1.height+30);
          rbt.mousePress(InputEvent.BUTTON1_MASK);
          for(int part=10;part>=0;part--)rbt.mouseMove((int)(drAr.getLocationOnScreen().x+old1.x+(old1.width+40)*part/10.0-10),(int)(drAr.getLocationOnScreen().y+old1.y+old1.height/2+(old1.height/2+30)*part/10.0));//do not cover fully with drag
          rbt.mouseRelease(InputEvent.BUTTON1_MASK);
          //
          dE1.waitSelection(true);
          try{Thread.sleep(100);}catch(Exception ex){}
          dE2.waitSelection(false);
          dE3.waitSelection(false);
          //
          drAr.pushKey(KeyEvent.VK_F2);//veriify focus
          assertTrue("Editcontrol do not show correct value, should El_1, now: "+new EditControlOperator().getTextAreaOperator().getText(),"El_1".equals(new EditControlOperator().getTextAreaOperator().getText()));
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
