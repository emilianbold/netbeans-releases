/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.RepositoriesSupport;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryNode extends AbstractNode {
    private Repository repository;

    public RepositoryNode(Repository repository) {
        super(Children.LEAF);
        this.repository = repository;
        setName(repository.getDisplayName());
    }

    @Override
    public Image getIcon(int type) {
        return repository.getIcon();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(SystemAction.get(QueryAction.class).getName()) {
                public void actionPerformed(ActionEvent e) {
                    QueryAction.openQuery(null, repository);
                }
            },
            new AbstractAction(SystemAction.get(IssueAction.class).getName()) {
                public void actionPerformed(ActionEvent e) {
                    IssueAction.openIssue(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_EditRepository")) { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    BugtrackingUtil.editRepository(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_RemoveRepository")) { // NOI18N
                public void actionPerformed(ActionEvent e) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(RepositoryNode.class, "MSG_RemoveRepository", new Object[] { repository.getDisplayName() }), // NOI18N
                        NbBundle.getMessage(RepositoryNode.class, "CTL_RemoveRepository"),      // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);

                    if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                            public void run() {
                                repository.remove();
                                RepositoriesSupport.getInstance().fireRepositoriesChanged();
                            }
                        });
                    }
                }
            }
        };
    }

}
