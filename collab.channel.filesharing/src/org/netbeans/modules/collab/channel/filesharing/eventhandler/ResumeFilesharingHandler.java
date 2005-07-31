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
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import com.sun.collablet.CollabException;

import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.Moderator;
import org.netbeans.modules.collab.channel.filesharing.msgbean.ResumeFilesharing;


/**
 * ResumeFilesharingHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class ResumeFilesharingHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public ResumeFilesharingHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * constructMsg
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructMsg(EventContext evContext) {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        ResumeFilesharing resumeFilesharing = new ResumeFilesharing();
        collab.setChResumeFilesharing(resumeFilesharing);

        Moderator moderator = new Moderator();
        resumeFilesharing.setModerator(moderator);
        moderator.setUsers(getContext().constructUsers(getLoginUser()));

        return collab;
    }

    /**
     * handleMsg
     *
     * @param        collabBean
     * @param        messageOriginator
     * @param        isUserSame
     */
    public void handleMsg(CCollab collabBean, String messageOriginator, boolean isUserSame)
    throws CollabException {
        getContext().setInPauseState(false);

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    SyncWaitPanel.hideDialog();
                }
            }
        );

        //Resume all edit operation
        getContext().resumeAll();
    }
}
