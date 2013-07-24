/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.server.ui.spi;

import java.beans.PropertyChangeListener;

/**
 * Abstraction for logged-in user.
 *
 * @author S. Aubrecht
 */
public abstract class LoginHandle {

    /**
     * The name of property which is fired when the list of user's member projects
     * has changed (projects added, removed, renamed). The property value is undefined.
     */
    public static final String PROP_MEMBER_PROJECT_LIST = "memberProjects"; // NOI18N

    /**
     *
     * @return Login name
     */
    public abstract String getUserName();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
