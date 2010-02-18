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

package org.netbeans.modules.bugtracking.kenai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bugtracking.jira.JiraUpdater;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle;
import org.netbeans.modules.kenai.ui.spi.QueryResultHandle.ResultType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Provides {@link QueryHandle}-s, {@link QueryResultHandle}-s and web access for
 * kenai projects with a JIRA issue tracker. To be used when no JIRA plugin is
 * available.
 *
 * @author Tomas Stupka
 */
class FakeJiraSupport {

    private static final String JIRA_SUBSTRING ="kenai.com/jira/"; // NOI18N

    private final String projectUrl;
    private final String createIssueUrl;
    private final String openIssueUrl;
    private static Map<String, FakeJiraSupport> supportedProjects = new HashMap<String, FakeJiraSupport>();
    private static List<QueryHandle> queryHandles;

    private FakeJiraSupport(String projectUrl, String createIssueUrl, String openIssueUrl) {
        this.projectUrl = projectUrl;
        this.createIssueUrl = createIssueUrl;
        this.openIssueUrl = openIssueUrl;
    }

    public static synchronized FakeJiraSupport get(ProjectHandle handle) {
        KenaiProject project = handle.getKenaiProject();
        if(project == null) {
            return null;
        }
        return get(project);
    }

    public static synchronized FakeJiraSupport get(KenaiProject project) {
        if(project == null) {
            return null;
        }
        FakeJiraSupport support = supportedProjects.get(project.getName());
        if(support != null) {
            return support;
        }
        
        String url = null;
        String createIssueUrl = null;
        String openIssueUrl = null;
        try {
            KenaiFeature[] features = project.getFeatures(KenaiService.Type.ISSUES);
            url = null;
            for (KenaiFeature f : features) {
                if (!KenaiService.Names.JIRA.equals(f.getService())) {
                    return null;
                }
                url = f.getLocation();
                break;
            }
        } catch (KenaiException kenaiException) {
            Exceptions.printStackTrace(kenaiException);
        }
        if(url == null) {
            return null;
        }
        int idx = url.indexOf(JIRA_SUBSTRING);
        if(idx > -1) {
            String urlBase = url.substring(0, idx + JIRA_SUBSTRING.length());
            createIssueUrl =
                    urlBase +
                    "secure/CreateIssue!default.jspa?pname=" + // NOI18N
                    project.getName();
            openIssueUrl =
                    urlBase +
                    "browse/"; // NOI18N
        }
        support = new FakeJiraSupport(url, createIssueUrl, openIssueUrl);
        supportedProjects.put(project.getName(), support);
        return support;
    }

    /**
     * Returns an action listerner for the "Create Issue" node in the Kenai dashboard.
     * The user is either notified about a missing jira pluging with the option to
     * download it or the create issue web page is opened in an external browser.
     *
     * @return an {@link ActionListener}
     */
    public ActionListener getCreateIssueListener() {
        return getJiraListener(createIssueUrl);
    }

    /**
     * Returns an action listerner for the "Find issues" node in the Kenai dashboard.
     * The user is either notified about a missing jira pluging with the option to
     * download it or the project web page is opened in an external browser.
     *
     * @return an {@link ActionListener}
     */
    public ActionListener getOpenProjectListener() {
        return getJiraListener(projectUrl);
    }

    /**
     * Opens the isssues web page in an external browser
     * @param issueID
     */
    public String getIssueUrl(String issueID) {
        return openIssueUrl + issueID;
    }

    /**
     * Returns a {@link QueryHandle} representing the "All Issues" query
     * @return
     */
    public QueryHandle getAllIssuesQuery() {
        List<QueryHandle> queries = getQueries();
        String allIssuesName = NbBundle.getMessage(FakeJiraSupport.class, "LBL_AllIssues");   // NOI18N
        for (QueryHandle queryHandle : queries) {
            if(queryHandle.getDisplayName().equals(allIssuesName)) {
                return queryHandle;
            }
        }
        return null;
    }

    /**
     * Returns a {@link QueryHandle} for each query from the project this
     * {@link FakeJiraSupport} was created for
     * 
     * @return
     */
    public List<QueryHandle> getQueries() {
        if(queryHandles == null) {
            queryHandles = createQueryHandles();
        }
        return queryHandles;
    }

    public static class FakeJiraQueryHandle extends QueryHandle implements QueryDescriptor, ActionListener {
        private final String displayName;
        private static List<QueryResultHandle> results;
        private final String projectUrl;
        private final String notAvailableResult;

        public FakeJiraQueryHandle(String displayName, String projectUrl) {
            this.displayName = displayName;
            this.projectUrl = projectUrl;
            notAvailableResult = NbBundle.getMessage(FakeJiraSupport.class, "LBL_NotAvailable");                     // NOI18N
        }
        @Override
        public String getDisplayName() {
            return displayName;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {}

        public void actionPerformed(ActionEvent e) {
            if(JiraUpdater.notifyJiraDownload(projectUrl)) {
                JiraUpdater.getInstance().downloadAndInstall();
            }
        }
        public List<QueryResultHandle> getQueryResults() {
            if(results == null) {
                List<QueryResultHandle> r = new ArrayList<QueryResultHandle>(1);
                r.add(new FakeJiraQueryResultHandle(notAvailableResult, ResultType.NAMED_RESULT, projectUrl));
                results = r;
            }
            return results; 
        }

        public QueryResultHandle getUnseenResult() {
            return new FakeJiraQueryResultHandle(notAvailableResult, ResultType.ALL_CHANGES_RESULT, projectUrl);
        }

        public boolean isPredefined() {
            return true;
        }

        public Query getQuery() {
            return null;
        }
    }

    public static class FakeJiraQueryResultHandle extends QueryResultHandle implements ActionListener {
        private final String label;
        private final ResultType type;
        private final String projectUrl;
        public FakeJiraQueryResultHandle(String label, ResultType type, String projectUrl) {
            this.label = label;
            this.type = type;
            this.projectUrl = projectUrl;
        }
        @Override
        public String getText() {
            return label;
        }
        @Override
        public String getToolTipText() {
            return NbBundle.getMessage(FakeJiraQueryHandle.class, "LBL_NotAvailableTooltip");   // NOI18N
        }
        public void actionPerformed(ActionEvent e) {
            if(JiraUpdater.notifyJiraDownload(projectUrl)) {
                JiraUpdater.getInstance().downloadAndInstall();
            }
        }
        @Override
        public ResultType getResultType() {
            return type;
        }
    }

    private List<QueryHandle> createQueryHandles() {
        List<QueryHandle> l = new ArrayList<QueryHandle>(2);
        l.add(new FakeJiraQueryHandle(NbBundle.getMessage(FakeJiraSupport.class, "LBL_MyIssues"), projectUrl));  // NOI18N
        l.add(new FakeJiraQueryHandle(NbBundle.getMessage(FakeJiraSupport.class, "LBL_AllIssues"), projectUrl)); // NOI18N
        return l;
    }

    private ActionListener getJiraListener(final String urlString) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(JiraUpdater.notifyJiraDownload(urlString)) {
                    JiraUpdater.getInstance().downloadAndInstall();
                }
            }
        };
    }
}
