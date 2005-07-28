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
package org.netbeans.modules.collab.ui.options;

import org.openide.options.*;
import org.openide.util.*;

import java.io.*;

import org.netbeans.modules.collab.*;


/**
 * This class is for storage of user-visible properties only.  All properties
 * must have a descriptor defined in the BeanInfo if they are to be persisted.
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class NotificationSettings extends SystemOption {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final String PROP_TEST = "test"; // NOI18N
    public static final String PROP_PLAY_AUDIO_NOTIFICATIONS = "playAudioNotifications"; // NOI18N
    public static final String PROP_SHOW_PRESENCE_NOTIFICATIONS = "showPresenceNotifications"; // NOI18N
    public static final String PROP_SHOW_CONVERSATION_NOTIFICATION_BAR = "showConversationNotificationBar";
    public static final String PROP_ANIMATE_CONVERSATION_NOTIFICATION_BAR = "animateConversationNotificationBar";

    /**
     *
     *
     */
    public NotificationSettings() {
        super();
    }

    /**
     *
     *
     */
    protected void initialize() {
        super.initialize();

        // If you have more complex default values which might require
        // other parts of the module to already be installed, do not
        // put them here; e.g. make the getter return them as a
        // default if getProperty returns null. (The class might be
        // initialized partway through module installation.)
        setTest(Boolean.FALSE);
        setPlayAudioNotifications(Boolean.TRUE);
        setShowConversationNotificationBar(Boolean.FALSE);
        setAnimateConversationNotificationBar(Boolean.TRUE);
        setShowPresenceNotifications(Boolean.TRUE);
    }

    /**
     *
     *
     */
    protected boolean clearSharedData() {
        super.clearSharedData();

        return false;
    }

    /**
     *
     *
     */
    public String displayName() {
        return NbBundle.getMessage(NotificationSettings.class, "LBL_NotificationSettings_DisplayName"); // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        // If you provide context help then use:
        // return new HelpCtx(NotificationSettings.class);
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static NotificationSettings getDefault() {
        NotificationSettings result = (NotificationSettings) findObject(NotificationSettings.class, true);
        assert result != null : "Default NotificationSettings object was null";

        return result;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static NotificationSettings getDefault(boolean value) {
        return (NotificationSettings) findObject(NotificationSettings.class, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Option property methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public Boolean getTest() {
        return (Boolean) getProperty(PROP_TEST);
    }

    /**
     *
     *
     */
    public void setTest(Boolean value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_TEST, value, true);
    }

    /**
     *
     *
     */
    public Boolean getPlayAudioNotifications() {
        return (Boolean) getProperty(PROP_PLAY_AUDIO_NOTIFICATIONS);
    }

    /**
     *
     *
     */
    public void setPlayAudioNotifications(Boolean value) {
        putProperty(PROP_PLAY_AUDIO_NOTIFICATIONS, value, true);
    }

    /**
     *
     *
     */
    public Boolean getShowConversationNotificationBar() {
        return (Boolean) getProperty(PROP_SHOW_CONVERSATION_NOTIFICATION_BAR);
    }

    /**
     *
     *
     */
    public void setShowConversationNotificationBar(Boolean value) {
        putProperty(PROP_SHOW_CONVERSATION_NOTIFICATION_BAR, value, true);
    }

    /**
     *
     *
     */
    public Boolean getAnimateConversationNotificationBar() {
        return (Boolean) getProperty(PROP_ANIMATE_CONVERSATION_NOTIFICATION_BAR);
    }

    /**
     *
     *
     */
    public void setAnimateConversationNotificationBar(Boolean value) {
        putProperty(PROP_ANIMATE_CONVERSATION_NOTIFICATION_BAR, value, true);
    }

    /**
     *
     *
     */
    public Boolean getShowPresenceNotifications() {
        return (Boolean) getProperty(PROP_SHOW_PRESENCE_NOTIFICATIONS);
    }

    /**
     *
     *
     */
    public void setShowPresenceNotifications(Boolean value) {
        putProperty(PROP_SHOW_PRESENCE_NOTIFICATIONS, value, true);
    }
}
