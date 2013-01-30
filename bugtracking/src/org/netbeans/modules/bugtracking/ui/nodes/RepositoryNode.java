/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.query.QueryAction;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryNode extends AbstractNode implements PropertyChangeListener {
    private RepositoryImpl repository;

    public RepositoryNode(RepositoryImpl repository) {
        super(Children.LEAF);
        this.repository = repository;
        setName(repository.getDisplayName());
        repository.addPropertyChangeListener(this);
    }

    @Override
    public Image getIcon(int type) {
        return repository.getIcon();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            new AbstractAction(SystemAction.get(QueryAction.class).getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    QueryAction.openQuery(null, repository);
                }
            },
            new AbstractAction(SystemAction.get(IssueAction.class).getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    IssueAction.createIssue(repository);
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_EditRepository")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    BugtrackingUtil.editRepository(repository.getRepository());
                }
            },
            new AbstractAction(NbBundle.getMessage(BugtrackingRootNode.class, "LBL_RemoveRepository")) { // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(RepositoryNode.class, "MSG_RemoveRepository", new Object[] { repository.getDisplayName() }), // NOI18N
                        NbBundle.getMessage(RepositoryNode.class, "CTL_RemoveRepository"),      // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);

                    if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                repository.remove();
                            }
                        });
                    }
                }
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.EVENT_ATTRIBUTES_CHANGED)) {
            Map<String, String> oldMap = (Map<String, String>) evt.getOldValue();
            Map<String, String> newMap = (Map<String, String>) evt.getNewValue();
            if(oldMap.containsKey(Repository.ATTRIBUTE_DISPLAY_NAME)) {
                super.setDisplayName(newMap.get(Repository.ATTRIBUTE_DISPLAY_NAME));
            }
        }
    }

}
