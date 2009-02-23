/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public final class Dashboard {

    private Dashboard() {
    }

    /**
     * Display given Kenai user in the Dashboard window, the UI will start querying for
     * user's member projects.
     * Typically should be called after successful login.
     * @param login User login details.
     * @see ActionsFactory.getLoginAction
     */
    public void setUser( LoginHandle login ) {
        //TODO implement
    }

    /**
     * Add a Kenai project which current user isn't member of to the Dashboard.
     * @param project
     * @see ActionsFactory.getOpenNonMemberProjectAction
     */
    public void addNonMemberProject( ProjectHandle project ) {
        //TODO implement
    }

//    public abstract void removeNonMemberProject( ProjectHandle project );
}
