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

package org.netbeans.spi.viewmodel;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.openide.util.datatransfer.PasteType;


/**
 * Provides extension to {@link NodeModelFilter},
 * filters content of some existing {@link ExtendedNodeModel}.
 *
 * @author   Martin Entlicher
 * @since 1.12
 */
public interface ExtendedNodeModelFilter extends NodeModel {
    
    /**
     * Test whether this node permits renaming.
     * @return <code>true</code> if so
     */
    public abstract boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException;

    /**
     * Test whether this node permits copying.
     * @return <code>true</code> if so
     */
    public abstract boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException;

    /**
     * Test whether this node permits cutting.
     * @return <code>true</code> if so
     */
    public abstract boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException;

    /**
     * Called when a node is to be copied to the clipboard.
     * @param node The node object
     * @return the transferable object representing the content of the clipboard
     * @exception IOException when the copy cannot be performed
     */
    public abstract Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException;

    /**
     * Called when a node is to be cut to the clipboard.
     * @param node The node object
     * @return the transferable object representing the content of the clipboard
     * @exception IOException when the cut cannot be performed
     */
    public abstract Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException;

    /**
     * Called when a drag is started with this node.
     * The node can attach a transfer listener to ExTransferable and
     * will be then notified about progress of the drag (accept/reject).
     *
     * @param node The node object
     * @return transferable to represent this node during a drag
     * @exception IOException if a drag cannot be started
     *
    public abstract Transferable drag(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException;
     */

    /**
     * Determine which paste operations are allowed when a given transferable is in the clipboard.
     * For example, a node representing a Java package will permit classes to be pasted into it.
     * @param node The node object
     * @param t the transferable in the clipboard
     * @return array of operations that are allowed
     */
    public abstract PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException;

    /** Determine if there is a paste operation that can be performed
     * on provided transferable. Used by drag'n'drop code to check
     * whether the drop is possible.
     *
     * @param node The node object
     * @param t the transferable
     * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
     * @param index index between children the drop occurred at or -1 if not specified
     * @return null if the transferable cannot be accepted or the paste type
     *    to execute when the drop occurs
     *
    public abstract PasteType getDropType(ExtendedNodeModel original, Object node, Transferable t, int action, int index) throws UnknownTypeException;
     */

    /**
     * Sets a new name for given node.
     *
     * @param node The object to set the new name to.
     * @param name The new name for the given node
     */
    public abstract void setName (ExtendedNodeModel original, Object node, String name) throws UnknownTypeException;

    /**
     * Returns icon resource with extension for given node.
     * This is the preferred way of icon specification over {@link org.netbeans.spi.viewmodel.NodeModel.getIconBase}
     *
     * @param node The node object
     * @return The base resouce name with extension (no initial slash)
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     */
    public abstract String getIconBaseWithExtension (ExtendedNodeModel original, Object node) throws UnknownTypeException;
    
}
