/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
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

    /**
     * Obtains the default accessor for the system from lookup.
     * @return the default instance, or a dummy fallback (never null)
     */
    public static BuildAccessor getDefault() {
        BuildAccessor dflt = Lookup.getDefault().lookup(BuildAccessor.class);
        if (dflt == null) {
            dflt = new BuildAccessor() {
                public boolean isEnabled(ProjectHandle project) {
                    return false;
                }
                public List<BuildHandle> getBuilds(ProjectHandle project) {
                    return Collections.emptyList();
                }
                public Action getNewBuildAction(ProjectHandle project) {
                    return null;
                }
            };
        }
        return dflt;
    }

    /**
     * Checks whether build-related UI should even be shown for this project.
     */
    public abstract boolean isEnabled(ProjectHandle project);
    
    /**
     * Retrieve the list of builds in given project.
     * @return a list of builds (never null)
     */
    public abstract List<BuildHandle> getBuilds( ProjectHandle project );

    /**
     * @return Action to invoke when user clicks 'New Build...' button, or null to disable
     */
    public abstract Action getNewBuildAction( ProjectHandle project );

}
