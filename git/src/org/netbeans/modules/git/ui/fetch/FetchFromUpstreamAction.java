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

package org.netbeans.modules.git.ui.fetch;

import java.io.File;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.SystemAction;
import static org.netbeans.modules.git.ui.fetch.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.FetchFromUpstreamAction", category = "Git")
@ActionRegistration(displayName = "#LBL_FetchFromUpstreamAction_Name")
public class FetchFromUpstreamAction extends GetRemoteChangesAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        fetch(repository);
    }
    
    @Messages({"LBL_Fetch.fetchFromUpstreamFailed=Fetch from Upstream Failed", "LBL_FetchFromUpstreamAction.preparing=Preparing Fetch..."})
    private void fetch (final File repository) {
        GitProgressSupport supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refreshRemotes();
                String errorLabel = LBL_Fetch_fetchFromUpstreamFailed();
                GitBranch trackedBranch = getTrackedBranch(info, errorLabel);
                if (trackedBranch == null) {
                    return;
                }
                GitRemoteConfig cfg = getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);                        
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getUris().get(0);
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                SystemAction.get(FetchAction.class).fetch(repository, uri, cfg.getFetchRefSpecs(), null);
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, LBL_FetchFromUpstreamAction_preparing());
    }

}
