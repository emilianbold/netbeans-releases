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
package org.netbeans.modules.bpel.model.impl.services;

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.impl.events.TreeCreatedEvent;

/**
 * This service is used for initilizing references objects inside of
 * ReferenceCollection Objects. This initialization will appear automatically
 * when one call method getXXXObject for ReferenceCollection class, but this
 * method could be not called and antoher service ( that is responsible for
 * reference integrity will not update correctly references ).
 * 
 * @author ads
 */
public class ReferenceInitilizerService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        return event.getClass().equals(TreeCreatedEvent.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.xdm.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        BpelEntity entity = event.getParent();
        initRefs(entity);
    }

    private void initRefs( BpelEntity entity ) {
        if (entity instanceof ReferenceCollection) {
            ((ReferenceCollection) entity).getReferences();
        }
        List<BpelEntity> children = ((BpelEntity) entity).getChildren();
        for (BpelEntity component : children) {
            initRefs(component);
        }
    }

}
