/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

