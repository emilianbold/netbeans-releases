/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Documents dialog
 *
 * @author  anebuzelsky@netbeans.org
 */
public class DocumentsDialog extends testUtilities.PerformanceTestCase {
    
    private static EditorWindowOperator editor;
    
    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of DocumentsDialog */
    public DocumentsDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    protected void initialize(){
        openFiles();
        editor = new EditorWindowOperator();
    }
    
    
    public void prepare() {
        // do nothing
        // work around issue 35962 (Main menu popup accidentally rolled up)
        new ActionNoBlock("Help|About", null).perform();
        new NbDialogOperator("About").close();
   }
    
    public ComponentOperator open() {
        // invoke Window / Documents from the main menu
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Window|Documents...","|");
        return new NbDialogOperator("Documents");
    }

    protected void shutdown(){
        editor.closeDiscard();
    }
    
    /**
     * Get a prepared java file (Main20kB.java), create 10 copies and 
     * open it in editor.
     */
    protected void openFiles(){
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
            
        for(int i=0; i<files_path.length; i++) {
                openFileNodes[i] = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),"Source Packages" + '|' +  files_path[i][0] + '|' + files_path[i][1]);
        }
            
        new OpenAction().performAPI(openFileNodes);
    }
    
}
