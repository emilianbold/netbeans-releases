/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.iep.editor.tcg.table;

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
