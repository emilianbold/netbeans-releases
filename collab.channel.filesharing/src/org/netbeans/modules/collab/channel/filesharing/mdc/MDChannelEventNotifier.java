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
package org.netbeans.modules.collab.channel.filesharing.mdc;

import org.openide.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabNotifierConfig;
import org.netbeans.modules.collab.core.Debug;


/**
 * MDC EventNotifier
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class MDChannelEventNotifier extends Object implements EventNotifier {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* EventProcessor */
    EventProcessor eventProcessor = null;

    /* notifier config */
    CollabNotifierConfig enConfig = null;

    /**
     * constructor
     *
     */
    public MDChannelEventNotifier(CollabNotifierConfig enConfig, EventProcessor eventProcessor) {
        super();
        this.enConfig = enConfig;
        this.eventProcessor = eventProcessor;

        //verify both notifier and processor configs
        this.enConfig.verify(
            this.enConfig.getNormalizedEventID(), this.eventProcessor.getConfig().getEventHandlerFactory()
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * exec
     *
     * @param        ce  (collab event)
     */
    public void notify(CollabEvent ce) {
        //TODO - need a better way to get ID for strong type
        String eventBeanID = ce.getID();
        Debug.log(this, "\nMDC EventNotifier, notifying event: [" + eventBeanID + "]");

        try {
            String normalizedEvent = getNormalizedEventID(eventBeanID);

            if (normalizedEvent == null) {
                throw new IllegalArgumentException("No mapping found for event: " + //No18n
                    eventBeanID
                );
            }

            EventContext evContext = ce.getEventContext();
            getEventProcessor().exec(normalizedEvent, evContext);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * setValid
     *
     * @param        valid
     * @throws CollabException
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
     * EventProcessor
     *
     * @return        EventProcessor
     */
    public EventProcessor getEventProcessor() {
        return this.eventProcessor;
    }

    /**
     * getVersion
     *
     * @return        version
     */
    public String getVersion() {
        return enConfig.getVersion();
    }

    /**
     * getNormalizedEventID
     *
     * @return        normalized eventBean ID for given event
     */
    public String getNormalizedEventID(String eventBeanID) {
        return this.enConfig.getNormalizedEventID(eventBeanID);
    }
}
