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

import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Find dialog from source editor.
 *
 * @author  mmirilovic@netbeans.org
 */
public class FindInSourceEditor extends testUtilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    
    /** Creates a new instance of FindInSourceEditor */
    public FindInSourceEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of FindInSourceEditor */
    public FindInSourceEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    protected void initialize() {
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),"Source Packages|bsh|Parser.java");
        new OpenAction().performAPI(openFile);
        editor = new EditorWindowOperator().getEditor("Parser.java");
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open(){
        // press CTRL+F
        new FindAction().performShortcut();
        return new NbDialogOperator("Find"); //NOI18N
    }

    protected void shutdown(){
        editor.closeDiscard();
    }
    
}
