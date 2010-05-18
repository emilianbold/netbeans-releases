/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/versioning/system/cvss/resources/icons/update.png"; // NOI18N
    }
}
