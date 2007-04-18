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

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class AddComponentLibraryDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node openNode;
    
    /** Creates a new instance of AddComponentLibraryDialog */
    public AddComponentLibraryDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    public AddComponentLibraryDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    protected void initialize() {
        log("::initialize");
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        String nodePath = Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.displayName");
        log("Node path = "+nodePath);
        openNode = new Node(projectRoot,nodePath);
    }
    
    public void prepare() {
        log("::prepare");
    }

    public ComponentOperator open() {
        log(":: open");
        openNode.callPopup().pushMenu(Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplibAction"));
        return new NbDialogOperator(Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplibTitle"));
    }

    public void close() {
        super.close();
    }
}
