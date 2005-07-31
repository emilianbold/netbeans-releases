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
package org.netbeans.modules.collab.channel.filesharing.config;

import com.sun.collablet.CollabException;

import org.openide.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabProcessorConfig;


/**
 * Default EventProcessor
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingProcessorConfig extends CollabProcessorConfig {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /* configURL */
    private static final String configURL = "nbresloc:/org/netbeans/modules/collab/channel/filesharing/resources/filesharing_event_processor_config.xml";

    /**
     * constructor
     *
     */
    public FilesharingProcessorConfig() {
        super();

        try {
            init();
        } catch (CollabException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * constructor
     *
     */
    public FilesharingProcessorConfig(String currentVersion) {
        super(currentVersion);

        try {
            init();
        } catch (CollabException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * init
     *
     */
    public void init() throws CollabException {
        init(configURL);
    }
}
