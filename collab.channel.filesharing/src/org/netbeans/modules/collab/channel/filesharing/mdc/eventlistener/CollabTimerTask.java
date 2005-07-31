/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
