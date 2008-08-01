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

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Immutable holder of the current state of the BPEL mapper.
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 */
public interface BpelDesignContext {
    
    Node getActivatedNode();
    Lookup getLookup();
    BpelModel getBpelModel();
    BpelEntity getContextEntity();
    BpelEntity getGraphEntity();
    BpelEntity getSelectedEntity();
  
    VisibilityScope getVisibilityScope();
    
    StringBuffer getValidationErrMsgBuffer();

/*
    private WeakReference<BpelEntity> mEntityRef;
    private WeakReference<Node> mActivatedNode;
    private Lookup mLookup;
    
    public BpelDesignContext(BpelEntity entity, Node node, Lookup lookup) {
        mEntityRef = new WeakReference<BpelEntity>(entity);
        mActivatedNode = new WeakReference<Node>(node);
        mLookup = lookup;
    }

    public BpelEntity getBpelEntity() {
        return mEntityRef.get();
    }
    
    public Node getActivatedNode() {
        return mActivatedNode.get();
    }
    
    public Lookup getLookup() {
        return mLookup;
    }
    
    public BpelModel getBpelModel() {
        if(mEntityRef != null) {
            BpelEntity entity = mEntityRef.get();
            if (entity != null) {
                return entity.getBpelModel();
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "BpelEntity: " + mEntityRef + " Lookup: " + mLookup;
    }
    
    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof BpelDesignContext) {
            BpelDesignContext otherContext = (BpelDesignContext)otherObj;
            if (otherContext.getBpelEntity() == getBpelEntity() && 
                    otherContext.getLookup() == getLookup()) {
                return true;
            }
        }
        return false;
    }
 */ 
}
