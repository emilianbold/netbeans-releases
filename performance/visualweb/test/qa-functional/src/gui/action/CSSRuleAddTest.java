/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

        
    }
    
    private void invokeAddRuleEditor() {
        JButtonOperator addRuleButton = new JButtonOperator(cssEditor, new ComponentChooser() {

            public boolean checkComponent(Component component) {
                
                if((((JButton)component).getToolTipText() != null)) {
                    if( ((JButton)component).getToolTipText().equals("Create Rule") ) {
                        return true;
                    }
                    else return false;
                } else return false;
            }

            public String getDescription() {
                return "Finds Add CSS Rule button component";
            }
        });
        System.out.println("add Rule Button obtained");
        addRuleButton.pushNoBlock();      
    }
    
    public ComponentOperator open() {
        System.out.println("::open");
        
        invokeAddRuleEditor();
        createRuleDialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.editor.Bundle", "STYLE_RULE_EDITOR_TITLE"));
        processAddRule();
        changeRuleValueOne();
        changeRuleValueTwo();
        return null;
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
        return new TopComponentOperator(fileName);
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
}
