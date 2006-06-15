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

import java.io.*;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

/**
 * Show basic conflict resolver UI (provided by the diff module).
 *
 * @author Petr Kuzel
 */
public class ResolveConflictsAction extends ContextAction {
    
    public ResolveConflictsAction() {
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "ResolveConflicts";  // NOI18N
    }

    protected void performContextAction(Node[] nodes) {
        Context ctx = getContext(nodes);
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);

        if (files.length == 0) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(org.openide.util.NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflictsFound")); // NOI18N
            DialogDisplayer.getDefault().notify(nd);
        } else {
            for (int i = 0; i<files.length; i++) {
                File file = files[i];
                ResolveConflictsExecutor executor = new ResolveConflictsExecutor(file);
                executor.exec();
            }
        }
    }

    public boolean asynchronous() {
        return false;
    }
    
}
