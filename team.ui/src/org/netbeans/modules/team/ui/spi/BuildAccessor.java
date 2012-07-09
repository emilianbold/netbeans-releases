/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.util.List;
import javax.swing.Action;

/**
 * Main access point to Teams's Build API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class BuildAccessor<P> {

    public abstract Class<P> type();
    
    /**
     * Checks whether build-related UI should even be shown for this project.
     */
    public abstract boolean isEnabled(ProjectHandle<P> project);
    
    /**
     * Retrieve the list of builds in given project.
     * @return a list of builds (never null)
     */
    public abstract List<BuildHandle> getBuilds( ProjectHandle<P> project );

    /**
     * @return Action to invoke when user clicks 'New Build...' button, or null to disable
     */
    public abstract Action getNewBuildAction( ProjectHandle<P> project );

}
