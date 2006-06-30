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

package org.netbeans.modules.versioning.system.cvss.ui.actions;

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
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Delete action enabled only for new local files only.
 * It eliminates <tt>CVS/Entries</tt> scheduling if exists too.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends AbstractSystemAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    public void performCvsAction(Node[] nodes) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt"));
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title"));
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }

        final File [] files = getContext(nodes).getFiles();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                async(files);
            }
        });
    }

    protected int getFileEnabledStatus() {
        return LOCALLY_DELETABLE_MASK;
    }

    protected String getBaseName(Node [] activatedNodes) {
        return "Delete";  // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }
    
    public void async(File[] files) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            StandardAdminHandler entries = new StandardAdminHandler();
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    entries.removeEntry(file);
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
