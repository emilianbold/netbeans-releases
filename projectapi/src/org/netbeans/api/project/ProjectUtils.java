/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.project;

import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeEvent;
import org.netbeans.spi.project.ProjectIconAnnotator;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.projectapi.AuxiliaryConfigBasedPreferencesProvider;
import org.netbeans.modules.projectapi.AuxiliaryConfigImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 * Utility methods to get information about {@link Project}s.
 * @author Jesse Glick
 */
public class ProjectUtils {

    private ProjectUtils() {}

    private static final Logger LOG = Logger.getLogger(ProjectUtils.class.getName());
    
    /**
     * Get basic information about a project.
     * If the project has a {@link ProjectInformation} instance in its lookup,
     * that is used. Otherwise, a basic dummy implementation is returned.
     * @param p a project
     * @return some information about it
     * @see Project#getLookup
     */
    public static ProjectInformation getInformation(@NonNull Project p) {
        Lookup l = p.getLookup();
        ProjectInformation pi = l.lookup(ProjectInformation.class);
        return new AnnotateIconProxyProjectInformation(pi != null ? pi : new BasicInformation(p));
    }
    
    /**
     * Get a list of sources for a project.
     * If the project has a {@link Sources} instance in its lookup,
     * that is used. Otherwise, a basic implementation is returned
     * using {@link GenericSources#genericOnly}.
     * @param p a project
     * @return a list of sources for it
     * @see Project#getLookup
     */
    public static Sources getSources(@NonNull Project p) {
        Lookup l = p.getLookup();
        Sources s = l.lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    /**
     * Check whether a project has, or might have, cycles in its subproject graph.
     * <p>
     * If the candidate parameter is null, this simply checks whether the master
     * project's current directed graph of (transitive) subprojects contains any
     * cycles. If the candidate is also passed, this checks whether the master
     * project's subproject graph would contain cycles if the candidate were added
     * as a (direct) subproject of the master project.
     * </p>
     * <p>
     * All cycles are reported even if they do not contain the master project.
     * </p>
     * <p>
     * If the master project already contains the candidate as a (direct) subproject,
     * the effect is as if the candidate were null.
     * </p>
     * <p>
     * Projects with no {@link SubprojectProvider} are considered to have no
     * subprojects, just as if the provider returned an empty set.
     * </p>
     * <p>
     * Acquires read access.
     * </p>
     * <p class="nonnormative">
     * Project types which let the user somehow configure subprojects in the GUI
     * (perhaps indirectly, e.g. via a classpath) should use this call to check
     * for possible cycles before adding new subprojects.
     * </p>
     * @param master a project to root the subproject graph from
     * @param candidate a potential direct subproject of the master project, or null
     * @return true if the master project currently has a cycle somewhere in its
     *         subproject graph, regardless of the candidate parameter, or if the
     *         candidate is not null and the master project does not currently have
     *         a cycle but would have one if the candidate were added as a subproject
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=43845">Issue #43845</a>
     */
    public static boolean hasSubprojectCycles(final Project master, final Project candidate) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                return visit(new HashMap<Project,Boolean>(), master, master, candidate);
            }
        });
    }
    
    /**
     * Return {@link Preferences} for the given project and given module.
     * 
     * <p class="nonnormative">
     * The preferences are stored in the project using either {@link AuxiliaryConfiguration}
     * or {@link AuxiliaryProperties}.
     * </p>
     * 
     * @param project project for which preferences should be returned
     * @param clazz module specification as in {@link org.openide.util.NbPreferences#forModule(java.lang.Class)}
     * @param shared whether the returned settings should be shared
     * @return {@link Preferences} for the given project
     * @since 1.16
     */
    public static Preferences getPreferences(@NonNull Project project, @NonNull Class clazz, boolean shared) {
        Parameters.notNull("project", project);
        Parameters.notNull("clazz", clazz);
        
        return AuxiliaryConfigBasedPreferencesProvider.getPreferences(project, clazz, shared);
    }
    
    /**
     * Do a DFS traversal checking for cycles.
     * @param encountered projects already encountered in the DFS
     * @param curr current node to visit
     * @param master the original master project (for use with candidate param)
     * @param candidate a candidate added subproject for master, or null
     */
    private static boolean visit(@NonNull Map<Project,Boolean> encountered, @NonNull Project curr, Project master, @NullAllowed Project candidate) {
        if (encountered.containsKey(curr)) {
            if (encountered.get(curr)) {
                return false;
            } else {
                LOG.log(Level.FINE, "Encountered cycle in {0} from {1} at {2} via {3}", new Object[] {master, candidate, curr, encountered});
                return true;
            }
        }
        encountered.put(curr, false);
        SubprojectProvider spp = curr.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            Set<? extends Project> subprojects = spp.getSubprojects();
            LOG.log(Level.FINEST, "Found subprojects {0} from {1}", new Object[] {subprojects, curr});
            for (Project child : subprojects) {
                if (visit(encountered, child, master, candidate)) {
                    return true;
                } else if (candidate == child) {
                    candidate = null;
                }
            }
        }
        if (candidate != null && curr == master) {
            if (visit(encountered, candidate, master, candidate)) {
                return true;
            }
        }
        assert !encountered.get(curr);
        encountered.put(curr, true);
        return false;
    }
    
    private static final class BasicInformation implements ProjectInformation {
        
        private final Project p;
        
        public BasicInformation(Project p) {
            this.p = p;
        }
        
        @Override
        public String getName() {
            return getProjectDirectory().toURL().toExternalForm();
        }
        
        @Override
        public String getDisplayName() {
            return getProjectDirectory().getNameExt();
        }
        
        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/projectapi/resources/empty.gif", false); // NOI18N
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        @Override
        public Project getProject() {
            return p;
        }

        @NonNull
        private FileObject getProjectDirectory() {
            final FileObject pd = p.getProjectDirectory();
            if (pd == null) {
                throw new IllegalStateException(String.format("Project: %s returned null project directory.", p));  //NOI18N
            }
            return pd;
        }
        
    }

    private static final class AnnotateIconProxyProjectInformation implements ProjectInformation, PropertyChangeListener, ChangeListener, LookupListener {

        private final ProjectInformation pinfo;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final Set<ProjectIconAnnotator> annotators = new WeakSet<ProjectIconAnnotator>();
        private boolean annotatorsInitialized = false;
        private boolean addedPropertyListener = false;
        private final Object LOCK = new Object(); //protects access to annotatorsInitialized, addedPropertyListener and icon
        private Result<ProjectIconAnnotator> annotatorResult;
        private Icon icon;

        @SuppressWarnings("LeakingThisInConstructor")
        public AnnotateIconProxyProjectInformation(ProjectInformation pi) {
            pinfo = pi;
        }

        private void annotatorsChanged() {
            synchronized (LOCK) {
                if (!annotatorsInitialized) {
                    annotatorResult = Lookup.getDefault().lookupResult(ProjectIconAnnotator.class);
                    annotatorResult.addLookupListener(WeakListeners.create(LookupListener.class, this, annotatorResult));
                    annotatorsInitialized = true;
                }
                for (ProjectIconAnnotator pa : annotatorResult.allInstances()) {
                    if (annotators.add(pa)) {
                        pa.addChangeListener(WeakListeners.change(this, pa));
                    }
                }
            }
        }

        public @Override void resultChanged(LookupEvent ev) {
            annotatorsChanged();
            updateIcon(true);
        }
        
        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (ProjectInformation.PROP_ICON.equals(evt.getPropertyName())) {
                synchronized (LOCK) {
                    if (!annotatorsInitialized) {
                        annotatorsChanged();
                    }
                }
                updateIcon(true);
            } else {
                pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }
        
        public @Override void stateChanged(ChangeEvent e) {
            updateIcon(true);
        }
        
        private void updateIcon(boolean fireChange) {
            Icon original = pinfo.getIcon();
            if (original == null) {
                // Forbidden generally but common in tests.
                return;
            }
            Image _icon = ImageUtilities.icon2Image(original);
            final Project prj = getProject();
            assert prj != null : "ProjectIformation.getProject() == null for " + pinfo;    //NOI18N
            if (prj != null) {
                for (ProjectIconAnnotator pa : annotatorResult.allInstances()) {
                    _icon = pa.annotateIcon(prj, _icon, false);
                }
            }
            Icon old = icon;
            Icon newOne;
            synchronized (LOCK) {
                icon = ImageUtilities.image2Icon(_icon);
                newOne = icon;
            }
            if (fireChange) {
                pcs.firePropertyChange(ProjectInformation.PROP_ICON, old, newOne);
            }
        }

        public @Override Icon getIcon() {
            synchronized (LOCK) {
                if (icon == null) {
                    if (!annotatorsInitialized) {
                        annotatorsChanged();
                    }
                    updateIcon(false);
                }
                return icon;
            }
        }
       
        public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
            synchronized (LOCK) {
                if (!addedPropertyListener) {
                    pinfo.addPropertyChangeListener(WeakListeners.propertyChange(this, pinfo));
                    addedPropertyListener = true;
                }
            }
            pcs.addPropertyChangeListener(listener);
        }

        public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public @Override Project getProject() {
            return pinfo.getProject();
        }
        public @Override String getName() {
            return pinfo.getName();
        }
        public @Override String getDisplayName() {
            return pinfo.getDisplayName();
        }

    }

    
    /**
     * Find a way of storing extra configuration in a project.
     * If the project's {@linkplain Project#getLookup lookup} does not provide an instance,
     * a fallback implementation is used.
     * <p class="nonnormative">
     * The current fallback implementation uses {@linkplain FileObject#setAttribute file attributes}
     * for "nonsharable" configuration, and a specially named file in the project directory
     * for "sharable" configuration. For compatibility purposes (in case a project adds an
     * {@link AuxiliaryConfiguration} instance to its lookup where before it had none),
     * the fallback storage is read (but not written) even if there is an instance in project lookup.
     * </p>
     * @param project a project
     * @return an auxiliary configuration handle
     * @since org.netbeans.modules.projectapi/1 1.17
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(@NonNull Project project) {
        Parameters.notNull("project", project);
        return new AuxiliaryConfigImpl(project);
    }

    /**
     * Gets a directory in which modules may store arbitrary extra unversioned files
     * associated with a project.
     * These could be caches of information found in sources, logs or snapshots
     * from activities associated with developing the project, etc.
     * <p>
     * If the project supplies a {@link CacheDirectoryProvider}, that will be used
     * for the parent directory. Otherwise an unspecified storage area will be used.
     * @param project a project
     * @param owner a class from the calling module (each module or package will get its own space)
     * @return a directory available for storing miscellaneous files
     * @throws IOException if no such directory could be created
     * @since org.netbeans.modules.projectapi/1 1.26
     */
    public static FileObject getCacheDirectory(@NonNull Project project, @NonNull Class<?> owner) throws IOException {
        FileObject d;
        CacheDirectoryProvider cdp = project.getLookup().lookup(CacheDirectoryProvider.class);
        if (cdp != null) {
            d = cdp.getCacheDirectory();
        } else {
            d = FileUtil.createFolder(FileUtil.getConfigRoot(),
                    String.format("Projects/extra/%s-%08x", getInformation(project).getName().replace('/', '_'), // NOI18N
                                  project.getProjectDirectory().getPath().hashCode()));
        }
        return FileUtil.createFolder(d, AuxiliaryConfigBasedPreferencesProvider.findCNBForClass(owner));
    }

}
