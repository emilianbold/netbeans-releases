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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.api.model;

import java.util.Collection;

/**
 * This interface is used for notifying and processing event firing.
 * For registering the implemenation see AccessControllerFactory interface.
 * <p>
 * Event firing is following these steps: For all registered AccessController classes a writeAccess method is called
 * by nested calls (passing the calls using Runnable). In the deepest Runnable: notifyEventFiring method is called
 * on all registered AccessController classes, then performed all event firing for all listeners, and finally
 * notifyEventFired method is called on all registered AccessController classes.
 * <p>
 * This interface is used to listen on document change and you need to lock additional lock or mutex
 * or some initialization/finalization code must performed before/after the event firing.
 *
 * @author David Kaspar
 */
public interface AccessController {

    /**
     * Called when the event-firing is going to happen. The firing is performed by a Runnable.
     * Using this method you are able to wrap the runnable code into your and perform your code before and after.
     * Usually this is used for performing event firing in write access on a mutex.
     * <p>
     * Note: Runnable.run method that is passed as an argument must be executed exactly once.
     * @param runnable the runnable
     */
    public void writeAccess (Runnable runnable);

    /**
     * This is called immediately before an event is fired. At that time writeAccess method is called on all AccessControllers.
     * @param event the event that is going to be fired
     */
    public void notifyEventFiring (DesignEvent event);

    /**
     * This is called immediately after an event is fired. At that time writeAccess method is called on all AccessControllers.
     * @param event the event that was fired
     */
    public void notifyEventFired (DesignEvent event);

    /**
     * This is called during event firing process to notify that new components are created in a document.
     * At the time of this method is called, all presenters are notified about their removing/adding and no event is fired
     * to any design listener yet.
     * @param createdComponents the newly created components
     */
    public void notifyComponentsCreated (Collection<DesignComponent> createdComponents);

}
