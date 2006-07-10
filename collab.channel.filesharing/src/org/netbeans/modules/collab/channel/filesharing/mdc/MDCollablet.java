/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
