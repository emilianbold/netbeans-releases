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
package org.netbeans.modules.collab.ui.options;

import java.awt.Image;
import java.beans.*;
import java.util.*;

import org.openide.util.*;

import org.netbeans.modules.collab.core.Debug;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class NotificationSettingsBeanInfo extends SimpleBeanInfo {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private PropertyDescriptor[] descriptors;
    private Image smallIcon;
    private Image largeIcon;

    /**
     *
     *
     */
    public NotificationSettingsBeanInfo() {
        super();
    }

    /**
     *
     *
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            List descriptorList = new LinkedList();

            try {
                PropertyDescriptor descriptor = null;

                /*
                descriptor=new PropertyDescriptor(
                        NotificationSettings.PROP_TEST, // NOI18N
                        NotificationSettings.class);
                descriptor.setDisplayName(
                        NbBundle.getMessage(NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_Test_DisplayName")); // NOI18N
                descriptor.setShortDescription(
                        NbBundle.getMessage(NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_Test_Description")); // NOI18N
                descriptorList.add(descriptor);
                */
                // Play audio notifications
                descriptor = new PropertyDescriptor(
                        NotificationSettings.PROP_PLAY_AUDIO_NOTIFICATIONS, NotificationSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "PlayAudioNotifications_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "PlayAudioNotifications_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Show conversation notification bar
                descriptor = new PropertyDescriptor(
                        NotificationSettings.PROP_SHOW_CONVERSATION_NOTIFICATION_BAR, NotificationSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "ShowConversationNotificationBar_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "ShowConversationNotificationBar_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Show animate notification bar
                descriptor = new PropertyDescriptor(
                        NotificationSettings.PROP_ANIMATE_CONVERSATION_NOTIFICATION_BAR, NotificationSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "AnimateConversationNotificationBar_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "AnimateConversationNotificationBar_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Show presence notifications
                descriptor = new PropertyDescriptor(
                        NotificationSettings.PROP_SHOW_PRESENCE_NOTIFICATIONS, NotificationSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "ShowPresenceNotifications_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        NotificationSettingsBeanInfo.class,
                        "PROP_NotificationSettingsBeanInfo_" + // NOI18N
                        "ShowPresenceNotifications_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);
            } catch (IntrospectionException e) {
                Debug.debugNotify(e);
            }

            descriptors = (PropertyDescriptor[]) descriptorList.toArray(new PropertyDescriptor[descriptorList.size()]);
        }

        return descriptors;
    }

    /**
     *
     *
     */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (smallIcon == null) {
                smallIcon = Utilities.loadImage(
                        "org/netbeans/modules/collab/ui/resources/" + // NOI18N
                        "conversation_notify_png.gif"
                    ); // NOI18N
            }

            return smallIcon;
        } else {
            if (largeIcon == null) {
                largeIcon = Utilities.loadImage(
                        "org/netbeans/modules/collab/ui/resources/" + // NOI18N
                        "conversation_notify_png.gif"
                    ); // NOI18N
            }

            return largeIcon;
        }
    }
}
