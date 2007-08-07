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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.designer.jsf.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.modules.visualweb.designer.jsf.DesignerServiceHackImpl;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

class CreatorPaletteInsertAction extends AbstractAction {
    
    private Lookup item;
    private Transferable itemTransfer;
    
    CreatorPaletteInsertAction(Lookup item) {
        this.item = item;
    }
    
    public void actionPerformed(ActionEvent e) {
        Node itemNode = item.lookup(Node.class);
        try {
            itemTransfer = itemNode.clipboardCopy();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        
        if (canDrop(itemTransfer)) {
            //                    DesignerServiceHackProviderImpl.drop(itemTransfer);
            DesignerServiceHackImpl.getDefault().drop(itemTransfer);
        }
    }
    
    private boolean canDrop( Transferable itemTransfer ){
        DataFlavor[] flavors = itemTransfer.getTransferDataFlavors();
        for (DataFlavor flavor : flavors){
            //                    if ( DesignerServiceHackProviderImpl.canDrop(flavors[i])){
            if (DesignerServiceHackImpl.getDefault().canDrop(flavor)) {
                return true;
            }
        }
        return false;
    }
}

