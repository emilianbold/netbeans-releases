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
package org.netbeans.modules.collab.channel.chat;

import com.sun.collablet.CollabException;
import com.sun.collablet.CollabMessage;
import com.sun.collablet.Conversation;
import com.sun.collablet.chat.ChatCollablet;

import org.openide.util.*;

import java.beans.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ChatChannelImpl extends Object implements ChatCollablet {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String PROP_TRANSCRIPT = "transcript";
    public static final String DISPLAY_CONTENT_TYPE_HEADER = "x-display-content-type";
    private static int activeChannelCount;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private ChatComponent component;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    public ChatChannelImpl(Conversation conversation) {
        super();
        this.conversation = conversation;
        activeChannelCount++;
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(ChatCollablet.class, "LBL_ChatChannel_DisplayName");
    }

    /**
     *
     *
     */
    public Icon getIcon() {
        return null;
    }

    /**
     *
     *
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     *
     *
     */
    public void close() {
        // Do nothing
        activeChannelCount--;
    }

    /**
     *
     *
     */
    public synchronized JComponent getComponent() throws CollabException {
        if (component == null) {
            component = new ChatComponent(this);
        }

        return component;
    }

    /**
     *
     *
     */
    public synchronized JComponent getFocusableComponent() {
        if (component == null) {
            component = new ChatComponent(this);
        }

        return component.getInputPane();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public boolean acceptMessage(CollabMessage message) {
        // Accept any message with no channel header for interoperability
        // with IM client  If there is a channel header, only accept if it
        // is ours.
        boolean result = true;

        if (message.getHeader("x-channel") != null) {
            result = "chat".equals(message.getHeader("x-channel"));
        }

        return result;
    }

    /**
     *
     *
     */
    public boolean handleMessage(final CollabMessage message) {
        // Add the message to the transcript, but in AWT!
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                addToTranscript(message);
                getChangeSupport().firePropertyChange(PROP_MODIFIED, false, true);
            }
        });

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transcript methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected void addToTranscript(CollabMessage message) {
        getChangeSupport().firePropertyChange(PROP_TRANSCRIPT, null, message);
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
     *
     */
    protected PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    /**
         *
         *
         */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
         *
         *
         */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of active channels; used to determine if there is
     * state that needs to be stored
     *
     */
    protected static int getActiveChannelCount() {
        return activeChannelCount;
    }
}
