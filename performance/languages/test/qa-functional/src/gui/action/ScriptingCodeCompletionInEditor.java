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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager.RegionFilter;

import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingCodeCompletionInEditor extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private int lineNumber = 39;
    private String ccText = ""; 
    private EditorOperator editorOperator;
    private char completionChar;
    
    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName; 
    protected String nodePath;    
    
    protected static ProjectsTabOperator projectsTab = null;
    
    public ScriptingCodeCompletionInEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    public ScriptingCodeCompletionInEditor(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        completionChar = '.';
    }
    @Override
    public void initialize() {
        String path = nodePath+"|"+fileName;
        log("attempting to open: "+path);
        
        fileToBeOpened = new Node(getProjectNode(testProject),path);
        new OpenAction().performAPI(fileToBeOpened);
        
        editorOperator = EditorWindowOperator.getEditor(fileName);        
        
    }
    
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = new ProjectsTabOperator();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    private void setCompletionForMeasureOn() {
        repaintManager().addRegionFilter(COMPLETION_FILTER);
    }
    
    private void setCompletionForMeasuringOff() {
        repaintManager().resetRegionFilters();        
    }
    @Override
    public void prepare() {
        // measure only paint events from QuietEditorPane
        repaintManager().addRegionFilter(COMPLETION_FILTER);
        setCompletionForMeasureOn();
        editorOperator.setCaretPositionToEndOfLine(lineNumber);        
    }

    @Override
    public ComponentOperator open() {
        editorOperator.txtEditorPane().typeKey(completionChar);        
        return new CompletionJListOperator();
    }
    @Override
    public void close() {
        log("close");
        super.close();
        setCompletionForMeasuringOff();
        editorOperator.setCaretPositionRelative(-1);
        editorOperator.delete(1);        
    }

    @Override
    public void shutdown() {
        // set default values after measuring
        setCompletionForMeasuringOff();
        
        editorOperator.closeDiscard();
        super.shutdown();
    }
    
    public void testCC_InRubyEditor() {
        lineNumber = 5;
        ccText = "";
        completionChar = '.'; // Set point character after "Hello world" string. Expected code completion list appears
        
        /*
         * org-netbeans-modules-ruby.jar
         * kitClass = org.netbeans.modules.css.editor.CssEditorKit.class;
         * optionsClass = org.netbeans.modules.ruby.options.RubyOptions.class;
        */        
        doMeasurement();
    }
    public void testCC_InRHTMLEditor() {
        lineNumber = 39;
        ccText = "";
        completionChar = '.';
        // optionsClass = org.netbeans.modules.rhtml.editor.RhtmlOptions.class;
        doMeasurement();        
    }
    public void testCC_InJavaScriptEditor() {
        lineNumber = 39;
        ccText = "";
        completionChar = '.';    
        
        doMeasurement();        
    }
    public void testCC_InCSSEditor() {
        lineNumber = 39;
        ccText = "";        
        completionChar = '.';        
        /*
         * org-netbeans-modules-css-visual.jar
         * kitClass = org.netbeans.modules.css.editor.CssEditorKit.class;
         * optionsClass = org.netbeans.modules.css.options.CssOptions.class;
        */
        doMeasurement();        
    }
    
    private static final RegionFilter COMPLETION_FILTER =
            new RegionFilter() {

                public boolean accept(javax.swing.JComponent c) {
                    return c.getClass().getName().equals("org.netbeans.modules.editor.completion.CompletionScrollPane") ||
                           c.getClass().getName().equals("org.openide.text.QuietEditorPane");
                }

                public String getFilterName() {
                    return "Accept paints from org.netbeans.modules.editor.completion.CompletionScrollPane || org.openide.text.QuietEditorPane";
                }
            };    

}
