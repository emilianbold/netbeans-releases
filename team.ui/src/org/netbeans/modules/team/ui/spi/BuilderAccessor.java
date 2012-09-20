/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.team.ui.spi.BuildHandle.Status;

/**
 * Main access point to Teams's Build API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class BuilderAccessor<P> {

    public abstract Class<P> type();
    
    /**
     * Checks whether build-related UI should even be shown for this project.
     */
    public abstract boolean isEnabled( ProjectHandle<P> project);
    
    /**
     * Retrieve the list of builds in given project.
     * @return a list of builds (never null)
     */
    public abstract List<JobHandle> getJobs( ProjectHandle<P> project );
    
    /**
     * Retrieve the handle to a job identified by its name
     * @return a job handle or <code>null</code> if no such job found
     */
    public abstract JobHandle getJob ( ProjectHandle<P> project, String jobName );

    /**
     * @return Action to invoke when user clicks 'New Build...' button, or null to disable
     */
    public abstract Action getNewBuildAction( ProjectHandle<P> project );

    /**
     * Determines the most interresting build handle from a collection of
     * handles. The default implementation returns build with the worst status.
     * If all builds are successful or unknown, null is returned.
     *
     * Note that default action of the returned build handle (see
     * {@link JobHandle#getDefaultAction()}) should have an icon.
     *
     * @return Build handle that deserves user's attention the most, or null if
     * there is no interresting build.
     */
    public JobHandle chooseMostInterrestingJob(
            Collection<? extends JobHandle> builds) {
        JobHandle worst = null;
        for (JobHandle bh : builds) {
            Status status = bh.getStatus();
            if ((status == Status.FAILED || status == Status.UNSTABLE)
                    && (worst == null
                    || (worst.getStatus() == Status.UNSTABLE
                    && status == Status.FAILED))) {
                worst = bh;
            }
        }
        return worst;
    }
}
