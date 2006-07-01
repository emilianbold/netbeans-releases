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
 */
package org.openide.util.datatransfer;

import java.awt.datatransfer.*;

import java.io.IOException;


/** Interface for transferring multiple objects at once.
*
* @author Jaroslav Tulach
* @version 0.12 Dec 16, 1997
*/
public interface MultiTransferObject {
    /** Get the number of transferred elements.
    * @return the count
    */
    public int getCount();

    /**  Get the transferable at some index.
     * @param index the index
     * @return the transferable
     */
    public Transferable getTransferableAt(int index);

    /** Test whether a given data flavor is supported by the item at <code>index</code>.
    *
    * @param index the index
    * @param flavor the flavor to test
    * @return <CODE>true</CODE> if the flavor is supported
    */
    public boolean isDataFlavorSupported(int index, DataFlavor flavor);

    /** Test whether each transferred item supports at least one of these
    * flavors. Different items may support different flavors, however.
    * @param array array of flavors
    * @return <code>true</code> if all items support one or more flavors
    */
    public boolean areDataFlavorsSupported(DataFlavor[] array);

    /** Get list of all supported flavors for the item at an index.
    * @param i the index
    * @return array of supported flavors
    */
    public DataFlavor[] getTransferDataFlavors(int i);

    /** Get transfer data for the item at some index.
    * @param indx the index
    * @param flavor the flavor desired
    * @return transfer data for item at that index
    * @throws IOException if there is an I/O problem
    * @throws UnsupportedFlavorException if that flavor is not supported
    */
    public Object getTransferData(int indx, DataFlavor flavor)
    throws UnsupportedFlavorException, IOException;
}
