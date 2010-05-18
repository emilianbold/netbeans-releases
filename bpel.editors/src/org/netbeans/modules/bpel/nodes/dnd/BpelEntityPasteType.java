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
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Vitaly Bychkov
 * @version 24 March 2006
 */
public abstract class BpelEntityPasteType<P extends BpelEntity, C extends BpelEntity>
        extends BpelElementPasteType<P, C>
{
    public BpelEntityPasteType(P parent,
        C transferedEntity,
        int i0)
        /* throws UnsupportedFlavorException, IOException */{
        super(parent, transferedEntity, i0);
    }
    
    public BpelEntityPasteType(P parent,
        C transferedEntity) {
        super(parent,transferedEntity);
    }
    
  /*
   * @return transferable which should be inserted into the clipboard after the
   * paste action. It can be null, meaning that the clipboard content is not affected.
   */
    public Transferable paste() throws IOException {
        try {
            getParentEntity().getBpelModel().invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    if (getDndAction() == DnDConstants.ACTION_MOVE) {
                        moveEntity();
                    } else if (getDndAction() == DnDConstants.ACTION_COPY) {
                        copyEntity();
                    } else if (getDndAction() == DnDConstants.ACTION_LINK) {
                        linkEntity();
                    }
                    
                    return null;
                }
            }, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
        return ExTransferable.EMPTY;
        
    }
    
    public String getName() {
        return "BpelEntity Paste Type Name"; // NOI18N
    }
}
