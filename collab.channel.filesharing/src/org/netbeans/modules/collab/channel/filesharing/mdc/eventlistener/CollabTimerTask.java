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
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;


/**
 *
 * @author  Owner
 */
public abstract class CollabTimerTask extends TimerTask {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* context */
    protected CollabContext context = null;

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /* event */
    protected CollabEvent event;

    /**
     *
     *
     */
    public CollabTimerTask(EventNotifier eventNotifier, CollabEvent event, CollabContext context) {
        super();
        this.eventNotifier = eventNotifier;
        this.event = event;
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * getEvent
     *
     * @return event
     */
    public CollabEvent getEvent() {
        return this.event;
    }

    /**
     * getEventNotifier
     *
     * @return eventNotifier
     */
    public EventNotifier getEventNotifier() {
        return this.eventNotifier;
    }

    /**
     * getContext
     *
     * @return context
     */
    public CollabContext getContext() {
        return this.context;
    }

    /**
     * run
     *
     */
    public abstract void run();

    /**
     * schedule
     *
     * @param delay
     */
    public abstract void schedule(long delay);

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public abstract void scheduleAtFixedRate(long delay, long rate);
}
