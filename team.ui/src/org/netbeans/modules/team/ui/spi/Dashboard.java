/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;

/**
 * Singleton providing access to Team Dashboard window.
 *
 * @author S. Aubrecht
 */
public abstract class Dashboard<S extends TeamServer, P> {

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

    /**
     * Display given Team user in the Dashboard window, the UI will start querying for
     * user's member projects.
     * Typically should be called after successful login.
     * @param login User login details.
     */
    public abstract void setUser( LoginHandle login );

    /**
     * Add a Team project to the Dashboard.
     * @param project
     * @param isMemberProject True if current team user is project's owner or observer.
     * @see ProjectAccessor#getOpenNonMemberProjectAction()
     */
    public abstract void addProject( ProjectHandle project, boolean isMemberProject, boolean select );

    public abstract void removeProject( ProjectHandle project );

    /**
     * getter for all open projects in Team Dashboard
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

    /**
     * currently visible team
     * @return
     */
    public abstract S getServer();
    
    public abstract Action createLogoutAction();
    public abstract Action createLoginAction();
    public abstract LeafNode createMemberNode(MemberHandle user, TreeListNode parent);
    protected abstract void setSelectedServer(ProjectHandle<P> project);
    public abstract TreeListNode createMessagingNode(ProjectNode pn, ProjectHandle<P> project);
    public abstract TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<P> project);
    public abstract TreeListNode createMyProjectNode(ProjectHandle<P> p);   
    public abstract TreeListNode createSourceNode(SourceHandle s, SourceListNode sln);    

    public abstract ProjectAccessor<S, P> getProjectAccessor();
    public abstract MessagingAccessor<P> getMessagingAccessor();
    public abstract MemberAccessor<P> getMemberAccessor();
    public abstract SourceAccessor<P> getSourceAccessor();
    public abstract QueryAccessor<P> getQueryAccessor();
    
}
