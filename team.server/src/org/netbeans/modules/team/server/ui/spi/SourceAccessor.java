/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.server.ui.spi;

import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.team.ide.spi.IDEProject;

/**
 * Main access point to Teams's source versioning API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinitely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 * 
 * @author S. Aubrecht
 */
public abstract class SourceAccessor<P> {

    public abstract Class<P> type();

    /**
     * Determines whether this project has a source service or not
     * 
     * @param project
     * @return <code>true</code> in case the given project has a source service otherwise <code>false</code>
     */    
    public abstract boolean hasSources(ProjectHandle<P> project);
    
    /**
     * 
     * @param src
     * @return Action that opens the working directory of given SourceHandle
     *         in a file browser UI (like Favorites window), or null if such
     *         capability does not exist.
     */
    public abstract Action getOpenFavoritesAction(SourceHandle src);

    /**
     * Retrieve the list of source repositories available for given project.
     * @param project
     * @return
     */
    public abstract List<SourceHandle> getSources( ProjectHandle<P> project );

    /**
     *
     * @param project
     * @return Action to invoke when user click 'get' button in the Sources list.
     */
    public abstract Action getOpenSourcesAction( SourceHandle project );

    /**
     *
     * @param source
     * @return Action to invoke when user pressed Enter key on given source line.
     */
    //maybe same as 'get'?
    public abstract Action getDefaultAction( SourceHandle source );

    /**
     * Get default action for project. Typically opens it.
     * @param prj
     * @return default action on Project
     */
    public abstract Action getDefaultAction(IDEProject prj);

    /**
     * Default action for "other" link
     * Should open project chooser
     * @param src
     * @return
     */
    public abstract Action getOpenOtherAction(SourceHandle src);
}
