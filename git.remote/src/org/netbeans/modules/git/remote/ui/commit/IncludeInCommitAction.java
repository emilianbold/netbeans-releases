/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote.ui.commit;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.commit.IncludeInCommitAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_IncludeInCommitAction_Name")
@NbBundle.Messages({
    "LBL_IncludeInCommitAction_Name=In&clude In Commit"
})
public class IncludeInCommitAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        return include(repository, roots);
    }

    @Override
    protected boolean enable (Node[] activatedNodes) {
        boolean enabled = super.enable(activatedNodes);
        if (enabled) {
            enabled = false;
            GitModuleConfig config = GitModuleConfig.getDefault();
            for (VCSFileProxy root : getCurrentContext(activatedNodes).getRootFiles()) {
                enabled = true;
                if (!config.isExcludedFromCommit(root.getPath())) {
                    enabled = false;
                    break;
                }
            }
        }
        return enabled;
    }

    public Task include (VCSFileProxy repository, VCSFileProxy[] roots) {
        List<String> toInclude = filterRoots(roots);
        GitModuleConfig config = GitModuleConfig.getDefault();
        config.removeExclusionPaths(toInclude);
        SystemAction.get(IncludeInCommitAction.class).setEnabled(false);
        SystemAction.get(ExcludeFromCommitAction.class).setEnabled(false);
        return null;
    }
    
    private static List<String> filterRoots (VCSFileProxy[] roots) {
        List<String> toInclude = new LinkedList<>();
        GitModuleConfig config = GitModuleConfig.getDefault();
        for (VCSFileProxy root : roots) {
            String path = root.getPath();
            if (config.isExcludedFromCommit(path)) {
                toInclude.add(path);
            }
        }
        return toInclude;
    }
    
}
