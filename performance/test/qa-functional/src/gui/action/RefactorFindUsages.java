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

package gui.action;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test of Find Usages
 *
 * @author  mmirilovic@netbeans.org
 */
public class RefactorFindUsages extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE, ACTION, NEXT;
    
    private static NbDialogOperator refactorDialog;
    private static TopComponentOperator usagesWindow;
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsages(String testName) {
        super(testName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public RefactorFindUsages(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }
    
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.ui.Bundle";
        NEXT = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.api.ui.Bundle","CTL_Finish");  // "Next >"
        TITLE = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsed");  // "Find Usages"
        ACTION = Bundle.getStringTrimmed(BUNDLE,"LBL_WhereUsedAction"); // "Find Usages..."
        testNode = new Node(new SourcePackagesNode("jEdit"),"org.gjt.sp.jedit|jEdit.java");
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
        usagesWindow = new TopComponentOperator("Usages"); // NOI18N
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        
        return usagesWindow;
    }

    
    public void close() {
        usagesWindow.close();
    }
}
