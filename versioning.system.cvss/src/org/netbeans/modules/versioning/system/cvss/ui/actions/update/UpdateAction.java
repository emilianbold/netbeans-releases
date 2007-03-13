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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.update;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;

import java.io.File;

/**
 * Performs the CVS 'update' command on selected nodes.
 * 
 * @author Maros Sandor
 */
public class UpdateAction extends AbstractSystemAction {
    
    protected String getBaseName(Node [] activatedNodes) {
        return "CTL_MenuItem_Update";  // NOI18N
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    public void performCvsAction(Node[] nodes) {

        ExecutorGroup group = new ExecutorGroup(getRunningName(nodes));
        group.progress(NbBundle.getMessage(UpdateAction.class, "BK1001"));
        Context context = getContext(nodes);
        GlobalOptions options = null;
        if (context.getExclusions().size() > 0) {
            options = CvsVersioningSystem.createGlobalOptions();
            options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
        }

        File [][] flatRecursive = Utils.splitFlatOthers(context.getRootFiles());

        if (flatRecursive[0].length > 0) {
            UpdateCommand cmd = new UpdateCommand();
            cmd.setDisplayName(NbBundle.getMessage(UpdateAction.class, "BK0001"));
            cmd.setBuildDirectories(false);
            cmd.setPruneDirectories(false);
            cmd.setRecursive(false);
            cmd.setFiles(flatRecursive[0]);
            group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, getContextDisplayName(nodes)));
        }
        if (flatRecursive[1].length > 0) {
            UpdateCommand cmd = new UpdateCommand();
            cmd.setDisplayName(NbBundle.getMessage(UpdateAction.class, "BK0001"));
            cmd.setBuildDirectories(true);
            cmd.setPruneDirectories(true);
            cmd.setFiles(flatRecursive[1]);
            group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, getContextDisplayName(nodes)));
        }
        group.execute();
    }

    protected boolean asynchronous() {
        return false;
    }
    
}
