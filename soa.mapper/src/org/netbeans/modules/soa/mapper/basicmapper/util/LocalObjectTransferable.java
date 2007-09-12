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

package org.netbeans.modules.soa.mapper.basicmapper.util;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 *
 * Title: A transferable data class </p> <p>
 *
 * Description: This class implements Transferable interface to provide a simple
 * transferable data object that can be set. The class return a default jvm
 * local object as its DataFlavor. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class LocalObjectTransferable
     implements Transferable {
    /**
     * The jvm local object data flavor.
     */
    private static DataFlavor mLocalObjectDataFlavor = null;

    /**
     * the object to transfer.
     */
    private Object mData;

    private static final Logger LOGGER = Logger.getLogger(LocalObjectTransferable.class.getName());

    static {
        try {
            mLocalObjectDataFlavor =
                new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, 
                "This should not happen! "
                + "Can't found DataFlavor.javaJVMLocalObjectMimeType.", e);
        }
    }


    /**
     * Creates a new LocalObjectTransferable object.
     */
    public LocalObjectTransferable() {
        this(null);
    }


    /**
     * Creates a new LocalObjectTransferable object.
     *
     * @param transferData  DOCUMENT ME!
     */
    public LocalObjectTransferable(Object transferData) {
        super();
        mData = transferData;
    }


    /**
     * Get the transferalbe object by the specified data flavor. Or null if the
     * data flavor is not <code>DataFlavor.javaJVMLocalObjectMimeType</code>.
     *
     * @param flavor  Description of the Parameter
     * @return        The transferData value
     */
    public Object getTransferData(DataFlavor flavor) {
        if (!isDataFlavorSupported(flavor)) {
            return null;
        }

        return mData;
    }


    /**
     * Get the DataFlavor of this transferable data. This method returns a
     * single element DataFlavor array contains a <code>DataFlavor.javaJVMLocalObjectMimeType</code>
     * DataFlavor.
     *
     * @return   a single <code>DataFlavor.javaJVMLocalObjectMimeType</code>
     *      DataFlavor element array.
     */
    public DataFlavor[] getTransferDataFlavors() {
        try {
            return new DataFlavor[]{mLocalObjectDataFlavor};
        } catch (Exception exc) {
            LOGGER.log(Level.SEVERE, "cannot create new data flavor.", exc);
        }

        return null;
    }


    /**
     * Return true if this transferable data supports this DataFlavor. This
     * method return true only if the DataFlavor is a <code>DataFlavor.javaJVMLocalObjectMimeType</code>
     * DataFlavor.
     *
     * @param flav  the DataFlavor to check.
     * @return      true if true the DataFlavor is a <code>DataFlavor.javaJVMLocalObjectMimeType</code>
     *      DataFlavor, false otherwise.
     */
    public boolean isDataFlavorSupported(DataFlavor flav) {
        return flav.equals(mLocalObjectDataFlavor);
    }


    /**
     * Set the object to be transfered.
     *
     * @param data  the object to be transfered.
     */
    public void setTransferData(Object data) {
        mData = data;
    }
}
