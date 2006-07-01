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

import org.openide.nodes.Node;
import org.openide.util.WeakSet;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants; // TEMP

import java.util.Iterator;

import javax.swing.JScrollPane;


/**
 * Manager for explorer DnD.
 *
 *
 * @author  Jiri Rechtacek
 *
 * @see TreeViewDragSupport
 * @see TreeViewDropSupport
 */
final class ExplorerDnDManager {
    /** Singleton instance of explorer dnd manager. */
    private static ExplorerDnDManager defaultDnDManager;
    private Node[] draggedNodes;
    private Transferable draggedTransForCut;
    private Transferable draggedTransForCopy;
    private boolean isDnDActive = false;
    private int nodeAllowed = 0;
    private Cursor cursor = null;
    private boolean maybeExternalDnD = false;

    /** Creates a new instance of <code>WindowsDnDManager</code>. */
    private ExplorerDnDManager() {
    }

    /** Gets the singleton instance of this window dnd manager. */
    static synchronized ExplorerDnDManager getDefault() {
        if (defaultDnDManager == null) {
            defaultDnDManager = new ExplorerDnDManager();
        }

        return defaultDnDManager;
    }

    void setDraggedNodes(Node[] n) {
        draggedNodes = n;
    }

    Node[] getDraggedNodes() {
        return draggedNodes;
    }

    void setDraggedTransferable(Transferable trans, boolean isCut) {
        if (isCut) {
            draggedTransForCut = trans;
        } else {
            draggedTransForCopy = trans;
        }
    }

    Transferable getDraggedTransferable(boolean isCut) {
        if (isCut) {
            return draggedTransForCut;
        }

        // only for copy
        return draggedTransForCopy;
    }

    void setNodeAllowedActions(int actions) {
        nodeAllowed = actions;
    }

    final int getNodeAllowedActions() {
        if( !isDnDActive && maybeExternalDnD )
            return DnDConstants.ACTION_COPY;
        
        return nodeAllowed;
    }

    void setDnDActive(boolean state) {
        isDnDActive = state;
    }

    boolean isDnDActive() {
        return isDnDActive || maybeExternalDnD;
    }

    int getAdjustedDropAction(int action, int allowed) {
        int possibleAction = action;

        if ((possibleAction & allowed) == 0) {
            possibleAction = allowed;
        }

        if ((possibleAction & getNodeAllowedActions()) == 0) {
            possibleAction = getNodeAllowedActions();
        }

        return possibleAction;
    }

    void setMaybeExternalDragAndDrop( boolean maybeExternalDnD ) {
        this.maybeExternalDnD = maybeExternalDnD;
    }

    void prepareCursor(Cursor c) {
        this.cursor = c;
    }

    Cursor getCursor() {
        return this.cursor;
    }
}
