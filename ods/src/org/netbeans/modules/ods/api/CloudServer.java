/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.api;

import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectAccessibility;
import com.tasktop.c2c.server.profile.domain.project.ProjectPreferences;
import com.tasktop.c2c.server.profile.domain.project.WikiMarkupLanguage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.netbeans.modules.ods.client.api.ODSFactory;
import org.netbeans.modules.ods.client.api.ODSClient;
import org.netbeans.modules.ods.client.api.ODSException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Vrabec
 */
public final class CloudServer {

    /**
     * fired when user logs in/out getOldValue() returns old
     * PasswordAuthentication or null getNewValue() returns new
     * PasswordAuthentication or null
     */
    public static final String PROP_LOGIN = "login";
    /**
     * fired when user login started
     */
    public static final String PROP_LOGIN_STARTED = "login_started";
    /**
     * fired when user login failed
     */
    public static final String PROP_LOGIN_FAILED = "login_failed";
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final URL url;
    private final String displayName;
    private Icon icon;
    private PasswordAuthentication auth;
    private Profile currentProfile;
    private final Map<String, ODSProject> projectsCache = new HashMap<String, ODSProject>();
    private final Map<String, List<ODSProject>> myProjectCache = new WeakHashMap<String, List<ODSProject>>();
    private final Map<String, List<ODSProject>> watchedProjectsCache = new WeakHashMap<String, List<ODSProject>>();

    private CloudServer(String displayName, String url) throws MalformedURLException {
        while (url.endsWith("/")) { //NOI18N
            url = url.substring(0, url.length() - 1);
        }
        this.displayName = displayName;
        this.url = new URL(url);
    }

    static CloudServer createInstance(String displayName, String url) throws MalformedURLException {
        return new CloudServer(displayName, url);
    }

