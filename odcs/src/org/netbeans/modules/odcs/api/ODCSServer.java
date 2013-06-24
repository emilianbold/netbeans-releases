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
package org.netbeans.modules.odcs.api;

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
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Ondrej Vrabec
 */
public final class ODCSServer {

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
    private final Map<String, ODCSProject> projectsCache = new HashMap<String, ODCSProject>();
    private final Map<String, List<ODCSProject>> myProjectCache = new WeakHashMap<String, List<ODCSProject>>();
    private final Map<String, List<ODCSProject>> watchedProjectsCache = new WeakHashMap<String, List<ODCSProject>>();

    private ODCSServer(String displayName, String url) throws MalformedURLException {
        while (url.endsWith("/")) { //NOI18N
            url = url.substring(0, url.length() - 1);
        }
        this.displayName = displayName;
        this.url = new URL(url);
    }

    static ODCSServer createInstance(String displayName, String url) throws MalformedURLException {
        return new ODCSServer(displayName, url);
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
            icon = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/odcs.png", false); //NOI18N
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
        PasswordAuthentication pw = auth;
        return pw == null ? null : new PasswordAuthentication(pw.getUserName(), 
                pw.getPassword() == null ? null : pw.getPassword().clone());
    }

    public void login(String username, char[] password) throws ODCSException {
        PasswordAuthentication old = auth;
        firePropertyChange(new PropertyChangeEvent(this, PROP_LOGIN_STARTED, null, username));
        ODCSClient createClient = null;
        synchronized (this) {
            createClient = ODCSFactory.getInstance().createClient(getUrl().toString(), new PasswordAuthentication(username, password.clone()));

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

    public ODCSProject getProject(String projectId, boolean refresh) throws ODCSException {
        if (!isLoggedIn()) {
            return null;
        }
        ODCSProject odcsProj;
        synchronized (projectsCache) {
            odcsProj = projectsCache.get(projectId);
        }
        if (refresh || odcsProj == null) {
            Project proj = getProject(projectId);
            if (proj != null) {
                odcsProj = setODCSProjectData(projectId, proj);
            }
        }
        return odcsProj;
    }

    public List<ODCSProject> findProjects (String pattern) throws ODCSException {
        if (!isLoggedIn()) {
            return Collections.emptyList();
        }

        ODCSClient client = createClient();
        List<Project> projs = client.searchProjects(pattern);
        List<ODCSProject> odcsProjects = new ArrayList<ODCSProject>(projs.size());
        for (Project p : projs) {
            odcsProjects.add(setODCSProjectData(p.getIdentifier(), p));
        }
        return odcsProjects;
    }

    public void watch (ODCSProject prj) throws ODCSException {
        createClient().watchProject(prj.getId());
    }

    public void unwatch (ODCSProject prj) throws ODCSException {
        createClient().unwatchProject(prj.getId());
    }

    public ODCSProject createProject (String projectTitle, String projectDescription,
            String accessibility, String wikiStyle) throws ODCSException {
        ODCSClient client = createClient();
        Project project = new Project();
        project.setName(projectTitle);
        project.setDescription(projectDescription);
        project.setAccessibility(ProjectAccessibility.valueOf(accessibility));
        ProjectPreferences prefs = new ProjectPreferences();
        prefs.setWikiLanguage(WikiMarkupLanguage.valueOf(wikiStyle));
        project.setProjectPreferences(prefs);
        Project created = client.createProject(project);
        return setODCSProjectData(created.getIdentifier(), created);
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        propertyChangeSupport.firePropertyChange(event);
        ODCSManager.getDefault().propertyChangeSupport.firePropertyChange(event);
    }

    public void refresh(ODCSProject odcsProject) throws ODCSException {
        if (!isLoggedIn()) {
            return;
        }
        Project p = getProject(odcsProject.getId());
        odcsProject.setProject(p);
    }

    public Collection<ODCSProject> getMyProjects(boolean force) throws ODCSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        String username = auth.getUserName();
        synchronized (myProjectCache) {
            List<ODCSProject> myProjs = myProjectCache.get(username);
            if (myProjs != null && !force) {
                return new ArrayList<ODCSProject>(myProjs);
            }
        }
        ODCSClient client = createClient();
        List<Project> mine = client.getMyProjects();
        if (mine == null) {
            synchronized (myProjectCache) {
                myProjectCache.put(username, Collections.<ODCSProject>emptyList());
                watchedProjectsCache.put(username, Collections.<ODCSProject>emptyList());
                return Collections.<ODCSProject>emptyList();
            }
        }
        List<Project> ps = client.getWatchedProjects();
        Set<ODCSProject> watched = new LinkedHashSet<ODCSProject>(ps.size());
        for (Project project : ps) {
            ODCSProject p = setODCSProjectData(project.getIdentifier(), project);
            watched.add(p);
        }
        Set<ODCSProject> ret = new LinkedHashSet<ODCSProject>(mine.size());
        for (Project project : mine) {
            ODCSProject p = setODCSProjectData(project.getIdentifier(), project);
            ret.add(p);
        }
        ret.addAll(watched);
        synchronized (myProjectCache) {
            myProjectCache.put(username, new ArrayList<ODCSProject>(ret));
            watchedProjectsCache.put(username, new ArrayList<ODCSProject>(watched));
        }
        return ret;
    }

    public Collection<ODCSProject> getMyProjects() throws ODCSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        synchronized (myProjectCache) {
            List<ODCSProject> myProjs = myProjectCache.get(auth.getUserName());
            if (myProjs != null) {
                return new ArrayList<ODCSProject>(myProjs);
            }
        }
        return getMyProjects(true);
    }

