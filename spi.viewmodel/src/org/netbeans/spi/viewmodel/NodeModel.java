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
 * Provides display name, icon and tooltip value for some type of objects.
 * Designed to be used with {@link TreeModel}.
 *
 * @author   Jan Jancura
 */
public interface NodeModel extends Model {

    /**
     * Returns display name for given node.
     *
     * @return  display name for given node
     */
    public abstract String getDisplayName (Object node)
    throws UnknownTypeException;

    /**
     * Returns icon for given node.
     *
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns tooltip for given node.
     *
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (Object node) 
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
