/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

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
public interface DashboardProvider<S extends TeamServer<P>, P> {

//    /**
//     * Display given Team user in the Dashboard window, the UI will start querying for
//     * user's member projects.
//     * Typically should be called after successful login.
//     * @param login User login details.
//     */
//    public void setUser( LoginHandle login );
//
//    /**
//     * Add a Team project to the Dashboard.
//     * @param project
//     * @param isMemberProject True if current team user is project's owner or observer.
//     * @see ProjectAccessor#getOpenNonMemberProjectAction()
//     */
//    public void addProject( ProjectHandle project, boolean isMemberProject, boolean select );
//
//    public void removeProject( ProjectHandle project );
//
//    /**
//     * getter for all open projects in Team Dashboard
//     * @return array of ProjectHandles
//     */
//    public ProjectHandle[] getOpenProjects();
//    
//    /**
//     * returns true if given project is member project
//     */
//    public boolean isMemberProject(ProjectHandle m);
//    /**
//     * Add listener for listening for property changes related to Dashboard
//     * @param listener listener to be notified about property change
//     */
//    public void addPropertyChangeListener(PropertyChangeListener listener);
//
//    /**
//     * Remove listener from list of listeners notified about property changes
//     * @param listener listener to be removed
//     */
//    public void removePropertyChangeListener(PropertyChangeListener listener);

//    /**
//     * currently visible team
//     * @return
//     */
//    public S getServer();
//    
//    protected void setSelectedServer(ProjectHandle<P> project);
    
    public Action createLogoutAction();
    public Action createLoginAction();
    public LeafNode createMemberNode(MemberHandle user, TreeListNode parent);
    public TreeListNode createMessagingNode(ProjectNode pn, ProjectHandle<P> project);
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<P> project);
    public TreeListNode createMyProjectNode(ProjectHandle<P> p);   
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln);    

    public ProjectAccessor<S, P> getProjectAccessor();
    public MessagingAccessor<P> getMessagingAccessor();
    public MemberAccessor<P> getMemberAccessor();
    public SourceAccessor<P> getSourceAccessor();
    public QueryAccessor<P> getQueryAccessor();

    public S getServer(ProjectHandle<P> project);
    
}
