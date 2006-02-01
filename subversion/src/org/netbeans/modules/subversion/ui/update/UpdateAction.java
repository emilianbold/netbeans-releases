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

package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;

/**
 * Update action
 *
 * @author Petr Kuzel
 */
public class UpdateAction extends ContextAction {

    protected String getBaseName() {
        return "CTL_MenuItem_Update";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected void performContextAction(ActionEvent e) {
        Context ctx = getContext();

        ISVNClientAdapter client = Subversion.getInstance().getClient();

        // FIXME add non-recursive folders splitting
        // FIXME add shalow logic allowing to ignore nested projects

        File[] roots = ctx.getRootFiles();
        ProgressHandle progress = ProgressHandleFactory.createHandle(getName());  // XXX running name
        try {
            progress.start();
            client.update(roots, SVNRevision.HEAD, true, true);
            StatusDisplayer.getDefault().setStatusText("Subversion update completed");
        } catch (SVNClientException e1) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e1, "Can not update");
            err.notify(e1);
        } finally {
            progress.finish();
        }
    }
}
