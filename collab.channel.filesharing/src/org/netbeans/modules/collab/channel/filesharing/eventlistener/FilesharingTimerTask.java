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
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener.CollabTimerTask;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Owner
 */
public class FilesharingTimerTask extends CollabTimerTask implements FilesharingConstants {
    /**
     *
     *
     */
    public FilesharingTimerTask(EventNotifier eventNotifier, CollabEvent event, CollabContext context) {
        super(eventNotifier, event, context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * run
     *
     */
    public void run() {
        getEventNotifier().notify(getEvent());
    }

    /**
     * schedule
     *
     * @param delay
     */
    public void schedule(long delay) {
        try {
            ((FilesharingContext) getContext()).getTimer().schedule(this, delay);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).schedule(this, delay);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot schedule task for delay: " + //NoI18n
                    delay
                );
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).schedule(this, delay);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot schedule task for delay: " + //NoI18n
                    delay
                );
            }
        }
    }

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(long delay, long rate) {
        try {
            ((FilesharingContext) getContext()).getTimer().scheduleAtFixedRate(this, delay, rate);
        } catch (java.lang.IllegalStateException ise) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).scheduleAtFixedRate(this, delay, rate);
            } catch (java.lang.Throwable th) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot scheduleAtFixedRate " + //NoI18n
                    "task for delay: " + delay + ", rate: " + rate
                ); //NoI18n
            }
        } catch (java.lang.Throwable th) {
            try {
                //force creation of timer
                ((FilesharingContext) getContext()).getTimer(true).scheduleAtFixedRate(this, delay, rate);
            } catch (java.lang.Throwable th1) {
                Debug.log(
                    "CollabFileHandlerSupport", //NoI18n
                    "FilesharingTimerTask, timer null, cannot scheduleAtFixedRate " + //NoI18n
                    "task for delay: " + delay + ", rate: " + rate
                ); //NoI18n
            }
        }
    }
}
