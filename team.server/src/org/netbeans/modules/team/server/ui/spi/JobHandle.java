/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.server.ui.spi;

import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Abstraction of a single build process (a line in Builds section)
 *
 * @author S. Aubrecht
 */
public abstract class JobHandle {

    /**
     * The name of property which is fired when the status of this build has changed.
     * The property value is the new build status.
     */
    public static final String PROP_STATUS = "status"; // NOI18N

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return Build status
     */
    public abstract BuildHandle.Status getStatus();

    /**
     *
     * @return Action to invoke when user pressed Enter key on given build line.
     */
    public abstract Action getDefaultAction();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );

    public abstract @NonNull List<BuildHandle> getBuilds();

    public abstract @CheckForNull BuildHandle getBuild(String buildId);

    /**
     * @return True if the job is watched (salient), false if it was marked as
     * unimportant.
     */
    public abstract boolean isWatched();
}
