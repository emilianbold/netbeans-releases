package org.netbeans.libs.odcs.jersey;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectRelationship;
import com.tasktop.c2c.server.profile.domain.project.ProjectsQuery;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.netbeans.libs.odcs.jersey.wrappers.ActivityWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.ProfileWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.ProjectWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.ProjectQueryResultWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.QueryWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.RepositoryConfigurationWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.RepositoryWrapper;
import org.netbeans.libs.odcs.jersey.wrappers.WatchingProjectWrapper;
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
    public Profile getCurrentProfile() throws ODCSException {
        WebResource root = client.resource(url + "api/profile");
        ProfileWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ProfileWrapper.class);
        return wrapper.profile;
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/search");
        ProjectsQuery query = new ProjectsQuery(ProjectRelationship.MEMBER, null);
        ProjectQueryResultWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(ProjectQueryResultWrapper.class, query);
        return result.queryResult.getResultPage();
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/").path(projectId);
        ProjectWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ProjectWrapper.class);
        return wrapper.project;
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/activity/").path(projectId);
        ActivityWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ActivityWrapper.class);
        return Arrays.asList(wrapper.projectActivityList);
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/activity/").path(projectId).path("short");
        ActivityWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(ActivityWrapper.class);
        return Arrays.asList(wrapper.projectActivityList);
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("s").path(projectId).path("scm/api/repository");
        RepositoryWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(RepositoryWrapper.class);
        return Arrays.asList(wrapper.scmRepositoryList);
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/").path(projectId).path("watch");
        return root.accept(MediaType.APPLICATION_JSON).get(WatchingProjectWrapper.class).isWatching;
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/search");
        ProjectsQuery query = new ProjectsQuery(pattern, null);
        ProjectQueryResultWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(ProjectQueryResultWrapper.class, query);
        return result.queryResult.getResultPage();
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/").path(projectId).path("unwatch");
        root.post();
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/").path(projectId).path("watch");
        root.post();
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        WebResource root = client.resource(url).path("api/projects/search");
        ProjectsQuery query = new ProjectsQuery(ProjectRelationship.WATCHER, null);
        ProjectQueryResultWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(ProjectQueryResultWrapper.class, query);
        return result.queryResult.getResultPage();
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
        WebResource root = client.resource(url).path("api/profile/project");
        ProjectWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(ProjectWrapper.class, project);
        Project p = result.project;
        if (result.project.getProjectServices() == null) {
            p = getProjectById(result.project.getIdentifier());
        }
        return p;
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        WebResource root = client.resource(url).path("s").path(projectId).path("tasks/task/query");
        QueryWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(QueryWrapper.class, query);
        return result.savedTaskQuery;
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        WebResource root = client.resource(url).path("s").path(projectId)
                .path("tasks/task/query").path(String.valueOf(query.getId()));
        QueryWrapper result = root
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .post(QueryWrapper.class, query);
        return result.savedTaskQuery;
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        WebResource root = client.resource(url).path("s").path(projectId)
                .path("tasks/task/query").path(String.valueOf(queryId));
        root.delete();
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        WebResource root = client.resource(url).path("s").path(projectId).path("tasks/repositoryContext");
        RepositoryConfigurationWrapper wrapper = root.accept(MediaType.APPLICATION_JSON).get(RepositoryConfigurationWrapper.class);
        return wrapper.repositoryConfiguration;
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
