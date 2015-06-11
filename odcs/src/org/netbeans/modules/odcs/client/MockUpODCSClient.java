package org.netbeans.modules.odcs.client;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.scm.domain.ScmLocation;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.openide.util.Exceptions;

public class MockUpODCSClient implements ODCSClient {
    
    private final Object PROJECT_LOCK = new Object();
    private final Object SCM_LOCK = new Object();
    
    private final static Logger LOG = Logger.getLogger(ODCSClient.class.getName());
    private DummyProfile profile;
    
    private final Map<String, List<ScmRepository>> scmRepositories = new HashMap<>();
    
    public MockUpODCSClient() {
        createProjects();
        
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.INFO, "Initialized MockODCSClient");
        }
    }

    private int newProject = 100;
    @Override
    public Profile getCurrentProfile() throws ODCSException {
        waitAMoment(1200);
        throwExIfGiven("getCurrentProfile");
            
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
            throwExIfGiven("getMyProjects");

            return new LinkedList<>(memberProjects.values());
    }

    @Override
    public Project getProjectById (String projectId) throws ODCSException {
        waitAMoment(800);
        throwExIfGiven("getProjectById:String");
        
        synchronized(PROJECT_LOCK) {
            return allProjects.get(projectId);
        }
    }

    @Override
    public List<Activity> getRecentActivities(String projectId) throws ODCSException {
        waitAMoment(800);
        throwExIfGiven("getActivities:String");
        
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<ScmRepository> getScmRepositories(String projectId) throws ODCSException {
        waitAMoment(800);
        throwExIfGiven("getScmRepositories:String");
        
        synchronized(SCM_LOCK) {
            List<ScmRepository> repos = scmRepositories.get(projectId);
            if(repos == null) {
                repos = new LinkedList<>();
                Project p = allProjects.get(projectId);
                repos.add(new DummySCMRepository(p.getName() + ".git"));
                try {
                    long l = Long.parseLong(projectId);
                    if(l < 4) {
                        repos.add(new DummySCMRepository(p.getName() + ".brizolit"));
                        repos.add(new DummySCMRepository(p.getName() + ".ekrazit"));
                    }
                    
                } catch (NumberFormatException e) { }
            }
            return repos;
        }
    }

    @Override
    public boolean isWatchingProject(String projectId) throws ODCSException {
        waitAMoment(500);
        throwExIfGiven("isWatchingProject:String");
        
        synchronized(PROJECT_LOCK) {
            return watchedProjects.containsKey(projectId);
        }
    }

    @Override
    public List<Project> searchProjects(String pattern) throws ODCSException {
        waitAMoment(2000);
        throwExIfGiven("searchProjects:String");
        
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
        throwExIfGiven("unwatchProject:String");
        
        synchronized(PROJECT_LOCK) {
            watchedProjects.remove(projectId);
        }
    }

    @Override
    public void watchProject(String projectId) throws ODCSException {
        waitAMoment(500);
        throwExIfGiven("watchProject:String");
        
        synchronized(PROJECT_LOCK) {
            Project p = allProjects.get(projectId);
            if(p != null) {
                watchedProjects.put(allProjects.get(projectId));
            }
        }
    }

    @Override
    public List<Project> getWatchedProjects () throws ODCSException {
        waitAMoment(500);
        throwExIfGiven("getWatchedProjects");
        
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
            LOG.log(Level.INFO, null, ex);
        }
    }

    private void throwExIfGiven(String method) throws ODCSException {
        
        String ex4Method = System.getProperty("odcs.client.mock.exception.4.method", null);
        if(ex4Method != null && !ex4Method.equals(method)) {
            return;
        }
        
        String ex = System.getProperty("odcs.client.mock.exception", null);
        String exCause = System.getProperty("odcs.client.mock.exception.cause", null);
        String exMsg = System.getProperty("odcs.client.mock.exception.msg", null);
        if(ex != null) {
            try {
                Class<?> clazz = Class.forName(ex);
                Class<?> clazzCause = exCause != null ? Class.forName(exCause) : null;
                Object o;
                if(clazzCause != null && exMsg != null) { 
                    o = clazz.getConstructor(Exception.class, String.class).newInstance(clazzCause.getConstructor().newInstance(), exMsg);
                } else if(clazzCause != null) { 
                    o = clazz.getConstructor(Exception.class).newInstance(clazzCause.getConstructor().newInstance());
                } else if(exMsg != null) { 
                    o = clazz.getConstructor(String.class).newInstance(exMsg);
                } else {
                    o = clazz.getConstructor().newInstance();
                }

                if(o instanceof ODCSException) {
                    throw (ODCSException) o;
                } else if(o instanceof RuntimeException) {
                    throw (RuntimeException) o;
                }
                throw new IllegalArgumentException("no idea what to do with : " + ex);
                
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
                     InstantiationException | IllegalAccessException | IllegalArgumentException | 
                     InvocationTargetException ex1) 
            {
                Exceptions.printStackTrace(ex1);
            }
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
            
            
            List<ProjectService> ps = new LinkedList<>();
            ps.add(new DummyProjectService(1, ServiceType.SCM));
            ps.add(new DummyProjectService(1, ServiceType.BUILD));
            setProjectServices(ps);
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
    
    private class DummySCMRepository extends ScmRepository {
        public DummySCMRepository(String name) {
            setName(name);
            setType(ScmType.GIT);
            setUrl("http://mockingbird/" + name);
            setScmLocation(ScmLocation.CODE2CLOUD);
        }
    }
    
    private class DummyProjectService extends ProjectService {

        public DummyProjectService(long id, ServiceType st) {
            setId(id);
            setServiceType(st);
            setAvailable(true);
            setUrl("http://mockingbird/" + st + "/");
            setWebUrl("http://mockingbird/web/" + st + "/");
        }
        
    }
}
