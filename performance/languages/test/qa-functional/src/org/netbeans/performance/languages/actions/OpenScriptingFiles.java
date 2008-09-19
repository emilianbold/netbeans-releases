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

import java.awt.Component;
import java.awt.Container;
import javax.swing.JEditorPane;
import javax.swing.text.Document;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;


/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenScriptingFiles extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    private static final Object EDITOR_REFS = new Object();
    
    /** Node to be opened/edited */
    public static Node fileToBeOpened ;
    protected static ProjectsTabOperator projectsTab = null;
    
    /** Folder with data */
    public static String testProject;
    protected String nodePath;
    protected String fileName;     
    
    /** Menu item name that opens the editor */
    public static String menuItem;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");    
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    public OpenScriptingFiles(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;        
    }
    public OpenScriptingFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);        
        expectedTime = WINDOW_OPEN;        
    }
    
    @Override
    protected void initialize(){
        log("::initialize");
        EditorOperator.closeDiscardAll();        

        closeAllModal();
        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        
    }
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    @Override
    public void prepare() {
        log("::prepare");
        String path = nodePath+"|"+fileName;    
        fileToBeOpened = new Node(getProjectNode(testProject),path);
        log("========== Open file path ="+fileToBeOpened.getPath());        
    }

    @Override
    public ComponentOperator open() {
        JPopupMenuOperator popup =  fileToBeOpened.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ["+ fileToBeOpened.getPath() + "] in project [" + testProject + "]");
        }
        log("------------------------- after popup invocation ------------");
        
        try {
            popup.pushMenu(menuItem);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error("Cannot push menu item ["+menuItem+"] of node [" + fileToBeOpened.getPath() + "] in project [" + testProject + "]");
        }
        log("------------------------- after open ------------");
        return new EditorOperator(this.fileName);
    }
    
    private void hookEditorDocument(Component comp) {
        if (comp instanceof Container) {
            Container cont = (Container)comp;
            for (Component c: cont.getComponents()) {
                hookEditorDocument(c);
            }
            if ("org.openide.text.QuietEditorPane".equals(comp.getClass().getName())) {
                JEditorPane pane = (JEditorPane)comp;
                Document doc = pane.getDocument();
                if (doc != null)
                    reportReference("Editor document from test "+getName(), doc, EDITOR_REFS);
            }
        }
    }
    
    @Override
    public void close(){
        if (testedComponentOperator != null) {
            hookEditorDocument(testedComponentOperator.getSource());
            ((EditorOperator)testedComponentOperator).closeDiscard();
        } else {
            throw new Error("no component to close");
        }
    }
    
    @Override
    protected void shutdown(){
        testedComponentOperator = null; // allow GC of editor and documents
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
    public void testOpening20kbRubyFile() {
        testProject = Projects.RUBY_PROJECT;
        setRubyEditorCaretFilteringOn();
        WAIT_AFTER_OPEN = 1500;
        menuItem = OPEN;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";        
        doMeasurement();        
    }
    protected void setRubyEditorCaretFilteringOn() {
        //setEditorCaretFilteringOn(org.netbeans.modules.editor.plain.PlainKit.class);        
    }
    public void testOpening20kbRHTMLFile() {
        testProject = Projects.RAILS_PROJECT;
        setRHTMLEditorCaretFilteringOn();
        WAIT_AFTER_OPEN = 1500;
        menuItem = OPEN;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Views";        
        doMeasurement();          
    }
    protected void setRHTMLEditorCaretFilteringOn() {
        //setEditorCaretFilteringOn(org.netbeans.modules.editor.plain.PlainKit.class);        
    }
    
    public void testOpening20kbJSFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        setRubyEditorCaretFilteringOn();
        WAIT_AFTER_OPEN = 1500;
        menuItem = OPEN;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";        
        doMeasurement();          
    }

    public void testOpening20kbPHPFile() {
        testProject = Projects.PHP_PROJECT;
        setRubyEditorCaretFilteringOn();
        WAIT_AFTER_OPEN = 1500;
        menuItem = OPEN;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        doMeasurement();
    }

    protected void setJSEditorCaretFilteringOn() {
        //setEditorCaretFilteringOn(org.netbeans.modules.editor.plain.PlainKit.class);        
    }    
    /*
    public void testOpening20kbJSONFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }    
    public void testOpening20kbCSSFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }
    public void testOpening20kbYMLFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }
    public void testOpening20kbBATFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }    
    public void testOpening20kbDIFFFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }
    public void testOpening20kbManifestFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }
    public void testOpening20kbShFile() {
        WAIT_AFTER_OPEN = 1500;
        menuItem = EDIT;
        doMeasurement();          
    }
    */
    /** Tests if created and later dclosed projects can be GCed from memory.
     */
    public void testGC() throws Exception {
        Thread.sleep(60*1000);
        runTestGC(EDITOR_REFS);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(OpenScriptingFiles.class)
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );
    }
}
