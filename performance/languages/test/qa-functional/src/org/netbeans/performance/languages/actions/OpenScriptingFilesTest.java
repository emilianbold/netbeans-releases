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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenScriptingFilesTest extends PerformanceTestCase {
    
    /** Node to be opened/edited */
    public static Node fileToBeOpened ;
    protected static ProjectsTabOperator projectsTab = null;
    
    /** Folder with data */
    public static String testProject;
    protected String nodePath;
    protected String fileName;     
    private static JPopupMenuOperator popup;

    /** Menu item name that opens the editor */
    public static String menuItem;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");    
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    
    public OpenScriptingFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    public OpenScriptingFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ScriptingSetup.class)
             .addTest(OpenScriptingFilesTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

        class PhaseHandler extends Handler {

            public boolean published = false;

            public void publish(LogRecord record) {
                if (record.getMessage().equals("Open Editor, phase 1, AWT [ms]"))
                     ActionTracker.getInstance().stopRecording();
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }

    PhaseHandler phaseHandler=new PhaseHandler();

    @Override
    protected void initialize(){
        EditorOperator.closeDiscardAll();        
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        
    }

    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        return projectsTab.getProjectRootNode(projectName);
    }
    
    @Override
    public void prepare() {
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
        String path = nodePath+"|"+fileName;    
        fileToBeOpened = new Node(getProjectNode(testProject),path);
         popup =  fileToBeOpened.callPopup();
    }

    @Override
    public ComponentOperator open() {
        popup.pushMenu(menuItem);
        return new EditorOperator(fileName);
    }
  
    @Override
    public void close(){
         new EditorOperator(fileName).closeDiscard();
    }
    
    @Override
    protected void shutdown(){
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
    
    public void testOpening20kbRubyFile() {
        testProject = Projects.RUBY_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";        
        doMeasurement();        
    }

    public void testOpening20kbRHTMLFile() {
        testProject = Projects.RAILS_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Test Files|unit";
        doMeasurement();          
    }

    public void testOpening20kbJSFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";        
        doMeasurement();          
    }

    public void testOpening20kbPHPFile() {
        testProject = Projects.PHP_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        doMeasurement();
    }

    public void testOpening20kbJSONFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "json20kb.json";
        doMeasurement();          
    }

    public void testOpening20kbCSSFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "css20kb.css";
        doMeasurement();          
    }

    public void testOpening20kbYMLFile() {
        testProject = Projects.RAILS_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        fileName = "yaml20kb.yml";
        nodePath = "Test Files|unit";
        doMeasurement();          
    }

    public void testOpening20kbBATFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "bat20kb.bat";
        doMeasurement();          
    }

    public void testOpening20kbDIFFFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "diff20kb.diff";
        doMeasurement();          
    }

    public void testOpening20kbManifestFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "manifest20kb.mf";
        doMeasurement();          
    }

    public void testOpening20kbShFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        menuItem = OPEN;
        nodePath = "Web Pages";
        fileName = "sh20kb.sh";
        doMeasurement();          
    }

}
