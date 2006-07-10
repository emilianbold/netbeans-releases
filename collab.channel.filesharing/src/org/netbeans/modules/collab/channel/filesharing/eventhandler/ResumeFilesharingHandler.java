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
