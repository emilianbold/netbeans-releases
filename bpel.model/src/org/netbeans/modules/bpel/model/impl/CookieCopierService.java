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
import org.netbeans.modules.bpel.model.api.events.OutOfModelEvent;
import org.netbeans.modules.bpel.model.impl.events.CutEvent;
import org.netbeans.modules.bpel.model.impl.services.InnerEventDispatcherAdapter;

/**
 * @author ads This is service for cookie copying from old element to cut
 *         element.
 */
public class CookieCopierService extends InnerEventDispatcherAdapter {


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        return event instanceof CutEvent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.soa.model.bpel20.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        BpelEntity old = event.getParent();
        BpelEntity newEntity = ((OutOfModelEvent) event).getOutOfModelEntity();
        setCookie(old, newEntity);
    }

    private void setCookie( BpelEntity old, BpelEntity newEntity ) {
        Map<Object, Object> map = ((BpelEntityImpl) old).getCookies();
        ((BpelEntityImpl) newEntity).setCookies(map);

        BpelEntity component = (BpelEntity) old;
        List<BpelEntity> list = ((BpelEntity) newEntity).getChildren();
        BpelEntity[] children = null;
        if (list.size() > 0) {
            children = list.toArray(new BpelEntity[list.size()]);
        }
        int i = 0;
        for (BpelEntity comp : component.getChildren()) {
            setCookie(comp, children[i]);
            i++;
        }
    }
    
}
