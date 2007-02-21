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
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Vitaly Bychkov
 * @version 24 March 2006
 */
public abstract class BpelElementPasteType<P,C> extends PasteType {
    private P parentBpelEntity;
    private int placeIndex = -1; // -1 means no idea about places inside childrens
    private C transferedBpelEntity;
    private int dndAction = DnDConstants.ACTION_COPY;
    
    /*
     * @param transferable transferable object
     */
    public BpelElementPasteType(P parent,
        C transferedEntity,
        int i0)
        /* throws UnsupportedFlavorException, IOException */{
        this.parentBpelEntity = parent;
        this.transferedBpelEntity = transferedEntity;
        this.placeIndex = -1;
    }
    
    public BpelElementPasteType(P parent,
        C transferedEntity) {
        this(parent,transferedEntity,-1);
    }
    
    /*
     * @return transferable which should be inserted into the clipboard after the
     * paste action. It can be null, meaning that the clipboard content is not affected.
     */
    public Transferable paste() throws IOException {
        try {
            if (getParentEntity() instanceof BpelEntity) {
                ((BpelEntity)getParentEntity()).getBpelModel().invoke(new Callable<Object>() {
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
            } else {
                if (getDndAction() == DnDConstants.ACTION_MOVE) {
                    moveEntity();
                } else if (getDndAction() == DnDConstants.ACTION_COPY) {
                    copyEntity();
                } else if (getDndAction() == DnDConstants.ACTION_LINK) {
                    linkEntity();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.getMessage());
        }
        return ExTransferable.EMPTY;
        
    }
    
    protected void moveEntity() {
        throw new UnsupportedOperationException();
    }
    
    protected void copyEntity() {
        throw new UnsupportedOperationException();
    }
    
    protected void linkEntity() {
        throw new UnsupportedOperationException();
    }
    
    public int[] getSupportedDnDOperations() {
        return new int[] {DnDConstants.ACTION_COPY,
        DnDConstants.ACTION_MOVE,
        DnDConstants.ACTION_REFERENCE
        };
    }
    
    public boolean isSupportedDnDOperations(int operation) {
        int[] supportedOps = getSupportedDnDOperations();
        if (operation < 1 || supportedOps == null || supportedOps.length == 0) {
            return false;
        }
        for (int elem : supportedOps) {
            if (elem == operation) {
                return true;
            }
        }
        
        return false;
    }
    
    public String getName() {
        return "BpelEntity Paste Type Name"; // NOI18N
    }
    
    public boolean isSupportedChildIndex(int i) {
        return true;
    }
    
    public P getParentEntity() {
        return parentBpelEntity;
    }
    
    public C getTransferedEntity() {
        return transferedBpelEntity;
    }
    
    public int getPlaceIndex() {
        return placeIndex;
    }
    
    public void setPlaceIndex(int placeIndex) {
        this.placeIndex = placeIndex;
    }
    
    public void setDndAction(int action) {
        switch (action) {
            case DnDConstants.ACTION_COPY:
            case DnDConstants.ACTION_MOVE:
            case DnDConstants.ACTION_REFERENCE:
                dndAction = action;
                return;
        }
    }
    
    public int getDndAction() {
        return dndAction;
    }
}
