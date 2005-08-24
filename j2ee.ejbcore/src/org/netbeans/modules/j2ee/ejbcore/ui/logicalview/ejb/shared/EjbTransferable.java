/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    
    private String stringRep;
    private EjbReference ref;
    
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
