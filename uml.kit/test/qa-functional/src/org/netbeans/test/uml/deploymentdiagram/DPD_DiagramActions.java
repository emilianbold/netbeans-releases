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


package org.netbeans.test.uml.deploymentdiagram;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.deploymentdiagram.utils.DPDUtils;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramOperator.ZoomCustomLevel;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.vrf.GenericVerifier;



/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class DPD_DiagramActions extends UMLTestCase {
    
    private DiagramOperator diagram = null;
    
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    
    //common test properties
    //private static String prName = "UMLProject15";
    //private static String projectName = prName+"|Model";
    private static String projectName = "UMLProjectDPD15";
    private String lastTestCase=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static long elCount=0;
    
    
    /** Need to be defined because of JUnit */
    public DPD_DiagramActions(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.deploymentdiagram.DPD_DiagramActions.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String diagramName) {
        DiagramOperator diagram = DPDUtils.openDiagram(projectName, diagramName, NewDiagramWizardOperator.DEPLOYMENT_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + diagramName + "', project '" + projectName + "'.");
        }
        return diagram;
    }
    
    
    public void testZoomInContext() {
        lastTestCase=Thread.currentThread().getStackTrace()[2].getMethodName();
        elCount++;
        String diagramName="dpdDg"+elCount;
        //
        DiagramOperator d=createOrOpenDiagram(diagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        //drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
        dE1=d.putElementOnDiagram("El_1",ElementTypes.COMPONENT,a.x,a.y);
        a=drAr.getFreePoint(150);
        dE2=d.putElementOnDiagram("El_2",ElementTypes.ARTIFACT,a.x,a.y);
        //store sizes, positions
        Rectangle old1=dE1.getBoundingRect();
        Rectangle old2=dE2.getBoundingRect();
        long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
        JComboBoxOperator zoom=new JComboBoxOperator(d);
        String oldZoom=zoom.getTextField().getText();
        assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
        
        d.zoomIn();
        //
        for(int i=0;i<10;i++) {
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
        // clean diagram and model
        new GenericVerifier(d).safeDeleteAllElements();
        //
        long change=Math.round((100.0*newd)/oldd);
        assertTrue("Distance change do not match 110%, current: "+change+" ( "+oldd+" vs "+newd+" )", change==110);
        change=Math.round(100.0*new1.width/old1.width);
        assertTrue("Width of first element do not match 110%, current: "+change,change==110);
        change=Math.round(100.0*new2.width/old2.width);
        assertTrue("Width of second element do not match 110%, current: "+change,change==110);
        change=Math.round(100.0*new1.height/old1.height);
        assertTrue("Height of first element do not match 110%, current: "+change,change==110);
        change=Math.round(100.0*new2.height/old2.height);
        assertTrue("Height of second element do not match 110%, current: "+change,change==110);
        
        
    }
    
    public void testZoomOutContext() {
        lastTestCase=Thread.currentThread().getStackTrace()[2].getMethodName();
        elCount++;
        String diagramName="dpdDg"+elCount;
        String workPkg="pkg"+elCount;
        //
        DiagramOperator d=createOrOpenDiagram(diagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
        dE1=d.putElementOnDiagram("E2_1",ElementTypes.COMPONENT,a.x,a.y);
        a=drAr.getFreePoint(150);
        dE2=d.putElementOnDiagram("E2_2",ElementTypes.ARTIFACT,a.x,a.y);
        
        //store sizes, positions
        Rectangle old1=dE1.getBoundingRect();
        Rectangle old2=dE2.getBoundingRect();
        long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
        JComboBoxOperator zoom=new JComboBoxOperator(d);
        String oldZoom=zoom.getTextField().getText();
        assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
        
        d.zoomOut();
        //
        for(int i=0;i<10;i++) {
            assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
            try{Thread.sleep(400);}catch(Exception ex){}
        }
        //
        assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "90.91%".equals(zoom.getTextField().getText()));
        dE1=new DiagramElementOperator(d,"E2_1");
        dE2=new DiagramElementOperator(d,"E2_2");
        Rectangle new1=dE1.getBoundingRect();
        Rectangle new2=dE2.getBoundingRect();
        long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
        // clean diagram and model
        new GenericVerifier(d).safeDeleteAllElements();
        //
        long change=Math.round((100.0*newd)/oldd);
        assertTrue("Distance change do not match 90-91%, current: "+change+" ( "+oldd+" vs "+newd+" )", change<=91 && change>=90);
        change=Math.round(100.0*new1.width/old1.width);
        assertTrue("Width of first element do not match 90-91%, current: "+change,change<=91 && change>=90);
        change=Math.round(100.0*new2.width/old2.width);
        assertTrue("Width of second element do not match 90-91%, current: "+change,change<=91 && change>=90);
        change=Math.round(100.0*new1.height/old1.height);
        assertTrue("Height of first element do not match 90-91%, current: "+change,change<=91 && change>=90);
        change=Math.round(100.0*new2.height/old2.height);
        assertTrue("Height of second element do not match 90-92%, current: "+change,change<=92 && change>=90);
    }
    
    public void testZoomCustomContext() {
        lastTestCase=Thread.currentThread().getStackTrace()[2].getMethodName();
        elCount++;
        String diagramName="dpdDg"+elCount;
        String workPkg="pkg"+elCount;
        //
        DiagramOperator d=createOrOpenDiagram(diagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
        dE1=d.putElementOnDiagram("E3_1",ElementTypes.COMPONENT,a.x,a.y);
        a=drAr.getFreePoint(150);
        dE2=d.putElementOnDiagram("E3_2",ElementTypes.ARTIFACT,a.x,a.y);
        //store sizes, positions
        Rectangle old1=dE1.getBoundingRect();
        Rectangle old2=dE2.getBoundingRect();
        long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
        JComboBoxOperator zoom=new JComboBoxOperator(d);
        String oldZoom=zoom.getTextField().getText();
        assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));

        drAr.getPopup().pushMenuNoBlock("Zoom...");

        d.selectZoomCustom(ZoomCustomLevel.PERCENT_200);
        //
        for(int i=0;i<10;i++) {
            assertFalse("Zoom isn't changed", oldZoom.equals(zoom.getTextField().getText()) && i==9);
            try{Thread.sleep(500);}catch(Exception ex){}
        }
        //
        assertTrue("Unexpected new zoom: "+zoom.getTextField().getText(), "200%".equals(zoom.getTextField().getText()));
        dE1=new DiagramElementOperator(d,"E3_1");
        dE2=new DiagramElementOperator(d,"E3_2");
        Rectangle new1=dE1.getBoundingRect();
        Rectangle new2=dE2.getBoundingRect();
        long newd=Math.round(Math.sqrt((new1.getCenterX()-new2.getCenterX())*(new1.getCenterX()-new2.getCenterX())+(new1.getCenterY()-new2.getCenterY())*(new1.getCenterY()-new2.getCenterY())));
        // clean diagram and model
        new GenericVerifier(d).safeDeleteAllElements();
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
    
    
    public void testSelectAllContext() {
        lastTestCase=Thread.currentThread().getStackTrace()[2].getMethodName();
        elCount++;
        String diagramName="dpdDg"+elCount;
        String workPkg="pkg"+elCount;
        //
        DiagramOperator d=createOrOpenDiagram(diagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
        dE1=d.putElementOnDiagram("E4_1",ElementTypes.COMPONENT,a.x,a.y);
        a=drAr.getFreePoint(150);
        dE2=d.putElementOnDiagram("E4_2",ElementTypes.ARTIFACT,a.x,a.y);
        //store sizes, positions
        Rectangle old1=dE1.getBoundingRect();
        Rectangle old2=dE2.getBoundingRect();
        long oldd=Math.round(Math.sqrt((old1.getCenterX()-old2.getCenterX())*(old1.getCenterX()-old2.getCenterX())+(old1.getCenterY()-old2.getCenterY())*(old1.getCenterY()-old2.getCenterY())));
        JComboBoxOperator zoom=new JComboBoxOperator(d);
        String oldZoom=zoom.getTextField().getText();
        assertTrue("Unexpected initial zoom: "+oldZoom, "100%".equals(oldZoom));
        a=drAr.getFreePoint(150);
        drAr.clickForPopup(a.x,a.y);
        new JPopupMenuOperator().pushMenu("Edit|Select All");
        //
        dE1.waitSelection(true);
        dE2.waitSelection(true);
        // clean diagram and model
        new GenericVerifier(d).safeDeleteAllElements();
    }
    public void testSelectAllShortcut() {
        lastTestCase=Thread.currentThread().getStackTrace()[2].getMethodName();
        elCount++;
        String diagramName="dpdDg"+elCount;
        String workPkg="pkg"+elCount;
        //
        DiagramOperator d=createOrOpenDiagram(diagramName);
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        new EventTool().waitNoEvent(500);
        DiagramElementOperator dE1=null,dE2=null;
        //
        dE1=d.putElementOnDiagram("E5_1",ElementTypes.COMPONENT,a.x,a.y);
        a=drAr.getFreePoint(150);
        dE2=d.putElementOnDiagram("E5_2",ElementTypes.ARTIFACT,a.x,a.y);
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
        // clean diagram and model
        new GenericVerifier(d).safeDeleteAllElements();
    }
    
///------
    
    protected void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
    }
    
    public void tearDown() throws FileNotFoundException, IOException{
        org.netbeans.test.umllib.util.Utils.tearDown();
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e1){
        }
        
        myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
        String line;
        do {
            line = myIn.readLine();
            if (line!=null && line.indexOf("Exception")!=-1){
                if ((line.indexOf("Unexpected Exception")==-1) &&
                        (line.indexOf("TimeoutExpiredException")==-1)){
                    //fail(line);
                }
            }
        } while (line != null);
    }
    
    
}
