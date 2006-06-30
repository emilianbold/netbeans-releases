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

import javax.swing.Action;


/**
 * Filters actions provided by some original {@link NodeActionsProvider}.
 * It can be used to add some new actions to nodes pop-up menu, remove
 * some actions or redefine behaviour of some actions.
 *
 * @author   Jan Jancura
 */
public interface NodeActionsProviderFilter extends Model {


    /**
     * Performs default action for given node. You should not throw
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.performDefaultAction (...)</code> method call only!
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original,
        Object node
    ) throws UnknownTypeException;
    
    /**
     * Returns set of actions for given node. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getActions (...)</code> method call only!
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
//    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
//    public abstract void removeModelListener (ModelListener l);
}
