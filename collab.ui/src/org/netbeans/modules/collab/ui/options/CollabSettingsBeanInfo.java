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
