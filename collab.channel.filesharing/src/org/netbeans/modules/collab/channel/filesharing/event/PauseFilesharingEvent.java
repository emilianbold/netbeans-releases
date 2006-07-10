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
package org.netbeans.modules.collab.channel.filesharing.event;

import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;


/**
 * LeaveFilesharing bean
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class PauseFilesharingEvent extends FilesharingEvent {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////

    /* LEAVEFILESHARING ID */
    public static final String EVENT_ID = "PauseFilesharingEvent"; // NOI18N

    /**
     * constructor
     *
     */
    public PauseFilesharingEvent(EventContext evContext) {
        super(evContext);
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
