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
package org.netbeans.modules.collab.channel.filesharing;

import com.sun.collablet.CollabException;

import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.MDChannelEventProcessor;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabProcessorConfig;


/**
 * Filesharing EventProcessor
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingEventProcessor extends MDChannelEventProcessor {
    /**
     * constructor
     *
     */
    public FilesharingEventProcessor(CollabProcessorConfig processorConfig, CollabContext context) {
        super(processorConfig, context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * exec
     *
     * @param        eventID                                        eventID
     * @param        context                                        Context
     */
    public void exec(String eventID, EventContext evContext)
    throws CollabException {
        super.exec(eventID, evContext);
    }
}
