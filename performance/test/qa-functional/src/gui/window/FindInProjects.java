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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of Find In Projects dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class FindInProjects extends testUtilities.PerformanceTestCase {
    
    /** Creates a new instance of FindInProjects */
    public FindInProjects(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of FindInProjects */
    public FindInProjects(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void prepare(){
        new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),"Source Packages").select();
    }
    
    public ComponentOperator open(){
//TODO doesn't work after refactoring merge        new FindAction().performShortcut();
        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock("Edit|Find in Projects...","|");
        
        return new NbDialogOperator("Find in Projects"); //NOI18N
    }
    
}
