/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import org.netbeans.jellytools.actions.RuntimeViewAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
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
        this(null);
    }
    
    /** Search for Runtime TopComponent inside given Container
     * @param contOper parent Container 
     * @deprecated Use {@link #RuntimeTabOperator()} instead.
     */
    public RuntimeTabOperator(ContainerOperator contOper) {
        super(waitTopComponent(contOper, RUNTIME_CAPTION, 0, new RuntimeTabSubchooser()));
        if(contOper != null) {
            copyEnvironment(contOper);
        }
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
