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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ExtTreeExpansionListener.java
 *
 * Created on February 1, 2004, 6:59 PM
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
