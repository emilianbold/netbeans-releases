/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceListener;
import java.io.IOException;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.ErrorManager;
import org.openide.explorer.view.NodeRenderer;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * A tree displaying a hierarchy of Node in a similar fashion as the TreeView does
 * except for unnecessary popmenus and drag'n'drop implementation.
 *
 * @author Stanislav Aubrecht
 */
public class ActionsTree extends JTree implements DragGestureListener, DragSourceListener {
    
    private boolean firstTimeExpand = true;
    
    private Cursor dragMoveCursor = Utilities.createCustomCursor( this, Utilities.loadImage( "org/openide/resources/cursorsmovesingle.gif"), "ACTION_MOVE" );
    private Cursor dragNoDropCursor = Utilities.createCustomCursor( this, Utilities.loadImage( "org/openide/resources/cursorsnone.gif"), "NO_ACTION_MOVE" );
    
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
                    ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
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
