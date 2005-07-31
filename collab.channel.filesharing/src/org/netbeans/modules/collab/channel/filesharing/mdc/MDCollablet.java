/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.mdc;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;
import com.sun.collablet.Collablet;
import com.sun.collablet.Conversation;

import org.openide.util.*;

import java.awt.Image;

import java.beans.*;

import javax.swing.*;


/**
 * Filesharing Channel
 *
 * @author Todd Fast, todd.fast@sun.com
 * @version 1.0
 */
public class MDCollablet extends Object implements Collablet {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private Icon icon;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     * @param conversation
     */
    public MDCollablet(Conversation conversation) {
        super();
        this.conversation = conversation;
    }

    /**
     *
     * @return channel display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(MDCollabletFactory.class, "LBL_MDCollablet_DisplayName"); // NOI18N
    }

    /**
     *
     * @return channel icon
     */
    public Icon getIcon() {
        if (icon == null) {
            Image image = (Image) UIManager.get("Nb.Explorer.Folder.icon");

            if (image != null) {
                icon = new ImageIcon(image);
            }
        }

        return icon;
    }

    /**
     *
     * @return conversation
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     *
     *
     */
    public void close() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return true if message is accepted
     * @param message
     */
    public boolean acceptMessage(CollabMessage message) {
        //return true;
        // TODO: How do we discriminate chat messages from any other message?
        // TODO: Temporary impl
        boolean result = "mdc".equals(message.getHeader("x-channel"));

        return result;
    }

    /**
     *
     * @param message
     * @throws CollabException
     * @return status
     */
    public boolean handleMessage(CollabMessage message)
    throws CollabException {
        return true;
    }

    /**
     *
     * @return Collablet
     */
    public Collablet getCollablet() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
        // Do nothing
    }

    /**
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}
