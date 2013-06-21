/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.versioning;

import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Milan Kubec, Jan Becicka
 * XXX somebody keeps and leaks the handles
 */
public class SourceHandleImpl extends SourceHandle {

    private static final int MAX_PROJECTS = 5;
    private static final String RECENTPROJECTS_PREFIX = "recent.projects."; // NOI18N
    public static final String SCM_TYPE_UNKNOWN = "unknown";//NOI18N
    private static final String WORKINGDIR = "working.dir."; //NOI18N

    private ScmRepository repository;
    private Preferences prefs;
    private ProjectHandle<ODCSProject> projectHandle;
    private final List<IDEProject> recent = new LinkedList<IDEProject>();
    private final boolean supported;

    private IDEProjectListener projectListener = new IDEProjectListener();

    public SourceHandleImpl(final ProjectHandle<ODCSProject> projectHandle, ScmRepository repository, boolean isSupported) {
        this.repository = repository;
        prefs = NbPreferences.forModule(SourceHandleImpl.class);
        this.projectHandle = projectHandle;
        this.supported = isSupported;
        initRecent();
    }

    public ScmRepository getRepository() {
        return repository;
    }

    public ProjectHandle<ODCSProject> getProjectHandle() {
        return projectHandle;
    }
    
    @Override
    public String getDisplayName() {
        return repository.getName();
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public String getScmFeatureName() {
        return repository.getType().name();
    }

    @Override
    public synchronized List<IDEProject> getRecentProjects() {
        return new ArrayList<IDEProject>(recent);
    }

    @Override
    public File getWorkingDirectory() {
        File wd = null;
        String uriString = prefs.get(WORKINGDIR + repository.getUrl(), null); // NOI18N
        try {
            if (uriString != null) {
                wd = getWorkDir(uriString);
            } else if (repository.getAlternateUrl() != null && !repository.getAlternateUrl().isEmpty()) {
                uriString = prefs.get(WORKINGDIR + repository.getAlternateUrl(), null);
                if (uriString != null) {
                    wd = getWorkDir(uriString);
                }
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (wd == null) {
            wd = guessWorkdir();
            if (wd != null) {
                setWorkingDirectory(repository.getUrl(), wd);
            }
        }
        return wd;
    }

    void refresh() {
        if (projectHandle!=null) {
            projectHandle.firePropertyChange(ProjectHandle.PROP_SOURCE_LIST, null, null);
        }
    }

    void setWorkingDirectory (String url, File dest) {
        prefs.put(WORKINGDIR + url, Utilities.toURI(dest).toString()); //NOI18N
    }

    private synchronized boolean addToRecent(IDEProject... projects) {
        boolean changed = false;
        for (IDEProject ideProject : projects) {
            boolean alreadyPresent = recent.remove(ideProject);
            if (alreadyPresent || isUnder(ideProject)) {
                recent.add(0, ideProject);
                if (!alreadyPresent) {
                    ideProject.addDeleteListener(projectListener);
                }
                changed = true;
            }
        }
        while (recent.size() > MAX_PROJECTS) {
            IDEProject ideProject = recent.remove(MAX_PROJECTS);
            ideProject.removeDeleteListener(projectListener);
            changed = true;
        }
        if (changed) {
            storeRecent();
        }
        return changed;
    }

    private synchronized boolean removeFromRecent(IDEProject ideProject) {
        boolean removed = recent.remove(ideProject);
        if (removed) {
            ideProject.removeDeleteListener(projectListener);
            storeRecent();
        }
        return removed;
    }

    private File guessWorkdir() {
        URL url = null;
        synchronized(this) {
            if (!recent.isEmpty()) {
                url = recent.get(0).getURL();
            }
        }
        if (url != null) {
            FileObject projectDirectory = URLMapper.findFileObject(url);
            if (projectDirectory != null) {
                FileObject parent = projectDirectory.getParent();
                if (parent != null && parent.isValid()) {
                    return FileUtil.toFile(parent);
                }
            }
        }
        return null;
    }

    private void initRecent() {
        if (prefs == null) { // external repository not supported
            return;
        }

        ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
        if (projects == null) {
            return;
        }

        List<String> roots = getStringList(prefs, RECENTPROJECTS_PREFIX + repository.getUrl());
        for (String root:roots) {
            try {
                IDEProject ideProject = projects.getIDEProject(new URL(root));
                if (ideProject != null) {
                    recent.add(ideProject); // 'recent' not yet available outside, does not have to by synchronized here
                    ideProject.addDeleteListener(projectListener);
                }
            } catch (IOException ex) {
                Logger.getLogger(SourceHandleImpl.class.getName()).fine("Project not found for " + root);
            }
        }
        projects.addProjectOpenListener(projectListener);
        addToRecent(projects.getOpenProjects());
    }

    private boolean isUnder(IDEProject ideProject) {
        FileObject projectDirectory = URLMapper.findFileObject(ideProject.getURL());
        String remoteLocation = projectDirectory != null ? VersioningQuery.getRemoteLocation(projectDirectory.toURI()) : null;
        if(remoteLocation == null) {
            return false;
        }
        String location = repository.getUrl();
        if (!location.endsWith("/")) {//NOI18N
            location+="/";//NOI18N
        }
        String alternateLocation = repository.getAlternateUrl();
        if (alternateLocation == null || alternateLocation.isEmpty()) {
            alternateLocation = null;
        } else if (!alternateLocation.endsWith("/")) {//NOI18N
            alternateLocation+="/";//NOI18N
        }
        String[] rls = remoteLocation.split(";");
        for (String rl : rls) {
            if(isUnder(rl, location)) {
                return true;
            }
            if(alternateLocation != null && isUnder(rl, alternateLocation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUnder(String remoteLocation, String location) {
        if (remoteLocation!=null && !remoteLocation.endsWith("/")) {//NOI18N
            remoteLocation+="/";//NOI18N
        }

        if (location.equals(remoteLocation)) {
            return true;
        }
        try {
            URI uri1 = new URI(location);
            URI uri2 = new URI(remoteLocation);
            if (location.substring(uri1.getScheme().length()).equals(remoteLocation.substring(uri2.getScheme().length()))) {
                return true;
            }
        } catch (Exception e) {
            //ignore
            return false;
        }
        return false;
    }

    private void storeRecent() {
        if (prefs != null) {
            List<String> value = new ArrayList<String>();
            for (IDEProject prj : recent) {
                value.add(prj.getURL().toString());
            }
            putStringList(prefs, RECENTPROJECTS_PREFIX + repository.getUrl(), value);
        }
    }

    /*
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList(Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
                    retval.add(idx + "." + prefs.get(k, null)); // NOI18N
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(SourceHandleImpl.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores a List of Strings into Preferences node under the given key.
     *
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void putStringList(Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) { // NOI18N
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s); // NOI18N
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(SourceHandleImpl.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private static File getWorkDir (String uriString) throws IllegalArgumentException, URISyntaxException {
        URI uri = new URI(uriString);
        final File file = Utilities.toFile(uri);
        if (file == null || !file.exists()) {
            return null;
        }
        FileObject f = FileUtil.toFileObject(file);
        if (f==null || !f.isValid())
            return null;
        return file;
    }

    private class IDEProjectListener implements IDEProject.OpenListener, IDEProject.DeleteListener {
        private RequestProcessor rp = new RequestProcessor(SourceHandleImpl.class);

        @Override
        public void projectsOpened(final IDEProject[] projects) {
            rp.post(new Runnable() { // in RP because of bug 179082
                @Override
                public void run() {
                    if (addToRecent(projects)) {
                        refresh();
                    }
                }
            });
        }

        @Override
        public void projectDeleted(IDEProject prj) {
            if (removeFromRecent(prj)) {
                refresh();
            }
        }
    }
}
