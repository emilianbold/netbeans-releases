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

import gui.Utilities;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.PropertiesAction;


import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Delete File Dialog
 *
 * @author  mmirilovic@netbeans.org
 */
public class DeleteFileDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE;
    
    /** Creates a new instance of RefactorRenameDialog */
    public DeleteFileDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of RefactorRenameDialog */
    public DeleteFileDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        TITLE = "Confirm Object Deletion"; //NOI18N
        testNode = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),Utilities.SOURCE_PACKAGES + "|org.gjt.sp.jedit|jEdit.java");
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Delete from the popup menu
        new DeleteAction().performShortcut();
        return new NbDialogOperator(TITLE);
    }

    public void close() {
        ((NbDialogOperator)testedComponentOperator).no();
    }
    
    
}
