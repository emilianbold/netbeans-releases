/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.commit;

import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.actions.*;
import org.netbeans.modules.subversion.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.tigris.subversion.svnclientadapter.*;

/**
 * Delete action enabled for new local files (not yet in repository).
 * It eliminates <tt>.svn/entries</tt> scheduling if exists too.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends ContextAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    protected String getBaseName(Node [] activatedNodes) {
        return "Delete";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return LOCALLY_DELETABLE_MASK;
    }
    
    protected void performContextAction(final Node[] nodes) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt"));
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title"));
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }

        final Context ctx = getContext(nodes);
        Runnable run = new Runnable() {
            public void run() {
                Object pair = startProgress(nodes);
                try {
                    performDelete(ctx);
                } finally {
                    finished(pair);
                }
            }
        };
        Subversion.getInstance().postRequest(run);
    }
    
    public static void performDelete(Context ctx) {

        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }

        FileStatusCache cache = Subversion.getInstance().getStatusCache();

        File[] files = ctx.getFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                FileLock lock = null;
                try {
                    try {
                        client.revert(file, false);
                    } catch (SVNClientException ex) {
                        // XXX mask (not versioned) it's expected
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }

                    lock = fo.lock();                    
                    fo.delete(lock);
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, NbBundle.getMessage(DeleteLocalAction.class, "BK0001", file.getAbsolutePath()));
                    err.notify(e);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
    }
    
}
