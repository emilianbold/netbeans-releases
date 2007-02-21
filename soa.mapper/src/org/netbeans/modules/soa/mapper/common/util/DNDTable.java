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

package org.netbeans.modules.soa.mapper.common.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.IOException;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Description of the Class
 *
 * @author    htung
 * @created   October 2, 2002
 */
public class DNDTable
     extends JTable
     implements DropTargetListener,
    DragSourceListener,
    DragGestureListener,
    MouseListener,
    KeyListener {

    /**
     * Table Model
     */
    protected TableModel mTableModel;

    /**
     * to keep the user data object, it should be transferable
     */
    protected Object mDataObject;

    /**
     * enables this component to be a mDropTarget
     */
    protected DropTarget mDropTarget = null;

    /**
     * enables this component to be a Drag Source
     */
    protected DragSource mDragSource = null;

    /**
     * The flavor transferable data
     */
    protected DataFlavor[] mDataFlavorArray = new DataFlavor[1];

    private int mDragTargetRow;

    /**
     * constructor - initializes the DropTarget and DragSource.
     *
     * @param model  Description of the Parameter
     */

    public DNDTable(TableModel model) {
        super(model);
        init();
    }

    /**
     * initialize the drag and drop
     */
    protected void init() {
        mDragTargetRow = -1;
        mDropTarget = new DropTarget(this, this);
        mDragSource = new DragSource();
        mDragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        try {
            mDataFlavorArray[0] =
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        //setDragEnabled(true);
    }

    /**
     * sets user data object to the given object
     *
     * @param obj - data object
     */
    public void setDataObject(Object obj) {
        mDataObject = obj;
    }

    /**
     * sets user data object to the given object
     *
     * @return   user data object
     */
    public Object getDataObject() {
        return mDataObject;
    }

    /**
     * This method should be called to set the column at column index to
     * a width of pWidth.
     *
     * @param column  The new columnWidth value
     * @param pWidth  The new columnWidth value
     */
    public void setColumnWidth(int column, int pWidth) {
        TableColumnModel colModel = getColumnModel();
        colModel.getColumn(column).setPreferredWidth(pWidth);
    }

    /**
     * This method should be called to set the column's minium width at
     * column index to a width of pWidth.
     *
     * @param column  The new columnMinWidth value
     * @param pWidth  The new columnMinWidth value
     */
    public void setColumnMinWidth(int column, int pWidth) {
        TableColumnModel colModel = getColumnModel();
        colModel.getColumn(column).setMinWidth(pWidth);
    }

    /**
     * Sets column with given index to resizable
     *
     * @param column    The new resizable value
     * @param isResize  The new resizable value
     */
    public void setResizable(int column, boolean isResize) {
        TableColumnModel colModel = getColumnModel();
        colModel.getColumn(column).setResizable(isResize);
    }

    /**
     * a drag gesture has been initiated
     *
     * @param event  Description of the Parameter
     */
    public void dragGestureRecognized(DragGestureEvent event) {

        Object selected = mDataObject;
        if (selected != null) {

            StringSelection text = new StringSelection(selected.toString());
            // as the name suggests, starts the dragging
            mDragSource.startDrag(event, DragSource.DefaultMoveDrop,
                text, this);
        } else {
            System.out.println("nothing was selected");
        }
    }

    /**
     * is invoked when you are dragging over the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragEnter(DropTargetDragEvent event) {

        // debug messages for diagnostics
        event.acceptDrag(DnDConstants.ACTION_COPY);
    }

    /**
     * is invoked when you are exit the DropSite without dropping
     *
     * @param event  Description of the Parameter
     */
    public void dragExit(DropTargetEvent event) { }

    /**
     * is invoked when a drag operation is going on
     *
     * @param event  Description of the Parameter
     */
    public void dragOver(DropTargetDragEvent event) { }

    /**
     * a drop has occurred
     *
     * @param event  Description of the Parameter
     */
    public void drop(DropTargetDropEvent event) {
        try {
            Transferable transferable = event.getTransferable();
            if (transferable.isDataFlavorSupported(mDataFlavorArray[0])) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                // set user object
                Object obj = transferable.getTransferData(mDataFlavorArray[0]);
                setDataObject(obj);
                event.getDropTargetContext().dropComplete(true);
            } else {
                event.rejectDrop();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Exception" + exception.getMessage());
            event.rejectDrop();
        } catch (UnsupportedFlavorException ufException) {
            ufException.printStackTrace();
            System.err.println("Exception" + ufException.getMessage());
            event.rejectDrop();
        }
    }


    /**
     * is invoked if the use modifies the current drop gesture
     *
     * @param event  Description of the Parameter
     */
    public void dropActionChanged(DropTargetDragEvent event) { }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has ended
     *
     * @param event  Description of the Parameter
     */
    public void dragDropEnd(DragSourceDropEvent event) {
        if (event.getDropSuccess()) {
            System.out.println(" dragDropEnd -> DropSuccess");
        } else {
            System.out.println(" dragDropEnd -> DropRejected");
        }
    }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has entered the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragEnter(DragSourceDragEvent event) { }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging has exited the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragExit(DragSourceEvent event) { }

    /**
     * this message goes to DragSourceListener, informing it that the
     * dragging is currently ocurring over the DropSite
     *
     * @param event  Description of the Parameter
     */
    public void dragOver(DragSourceDragEvent event) { }

    /**
     * is invoked when the user changes the dropAction
     *
     * @param event  Description of the Parameter
     */
    public void dropActionChanged(DragSourceDragEvent event) { }

    /**
     * Mouse pressed event handler
     *
     * @param event - mouse event
     */
    public void mousePressed(MouseEvent event) { }

    /**
     * Mouse clicked event handler
     *
     * @param event - mouse event
     */
    public void mouseClicked(MouseEvent event) { }

    /**
     * Mouse released event handler
     *
     * @param event - mouse event
     */
    public void mouseReleased(MouseEvent event) { }

    /**
     * Mouse entered event handler
     *
     * @param event - mouse event
     */
    public void mouseEntered(MouseEvent event) { }

    /**
     * Mouse exited event handler
     *
     * @param event - mouse event
     */
    public void mouseExited(MouseEvent event) { }

    /**
     * key pressed event handler
     *
     * @param event - key event
     */
    public void keyPressed(KeyEvent event) { }

    /**
     * key released event handler
     *
     * @param event - key event
     */
    public void keyReleased(KeyEvent event) { }

    /**
     * key typed event lintener handler
     *
     * @param event - key event
     */
    public void keyTyped(KeyEvent event) { }
}
