package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.openide.util.Exceptions;

public class MockUpODCSClient implements ODCSClient {
    
    private final Object PROJECT_LOCK = new Object();
    
    private final static Logger LOG = Logger.getLogger(ODCSClient.class.getName());
    private DummyProfile profile;
    
    public MockUpODCSClient() {
        createProjects();
        
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.INFO, "Initialized MockODCSClient");
        }
    }

    private int newProject = 100;
    @Override
    public Profile getCurrentProfile() throws ODCSException {
        if(Boolean.getBoolean("odcs.mock.createNewProjectBetweenSessions")) {
            synchronized(PROJECT_LOCK) {
                DummyProject p = new DummyProject(newProject, "new_project_"+ newProject, "New Project " + newProject);
                newProject++;
                allProjects.put(p);
                memberProjects.put(p);
            }
        }
        synchronized(this) {
            if(profile == null) {
                profile = new DummyProfile();
            }
            return profile;
        }
    }

    @Override
    public List<Project> getMyProjects() throws ODCSException {
        waitAMoment(2500);
        
        return new LinkedList<>(memberProjects.values());
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        waitAMoment(800);
        
        synchronized(PROJECT_LOCK) {
            return allProjects.get(projectId);
        }
    }

    @Override
    public List<ProjectActivity> getRecentActivities(String projectId) throws ODCSException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<ProjectActivity> getRecentShortActivities(String projectId) throws ODCSException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        waitAMoment(500);
        
        synchronized(PROJECT_LOCK) {
            return watchedProjects.containsKey(projectId);
        }
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        waitAMoment(2000);
        
        List<Project> ret = new LinkedList<>();
        synchronized(PROJECT_LOCK) {
            for (Project p : allProjects.values()) {
                if(p.getName().contains(pattern) || p.getDescription().contains(pattern)) {
                    ret.add(p);
                }
            }
        }
        return ret;
    }

    @Override
    public void unwatchProject(String projectId) throws ODCSException {
        waitAMoment(500);
        
        synchronized(PROJECT_LOCK) {
            watchedProjects.remove(projectId);
        }
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        waitAMoment(500);
        
        synchronized(PROJECT_LOCK) {
            Project p = allProjects.get(projectId);
            if(p != null) {
                watchedProjects.put(allProjects.get(projectId));
            }
        }
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        return new LinkedList<>(watchedProjects.values());
    }

    @Override
    public Project createProject (Project project) throws ODCSException {
       throw new UnsupportedOperationException("NOWAY!");
    }
    
    @Override
    public SavedTaskQuery createQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        throw new UnsupportedOperationException("NOWAY!");
    }

    @Override
    public SavedTaskQuery updateQuery(String projectId, com.tasktop.c2c.server.tasks.domain.SavedTaskQuery query) throws ODCSException {
        throw new UnsupportedOperationException("NOWAY!");
    }

    @Override
    public void deleteQuery(String projectId, Integer queryId) throws ODCSException {
        throw new UnsupportedOperationException("NOWAY!");
    }

    @Override
    public RepositoryConfiguration getRepositoryContext(String projectId) throws ODCSException {
        throw new UnsupportedOperationException("NOWAY!");
    }

    private void waitAMoment(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class DummyProfile extends Profile {
        public DummyProfile() {
            setAccountDisabled(false);
            setEmail("Juraj.Janosheek@hornychlapci.org");
            setEmailVerfied(true);
            setFirstName("Juraj");
            setFirstName("Janosheek");
        }
    }
    
    private final ProjectMap memberProjects = new ProjectMap();
    private final ProjectMap allProjects = new ProjectMap();
    private final ProjectMap watchedProjects = new ProjectMap();
    private void createProjects() {
        for (int i = 0; i < 10; i++) {
            allProjects.put(new DummyProject(i, "member_project_" + i, "Member Project " + i));
        }
        
        for (int i = 0; i <= 2; i++) {
            memberProjects.put(allProjects.get("" + i));
        }
    }
    
    private class DummyProject extends Project {
        public DummyProject(long id, String name, String desc) {
            setDescription(desc);
            setId(id);
            setIdentifier("" + id);
            setName(name);
            setNumCommiters(0);
            setNumWatchers(0);
            setProjectServices(Collections.EMPTY_LIST);
        }
    }

    private class ProjectMap extends HashMap<String, Project>  {
        void put(Project p) {
            put(p.getIdentifier(), p);
        }
        Project remove(Project p) {
            return remove(p.getIdentifier());
        }
    }
}
