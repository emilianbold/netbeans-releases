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


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class EventContext extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* eventID */
    protected String eventID;

    /* event Source */
    protected Object evSource;

    /**
         *
         * @param channel
         */
    public EventContext(String eventID, Object evSource) {
        super();
        this.eventID = eventID;
        this.evSource = evSource;
    }

    /**
     * getSource
     *
     * @return        FileEvent
     */
    public Object getSource() {
        return this.evSource;
    }

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getEventID() {
        return this.eventID;
    }
}
