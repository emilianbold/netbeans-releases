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

package org.netbeans.modules.xml.xam;

/**
 * Updater for children list of a component.
 *
 * @author nn136682
 */
public interface ComponentUpdater<C extends Component> {
    public enum Operation { ADD, REMOVE };
    
    /**
     * Updates children list.
     *
     * @param target component to be updated
     * @param child component to be added or removed.
     * @param operation add or remove; if null, no update should happen, only 
     * query for possibility the update.
     */
    void update(C target, C child, Operation operation);
    
    /**
     * Updates children list.
     *
     * @param target component to be updated
     * @param child component to be added or removed.
     * @param index of child component to be added or removed.
     * @param operation add or remove; if null, no update should happen, only 
     * query for possibility the update.
     */
    void update(C target, C child, int index, Operation operation);
    
    /**
     *  Provide capability to query for updatability.
     */
    interface Query<C extends Component> {
        /**
         * Check if a component can be added to target component.
         *
         * @param target component to be updated
         * @param child component to be added.
         */
        boolean canAdd(C target, Component child);
    }
}
