package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.common.service.WrappedCheckedException;
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
import java.net.PasswordAuthentication;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.team.commons.LogUtils;

public class ODCSClientImpl implements ODCSClient {
    
    private final String url;
    private final PasswordAuthentication pa;
    private final HttpClient httpClient = new HttpClient(WebUtil.getConnectionManager());
    
    private final static Logger LOG = Logger.getLogger(ODCSClient.class.getName());
    private ActivityServiceClient actvityServiceClient;
    private ProfileWebServiceClient profileServiceClient;
    private ScmServiceClient scmServiceClient;
    private TaskServiceClient tasksServiceClient;
    
    public ODCSClientImpl(String url, PasswordAuthentication pa) {
        if (!url.endsWith("/")) { //NOI18N
            url = url + '/';
        }
        this.url = url;
        this.pa = pa;
        WebUtil.configureHttpClient(httpClient, "");
        httpClient.getParams().setAuthenticationPreemptive(true);
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Initialized ODCSClient for {0} u: {1} p:{2}", new Object[]{url, pa.getUserName(), LogUtils.getPasswordLog(pa.getPassword())});
        }
    }

    @Override
    public Profile getCurrentProfile() throws ODCSException {
        try {
            return getProfileClient().getCurrentProfile();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
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
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        try {
            return getActivityClient().getRecentActivity(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        try {
            return getActivityClient().getShortActivityList(projectId);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        try {
            return getScmClient(projectId).getScmRepositories();
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        try {
            return getProfileClient().isWatchingProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        try {
            QueryResult<Project> r = getProfileClient().findProjects(new ProjectsQuery(pattern, null));
            return r != null ? r.getResultPage() : null;
                } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        try {
            getProfileClient().unwatchProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        try {
            getProfileClient().watchProject(projectId);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        try {
            QueryResult<Project> r = getProfileClient().findProjects(new ProjectsQuery(ProjectRelationship.WATCHER, null));
            return r != null ? r.getResultPage() : null;
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        try {
            return getProfileClient().createProject(project);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        try {
            return getTasksClient(projectId).createQuery(query);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
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
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        try {
            getTasksClient(projectId).deleteQuery(queryId);
        } catch (ValidationException ex) {
            throw new ODCSException(ex);
        } catch (EntityNotFoundException ex) {
            throw new ODCSException(ex);
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
        }
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        try {
            return getTasksClient(projectId).getRepositoryContext();
        } catch (WrappedCheckedException e) {
            throw new ODCSException(e.getCause());
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
    
}
