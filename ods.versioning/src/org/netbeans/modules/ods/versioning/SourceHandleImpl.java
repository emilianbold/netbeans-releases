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

package org.netbeans.modules.ods.versioning;

import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.team.ui.common.NbProjectHandleImpl;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.common.RecentProjectsCache;
import org.netbeans.modules.team.ui.spi.NbProjectHandle;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kubec, Jan Becicka
 * XXX somebody keeps and leaks the handles
 */
public class SourceHandleImpl extends SourceHandle implements PropertyChangeListener {

    private ScmRepository repository;
    private Preferences prefs;
    private ProjectHandle<ODSProject> projectHandle;
    private static final int MAX_PROJECTS = 5;
    private static final String RECENTPROJECTS_PREFIX = "recent.projects."; // NOI18N
    public static final String SCM_TYPE_UNKNOWN = "unknown";//NOI18N
    private static final String WORKINGDIR = "working.dir."; //NOI18N
    private RequestProcessor rp = new RequestProcessor(SourceHandleImpl.class);
    private final boolean supported;

    public SourceHandleImpl(final ProjectHandle<ODSProject> projectHandle, ScmRepository repository, boolean isSupported) {
        this.repository = repository;
        prefs = NbPreferences.forModule(SourceHandleImpl.class);
        this.projectHandle = projectHandle;
        this.supported = isSupported;
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this , OpenProjects.getDefault()));
        initRecent();
    }

    public ScmRepository getRepository() {
        return repository;
    }

    public ProjectHandle<ODSProject> getProjectHandle() {
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

    List<NbProjectHandle> recent = new ArrayList<NbProjectHandle>();
    @Override
    public List<NbProjectHandle> getRecentProjects() {
        return recent;
    }

    @Override
    public File getWorkingDirectory() {
        try {
            String uriString = prefs.get(WORKINGDIR + repository.getUrl(), null); // NOI18N
            if (uriString!=null) {
                return getWorkDir(uriString);
            } else if (repository.getAlternateUrl() != null && !repository.getAlternateUrl().isEmpty()) {
                uriString = prefs.get(WORKINGDIR + repository.getAlternateUrl(), null);
                if (uriString != null) {
                    return getWorkDir(uriString);
                }
            }
            return guessWorkdir();
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return guessWorkdir();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final List<Project> newProjects = getNewProjects((Project[])evt.getOldValue(), (Project[])evt.getNewValue());
        rp.post(new Runnable() {
            public void run() {
                addToRecentProjects(newProjects, true);
            }
        });
    }

    void remove(NbProjectHandleImpl aThis) {
        recent.remove(aThis);
        storeRecent();
        projectHandle.firePropertyChange(ProjectHandle.PROP_SOURCE_LIST, null, null);
    }

    void refresh() {
        if (projectHandle!=null) {
            projectHandle.firePropertyChange(ProjectHandle.PROP_SOURCE_LIST, null, null);
        }
    }

    void setWorkingDirectory (String url, File dest) {
        prefs.put(WORKINGDIR + url, Utilities.toURI(dest).toString()); //NOI18N
    }

    private synchronized void addToRecentProjects(List<Project> newProjects, boolean fireChanges) {
        for (Project prj : newProjects) {
            try {
                if (isUnder(prj.getProjectDirectory())) {
                    NbProjectHandleImpl nbHandle = RecentProjectsCache.getDefault().getProjectHandle(prj, this, removeHandler);
                    recent.remove(nbHandle);
                    recent.add(0, nbHandle);
                    if (recent.size()>MAX_PROJECTS) {
                        recent.remove(MAX_PROJECTS);
                    }
                    storeRecent();
                    if (fireChanges) {
                        projectHandle.firePropertyChange(ProjectHandle.PROP_SOURCE_LIST, null, null);
                    }    
                }
            } catch (IOException ex) {
                Logger.getLogger(SourceHandleImpl.class.getName()).fine("Project not found for " + prj);
            }
        }
    }

    private List<Project> getNewProjects(Project[] old, Project[] newp) {
        if (newp == null) {
            return Collections.emptyList();
        } else if (old == null) {
            return Arrays.asList(newp);
        }
        List result = new ArrayList();
        result.addAll(Arrays.asList(newp));
        result.removeAll(Arrays.asList(old));
        return result;
    }

    private File guessWorkdir() {
        if (recent.isEmpty()) {
            return null;
        }
        try {
            final FileObject parent = ((NbProjectHandleImpl) recent.iterator().next()).getProject().getProjectDirectory().getParent();
            if (parent.isValid())
                return FileUtil.toFile(parent);
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private void initRecent() {
        if (prefs==null) {
            //external repository not supported
            return;
        }
        List<String> roots = getStringList(prefs, RECENTPROJECTS_PREFIX + repository.getUrl());
        for (String root:roots) {
            try {
                NbProjectHandleImpl nbH = RecentProjectsCache.getDefault().getProjectHandle(new URL(root), this, removeHandler);
                if (nbH!=null)
                    recent.add(nbH);
            } catch (IOException ex) {
                Logger.getLogger(SourceHandleImpl.class.getName()).fine("Project not found for " + root);
            }
        }
        int count = recent.size();
        List list = new LinkedList();
        final Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i=0;count<MAX_PROJECTS && i<openProjects.length;i++) {
            if (isUnder(openProjects[i].getProjectDirectory())) {
                list.add(openProjects[i]);
                count++;
            }
        }
        
        addToRecentProjects(list, false);
    }

    private boolean isUnder(FileObject projectDirectory) {
        String remoteLocation = (String) projectDirectory.getAttribute("ProvidedExtensions.RemoteLocation"); //NOI18N
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

    private boolean isUnder(String remoteLocation, String location) {
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
        List<String> value = new ArrayList<String>();
        for (NbProjectHandle nbp:recent) {
            value.add(((NbProjectHandleImpl) nbp).getUrl());
        }
        if (prefs!=null)
            putStringList(prefs, RECENTPROJECTS_PREFIX + repository.getUrl(), value);
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

    private NbProjectHandleImpl.RemoveHandler removeHandler = new NbProjectHandleImpl.RemoveHandler() {
        @Override
        public void remove(NbProjectHandleImpl nbProjectHandle) {
            SourceHandleImpl.this.remove(nbProjectHandle);
        }
    };    

    private File getWorkDir (String uriString) throws IllegalArgumentException, URISyntaxException {
        URI uri = new URI(uriString);
        final File file = Utilities.toFile(uri);
        FileObject f = FileUtil.toFileObject(file);
        if (f==null || !f.isValid())
            return null;
        return file;
    }
}
