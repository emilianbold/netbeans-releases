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

package org.netbeans.modules.mobility.end2end.ui.treeview;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * User: suchys
 * Date: Dec 20, 2003
 * Time: 12:48:08 PM
 */
public class MethodCheckedNodeEditor extends AbstractCellEditor implements TreeCellEditor{
    static final long serialVersionUID = -5087358518052291490L;
    MethodCheckedNodeRenderer customRenderer = null;
    final JTree jtree;
    ItemListener itemListener;
    protected MethodCheckedTreeBeanView storage;
    
    public MethodCheckedNodeEditor(JTree jtree) {
        this.jtree = jtree;
        customRenderer = new MethodCheckedNodeRenderer();
    }
    
    public void setContentStorage(final MethodCheckedTreeBeanView storage) {
        this.storage = storage;
        customRenderer.setContentStorage(storage);
    }
    
    public Object getCellEditorValue() {
        return customRenderer;
    }
    
    public boolean isCellEditable(final EventObject e) {
        // System.out.println("isCellEditable");
        final boolean returnValue = false;
        if (e instanceof MouseEvent){
            if (!canEditImmediately(e)) return false;
            final MouseEvent mouseEvent = (MouseEvent) e;
            final TreePath path = jtree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            
            if (path != null){
                // System.out.println("path != null");
                final Object o = path.getLastPathComponent();
                final Node node = Visualizer.findNode(o);
                if (node != null ){
                    //System.out.println("TRUEEEE!!!");
                    return true;
                }
            }
        }
        //System.out.println("FALSEEEEEE");
        return returnValue;
    }
    
    /**
     * Returns true if <code>event</code> is <code>null</code>,
     * or it is a <code>MouseEvent</code> with a click count > 2
     * and <code>inHitRegion</code> returns true.
     * @param event the event being studied
     */
    protected boolean canEditImmediately(final EventObject event) {
        if ((event instanceof MouseEvent) && SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
            final MouseEvent me = (MouseEvent) event;
            return inHitRegion(me.getX(), me.getY());
        }
        return false;
    }
    
    /**
     * Returns true if the passed in location is a valid mouse location
     * to start editing from. This is implemented to return false if
     * <code>x</code> is <= the width of the icon and icon gap displayed
     * by the renderer. In other words this returns true if the user
     * clicks over the text part displayed by the renderer, and false
     * otherwise.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return true if the passed in location is a valid mouse location
     */
    protected boolean inHitRegion(final int x, final int y) {
        final int lastRow = jtree.getRowForLocation(x, y);
        if (lastRow != -1 && jtree != null) {
            final Rectangle bounds = jtree.getRowBounds(lastRow);
            if (bounds != null && (x - bounds.x ) < 10 && (x - bounds.x) >= 0) {
                return true;
            }
        }
        return false;
    }
    
    public Component getTreeCellEditorComponent(final JTree tree, final Object value,
            final boolean isSelected, final boolean expanded,
            final boolean leaf, final int row) {
        //System.out.println("INNNNNNN getTreeCellEditorComponent") ;
        final Component editor = customRenderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, true);
        //  System.out.println("editor1 = " + editor.getClass().getName());
        itemListener = new ItemListener() {
            public void itemStateChanged(@SuppressWarnings("unused") ItemEvent e) {
                //System.out.println("itemStateChanged") ;
                if (stopCellEditing()){
                    if (editor instanceof MethodCheckedNodeRenderer){
                        ((MethodCheckedNodeRenderer)editor).removeItemListener(itemListener);
                    }
                    final Node node = Visualizer.findNode(value);
                    
                    storage.setState(node, MethodCheckedTreeBeanView.UNSELECTED == storage.getState(node));
                    final TreePath path = tree.getAnchorSelectionPath();
                    ((DefaultTreeModel)tree.getModel()).reload();
                    tree.setAnchorSelectionPath(path);
                }
            }
        };
        //  System.out.println("editor2 = " + editor.getClass().getName());
        if (editor instanceof MethodCheckedNodeRenderer){
            // System.out.println("add Item Listener");
            ((MethodCheckedNodeRenderer)editor).addItemListener(itemListener);
        }
        return editor;
    }
    
}
