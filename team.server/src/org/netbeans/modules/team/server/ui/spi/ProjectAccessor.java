/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.server.ui.spi;

import java.util.List;
import javax.swing.Action;

/**
 * Main access point to a Team Project.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class ProjectAccessor<P> {

    /**
     * Retrieve the list of projects the given user is member of.
     * @param server
     * @param login
     * @param forceRefresh force reload from server
     * @return list of member projects or null, if member projects
     * are not accessible
     */
    public abstract List<ProjectHandle<P>> getMemberProjects(TeamServer server, LoginHandle login, boolean forceRefresh );

    /**
     * Load details for given project.
     * @param server
     * @param projectId Project identification
     * @param forceRefresh force reload from server
     * @return projectHandle or null, if project handle not accessible
     */
    public abstract ProjectHandle<P> getNonMemberProject(TeamServer server, String projectId, boolean forceRefresh);

    /**
     * @param project
     * @return Show details of given project
     */
    public abstract Action getDetailsAction( ProjectHandle<P> project );
    /**
     *
     * @param project 
     * @return Action to invokie when user pressed Enter key on the header line
     * for given project.
     */
    //maybe same as 'details'?
    public abstract Action getDefaultAction( ProjectHandle<P> project, boolean opened );
    /**
     *
     * @param project
     * @return Action for project's popup menu, null entries represent menu separator.
     */
    public abstract Action[] getPopupActions( ProjectHandle<P> project, boolean opened );

    public abstract Action getOpenWikiAction( ProjectHandle<P> project );

    public abstract Action getOpenDownloadsAction( ProjectHandle<P> project );

    public abstract void bookmark(ProjectHandle<P> project);
}
