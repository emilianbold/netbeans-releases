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

package org.netbeans.modules.soa.mapper.basicmapper.dnd;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
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

import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;

/**
 * <p>
 *
 * Title: </p> ComponentDnDAdapter <p>
 *
 * Description: </p> ComponentDnDAdapter implements all java.awt.dnd drag and
 * drop interfaces to provide a convience object to be delagated. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 * @version   1.0
 */
public final class ComponentDnDHandler
     implements DragGestureListener,
    DropTargetListener, DragSourceListener {

    /**
     * the component that this dnd controlers over
     */
    private Component mComp;

    /**
     * the drop target instance for this adapter
     */
    private DropTarget mDropTarget;

    /**
     * the gesture recognizer of this adapter
     */
    private DragGestureRecognizer mRecognizer;

    /**
     * the DnDHandler
     */
    private IDnDHandler mHandler;

    /**
     * Creates a new ComponentDnDAdapter object.
     */
    public ComponentDnDHandler() { }

    /**
     * Sets the handler attribute of the ComponentDnDHandler object
     *
     * @param handler  The new handler value
     */
    public void setHandler(IDnDHandler handler) {
        mHandler = handler;
    }

    /**
     * Gets the handler attribute of the ComponentDnDHandler object
     *
     * @return   The handler value
     */
    public IDnDHandler getHandler() {
        return mHandler;
    }

    /**
     * Set the component of this adapter to handle.
     *
     * @param comp  the component of this adapter to handle.
     */
    public void setComponent(Component comp) {
        if (mComp != null) {
            mRecognizer.removeDragGestureListener(this);
            mDropTarget.removeDropTargetListener(this);
            DragSource.getDefaultDragSource().removeDragSourceListener(this);
            mComp.setDropTarget(null);
            mRecognizer = null;
            mDropTarget = null;
        }

        mComp = comp;

        if (mComp != null) {
            mRecognizer = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
                mComp, DnDConstants.ACTION_COPY_OR_MOVE, this);
            DragSource.getDefaultDragSource().addDragSourceListener(this);
            mDropTarget = new DropTarget(mComp, this);
            mComp.setDropTarget(mDropTarget);
        }
    }

    /**
     * Return the drop target of the handler, or null if no component is under
     * control.
     *
     * @return   the drop target of the handler.
     */
    public DropTarget getDropTarget() {
        return mDropTarget;
    }

    /**
     * Return the DragGestureRecognizer of this handler, or null if no component
     * is under control.
     *
     * @return   the DragGestureRecognizer of this handler.
     */
    public DragGestureRecognizer getDragGestureRecognizer() {
        return mRecognizer;
    }

    /**
     * Return the DragSource of this handler, or null if no component is under
     * control.
     *
     * @return   the DragSource of this handler.
     */
    public DragSource getDragSource() {
        if (mComp == null) {
            return null;
        }
        return DragSource.getDefaultDragSource();
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragEnter, this method
     * delegates to IDnDHandler.getDropTargetListener.dragEnter.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragEnter(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragExit, this method delegates
     * to IDnDHandler.getDropTargetListener.dragExit.
     *
     * @param dte  the DropTargetEvent
     */
    public void dragExit(DropTargetEvent dte) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragExit(dte);
        }
    }

    /**
     * Overrides java.awt.dnd.DragGestureListener.dragGestureRecognized, this
     * method delegates to IDnDHandler.getDragGestureListener.dragGestureRecognized.
     *
     * @param e  the DragGestureEvent
     */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (mHandler != null && mHandler.getDragGestureListener() != null) {
            mHandler.getDragGestureListener().dragGestureRecognized(e);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dragOver, this method delegates
     * to IDnDHandler.getDropTargetListener.dragOver.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dragOver(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dragOver(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.drop, this method delegates to
     * IDnDHandler.getDropTargetListener.drop.
     *
     * @param event  the DropTargetDropEvent
     */
    public void drop(DropTargetDropEvent event) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().drop(event);
        }
    }

    /**
     * Overrides java.awt.dnd.DropTargetListener.dropActionChanged, this method
     * delegates to IDnDHandler.getDropTargetListener.dropActionChanged.
     *
     * @param dtde  the DropTargetDragEvent
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (mHandler != null && mHandler.getDropTargetListener() != null) {
            mHandler.getDropTargetListener().dropActionChanged(dtde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragDropEnd, this method
     * delegates to IDnDHandler.getDragSourceListener.dragDropEnd.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragDropEnd(DragSourceDropEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragDropEnd(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragEnter, this method
     * delegates to IDnDHandler.getDragSourceListener.dragEnter.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragEnter(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragEnter(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragExit, this method delegates
     * to IDnDHandler.getDragSourceListener.dragExit.
     *
     * @param dse  Description of the Parameter
     */
    public void dragExit(DragSourceEvent dse) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragExit(dse);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dragOver, this method delegates
     * to IDnDHandler.getDragSourceListener.dragOver.
     *
     * @param dsde  Description of the Parameter
     */
    public void dragOver(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dragOver(dsde);
        }
    }

    /**
     * Overrides java.awt.dnd.DragSourceListener.dropActionChanged, this method
     * delegates to IDnDHandler.getDragSourceListener.dropActionChanged.
     *
     * @param dsde  Description of the Parameter
     */
    public void dropActionChanged(DragSourceDragEvent dsde) {
        if (mHandler != null && mHandler.getDragSourceListener() != null) {
            mHandler.getDragSourceListener().dropActionChanged(dsde);
        }
    }
}
