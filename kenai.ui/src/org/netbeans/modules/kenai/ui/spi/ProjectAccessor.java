/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Main access point to Kenai's Project API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class ProjectAccessor {

    public static ProjectAccessor getDefault() {
        return Lookup.getDefault().lookup( ProjectAccessor.class );
    }

    /**
     * Retrieve the list of projects the given user is member of.
     * @param login
     * @param forceRefresh force reload from server
     * @return list of member projects or null, if member projects
     * are not accessible
     */
    public abstract List<ProjectHandle> getMemberProjects( LoginHandle login, boolean forceRefresh );

    /**
     * Load details for given project.
     * @param projectId Project identification
     * @param forceRefresh force reload from server
     * @return projectHandle or null, if project handle not accessible
     */
    public abstract ProjectHandle getNonMemberProject(String projectId, boolean forceRefresh);

    /**
     * @return Adds a Kenai project into given Dashboard window.
     */
    public abstract Action getOpenNonMemberProjectAction();

    /**
     * @param project
     * @return Show details of given project
     */
    public abstract Action getDetailsAction( ProjectHandle project );
    /**
     *
     * @param project 
     * @return Action to invokie when user pressed Enter key on the header line
     * for given project.
     */
    //maybe same as 'details'?
    public abstract Action getDefaultAction( ProjectHandle project );
    /**
     *
     * @param project
     * @return Action for project's popup menu, null entries represent menu separator.
     */
    public abstract Action[] getPopupActions( ProjectHandle project );

    public abstract Action getOpenWikiAction( ProjectHandle project );

    public abstract Action getOpenDownloadsAction( ProjectHandle project );

    public abstract Action getBookmarkAction(ProjectHandle project);
}
