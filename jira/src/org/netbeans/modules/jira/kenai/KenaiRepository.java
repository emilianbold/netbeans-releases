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

package org.netbeans.modules.jira.kenai;

import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.*;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugtracking.team.spi.TeamAccessor;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class KenaiRepository extends JiraRepository implements PropertyChangeListener {

    static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/kenai-small.png"; // NOI18N
    private Image icon;
    private String projectName;
    private KenaiQuery myIssues;
    private KenaiQuery allIssues;
    private String host;
    private final TeamProject kenaiProject;

    public KenaiRepository(TeamProject kenaiProject, String repoName, String url, String host, String project) {
        // use name for id, can't be changed anyway
        super(createInfo(repoName, url));
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.projectName = project;
        this.host = host;
        this.kenaiProject = kenaiProject;
        TeamAccessor kenaiAccessor = TeamUtil.getTeamAccessor(url);
        if (kenaiAccessor != null) {
            kenaiAccessor.addPropertyChangeListener(this, kenaiProject.getWebLocation().toString());
        }
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public JiraQuery createQuery() {
        FilterDefinition fd = new FilterDefinition();
        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return null;
        }
        Project project = configuration.getProjectByKey(projectName);
        fd.setProjectFilter(new ProjectFilter(project));
        KenaiQuery q = new KenaiQuery(null, this, fd, projectName, false, false);
        return q;
    }

    @Override
    public NbJiraIssue createIssue() {
        return super.createIssue(projectName);
    }

    @Override
    public synchronized Collection<JiraQuery> getQueries() {
        List<JiraQuery> ret = new LinkedList<JiraQuery>();
        ret.addAll(super.getQueries());
        ret.addAll(getDefinedQueries());
        return ret;
    }

    @Override
    public JiraQuery createPersistentQuery (String queryName, FilterDefinition filter) {
        return new KenaiQuery(queryName, this, filter, projectName, true, false);
    }

    public String getHost() {
        return host;
    }

    private Collection<JiraQuery> getDefinedQueries() {
        List<JiraQuery> queries = new ArrayList<JiraQuery>();

        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return Collections.emptyList();
        }

        JiraQuery mi = getMyIssuesQuery(configuration);
        if(mi != null) {
            queries.add(mi);
        }

        JiraQuery ai = getAllIssuesQuery(configuration);
        if(ai != null) {
            queries.add(ai);
        }

        return queries;
    }
    
    public TeamProject getKenaiProject() {
        return kenaiProject;
    }    

    public JiraQuery getMyIssuesQuery() throws MissingResourceException {
        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return null;
        }
        return getMyIssuesQuery(configuration);
    }

    synchronized private JiraQuery getMyIssuesQuery(JiraConfiguration configuration) throws MissingResourceException {
        if(myIssues == null) {
            Project p = configuration.getProjectByKey(projectName);
            if(p != null) {
                FilterDefinition fd = new FilterDefinition();
                fd.setAssignedToFilter(new CurrentUserFilter());
                fd.setProjectFilter(new ProjectFilter(p));
                fd.setStatusFilter(new StatusFilter(getOpenStatuses()));
                myIssues =
                    new KenaiQuery(
                        NbBundle.getMessage(KenaiRepository.class, "LBL_MyIssues"), // NOI18N
                        this,
                        fd,
                        projectName,
                        true,
                        true);
            } else {
                // XXX warning
            }
        }
        return myIssues;
    }

    public JiraQuery getAllIssuesQuery() throws MissingResourceException {
        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return null;
        }
        return getAllIssuesQuery(configuration);
    }

    synchronized private JiraQuery getAllIssuesQuery(JiraConfiguration configuration) throws MissingResourceException {
        if (allIssues == null) {
            Project p = configuration.getProjectByKey(projectName);
            if (p != null) {
                FilterDefinition fd = new FilterDefinition();
                fd.setProjectFilter(new ProjectFilter(p));
                fd.setStatusFilter(new StatusFilter(getOpenStatuses()));
                allIssues = new KenaiQuery(NbBundle.getMessage(KenaiRepository.class, "LBL_AllIssues"), this, fd, projectName, true, true);
            } 
        }
        return allIssues;
    }

    private JiraStatus[] getOpenStatuses() {
        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return new JiraStatus[0];
        }
        JiraStatus[] statuses = configuration.getStatuses();
        if(statuses == null || statuses.length == 0) {
            return new JiraStatus[0];
        }
        List<JiraStatus> ret = new ArrayList<JiraStatus>();
        for (JiraStatus s : statuses) {
            if("Open".equals(s.getName()) ||                                    // NOI18N
               "Reopened".equals(s.getName()) ||                                // NOI18N
               "In Progress".equals(s.getName()))                               // NOI18N
            {
                ret.add(s);
            }
        }
        return ret.toArray(new JiraStatus[ret.size()]);
    }

    @Override
    protected JiraConfiguration createConfiguration(JiraClient client) throws CoreException {
        KenaiConfiguration c = new KenaiConfiguration(client, this);
        c.addProject(projectName);
        return c;
    }

    protected void setCredentials(String user, char[] password) {
        super.setCredentials(user, password, null, null);
    }

    @Override
    protected void getRemoteFilters() {
        if(!TeamUtil.isLoggedIn(kenaiProject.getWebLocation())) {
            return;
        }
        super.getRemoteFilters();
    }

    @Override
    public void ensureCredentials() {
        authenticate(null);
    }
    
    @Override
    public boolean authenticate(String errroMsg) {
        PasswordAuthentication pa = TeamUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), true);
        if(pa == null) {
            return false;
        }

        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, password);

        return true;
    }

    private static String getKenaiUser(TeamProject kenaiProject) {
        PasswordAuthentication pa = TeamUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false);
        if(pa != null) {
            return pa.getUserName();
        }
        return "";                                                              // NOI18N
    }

    private static char[] getKenaiPassword(TeamProject kenaiProject) {
        PasswordAuthentication pa = TeamUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false);
        if(pa != null) {
            return pa.getPassword();
        }
        return new char[0];                                                              
    }

    /**
     * Returns null if key is not a valid Jira issue key or tries to add a project prefix to the key if the key is a number
     * @param key
     * @return
     */
    @Override
    protected String repairKeyIfNeeded (String key) {
        String retval = null;
        try {
            Long.parseLong(key);
            // problem
            // mylyn will interpret this key as an ID
            assert projectName != null;
            assert !"".equals(projectName);                             //NOI18N
            retval = projectName + "-" + key;                           //NOI18N
        } catch (NumberFormatException ex) {
            // this is good, no InsufficientRightsException will be thrown in mylyn
            retval = key;
        }
        return retval;
    }

    @Override
    protected ProjectFilter getProjectFilter() {
        ProjectFilter pf = null;
        JiraConfiguration config = getConfiguration();
        if (config != null) {
            Project p = config.getProjectByKey(projectName);
            assert p != null;
            pf = new ProjectFilter(p);
        }
        return pf;
    }

    @Override
    public Collection<RepositoryUser> getUsers() {
         Collection<RepositoryUser> users = TeamUtil.getProjectMembers(kenaiProject);
         if (users.isEmpty()) {
             // fallback - try cache
             users = super.getUsers();
         }
         return users;
    }

    private static String getRepositoryId(String name, String url) {
        return TextUtils.encodeURL(url) + ":" + name;                           // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(TeamAccessor.PROP_LOGIN)) {
//            if(myIssues != null) {
//                KenaiQueryController c = (KenaiQueryController) myIssues.getController();
//                c.populate(getQueryUrl());
//            }
            
            // XXX move to spi?
            // get kenai credentials
            String user;
            char[] psswd;
            PasswordAuthentication pa =
                TeamUtil.getPasswordAuthentication(kenaiProject.getWebLocation().toString(), false); // do not force login
            if(pa != null) {
                user = pa.getUserName();
                psswd = pa.getPassword();
            } else {
                user = "";                                                      // NOI18N
                psswd = new char[0]; 
            }

            setCredentials(user, psswd);
        }
    }
    private static RepositoryInfo createInfo(String repoName, String url) {
        String id = getRepositoryId(repoName, url);
        String tooltip = NbBundle.getMessage(JiraRepository.class, "LBL_RepositoryTooltip", new Object[] {repoName, url}); // NOI18N
        return new RepositoryInfo(id, JiraConnector.ID, url, repoName, tooltip);
    }
}
