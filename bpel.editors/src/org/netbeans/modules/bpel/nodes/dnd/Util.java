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
package org.netbeans.modules.bpel.nodes.dnd;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.ErrorManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 24 March 2006
 */
public class Util {

    private Util() {
    }

    public static BpelNode getTransferableBpelNode(Transferable transferable) {
        if (transferable.isDataFlavorSupported(BpelNodeTransferable.bpelNodeDataFlavor)) {
            try {
                return (BpelNode) transferable
                    .getTransferData(BpelNodeTransferable.bpelNodeDataFlavor);
            } catch (UnsupportedFlavorException ex) {
                ex.printStackTrace();
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ex.printStackTrace();
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        return null;
    }
    
    public static BpelEntity getTransferabpleBpelEntity(Transferable transferable) {
        BpelNode bpelNode = getTransferableBpelNode(transferable);
        if (bpelNode != null) {
            Object refObj = bpelNode.getReference();
            if (refObj instanceof BpelEntity) {
                return (BpelEntity) refObj;
            }
        }
        return null;
    }
    
}
