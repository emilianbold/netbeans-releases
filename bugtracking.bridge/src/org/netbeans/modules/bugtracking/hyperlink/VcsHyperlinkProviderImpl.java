/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.hyperlink;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.IssueFinder;
import org.netbeans.modules.versioning.util.HyperlinkProvider;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Provides hyperlink functionality on issue reference in VCS artefects as e.g. log messages in Serach History
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.HyperlinkProvider.class)
public class VcsHyperlinkProviderImpl extends HyperlinkProvider {

    @Override
    public int[] getSpans(String text) {
        return IssueFinder.getIssueSpans(text);
    }

    @Override
    public String getTooltip(String text, int offsetStart, int offsetEnd) {
        return NbBundle.getMessage(VcsHyperlinkProviderImpl.class, "LBL_OpenIssue", new Object[] { getIssueId(text, offsetStart, offsetEnd) });
    }

    @Override
    public void onClick(final File file, final String text, int offsetStart, int offsetEnd) {
        // XXX run async
        final String issueId = getIssueId(text, offsetStart, offsetEnd);
        if(issueId == null) return;

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final Repository repo = BugtrackingOwnerSupport.getInstance().getRepository(file, issueId, true);
        if(repo == null) return;

        BugtrackingOwnerSupport.getInstance().setFirmAssociation(file, repo);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(VcsHyperlinkProviderImpl.class, "MSG_Opening", new Object[] {issueId}), c); // NOI18N
        class IssueDisplayer implements Runnable {
            private Issue issue = null;
            public void run() {
                if (issue == null) {
                    /* (request processor thread) - find the issue */
                    try {
                        issue = repo.getIssue(issueId);
                    } finally {
                        handle.finish();
                    }
                    if (issue != null) {
                        EventQueue.invokeLater(this);
                    }
                } else {
                    /* (AWT event-dispatching thread) - display the issue */
                    assert EventQueue.isDispatchThread();
                    issue.open();
                }
            }
        }
        handle.start();
        t[0] = RequestProcessor.getDefault().post(new IssueDisplayer());
    }

    private String getIssueId(String text, int offsetStart, int offsetEnd) {        
        return IssueFinder.getIssueNumber(text.substring(offsetStart, offsetEnd));
    }

}
