/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.collab.ui.options;

import org.openide.options.SystemOption;
import org.openide.util.*;

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
