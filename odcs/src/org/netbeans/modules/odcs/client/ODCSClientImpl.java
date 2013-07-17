package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.commons.net.AuthenticatedProxy;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.openide.util.NetworkSettings;

public class ODCSClientImpl implements ODCSClient {
    
    private final String url;
    private final PasswordAuthentication pa;
    private final AbstractWebLocation location;
    private final HttpClient httpClient = new HttpClient(WebUtil.getConnectionManager());
    private final NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    
    private final static Logger LOG = Logger.getLogger(ODCSClient.class.getName());
    
    public ODCSClientImpl(String url, PasswordAuthentication pa) {
        this.location = new WebLocation(pa, url);
        if (!url.endsWith("/")) { //NOI18N
            url = url + '/';
        }
        this.url = url;
        this.pa = pa;
        WebUtil.configureHttpClient(httpClient, "");
        httpClient.getParams().setAuthenticationPreemptive(true);
    }

    public <T> T runGet(Class<T> t, String service) throws ODCSException {
        return run(new GetMethod(url + service), t, service);
    }
    
    private <T> T runDelete(Class<T> t, String service) throws ODCSException {
        return run(new DeleteMethod(url + service), t, service);
    }

    private <T> T run(HttpMethodBase method, Class<T> t, String service) throws ODCSException {
        method.setDoAuthentication(true);
        try {
            HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, nullProgressMonitor);
            int result = WebUtil.execute(httpClient, hostConfiguration, method, nullProgressMonitor);
            if (result == HttpStatus.SC_OK) {
                InputStream in = WebUtil.getResponseBodyAsStream(method, nullProgressMonitor);
                try {
                    ObjectMapper m = new ObjectMapper();
                    m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return m.readValue(new InputStreamReader(in), t);
                } finally {
                    in.close();
                }
            } else if(result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                handleInternalServerError(method, service, result);
                return null;
            } else {
                throw new ODCSException(service + " returned http code : " + result);
            }
        } catch (IOException e) {
            throw new ODCSException(e);
        } finally {
            method.releaseConnection();
        }
    }
    
    public <T> T runPost(Class<T> t, String service, Object content) throws ODCSException {
        PostMethod method = new PostMethod(url + service);
        method.setDoAuthentication(true);
        method.setRequestHeader("Content-Type", "application/json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(content != null) {
                method.setRequestEntity(new StringRequestEntity(mapper.writeValueAsString(content), "application/json", "UTF-8"));
            }
        } catch (Exception e) {
            throw new ODCSException(e);
        } 
        try {
            HostConfiguration hostConfiguration = WebUtil.createHostConfiguration(httpClient, location, nullProgressMonitor);
            int result = WebUtil.execute(httpClient, hostConfiguration, method, nullProgressMonitor);
            if (result == HttpStatus.SC_OK) {
                InputStream in = WebUtil.getResponseBodyAsStream(method, nullProgressMonitor);
                try {
                    ObjectMapper m = new ObjectMapper();
                    m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    return m.readValue(new InputStreamReader(in), t);
                } finally {
                    in.close();
                }
            } else if(result == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                handleInternalServerError(method, service, result);
                return null;
            } else {
                throw new ODCSException(service + " returned http code : " + result);
            }
        } catch (IOException e) {
            throw new ODCSException(e);
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    public Profile getCurrentProfile() throws ODCSException {
        return runGet(ProfileWrapper.class, "api/profile").profile;
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        ProjectsQuery query = new ProjectsQuery(ProjectRelationship.MEMBER, null);
        ProjectQueryResultWrapper w = runPost(ProjectQueryResultWrapper.class, "api/projects/search", query);
        return w.queryResult.getResultPage();
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        return runGet(ProjectWrapper.class, "api/projects/" + projectId).project;
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        ProjectActivity[] l = runGet(ActivityWrapper.class, "api/activity/" + projectId).projectActivityList;
        return Arrays.asList(l);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        ProjectActivity[] l = runGet(ActivityWrapper.class, "api/activity/" + projectId + "/short").projectActivityList;
        return Arrays.asList(l);
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        ScmRepository[] l = runGet(RepositoryWrapper.class, "s/" + projectId + "/scm/api/repository").scmRepositoryList;
        return Arrays.asList(l);
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        return runGet(WatchingProjectWrapper.class, "api/projects/" + projectId + "/watch").isWatching;
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        ProjectsQuery query = new ProjectsQuery(pattern, null);
        ProjectQueryResultWrapper w = runPost(ProjectQueryResultWrapper.class, "api/projects/search", query);
        return w.queryResult.getResultPage();
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        runPost(WatchingProjectWrapper.class, "api/projects/" + projectId + "/unwatch", null);
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        runPost(WatchingProjectWrapper.class, "api/projects/" + projectId + "/watch", null);
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        ProjectsQuery query = new ProjectsQuery(ProjectRelationship.WATCHER, null);
        ProjectQueryResultWrapper w = runPost(ProjectQueryResultWrapper.class, "api/projects/search", query);
        return w.queryResult.getResultPage();
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        ProjectWrapper w = runPost(ProjectWrapper.class, "api/profile/project", project);
        Project p = w.project;
        if (w.project.getProjectServices() == null) {
            p = getProjectById(w.project.getIdentifier());
        }
        return p;
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        QueryWrapper w = runPost(QueryWrapper.class, "s/" + projectId + "/tasks/task/query", query);
        return w.savedTaskQuery;
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        QueryWrapper w = runPost(QueryWrapper.class, "s/" + projectId + "/tasks/task/query/" + query.getId(), query);
        return w.savedTaskQuery;
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        runDelete(QueryWrapper.class, "s/" + projectId + "/tasks/task/query/" + queryId);
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        RepositoryConfigurationWrapper w = runGet(RepositoryConfigurationWrapper.class, "s/" + projectId + "/tasks/repositoryContext");
        return w.repositoryConfiguration;
    }

    private void handleInternalServerError(HttpMethodBase method, String service, int result) throws ODCSException, IOException {
        InputStream in = WebUtil.getResponseBodyAsStream(method, nullProgressMonitor);
        try {
            ObjectMapper m = new ObjectMapper();
            m.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ErrorWrapper ew = m.readValue(new InputStreamReader(in), ErrorWrapper.class);
            LOG.log(Level.WARNING, "{0} returned http code : {1}", new Object[]{service, result});
            LOG.log(Level.WARNING, ew.error.getMessage());
            if(ew.error.getException() != null) {
                LOG.log(Level.OFF, service, ew.error.getException());
            }
            throw new ODCSException(ew.error.getMessage());
        } finally {
            in.close();
        }
    }

    private class WebLocation extends AbstractWebLocation {
        
        private final PasswordAuthentication pa;

        public WebLocation(PasswordAuthentication pa, String url) {
            super(url);
            this.pa = pa;
        }
        
        @Override
        public AuthenticationCredentials getCredentials(AuthenticationType at) {
            return new AuthenticationCredentials(pa.getUserName(), new String(pa.getPassword()));
        }

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
