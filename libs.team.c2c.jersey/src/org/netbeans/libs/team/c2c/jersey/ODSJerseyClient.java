package org.netbeans.libs.team.c2c.jersey;

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
import java.net.PasswordAuthentication;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.netbeans.libs.team.c2c.jersey.wrappers.ActivityWrapper;
import org.netbeans.libs.team.c2c.jersey.wrappers.ProfileWrapper;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.c2c.client.api.CloudException;

public class ODSJerseyClient implements CloudClient {
    
    private final String url;
    private final PasswordAuthentication pa;
    private final Client client;

    public ODSJerseyClient(String url, PasswordAuthentication pa) {
        this.url = url;
        this.pa = pa;
        this.client = getClient();
    }
        
    @Override
    public BuildDetails getBuildDetails(String projectId, String jobName, int buildNumber) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Profile getCurrentProfile() throws CloudException {
        WebResource root = client.resource(url + "/alm/api/profile");
        ProfileWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ProfileWrapper.class);
        return wrapper.profile;
    }

    @Override
    public HudsonStatus getHudsonStatus(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JobDetails getJobDetails(String projectId, String jobName) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> getMyProjects() throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Project getProjectById(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws CloudException {
        WebResource root = client.resource(url + "/alm/api/activity/"+ projectId);
        ActivityWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ActivityWrapper.class);
        if(wrapper.commits == null) {
            return Collections.emptyList();
        }
            return Collections.emptyList();
//        return Arrays.asList(wrapper.commits);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWatchingProject(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Project> searchProjects(String pattern) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unwatchProject(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void watchProject(String projectId) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
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
