package org.netbeans.libs.team.c2c.jersey;

import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.HudsonStatus;
import com.tasktop.c2c.server.profile.domain.build.JobDetails;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.net.PasswordAuthentication;
import java.util.List;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.c2c.client.api.CloudException;

public class C2CJersey implements CloudClient {
    
    @Override
    public BuildDetails getBuildDetails(String projectId, String jobName, int buildNumber) throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Profile getCurrentProfile() throws CloudException {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
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

//    public static final class ProfileWrapper {
//        Profile profile;
//    }
//
//    @XmlRootElement
//    public static final class Record {
//        @XmlElement
//        CustomProfile profile;
//
//        public static final class CustomProfile {
//            @XmlElement
//            String username;
//            @XmlElement
//            String password;
//            @XmlElement
//            String firstName;
//            @XmlElement
//            String lastName;
//            @XmlElement
//            String email;
//            @XmlElement
//            Settings notificationSettings;
//            @XmlElement
//            String gravatarHash;
//            @XmlElement
//            boolean accountDisabled;
//            @XmlElement
//            String githubUsername;
//            @XmlElement
//            boolean emailVerfied;
//            @XmlElement
//            int id;
//
//            public static final class Settings {
//                @XmlElement
//                boolean emailTaskActivity;
//                @XmlElement
//                boolean emailNewsAndEvents;
//                @XmlElement
//                boolean emailServiceAndMaintenance;
//                @XmlElement
//                int id;
//            }
//        }
//    }

    @Override
    public void initialize(String url, PasswordAuthentication pa) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
