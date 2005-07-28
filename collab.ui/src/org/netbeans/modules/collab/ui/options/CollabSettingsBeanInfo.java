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

import org.openide.*;
import org.openide.options.*;
import org.openide.util.*;

import java.awt.Image;

import java.beans.*;

import java.io.*;

import java.util.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class CollabSettingsBeanInfo extends SimpleBeanInfo {
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
    public CollabSettingsBeanInfo() {
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
                        CollabSettings.PROP_TEST, // NOI18N
                        CollabSettings.class);
                descriptor.setDisplayName(
                        NbBundle.getMessage(CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_Test_DisplayName")); // NOI18N
                descriptor.setShortDescription(
                        NbBundle.getMessage(CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_Test_Description")); // NOI18N
                descriptorList.add(descriptor);
                */
                // Idle timout setting
                descriptor = new PropertyDescriptor(CollabSettings.PROP_IDLE_TIMEOUT, // NOI18N
                        CollabSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "IdleTimeout_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "IdleTimeout_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Presence subscription approval
                descriptor = new PropertyDescriptor(CollabSettings.PROP_AUTO_APPROVE, // NOI18N
                        CollabSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoApprove_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoApprove_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Auto-login
                descriptor = new PropertyDescriptor(CollabSettings.PROP_AUTO_LOGIN, // NOI18N
                        CollabSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class, "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoLogin_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class, "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoLogin_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Auto-accept conversation
                descriptor = new PropertyDescriptor(
                        CollabSettings.PROP_AUTO_ACCEPT_CONVERSATION, // NOI18N
                        CollabSettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoAcceptConversation_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        CollabSettingsBeanInfo.class,
                        "PROP_CollabSettingsBeanInfo_" + // NOI18N
                        "AutoAcceptConversation_Description"
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
                        "org/netbeans/modules/collab/core/resources/" + // NOI18N
                        "collab_png.gif"
                    ); // NOI18N
            }

            return smallIcon;
        } else {
            if (largeIcon == null) {
                largeIcon = Utilities.loadImage(
                        "org/netbeans/modules/collab/core/resources/" + // NOI18N
                        "collab_png.gif"
                    ); // NOI18N
            }

            return largeIcon;
        }
    }
}
