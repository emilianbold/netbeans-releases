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

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.FileInformation;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
public class CommitAction extends ContextAction {

    protected String getBaseName() {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    protected void performContextAction(ActionEvent e) {
        Context ctx = getContext();

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);

        // show commit dialog

        // if OK setup sequence of add, remove and commit calls
    }
}
