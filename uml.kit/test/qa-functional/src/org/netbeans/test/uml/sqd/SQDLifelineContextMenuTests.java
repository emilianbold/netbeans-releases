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



package org.netbeans.test.uml.sqd;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.sqd.utils.GenericContextMenuVerifier;
import org.netbeans.test.uml.sqd.utils.Util;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.customelements.SequenceDiagramOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class SQDLifelineContextMenuTests  extends UMLTestCase {
    
    public SQDLifelineContextMenuTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SQDLifelineContextMenuTests.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String PROJECT_NAME = "SQD_umlLCMT";      
    private String JAVA_PROJECT_NAME = "SQD_java";      
    private String EXCEPTION_DLG = "Exception";
    private String PKG_PATH = "Model|sqd";
    private String DIAGRAM = "NewSequenceDiagram";
    private String PATH_TO_DIAGRAM = "Model|sqd|"+DIAGRAM;
    private String DELETE_DLG = "Delete";
    private String YES_BTN = "Yes";
    private String OK_BTN = "Ok";
    private String CANCEL_BTN = "Cancel";
    private String FONT_DLG = "Font";
    private String BOLD_CHB = "Bold";
    private String ITALIC_CHB = "Italic";
    private final String PROJECT_PATH = System.getProperty("nbjunit.workdir");
    /********************************************************/
    
    
    
    Util util = new Util(PROJECT_NAME);
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;    
    private static boolean initialized = false;
    private static SequenceDiagramOperator dia = null;
    
    private String lastTestCase=null;
    
    protected void setUp() {
        eventTool.waitNoEvent(2000);                   
        if (!initialized){                 
                //util.closeStartupException();
                //associating java project
                //util.associateJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME);
                Project.openProject(this.XTEST_PROJECT_DIR+File.separator+"Project-SQD");
                org.netbeans.test.umllib.Utils.createUMLProjectFromJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME, PROJECT_PATH);
                
                
                eventTool.waitNoEvent(2000);
                                        
                //setting up environment
                util.addDiagram(DIAGRAM, PKG_PATH);
                dia = new SequenceDiagramOperator(DIAGRAM);
                
                initialized = true;
        }else{
            dia = new SequenceDiagramOperator(DIAGRAM);
        }
    }
    
   private DiagramElementOperator createWorkingElement(String lineName, String className) throws NotFoundException{ 
       DiagramElementOperator el = dia.putElementOnDiagram(lineName+":"+className, ElementTypes.LIFELINE);
       return el;
   }
   
   public void testEdit_Copy(){
        lastTestCase=getCurrentTestMethodName();;
        String lineName = "testEC";
        String className = "EC";
        final String popupPath = "Edit|Copy";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            eventTool.waitNoEvent(1500);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    eventTool.waitNoEvent(2000);                     
                    return new GenericContextMenuVerifier(el, tmpDia).verifyElement("Edit|Paste", false);
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_Copy failed. Reason unknown");
            }            
   }
   
   
   public void testEdit_Cut(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testECU";
        final String className = "ECU";
        final String popupPath = "Edit|Cut";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    long timeoutEl = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
                    JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 1000);
                    try{
                        new EventTool().waitNoEvent(1000);
                        new LifelineOperator(dia, lineName, className);
                        return false;
                    }catch(Exception e){
                        return new GenericContextMenuVerifier(el, tmpDia).verifyElement("Edit|Paste", false);
                    }finally{
                        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutEl);
                    }
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_Cut failed. Reason unknown");
            }            
    }
   
   
   
   public void testEdit_Delete(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testED";
        final String className = "ED";
        final String popupPath = "Edit|Delete";
        boolean enabled = true;                
            DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){                                      
                    long timeoutEl = JemmyProperties.getCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime");
                    JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 1000);
                    try{
                        new EventTool().waitNoEvent(1000);
                        new LifelineOperator(dia, lineName, className);
                        return false;
                    }catch(Exception e){                        
                    }finally{
                        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", timeoutEl);
                        //workaround bug 
                        eventTool.waitNoEvent(1000);
                        Point p = dia.getDrawingArea().getFreePoint(150);
                        dia.getDrawingArea().clickMouse(p.x, p.y, 1);
                        eventTool.waitNoEvent(1000);
                    }
                    return true;
                }
            };
            
            new Thread(new Runnable() {
                public void run() {
                    JDialogOperator dlg = new JDialogOperator(DELETE_DLG);
                    new JButtonOperator(dlg, YES_BTN).pushNoBlock();
                }
            }).start();  
            
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_Delete failed. Reason unknown");
            }            
    }
    
   
   
   public void testEdit_Paste(){
        lastTestCase=getCurrentTestMethodName();;
        String lineName = "testEP";
        String className = "EP";
        final String popupPath = "Edit|Paste";
        boolean enabled = false;                
            DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia);
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_Paste failed. Reason unknown");
            }            
   }
   
   
   public void testEdit_LockEdit(){
        lastTestCase=getCurrentTestMethodName();;
       //TODO: add functionality checking lock edit works fine 
       String lineName = "testELE";
        String className = "ELE";
        final String popupPath = "Edit|Lock Edit";
        boolean enabled = true;                
            DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia);
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_LockEdit failed. Reason unknown");
            }            
   }
   
   
   public void testEdit_SelectAll(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testESA";
        final String className = "ESA";
        final String popupPath = "Edit|Select All";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            final DiagramElementOperator el1 = createWorkingElement(lineName+"1", className+"1");
            dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);
            final DiagramElementOperator el2 = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT),0);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    eventTool.waitNoEvent(2000);
                    ArrayList<DiagramElementOperator> al = tmpDia.getDiagramElements();
                    for(int i=0;i<al.size();i++){
                        DiagramElementOperator tmpEl = al.get(i);
                        if (!tmpEl.isSelected()){
                            return false;
                        }
                    }
                    return true;                    
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_SelectAll failed. Reason unknown");
            }            
    }
   
   
   
   public void testEdit_SelectAllSimilar(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testESS";
        final String className = "ESS";
        final String popupPath = "Edit|Select All Similar Elements";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            final DiagramElementOperator el1 = createWorkingElement(lineName+"1", className+"1");
            dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);
            final DiagramElementOperator el2 = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT),0);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    eventTool.waitNoEvent(2000);
                    ArrayList<DiagramElementOperator> al = tmpDia.getDiagramElements();
                    String elType = el.getElementType();
                    for(int i=0;i<al.size();i++){
                        DiagramElementOperator tmpEl = al.get(i);
                        if ((el.isSelected()&&!el.getElementType().equals(elType)) || (!el.isSelected()&&el.getElementType().equals(elType))){
                            return false;
                        }
                        
                    }
                    return true;                    
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_SelectAllSimilar failed. Reason unknown");
            }            
    }
   
   
   
   
   public void testEdit_InvertSelection(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testEIS";
        final String className = "EIS";
        final String popupPath = "Edit|Invert Selection";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            final DiagramElementOperator el1 = createWorkingElement(lineName+"1", className+"1");
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    eventTool.waitNoEvent(2000);
                    if (el.isSelected()){
                        return false;
                    }
                    ArrayList<DiagramElementOperator> al = tmpDia.getDiagramElements();                    
                    String elType = el.getElementType();
                    for(int i=0;i<al.size();i++){
                        DiagramElementOperator tmpEl = al.get(i);
                        if (el.getGraphObject()!=tmpEl.getGraphObject() && !tmpEl.isSelected()){
                           return false;
                        }
                        
                    }
                    return true;                    
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_InvertSelection failed. Reason unknown");
            }            
    }
   
   
   
   public void testProperties(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testP";
        final String className = "P";
        final String popupPath = "Properties";
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    new JButtonOperator(new JDialogOperator(lineName+" - Properties"), "Close").push();
                    return true;                    
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testEdit_Properties failed. Reason unknown");
            }            
    }
   
   
   
   
   public void testLifeline_Font(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testLF";
        final String className = "LF";
        final String popupPath = "Lifeline|Font";
        final String fontFamily = "Tahoma";
        final int fontSize = 14;
        final boolean isBold = true;
        final boolean isItalic = true;
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    try{
                        //setting the font
                        JDialogOperator fontDlg = new JDialogOperator(FONT_DLG);
                        //setting font family
                        new JListOperator(fontDlg,0).selectItem(fontFamily);
                        //setting font size
                        new JListOperator(fontDlg,1).selectItem(String.valueOf(String.valueOf(fontSize)));
                        //setting bold
                        new JCheckBoxOperator(fontDlg, BOLD_CHB).changeSelection(isBold);            
                        //setting italic
                        new JCheckBoxOperator(fontDlg, ITALIC_CHB).changeSelection(isItalic);

                        eventTool.waitNoEvent(500);
                        new JButtonOperator(fontDlg, OK_BTN).push();

                        eventTool.waitNoEvent(5000);

                        //checking everything was changed correctly
                        Font font = new CompartmentOperator(el, CompartmentTypes.SEQUENCE_LIFELINE_NAME_COMPARTMENT).getFont();

                        if (font == null){
                            log("Font is null");
                            return false;
                        }

                        if (!font.getFamily().equals(fontFamily) || !(font.getSize()==fontSize) || !(font.isBold()==isBold) || !(font.isItalic()==isItalic)){
                            log("Font failed: " + font.getFamily()+ " "+font.getSize()+" "+font.isBold()+" "+font.isItalic());
                            log(el.getFont().toString());
                            return false;
                        }

                        return true;
                    }catch(Exception e){
                        e.printStackTrace(getLog());
                        return false;
                    }
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testLifeline_Font failed. Reason unknown");
            }            
    }
   
   
   
   public void testLifeline_FontColor(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testLFC";
        final String className = "LFC";
        final String popupPath = "Lifeline|Font Color";        
        final int red = 250;
        final int green = 0;
        final int blue = 0;
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    try{
                        //setting the font
                        JDialogOperator colorDlg = new JDialogOperator();
                        //setting font family
                        util.setColor(colorDlg, red, green, blue);
                        eventTool.waitNoEvent(5000);

                        //checking everything was changed correctly
                        Color color = new CompartmentOperator(el, CompartmentTypes.SEQUENCE_LIFELINE_NAME_COMPARTMENT).getFontColor();

                        if (color == null){
                            log("Color is null");
                            return false;
                        }
                        
                        if (!(color.getRed()==red) || !(color.getGreen()==green) || !(color.getBlue()==blue)){
                            log("Font color failed: (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
                            return false;
                        }
                        return true;
                    }catch(Exception e){
                        e.printStackTrace(getLog());
                        return false;
                    }
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testLifeline_Font failed. Reason unknown");
            }            
    }
   
   
   /*
   //EXCLUDED FROM RUN
   public void testLifeline_BackgroundColor(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testLBKC";
        final String className = "LBKC";
        final String popupPath = "Lifeline|Background Color";        
        final int red = 0;
        final int green = 250;
        final int blue = 0;
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    try{
                        //setting the font
                        JDialogOperator colorDlg = new JDialogOperator();
                        //setting font family
                        util.setColor(colorDlg, red, green, blue);
                        eventTool.waitNoEvent(5000);

                        //checking everything was changed correctly
                        Color color = new CompartmentOperator(el, CompartmentTypes.SEQUENCE_LIFELINE_NAME_COMPARTMENT).getBackgroundColor();
                        //Color color = el.getBorderColor();

                        if (color == null){
                            log("Color is null");
                            return false;
                        }
                        
                        if (!(color.getRed()==red) || !(color.getGreen()==green) || !(color.getBlue()==blue)){
                            log("Background color failed: (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
                            return false;
                        }
                        return true;
                    }catch(Exception e){
                        e.printStackTrace(getLog());
                        return false;
                    }
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testLifeline_BackgroundColor failed. Reason unknown");
            }            
    }
   */
   
   
   public void testLifeline_BorderColor(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testLBC";
        final String className = "LBC";
        final String popupPath = "Lifeline|Border Color";        
        final int red = 0;
        final int green = 0;
        final int blue = 250;
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    try{
                        //setting the font
                        JDialogOperator colorDlg = new JDialogOperator();
                        //setting font family
                        util.setColor(colorDlg, red, green, blue);
                        eventTool.waitNoEvent(5000);

                        //checking everything was changed correctly
                        Color color = new CompartmentOperator(el, CompartmentTypes.SEQUENCE_LIFELINE_NAME_COMPARTMENT).getBorderColor();
                        //Color color = el.getBorderColor();

                        if (color == null){
                            log("Color is null");
                            return false;
                        }
                        
                        if (!(color.getRed()==red) || !(color.getGreen()==green) || !(color.getBlue()==blue)){
                            log("Border color failed: (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");
                            return false;
                        }
                        return true;
                    }catch(Exception e){
                        e.printStackTrace(getLog());
                        return false;
                    }
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testLifeline_BorderColor failed. Reason unknown");
            }            
    }
   
   
   
   public void testAssociateWith(){
        lastTestCase=getCurrentTestMethodName();;
        final String lineName = "testAW";
        final String className = "AW";
        final String popupPath = "Associate With...";        
        boolean enabled = true;                
            final DiagramElementOperator el = createWorkingElement(lineName, className);
            eventTool.waitNoEvent(1500);
            final SequenceDiagramOperator tmpDia = new SequenceDiagramOperator(DIAGRAM);
            GenericContextMenuVerifier verifier = new GenericContextMenuVerifier(el, dia){
                protected boolean checkActionResult(){
                    JDialogOperator assDlg = new JDialogOperator("Associate");                        
                    return true;                    
                }
            };
            boolean result = verifier.verifyElement(popupPath, enabled);
            if (!result){
                fail("testAssociateWith failed. Reason unknown");
            }            
    }
   
   
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
            new JDialogOperator(EXCEPTION_DLG).close();
            if (!failedByBug){
                fail("Unexpected Exception dialog was found");
            }            
        }catch(Exception excp){
        }finally{
            org.netbeans.test.umllib.util.Utils.saveAll();
            closeAllModal();
            if (failedByBug){
                failedByBug = false;                        
            }                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
            //TODO: should be removed later
            util.closeSaveDlg();
        }        
    }
    
    
    
    
}
