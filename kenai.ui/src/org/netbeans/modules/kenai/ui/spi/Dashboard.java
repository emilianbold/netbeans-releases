/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public abstract class Dashboard {

    /**
     * Name of the property that will be fired when some change in opened projects
     * in Dashboard occurs. Firing this property doesn't neccessary mean that number
     * of opened project has changed.
     */
    public static final String PROP_OPENED_PROJECTS = "openedProjects"; // NOI18N

    /**
     * fired when user clicks refresh
     */
    public static final String PROP_REFRESH_REQUEST = "refreshRequest";// NOI18N

    public static Dashboard getDefault() {
        return DashboardImpl.getInstance();
    }

    /**
     * Display given Kenai user in the Dashboard window, the UI will start querying for
     * user's member projects.
     * Typically should be called after successful login.
     * @param login User login details.
     */
    public abstract void setUser( LoginHandle login );

    /**
     * Add a Kenai project to the Dashboard.
     * @param project
     * @param isMemberProject True if current kenai user is project's owner or observer.
     * @see ProjectAccessor#getOpenNonMemberProjectAction()
     */
    public abstract void addProject( ProjectHandle project, boolean isMemberProject );

    public abstract void removeProject( ProjectHandle project );

    /**
     * getter for all open projects in Kenai Dashboard
     * @return array of ProjectHandles
     */
    public abstract ProjectHandle[] getOpenProjects();
    
    /**
     * returns true if given project is member project
     */
    public abstract boolean isMemberProject(ProjectHandle m);
    /**
     * Add listener for listening for property changes related to Dashboard
     * @param listener listener to be notified about property change
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove listener from list of listeners notified about property changes
     * @param listener listener to be removed
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener listener);

}
