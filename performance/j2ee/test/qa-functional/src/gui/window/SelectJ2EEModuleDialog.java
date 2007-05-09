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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Test of Project Properties Window
 *
 * @author  mmirilovic@netbeans.org
 */
public class SelectJ2EEModuleDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static Node testNode;
    private String TITLE;
    
    /**
     * Creates a new instance of SelectJ2EEModuleDialog 
     */
    public SelectJ2EEModuleDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }
    
    /**
     * Creates a new instance of SelectJ2EEModuleDialog 
     */
    public SelectJ2EEModuleDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }
    
    public void initialize() {
        JTreeOperator tree = new ProjectsTabOperator().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        String JAVA_EE_MODULES = Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.earproject.ui.Bundle",
                "LBL_LogicalViewNode");
        testNode = new Node(new ProjectRootNode(tree, "TestApplication"), JAVA_EE_MODULES);
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Window / Properties from the main menu
        testNode.performPopupActionNoBlock("Add J2EE Module...");
        return new NbDialogOperator("Add J2ee Module");
    }
    
}
