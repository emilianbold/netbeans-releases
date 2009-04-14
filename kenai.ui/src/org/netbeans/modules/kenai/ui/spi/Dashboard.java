/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import org.netbeans.modules.kenai.ui.dashboard.DashboardImpl;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public abstract class Dashboard {

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
}
