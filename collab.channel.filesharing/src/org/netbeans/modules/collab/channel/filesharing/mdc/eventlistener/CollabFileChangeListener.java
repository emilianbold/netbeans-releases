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
