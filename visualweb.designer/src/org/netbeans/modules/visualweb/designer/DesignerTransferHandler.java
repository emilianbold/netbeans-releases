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


package org.netbeans.modules.visualweb.designer;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;


/**
 * TransferHandler for the designer component.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original code from old DndHandler)
 */
class DesignerTransferHandler extends TransferHandler {


    private final WebForm webForm;


    /** Creates a new instance of DesignerTransferHandler */
    public DesignerTransferHandler(WebForm webForm) {
        this.webForm = webForm;
    }


    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        //the following assert is changed by an if statement
        //assert comp == webform.getPane();
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".canImport(JComponent, DataFlavor[])");
        }
        if(comp != webForm.getPane()) {
            throw(new IllegalArgumentException("Wrong component.")); // NOI18N
        }
        if(transferFlavors == null) {
            throw(new IllegalArgumentException("Transferable has not flavors.")); // NOI18N
        }

        // XXX
        return webForm.canImport(comp, transferFlavors);
    }

    public boolean importData(JComponent comp, Transferable t) {
        return webForm.getPane().getDndHandler().importData(comp, t);
    }

}
