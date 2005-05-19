/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package prepare;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.Operator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestSuite;


/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened project and 10 files.
 * Open 10 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author Marian.Mirilovic@sun.com
 */
public class PrepareIDEForComplexMeasurements extends JellyTestCase {
    
    /** Number of copies of the Main20kB.java */
    protected static int numberCopies = 10;
    
    /** Error output from the test. */
    protected static PrintStream err;
    
    /** Logging output from the test. */
    protected static PrintStream log;
    
    /** If true - at least one test failed */
    protected static boolean test_failed = false;
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForComplexMeasurements("closeAllDocuments"));
        suite.addTest(new PrepareIDEForComplexMeasurements("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForComplexMeasurements("openFiles"));
        suite.addTest(new PrepareIDEForComplexMeasurements("saveStatus"));
        return suite;
    }
    
    
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }
    
    /**
     * Close Welcome.
     */
    public void closeWelcome(){
        try {
            new TopComponentOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.welcome.Bundle","LBL_Tab_Title")).close();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Close BluePrints.
     */
    public void closeBluePrints(){
        try {
            new TopComponentOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.blueprints.Bundle","LBL_Tab_Title")).close();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Close All Documents.
     */
    public void closeAllDocuments(){
        try {
            new CloseAllDocumentsAction().perform();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Close Memory Toolbar.
     */
    public void closeMemoryToolbar(){
        try {
            String MENU =
                    org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/View") + "|" +
                    org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle","CTL_ToolbarsListAction") + "|" +
                    org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Toolbars/Memory");
            
            MainWindowOperator mainWindow = MainWindowOperator.getDefault();
            JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
            JMenuItemOperator menuItem = menuBar.showMenuItem(MENU,"|");
            
            if(menuItem.isSelected())
                menuItem.push();
            else {
                menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
                mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            }
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
        
    }
    
    /**
     * Open 10 selected files from jEdit project.
     */
    public void openFiles(){
        
        try {
            String[][] files_path = {
                {"bsh","Interpreter.java"},
                {"bsh","JThis.java"},
                {"bsh","Name.java"},
                {"bsh","Parser.java"},
                {"bsh","Primitive.java"},
                {"com.microstar.xml","XmlParser.java"},
                {"org.gjt.sp.jedit","BeanShell.java"},
                {"org.gjt.sp.jedit","Buffer.java"},
                {"org.gjt.sp.jedit","EditPane.java"},
                {"org.gjt.sp.jedit","EditPlugin.java"},
                {"org.gjt.sp.jedit","EditServer.java"}
            };
            
            Node[] openFileNodes = new Node[files_path.length];
            Node node;
            
            // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
            ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("jEdit");
            projectNode.expand();
            
            SourcePackagesNode sourceNode = new SourcePackagesNode(projectNode);
            sourceNode.expand();
            
            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            sourceNode.setComparator(comparator);
            
            for(int i=0; i<files_path.length; i++) {
                node = new Node(sourceNode,files_path[i][0]);
                node.expand();
                
                openFileNodes[i] = new Node(node,files_path[i][1]);
                
                //try to avoid issue 56825
                openFileNodes[i].select();
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }
            
            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes);
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes);
                err.println("issue 56825 : Success");
            }
            
            
            // check whether files are opened in editor
            for(int i=0; i<files_path.length; i++) {
                new EditorOperator(files_path[i][1]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
            
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }

    /**
     * Save status, if one of the above defined test failed, this method creates 
     * file in predefined path and it means the complex tests will not run.
     */
    public void saveStatus() throws IOException{
        if(test_failed)
            new StatusFile().createNewFile();
    }
    
}
