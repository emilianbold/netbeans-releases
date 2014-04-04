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

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectManagerImplementation;
import org.netbeans.spi.project.ProjectManagerImplementation.LockManagerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Manages loaded projects.
 * @author Jesse Glick
 * @author Tomas Zezula
 */
public final class ProjectManager {

    private static final Logger LOG = Logger.getLogger(ProjectManager.class.getName());
    private static final ProjectManager DEFAULT = new ProjectManager();
    private final ProjectManagerImplementation impl;
    private final LockManager lm;
    
    private ProjectManager() {
        this.impl = Lookup.getDefault().lookup(ProjectManagerImplementation.class);
        if (this.impl == null) {
            throw new IllegalStateException("No ProjectManagerImplementation found in global Lookup."); //NOI18N
        }
        this.lm = new LockManager(impl.getLockManager());
        LOG.log(
            Level.FINE,
            "ProjectManager created with implementation {0} : {1}", //NOI18N
            new Object[]{
                this.impl,
                this.impl.getClass()
            });
    }       

    /**
     * Returns the singleton project manager instance.
     * @return the default instance
     */
    @NonNull
    public static ProjectManager getDefault() {
        return DEFAULT;
    }    

    /**
     * Get a read/write lock to be used for all project metadata accesses.
     * All methods relating to recognizing and loading projects, saving them,
     * getting or setting their metadata, etc. should be controlled by this
     * mutex and be marked as read operations or write operations. Unless
     * otherwise stated, project-related methods automatically acquire the
     * mutex for you, so you do not necessarily need to pay attention to it;
     * but you may directly acquire the mutex in order to ensure that a block
     * of reads does not have any interspersed writes, or in order to ensure
     * that a write is not clobbering an unrelated write, etc.
     * @return a general read/write lock for project metadata operations of all sorts
     */
    @NonNull
    public static Mutex mutex() {
        return getDefault().impl.getMutex();
    }

    /**
     * Returns projects locks manager.
     * @return the {@link LockManager}
     * @since 1.59
     */
    @NonNull
    public static LockManager getLockManager() {
        return getDefault().lm;
    }
        
    /**
     * Find an open project corresponding to a given project directory.
     * Will be created in memory if necessary.
     * <p>
     * Acquires read access.
     * </p>
     * <p>
     * It is <em>not</em> guaranteed that the returned instance will be identical
     * to that which is created by the appropriate {@link ProjectFactory}. In
     * particular, the project manager is free to return only wrapper <code>Project</code>
     * instances which delegate to the factory's implementation. If you know your
     * factory created a particular project, you cannot safely cast the return value
     * of this method to your project type implementation class; you should instead
     * place an implementation of some suitable private interface into your project's
     * lookup, which would be safely proxied.
     * </p>
     * @param projectDirectory the project top directory
     * @return the project (object identity may or may not vary between calls)
     *         or null if the directory is not recognized as a project by any
     *         registered {@link ProjectFactory}
     *         (might be null even if {@link #isProject} returns true)
     * @throws IOException if the project was recognized but could not be loaded
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    @CheckForNull
    public Project findProject(@NonNull final FileObject projectDirectory) throws IOException, IllegalArgumentException {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        return impl.findProject(projectDirectory);
    }
        
    
    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject</code> is true but {@link #findProject}
     * will return null), for example if there is trouble loading the project.
     * False negatives are possible only if there are bugs in the project factory.</p>
     * <p>Acquires read access.</p>
     * <p class="nonnormative">
     * You do <em>not</em> need to call this method if you just plan to call {@link #findProject}
     * afterwards. It is intended for only those clients which would discard the
     * result of {@link #findProject} other than to check for null, and which
     * can also tolerate false positives.
     * </p>
     * @param projectDirectory a directory which may be some project's top directory
     * @return true if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     */
    public boolean isProject(@NonNull final FileObject projectDirectory) throws IllegalArgumentException {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        return isProject2(projectDirectory) != null;
    }

