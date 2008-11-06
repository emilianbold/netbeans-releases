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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;

/**
 * This special service dispatch ArrayUpdateEvent. The situation with this event
 * is complecated then other events. When one set array then new childrne array
 * may contains children that already exist in the same parent. In this case
 * such elements will be cloned and their copies will be set. In this case we
 * need to set old content to new element. ( cookies , the same id's ).
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class ArraySetterService extends InnerEventDispatcherAdapter {

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        return event.getClass().equals(ArrayUpdateEvent.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        ArrayUpdateEvent<? extends BpelEntity> arrayEvent = 
            (ArrayUpdateEvent<? extends BpelEntity>) event;
        BpelEntity[] old = arrayEvent.getOldArray();
        for (BpelEntity entity : old) {
            Object obj = entity.getCookie(BpelContainerImpl.CopyKey.class);
            if (obj != null) {
                // need to set cookie , id's from old to new "obj" entity
                assert obj instanceof BpelEntity;
                copyContent(entity, (BpelEntity) obj);
            }
        }
    }

    @SuppressWarnings("unchecked") // NOI18N
    private void copyContent( BpelEntity orig, BpelEntity copy )
    {

        // set the same uid after copy.
        UniqueId id = orig.getUID();
        ((BpelEntityImpl) copy).setUID(id);

        // clone hashmap , remove internalally used key CopyKey.class and set to
        // new element.
        Map<Object, Object> map = ((BpelEntityImpl) orig).getCookies();
        assert map instanceof HashMap;
        map = (Map) ((HashMap<Object, Object>) map).clone();
        map.remove(BpelContainerImpl.CopyKey.class);
        ((BpelEntityImpl) copy).setCookies(map);

        // recursively proceed with children.
        BpelEntity component = (BpelEntity) orig;
        if (component.getChildren().size() > 0) {
            int i = 0;
            List<BpelEntity> copyChildren = ((BpelEntity) copy).getChildren();
            BpelEntity[] childOfCopy = copyChildren
                    .toArray(new BpelEntity[copyChildren.size()]);
            for (BpelEntity child : component.getChildren()) {
                copyContent(child, childOfCopy[i]);
                i++;
            }
        }
    }
}
