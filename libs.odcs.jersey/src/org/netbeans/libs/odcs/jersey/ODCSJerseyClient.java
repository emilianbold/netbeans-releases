package org.netbeans.libs.odcs.jersey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.netbeans.libs.odcs.jersey.wrappers.ActivityWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.ProfileWrapper;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;

public class ODCSJerseyClient implements ODCSClient {
    
    private final String url;
    private final PasswordAuthentication pa;
    private final Client client;

    public ODCSJerseyClient(String url, PasswordAuthentication pa) {
        if (!url.endsWith("/")) { //NOI18N
            url = url + '/';
        }
        this.url = url;
        this.pa = pa;
        this.client = getClient();
    }
        
    @Override
    public BuildDetails getBuildDetails(String projectId, String jobName, int buildNumber) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Profile getCurrentProfile() throws ODCSException {
        WebResource root = client.resource(url + "api/profile");
        ProfileWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ProfileWrapper.class);
        return wrapper.profile;
    }

    @Override
    public HudsonStatus getHudsonStatus(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JobDetails getJobDetails(String projectId, String jobName) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Project getProjectById(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        WebResource root = client.resource(url + "api/activity/"+ projectId);
        ActivityWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ActivityWrapper.class);
        if(wrapper.commits == null) {
            return Collections.emptyList();
        }
            return Collections.emptyList();
//        return Arrays.asList(wrapper.commits);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Client getClient() {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        clientConfig.getClasses().add(ObjectMapperProvider.class);

        Client c = Client.create(clientConfig);
        if(pa != null) {
            c.addFilter(new HTTPBasicAuthFilter(pa.getUserName(), new String(pa.getPassword())));        
            
        }
        return c;
    }



}
