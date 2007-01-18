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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;


/**
 * Provide a class supporting Drag and Drop for Session and Entity Beans.
 * @author  Chris Webster
 */
public class EjbTransferable implements Transferable {
    private static final DataFlavor TEXT_FLAVOR =
        new DataFlavor("text/plain; charset=unicode", null);
    public static final DataFlavor EJB_FLAVOR =
        new DataFlavor(EjbReference.class, "ejb ref");
    
    private final String stringRep;
    private final EjbReference ref;
    
    public EjbTransferable(String stringRep, EjbReference ref) {
        this.stringRep = stringRep;
        this.ref = ref;
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (TEXT_FLAVOR.equals(flavor)) {
            return stringRep;
        } else if (EJB_FLAVOR.equals(flavor)) {
            return ref;
        }
        throw new UnsupportedFlavorException(flavor);
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {
            TEXT_FLAVOR,
            EJB_FLAVOR
        };
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return TEXT_FLAVOR.equals(flavor) ||
        EJB_FLAVOR.equals(flavor);
    }
}
