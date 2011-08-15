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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mercurial.ui.log;

import java.awt.EventQueue;
import org.netbeans.modules.versioning.spi.VCSContext;
import java.io.File;
import java.util.List;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Log action for mercurial:
 * hg log - show revision history of entire repository or files
 *
 * @author John Rice
 */
public class LogAction extends SearchHistoryAction {

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Log";                                      //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        openHistory(context, NbBundle.getMessage(LogAction.class, "MSG_Log_TabTitle", org.netbeans.modules.versioning.util.Utils.getContextDisplayName(context)));
    }

    private void openHistory (final VCSContext context, final String title) {
        Utils.postParallel(new Runnable() {
            @Override
            public void run () {
                File repositoryRoot = getRepositoryRoot(context);
                final File[] files = getFiles(context, repositoryRoot);
                if (files == null) {
                    return;
                }
                outputSearchContextTab(repositoryRoot, files, "MSG_Log_Title");
                final boolean startSearch = files != null && (files.length == 1 && files[0].isFile() || files.length > 1 && Utils.shareCommonDataObject(files));
                final String branchName;
                HgLogMessage[] parents = WorkingCopyInfo.getInstance(repositoryRoot).getWorkingCopyParents();
                if (parents.length == 1) {
                    if (parents[0].getBranches().length > 0) {
                        branchName = parents[0].getBranches()[0];
                    } else {
                        branchName = HgBranch.DEFAULT_NAME;
                    }
                } else {
                    branchName = ""; //NOI18N
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(files, branchName);
                        tc.setDisplayName(title);
                        tc.open();
                        tc.requestActive();
                        if (startSearch) {
                            tc.search();
                        }
                    }
                });
            }
        }, 0);
    }

    /**
     * Opens search panel with a diff view fixed on a line
     * @param file file to search history for
     * @param lineNumber number of a line to fix on
     */
    public static void openSearch(final File file, final int lineNumber) {
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(file, new SearchHistoryTopComponent.DiffResultsViewFactory() {
            @Override
            DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
                return new DiffResultsViewForLine(panel, results, lineNumber);
            }
        });
        String tcTitle = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", file.getName()); // NOI18N
        tc.setDisplayName(tcTitle);
        tc.open();
        tc.requestActive();
    }
}
