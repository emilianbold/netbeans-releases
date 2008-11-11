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

import java.util.List;
import java.util.Map;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.impl.events.BuildEvent;
import org.netbeans.modules.bpel.model.impl.events.CopyEvent;
import org.netbeans.modules.bpel.model.impl.events.CutEvent;
import org.netbeans.modules.bpel.model.impl.events.TreeCreatedEvent;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;


/**
 * This is unique id service. It created new ids for just created model, new ids
 * for copied element, set the same id references for cut entity.
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class UIDCreationService extends InnerEventDispatcherAdapter {


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        if ( event instanceof TreeCreatedEvent ){
            return true;
        }
        BpelEntity parent = event.getParent();
        if ( parent!=null ){
            // we need to generate id when model is syncing from source for added/inserted elements.
            return parent.getBpelModel().inSync() && ( 
                    event instanceof EntityInsertEvent || 
                    event instanceof EntityUpdateEvent );
        }
        return false;    
    }
    
    
    @Override
    public void preDispatch( ChangeEvent event )  {
        if ( event.getClass().equals( EntityInsertEvent.class)){
            createUid( null , 
                    (( EntityInsertEvent<? extends BpelEntity>)event).getValue(),
                    null);
        }
        if ( event.getClass().equals( EntityUpdateEvent.class)){
            createUid( null , 
                    (( EntityUpdateEvent<? extends BpelEntity>)event).getNewValue(),
                    null);
        }
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        BpelEntity old = null;
        BpelEntity newEntity = null;
        Map map = null;
        if (event.getClass().equals(CutEvent.class)) {
            old = event.getParent();
            newEntity = ((CutEvent<? extends BpelEntity>) event)
                    .getOutOfModelEntity();
        }
        else if (event.getClass().equals(CopyEvent.class)) {
            old = event.getParent();
            newEntity = ((CopyEvent<? extends BpelEntity>) event)
                    .getOutOfModelEntity();
            map = (Map) newEntity.getCookie(BpelEntityImpl.IdMapKey.class);
        }
        else if (event.getClass().equals(TreeCreatedEvent.class)) {
            newEntity = event.getParent();
        }
        else if (event.getClass().equals(BuildEvent.class)) {
            newEntity = ((BuildEvent<? extends BpelEntity>) event).getParent();
        }
        else {
            return;
        }
        createUid(old, newEntity, map);
    }

    @SuppressWarnings("unchecked")
    private void createUid( BpelEntity old, BpelEntity newEntity, Map map ) {
        if ((old == null) || (map != null)) {
            if ( newEntity.getUID()!=null ){

                assert newEntity.getBpelModel().inSync();
                /* 
                 * if this is update from source then EntityUpdateEvent
                 * could be generated when regular element was deleted
                 * and its place was occupied by nonregular element.
                 * Such element already have ID. 
                 */ 
                return;
            }
            // this is the case when element just created via builder or
            // it is copied or event generated while syncing with source.

            BpelModelImpl modelImpl = (BpelModelImpl) newEntity.getBpelModel();
            long id = modelImpl.getNextID();
            UniqueIdImpl newId = new UniqueIdImpl((BpelEntityImpl) newEntity,
                    id);
            ((BpelEntityImpl) newEntity).setUID(newId);
            if (map != null) {// copy case.
                assert old != null;
                UniqueId uid = old.getUID();
                map.put(uid, newId);
            }
        }
        else {// this is "cut" case.
            UniqueId id = old.getUID();
            ((BpelEntityImpl) newEntity).setUID(id);
            (( UniqueIdImpl) id).setEntity( (BpelEntityImpl)newEntity );
        }
        BpelEntity component = (BpelEntity) newEntity;
        BpelEntity[] oldChildren = null;
        if (old != null) {
            List<? extends BpelEntity> list = ((BpelEntity) old)
                    .getChildren();
            oldChildren = list.toArray(new BpelEntity[list.size()]);
        }
        int i = 0;
        for (BpelEntity comp : component.getChildren()) {
            if (old != null) {
                assert oldChildren != null;
                createUid(oldChildren[i], comp, map);
            }
            else {
                createUid(null, comp, map);
            }
            
            i++;
        }
    }

}
