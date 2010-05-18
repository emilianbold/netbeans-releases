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

package org.netbeans.modules.tbls.editor.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Concrete implementation of interface Transferable to transport list of
 * integers representing rows' indices in a table. This class supports
 * MoveableRowTable class where rows are draggable and droppable.
 *
 * @author Bing Lu
 *
 */
class TransferableRowIndices
    implements Transferable {

    /**
     * The supported Data Flavor
     */
    public static final DataFlavor ROW_INDICES_FLAVOR =
        new DataFlavor(java.util.ArrayList.class, "RowIndicesList");

    /**
     * Array of supported Data Flavor
     */
    private static DataFlavor[] mFlavors = {ROW_INDICES_FLAVOR};

    /**
     * List of row indices to transfer
     */
    private ArrayList mIndices;

    /**
     * Constructor for the TransferableRowIndices object
     *
     * @param indices Array of row indices to transfer in a DnD operation
     */
    public TransferableRowIndices(int[] indices) {

        mIndices = new ArrayList();

        for (int i = 0; i < indices.length; i++) {
            mIndices.add(new Integer(indices[i]));
        }
    }

    /**
     * Returns whether or not the specified data flavor is supported for this
     * object.
     *
     * @param flavor the requested flavor for the data
     *
     * @return boolean indicating whether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Arrays.asList(mFlavors).contains(flavor);
    }

    /**
     * Returns an object which represents the data to be transferred. The class
     * of the object returned is defined by the representation class of the
     * flavor.
     *
     * @param flavor the requested flavor for the data
     *
     * @return The transferData value
     *
     * @exception UnsupportedFlavorException if the requested data flavor is
     *            not supported.
     *
     * @see DataFlavor#getRepresentationClass
     */
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException {

        if (flavor.equals(ROW_INDICES_FLAVOR)) {
            return mIndices;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in. The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least
     * descriptive).
     *
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return mFlavors;
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
