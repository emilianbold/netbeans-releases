/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.utils;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.git.remote.ui.diff.DiffAction;
import org.netbeans.modules.git.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.git.remote.ui.repository.Revision;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 */
public class LogUtils {
    
    private LogUtils () {
        
    }
    
    @NbBundle.Messages({
        "# {0} - [history view]", "# {1} - [diff]",
        "MSG_LogUtils.updateBranch.actions=Show changes in {0} or as {1}\n",
        "MSG_LogUtils.updateBranch.actions.history=[Search History]",
        "MSG_LogUtils.updateBranch.actions.diff=[Diff]"
    })
    public static void logBranchUpdateReview (VCSFileProxy repository, String branchName, String oldId, String newId, OutputLogger logger) {
        if (oldId != null && newId != null
                && !oldId.equals(newId)) {
            String line = Bundle.MSG_LogUtils_updateBranch_actions("{0}", "{1}");
            int historyPos = line.indexOf("{0}");
            int diffPos = line.indexOf("{1}");
            List<String> segments = new ArrayList<>();
            OutputListener list1, list2;
            if (historyPos < diffPos) {
                segments.add(line.substring(0, historyPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_history());
                list1 = new ShowHistoryListener(repository, oldId, newId);
                segments.add(line.substring(historyPos + 3, diffPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_diff());
                list2 = new DiffListener(repository, branchName, oldId, newId);
                segments.add(line.substring(diffPos + 3));
            } else {
                segments.add(line.substring(0, diffPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_diff());
                list1 = new DiffListener(repository, branchName, oldId, newId);
                segments.add(line.substring(diffPos + 3, historyPos));
                segments.add(Bundle.MSG_LogUtils_updateBranch_actions_history());
                list2 = new ShowHistoryListener(repository, oldId, newId);
                segments.add(line.substring(historyPos + 3));
            }
            logger.output(segments.get(0), null);
            logger.output(segments.get(1), list1);
            logger.output(segments.get(2), null);
            logger.output(segments.get(3), list2);
            logger.output(segments.get(4), null);
        }
        logger.outputLine("");
    }
    
    private static class ShowHistoryListener implements OutputListener {
        private final VCSFileProxy repository;
        private final String from;
        private final String to;

        public ShowHistoryListener (VCSFileProxy repository, String from, String to) {
            this.repository = repository;
            this.from = from.length() > 7 ? from.substring(0, 7) : from;
            this.to = to.length() > 7 ? to.substring(0, 7) : to;
        }
        
        @Override
        public void outputLineSelected (OutputEvent ev) {
        }

        @Override
        public void outputLineAction (OutputEvent ev) {
            SearchHistoryAction.openSearch(repository, repository, repository.getName(), from, to);
        }

        @Override
        public void outputLineCleared (OutputEvent ev) {
        }
        
    }
    
    @NbBundle.Messages({
        "# {0} - branch name", "# {1} - commit id",
        "MSG_LogUtils.updateBranch.actions.diff.previous={0} - {1}",
        "# {0} - branch name", "# {1} - commit id",
        "MSG_LogUtils.updateBranch.actions.diff.updated={0} - {1}"
    })
    private static class DiffListener implements OutputListener {
        private final VCSFileProxy repository;
        private final String branchName;
        private final String from;
        private final String to;

        public DiffListener (VCSFileProxy repository, String branchName, String from, String to) {
            this.repository = repository;
            this.branchName = branchName;
            this.from = from.length() > 7 ? from.substring(0, 7) : from;
            this.to = to.length() > 7 ? to.substring(0, 7) : to;
        }
        
        @Override
        public void outputLineSelected (OutputEvent ev) {
        }

        @Override
        public void outputLineAction (OutputEvent ev) {
            SystemAction.get(DiffAction.class).diff(GitUtils.getContextForFile(repository),
                    new Revision(from, Bundle.MSG_LogUtils_updateBranch_actions_diff_previous(branchName, from)),
                    new Revision(to, Bundle.MSG_LogUtils_updateBranch_actions_diff_previous(branchName, to)));
        }

        @Override
        public void outputLineCleared (OutputEvent ev) {
        }
        
    }
    
}
