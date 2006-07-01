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
import org.openide.util.NbBundle;

import javax.swing.*;
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
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt")); // NOI18N
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title")); // NOI18N
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }
        
        final Context ctx = getContext(nodes);
        ProgressSupport support = new ContextAction.ProgressSupport(this, nodes) {
            public void perform() {
                performDelete(ctx, this);
            }
        };
        support.start(createRequestProcessor(nodes));        
    }
    
    public static void performDelete(Context ctx, SvnProgressSupport support) {

        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex); 
            return;
        }        

        if(support.isCanceled()) {
            return;
        }
        File[] files = ctx.getFiles();
        for (int i = 0; i < files.length; i++) {
            if(support.isCanceled()) {
                return;
            }
        
            File file = files[i];
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                FileLock lock = null;
                try {
                    try {
                        client.revert(file, false);
                    } catch (SVNClientException ex) {
                        // XXX use ExceptionHandler
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    lock = fo.lock();                    
                    fo.delete(lock);       
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, NbBundle.getMessage(DeleteLocalAction.class, "BK0001", file.getAbsolutePath())); // NOI18N
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
