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


package org.netbeans.test.uml.tutorial.webreport;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.project.ProjectType;
import org.netbeans.test.umllib.project.UMLProject;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LibProperties;
import org.netbeans.test.umllib.util.OptionsOperator;
import org.netbeans.test.umllib.util.Utils;



/**
 *
 * @author psb
 */
public class WebReport extends UMLTestCase {
    
    private static String prFolder="Projects-Tutorials"+File.separator+"Project-ReportingTutorial";
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "UMLTutorialProject";
    private static String project = prName+"|Model";
    private static boolean codeSync=false;
    private static String defaultNewElementName=LibProperties.getProperties().COMMON_NEW_ELEMENT_NAME;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static String lastTestCase=null;
    private static boolean setup_completed=false;
    private    Dimension scrSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();

    
    /** Need to be defined because of JUnit */
    public WebReport(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.tutorial.webreport.WebReport.class);
        return suite;
    }
    
  
    

    public void testCreatingWebReport() {
        lastTestCase=getCurrentTestMethodName();
        UMLProject pr=new UMLProject(prName,ProjectType.UML_JAVA_PLATFORM_MODEL);
        pr.getProjectNode().callPopup().pushMenu("Generate Model Report");
        try{
            //avoid concurrent
            Thread.sleep(15000);
        }
        catch(Exception ex)
        {
            
        }
        //eventTool.waitNoEvent(1500);
        JComponent top=null;
        for(int i=0;i<600;i++)
        {
            top=TopComponentOperator.findTopComponent("Output - UML Report Log",0);
            if(top!=null)break;
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        if(top==null)throw new UMLCommonException("Can't find Output - UML Report Log in 60 seconds");
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("1_",true);
        //
        //TopComponentOperator repOut=new TopComponentOperator("Output - UML Report Log");
        JComponentOperator repOut=new JComponentOperator(top);
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("2_",true);
        JEditorPaneOperator ed=new JEditorPaneOperator(repOut);
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("3_",true);
        ed.waitText("generating summary files");
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("4_",true);
        ed.waitText("Report Successful");
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom("5_",true);
    }
 


 
      
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
         pto = ProjectsTabOperator.invoke();
        if(!setup_completed)
        {
             Project.openProject(this.XTEST_PROJECT_DIR+File.separator+prFolder);
             OptionsOperator op=OptionsOperator.invoke();
             op.addWebBrowser("Java Browser", "java -jar \""+this.XTEST_PROJECT_DIR+File.separator+"Soft"+File.separator+"JavaBrowser.jar\" ");
             JLabelOperator webbrlbl=new JLabelOperator(op,"Web Browser");
             JComboBoxOperator webbrrcmb=new JComboBoxOperator((JComboBox)(webbrlbl.getLabelFor()));
             webbrrcmb.selectItem("Java Browser");
             op.close();
             setup_completed=true;
         }
   }
    
    public void tearDown() {
        Utils.makeScreenShot(lastTestCase,true);
        //start browser search
        /*java.awt.Robot rbt=null;
        try
        {
            rbt=new Robot();
        }
        catch(AWTException ex)
        {
            fail("Can't initialize robot, browser may remain open and affect later tests!");
        }
        //scan screen #EEEEFF line, #CCCCFF line and #eeeeFF line again
        int x0=-1,y0=-1;
        out: for(int x=0;x<scrSize.width;x++)
        {
            int passinrow=0;
            for(int y=0;y<scrSize.height;y++)
            {
                Color clr=rbt.getPixelColor(x,y);
                if(clr.getRed()==0xee && clr.getGreen()==0xee && clr.getBlue()==0xff &&  (passinrow==0 || passinrow==2))
                {
                    passinrow++;
                    if(passinrow==3)
                    {
                        x0=x;
                        y0=y;
                        break out;
                    }
                    y+=20;//small jump
                }
                else if(clr.getRed()==0xcc && clr.getGreen()==0xcc && clr.getBlue()==0xff && passinrow==1)
                {
                    passinrow++;
                    y+=20;
                }

            }
        }
        if(x0==-1 || y0==-1)
        {
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(lastTestCase,"CantFindBrowser",true);
            fail("can't find browser,browser may remain open and affect later tests!");
        }
        rbt.setAutoDelay(100);
        rbt.mouseMove(x0,y0);
        rbt.mousePress(InputEvent.BUTTON1_MASK);
        rbt.mouseRelease(InputEvent.BUTTON1_MASK);
        rbt.keyPress(KeyEvent.VK_ALT);
        rbt.keyPress(KeyEvent.VK_F4);
        rbt.keyRelease(KeyEvent.VK_F4);
        rbt.keyRelease(KeyEvent.VK_ALT);
        try{Thread.sleep(100);}catch(Exception ex){}
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(lastTestCase,"AfterCloseBrowser");*/
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
            java.awt.Point a=drAr.getFreePoint();
            drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
            drAr.pushKey(KeyEvent.VK_ESCAPE);
           new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"No").push();
                }
            }).start();
            d.closeAllDocuments();
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
    
}
