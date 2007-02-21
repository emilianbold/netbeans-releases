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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.awt.Color;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public class PaletteButton
     extends JButton
     implements DragGestureListener, DragSourceListener, Transferable {

    /**
     * Drag source
     */
    protected DragSource mDragSource = new DragSource();

    /**
     * is the button draggable
     */
    protected boolean mIsDraggable = false;

    /**
     * data flavor
     */
    protected DataFlavor[] mDataFlavorArray = new DataFlavor[1];

    /**
     * data map
     */
    protected Map mDataMap = new HashMap();

    /**
     * Description of the Field
     */
    protected Icon mIcon = null;

    /**
     * Description of the Field
     */
    protected int mComponentType = -1;


    /**
     * Constructor
     *
     * @param icon - icon
     * @param margin - margin
     * @param toolTip - tool tip
     */
    public PaletteButton(Icon icon, Insets margin, String toolTip) {

        super(icon);
        if (margin != null) {
            this.setMargin(margin);
        }
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        mDragSource.createDefaultDragGestureRecognizer(
            this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        this.setToolTipText(toolTip);
        setBackground(Color.white);
        setMargin(margin);
        setBorderPainted(false);
        setContentAreaFilled(false);
    }

    /**
     * Constructor
     *
     * @param icon - large icon
     * @param smallIcon - small icon
     * @param margin - margin
     * @param tooltip - tool tip
     * @param type       Description of the Parameter
     */
    public PaletteButton(Icon icon, Icon smallIcon, Insets margin,
        String tooltip, int type) {
        this(smallIcon, margin, tooltip);
        mIcon = icon;
        mComponentType = type;
    }

    /**
     * Default drag and drop interface implementation
     *
     * @param event - drag gesture event
     */
    public void dragGestureRecognized(DragGestureEvent event) {
        if (this.isDraggable()) {
            mDragSource.startDrag(event, DragSource.DefaultCopyDrop, this, null);
        }
    }

    /**
     * Default dragDropEnd implementation
     *
     * @param event - drag source drop event
     */
    public void dragDropEnd(DragSourceDropEvent event) { }

    /**
     * Default dragDropEnd implementation
     *
     * @param event - dreag source drag event
     */
    public void dragEnter(DragSourceDragEvent event) { }

    /**
     * Default dragExit implementation
     *
     * @param event - drag source event
     */
    public void dragExit(DragSourceEvent event) { }


    /**
     * Default dragOver implementation
     *
     * @param event - drag source drag event
     */
    public void dragOver(DragSourceDragEvent event) { }

    /**
     * Default dropActionChanged implemetnation
     *
     * @param event drag source drag event
     */
    public void dropActionChanged(DragSourceDragEvent event) { }

    /**
     * Default getTransferData implementation for Transferable
     *
     * @param flavor - data flavor
     * @return Object
     */
    public Object getTransferData(DataFlavor flavor) {

        if (isDataFlavorSupported(flavor)) {
            return this;
        }
        return null;
    }

    /**
     * Retrieves an array of valid data flavors
     *
     * @return DataFlavor[]
     */
    public DataFlavor[] getTransferDataFlavors() {
        //System.err.println("mDataFlavorArray return " +mDataFlavorArray.length );
        return mDataFlavorArray;
    }

    /**
     * Checks whether the data flavor is supported
     *
     * @param flavor -data flavor
     * @return boolean
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < mDataFlavorArray.length; i++) {
            if (flavor.equals(mDataFlavorArray[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines whether this button isDraggable
     *
     * @return boolean
     */
    public boolean isDraggable() {
        return mIsDraggable;
    }

    /**
     * Sets the button's draggablity
     *
     * @param val - the value
     */
    public void setDraggable(boolean val) {
        mIsDraggable = val;
    }

    /**
     * adds data
     *
     * @param key - map key
     * @param val - map value
     */
    public void addData(Object key, Object val) {
        mDataMap.put(key, val);
    }

    /**
     * get data
     *
     * @param key - map key
     * @return Objec - map value
     */
    public Object getData(Object key) {
        return mDataMap.get(key);
    }


}
