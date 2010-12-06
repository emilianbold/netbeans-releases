/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.actions;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.actions.ConnectAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ConnectAction_Name")
public class ConnectAction extends GitAction {

    @Override
    protected boolean enable (Node[] activatedNodes) {
        boolean retval = false;
        Set<File> roots = getRepositoryRoots(activatedNodes);
        if (roots.size() == 1) {
            File root = roots.iterator().next();
            retval = Git.getInstance().isDisconnected(root);
        }
        return retval;
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        if (isEnabled()) {
            Set<File> roots = getRepositoryRoots(nodes);
            if (roots.size() == 1) {
                final File repository = roots.iterator().next();
                new GitProgressSupport() {
                    @Override
                    protected void perform () {
                        Git.getInstance().connectRepository(repository);
                        Git.getInstance().refreshAllAnnotations();
                        refreshState();
                        SystemAction.get(DisconnectAction.class).refreshState();
                    }
                }.start(Git.getInstance().getRequestProcessor(), repository, NbBundle.getMessage(ConnectAction.class, "LBL_ConnectProgress")); //NOI18N
            }
        }
    }

    void refreshState () {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                setEnabled(enable(getActivatedNodes()));
            }
        });
    }

    private Set<File> getRepositoryRoots (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        Set<File> roots = new HashSet<File>();
        Git git = Git.getInstance();
        for (File file : context.getRootFiles()) {
            File repoRoot = git.getRepositoryRoot(file);
            if(repoRoot != null) {
                roots.add(repoRoot);
            }
        }
        return roots;
    }
}
