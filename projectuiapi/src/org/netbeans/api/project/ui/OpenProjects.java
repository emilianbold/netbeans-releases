/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project.ui;

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * List of projects open in the GUI.
 * <p class="nonnormative">
 * <strong>Warning:</strong> this API is intended only for a limited set of use
 * cases where obtaining a list of all open projects is really the direct goal.
 * For example, you may wish to display a chooser letting the user select a
 * file from among the top-level source folders of any open project.
 * For many cases, however, this API is not the correct approach, so use it as
 * a last resort. Consider <a href="@JAVA/API@/org/netbeans/api/java/classpath/GlobalPathRegistry.html"><code>GlobalPathRegistry</code></a>
 * and {@link org.netbeans.spi.project.ui.ProjectOpenedHook}
 * first. Only certain operations should actually be aware of which projects
 * are "open"; by default, all project functionality should be available whether
 * it is open or not.
 * </p>
 * @author Jesse Glick, Petr Hrebejk
 */
public final class OpenProjects {
    
    /**
     * Property representing open projects.
     * @see #getOpenProjects
     */
    public static final String PROPERTY_OPEN_PROJECTS = "openProjects"; // NOI18N
    
    private static OpenProjects INSTANCE = new OpenProjects();
    
    private OpenProjectsTrampoline trampoline;
    
    private OpenProjects() {
        this.trampoline = Utilities.getOpenProjectsTrampoline();
    }

    /**
     * Get the default singleton instance of this class.
     * @return the default instance
     */
    public static OpenProjects getDefault() {                
        return INSTANCE;
    }
    
    /**
     * Gets a list of currently open projects.
     * @return list of projects currently opened in the IDE's GUI; order not specified
     */
    public Project[] getOpenProjects() {
        return trampoline.getOpenProjectsAPI();
    }
            
    /**
     * Adds a listener to changes in the set of open projects.
     * As this class is a singleton and is not subject to garbage collection,
     * it is recommended to add only weak listeners, or remove regular listeners reliably.
     * @param listener a listener to add
     * @see #PROPERTY_OPEN_PROJECTS
     */    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.addPropertyChangeListenerAPI( listener );
    }
    
    /**
     * Removes a listener.
     * @param listener a listener to remove
     */
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        trampoline.removePropertyChangeListenerAPI( listener );
    }
    
}
