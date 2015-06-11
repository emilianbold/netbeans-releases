package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.web.ApacheHttpRestClientDelegate;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.profile.service.ProfileWebServiceClient;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.service.ScmServiceClient;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import com.tasktop.c2c.server.tasks.service.TaskServiceClient;
import oracle.clouddev.server.profile.activity.client.api.ActivityApi;
import oracle.clouddev.server.profile.activity.client.api.ListRequestParams;
import oracle.clouddev.server.profile.activity.client.rest.ActivityApiClient;
import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.team.commons.LogUtils;
import org.openide.util.Lookup;

public class ODCSClientImpl implements ODCSClient {
    
    private final String url;
    private final String organizationId;
    private final PasswordAuthentication pa;
    private final HttpClient httpClient = new HttpClient(WebUtil.getConnectionManager());
    
    private final static Logger LOG = Logger.getLogger(ODCSClient.class.getName());
    private ActivityApi activityClient;
    private ProfileWebServiceClient profileServiceClient;
    private ScmServiceClient scmServiceClient;
    private TaskServiceClient tasksServiceClient;
    private final MockUpODCSClient mockDelegate;
    
    public ODCSClientImpl(String url, PasswordAuthentication pa) {
        if (!url.endsWith("/")) { //NOI18N
            url = url + '/';
        }
        this.url = url;
        String[] splitUrl = url.split("/"); // NOI18N
        this.organizationId = splitUrl != null && splitUrl.length > 0 ? splitUrl[splitUrl.length-1] : null;
        this.pa = pa;
        WebUtil.configureHttpClient(httpClient, "");
        httpClient.getParams().setAuthenticationPreemptive(true);
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Initialized ODCSClient for {0} u: {1} p:{2}", new Object[]{url, pa.getUserName(), LogUtils.getPasswordLog(pa.getPassword())});
        }
        
        mockDelegate = "http://mockingbird/".equals(url) ? new MockUpODCSClient() : null;
    }

    @Override
    public Profile getCurrentProfile() throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getCurrentProfile();
            }
            
            return getProfileClient().getCurrentProfile();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch(RuntimeException e) {
            throw new ODCSException(e);
        }
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getMyProjects();
            }
            
            QueryResult<Project> r = getProfileClient().findProjects(createProjectsQuery(ProjectRelationship.MEMBER));
            return r != null ? r.getResultPage() : null;
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (RuntimeException ex) {
            throw new ODCSException(ex);
        }    
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getProjectById(projectId);
            }
                        
            return getProfileClient().getProjectByIdentifier(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }    
    }

    @Override
    public List<Activity> getRecentActivities(String projectId) throws ODCSException {
        ClassLoader originalContextCL = null;
        try {
            if (mockDelegate != null) {
                return mockDelegate.getRecentActivities(projectId);
            }
            originalContextCL = setupContextClassLoader();

            ListRequestParams params = new ListRequestParams(Collections.EMPTY_LIST, 0, 100);
            oracle.clouddev.server.profile.activity.client.api.QueryResult r = getActivityClient().list(projectId, params);
            return r.getActivities();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (ValidationException e) {
            throw new ODCSException(e);
        } catch(RuntimeException e) {
            throw new ODCSException(e);
        } finally {
            restoreContextClassLoader(originalContextCL);
        }
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getScmRepositories(projectId);
            }
            
            return getScmClient(projectId).getScmRepositories();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.isWatchingProject(projectId);
            }
            
            return getProfileClient().isWatchingProject(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        if(pattern != null && "".equals(pattern.trim())) {
            pattern = null;
        }
        
        try {
            if(mockDelegate != null) {
                return mockDelegate.searchProjects(pattern);
            }
            
            ProjectsQuery q = createProjectsQuery(ProjectRelationship.ALL);
            q.setQueryString(pattern);
            QueryResult<Project> r = getProfileClient().findProjects(q);
            return r != null ? r.getResultPage() : null;
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch(RuntimeException e) {
            throw new ODCSException(e);
        }
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                mockDelegate.unwatchProject(projectId);
            }
            
            getProfileClient().unwatchProject(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                mockDelegate.watchProject(projectId);
            }
            
            getProfileClient().watchProject(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getWatchedProjects();
            }
            
            QueryResult<Project> r = getProfileClient().findProjects(createProjectsQuery(ProjectRelationship.WATCHER));
            return r != null ? r.getResultPage() : null;
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch(RuntimeException e) {
            throw new ODCSException(e);
        }
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.createProject(project);
            }
            
            return getProfileClient().createProject(project);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (EntityNotFoundException | ValidationException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.createQuery(projectId, query);
            }
            
            return getTasksClient(projectId).createQuery(query);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (ValidationException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.updateQuery(projectId, query);
            }
                    
            return getTasksClient(projectId).updateQuery(query);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (ValidationException | EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                mockDelegate.deleteQuery(projectId, queryId);
            }
            
            getTasksClient(projectId).deleteQuery(queryId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch (ValidationException | EntityNotFoundException | RuntimeException ex) {
            throw new ODCSException(ex);
        }
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        try {
            if(mockDelegate != null) {
                return mockDelegate.getRepositoryContext(projectId);
            }            
            
            return getTasksClient(projectId).getRepositoryContext();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        } catch(RuntimeException e) {
            throw new ODCSException(e);
        }
    }

    private ActivityApi getActivityClient() {
        if (activityClient == null) {
            ActivityApiClient client = new ActivityApiClient();
            ApacheHttpRestClientDelegate delegate = new ApacheHttpRestClientDelegate(pa.getUserName(), new String(pa.getPassword()));
            client.setRestClientDelegate(delegate);
            client.setBaseUrl(url + "api/activity/"); // NOI18N
            activityClient = client;
        }
        return activityClient;
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

    private ProjectsQuery createProjectsQuery(ProjectRelationship rel) {
        ProjectsQuery query = new ProjectsQuery(rel, null);
        query.setOrganizationIdentifier(organizationId);
        return query;
    }

    // JDev bug 19823944
    // We need to set the NetBeans system classloader as the thread context
    // classloader when running in JDeveloper (the Equinox' ContextFinder used in
    // JDev does not work for the jackson mapper lib to load deserialized classes).
    // Here is the only entry point we can do it (unfortunately not in JDev).
    private static ClassLoader setupContextClassLoader() {
        ClassLoader systemCL = Lookup.getDefault().lookup(ClassLoader.class);
        if (systemCL != null) {
            ClassLoader currentContextCL = Thread.currentThread().getContextClassLoader();
            if (currentContextCL != null && currentContextCL != systemCL) {
                Thread.currentThread().setContextClassLoader(systemCL);
                return currentContextCL;
            }
        }
        return null;
    }

    private static void restoreContextClassLoader(ClassLoader originalContextCL) {
        if (originalContextCL != null) {
            Thread.currentThread().setContextClassLoader(originalContextCL);
        }
    }
}
