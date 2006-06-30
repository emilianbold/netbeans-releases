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

package org.netbeans.jellytools.modules.junit.nodes;

/*
 * JUnitNode.java
 *
 * Created on 2/6/03 2:21 PM
 */

import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.junit.actions.*;
import org.netbeans.jellytools.nodes.Node;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.JTreeOperator;

/** JUnitNode Class
 * @author dave */
public class JUnitNode extends Node {

    private static final Action createTestsAction = new CreateTestsAction();
    private static final Action executeTestAction = new ExecuteTestAction();
    private static final Action openTestAction = new OpenTestAction();
    private static final Action propertiesAction = new PropertiesAction();

    /** creates new JUnitNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public JUnitNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }

    /** creates new JUnitNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public JUnitNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }

    /** creates new JUnitNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public JUnitNode(Node parent, String treePath) {
        super(parent, treePath);
    }

    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            createTestsAction,
            executeTestAction,
            openTestAction,
            propertiesAction
        });
    }

    /** performs CreateTestsAction with this node */
    public void createTests() {
        createTestsAction.perform(this);
    }

    /** performs ExecuteTestAction with this node */
    public void executeTest() {
        executeTestAction.perform(this);
    }

    /** performs OpenTestAction with this node */
    public void openTest() {
        openTestAction.perform(this);
    }

    /** performs PropertiesAction with this node */
    public void properties() {
        propertiesAction.perform(this);
    }
}
