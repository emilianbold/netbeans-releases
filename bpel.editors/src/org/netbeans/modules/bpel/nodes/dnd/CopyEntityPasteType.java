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

import java.util.List;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class CopyEntityPasteType extends BpelEntityPasteType<Assign, Copy> {
    
    public CopyEntityPasteType(Assign parent,Copy transferedEntity,int i0) {
        super(parent, transferedEntity, i0);
    }
    
    public CopyEntityPasteType(Assign parent,Copy transferedEntity) {
        super(parent, transferedEntity);
    }
    
    protected void moveEntity() {
        Copy cuttedEntity = (Copy) getTransferedEntity().cut();
        int index = getPlaceIndex();
        List<BpelEntity> childs = getParentEntity().getChildren();
        if (index > childs.size()) {
            return;
        }
        if (childs == null || childs.size() < 1 || index == -1) {
            getParentEntity().addAssignChild(cuttedEntity);
        } else {
            getParentEntity().insertAssignChild(cuttedEntity,index);
        }
    }
}
