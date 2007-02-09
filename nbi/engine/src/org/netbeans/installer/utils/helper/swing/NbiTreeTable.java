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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTreeTable extends JTable {
    private NbiTreeTableModel model;
    
    private NbiTreeTableColumnRenderer treeRenderer;
    
    private boolean mousePressedEventConsumed = false;
    
    public NbiTreeTable(final NbiTreeTableModel model) {
        this.model = model;
        
        setTreeColumnRenderer(new NbiTreeTableColumnRenderer(this));
        model.setTree(treeRenderer);
        
        super.setModel(model);
        
        getColumnModel().getColumn(model.getTreeColumnIndex()).setCellRenderer(treeRenderer);
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    public void updateUI() {
        super.updateUI();
        
        if (treeRenderer != null) {
            treeRenderer.updateUI();
        }
    }
    
    public void setRowHeight(int height) {
        super.setRowHeight(height);
        
        if (treeRenderer != null) {
            treeRenderer.setRowHeight(height);
        }
    }
    
    public NbiTreeTableModel getModel() {
        return model;
    }
    
    public NbiTreeTableColumnRenderer getTreeColumnRenderer() {
        return treeRenderer;
    }
    
    public void setTreeColumnRenderer(NbiTreeTableColumnRenderer renderer) {
        treeRenderer = renderer;
        
        model.setTree(renderer);
        model.setTreeModel(renderer.getModel());
        
        treeRenderer.setRowHeight(getRowHeight());
    }
    
    public NbiTreeTableColumnCellRenderer getTreeColumnCellRenderer() {
        return treeRenderer.getTreeColumnCellRenderer();
    }
    
    public void setTreeColumnCellRenderer(NbiTreeTableColumnCellRenderer renderer) {
        treeRenderer.setTreeColumnCellRenderer(renderer);
    }
    
    protected void processMouseEvent(MouseEvent event) {
        int column = columnAtPoint(event.getPoint());
        int row = rowAtPoint(event.getPoint());
        
        if ((event.getID() == MouseEvent.MOUSE_RELEASED) && mousePressedEventConsumed) {
            mousePressedEventConsumed = false;
            event.consume();
            return;
        }
        
        if (mouseEventHitTreeHandle(event)) {
            mousePressedEventConsumed = true;
            event.consume();
            sendTreeHandleEvents(event);
            return;
        }
        
        mousePressedEventConsumed = false;
        super.processMouseEvent(event);
    }
    
    protected void processKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_RELEASED) {
            int row = getSelectedRow();
            
            if (row != -1) {
                if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (treeRenderer.isExpanded(row)) {
                        treeRenderer.collapseRow(row);
                    } else {
                        int parentRow = treeRenderer.getRowForPath(treeRenderer.getPathForRow(row).getParentPath());
                        
                        treeRenderer.collapseRow(parentRow);
                        getSelectionModel().setSelectionInterval(parentRow, parentRow);
                    }
                    event.consume();
                    return;
                }
                if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (treeRenderer.isCollapsed(row)) {
                        treeRenderer.expandRow(row);
                    }
                    event.consume();
                    return;
                }
            }
        }
        
        super.processKeyEvent(event);
    }
    
    private boolean mouseEventHitTreeHandle(MouseEvent event) {
        if ((event.getID() != MouseEvent.MOUSE_PRESSED)) {
            return false;
        }
        
        int column = columnAtPoint(event.getPoint());
        int row = rowAtPoint(event.getPoint());
        
        if (column == model.getTreeColumnIndex()) {
            MouseEvent mousePressed = new MouseEvent(treeRenderer,
                    MouseEvent.MOUSE_PRESSED,
                    event.getWhen(),
                    event.getModifiers(),
                    event.getX() - getCellRect(row, column, true).x,
                    event.getY(),
                    event.getClickCount(),
                    event.isPopupTrigger());
            MouseEvent mouseReleased = new MouseEvent(treeRenderer,
                    MouseEvent.MOUSE_RELEASED,
                    event.getWhen(),
                    event.getModifiers(),
                    event.getX() - getCellRect(row, column, true).x,
                    event.getY(),
                    event.getClickCount(),
                    event.isPopupTrigger());
            
            TreePath targetPath = treeRenderer.getPathForRow(row);
            
            boolean currentState = treeRenderer.isExpanded(targetPath);
            
            // dispatch the event and see whether the node changed its state
            model.consumeNextExpansionEvent();
            treeRenderer.dispatchEvent(mousePressed);
            treeRenderer.dispatchEvent(mouseReleased);
            
            if (treeRenderer.isExpanded(targetPath) == currentState) {
                model.cancelConsume();
                return false;
            } else {
                model.consumeNextExpansionEvent();
                treeRenderer.dispatchEvent(mousePressed);
                treeRenderer.dispatchEvent(mouseReleased);
                return true;
            }
        } else {
            return false;
        }
    }
    
    private void sendTreeHandleEvents(MouseEvent event) {
        int column = model.getTreeColumnIndex();
        int row = rowAtPoint(event.getPoint());
        
        MouseEvent mousePressed = new MouseEvent(treeRenderer,
                MouseEvent.MOUSE_PRESSED,
                event.getWhen(),
                event.getModifiers(),
                event.getX() - getCellRect(row, column, true).x,
                event.getY(),
                event.getClickCount(),
                event.isPopupTrigger());
        MouseEvent mouseReleased = new MouseEvent(treeRenderer,
                MouseEvent.MOUSE_RELEASED,
                event.getWhen(),
                event.getModifiers(),
                event.getX() - getCellRect(row, column, true).x,
                event.getY(),
                event.getClickCount(),
                event.isPopupTrigger());
        
        treeRenderer.dispatchEvent(mousePressed);
        treeRenderer.dispatchEvent(mouseReleased);
    }
}
