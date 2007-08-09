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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package footprint;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.QueueTool;

import org.netbeans.junit.ide.ProjectSupport;

import gui.Utilities;
/**
 * Measure Rename Class Memory footprint
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class RefactoringRename extends org.netbeans.performance.test.utilities.MemoryFootprintTestCase {
    
    private static final String rename = "jEdit";
    
    /**
     * Creates a new instance of RefactoringRename
     *
     * @param testName the name of the test
     */
    public RefactoringRename(String testName) {
        super(testName);
        prefix = "Refactoring Rename |";
    }
    
    /**
     * Creates a new instance of RefactoringRename
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public RefactoringRename(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "Refactoring Rename |";
    }
    
    public void initialize() {
        super.initialize();
        FootprintUtilities.closeAllDocuments();
        FootprintUtilities.closeMemoryToolbar();
    }
    
    @Override
    public void setUp() {
        //do nothing
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open(){
        String rename_to, rename_from = rename;
        
        // jEdit project
        log("Opening project jEdit");
        ProjectsTabOperator.invoke();
        FootprintUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +"jEdit41");
        FootprintUtilities.waitForPendingBackgroundTasks();
        
        // find existing node
        Node packagenode = new Node(new SourcePackagesNode("jEdit"), "org.gjt.sp.jedit");
        String[] children = packagenode.getChildren();
        for(int i=0; i<children.length; i++) {
            if(children[i].startsWith(rename)){
                rename_from = children[i];
                break;
            }
        }
        
        log(" Trying to rename file "+rename_from);
        
        //generate name for new node
/*        String number = "1";
        if(rename_from.equalsIgnoreCase(rename+".java")) {
            rename_to = rename + number;
        } else {
            number = rename_from.substring(rename.length(),rename_from.indexOf('.'));
            rename_to = rename + (Integer.parseInt(number) + 1);
        }
*/
	rename_to=rename+Utilities.getTimeIndex();
        
        // invoke Rename
        Node filenode = new Node(packagenode, rename_from);
        filenode.callPopup().pushMenuNoBlock("Refactor|Rename..."); // NOI18N
        NbDialogOperator renamedialog = new NbDialogOperator("Rename "); // NOI18N
        JTextFieldOperator txtfNewName = new JTextFieldOperator(renamedialog);
        JButtonOperator btnRefactor = new JButtonOperator(renamedialog,"Refactor"); // NOI18N
        
        // if the project exists, try to generate new name
        rename_to = rename_to+"1";
        log("    ... rename to  " + rename_to);
        txtfNewName.clearText();
        txtfNewName.typeText(rename_to);
        
        new JCheckBoxOperator(renamedialog,"Apply Rename on Comments").changeSelection(true); // NOI18N
        btnRefactor.push();
        
        MainWindowOperator.getDefault().waitStatusText("Save All finished"); // NOI18N
        
        rename_to = rename_from;
	return null;
    }
    
    public void close(){
        ProjectSupport.closeProject("jEdit");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new RefactoringRename("measureMemoryFootprint"));
    }
    
}
