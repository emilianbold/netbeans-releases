/*
 *                Sun Public License Notice
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

package org.netbeans.jellytools.modules.xml.catalog.nodes;

import java.lang.reflect.Constructor;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/** AbstractNode Class
 * @author ms113234 */
public abstract class AbstractNode extends Node {
    private static RuntimeTabOperator runtimeTabOperator;
    
    /** creates new DriversNode
     * @param tree JTreeOperator of tree
     * @param treePath String tree path */
    public AbstractNode(JTreeOperator tree, String treePath) {
        super(tree, treePath);
    }
    
    /** creates new DriversNode
     * @param tree JTreeOperator of tree
     * @param treePath TreePath of node */
    public AbstractNode(JTreeOperator tree, TreePath treePath) {
        super(tree, treePath);
    }
    
    /** creates new DriversNode
     * @param parent parent Node
     * @param treePath String tree path from parent Node */
    public AbstractNode(Node parent, String treePath) {
        super(parent, treePath);
    }
    
    /** This method tests if the specified children exists.
     * @param displayName children name
     * @return true if the specified children exists
     */
    public boolean containsChild(String displayName) {
        String[] drivers = this.getChildren();
        for (int i = 0 ; i < drivers.length ; i++) {
            if (displayName.equals(drivers[i]) ) {
                return true;
            }
        }
        return false;
    }
    
    /** This method creates a new node operator for child.
     * @param displayName children name
     * @param clazz children class
     * @return children node
     */
    public Node getChild(String displayName, Class clazz) {
        if (!Node.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz + " is not instance of org.netbeans.jellytools.nodes.Node");
        }
        if (!this.containsChild(displayName) ) {
            return null;
        }
        Node node = null;
        try {
            Constructor constructor = clazz.getConstructor(new Class[] {Node.class, String.class});
            node = (Node) constructor.newInstance(new Object[] {this, displayName});
        } catch (Exception ex) {
            throw new RuntimeException("Cannot instantiate " + clazz, ex);
        }
        return node;
    }
    
    public static synchronized RuntimeTabOperator getRuntimeTab() {
        if (runtimeTabOperator == null) {
            runtimeTabOperator = new RuntimeTabOperator();
        }
        runtimeTabOperator.invoke();
        return runtimeTabOperator;
    }
}