    /**
     * Check whether a given directory is likely to contain a project without
     * actually loading it. The returned {@link org.netbeans.api.project.ProjectManager.Result} object contains additional
     * information about the found project.
     * Should be faster and use less memory than {@link #findProject} when called
     * on a large number of directories.
     * <p>The result is not guaranteed to be accurate; there may be false positives
     * (directories for which <code>isProject2</code> is non-null but {@link #findProject}
     * will return null), for example if there is trouble loading the project.
     * False negatives are possible only if there are bugs in the project factory.</p>
     * <p>Acquires read access.</p>
     * <p class="nonnormative">
     * You do <em>not</em> need to call this method if you just plan to call {@link #findProject}
     * afterwards. It is intended for only those clients which would discard the
     * result of {@link #findProject} other than to check for null, and which
     * can also tolerate false positives.
     * </p>
     * @param projectDirectory a directory which may be some project's top directory
     * @return Result object if the directory is likely to contain a project according to
     *              some registered {@link ProjectFactory}, or null if not a project folder.
     * @throws IllegalArgumentException if the supplied file object is null or not a folder
     * @since org.netbeans.modules.projectapi 1.22
     */
    @CheckForNull
    public Result isProject2(@NonNull final FileObject projectDirectory) throws IllegalArgumentException {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        return impl.isProject(projectDirectory);
    }
        
    /**
     * Clear the cached list of folders thought <em>not</em> to be projects.
     * This may be useful after creating project metadata in a folder, etc.
     * Cached project objects, i.e. folders that <em>are</em> known to be
     * projects, are not affected.
     */
    public void clearNonProjectCache() {
        impl.clearNonProjectCache();
    }
    
    /**
     * Get a list of all projects which are modified and need to be saved.
     * <p>Acquires read access.
     * @return an immutable set of projects
     */
    @NonNull
    public Set<Project> getModifiedProjects() {
        return impl.getModifiedProjects();
    }
    
    /**
     * Check whether a given project is current modified.
     * <p>Acquires read access.
     * @param p a project loaded by this manager
     * @return true if it is modified, false if has been saved since the last modification
     */
    public boolean isModified(@NonNull final Project p) {
        return impl.isModified(p);
    }
    
    /**
     * Save one project (if it was in fact modified).
     * <p>Acquires write access.</p>
     * <p class="nonnormative">
     * Although the project infrastructure permits a modified project to be saved
     * at any time, current UI principles dictate that the "save project" concept
     * should be internal only - i.e. a project customizer should automatically
     * save the project when it is closed e.g. with an "OK" button. Currently there
     * is no UI display of modified projects; this module does not ensure that modified projects
     * are saved at system exit time the way modified files are, though the Project UI
     * implementation module currently does this check.
     * </p>
     * @param p the project to save
     * @throws IOException if it cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveProject(@NonNull final Project p) throws IOException {
        Parameters.notNull("p", p); //NOI18N
        impl.saveProject(p);
    }
    
    /**
     * Save all modified projects.
     * <p>Acquires write access.
     * @throws IOException if any of them cannot be saved
     * @see ProjectFactory#saveProject
     */
    public void saveAllProjects() throws IOException {
        impl.saveAllProjects();
    }
    
    /**
     * Checks whether a project is still valid.
     * <p>Acquires read access.</p>
     *
     * @since 1.6
     *
     * @param p a project
     * @return true if the project is still valid, false if it has been deleted
     */
    public boolean isValid(@NonNull final Project p) {
        Parameters.notNull("p", p); //NOI18N
        return impl.isValid(p);
    }
    
    /**
     *  A result (immutable) object returned from {@link org.netbeans.api.project.ProjectManager#isProject2} method.
     *  To be created by {@link org.netbeans.spi.project.ProjectFactory2} project factories.
     *  @since org.netbeans.modules.projectapi 1.22
     */
    public static final class Result {
        private final Icon icon;


        public Result(Icon icon) {
            this.icon = icon;
        }

        /**
         * Get the project icon.
         * @return project type icon for the result or null if the icon cannot be found this way.
         */
        public Icon getIcon() {
            return icon;
        }
    }

    /**
     * Project lock manager.
     * Todo: replace with plugable {@link Mutex}.
     * @since 1.59
     */
    public static final class LockManager {
        private final LockManagerImplementation impl;

        private LockManager(@NonNull final LockManagerImplementation impl) {
            Parameters.notNull("impl", impl);
            this.impl = impl;
        }

        @NullUnknown
        public <R> R readAccess(
            @NonNull Mutex.Action<R> action,
            @NonNull Project project,
            @NonNull Project... otherProjects) {
            Parameters.notNull("action", action);   //NOI18N
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("otherProjects", otherProjects); //NOI18N
            return impl.readAccess(action, project, otherProjects);
        }

        @NullUnknown
        public <R> R writeAccess(
            @NonNull Mutex.Action<R> action,
            boolean autoSave,
            @NonNull Project project,
            @NonNull Project... otherProjects) {
            Parameters.notNull("action", action);   //NOI18N
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("otherProjects", otherProjects); //NOI18N
            return impl.writeAccess(action, autoSave, project, otherProjects);
        }
    }
}
