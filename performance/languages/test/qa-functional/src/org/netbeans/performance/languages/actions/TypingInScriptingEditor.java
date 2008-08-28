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

package org.netbeans.performance.languages.actions;


import java.awt.Font;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;


/**
 *
 * @author Administrator
 */
public class TypingInScriptingEditor extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName; 
    protected String nodePath;
    
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;
    
    private int caretBlinkRate;
    private Font font;
    
    public TypingInScriptingEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;       
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations             
    }
    public TypingInScriptingEditor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = UI_RESPONSE;       
        HEURISTIC_FACTOR = -1; // use default WaitAfterOpen for all iterations             
    }
    @Override
    public void initialize() {
        log("::initialize");
        closeAllModal();
        String path = nodePath+"|"+fileName;
        log("attempting to open: "+path);
        
        fileToBeOpened = new Node(getProjectNode(testProject),path);
    }
    
    @Override
    public void prepare() {
        log("::prepare");
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();
        
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
        editorOperator.setCaretPosition(8, 1);        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        waitNoEvent(2000);        
    }

    @Override
    public ComponentOperator open() {
        log("::typing...");
        editorOperator.typeKey('z');        
        return null;
    }
    @Override
    public void close() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();        
        EditorOperator.closeDiscardAll();
        
    }
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    public void test_RB_EditorTyping() {
        testProject = Projects.RUBY_PROJECT;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";
        doMeasurement();
    }
    public void test_RHTML_EditorTyping() {
        testProject = Projects.RAILS_PROJECT;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Views";
        doMeasurement();
    }
    public void test_JScript_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";
        doMeasurement();        
    }
    public void test_PHP_EditorTyping() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        doMeasurement();
    }
    /*
    public void test_JS_EditorTyping() {
        testProject = "";
        fileName = "";        
        nodePath = "";        
        doMeasurement();
    }
    public void test_JSON_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_CSS_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_YML_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_BAT_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_DIFF_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_MANIFEST_EditorTyping() {
        testProject = "";
        fileName = "";
        nodePath = "";        
        doMeasurement();
    }
    public void test_SH_EditorTyping() {
        testProject = "";
        fileName = "";        
        doMeasurement();
    }
    */
    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(TypingInScriptingEditor.class)
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );    
    }    
}
