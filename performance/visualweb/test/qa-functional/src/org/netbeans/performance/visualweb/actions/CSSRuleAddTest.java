/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.VWPUtilities;
import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CSSRuleAddTest  extends PerformanceTestCase {

    private String fileName;
    private String projectName = "UltraLargeWA";
    private String styledocfolder;
    
    /** Node to be opened/edited */
    private static Node openNode ;       
    protected static String OPEN;
    
    private EditorOperator cssEditor = null;
    private NbDialogOperator createRuleDialog = null;
    
    public CSSRuleAddTest(String testName)
    {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    public CSSRuleAddTest(String testName, String performanceDataName)
    {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(CSSRuleAddTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCSSRuleAddTest() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        styledocfolder = VWPUtilities.WEB_PAGES+"|"+"resources"; // NOI18N
        OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        fileName = "stylesheet.css";  //NO18N  
        EditorOperator.closeDiscardAll();        
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
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
        invokeAddRuleEditor();
        String dialogTitle = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.css.actions.Bundle", "STYLE_RULE_EDITOR_TITLE");
        createRuleDialog = new NbDialogOperator(dialogTitle);
    }
    
    private void invokeAddRuleEditor() {
        AbstractButtonOperator addRuleButton;
        addRuleButton = cssEditor.getToolbarButton("Create Rule");
        addRuleButton.pushNoBlock();      
    }
    
    public ComponentOperator open() {
      
        processAddRule();
        return cssEditor;
    }
    
    @Override
    public void close() {
    }
    
    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    private EditorOperator findCSSEditor() {
        EditorOperator cssEditorToFind; 
        cssEditorToFind = new EditorOperator(fileName);
        return cssEditorToFind; 
    }

    private void processAddRule()
    {
        JButtonOperator addButton = new JButtonOperator(createRuleDialog,">");
        
        JTextComponentOperator textb = new JTextComponentOperator(createRuleDialog,1);
        textb.typeText("ZZZ");
        addButton.push();
        createRuleDialog.ok();
    }

}
