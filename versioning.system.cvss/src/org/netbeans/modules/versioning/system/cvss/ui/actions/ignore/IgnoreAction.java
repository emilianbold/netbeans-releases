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

package org.netbeans.modules.versioning.system.cvss.ui.actions.ignore;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.openide.nodes.Node;

import java.io.File;
import java.awt.event.ActionEvent;

/**
 * Adds files to .cvsignore file.
 * 
 * @author Maros Sandor
 */
public class IgnoreAction extends AbstractSystemAction {
    
    public static final int UNDEFINED  = 0;
    public static final int IGNORING   = 1;
    public static final int UNIGNORING = 2;
    
    protected String getBaseName() {
        int actionStatus = getActionStatus();
        switch (actionStatus) {
        case UNDEFINED:
        case IGNORING:
            return "CTL_MenuItem_Ignore";  // NOI18N
        case UNIGNORING:
            return "CTL_MenuItem_Unignore"; // NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    public int getActionStatus() {
        int actionStatus = -1;
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        File [] files = Utils.getCurrentContext(null).getFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(".cvsignore")) { // NOI18N
                actionStatus = UNDEFINED;
                break;
            }
            FileInformation info = cache.getStatus(files[i]);
            if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                actionStatus = (actionStatus == -1 || actionStatus == IGNORING) ? IGNORING : UNDEFINED;
            } else if (info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                actionStatus = ((actionStatus == -1 || actionStatus == UNIGNORING) && canBeUnignored(files[i])) ? UNIGNORING : UNDEFINED;
            } else {
                actionStatus = UNDEFINED;
                break;
            }
        }
        return actionStatus == -1 ? UNDEFINED : actionStatus;
    }

    private boolean canBeUnignored(File file) {
        return CvsVersioningSystem.getInstance().isInCvsIgnore(file);
    }

    protected boolean enable(Node[] nodes) {
        return getActionStatus() != UNDEFINED;
    }

    public void performCvsAction(Node[] nodes) {
        int actionStatus = getActionStatus();
        if (actionStatus == IGNORING) {
            CvsVersioningSystem.getInstance().setIgnored(Utils.getCurrentContext(nodes).getFiles());
        } else if (actionStatus == UNIGNORING) {
            CvsVersioningSystem.getInstance().setNotignored(Utils.getCurrentContext(nodes).getFiles());
        } else {
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }

    protected boolean asynchronous() {
        return false;
    }

}
