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
 */

package org.netbeans.modules.vmd.screen.resource;

import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.screen.MainPanel;
import org.netbeans.modules.vmd.screen.ScreenViewController;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavor;

/**
 * @author breh
 */
public class ResourceItemPanel extends JLabel implements MouseListener {
    
    private static Border SELECTED_RESOURCE_BORDER = new LineBorder(MainPanel.SELECT_COLOR, 2, false);
    private static Border HOVER_RESOURCE_BORDER = new LineBorder(MainPanel.HOVER_COLOR, 2, false);
    private static Border RESOURCE_BORDER = new EmptyBorder(2, 2, 2, 2);
    
    private DesignComponent component;
    private boolean selected;
    private boolean hovered;
    private DragSource dragSource;
    private DragListener listener;
    private DropTarget dropTarget;
    
    public ResourceItemPanel(DesignComponent component) {
        this.component = component;
        setOpaque(false);
        setBackground(Color.WHITE);
        addMouseListener(this);
        listener =  new DragListener();
        initDragAndDrop();
    }
    
    private void initDragAndDrop() {
        dragSource = new DragSource();
        dropTarget = new DropTarget(this, new DropListener());
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, listener);
    }
    
    // called from AWT and document transaction
    public void reload() {
        InfoPresenter infoPresenter = component.getPresenter(InfoPresenter.class);
        ScreenResourceItemPresenter itemPresenter = component.getPresenter(ScreenResourceItemPresenter.class);
        assert infoPresenter != null : "Null InfoPresenter"; //NOI18N
        assert itemPresenter != null : "Null ScreenResourceItemPresenter"; //NOI18N
        InfoPresenter.NameType nameType = itemPresenter.getNameType();
        Image image = infoPresenter.getIcon(InfoPresenter.IconType.COLOR_16x16);
        setIcon(image != null ? new ImageIcon(image) : null);
        
        selected = component.getDocument().getSelectedComponents().contains(component);
        resolveBorder();
        
        setText(infoPresenter.getDisplayName(nameType));
    }
    
    public JPopupMenu getComponentPopupMenu() {
        return null;
    }
    
    public void mouseClicked(final MouseEvent e) {
        doSelect(e);
    }
    
    private void doSelect(final MouseEvent e) {
        final DesignDocument doc = component.getDocument();
        final Collection<DesignComponent> newSelection = new ArrayList<DesignComponent> ();
        doc.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK) {
                    // invert selection
                    Collection<DesignComponent> currentSelection = doc.getSelectedComponents();
                    newSelection.addAll(currentSelection);
                    if (currentSelection.contains(component)) {
                        newSelection.remove(component);
                    } else {
                        newSelection.add(component);
                    }
                } else {
                    newSelection.add(component);
                }
                doc.setSelectedComponents(ScreenViewController.SCREEN_ID, newSelection);
            }
        });
    }
    
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            doSelect(e);
            Utilities.actionsToPopup(ActionsSupport.createActionsArray(component), this).show(this, e.getX(), e.getY());
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            doSelect(e);
            Utilities.actionsToPopup(ActionsSupport.createActionsArray(component), this).show(this, e.getX(), e.getY());
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        hovered = true;
        resolveBorder();
    }
    
    public void mouseExited(MouseEvent e) {
        hovered = false;
        resolveBorder();
    }
    
    private void resolveBorder() {
        if (hovered)
            setBorder(HOVER_RESOURCE_BORDER);
        else
            setBorder(selected ? SELECTED_RESOURCE_BORDER : RESOURCE_BORDER);
    }
    
    private class DragListener implements DragGestureListener {
        private ScreenTransferable flavor;
        
        public void dragGestureRecognized(DragGestureEvent dgEvent) {
            if (flavor == null)
                flavor = new ScreenTransferable(component);
            try {
                dgEvent.startDrag(null , flavor);
            } catch (InvalidDnDOperationException e) {
                e.printStackTrace();
            }
        }
    }
    
    private class DropListener implements DropTargetListener {
        
        public void dragEnter(DropTargetDragEvent dtde) {
            hovered = true;
            resolveBorder();
        }
        
        public void dragOver(DropTargetDragEvent dtde) {
            dtde.rejectDrag();
        }
        
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
        
        public void dragExit(DropTargetEvent dte) {
            hovered = false;
            resolveBorder();
        }
        
        public void drop(DropTargetDropEvent dtde) {
        }
        
    }
    
    private class ScreenTransferable implements Transferable {
        private DesignComponent component;
        
        public ScreenTransferable(DesignComponent component) {
            this.component = component;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR};
        }
        
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor == DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR) 
                return  true;
            return false;
        }
        
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor == DesignComponentDataFlavor.DESIGN_COMPONENT_DATA_FLAVOR)
                return component;
            return null;
        }
    }
    
}
