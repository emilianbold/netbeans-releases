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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;


/**
 * @author ads
 *
 * This service set flag for element that means element was built via builder.
 */
public class ResetBuilderElement extends InnerEventDispatcherAdapter {


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        boolean flag = event instanceof EntityInsertEvent;
        flag = flag || event instanceof ArrayUpdateEvent;
        flag = flag || event instanceof EntityUpdateEvent;
        return flag && (!event.getParent().getBpelModel().inSync());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        if ( event instanceof EntityInsertEvent ) {
            ((EntityInsertEvent)event).getValue().removeCookie( 
                    MarkBuilderElement.getMark() );
        }
        else if ( event instanceof EntityUpdateEvent ) {
            ((EntityUpdateEvent)event).getNewValue().removeCookie( 
                    MarkBuilderElement.getMark() ); 
        }
        else if ( event instanceof ArrayUpdateEvent ) {
           BpelEntity[] entities = ((ArrayUpdateEvent)event).getNewArray();
           for (BpelEntity entity : entities) {
               entity.removeCookie( MarkBuilderElement.getMark() );
           }
        }
    }

}
