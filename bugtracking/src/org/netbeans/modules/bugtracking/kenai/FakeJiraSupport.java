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

import org.netbeans.modules.bugtracking.jira.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.kenai.QueryAccessorImpl;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
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
 *
 * @author Tomas Stupka
 */
public class FakeJiraSupport {

    private static final String JIRA_SUBSTRING ="kenai.com/jira/"; // NOI18N

    private String projectUrl;
    private String createIssueUrl;
    private static Map<String, FakeJiraSupport> supportedProjects = new HashMap<String, FakeJiraSupport>();
    private static List<QueryHandle> queryHandles;

    private FakeJiraSupport(String projectUrl, String createIssueUrl) {
        this.projectUrl = projectUrl;
        this.createIssueUrl = createIssueUrl;
    }

    public static synchronized FakeJiraSupport get(ProjectHandle handle) {
        FakeJiraSupport support = supportedProjects.get(handle.getId());
        if(support != null) {
            return support;
        }
        KenaiProject project = KenaiUtil.getKenaiProject(handle);
        if(project == null) {
            return null;
        }
        String url = null;
        String issueUrl = null;
        try {
            KenaiFeature[] features = project.getFeatures(KenaiService.Type.ISSUES);
            url = null;
            issueUrl = null;
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
            issueUrl =
                    url.substring(0, idx + JIRA_SUBSTRING.length()) +
                    "secure/CreateIssue!default.jspa?pname=" + // NOI18N
                    project.getName();

        }
        support = new FakeJiraSupport(url, issueUrl);
        supportedProjects.put(handle.getId(), support);
        return support;
    }

    public ActionListener getCreateIssueListener() {
        return getJiraListener(createIssueUrl);
    }

    public ActionListener getOpenProjectListener() {
        return getJiraListener(projectUrl);
    }

    public static void openIsssueOnWeb() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public QueryHandle getAllIssuesQuery() {
        List<QueryHandle> queries = getQueries();
        String allIssuesName = NbBundle.getMessage(QueryAccessorImpl.class, "LBL_AllIssues");   // NOI18N
        for (QueryHandle queryHandle : queries) {
            if(queryHandle.getDisplayName().equals(allIssuesName)) {
                return queryHandle;
            }
        }
        return null;
    }

    public List<QueryHandle> getQueries() {
        if(queryHandles == null) {
            queryHandles = createQueryHandles();
        }
        return queryHandles;
    }

    private List<QueryHandle> createQueryHandles() {
        List<QueryHandle> l = new ArrayList<QueryHandle>(2);
        l.add(new FakeJiraQueryHandle(NbBundle.getMessage(QueryAccessorImpl.class, "LBL_MyIssues")));  // NOI18N
        l.add(new FakeJiraQueryHandle(NbBundle.getMessage(QueryAccessorImpl.class, "LBL_AllIssues"))); // NOI18N
        return l;
    }

    private ActionListener getJiraListener(String urlString) {
        final URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
                    public void run() {
                        JiraUpdater.notifyJiraDownload();
                    }
                });
            }
        };
    }

    public static class FakeJiraQueryHandle extends QueryHandle implements ActionListener {
        private final String displayName;
        private static List<QueryResultHandle> results;
        public FakeJiraQueryHandle(String displayName) {
            this.displayName = displayName;
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
            JiraUpdater.notifyJiraDownload();
        }
        public List<QueryResultHandle> getQueryResults() {
            if(results == null) {
                List<QueryResultHandle> r = new ArrayList<QueryResultHandle>(1);
                r.add(new FakeJiraQueryResultHandle(
                            NbBundle.getMessage(
                                QueryAccessorImpl.class,
                                "LBL_QueryResultTotal",  // NOI18N
                                new Object[] {0}),
                                ResultType.NAMED_RESULT));
                results = r;
            }
            return results; 
        }

        public QueryResultHandle getUnseenResult() {
            return new FakeJiraQueryResultHandle("0", ResultType.ALL_CHANGES_RESULT); // NOI18N
        }
    }

    public static class FakeJiraQueryResultHandle extends QueryResultHandle implements ActionListener {
        private final String label;
        private final ResultType type;
        public FakeJiraQueryResultHandle(String label, ResultType type) {
            this.label = label;
            this.type = type;
        }
        @Override
        public String getText() {
            return label;
        }
        public void actionPerformed(ActionEvent e) {
            JiraUpdater.notifyJiraDownload();
        }
        @Override
        public ResultType getResultType() {
            return type;
        }
    }
}
