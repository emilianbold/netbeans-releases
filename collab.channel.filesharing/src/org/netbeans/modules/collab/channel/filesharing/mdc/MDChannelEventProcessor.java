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
package org.netbeans.modules.collab.channel.filesharing.mdc;

import com.sun.collablet.CollabException;

import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabProcessorConfig;
import org.netbeans.modules.collab.core.Debug;


/**
 * MDC EventProcessor
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class MDChannelEventProcessor extends Object implements EventProcessor {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* eventHandler config */
    CollabProcessorConfig evConfig = null;

    /* context */
    CollabContext context = null;

    /**
     * constructor
     *
     */
    public MDChannelEventProcessor(CollabProcessorConfig evConfig, CollabContext context) {
        super();
        this.evConfig = evConfig;
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getContext
     *
     * @return        context
     */
    public CollabContext getContext() {
        return this.context;
    }

    /**
     * exec
     *
     * @param        eventID
     */
    public void exec(String eventID, EventContext evContext)
    throws CollabException {
        Debug.log(this, "\nMDC EventProcessor, exec event: [" + eventID + "] with event context: " + evContext);

        EventHandler eventHandler = getEventHandler(eventID, getContext());

        if (eventHandler == null) {
            throw new IllegalArgumentException("No eventhandler found for event: " + //No18n
                eventID
            );
        }

        eventHandler.exec(eventID, evContext);
    }

    /**
     * setValid
     *
     * @param        valid
     */
    public void setValid(boolean valid) {
    }

    /**
     * test if the filehandler is valid
     *
     * @return        true/false                                true if valid
     */
    public boolean isValid() {
        return true;
    }

    /**
     * getConfig
     *
     * @return        config
     */
    public CollabProcessorConfig getConfig() {
        return this.evConfig;
    }

    /**
     * getEventHandler
     *
     * @return        eventHandler for given event
     */
    public EventHandler getEventHandler(String eventID, CollabContext context)
    throws CollabException {
        EventHandler eventHandler = this.evConfig.getEventHandlerFactory(eventID).createEventHandler(context);

        return eventHandler;
    }
}
