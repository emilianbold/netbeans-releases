/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.util.List;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 * Main access point to Kenai's source versioning API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 * 
 * @author S. Aubrecht
 */
public abstract class SourceAccessor {

    public static SourceAccessor getDefault() {
        return Lookup.getDefault().lookup(SourceAccessor.class);
    }

    public abstract Action getOpenFavorites(SourceHandle src);

    /**
     * Retrieve the list of source repositories available for given project.
     * @param project
     * @return
     */
    public abstract List<SourceHandle> getSources( ProjectHandle project );

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
    public abstract Action getDefaultAction( NbProjectHandle prj );

    /**
     * Default action for "other" link
     * Should open project chooser
     * @param src
     * @return
     */
    public abstract Action getOpenOtherAction(SourceHandle src);
}
