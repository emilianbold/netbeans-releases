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
package org.netbeans.modules.bugtracking.ui.issue;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Tomas Stupka
 */
public class IssueAction extends SystemAction {

    private static final RequestProcessor rp = new RequestProcessor("Bugtracking IssueAction"); // NOI18N

    public IssueAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IssueAction.class, "CTL_IssueAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(IssueAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        createIssue();
    }

    public static void openIssue(final Issue issue, final boolean refresh) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(issue);
                tc.open();
                tc.requestActive();
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueAction.class, "LBL_REFRESING_ISSUE", new Object[]{issue.getID()}));
                        try {
                            handle.start();
                            if (refresh && !issue.refresh()) {
                                return;
                            }
                            IssueCacheUtils.setSeen(issue, true);
                        } finally {
                            if(handle != null) handle.finish();
                        }
                    }
                });
            }
        });
    }

    private static void createIssue() {
        createIssue(null, WindowManager.getDefault().getRegistry().getActivatedNodes());
    }

    public static void createIssue(final Repository repository) {
        createIssue(repository, WindowManager.getDefault().getRegistry().getActivatedNodes());
    }

    private static void createIssue(final Repository repository, final Node[] context) {
        final boolean repositoryGiven = repository != null;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueTopComponent tc = new IssueTopComponent();
                tc.initNewIssue(repository, !repositoryGiven, context);
                tc.open();
                tc.requestActive();
            }
        });
    }

    public static void openIssue(final File file, final String issueId) {
        openIssueIntern(null, file, issueId);
    }

    public static void openIssue(final Repository repository, final String issueId) {
        openIssueIntern(repository, null, issueId);
    }

    public static void openIssueIntern(final Repository repositoryParam, final File file, final String issueId) {
        assert issueId != null;
        assert repositoryParam != null && file == null || repositoryParam == null && file != null;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final IssueTopComponent tc = IssueTopComponent.find(issueId);
                final boolean tcOpened = tc.isOpened();
                final Issue[] issue = new Issue[1];
                issue[0] = tc.getIssue();
                if (issue[0] == null) {
                    tc.initNoIssue(issueId);
                }
                if(!tcOpened) {
                    tc.open();
                }
                tc.requestActive();
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHandle handle = null;
                        try {
                            if (issue[0] != null) {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueAction.class, "LBL_REFRESING_ISSUE", new Object[]{issueId}));
                                handle.start();
                                issue[0].refresh();
                            } else {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueAction.class, "LBL_OPENING_ISSUE", new Object[]{issueId}));
                                handle.start();

                                Repository repository;
                                if(repositoryParam == null) {
                                    repository = BugtrackingOwnerSupport.getInstance().getRepository(file, issueId, true);
                                    if(repository == null) {
                                        // if no repository was known user was supposed to choose or create one
                                        // in scope of the previous getRepository() call. So null shoud stand
                                        // for cancel in this case.
                                        handleTC();
                                        return;
                                    }
                                    BugtrackingOwnerSupport.getInstance().setFirmAssociation(file, repository);

                                } else {
                                    repository = repositoryParam;
                                }

                                issue[0] = repository.getIssue(issueId);
                                if(issue[0] == null) {
                                    // lets hope the repository was able to handle this
                                    // because whatever happend, there is nothing else
                                    // we can do at this point
                                    handleTC();
                                    return;
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tc.setIssue(issue[0]);
                                    }
                                });
                                IssueCacheUtils.setSeen(issue[0], true);
                            }
                        } finally {
                            if(handle != null) handle.finish();
                        }
                    }

                    public void handleTC() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!tcOpened) {
                                    tc.close();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public static void closeIssue(final Issue issue) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(issue);
                if(tc != null) {
                    tc.close();
                }
            }
        });
    }
}
