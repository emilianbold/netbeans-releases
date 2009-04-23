/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira;

import org.netbeans.modules.jira.repository.JiraRepository;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentListener;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.jira.core.JiraRepositoryConnector;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka
 */
public class JiraQuery extends Query {

    private String name;
    private final JiraRepositoryConnector jrc;
    private final JiraRepository repository;
    private FilterDefinition filterDefinition;
    private QueryPanel queryPanel; // XXX this is crap
    private List<JiraIssue> jiraIssues;
    private Controller controller;

    public JiraQuery(JiraRepositoryConnector jrc, JiraRepository repository) {
        this.jrc = jrc;
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public boolean refresh() {
        try {
            if (filterDefinition == null) {
                // XXX
            }
            jiraIssues = list(filterDefinition);
            return true;
        } catch (JiraException ex) {
            // XXX exceptions
            Jira.LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void setFilterDefinition(FilterDefinition filterDefinition) {
        this.filterDefinition = filterDefinition;
    }

    private Issue[] getIssuesIntern() {
        if (jiraIssues == null) {
            return new Issue[0];
        }
        List<Issue> issues = new ArrayList<Issue>(jiraIssues.size());
        for (JiraIssue i : jiraIssues) {
            issues.add(new NbJiraIssue(i, repository));
        }
        return issues.toArray(new NbJiraIssue[issues.size()]);
    }

    private List<JiraIssue> list(FilterDefinition fd) throws JiraException {
        JiraCollector c = new JiraCollector();
        Jira.getInstance().getClient(repository.getTaskRepository()).findIssues(fd, c, new NullProgressMonitor());
        return c.issues; //tdc.data;
    }

    public Issue[] getIssues(boolean refresh) {
        if(refresh) refresh();
        return getIssuesIntern();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " : " + repository.getTaskRepository().getCredentials(AuthenticationType.REPOSITORY).getUserName() + "@" + repository.getUrl();
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new Controller();
            controller.populate();                 // XXX Async
        }
        return controller;
    }

    @Override
    public Repository getRepository() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Issue[] getIssues(int includeStatus) {
        return getIssuesIntern();
    }

    @Override
    public boolean contains(Issue issue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ColumnDescriptor[] getColumnDescriptors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIssueStatus(Issue issue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class JiraCollector implements IssueCollector {
        private List<JiraIssue> issues = new ArrayList<JiraIssue>();
        public void start() { }
        public void done() { }
        public void collectIssue(org.eclipse.mylyn.internal.jira.core.model.JiraIssue issue) {
            issues.add(issue);
        }
        public boolean isCancelled() {
            return false; // XXX
        }
        public int getMaxHits() {
            return -1; // XXX
        }
    }

    private class Controller extends BugtrackingController implements DocumentListener, ItemListener {
        private QueryPanel queryPanel = new QueryPanel();

        public Controller() {
            queryPanel.nameField.getDocument().addDocumentListener(this);
            queryPanel.projectCbo.addItemListener(this);
        }

        @Override
        public JComponent getComponent() {
            return queryPanel;
        }

        @Override
        public boolean isValid() {
            return queryPanel.projectCbo.getSelectedItem() != null &&
                   !queryPanel.nameField.getText().trim().equals("");
        }

        @Override
        public void applyChanges() {
            JiraQuery.this.name = queryPanel.nameField.getText();
            FilterDefinition fd = new FilterDefinition();
            fd.setProjectFilter(new ProjectFilter((Project) queryPanel.projectCbo.getSelectedItem()));
            JiraQuery.this.setFilterDefinition(fd);
        }

        void populate() {
            try {
                Project[] projects = Jira.getInstance().getProjects(Jira.getInstance().getClient(repository.getTaskRepository()));
                queryPanel.projectCbo.setModel(new DefaultComboBoxModel(projects));
            } catch (JiraException ex) {
                Jira.LOG.log(Level.SEVERE, null, ex);
            }
        }

        public void insertUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void changedUpdate(DocumentEvent e) {
            fireDataChanged();
        }

        public void itemStateChanged(ItemEvent e) {
            fireDataChanged();
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
