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
     * Add a Kenai project which current user isn't member of to the Dashboard.
     * @param project
     * @see ActionsFactory.getOpenNonMemberProjectAction
     */
    public abstract void addProject( ProjectHandle project );

    public abstract void removeProject( ProjectHandle project );

    /**
     * getter for all open projects in Kenai Dashboard
     * @return array of ProjectHandles
     */
    public abstract ProjectHandle[] getOpenProjects();
}
