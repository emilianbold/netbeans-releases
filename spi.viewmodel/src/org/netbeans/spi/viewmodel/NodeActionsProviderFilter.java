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

import javax.swing.Action;


/**
 * Filters actions provided by some original {@link NodeActionsProvider}.
 *
 * @author   Jan Jancura
 */
public interface NodeActionsProviderFilter {
    
    
    /**
     * Performs default action for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original,
        Object node
    ) throws UnknownTypeException;
    
    /**
     * Returns set of actions for given node.
     *
     * @throws  UnknownTypeException if this NodeActionsProvider implementation 
     *          is not able to resolve actions for given node type
     * @return  set of actions for given node
     */
    public abstract Action[] getActions (
         NodeActionsProvider original,
         Object node
    ) throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addTreeModelListener (TreeModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeTreeModelListener (TreeModelListener l);
}
