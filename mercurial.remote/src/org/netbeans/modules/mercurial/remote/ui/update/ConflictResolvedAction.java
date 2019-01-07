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

package org.netbeans.modules.mercurial.remote.ui.update;

import java.util.logging.Level;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import  org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Allow files who have Conlfict Status to be manually resolved.
 *
 * 
 */
@Messages({
    "CTL_MenuItem_MarkResolved=&Mark as Resolved"
})
public class ConflictResolvedAction extends ContextAction {

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        return cache.containsFileOfStatus(context, FileInformation.STATUS_VERSIONED_CONFLICT, false);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_MarkResolved";                             //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        resolved(context);
    }

    public static void resolved(VCSContext ctx) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        VCSFileProxy[] files = cache.listFiles(ctx, FileInformation.STATUS_VERSIONED_CONFLICT);
        final VCSFileProxy root = HgUtils.getRootFile(ctx);
        if (root == null || files == null || files.length == 0) {
            return;
        }

        conflictResolved(root, files);
    }

    public static void conflictResolved(VCSFileProxy repository, final VCSFileProxy[] files) {
        if (repository == null || files == null || files.length == 0) {
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {

            @Override
            public void perform() {
                OutputLogger logger = getLogger();
                for (VCSFileProxy file : files) {
                    if (isCanceled()) {
                        return;
                    }
                    VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(file);
                    ConflictResolvedAction.perform(file, repository, logger);
                }
            }
        };
        support.start(rp, repository, NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConflictResolved_Progress")); // NOI18N
    }
    
    private static void perform(VCSFileProxy file, VCSFileProxy repository, OutputLogger logger) {
        if (file == null) {
            return;
        }
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        file = file.normalizeFile();
        try {
            HgCommand.markAsResolved(repository, file, logger);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
        HgCommand.deleteConflictFile(file);
        Mercurial.LOG.log(Level.FINE, "ConflictResolvedAction.perform(): DELETE CONFLICT File: {0}", // NOI18N
                new Object[] {file.getPath() + HgCommand.HG_STR_CONFLICT_EXT} );
        cache.refresh(file);
    }
    
    public static void resolved(final VCSFileProxy file) {
        if (file == null) {
            return;
        }
        final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(file);
        if (repository == null) {
            return;
        }
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                ConflictResolvedAction.perform(file, repository, getLogger());
            }
        };
        support.start(rp, repository, NbBundle.getMessage(ConflictResolvedAction.class, "MSG_ConflictResolved_Progress")); // NOI18N
    }

}
