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

package org.netbeans.core.windows.view.ui.toolbars;


import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.*;
import org.openide.explorer.view.*;
import org.openide.nodes.Node;

/**
 * A tree displaying a hierarchy of Node in a similar fashion as the TreeView does
 * except for unnecessary popmenus and drag'n'drop implementation.
 *
 * @author Stanislav Aubrecht
 */
public class ActionsTree extends JTree implements DragGestureListener, DragSourceListener {
    
    private boolean firstTimeExpand = true;
    
    private Cursor dragMoveCursor = DragSource.DefaultMoveDrop;
    private Cursor dragNoDropCursor = DragSource.DefaultMoveNoDrop;
    
    /** Creates a new instance of ActionsTree */
    public ActionsTree( Node root ) {
        super( new NodeTreeModel( root ) );
        setRootVisible( false );
        getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        setCellRenderer( new NodeRenderer() );
        setShowsRootHandles( true );
        expandAll();
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this );
    }
    
    private void expandAll() {
        int i = 0;
        int j /*, k = tree.getRowCount()*/;

        do {
            do {
                j = getRowCount();
                expandRow(i);
            } while (j != getRowCount());

            i++;
        } while (i < getRowCount());
    }

    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = getPathForLocation( dge.getDragOrigin().x, dge.getDragOrigin().y );
        if( null != path ) {
            Object obj = path.getLastPathComponent();
            if( getModel().isLeaf( obj ) ) {
                try {
                    Node node = Visualizer.findNode( obj );
                    Transferable t = node.drag();
                    dge.getDragSource().addDragSourceListener( this );
                    dge.startDrag( dragNoDropCursor, t );
                } catch( IOException e ) {
                    Logger.global.log(Level.WARNING, null, e);
                }
            }
        }
    }

    public void dragExit(java.awt.dnd.DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor( dragNoDropCursor );
    }

    public void dropActionChanged(java.awt.dnd.DragSourceDragEvent dsde) {
    }

    public void dragOver(java.awt.dnd.DragSourceDragEvent e) {
        DragSourceContext context = e.getDragSourceContext();
        int action = e.getDropAction();
        if ((action & DnDConstants.ACTION_MOVE) != 0) {
            context.setCursor( dragMoveCursor );
        } else {
            context.setCursor( dragNoDropCursor );
        }
    }

    public void dragEnter(java.awt.dnd.DragSourceDragEvent dsde) {
        dragOver( dsde );
    }

    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dsde) {
    }
}
