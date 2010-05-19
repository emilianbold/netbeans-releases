/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.edm.editor.graph.components;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseMotionAdapter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Base dragable menu item class
 * 
 * @author Girish Patil
 */
public class BaseDragableMenuItem extends JButton implements DragGestureListener, Transferable {

    /**
     * The default data flavor of this class.
     */
    protected static final DataFlavor DEFAULT_FLAVORS[] = new DataFlavor[1];

    /**
     * Is this label draggable.
     */
    protected boolean mIsDraggable = true;

    /**
     * The cursor of this drag operation. Default to copy drop cursor.
     */
    protected Cursor mDragCursor = DragSource.DefaultCopyDrop;

    /**
     * data flavor and transfer object storage
     */
    protected Map dataFlavorMap;

    /**
     * The default transfer data. Default reference to this object instance.
     */
    protected Object mDefaultData;

    /**
     * the gesture recognizer from default drag source.
     */
    protected DragGestureRecognizer mRecognizer;

    static {
        try {
            DEFAULT_FLAVORS[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException ex) {
            // Ignore
        }
    }

    /**
     * Creates a DragableJLabel instance with no image and with an empty string for the
     * title.
     */
    public BaseDragableMenuItem() {
        super();
        init();
    }

    /**
     * Creates a BaseDragableMenuItem instance with the specified image.
     * 
     * @param image the image of this label
     */
    public BaseDragableMenuItem(Icon image) {
        super(image);
        init();
    }

    /**
     * Creates a BaseDragableMenuItem instance with the specified image and horizontal
     * alignment.
     * 
     * @param image the image of this label
     * @param horizontalAlignment the horizontal aligmenet
     */
    public BaseDragableMenuItem(Icon image, int horizontalAlignment) {
        super(image);
        this.setHorizontalAlignment(horizontalAlignment);
        init();
    }

    /**
     * Creates a BaseDragableMenuItem instance with the specified text.
     * 
     * @param text the text of this label.
     */
    public BaseDragableMenuItem(String text) {
        super(text);
        init();
    }

    /**
     * Creates a BaseDragableMenuItem instance with the specified text, image, and
     * horizontal alignment.
     * 
     * @param text the text of this label
     * @param icon the icon of this label.
     * @param horizontalAlignment the horizontal aligment
     */
    public BaseDragableMenuItem(String text, Icon icon, int horizontalAlignment) {
        super(text, icon);
        this.setHorizontalAlignment(horizontalAlignment);
        init();
    }

    /**
     * Creates a BaseDragableMenuItem instance with the specified text and horizontal
     * alignment.
     * 
     * @param text the text of this label
     * @param horizontalAlignment the horizontal aligment
     */
    public BaseDragableMenuItem(String text, int horizontalAlignment) {
        super(text);
        this.setHorizontalAlignment(horizontalAlignment);
        init();
    }

    /**
     * Return the default data flavor DataFlavor.javaJVMLocalObjectMimeType. Or null if
     * the VM does not support DataFlavor.javaJVMLocalObjectMimeType.
     * 
     * @return the default data flavor of this drag operation.
     */
    public DataFlavor getDefaultDataFlavor() {
        return DEFAULT_FLAVORS[0];
    }

    /**
     * Retrun the drag action of this dragable.
     * 
     * @return the drag action of this dragable.
     */
    public int getDragAction() {
        return mRecognizer.getSourceActions();
    }

    /**
     * Retrun the cursor for this drag operation.
     * 
     * @return the cursor to display during this drag operation.
     */
    public Cursor getDragCursor() {
        return mDragCursor;
    }

    /**
     * Default getTransferData implementation for Transferable.
     * 
     * @param flavor Description of the Parameter
     * @return The transferData value
     */
    public Object getTransferData(DataFlavor flavor) {
        return dataFlavorMap.get(flavor);
    }

    /**
     * Retrieves an array of valid data flavors.
     * 
     * @return The transferDataFlavors value
     */
    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) dataFlavorMap.keySet().toArray(new DataFlavor[0]);
    }

    /**
     * Checks whether the data flavor is supported.
     * 
     * @param flavor Description of the Parameter
     * @return The dataFlavorSupported value
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor dataFlavors[] = this.getTransferDataFlavors();

        if ((dataFlavors != null) && (dataFlavors.length > 0)) {
            for (int i = 0; i < dataFlavors.length; i++) {
                if (flavor.equals(dataFlavors[i])) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines whether this button isDraggable
     * 
     * @return The draggable value
     */
    public boolean isDraggable() {
        return mIsDraggable;
    }

    /**
     * Set the default transfer data object. This object is returned when
     * <code>getTransferData</code> has been passed the default data flavor as its
     * paramter. If data is null, transfer data will be this object instance.
     * 
     * @param data the defautl transfer data for the drag operation.
     */
    public void setDefaultTransferData(Object data) {
        if (data == null) {
            dataFlavorMap.put(getDefaultDataFlavor(), this);
        }

        dataFlavorMap.put(getDefaultDataFlavor(), data);
    }

    /**
     * Set the drag action of this label.
     * 
     * @param dragAction the drag action of this label.
     */
    public void setDragAction(int dragAction) {
        mRecognizer.setSourceActions(dragAction);
    }

    /**
     * Set the cursor for this drag operation. Default to
     * <code>DragSource.DefaultCopyDrop</code> if dragCursor is null.
     * 
     * @param dragCursor the cursor to display during this drag operation.
     */
    public void setDragCursor(Cursor dragCursor) {
        if (dragCursor == null) {
            mDragCursor = DragSource.DefaultCopyDrop;
        }

        mDragCursor = dragCursor;
    }

    /**
     * Sets the button's draggablity
     * 
     * @param val The new draggable value
     */
    public void setDraggable(boolean val) {
        mIsDraggable = val;
    }

    /**
     * Add a data flavor for this drag action.
     * 
     * @param flavor the dataflavor of this drag action.
     * @param transferData the object return by this dataflavor.
     */
    public void addDataFlavor(DataFlavor flavor, Object transferData) {
        dataFlavorMap.put(flavor, transferData);
    }

    /**
     * Add a drag source listener for this drag operation.
     * 
     * @param listener the listener to be added
     */
    public void addDragSourceListener(DragSourceListener listener) {
        DragSource.getDefaultDragSource().addDragSourceListener(listener);
    }

    public void dragDropEnd(DragSourceDropEvent event) {
        setCursor(Cursor.getDefaultCursor());
    }

    public void dragEnter(DragSourceDragEvent event) {
    }

    public void dragExit(DragSourceEvent event) {
        BaseDragableMenuItem.this.doClick(0);
    }

    protected Transferable getTransferable() {
        return this;
    }

    public void dragGestureRecognized(DragGestureEvent event) {
        if (this.isEnabled() && this.isVisible() && this.isDraggable()) {
            DragSource.getDefaultDragSource().startDrag(event, mDragCursor, getTransferable(), null);

        }
    }

    public void dragOver(DragSourceDragEvent event) {
    }

    public void dropActionChanged(DragSourceDragEvent event) {
    }

    /**
     * Remove a data flavor for this drag action.
     * 
     * @param flavor the data flavor to be removed.
     */
    public void removeDataFlavor(DataFlavor flavor) {
        dataFlavorMap.remove(flavor);
    }

    public void removeDragSourceListener(DragSourceListener listener) {
        DragSource.getDefaultDragSource().removeDragSourceListener(listener);
    }

    /**
     * Initialize this Object with the following steps: 1. initialize a
     * DragGestureRecognizer to start listener on the gesture. 2. set the default transfer
     * data to this object instance.
     */
    private void init() {
        mRecognizer = DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        dataFlavorMap = Collections.synchronizedMap(new HashMap());
        dataFlavorMap.put(DEFAULT_FLAVORS[0], this);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            // public void mouseExit(MouseEvent event) {
                // BaseDragableMenuItem.this.doClick(0);
                // }
            });
    }
}
