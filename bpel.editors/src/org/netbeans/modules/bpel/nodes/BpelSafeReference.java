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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 * This class helps to safe reference to bpelEntity by using uid instead entity instance
 * Copied from BpelNode
 *
 * @author Vitaly Bychkov
 * @version 4 April 2006
 *
 */
public class BpelSafeReference<T extends BpelEntity> {

    private UniqueId myId;
    private BpelModel model;

    public BpelSafeReference(T bpelEntity) {

        myId = bpelEntity.getUID();
        model = bpelEntity.getBpelModel();
    }
    

    public T getBpelObject() {
        BpelEntity bpelEntity = myId == null ? null : model.getEntity(myId);
        return (T)bpelEntity;
    }

    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (obj instanceof BpelSafeReference){
            BpelSafeReference bsf = (BpelSafeReference)obj;
            return myId != null && bsf.myId != null &&
                    this.myId.equals(bsf.myId);
        }
        return false;
    }

    public int hashCode() {
        return myId.hashCode();
    }

}
