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
package org.netbeans.modules.ods;

import com.tasktop.c2c.client.commons.client.CredentialsInjector;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.AbstractRestServiceClient;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.profile.service.ActivityServiceClient;
import com.tasktop.c2c.server.profile.service.HudsonServiceClient;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.service.ScmServiceClient;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.IProxyProvider;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.commons.net.AuthenticatedProxy;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;
import org.openide.util.NetworkSettings;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author ondra
 */
public final class ODSClientImpl implements ODSClient {
    private ProfileWebServiceClient profileClient;
    private static final String PROFILE_SERVICE = "api"; //NOI18N
    private static final String HUDSON_SERVICE = "s/%s/hudson"; //NOI18N
    private static final String SCM_SERVICE = "s/%s/scm/api"; //NOI18N
    private AbstractWebLocation location;
    private ActivityServiceClient activityClient;
    private HudsonServiceClient hudsonClient;
    private ScmServiceClient scmClient;

    private static ClassPathXmlApplicationContext appContext;
    
    public ODSClientImpl(String url, PasswordAuthentication auth) {
        if (!url.endsWith("/")) { //NOI18N
            url = url + '/';
        }
        location = new WebLocation(url, 
                auth.getUserName(), 
                auth.getPassword() == null ? "" : new String(auth.getPassword()), 
                new ProxyProvider());
        // maybe proxy credentials weel need to be provided somehow
        ClassPathXmlApplicationContext context = getContext();
        profileClient = context.getBean(ProfileWebServiceClient.class);
        activityClient = context.getBean(ActivityServiceClient.class);
        hudsonClient = context.getBean(HudsonServiceClient.class);
        scmClient = context.getBean(ScmServiceClient.class);
        CredentialsInjector.configureRestTemplate(location, (RestTemplate) context.getBean(RestTemplate.class));
    }

