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

import org.openide.util.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessorFactory;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabProcessorConfig;


/**
 * Default EventProcessor Factory
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingEventProcessorFactory extends Object implements EventProcessorFactory {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /* FileHandlerFactory ID */
    public static final String EVENTPROCESSOR_FACTORY_ID = "eventprocessorfactory"; // NOI18N            

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* instance */
    private static FilesharingEventProcessorFactory instance = null;

    /**
     * constructor
     *
     */
    public FilesharingEventProcessorFactory() {
        super();
    }

    /**
     * getter for ID
     *
     * @return        ID
     */
    public String getID() {
        return EVENTPROCESSOR_FACTORY_ID;
    }

    /**
     * getter for displayName
     *
     * @return        displayName
     */
    public String getDisplayName() {
        return NbBundle.getMessage(EventProcessorFactory.class, "LBL_EventProcessorFactory_DisplayName"); // NOI18N
    }

    /**
     * create FileHandler instance
     *
     * @return        CollabFileHandler
     */
    public EventProcessor createEventProcessor(CollabProcessorConfig processorConfig, CollabContext context)
    throws CollabException {
        return new FilesharingEventProcessor(processorConfig, context);
    }

    /**
     *
     * @return EventProcessor Factory
     */
    public static EventProcessorFactory getDefault() {
        if (instance == null) {
            instance = new FilesharingEventProcessorFactory();
        }

        return instance;
    }
}
