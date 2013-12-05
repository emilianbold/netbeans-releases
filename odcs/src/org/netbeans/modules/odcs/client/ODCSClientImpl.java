package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.ApacheHttpRestClientDelegate;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.profile.service.ActivityServiceClient;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.service.ScmServiceClient;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
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
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
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
    private ActivityServiceClient actvityServiceClient;
    private ProfileWebServiceClient profileServiceClient;
    private ScmServiceClient scmServiceClient;
    private TaskServiceClient tasksServiceClient;
    
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

    @Override
    public Profile getCurrentProfile() throws ODCSException {
        return getProfileClient().getCurrentProfile();
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        QueryResult<Project> r = getProfileClient().findProjects(new ProjectsQuery(ProjectRelationship.MEMBER, null));
        return r != null ? r.getResultPage() : null;
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        try {
            return getProfileClient().getProjectByIdentifier(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        return getActivityClient().getRecentActivity(projectId);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        return getActivityClient().getShortActivityList(projectId);
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        try {
            return getScmClient(projectId).getScmRepositories();
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        try {
            return getProfileClient().isWatchingProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        QueryResult<Project> r = getProfileClient().findProjects(new ProjectsQuery(pattern, null));
        return r != null ? r.getResultPage() : null;
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        try {
            getProfileClient().unwatchProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        try {
            getProfileClient().watchProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        QueryResult<Project> r = getProfileClient().findProjects(new ProjectsQuery(ProjectRelationship.WATCHER, null));
        return r != null ? r.getResultPage() : null;
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        try {
            return getProfileClient().createProject(project);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        }
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        try {
            return getTasksClient(projectId).createQuery(query);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        try {
            return getTasksClient(projectId).updateQuery(query);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        // XXX UnsupportedOperationException !!!
//        try {
//            getTasksClient(projectId).deleteQuery(queryId);
//        } catch (ValidationException ex) {
//            throw new ODCSException(ex);
//        } catch (EntityNotFoundException ex) {
//            throw new ODCSException(ex);
//        }
        runDelete(QueryWrapper.class, "s/" + projectId + "/tasks/task/query/" + queryId);
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        return getTasksClient(projectId).getRepositoryContext();
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
                LOG.log(Level.WARNING, service, ew.error.getException());
            }
            throw new ODCSException(ew.error.getMessage());
        } finally {
            in.close();
        }
    }

    private ActivityServiceClient getActivityClient() {
        if (actvityServiceClient == null) {
            actvityServiceClient = new ActivityServiceClient();
            actvityServiceClient.setBaseUrl(url + "api/activity/");
            ApacheHttpRestClientDelegate delegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
            actvityServiceClient.setRestClientDelegate(delegate);
        }
        return actvityServiceClient;
    }
    
    private ProfileWebServiceClient getProfileClient() {
        if (profileServiceClient == null) {
            profileServiceClient = new ProfileWebServiceClient();
            profileServiceClient.setBaseUrl(url + "api/");
            ApacheHttpRestClientDelegate delegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
            profileServiceClient.setRestClientDelegate(delegate);
        }
        return profileServiceClient;
    }

    private ScmServiceClient getScmClient(String projectId) {
        if (scmServiceClient == null) {
            scmServiceClient = new ScmServiceClient();
            scmServiceClient.setBaseUrl(url + "s/" + projectId + "/scm/api/");
            ApacheHttpRestClientDelegate delegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
            scmServiceClient.setRestClientDelegate(delegate);
        }
        return scmServiceClient;        
    }

    private TaskServiceClient getTasksClient(String projectId) {
        if (tasksServiceClient == null) {
            tasksServiceClient = new TaskServiceClient();
            tasksServiceClient.setBaseUrl(url + "s/" + projectId + "/tasks/");
            ApacheHttpRestClientDelegate delegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
            tasksServiceClient.setRestClientDelegate(delegate);
        }
        return tasksServiceClient;     
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
