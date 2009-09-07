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

package org.netbeans.modules.jira.kenai;

import java.awt.Image;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.filter.CurrentUserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class KenaiRepository extends JiraRepository {

    static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/kenai-small.png"; // NOI18N
    private Image icon;
    private String projectName;
    private KenaiQuery myIssues;
    private KenaiQuery allIssues;
    private String host;
    private final KenaiProject kenaiProject;

    public KenaiRepository(KenaiProject kenaiProject, String repoName, String url, String host, String project) {
        // use name for id, can't be changed anyway
        super(repoName, repoName, url, getKenaiUser(), getKenaiPassword(), null, null);
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.projectName = project;
        this.host = host;
        this.kenaiProject = kenaiProject;
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public Query createQuery() {
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
    public Issue createIssue() {
        return super.createIssue();
    }

    @Override
    public synchronized Query[] getQueries() {
        Query[] qs = super.getQueries();
        Query[] dq = getDefinedQueries();
        Query[] ret = new Query[qs.length + dq.length];
        System.arraycopy(qs, 0, ret, 0, qs.length);
        System.arraycopy(dq, 0, ret, qs.length, dq.length);
        return ret;
    }

    @Override
    protected Object[] getLookupObjects() {
        Object[] obj = super.getLookupObjects();
        Object[] obj2 = new Object[obj.length + 1];
        System.arraycopy(obj, 0, obj2, 0, obj.length);
        obj2[obj.length] = kenaiProject;
        return obj2;
    }
    
    private Query[] getDefinedQueries() {
        List<Query> queries = new ArrayList<Query>();

        JiraConfiguration configuration = getConfiguration();
        if(configuration == null) {
            return new Query[0];
        }

        // my issues - only if logged in
        if(KenaiUtil.isLoggedIn()) {
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
            queries.add(myIssues);
        }

        // all issues
        if(allIssues == null) {
            Project p = configuration.getProjectByKey(projectName);
            if(p != null) {
                FilterDefinition fd = new FilterDefinition();
                fd.setProjectFilter(new ProjectFilter(p));
                fd.setStatusFilter(new StatusFilter(getOpenStatuses()));
                allIssues =
                    new KenaiQuery(
                        NbBundle.getMessage(KenaiRepository.class, "LBL_AllIssues"), // NOI18N
                        this,
                        fd,
                        projectName,
                        true,
                        true);
            } else {
                // XXX warning
            }
        }
        if(allIssues != null) queries.add(allIssues);
        return queries.toArray(new Query[queries.size()]);
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
    protected JiraConfiguration createConfiguration(JiraClient client) {
        KenaiConfiguration c = new KenaiConfiguration(client, this);
        if(c != null) {
            c.addProject(projectName);
            return c;
        }
        return null;        
    }

    protected void setCredentials(String user, String password) {
        super.setCredentials(user, password, null, null);
    }

    @Override
    public boolean authenticate(String errroMsg) {
        PasswordAuthentication pa = org.netbeans.modules.bugtracking.util.KenaiUtil.getPasswordAuthentication(true);
        if(pa == null) {
            return false;
        }

        String user = pa.getUserName();
        char[] password = pa.getPassword();

        setCredentials(user, new String(password));

        return true;
    }

    private static String getKenaiUser() {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(false);
        if(pa != null) {
            return pa.getUserName();
        }
        return "";                                                              // NOI18N
    }

    private static String getKenaiPassword() {
        PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(false);
        if(pa != null) {
            return new String(pa.getPassword());
        }
        return "";                                                              // NOI18N
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
         Collection<RepositoryUser> users = KenaiUtil.getProjectMembers(projectName.toLowerCase());
         if (users.isEmpty()) {
             // fallback - try cache
             users = super.getUsers();
         }
         return users;
    }

}
