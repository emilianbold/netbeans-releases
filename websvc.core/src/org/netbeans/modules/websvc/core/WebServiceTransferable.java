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
/*
 * WebServiceTransferable.java
 *
 * Created on August 23, 2006, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author rico
 */
public class WebServiceTransferable implements Transferable{
    private WebServiceReference ref;
    
    public static final DataFlavor WS_FLAVOR =
        new DataFlavor(WebServiceReference.class, "webservice ref");
    
    /** Creates a new instance of WebServiceTransferable */
    public WebServiceTransferable(WebServiceReference ref) {
        this.ref = ref;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return WS_FLAVOR.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (WS_FLAVOR.equals(flavor)) {
            return ref;
        } 
        throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[] {
            WS_FLAVOR
        };
    }
    
}
