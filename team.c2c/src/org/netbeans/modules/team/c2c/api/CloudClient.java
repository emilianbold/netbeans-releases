/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.c2c.api;

import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.profile.service.ActivityServiceClient;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 *
 * @author ondra
 */
public final class CloudClient {
    private final ProfileWebServiceClient profileClient;
    private static final String PROFILE_SERVICE = "alm/api"; //NOI18N
    private final AbstractWebLocation location;
    private final ActivityServiceClient activityClient;

    CloudClient (ProfileWebServiceClient profileClient, ActivityServiceClient activityClient, AbstractWebLocation location) {
        this.profileClient = profileClient;
        this.activityClient = activityClient;
        this.location = location;
    }

    public Profile getCurrentProfile () throws CloudException {
        return run(new Callable<Profile> () {
            @Override
            public Profile call () throws Exception {
                return profileClient.getCurrentProfile();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public Project getProjectById (final String projectId) throws CloudException {
        return run(new Callable<Project> () {
            @Override
            public Project call () throws Exception {
                return profileClient.getProjectByIdentifier(projectId);
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public List<Project> getMyProjects () throws CloudException {
        return run(new Callable<List<Project>> () {
            @Override
            public List<Project> call () throws Exception {
                ProjectsQuery query = new ProjectsQuery(ProjectRelationship.MEMBER, null);
                QueryResult<Project> res = profileClient.findProjects(query);
                return res.getResultPage();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public boolean isWatchingProject (final String projectId) throws CloudException {
        return Boolean.TRUE.equals(run(new Callable<Boolean> () {
            @Override
            public Boolean call () throws Exception {
                return profileClient.isWatchingProject(projectId);
            }
        }, profileClient, PROFILE_SERVICE));
    }

    public List<Project> searchProjects (final String pattern) throws CloudException {
        return run(new Callable<List<Project>> () {
            @Override
            public List<Project> call () throws Exception {
                ProjectsQuery query = new ProjectsQuery(pattern, new QueryRequest());
                QueryResult<Project> res = profileClient.findProjects(query);
                return res.getResultPage();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public void unwatchProject (final String projectId) throws CloudException {
        run(new Callable<Void> () {
            @Override
            public Void call () throws Exception {
                profileClient.unwatchProject(projectId);
                return null;
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public void watchProject (final String projectId) throws CloudException {
        run(new Callable<Void> () {
            @Override
            public Void call () throws Exception {
                profileClient.watchProject(projectId);
                return null;
            }
        }, profileClient, PROFILE_SERVICE);
    }

    public List<ProjectActivity> getRecentActivities (final Project project) throws CloudException {
        return run(new Callable<List<ProjectActivity>> () {
            @Override
            public List<ProjectActivity> call () throws Exception {
                return activityClient.getRecentActivity(project.getIdentifier());
            }
        }, activityClient, PROFILE_SERVICE);
    }

    public List<ProjectActivity> getRecentShortActivities (final Project project) throws CloudException {
        return run(new Callable<List<ProjectActivity>> () {
            @Override
            public List<ProjectActivity> call () throws Exception {
                return activityClient.getShortActivityList(project.getIdentifier());
            }
        }, activityClient, PROFILE_SERVICE);
    }

    private <T> T run (Callable<T> callable, AbstractRestServiceClient client, String service) throws CloudException {
        try {
            Authentication auth = null;
            AuthenticationCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
            if (credentials != null && !credentials.getUserName().trim().isEmpty()) {
                String password = credentials.getPassword();
                auth = new UsernamePasswordAuthenticationToken(new User(credentials.getUserName(), password, true, true, true, true, 
                    Collections.EMPTY_LIST), password);
            }
            SecurityContextHolder.getContext().setAuthentication(auth);
            try {
                client.setBaseUrl(location.getUrl() + service);
                return callable.call();
            } finally {
                client.setBaseUrl(location.getUrl());
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        } catch (Exception ex) {
            throw new CloudException(ex);
        }
    }
    
}
