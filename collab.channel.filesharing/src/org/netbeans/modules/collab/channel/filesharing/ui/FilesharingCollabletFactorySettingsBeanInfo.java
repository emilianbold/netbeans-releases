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
package org.netbeans.modules.collab.channel.filesharing.ui;

import org.openide.util.*;

import java.awt.Image;

import java.beans.*;

import java.util.*;

import org.netbeans.modules.collab.core.Debug;


//import com.sun.tools.ide.collab.channel.chat.*;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class FilesharingCollabletFactorySettingsBeanInfo extends SimpleBeanInfo {
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
    public FilesharingCollabletFactorySettingsBeanInfo() {
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

                //				// Test
                //				descriptor=new PropertyDescriptor(
                //					"test", // NOI18N
                //					FilesharingCollabletFactorySettings.class,
                //					"getTest","setTest"); // NOI18N
                //				descriptor.setDisplayName("Test"); // NOI18N
                //				descriptor.setShortDescription("Test"); // NOI18N
                //				descriptorList.add(descriptor);
                // Filesharing timer setting
                descriptor = new PropertyDescriptor(
                        FilesharingCollabletFactorySettings.PROP_LOCK_TIMEOUT_INTERVAL, // NOI18N
                        FilesharingCollabletFactorySettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        FilesharingCollabletFactorySettingsBeanInfo.class,
                        "PROP_FilesharingCollabletFactorySettingsBeanInfo_" + // NOI18N
                        "LockTimeoutInterval_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        FilesharingCollabletFactorySettingsBeanInfo.class,
                        "PROP_FilesharingCollabletFactorySettingsBeanInfo_" + // NOI18N
                        "LockTimeoutInterval_Description"
                    )
                ); // NOI18N
                descriptorList.add(descriptor);

                // Maximum number of shared file folders setting
                descriptor = new PropertyDescriptor(
                        FilesharingCollabletFactorySettings.PROP_MAX_SHARED_FILE_FOLDERS, // NOI18N
                        FilesharingCollabletFactorySettings.class
                    );
                descriptor.setDisplayName(
                    NbBundle.getMessage(
                        FilesharingCollabletFactorySettingsBeanInfo.class,
                        "PROP_FilesharingCollabletFactorySettingsBeanInfo_" + // NOI18N
                        "MaxSharedFileFolders_DisplayName"
                    )
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(
                        FilesharingCollabletFactorySettingsBeanInfo.class,
                        "PROP_FilesharingCollabletFactorySettingsBeanInfo_" + // NOI18N
                        "MaxSharedFileFolders_Description"
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
                        "org/netbeans/modules/collab/channel/filesharing/" + // NOI18N
                        "resources/filesharing_png.gif"
                    ); // NOI18N
            }

            return smallIcon;
        } else {
            if (largeIcon == null) {
                largeIcon = Utilities.loadImage(
                        "org/netbeans/modules/collab/channel/filesharing/" + // NOI18N
                        "resources/filesharing_png.gif"
                    ); // NOI18N
            }

            return largeIcon;
        }
    }
}
