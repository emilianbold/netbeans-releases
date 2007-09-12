/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