    /**
     * Adds listener to the server instance
     *
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Adds listener to the server instance
     *
     * @param name
     * @param l
     */
    public void addPropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(name, l);
    }

    /**
     * Removes listener from the server instance
     *
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Removes listener from the server instance
     *
     * @param name
     * @param l
     */
    public void removePropertyChangeListener(String name, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(name, l);
    }

    public URL getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Icon getIcon() {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/ods/ui/resources/ods.png", false); //NOI18N
        }
        return icon;
    }

    public void logout() {
        PasswordAuthentication old = auth;
        synchronized (this) {
            auth = null;
            currentProfile = null;
            projectsCache.clear();
        }
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, PROP_LOGIN, old, auth);
        firePropertyChange(propertyChangeEvent);
    }

    public boolean isLoggedIn() {
        return auth != null;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }

    public void login(String username, char[] password) throws ODSException {
        PasswordAuthentication old = auth;
        firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_STARTED, null, username));
        ODSClient createClient = null;
        synchronized (this) {
            createClient = ODSFactory.getInstance().createClient(getUrl().toString(), new PasswordAuthentication(username, password.clone()));

            currentProfile = createClient.getCurrentProfile();

            auth = new PasswordAuthentication(username, password.clone());
            Arrays.fill(password, '\0');
        }
        // XXX ts - need perhaps a different way how to determine if failed or not
        if (currentProfile == null) {
            firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_FAILED, null, null));
        } else {
            firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN, old, auth));
        }
    }

    public ODSProject getProject(String projectId, boolean refresh) throws ODSException {
        if (!isLoggedIn()) {
            return null;
        }
        ODSProject odsProj;
        synchronized (projectsCache) {
            odsProj = projectsCache.get(projectId);
        }
        if (refresh || odsProj == null) {
            Project proj = getProject(projectId);
            if (proj != null) {
                odsProj = setOdsProjectData(projectId, proj);
            }
        }
        return odsProj;
    }

    public List<ODSProject> findProjects (String pattern) throws ODSException {
        if (!isLoggedIn()) {
            return Collections.emptyList();
        }

        ODSClient client = createClient();
        List<Project> projs = client.searchProjects(pattern);
        List<ODSProject> odsProjects = new ArrayList<ODSProject>(projs.size());
        for (Project p : projs) {
            odsProjects.add(setOdsProjectData(p.getIdentifier(), p));
        }
        return odsProjects;
    }

    public void watch (ODSProject prj) throws ODSException {
        createClient().watchProject(prj.getId());
    }

    public void unwatch (ODSProject prj) throws ODSException {
        createClient().unwatchProject(prj.getId());
    }

    public ODSProject createProject (String projectTitle, String projectDescription,
            String accessibility, String wikiStyle) throws ODSException {
        ODSClient client = createClient();
        Project project = new Project();
        project.setName(projectTitle);
        project.setDescription(projectDescription);
        project.setAccessibility(ProjectAccessibility.valueOf(accessibility));
        ProjectPreferences prefs = new ProjectPreferences();
        prefs.setWikiLanguage(WikiMarkupLanguage.valueOf(wikiStyle));
        project.setProjectPreferences(prefs);
        Project created = client.createProject(project);
        return setOdsProjectData(created.getIdentifier(), created);
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
        CloudServerManager.getDefault().propertyChangeSupport.firePropertyChange(event);
    }

    public void refresh(ODSProject odsProject) throws ODSException {
        if (!isLoggedIn()) {
            return;
        }
        Project p = getProject(odsProject.getId());
        odsProject.setProject(p);
    }

    public Collection<ODSProject> getMyProjects(boolean force) throws ODSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        String username = auth.getUserName();
        synchronized (myProjectCache) {
            List<ODSProject> myProjs = myProjectCache.get(username);
            if (myProjs != null && !force) {
                return new ArrayList<ODSProject>(myProjs);
            }
        }
        ODSClient client = createClient();
        List<Project> mine = client.getMyProjects();
        if (mine == null) {
            synchronized (myProjectCache) {
                myProjectCache.put(username, Collections.<ODSProject>emptyList());
                watchedProjectsCache.put(username, Collections.<ODSProject>emptyList());
                return Collections.<ODSProject>emptyList();
            }
        }
        List<Project> ps = client.getWatchedProjects();
        Set<ODSProject> watched = new LinkedHashSet<ODSProject>(ps.size());
        for (Project project : ps) {
            ODSProject p = setOdsProjectData(project.getIdentifier(), project);
            watched.add(p);
        }
        Set<ODSProject> ret = new LinkedHashSet<ODSProject>(mine.size());
        for (Project project : mine) {
            ODSProject p = setOdsProjectData(project.getIdentifier(), project);
            ret.add(p);
        }
        ret.addAll(watched);
        synchronized (myProjectCache) {
            myProjectCache.put(username, new ArrayList<ODSProject>(ret));
            watchedProjectsCache.put(username, new ArrayList<ODSProject>(watched));
        }
        return ret;
    }

    public Collection<ODSProject> getMyProjects() throws ODSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        synchronized (myProjectCache) {
            List<ODSProject> myProjs = myProjectCache.get(auth.getUserName());
            if (myProjs != null) {
                return new ArrayList<ODSProject>(myProjs);
            }
        }
        return getMyProjects(true);
    }

    public Collection<ODSProject> getWatchedProjects (boolean cached) throws ODSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        synchronized (myProjectCache) {
            List<ODSProject> watchedProjs = watchedProjectsCache.get(auth.getUserName());
            if (watchedProjs != null) {
                return new ArrayList<ODSProject>(watchedProjs);
            }
        }
        if (cached) {
            return Collections.EMPTY_LIST;
        } else {
            getMyProjects(true);
            synchronized (myProjectCache) {
                List<ODSProject> watchedProjs = watchedProjectsCache.get(auth.getUserName());
                return watchedProjs == null ? Collections.<ODSProject>emptyList() : new ArrayList<ODSProject>(watchedProjs);
            }
        }
    }

    public static CloudServer findServerForRepository (String uri) {
        Map.Entry<CloudServer, String> pair = findServerAndProjectForRepository(uri);
        if (pair == null) {
            return null;
        } else {
            return pair.getKey();
        }
    }
    
    static Map.Entry<CloudServer, String> findServerAndProjectForRepository (String uri) {
        if (uri == null) {
            return null;
        }
        for (CloudServer k : CloudServerManager.getDefault().getServers()) {
            for (Map.Entry<Pattern, Integer> e : getRepositoryPatterns(k).entrySet()) {
                Matcher m = e.getKey().matcher(uri);
                if (m.matches()) {
                    return new AbstractMap.SimpleImmutableEntry(k, m.group(e.getValue()));
                }
            }
            // what about external repositories??
        }
        return null;
    }
    
    private static Map<Pattern, Integer> getRepositoryPatterns (CloudServer server) {
        Map<Pattern, Integer> patterns = new LinkedHashMap<Pattern, Integer>(2);
        patterns.put(Pattern.compile("(http|https)://" + (server.getUrl().getHost() + server.getUrl().getPath()).replace(".", "\\.") + "/s/(\\S*)/scm/.*"), //NOI18N
                2);
        patterns.put(Pattern.compile("ssh://" + server.getUrl().getHost().replace(".", "\\.") + "(:[0-9]+)?/(\\S*)/.*"), //NOI18N
                2);
        return patterns;
    }

    private Project getProject(String projectId) throws ODSException {
        ODSClient client = createClient();
        Project p = client.getProjectById(projectId);
        return p;
    }

    private ODSProject setOdsProjectData(String projectId, Project proj) {
        ODSProject odsProj;
        synchronized (projectsCache) {
            odsProj = projectsCache.get(projectId);
            if (odsProj == null) {
                projectsCache.put(projectId, odsProj = new ODSProject(proj, this));
            } else {
                odsProj.setProject(proj);
            }
        }
        return odsProj;
    }

    private ODSClient createClient () {
        return ODSFactory.getInstance().createClient(getUrl().toString(), getPasswordAuthentication());
    }
}
