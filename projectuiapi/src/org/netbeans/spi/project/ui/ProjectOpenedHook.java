/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui;

import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;

/**
 * A hook which can be run when a project is "opened" or "closed" in the GUI.
 * <p>
 * The meaning of these terms is intentionally left vague, but typically opening
 * a project signals that the user may wish to work with it, so it would be a good
 * idea to make sure caches are up to date, etc. It is perfectly possible to load
 * and use (even run) projects which are <em>not</em> open, so any project type
 * provider using this hook cannot rely on it for basic semantics.
 * </p>
 * <p>
 * XXX run with mutex read or write held?
 * </p>
 * <p>
 * {@link #projectOpened} and {@link #projectClosed} are always called in pairs,
 * e.g. a project cannot be opened twice in a row without being closed in between.
 * Also a project left open at the end of one VM session will receive
 * {@link #projectClosed} before shutdown and (if an open project list is persisted)
 * {@link #projectOpened} sometime during the next startup.
 * </p>
 * <p>
 * An instance should be placed into a project's lookup to register it.
 * All instances found in the lookup will be notified on project open and close.
 * </p>
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public abstract class ProjectOpenedHook {
    

    static {
        ProjectOpenedTrampoline.DEFAULT = new ProjectOpenedTrampoline() {
            public void projectOpened(ProjectOpenedHook hook) {
                hook.projectOpened();
            }
            public void projectClosed(ProjectOpenedHook hook) {
                hook.projectClosed();
            }
        };
    }

    
    /**
     * Default constructor for use by subclasses.
     */
    protected ProjectOpenedHook() {}
    
    /**
     * Called when a project is opened in the GUI.
     * <div class="nonnormative">
     * <p>Typical things to do here:</p>
     * <ul>
     * <li><p>
     * Update build scripts using
     * <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/GeneratedFilesHelper.html#refreshBuildScript"><code>GeneratedFilesHelper.refreshBuildScript(...)</code></a>.
     * </p></li>
     * <li><p>Call <a href="@JAVA/API@/org/netbeans/api/java/classpath/GlobalPathRegistry.html#register"><code>GlobalPathRegistry.register(...)</code></a>
     * with source, compile, and boot paths known to the project.</p></li>
     * <li><p>Write property <code>user.properties.file</code> to <code>private.properties</code>
     * with absolute file path of the <code>build.properties</code> from 
     * the IDE's user directory. This makes it easier for the user to run headless 
     * builds in some cases. The IDE's user directory is defined in 
     * <code>netbeans.user</code> property of IDE's VM.</p></li>
     * </ul>
     * </div>
     */
    protected abstract void projectOpened();
    
    /**
     * Called when a project is closed in the GUI.
     * <div class="nonnormative">
     * <p>Typical things to do here:</p>
     * <ul>
     * <li><p>
     * Call
     * {@link org.netbeans.api.project.ProjectManager#saveProject}
     * as a precaution in case the project was modified in an unusual
     * way (e.g. using
     * {@link org.netbeans.spi.project.AuxiliaryConfiguration}).
     * </p></li>
     * <li><p>Call <a href="@JAVA/API@/org/netbeans/api/java/classpath/GlobalPathRegistry.html#unregister"><code>GlobalPathRegistry.unregister(...)</code></a>
     * with the same paths are were previously registered.</p></li>
     * </ul>
     * </div>
     */
    protected abstract void projectClosed();
    
}
