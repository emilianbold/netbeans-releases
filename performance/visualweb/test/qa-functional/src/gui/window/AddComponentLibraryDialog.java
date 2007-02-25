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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.DialogOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class AddComponentLibraryDialog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node openNode;
    
    /** Creates a new instance of AddComponentLibraryDialog */
    public AddComponentLibraryDialog(String testName) {
        super(testName);
        expectedTime = 3000;
        WAIT_AFTER_OPEN=5000;        
    }
    
    public AddComponentLibraryDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 3000;
        WAIT_AFTER_OPEN=5000;
    }
    
    public void prepare() {
        log("::prepare");
    }

    public ComponentOperator open() {
        log(":: open");
        openNode.callPopup().pushMenu(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplibAction"));
        return new DialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.addComplib"));
    }

    public void close() {
        super.close();
    }
    
    protected void initialize() {
        log("::initialize");
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode("VisualWebProject"),org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.complib.ui.Bundle", "ComplibsRootNode.displayName"));
    }
}
