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
package org.netbeans.modules.collab.channel.filesharing.mdc.eventlistener;

import org.openide.*;
import org.openide.filesystems.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.event.*;


/**
 *
 * @author  Owner
 */
public class CollabFileChangeListener extends Object implements FileChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* notifier */
    protected EventNotifier eventNotifier = null;

    /**
     * constructor
     *
     */
    public CollabFileChangeListener(EventNotifier eventNotifier) {
        super();
        this.eventNotifier = eventNotifier;
    }

    public CollabFileChangeListener() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////           

    /**
     *
     * @param fileAttributeEvent
     */
    public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
    }

    /**
     * fileChanged
     *
     * @param fileEvent
     */
    public void fileChanged(FileEvent fileEvent) {
    }

    /**
     *
     * @param fileEvent
     */
    public void fileDataCreated(FileEvent fileEvent) {
        EventContext evContext = new EventContext(FileCreated.getEventID(), fileEvent.getFile());
        CollabEvent ce = new FileCreated(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     *
     * @param fileEvent
     */
    public void fileDeleted(FileEvent fileEvent) {
        EventContext evContext = new EventContext(FileDeleted.getEventID(), fileEvent.getFile());
        CollabEvent ce = new FileDeleted(evContext);

        try {
            eventNotifier.notify(ce);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     *
     * @param fileEvent
     */
    public void fileFolderCreated(FileEvent fileEvent) {
    }

    /**
     *
     * @param fileRenameEvent
     */
    public void fileRenamed(FileRenameEvent fileRenameEvent) {
    }
}
