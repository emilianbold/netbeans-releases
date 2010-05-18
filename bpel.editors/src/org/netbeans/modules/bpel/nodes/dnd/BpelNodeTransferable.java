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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Vitaly Bychkov
 *
 */
public class BpelNodeTransferable extends ExTransferable.Single {
    private BpelNode node;
    public static BpelNodeDataFlavor bpelNodeDataFlavor = new BpelNodeDataFlavor();

    public BpelNodeTransferable(BpelNode node) throws ClassNotFoundException {
        super(bpelNodeDataFlavor);
        this.node = node;
    }
    
    protected Object getData() throws IOException, UnsupportedFlavorException {
        return node;
    }
    
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return dataFlavor.getRepresentationClass() == BpelNode.class;
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {bpelNodeDataFlavor};
    }
    
    private static class BpelNodeDataFlavor extends DataFlavor {
        public BpelNodeDataFlavor() {
            super(BpelNode.class,"Bpel Node flavor"); // NOI18N
        }
    }
}
