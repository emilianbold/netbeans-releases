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

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.RuntimeViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Operator handling Runtime TopComponent.<p>
 * Functionality related to Runtime tree is delegated to JTreeOperator (method
 * tree()) and nodes (method getRootNode()).<p>
 * Example:<p>
 * <pre>
 *      RuntimeTabOperator rto = RuntimeTabOperator.invoke();
 *      // or when Runtime pane is already opened
 *      //RuntimeTabOperator rto = new RuntimeTabOperator();
 *      
 *      // get the tree if needed
 *      JTreeOperator tree = rto.tree();
 *      // work with nodes
 *      rto.getRootNode().select();
 *      Node node = new Node(rto.getRootNode(), "subnode|sub subnode");
 * </pre> 
 *
 * @see RuntimeViewAction
 */
public class RuntimeTabOperator extends TopComponentOperator {

    static final String RUNTIME_CAPTION = Bundle.getString("org.netbeans.core.Bundle", "UI/Runtime");
    private static final RuntimeViewAction viewAction = new RuntimeViewAction();
    
    private JTreeOperator _tree;
    
    /** Search for Runtime TopComponent through all IDE. */    
    public RuntimeTabOperator() {
        super(waitTopComponent(null, RUNTIME_CAPTION, 0, new RuntimeTabSubchooser()));
    }
    
    /** invokes Runtime and returns new instance of RuntimeTabOperator
     * @return new instance of RuntimeTabOperator */    
    public static RuntimeTabOperator invoke() {
        viewAction.perform();
        return new RuntimeTabOperator();
    }
    
    /** getter for Runtime JTreeOperator
     * @return JTreeOperator of Runtime tree */    
    public JTreeOperator tree() {
        makeComponentVisible();
        if(_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    /** getter for Runtime root node
     * @return RuntimeRootNode */    
    public Node getRootNode() {
        return new Node(tree(), "");
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        tree();
    }
    
    /** SubChooser to determine TopComponent is instance of 
     * org.netbeans.core.NbMainExplorer$MainTab
     * Used in constructor.
     */
    private static final class RuntimeTabSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("MainTab");
        }
        
        public String getDescription() {
            return "org.netbeans.core.NbMainExplorer$MainTab";
        }
    }
}
