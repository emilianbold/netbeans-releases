/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import gui.Utilities;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;

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
        TITLE = Bundle.getStringTrimmed("org.openide.explorer.Bundle","MSG_ConfirmDeleteObjectTitle"); //Confirm Object Deletion
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
        if(testedComponentOperator!=null && testedComponentOperator.isShowing())
            ((NbDialogOperator)testedComponentOperator).no();
    }
    
    
}
