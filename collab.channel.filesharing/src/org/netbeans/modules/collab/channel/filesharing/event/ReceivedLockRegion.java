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
package org.netbeans.modules.collab.channel.filesharing.event;

import org.netbeans.modules.collab.channel.filesharing.context.MessageContext;


/**
 * LockRegion bean
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class ReceivedLockRegion extends ReceivedFilesharingMessage {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////

    /* LEAVEFILESHARING ID */
    public static final String EVENT_ID = "ReceivedLockRegion"; // NOI18N

    /**
     * constructor
     *
     */
    public ReceivedLockRegion(MessageContext msgContext) {
        super(msgContext);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID() {
        return getEventID();
    }

    /**
     * getter for ID
     *
     * @return        ID
     */
    public static String getEventID() {
        return EVENT_ID;
    }
}
