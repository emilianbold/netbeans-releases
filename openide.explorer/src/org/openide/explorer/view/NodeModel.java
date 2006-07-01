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
package org.openide.explorer.view;


/** Base class for all models that work over nodes and are swing thread safe.
*
* @author Jaroslav Tulach
*/
interface NodeModel extends java.util.EventListener {
    /** Notification of children addded event. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void added(VisualizerEvent.Added ev);

    /** Notification that children has been removed. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void removed(VisualizerEvent.Removed ev);

    /** Notification that children has been reordered. Modifies the list of nodes
    * and fires info to all listeners.
    */
    abstract void reordered(VisualizerEvent.Reordered ev);

    /** Update a visualizer (change of name, icon, description, etc.)
    */
    abstract void update(VisualizerNode v);

    /** Notification about large change in the sub tree
     */
    abstract void structuralChange(VisualizerNode v);
}
