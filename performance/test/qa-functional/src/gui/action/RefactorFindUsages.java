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

package gui.action;

import gui.Utilities;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jemmy.JemmyProperties;


import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test of Find Usages
 *
 * @author  mmirilovic@netbeans.org
 */
public class RefactorFindUsages extends testUtilities.PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE, ACTION, NEXT;
    
    private static NbDialogOperator refactorDialog;
    private static TopComponentOperator usagesWindow;
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsages(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsages(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.ui.Bundle";
        NEXT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.refactoring.api.ui.Bundle","CTL_Finish");  // "Next >"
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsed");  // "Find Usages"
        ACTION = org.netbeans.jellytools.Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsedAction"); // "Find Usages..."
        testNode = new Node(new ProjectsTabOperator().getProjectRootNode("jEdit"),Utilities.SOURCE_PACKAGES + "|org.gjt.sp.jedit|jEdit.java");
    }
    
    public void prepare() {
        // invoke Find Usages from the popup menu
        testNode.performPopupAction(ACTION);
        refactorDialog = new NbDialogOperator(TITLE);
    }
    
    public ComponentOperator open() {
        new JButtonOperator(refactorDialog, NEXT).push();
        
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 360000);
        usagesWindow = new TopComponentOperator("Usages");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        
        return usagesWindow;
    }

    
    public void close() {
        usagesWindow.close();
    }
}
