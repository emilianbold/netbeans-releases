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
 * Filters content of some existing {@link NodeModel}.
 *
 * @author   Jan Jancura
 */
public interface NodeModelFilter {
    
    /**
     * Returns filterred display name for given node.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Object node) 
    throws ComputingException, UnknownTypeException;
    
    /**
     * Returns filterred icon for given node.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Object node) 
    throws ComputingException, UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given node.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Object node) 
    throws ComputingException, UnknownTypeException;


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
