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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.CvsFileNode;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.io.File;

/**
 * Opens the Visual Merge component. 
 *  
 * @author Maros Sandor
 */
public class ResolveConflictsAction extends AbstractSystemAction {
    
    public ResolveConflictsAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_ResolveConflicts"; // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        return CvsVersioningSystem.getInstance().getFileTableModel(Utils.getCurrentContext(nodes), FileInformation.STATUS_VERSIONED_CONFLICT).getNodes().length > 0;
    }

    public void performCvsAction(Node[] nodes) {
        CvsFileNode [] fileNodes = CvsVersioningSystem.getInstance().getFileTableModel(getContext(nodes), FileInformation.STATUS_VERSIONED_CONFLICT).getNodes();
        if (fileNodes.length == 0) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(ResolveConflictsAction.class, "MSG_NoConflicts")));
            return;
        }
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < fileNodes.length; i++) {
            File file = fileNodes[i].getFile();
            FileInformation info = cache.getStatus(file);
            Entry entry = info.getEntry(file);
            if (entry == null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ResolveConflictsAction.class, "MSG_MoveAwayLocalFileConflict", file.getName())));
            } else {
                ResolveConflictsExecutor rce = new ResolveConflictsExecutor();
                rce.exec(file);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
    
}
