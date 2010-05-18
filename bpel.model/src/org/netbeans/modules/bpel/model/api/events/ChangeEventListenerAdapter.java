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
package org.netbeans.modules.bpel.model.api.events;

/**
 * This is adapter class. One may override methods that needed. All other
 * methods delegates call to notifyEvent that don't have any implementation. If
 * one don't want to distinguish events he may just override notifyEvent. Either
 * he can override some of public methods and don't override notifyEvent.
 *
 * @author ads
 */
public class ChangeEventListenerAdapter implements ChangeEventListener {

    /*
     * (non-Javadoc)
     *
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyPropertyRemoved(org.netbeans.modules.soa.model.bpel.api.events.PropertyRemoveEvent)
     */
    /** {@inheritDoc} */
    public void notifyPropertyRemoved( PropertyRemoveEvent event ) {
        notifyEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyEntityInserted(org.netbeans.modules.soa.model.bpel.api.events.EntityInsertEvent)
     */
    /** {@inheritDoc} */
    public void notifyEntityInserted( EntityInsertEvent event ) {
        notifyEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyPropertyUpdated(org.netbeans.modules.soa.model.bpel.api.events.PropertyUpdateEvent)
     */
    /** {@inheritDoc} */
    public void notifyPropertyUpdated( PropertyUpdateEvent event ) {
        notifyEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyEntityRemoved(org.netbeans.modules.soa.model.bpel.api.events.EntityRemoveEvent)
     */
    /** {@inheritDoc} */
    public void notifyEntityRemoved( EntityRemoveEvent event ) {
        notifyEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyEntityUpdated(org.netbeans.modules.soa.model.bpel.api.events.EntityUpdateEvent)
     */
    /** {@inheritDoc} */
    public void notifyEntityUpdated( EntityUpdateEvent event ) {
        notifyEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.events.ChangeEventListener#notifyArrayUpdated(org.netbeans.modules.soa.model.bpel.api.events.ArrayUpdateEvent)
     */
    /** {@inheritDoc} */
    public void notifyArrayUpdated( ArrayUpdateEvent event ) {
        notifyEvent(event);
    }

    /**
     * This is method that is used for delegation from each listener methods.
     * 
     * @param event
     *            notification event.
     */
    protected void notifyEvent( ChangeEvent event ) {
    }
}
