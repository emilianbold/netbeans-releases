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
