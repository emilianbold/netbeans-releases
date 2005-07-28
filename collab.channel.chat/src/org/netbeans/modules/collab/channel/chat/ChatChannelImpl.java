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

        // If this is the last active channel, save its settings
        if ((--activeChannelCount <= 0) && (component != null)) {
            Map settingsMap = component.getContentTypeSettings();
            HiddenChatChannelSettings.ContentTypeSettings[] settings = (HiddenChatChannelSettings.ContentTypeSettings[]) settingsMap.values()
                                                                                                                                    .toArray(
                    new HiddenChatChannelSettings.ContentTypeSettings[0]
                );
            HiddenChatChannelSettings.getDefault().setContentTypeSettings(settings);
        }
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
    public boolean handleMessage(CollabMessage message)
    throws CollabException {
        // Add the message to the transcript
        addToTranscript(message);
        getChangeSupport().firePropertyChange(PROP_MODIFIED, false, true);

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transcript methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected void addToTranscript(CollabMessage message)
    throws CollabException {
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
