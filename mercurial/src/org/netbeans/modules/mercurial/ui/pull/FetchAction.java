/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.mercurial.ui.pull;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.RequestProcessor;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Fetch action for mercurial: 
 * hg fetch - launch hg view to view the dependency tree for the repository
 * Pull changes from a remote repository, merge new changes if needed.
 * This finds all changes from the repository at the specified path
 * or URL and adds them to the local repository.
 * 
 * If the pulled changes add a new head, the head is automatically
 * merged, and the result of the merge is committed.  Otherwise, the
 * working directory is updated.
 * 
 * @author John Rice
 */
public class FetchAction extends ContextAction {
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_FetchLocal";                               //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext context = HgUtils.getCurrentContext(nodes);
        final Set<File> repositoryRoots = HgUtils.getRepositoryRoots(context);
        // run the whole bulk operation in background
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                for (File repositoryRoot : repositoryRoots) {
                    final File root = repositoryRoot;
                    final boolean[] canceled = new boolean[1];
                    RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                    // run every repository fetch in its own support with its own output window
                    HgProgressSupport support = new HgProgressSupport() {
                        public void perform() {
                            performFetch(root, this.getLogger());
                            canceled[0] = isCanceled();
                        }
                    };
                    support.start(rp, root, org.openide.util.NbBundle.getMessage(FetchAction.class, "MSG_FETCH_PROGRESS")).waitFinished(); //NOI18N
                    if (canceled[0]) {
                        break;
                    }
                }
            }
        });
    }

    static void performFetch(final File root, OutputLogger logger) {
        HgURL pullSource = null;
        try {
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE")); // NOI18N
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_TITLE_SEP")); // NOI18N
            
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, 
                    "MSG_FETCH_LAUNCH_INFO", root.getAbsolutePath())); // NOI18N

            final String pullSourceString = new HgConfigFiles(root).getDefaultPull(true);
            // If the repository has no default pull path then inform user
            if (pullSourceString == null) {
                return;
            }

            try {
                pullSource = new HgURL(pullSourceString);
            } catch (URISyntaxException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
                return;
            }

            List<String> list;
            list = HgCommand.doFetch(root, pullSource, logger);

            if (list != null && !list.isEmpty()) {
                logger.output(HgUtils.replaceHttpPassword(list));
                MergeAction.handleMergeOutput(root, list, false, logger);
                HgUtils.notifyUpdatedFiles(root, list);
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_DONE")); // NOI18N
            logger.output(""); // NOI18N
            if (pullSource != null) {
                pullSource.clearPassword();
            }
        }
    }
}
