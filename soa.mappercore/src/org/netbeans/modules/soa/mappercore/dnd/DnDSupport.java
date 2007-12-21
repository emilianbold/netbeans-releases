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
package org.netbeans.modules.soa.mappercore.dnd;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/**
 *
 * @author anjeleevich
 */
public class DnDSupport implements DnDConstants {

    private DnDHandler dndHandler;
    private JComponent component;
    
    private DragSource dragSource;
    private DropTarget dropTarget;
    
    private DnDRecognizer recognizer;
    
    private Transferable cachedTransferable;
    
    private DragHandler dragHandler;
    private DropHandler dropHandler;

    public DnDSupport(JComponent component, DnDHandler dndHandler) {
        this.component = component;
        this.dndHandler = dndHandler;

        dropHandler = new DropHandler();
        dropTarget = new DropTarget(component, dropHandler);

    }

    private DragHandler getDragHandler() {
        if (dragHandler == null) {
            dragHandler = new DragHandler();
        }
        return dragHandler;
    }

    private DragSource getDragSource() {
        if (dragSource == null) {
            dragSource = new DragSource();
        }
        return dragSource;
    }

    private DnDRecognizer getDnDRecognizer() {
        if (recognizer == null) {
            recognizer = new DnDRecognizer(getDragSource(), getDragHandler());
        }
        return recognizer;
    }

    public void startDrag(MouseEvent event, Transferable transferable,
            int action) {
        if (action != NONE) {
            cachedTransferable = transferable;
            getDnDRecognizer().startDrag(component, event, COPY | MOVE | LINK, action);
        }
    }

    private class DropHandler implements DropTargetListener {
        public void dragEnter(DropTargetDragEvent dtde) {
            dndHandler.drag(component, dtde);
        }

        public void dragOver(DropTargetDragEvent dtde) {
            dndHandler.drag(component, dtde);
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        public void dragExit(DropTargetEvent dte) {
            dndHandler.dragExit(component);
        }

        public void drop(DropTargetDropEvent dtde) {
            dndHandler.drop(component, dtde);
        }
    }

    private class DragHandler implements
            DragSourceListener, DragGestureListener {

        public void dragGestureRecognized(DragGestureEvent dge) {
            if (cachedTransferable != null) {
                try {
                    dge.startDrag(null, cachedTransferable, this);
                } finally {
                    cachedTransferable = null;
                }
            }
        }

        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {}
        public void dragDropEnd(DragSourceDropEvent dsde) {
            dndHandler.dragDone(component, dsde);
        }
    }

    private static class DnDRecognizer extends DragGestureRecognizer {

        DnDRecognizer(DragSource dragSource, DragGestureListener dgl) {
            super(dragSource, null, NONE, dgl);
        }

        void startDrag(JComponent component, MouseEvent event,
                int srcActions, int action) {
            setComponent(component);
            setSourceActions(srcActions);
            appendEvent(event);
            fireDragGestureRecognized(action, event.getPoint());
        }

        protected void registerListeners() {
        }

        protected void unregisterListeners() {
        }
    }
}
