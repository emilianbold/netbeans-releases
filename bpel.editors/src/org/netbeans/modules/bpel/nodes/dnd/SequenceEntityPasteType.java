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

import java.awt.dnd.DnDConstants;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 *
 * @author Vitaly Bychkov
 * @version 24 March 2006
 *
 */
public class SequenceEntityPasteType extends BpelEntityPasteType<BpelEntity, BpelEntity> {
    public SequenceEntityPasteType(BpelEntity parent,
        BpelEntity transferedEntity) {
        super(parent,transferedEntity);
    }

    public int[] getSupportedDnDOperations() {
        return new int[] {
            DnDConstants.ACTION_MOVE
        };
    }
    
    protected void moveEntity() {
        BpelEntity cuttedEntity = getTransferedEntity().cut();
        if (getParentEntity() instanceof BaseScope
            && cuttedEntity instanceof Activity) {
            ((BaseScope)getParentEntity()).setActivity((Activity)cuttedEntity);
        }
    }
    
}
