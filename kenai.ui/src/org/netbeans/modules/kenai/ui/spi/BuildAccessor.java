/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.event.ActionListener;
import java.util.List;
import org.openide.util.Lookup;

/**
 * Main access point to Kenai's Build API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class BuildAccessor {

    public static BuildAccessor getDefault() {
        return Lookup.getDefault().lookup(BuildAccessor.class);
    }
    
    /**
     * Retrieve the list of builds in given project.
     * @param project
     * @return
     */
    public abstract List<BuildHandle> getBuilds( ProjectHandle project );

    /**
     *
     * @param project
     * @return Action to invoke when user clicks 'New Build...' button.
     */
    public abstract ActionListener getNewBuildAction( ProjectHandle project );

}
