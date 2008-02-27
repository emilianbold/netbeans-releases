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

package gui.action;

import gui.VWPUtilities;
import java.awt.Component;
import javax.swing.JButton;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CSSRuleAddTest  extends org.netbeans.performance.test.utilities.PerformanceTestCase {

    private String fileName;
    private String projectName = "VisualWebProject";    
    private String styledocfolder = VWPUtilities.WEB_PAGES+"|"+"resources"; // NOI18N
    //private String styledocfolder = "Web Pages"+"|"+"resources"; // NOI18N
    
    /** Node to be opened/edited */
    private static Node openNode ;       
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");    
    //protected static String OPEN = "Open";
    
    private TopComponentOperator cssEditor = null;
    private NbDialogOperator createRuleDialog = null;
    
    public CSSRuleAddTest(String testName)
    {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=4000;        
    }
    
    public CSSRuleAddTest(String testName, String performanceDataName)
    {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=4000;        
    }
    @Override
    public void initialize() {
        log("::initialize");
        fileName = "stylesheet.css";  //NO18N  
        EditorOperator.closeDiscardAll();        
        
    }
    public void prepare() {
        Node projectRoot = null;        
        projectRoot = new ProjectsTabOperator().getProjectRootNode(projectName);
        projectRoot.select();
        openNode = new Node(projectRoot,styledocfolder+"|"+fileName);
        openNode.performPopupActionNoBlock(OPEN);
        cssEditor = findCSSEditor();
        if(cssEditor != null) {
            log("css editor found");
        }
        
    }
    
    private void invokeAddRuleEditor() {
        JButtonOperator addRuleButton;
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 90000);
        {
            addRuleButton = new JButtonOperator(cssEditor, new ComponentChooser() {

            public boolean checkComponent(Component component) {
                log("looking for component: "+component.toString());
                if((((JButton)component).getToolTipText() != null)) {
                    if( ((JButton)component).getToolTipText().equals("Create Rule") ) {
                        return true;
                    }
                    else return false;
                } else return false;
            }

            public String getDescription() {
                return "Add CSS Rule button";
            }
        });
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
        log("add Rule Button obtained");
        addRuleButton.pushNoBlock();      
    }
    
    public ComponentOperator open() {
        log("::open");
        
        invokeAddRuleEditor();
        createRuleDialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.editor.Bundle", "STYLE_RULE_EDITOR_TITLE"));
        processAddRule();
        changeRuleValueOne();
        changeRuleValueTwo();
        return cssEditor;
    }
    
    @Override
    public void close() {
        log("::close");
        EditorOperator.closeDiscardAll();
    }
    
    @Override
    protected void shutdown() {
        log("::shutdown");
        EditorOperator.closeDiscardAll();         
    }

    private TopComponentOperator findCSSEditor() {
        TopComponentOperator cssEditorToFind; 
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout",120000);
          cssEditorToFind = new TopComponentOperator(fileName);
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
        return cssEditorToFind; 
    }
    private TopComponentOperator findStyleBuilder() {
        return new TopComponentOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.visual.ui.Bundle", "CTL_CSSStyleBuilderTopComponent"));
    }
    private void processAddRule()
    {
        JButtonOperator addButton = new JButtonOperator(createRuleDialog,">");
        
        JTextComponentOperator textb = new JTextComponentOperator(createRuleDialog,1);
        textb.typeText("ZZZ");
        addButton.pushNoBlock();
        createRuleDialog.ok();
        createRuleDialog.waitClosed();
    }
    private void changeRuleValueOne() {
        TopComponentOperator builder = findStyleBuilder();
        JTabbedPaneOperator styleParts = new JTabbedPaneOperator(builder,0);
        styleParts.selectPage("Background");
        JComboBoxOperator bkColorCombo = new JComboBoxOperator(styleParts,BACKCOLOR_COMBO_INDEX);
        bkColorCombo.selectItem("teal");
        
    }
    private void changeRuleValueTwo() {
        TopComponentOperator builder = findStyleBuilder();
        JTabbedPaneOperator styleParts = new JTabbedPaneOperator(builder,0);
        styleParts.selectPage("Position");
        JComboBoxOperator modeCombo = new JComboBoxOperator(styleParts,POSITION_COMBO_INDEX);
        modeCombo.selectItem("relative");
        System.out.println(modeCombo.getSelectedIndex());
        
        
    }    
    private void OpenPreviewPane() {
        String CSSPreviewItem = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.visual.ui.preview.Bundle", "CTL_CssPreviewAction");
        new ActionNoBlock("Window"+"|"+"Other"+CSSPreviewItem,null).performMenu(); // NOI18N        
    }
    private void OpenBuilderPane() {
        String CSSBuilderItem = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.visual.ui.Bundle", "CTL_CSSStyleBuilderAction");
        new ActionNoBlock("Window"+"|"+"Other"+CSSBuilderItem,null).performMenu(); // NOI18N          
        
        
    }
    
    private static final int BACKCOLOR_COMBO_INDEX = 6;
    private static final int POSITION_COMBO_INDEX = 16;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new CSSRuleAddTest("doMeasurement"));        
    }

}
