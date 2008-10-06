/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.performance.languages.actions;


import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.actions.ActionNoBlock;
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
public class PageUpPageDownScriptingEditor extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    private boolean pgup;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;    
    
    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName; 
    protected String nodePath;    
    
    public PageUpPageDownScriptingEditor(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;        
    }
    public PageUpPageDownScriptingEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;        
    }
    
    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        
        String path = nodePath+"|"+fileName;
        fileToBeOpened = new Node(getProjectNode(testProject),path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        waitNoEvent(2000);
        
        
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();   //????
        ///setEditorCaretFilteringOn(arg0);   //Which classes I can use???
        

    }
    
    protected Node getProjectNode(String projectName) {
        if(projectsTab==null)
            projectsTab = ScriptingUtilities.invokePTO();
        
        return projectsTab.getProjectRootNode(projectName);
    }
    
    @Override
    public void prepare() {
        // scroll to the place where we start
        if (pgup)
            // press CTRL+END
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        else
            // go to the first line
            editorOperator.setCaretPositionToLine(1);
    }

    @Override
    public ComponentOperator open() {
        if (pgup)
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_UP)).perform(editorOperator);
        else
            new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_PAGE_DOWN)).perform(editorOperator);
        return null;
    }
    
    @Override
    public void close() {
        log("close");
    }
    
    @Override
    public void shutdown() {
        log("shutdown");
        super.shutdown();
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();        
    }

    public void testPgUp_In_RBEditor() {
        testProject = Projects.RUBY_PROJECT;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";        
        pgup = true;
        doMeasurement();
    }
    public void testPgDn_In_RBEditor() {
        testProject = Projects.RUBY_PROJECT;
        fileName = "ruby20kb.rb";
        nodePath = "Source Files";        
        pgup = false;        
        doMeasurement();
    }
    public void testPgUp_In_PHPEditor() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        pgup = true;
        doMeasurement();
    }
    public void testPgDn_In_PHPEditor() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        pgup = false;
        doMeasurement();
    }
    public void testPgUp_In_RHTMLEditor() {
        testProject = Projects.RAILS_PROJECT;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Views";        
        pgup = true;
        doMeasurement();
    }    
    public void testPgDn_In_RHTMLEditor() {
        testProject = Projects.RAILS_PROJECT;
        fileName = "rhtml20kb.rhtml";
        nodePath = "Views";        
        pgup = false;        
        doMeasurement();
    }
    
    public void testPgUp_In_JSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript20kb.js";         
        pgup = true;
        doMeasurement();
    }
    
    public void testPgDn_In_JSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript20kb.js";         
        pgup = false;        
        doMeasurement();
    }
    
    public void testPgUp_In_CSSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "css20kb.css";        
        pgup = true;
        doMeasurement();
    }
    
    public void testPgDn_In_CSSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "css20kb.css";        
        pgup = false;        
        doMeasurement();
    }    
    
    public static Test suite() {
        prepareForMeasurements();

        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(PageUpPageDownScriptingEditor.class)
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );    
    }
}
