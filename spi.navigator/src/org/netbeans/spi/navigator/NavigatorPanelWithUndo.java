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

package org.netbeans.spi.navigator;

import org.openide.awt.UndoRedo;

/** Description of navigation view with undo/redo support on top of basic
 * NavigatorPanel features.
 * 
 * Clients will implement this interface when they need undo and redo support
 * enabled for their Navigator view/panel.
 *
 * Implementors of this interface, also registered in layer,
 * will be plugged into Navigator UI.
 *
 * @author Dafe Simonek
 */
public interface NavigatorPanelWithUndo extends NavigatorPanel {

    /** Returns instance of UndoRedo which will be propagated into 
     * Navigator TopComponent's getUndoRedo() when this panel is active.
     * 
     * It allows clients to enable undo/redo management and undo/redo actions for
     * this panel in Navigator.
     * 
     * @return Instance of UndoRedo.
     */
    public UndoRedo getUndoRedo ();
    
}
