/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import javax.swing.Action;

/**
 * Main access point to Team's Messaging API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class MessagingAccessor<S extends TeamServer, P> {

    /**
     * Retrieve messaging info associated with given project.
     * @param project
     * @return
     */
    public abstract MessagingHandle getMessaging( ProjectHandle<S, P> project );

    /**
     * @param project
     * @return Show messages for given project
     */
    public abstract Action getOpenMessagesAction( ProjectHandle<S, P> project );

    /**
     * Action, which creates chat room on server
     * @param project
     * @return
     */
    public abstract Action getCreateChatAction(ProjectHandle<S, P> project);

    /**
     * Action, which retries connecting to server
     * @param project
     * @return
     */
    public abstract Action getReconnectAction(ProjectHandle<S, P> project);

}
