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


package org.netbeans.test.uml.robustness;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.test.uml.robustness.utils.RUtils;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.UMLWidgetOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.vrf.GenericVerifier;

/**
 *
 * @author yaa
 * @spec uml/UMLRobustness.xml
 */
public class RobustnessOther extends UMLTestCase {
    private static String prName = "UMLProject4";
    private static String cldName1 = "DClass1";
    private static String cldName2 = "DClass2";
    private static String cldName3 = "DClass3";
    private static String cldName4 = "DClass4";
    private static String className1 = "ClassA";
    private static String className2 = "ClassB";
    private static String interfaceName1 = "InterfaceA";
    private static String interfaceName2 = "InterfaceB";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    private boolean testIsFailed = false;
    /** Need to be defined because of JUnit */
    public RobustnessOther(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.robustness.RobustnessOther.class);
        return suite;
    }
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
        testIsFailed = false;
    }
    
    public void tearDown() throws FileNotFoundException, IOException, InterruptedException{
        closeAllModal();
        try{
            DiagramOperator diagram=new DiagramOperator("D");
            new GenericVerifier(diagram).safeDeleteAllElements();
        }catch(Exception ex){};
        
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            if(!testIsFailed){fail(" " + str);}
        }catch(TimeoutExpiredException e){}
        
        myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
        String line;
        do {
            line = myIn.readLine();
            if (line!=null && line.indexOf("Exception")!=-1){
                if ((line.indexOf("Unexpected Exception")==-1) &&
                        (line.indexOf("TimeoutExpiredException")==-1)){
                    if(!testIsFailed){
                        //fail(line);
                    }
                }
            }
        } while (line != null);
        org.netbeans.test.umllib.util.Utils.tearDown();
        
    }
    
     public void testCLD_DeleteWhileResizeClass() throws AWTException, InterruptedException{
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName1, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        DiagramElementOperator e1 = diagram.putElementOnDiagram(className1, ElementTypes.CLASS);
        Widget elementGraphObject = e1.getGraphObject();
        UMLWidgetOperator wo = new UMLWidgetOperator(elementGraphObject);
        Rectangle rect = wo.getRectangle();
        //TODO: uncomment it once implement getBottomRight()
        Point point = wo.getBottomRight();
        e1.select();
        diagram.getDrawingArea().moveMouse(point.x+4, point.y+4);
        Robot robot = new Robot();
        robot.mousePress(InputEvent.BUTTON1_MASK);
        diagram.getDrawingArea().moveMouse(point.x+20, point.y+20);
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        
        JDialogOperator dlg = new JDialogOperator("Delete");
        JButtonOperator btn = new JButtonOperator(dlg, "Yes");
        btn.pushNoBlock();
        
        Point p = diagram.getDrawingArea().getFreePoint();
        diagram.getDrawingArea().moveMouse(p.x, p.y);
        
        Thread.sleep(1000);
        
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        
        Thread.sleep(2000);
        
        try {
            myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("Exception")!=-1){
                    if ((line.indexOf("Unexpected Exception")==-1) &&
                            (line.indexOf("TimeoutExpiredException")==-1)){
                        testIsFailed = true;
                        fail(87979, line);
                    }
                }
            } while (line != null);
        }catch(Exception e){
            fail("Unexpected exception: " + e.getMessage());
        }
        
    }
    
    public void testCLD_DeleteWhileMoveInterface() throws AWTException{
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName2, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        DiagramElementOperator e1 = diagram.putElementOnDiagram(interfaceName1, ElementTypes.INTERFACE);
        Point pcenter = e1.getCenterPoint();
        diagram.getDrawingArea().moveMouse(pcenter.x, pcenter.y);
        Robot robot = new Robot();
        robot.mousePress(InputEvent.BUTTON1_MASK);
        diagram.getDrawingArea().moveMouse(pcenter.x + 20, pcenter.y + 20);
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        
        try{
            JDialogOperator dlg = new JDialogOperator("Delete");
            fail("element deletion should not be allowed during dragging element");
            //JButtonOperator btn = new JButtonOperator(dlg, "Yes");
            //btn.pushNoBlock();
        } catch (TimeoutExpiredException tex){
            // this exception is expected
        }
    }
    
    public void testCLD_CloseCLDWhileDrawingImplementation() throws AWTException{
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName3, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        
        DiagramElementOperator e1 = diagram.putElementOnDiagram(interfaceName2, ElementTypes.INTERFACE);
        new UMLPaletteOperator().selectToolByType(LinkTypes.IMPLEMENTATION);
        e1.clickOnCenter();
        
        Point p = diagram.getDrawingArea().getFreePoint();
        diagram.getDrawingArea().moveMouse(p.x, p.y);
        
        new Thread(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    try { Thread.sleep(1000); } catch (Exception e){}
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
                    JDialogOperator saveDlg = new JDialogOperator(LabelsAndTitles.SAVE_DIAGRAM_CHANGES);
                    new JButtonOperator(saveDlg, "Save").pushNoBlock();
                } finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
                }
            }
        }).start();
        
        diagram.close();
        
        try{
            Thread.sleep(2000);
        }catch(Exception e){}
        
        DiagramOperator diagram1 = RUtils.openDiagram(prName, cldName3, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram1 == null){
            fail("Can't reopen diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        
    }
    
    public void testCLD_DeleteAllWhileDrawingGeneralization() throws AWTException{
        DiagramOperator diagram = RUtils.openDiagram(prName, cldName4, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName1 + "', project '" + prName + "'.");
        }
        
        DiagramElementOperator e1 = diagram.putElementOnDiagram(className2, ElementTypes.CLASS);
        new UMLPaletteOperator().selectToolByType(LinkTypes.GENERALIZATION);
        e1.clickOnCenter();
        
        Point p = diagram.getDrawingArea().getFreePoint();
        diagram.getDrawingArea().moveMouse(p.x, p.y);
        
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
        
        JDialogOperator dlg = new JDialogOperator("Delete");
        JButtonOperator btn = new JButtonOperator(dlg, "Yes");
        btn.pushNoBlock();
        
        diagram.toolbar().selectDefault();
        
    }
}

