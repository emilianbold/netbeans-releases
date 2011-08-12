/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Links the Maven project with its artifact in the local repository.
 */
@ServiceProviders({@ServiceProvider(service=FileOwnerQueryImplementation.class, position=97), @ServiceProvider(service=MavenFileOwnerQueryImpl.class)})
public class MavenFileOwnerQueryImpl implements FileOwnerQueryImplementation {
    
    private final PropertyChangeListener projectListener;
    private final ChangeSupport cs = new ChangeSupport(this);

    private static final Logger LOG = Logger.getLogger(MavenFileOwnerQueryImpl.class.getName());
    
    public MavenFileOwnerQueryImpl() {
        projectListener = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                    registerProject((NbMavenProjectImpl) evt.getSource());
                    fireChange();
                }
            }
        };
    }
    
    public static MavenFileOwnerQueryImpl getInstance() {
        return Lookup.getDefault().lookup(MavenFileOwnerQueryImpl.class);
    }

    public void registerCoordinates(String groupId, String artifactId, URL owner) {
        String key = groupId + ':' + artifactId;
        prefs().put(key, owner.toString());
        LOG.log(Level.FINE, "Registering {0} under {1}", new Object[] {owner, key});
    }
    
    public void registerProject(NbMavenProjectImpl project) {
        MavenProject model = project.getOriginalMavenProject();
        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        if (groupId.equals("error") && artifactId.equals("error")) {
            LOG.log(Level.FINE, "will not register unloadable {0}", project.getPOMFile());
            return;
        }
        try {
            registerCoordinates(groupId, artifactId, project.getProjectDirectory().getURL());
        } catch (FileStateInvalidException x) {
            LOG.log(Level.INFO, null, x);
        }
        project.getProjectWatcher().removePropertyChangeListener(projectListener);
        project.getProjectWatcher().addPropertyChangeListener(projectListener);
        fireChange();
    }
    
    public void addChangeListener(ChangeListener list) {
        cs.addChangeListener(list);
    }
    
    public void removeChangeListener(ChangeListener list) {
        cs.removeChangeListener(list);
    }
    
    private void fireChange() {
        cs.fireChange();
    }
    
    public @Override Project getOwner(URI uri) {
        LOG.log(Level.FINEST, "getOwner of uri={0}", uri);
        if ("file".equals(uri.getScheme())) { //NOI18N
            File file = new File(uri);
            return getOwner(file);
        }
        return null;
    }
    
    public @Override Project getOwner(FileObject fileObject) {
        LOG.log(Level.FINEST, "getOwner of fileobject={0}", fileObject);
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            return getOwner(file);
        }
        return null;
    }

    /**
     * Utility method to identify a file which might be an artifact in the local repository.
     * @param file a putative artifact
     * @return its coordinates (groupId/artifactId/version), or null if it cannot be identified
     */
    static @CheckForNull String[] findCoordinates(File file) {
        String nm = file.getName(); // commons-math-2.1.jar
        File parentVer = file.getParentFile(); // ~/.m2/repository/org/apache/commons/commons-math/2.1
        if (parentVer != null) {
            File parentArt = parentVer.getParentFile(); // ~/.m2/repository/org/apache/commons/commons-math
            if (parentArt != null) {
                String artifactID = parentArt.getName(); // commons-math
                String version = parentVer.getName(); // 2.1
                if (nm.startsWith(artifactID + '-' + version)) {
                    File parentGroup = parentArt.getParentFile(); // ~/.m2/repository/org/apache/commons
                    if (parentGroup != null) {
                        // Split rest into separate method, to avoid linking EmbedderFactory unless and until needed.
                        return findCoordinates(parentGroup, artifactID, version);
                    }
                }
            }
        }
        return null;
    }
    private static @CheckForNull String[] findCoordinates(File parentGroup, String artifactID, String version) {
        File repo = new File(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir()); // ~/.m2/repository
        String repoS = repo.getAbsolutePath();
        if (!repoS.endsWith(File.separator)) {
            repoS += File.separatorChar; // ~/.m2/repository/
        }
        String parentGroupS = parentGroup.getAbsolutePath();
        if (parentGroupS.endsWith(File.separator)) {
            parentGroupS = parentGroupS.substring(0, parentGroupS.length() - 1);
        }
        if (parentGroupS.startsWith(repoS)) {
            String groupID = parentGroupS.substring(repoS.length()).replace(File.separatorChar, '.'); // org.apache.commons
            return new String[] {groupID, artifactID, version};
        } else {
            return null;
        }
    }

    private Project getOwner(File file) {
        LOG.log(Level.FINER, "Looking for owner of {0}", file);
        String[] coordinates = findCoordinates(file);
        if (coordinates == null) {
            LOG.log(Level.FINE, "{0} not an artifact in local repo", file);
            return null;
        }
        return getOwner(coordinates[0], coordinates[1], coordinates[2]);
    }

    public Project getOwner(String groupId, String artifactId, String version) {
        LOG.log(Level.FINER, "Checking {0} / {1} / {2}", new Object[] {groupId, artifactId, version});
        String key = groupId + ':' + artifactId;
        String ownerURI = prefs().get(key, null);
        if (ownerURI != null) {
            boolean stale = true;
            try {
                FileObject projectDir = URLMapper.findFileObject(new URI(ownerURI).toURL());
                if (projectDir != null && projectDir.isFolder()) {
                    Project p = ProjectManager.getDefault().findProject(projectDir);
                    if (p != null) {
                        NbMavenProjectImpl mp = p.getLookup().lookup(NbMavenProjectImpl.class);
                        if (mp != null) {
                            MavenProject model = mp.getOriginalMavenProject();
                            if (model.getGroupId().equals(groupId) && model.getArtifactId().equals(artifactId)) {
                                if (model.getVersion().equals(version)) {
                                    LOG.log(Level.FINE, "Found match {0}", p);
                                    return p;
                                } else {
                                    LOG.log(Level.FINE, "Mismatch on version {0} in {1}", new Object[] {model.getVersion(), ownerURI});
                                    stale = false; // we merely remembered another version
                                }
                            } else {
                                LOG.log(Level.FINE, "Mismatch on group and/or artifact ID in {0}", ownerURI);
                            }
                        } else {
                            LOG.log(Level.FINE, "Not a Maven project {0} in {1}", new Object[] {p, ownerURI});
                        }
                    } else {
                        LOG.log(Level.FINE, "No such project in {0}", ownerURI);
                    }
                } else {
                    LOG.log(Level.FINE, "No such folder {0}", ownerURI);
                }
            } catch (IOException x) {
                LOG.log(Level.FINE, "Could not load project in " + ownerURI, x);
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
            if (stale) {
                prefs().remove(key); // stale
            }
        } else {
            LOG.log(Level.FINE, "No known owner for {0}", key);
        }
        return null;
    }

    private static Preferences prefs() {
        return NbPreferences.forModule(MavenFileOwnerQueryImpl.class).node("externalOwners"); // NOI18N
    }

}
