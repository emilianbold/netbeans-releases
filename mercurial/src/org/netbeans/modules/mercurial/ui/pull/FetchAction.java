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
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.filesystems.FileUtil;
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
    
    private final VCSContext context;

    public FetchAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public void performAction(ActionEvent e) {
        final File root = HgUtils.getRootFile(context);
        if (root == null) return;
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() { performFetch(root, this.getLogger()); } };

        support.start(rp, root, org.openide.util.NbBundle.getMessage(FetchAction.class, "MSG_FETCH_PROGRESS")); // NOI18N
    }

    static void performFetch(final File root, OutputLogger logger) {
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

            HgURL pullSource;
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
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.outputInRed(NbBundle.getMessage(FetchAction.class, "MSG_FETCH_DONE")); // NOI18N
            logger.output(""); // NOI18N
            Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    FileUtil.refreshFor(root); // fetch just might have changed the file layout...
                }
            });
        }
    }

    public boolean isEnabled() {
        return HgUtils.getRootFile(context) != null;
    } 
}
