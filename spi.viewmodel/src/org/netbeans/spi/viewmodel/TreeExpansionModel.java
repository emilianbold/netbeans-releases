/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.viewmodel;


/**
 * This model controlls expansion, collapsion of nodes in tree view, and 
 * defindes default expand state for all node in it.
 *
 * @author   Jan Jancura
 */
public interface TreeExpansionModel extends Model {
  
    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    public abstract boolean isExpanded (Object node) 
    throws UnknownTypeException;
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    public abstract void nodeExpanded (Object node);
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    public abstract void nodeCollapsed (Object node);
}
