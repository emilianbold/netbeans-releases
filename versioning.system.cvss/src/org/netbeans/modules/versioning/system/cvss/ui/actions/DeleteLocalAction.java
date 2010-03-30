/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;

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
        CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
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
