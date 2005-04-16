/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.outline;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

/** A trivial extension to TreeWillExpandListener, to allow listeners to be
 * notified if another TreeWillExpandListener vetos a pending expansion.
 * If a TreeExpansionListener added to an instance of TreePathSupport implements
 * this interface, it will be notified by the TreePathSupport if some other
 * listener vetos expanding a node.
 * <p>
 * This interface is primarily used to avoid memory leaks if a TreeWillExpandListener
 * constructs some data structure (like a TableModelEvent that is a translation
 * of a TreeExpansionEvent) for use when the expansion actually occurs, to notify
 * it that the pending TableModelEvent will never be fired.  It is not of much
 * interest to the rest of the world.
 *
 * @author  Tim Boudreau
 */
public interface ExtTreeWillExpandListener extends TreeWillExpandListener {
    
    public void treeExpansionVetoed (TreeExpansionEvent event, 
        ExpandVetoException exception);
    
}
