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

package org.netbeans.modules.versioning.system.cvss.ui.actions;

import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.netbeans.modules.versioning.system.cvss.FileInformation;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Delete action enabled only for new local files only.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends AbstractSystemAction {

    public void actionPerformed(ActionEvent ev) {
        File[] files = getFilesToProcess();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    fo.delete(lock);
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not lock " + file.toString());  // NOi18N
                    err.notify(e);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected String getBaseName() {
        return "Delete";  // NOI18M
    }
}
