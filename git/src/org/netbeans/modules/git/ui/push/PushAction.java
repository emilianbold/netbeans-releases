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

package org.netbeans.modules.git.ui.push;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.push.PushAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PushAction_Name")
public class PushAction extends SingleRepositoryAction {
    
    private static final Logger LOG = Logger.getLogger(PushAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        push(repository);
    }
    
    private void push (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refreshRemotes();
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        PushWizard wiz = new PushWizard(repository, remotes);
        if (wiz.show()) {
            push(repository, wiz.getPushUri(), wiz.getPushRefSpecs(), wiz.getFetchRefSpecs());
        }
    }
    
    public void push (File repository, final String remote, final List<String> pushRefSpecs, final List<String> fetchRefSpecs) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                LOG.log(Level.FINE, "Pushing {0}/{1} to {2}", new Object[] { pushRefSpecs, fetchRefSpecs, remote }); //NOI18N
                try {
                    GitClient client = getClient();
                    Map<String, GitTransportUpdate> result = client.push(remote, pushRefSpecs, fetchRefSpecs, this);
                    log(result);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }
            
            protected void log (Map<String, GitTransportUpdate> updates) {
                OutputLogger logger = getLogger();
                if (updates.isEmpty()) {
                    logger.output(NbBundle.getMessage(PushAction.class, "MSG_PushAction.updates.noChange")); //NOI18N
                } else {
                    for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                        GitTransportUpdate update = e.getValue();
                        if (update.getType() == Type.BRANCH) {
                            logger.output(NbBundle.getMessage(PushAction.class, "MSG_PushAction.updates.updateBranch", new Object[] { //NOI18N
                                update.getLocalName(), 
                                update.getOldObjectId(),
                                update.getNewObjectId(),
                                update.getResult(),
                            }));
                        } else {
                            logger.output(NbBundle.getMessage(PushAction.class, "MSG_PushAction.updates.updateTag", new Object[] { //NOI18N
                                update.getLocalName(), 
                                update.getResult(),
                            }));
                        }
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PushAction.class, "LBL_PushAction.progressName")); //NOI18N
    }
}
