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


package org.netbeans.test.uml.diagramtoolbarbuttons;
import java.io.*;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.diagramtoolbarbuttons.utils.DTBUtils;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author yaa
 * @spec UML/Diagram_ToolbarButtons.xml
 */
public class DiagramToolbarButtons_CPD extends UMLTestCase {
    private static String prName = "UMLProjectDTlbBtn";
    private static String cpdName = "DComponent";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    public DiagramToolbarButtons_CPD(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.diagramtoolbarbuttons.DiagramToolbarButtons_CPD.class);
        return suite;
    }

    public void testBtn_PrintPreviewDiagram(){
        String btnTooltipName = DiagramToolbarOperator.PRINT_PREVIEW_DIAGRAM_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
            new JButtonOperator(btn).pushNoBlock();
            if(!DTBUtils.findAndCloseDialog(DTBUtils.DialogTitles.PRINT_PREVIEW)){
                fail("Dialog with title '" + DTBUtils.DialogTitles.PRINT_PREVIEW + "' not found or not closed correctly");
            }
            
    }
    
/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_ExportAsImage(){
        String btnTooltipName = DiagramToolbarOperator.EXPORT_AS_IMAGE_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
            new JButtonOperator(btn).pushNoBlock();
            if(!DTBUtils.findAndCloseDialog(DTBUtils.DialogTitles.EXPORT_AS_IMAGE)){
                fail(6276927, "Dialog with title '" + DTBUtils.DialogTitles.EXPORT_AS_IMAGE + "' not found or not closed correctly");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_Select(){
        String btnTooltipName = DiagramToolbarOperator.SELECT_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_Pan(){
        String btnTooltipName = DiagramToolbarOperator.PAN_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_ZoomWithMarquee(){
        String btnTooltipName = DiagramToolbarOperator.ZOOM_WITH_MARQUEE_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_ZoomInteractively(){
        String btnTooltipName = DiagramToolbarOperator.ZOOM_INTERACTIVELY_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_NavigateLink(){
        String btnTooltipName = DiagramToolbarOperator.NAVIGATE_LINK_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_OverviewWindow(){
        String btnTooltipName = DiagramToolbarOperator.OVERVIEW_WINDOW_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
            new JButtonOperator(btn).pushNoBlock();
            if(!DTBUtils.findAndCloseDialog(DTBUtils.DialogTitles.OVERVIEW)){
                fail("Dialog with title '" + DTBUtils.DialogTitles.OVERVIEW + "' not found or not closed correctly");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_DiagramSynchronization(){
        String btnTooltipName = DiagramToolbarOperator.DIAGRAM_SYNCHRONIZATION_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/*    
    public void testBtn_ShowFriendlyNames(){
        String btnTooltipName = DiagramToolbarOperator.SHOW_FRIENDLY_NAMES_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }
*/

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_RelationshipDiscovery(){
        String btnTooltipName = DiagramToolbarOperator.RELATIONSHIP_DISCOVERY_TOOL;
             JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_FitToWindow(){
        String btnTooltipName = DiagramToolbarOperator.FIT_TO_WINDOW_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

    public void testBtn_ZoomComboBox(){
            JComboBoxOperator cmbZoom = new JComboBoxOperator(diagram, 0);
            JTextFieldOperator txt = new JTextFieldOperator(cmbZoom, 0);
            JButtonOperator btnArrow = new JButtonOperator(cmbZoom, 0);
            
            btnArrow.pushNoBlock();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
            btnArrow.pushNoBlock();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_ZoomIn(){
        String btnTooltipName = DiagramToolbarOperator.ZOOM_IN_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_ZoomOut(){
        String btnTooltipName = DiagramToolbarOperator.ZOOM_OUT_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_MoveForward(){
        String btnTooltipName = DiagramToolbarOperator.MOVE_FORWARD_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_MoveToFront(){
        String btnTooltipName = DiagramToolbarOperator.MOVE_TO_FRONT_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_MoveBackward(){
        String btnTooltipName = DiagramToolbarOperator.MOVE_BACKWARD_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_MoveToBack(){
        String btnTooltipName = DiagramToolbarOperator.MOVE_TO_BACK_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_HierarchicalLayout(){
        String btnTooltipName = DiagramToolbarOperator.HIERARCHICAL_LAYOUT_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_OrthogonalLayout(){
        String btnTooltipName = DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_SymmetricLayout(){
        String btnTooltipName = DiagramToolbarOperator.SYMMETRIC_LAYOUT_TOOL;
            JToggleButton btn = diagram.toolbar().getToggleButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_SequenceLayout(){
        String btnTooltipName = DiagramToolbarOperator.SEQUENCE_LAYOUT_TOOL;
        try
        {
            diagram.toolbar().getButtonByTooltip(btnTooltipName);
            fail("unexpected "+btnTooltipName+" on component diagram.");
        }
        catch(Exception ex)
        {
            //good
        }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testBtn_IncrementalLayout(){
        String btnTooltipName = DiagramToolbarOperator.INCREMENTAL_LAYOUT_TOOL;
            JButton btn = diagram.toolbar().getButtonByTooltip(btnTooltipName);
            if(!btn.isEnabled()){
                fail("Diagram toolbar button '" + btnTooltipName + "' is disabled but should not be");
            }
    }

/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testCBox_Zoom(){
        JComboBoxOperator cbox = new JComboBoxOperator(diagram);
        if(cbox == null){
            fail("Diagram toolbar ComboBox 'Zoom' not found");
        }else if(!cbox.isEnabled()){
            fail("Diagram toolbar ComboBox 'Zoom' is disabled but should not be");
        }
    }
    
/**
 * @caseblock Component Diagram
 * @usecase Check toolbar buttons of component diagram
 */
    public void testCheckUnnecessaryItems(){
        String[] buttonsNecessary = new String[]{
            DiagramToolbarOperator.PRINT_PREVIEW_DIAGRAM_TOOL,
            DiagramToolbarOperator.EXPORT_AS_IMAGE_TOOL,
            DiagramToolbarOperator.SELECT_TOOL,
            DiagramToolbarOperator.PAN_TOOL,
            DiagramToolbarOperator.ZOOM_WITH_MARQUEE_TOOL,
            DiagramToolbarOperator.ZOOM_INTERACTIVELY_TOOL,
            DiagramToolbarOperator.NAVIGATE_LINK_TOOL,
            DiagramToolbarOperator.OVERVIEW_WINDOW_TOOL,
            DiagramToolbarOperator.DIAGRAM_SYNCHRONIZATION_TOOL,
//            DiagramToolbarOperator.SHOW_FRIENDLY_NAMES_TOOL,
            DiagramToolbarOperator.RELATIONSHIP_DISCOVERY_TOOL,
            DiagramToolbarOperator.FIT_TO_WINDOW_TOOL,
            DiagramToolbarOperator.ZOOM_IN_TOOL,
            DiagramToolbarOperator.ZOOM_OUT_TOOL,
            DiagramToolbarOperator.MOVE_FORWARD_TOOL,
            DiagramToolbarOperator.MOVE_TO_FRONT_TOOL,
            DiagramToolbarOperator.MOVE_BACKWARD_TOOL,
            DiagramToolbarOperator.MOVE_TO_BACK_TOOL,
            DiagramToolbarOperator.HIERARCHICAL_LAYOUT_TOOL,
            DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL,
            DiagramToolbarOperator.SYMMETRIC_LAYOUT_TOOL,
            DiagramToolbarOperator.SEQUENCE_LAYOUT_TOOL,
            DiagramToolbarOperator.INCREMENTAL_LAYOUT_TOOL
        };
        
        boolean result = DTBUtils.checkUnnecessaryButtons(diagram, buttonsNecessary, getLog());
        if(!result){
            fail("Diagram toolbar contains unnecessary buttons (see log)");
        }
    }

//------------------------------------------------------------------------------
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 1000);
        JemmyProperties.setCurrentTimeout("JMenuOperator.WaitPopupTimeout", 2000);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 2000);
        JemmyProperties.setCurrentTimeout("WindowWaiter.WaitWindowTimeout", 2000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
        diagram = DTBUtils.openDiagram(prName, cpdName, NewDiagramWizardOperator.COMPONENT_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cpdName + "', project '" + prName + "'.");
        }
    }
    
    public void tearDown() throws FileNotFoundException, IOException, InterruptedException{
        org.netbeans.test.umllib.util.Utils.tearDown();
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e){}
        
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
    
    private DiagramOperator diagram = null;
}
       
