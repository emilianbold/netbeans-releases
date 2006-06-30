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

package org.netbeans.spi.viewmodel;


/**
 * Filters content of some existing {@link NodeModel}. You can change display
 * name, tooltip, or icon for some existing object here.
 *
 * @author   Jan Jancura
 */
public interface NodeModelFilter extends Model {

    /**
     * Returns filterred display name for given node. You should not
     * throw UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from
     *          <code>original.getDisplayName (...)</code> method call only!
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred icon for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getIconBase (...)</code> method call only!
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getShortDescription (...)</code> method call only!
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Object node) 
    throws UnknownTypeException;


    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeModelListener (ModelListener l);
}
