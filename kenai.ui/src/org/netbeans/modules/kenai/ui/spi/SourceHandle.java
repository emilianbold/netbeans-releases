/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.io.File;
import java.util.List;

/**
 * Abstraction for a single source repository (a line in 'Sources' section).
 *
 * @author S. Aubrecht
 */
public abstract class SourceHandle {

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return True if 'get' link is available, false to render the repository as disabled.
     */
    public abstract boolean isSupported();

    /**
     * Returns SCM feature's name, deduced from the Kenai server response.<br><br>
     * Usage should be with KenaiService.Names.*, such as:<br><br>
     * <pre>
     * // source is an instance of SourceHandle...
     * String featureName = source.getScmFeatureName();
     * if (featureName.equals(KenaiService.Names.SUBVERSION)) { ... }
     * </pre>
     * @return the name of the issue tracking fature
     */
    public abstract String getScmFeatureName();

    /**
     * getter for max 5 recent projects
     * @return
     */
    public abstract List<NbProjectHandle> getRecentProjects();

    /**
     * getter for last checked out working directory
     * can return null
     * @return
     */
    public abstract File getWorkingDirectory();
}
