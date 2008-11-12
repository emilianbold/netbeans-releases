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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;


/**
 * This visitor marks elements that was lost in model that they are deleted.
 * This service is responsible for inner flag isDeleted inside model. When
 * element is disconnected from tree model it should have isDeleted flag equals
 * true. Element could be disconnected from tree after deletion and also after
 * replacing it by another element ( set method ). This flag needs to be set for
 * each child element.
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class SetDeletedService extends InnerEventDispatcherAdapter {


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        boolean flag = event instanceof ArrayUpdateEvent;
        flag = flag || (event instanceof EntityUpdateEvent);
        flag = flag || (event instanceof EntityRemoveEvent);
        return flag;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        BpelEntity entity = null;
        BpelEntity[] entities = null;
        if (event instanceof EntityRemoveEvent) {
            entity = ((EntityRemoveEvent<? extends BpelEntity>) event)
                    .getOldValue();
        }
        else if (event instanceof EntityUpdateEvent) {
            entity = ((EntityUpdateEvent<? extends BpelEntity>) event)
                    .getOldValue();
            if ( entity!= null && entity.getBpelModel().inSync() ) {
                /* 
                 * in process of sync EntityUpdateEvent can appear only when
                 * old element is not removed. It actually is shifted up. 
                 */ 
                  entity = null; // do not do enything.
            }
        }
        else if (event instanceof ArrayUpdateEvent) {
            entities = ((ArrayUpdateEvent<? extends BpelEntity>) event)
                    .getOldArray();
        }

        if (entities != null) {
            for (BpelEntity child : entities) {
                setDeleted(child);
            }
        }
        if (entity != null) {
            setDeleted(entity);
        }
    }

    private void setDeleted( BpelEntity entity ) {
        ((BpelEntityImpl) entity).setDeleted();
        BpelEntity component = (BpelEntity) entity;
        for (BpelEntity child : component.getChildren()) {
            setDeleted(child);
        }
    }

}