    @Override
    public Profile getCurrentProfile () throws ODSException {
        return run(new Callable<Profile> () {
            @Override
            public Profile call () throws Exception {
                return profileClient.getCurrentProfile();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public Project getProjectById (final String projectId) throws ODSException {
        return run(new Callable<Project> () {
            @Override
            public Project call () throws Exception {
                return profileClient.getProjectByIdentifier(projectId);
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public List<Project> getMyProjects () throws ODSException {
        return run(new Callable<List<Project>> () {
            @Override
            public List<Project> call () throws Exception {
                ProjectsQuery query = new ProjectsQuery(ProjectRelationship.MEMBER, null);
                QueryResult<Project> res = profileClient.findProjects(query);
                return res.getResultPage();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public List<Project> getWatchedProjects () throws ODSException {
        return run(new Callable<List<Project>> () {
            @Override
            public List<Project> call () throws Exception {
                ProjectsQuery query = new ProjectsQuery(ProjectRelationship.WATCHER, null);
                QueryResult<Project> res = profileClient.findProjects(query);
                return res.getResultPage();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public boolean isWatchingProject (final String projectId) throws ODSException {
        return Boolean.TRUE.equals(run(new Callable<Boolean> () {
            @Override
            public Boolean call () throws Exception {
                return profileClient.isWatchingProject(projectId);
            }
        }, profileClient, PROFILE_SERVICE));
    }

    @Override
    public List<Project> searchProjects (final String pattern) throws ODSException {
        return run(new Callable<List<Project>> () {
            @Override
            public List<Project> call () throws Exception {
                ProjectsQuery query = new ProjectsQuery(pattern, new QueryRequest());
                QueryResult<Project> res = profileClient.findProjects(query);
                return res.getResultPage();
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public void unwatchProject (final String projectId) throws ODSException {
        run(new Callable<Void> () {
            @Override
            public Void call () throws Exception {
                profileClient.unwatchProject(projectId);
                return null;
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public void watchProject (final String projectId) throws ODSException {
        run(new Callable<Void> () {
            @Override
            public Void call () throws Exception {
                profileClient.watchProject(projectId);
                return null;
            }
        }, profileClient, PROFILE_SERVICE);
    }

    @Override
    public List<ProjectActivity> getRecentActivities (final String projectId) throws ODSException {
        return run(new Callable<List<ProjectActivity>> () {
            @Override
            public List<ProjectActivity> call () throws Exception {
                return activityClient.getRecentActivity(projectId);
            }
        }, activityClient, PROFILE_SERVICE);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities (final String projectId) throws ODSException {
        return run(new Callable<List<ProjectActivity>> () {
            @Override
            public List<ProjectActivity> call () throws Exception {
                return activityClient.getShortActivityList(projectId);
            }
        }, activityClient, PROFILE_SERVICE);
    }

    @Override
    public HudsonStatus getHudsonStatus (String projectId) throws ODSException {
        return run(new Callable<HudsonStatus> () {
            @Override
            public HudsonStatus call () throws Exception {
                return hudsonClient.getStatus();
            }
        }, hudsonClient, buildUrl(HUDSON_SERVICE, projectId));
    }

    @Override
    public JobDetails getJobDetails (String projectId, final String jobName) throws ODSException {
        return run(new Callable<JobDetails> () {
            @Override
            public JobDetails call () throws Exception {
                return hudsonClient.getJobDetails(jobName);
            }
        }, hudsonClient, buildUrl(HUDSON_SERVICE, projectId));
    }

    @Override
    public BuildDetails getBuildDetails (String projectId, final String jobName, final int buildNumber) throws ODSException {
        return run(new Callable<BuildDetails> () {
            @Override
            public BuildDetails call () throws Exception {
                return hudsonClient.getBuildDetails(jobName, buildNumber);
            }
        }, hudsonClient, buildUrl(HUDSON_SERVICE, projectId));
    }
    
    @Override
    public List<ScmRepository> getScmRepositories (String projectId) throws ODSException {
        return run(new Callable<List<ScmRepository>> () {
            @Override
            public List<ScmRepository> call () throws Exception {
                return scmClient.getScmRepositories();
            }
        }, scmClient, buildUrl(SCM_SERVICE, projectId));
    }

    @Override
    public Project createProject (final Project project) throws ODSException {
        return run(new Callable<Project> () {
            @Override
            public Project call () throws Exception {
                Project p = profileClient.createProject(project);
                if (p.getProjectServices() == null) {
                    p = profileClient.getProjectByIdentifier(p.getIdentifier());
                }
                return p;
            }
        }, profileClient, PROFILE_SERVICE);
    }

    private <T> T run (Callable<T> callable, AbstractRestServiceClient client, String service) throws ODSException {
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
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof InterruptedException) {
                throw new ODSException.ODSCanceledException(ex);
            } else {
                throw new ODSException(ex);
            }
        } catch (Exception ex) {
            throw new ODSException(ex);
        }
    }
    
    private static String buildUrl (String urlTemplate, String projectName) {
        return String.format(urlTemplate, projectName);
    }

    private static ClassPathXmlApplicationContext createContext(String[] resourceNames, ClassLoader classLoader) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.setClassLoader(classLoader);
        context.setConfigLocations(resourceNames);
        context.refresh();
        return context;
    }

    private static ClassPathXmlApplicationContext getContext () {
        if (appContext == null) {
            appContext = createContext(new String[] { 
                "org/netbeans/modules/ods/defs-clients.xml" }, Thread.currentThread().getContextClassLoader());
        }
        return appContext;
    }
    
    private static class ProxyProvider implements IProxyProvider {

	@Override
	public Proxy getProxyForHost(String host, String proxyType) {
            try {
                String scheme = null;
                if (IProxyData.HTTPS_PROXY_TYPE.equals(proxyType)) {
                    scheme = "https://"; //NOI18N
                } else if (IProxyData.HTTP_PROXY_TYPE.equals(proxyType)) {
                    scheme = "http://"; //NOI18N
                }
                if (scheme != null) {
                    URI uri = new URI(scheme + host);
                    List<Proxy> select = ProxySelector.getDefault().select(uri);
                    if (select.size() > 0) {
                        Proxy p = select.get(0);
                        String uname = NetworkSettings.getAuthenticationUsername(uri);
                        if (uname != null && !uname.trim().isEmpty()) {
                            String pwdkey = NetworkSettings.getKeyForAuthenticationPassword(uri);
                            char[] pwd = null;
                            if (pwdkey != null && !pwdkey.trim().isEmpty()) {
                                pwd = Keyring.read(pwdkey);
                            }
                            if (pwd != null) {
                                p = new AuthenticatedProxy(p.type(), p.address(), uname, new String(pwd));
                                Arrays.fill(pwd, (char) 0);
                            }
                        }
                        return p;
                    }
                }
            } catch (URISyntaxException ex) {
            }
            return Proxy.NO_PROXY;
	}
    }    
}