    public Collection<ODCSProject> getWatchedProjects () throws ODCSException {
        if (!isLoggedIn()) {
            return Collections.EMPTY_LIST;
        }
        synchronized (myProjectCache) {
            List<ODCSProject> watchedProjs = watchedProjectsCache.get(auth.getUserName());
            if (watchedProjs != null) {
                return new ArrayList<ODCSProject>(watchedProjs);
            }
        }
        return Collections.EMPTY_LIST;
    }

    public static ODCSServer findServerForRepository (String uri) {
        Map.Entry<ODCSServer, String> pair = findServerAndProjectForRepository(uri);
        if (pair == null) {
            return null;
        } else {
            return pair.getKey();
        }
    }
    
    static Map.Entry<ODCSServer, String> findServerAndProjectForRepository (String uri) {
        if (uri == null) {
            return null;
        }
        for (ODCSServer k : ODCSManager.getDefault().getServers()) {
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
    
    private static Map<Pattern, Integer> getRepositoryPatterns (ODCSServer server) {
        Map<Pattern, Integer> patterns = new LinkedHashMap<Pattern, Integer>(2);
        patterns.put(Pattern.compile("(http|https)://" + (server.getUrl().getHost() + server.getUrl().getPath()).replace(".", "\\.") + "/s/(\\S*)/scm/.*"), //NOI18N
                2);
        patterns.put(Pattern.compile("ssh://" + server.getUrl().getHost().replace(".", "\\.") + "(:[0-9]+)?/(\\S*)/.*"), //NOI18N
                2);
        return patterns;
    }

    private Project getProject(String projectId) throws ODCSException {
        ODCSClient client = createClient();
        Project p = client.getProjectById(projectId);
        return p;
    }

    private ODCSProject setODCSProjectData(String projectId, Project proj) {
        ODCSProject odcsProj;
        synchronized (projectsCache) {
            odcsProj = projectsCache.get(projectId);
            if (odcsProj == null) {
                projectsCache.put(projectId, odcsProj = new ODCSProject(proj, this));
            } else {
                odcsProj.setProject(proj);
            }
        }
        return odcsProj;
    }

    private ODCSClient createClient () {
        return ODCSFactory.getInstance().createClient(getUrl().toString(), getPasswordAuthentication());
    }
}
