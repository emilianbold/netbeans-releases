/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.kenai.ui;

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
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.spi.NbProjectHandle;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.SourceHandle;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.versioning.system.cvss.api.CVS;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kubec, Jan Becicka
 */
public class SourceHandleImpl extends SourceHandle implements PropertyChangeListener {

    private KenaiFeature feature;
    private Preferences prefs;
    private ProjectHandle projectHandle;
    private static final int MAX_PROJECTS = 5;
    private static final String RECENTPROJECTS_PREFIX = "recent.projects."; // NOI18N
    private String externalScmType=SCM_TYPE_UNKNOWN;
    public static final String SCM_TYPE_UNKNOWN = "unknown";//NOI18N
    public static final String SCM_TYPE_CVS = "cvs";//NOI18N

    public String getExternalScmType() {
        return externalScmType;
    }

    public SourceHandleImpl(final ProjectHandle projectHandle, KenaiFeature ftr) {
        feature = ftr;
        if (KenaiService.Names.MERCURIAL.equals(feature.getService())) {
            prefs= NbPreferences.forModule(Mercurial.class);
        } else if (KenaiService.Names.SUBVERSION.equals(feature.getService())) {
            prefs= NbPreferences.forModule(Subversion.class);
        } else if (KenaiService.Names.EXTERNAL_REPOSITORY.equals(feature.getService())) {
            if (Subversion.isRepository(feature.getLocation())) {
                externalScmType=KenaiService.Names.SUBVERSION;
                return;
            } else if (CVS.isRepository(feature.getLocation())) {
                externalScmType=SCM_TYPE_CVS;
                return;
            } else if (Mercurial.isRepository(feature.getLocation())) {
                externalScmType=KenaiService.Names.MERCURIAL;
                return;
            }
        }
        this.projectHandle = projectHandle;
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this , OpenProjects.getDefault()));
        initRecent();
    }

    @Override
    public String getDisplayName() {
        return feature.getDisplayName();
    }

    @Override
    public boolean isSupported() {
        // names of those services are values returned from Kenai WS API !!!
        if (KenaiService.Names.SUBVERSION.equals(feature.getService()) ||
            KenaiService.Names.MERCURIAL.equals(feature.getService()) ||
            KenaiService.Names.EXTERNAL_REPOSITORY.equals(feature.getService())) {
            return true;
        }
        return false;
    }

    @Override
    public String getScmFeatureName() {
        return feature.getService();
    }

    List<NbProjectHandle> recent = new ArrayList<NbProjectHandle>();
    @Override
    public List<NbProjectHandle> getRecentProjects() {
        return recent;
    }

    @Override
    public File getWorkingDirectory() {
        if (prefs==null)
            return guessWorkdir();
        try {
            String uriString = prefs.get("working.dir." + feature.getLocation(), null); // NOI18N
            if (uriString!=null) {
                URI uri = new URI(uriString);
                final File file = new File(uri);
                FileObject f = FileUtil.toFileObject(file);
                if (f==null || !f.isValid())
                    return null;
                return file;
            } else {
                return guessWorkdir();
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return guessWorkdir();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        List<Project> newProjects = getNewProjects((Project[])evt.getOldValue(), (Project[])evt.getNewValue());
        addToRecentProjects(newProjects, true);
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

    private void addToRecentProjects(List<Project> newProjects, boolean fireChanges) {
        for (Project prj : newProjects) {
            try {
                if (isUnder(prj.getProjectDirectory())) {
                    NbProjectHandleImpl nbHandle = RecentProjectsCache.getDefault().getProjectHandle(prj, this);
                    recent.remove(nbHandle);
                    recent.add(0, nbHandle);
                    if (recent.size()>MAX_PROJECTS) {
                        recent.remove(MAX_PROJECTS);
                    }
                    storeRecent();
                    if (fireChanges)
                        projectHandle.firePropertyChange(ProjectHandle.PROP_SOURCE_LIST, null, null);
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
        List<String> roots = getStringList(prefs, RECENTPROJECTS_PREFIX + feature.getLocation());
        for (String root:roots) {
            try {
                NbProjectHandleImpl nbH = RecentProjectsCache.getDefault().getProjectHandle(new URL(root), this);
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
        String remoteLocation = (String) projectDirectory.getAttribute("ProvidedExtensions.RemoteLocation"); // NOI18N
        if (remoteLocation==null || remoteLocation.length()==0) {
            return false;
        }
        if (feature.getLocation().equals(remoteLocation))
            return true;
        return false;
    }

    private void storeRecent() {
        List<String> value = new ArrayList<String>();
        for (NbProjectHandle nbp:recent) {
            value.add(((NbProjectHandleImpl) nbp).url.toString());
        }
        if (prefs!=null)
            putStringList(prefs, RECENTPROJECTS_PREFIX + feature.getLocation(), value);
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
}
