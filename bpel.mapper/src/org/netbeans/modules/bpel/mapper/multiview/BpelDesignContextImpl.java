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

import java.lang.ref.WeakReference;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Immutable holder of the current state of the BPEL mapper.
 * 
 * @author Vitaly Bychkov
 */
public class BpelDesignContextImpl implements BpelDesignContext {

    private WeakReference<Node> mActivatedNode;
    private Lookup mLookup;
    private WeakReference<BpelEntity> mSelectedEntityRef;
    private WeakReference<BpelEntity> mGraphEntityRef;
    private WeakReference<BpelEntity> mContextEntityRef;
    private String mMessage;
    private VisibilityScope mVisibilityScope;
    private StringBuffer validationErrMsgBuffer = new StringBuffer();

    public BpelDesignContextImpl(BpelEntity contextEntity, 
            BpelEntity graphEntity, BpelEntity selectedEntity, 
            Node node, Lookup lookup) {
        mActivatedNode = new WeakReference<Node>(node);
        mLookup = lookup;
        mContextEntityRef = new WeakReference<BpelEntity>(contextEntity);
        mGraphEntityRef = new WeakReference<BpelEntity>(graphEntity);
        mSelectedEntityRef = new WeakReference<BpelEntity>(selectedEntity);
    }

    public BpelDesignContextImpl(String message) {
        this(null, null, null, null, null);
        mMessage = message;
    }

    public BpelEntity getContextEntity() {
        return mContextEntityRef.get();
    }

    public BpelEntity getGraphEntity() {
        BpelEntity result = mGraphEntityRef.get();
        if (result != null) {
            if (result.isInDocumentModel()) {
                return result;
            }
        }
        return getContextEntity();
    }

    public BpelEntity getSelectedEntity() {
        BpelEntity result = mSelectedEntityRef.get();
        if (result != null) {
            if (result.isInDocumentModel()) {
                return result;
            }
        }
        return getGraphEntity();
    }

    public Node getActivatedNode() {
         return mActivatedNode.get();
    }

    public Lookup getLookup() {
        return mLookup;
    }

    public BpelModel getBpelModel() {
        if(mContextEntityRef != null) {
            BpelEntity entity = mContextEntityRef.get();
            if (entity != null) {
                return entity.getBpelModel();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ContextEntity: " + mContextEntityRef + 
                "GraphEntity: " + mGraphEntityRef + 
                "SelectedEntity: " + mSelectedEntityRef + 
                " Lookup: " + mLookup; // NOI18N
    }
    
    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof BpelDesignContext) {
            BpelDesignContext otherContext = (BpelDesignContext)otherObj;
            if (otherContext.getContextEntity() == getContextEntity() 
                    && otherContext.getGraphEntity() == getGraphEntity() 
                    && otherContext.getSelectedEntity() == getSelectedEntity() 
                    && otherContext.getLookup() == getLookup()) 
            {
                return true;
            }
        }
        return false;
    }

    public String getMessage() {
        return mMessage;
    }

    public synchronized VisibilityScope getVisibilityScope() {
        if (mVisibilityScope == null) {
            mVisibilityScope = new VisibilityScope(getSelectedEntity());
        }
        return mVisibilityScope;
    }

    public StringBuffer getValidationErrMsgBuffer() {
        return validationErrMsgBuffer;
    }
}