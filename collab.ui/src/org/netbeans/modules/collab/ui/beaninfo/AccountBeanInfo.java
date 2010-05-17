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
package org.netbeans.modules.collab.ui.beaninfo;

import java.awt.Image;
import java.beans.*;
import java.util.*;

import org.openide.util.*;

import com.sun.collablet.Account;

import org.netbeans.modules.collab.core.Debug;

/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class AccountBeanInfo extends SimpleBeanInfo {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private BeanDescriptor descriptor;
    private PropertyDescriptor[] descriptors;
    private Image smallIcon;
    private Image largeIcon;

    /**
     *
     *
     */
    public AccountBeanInfo() {
        super();
    }

    /**
     *
     *
     */
    public BeanDescriptor getBeanDescriptor() {
        if (descriptor == null) {
            descriptor = new BeanDescriptor(Account.class);
            descriptor.setDisplayName(NbBundle.getMessage(AccountBeanInfo.class, "LBL_AccountBeanInfo_NewAccount")); // NOI18N
        }

        return descriptor;
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

                // displayName
                descriptor = new PropertyDescriptor(Account.PROP_DISPLAY_NAME, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_displayName_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_displayName_Description")
                ); // NOI18N
                descriptorList.add(descriptor);

                // userName
                descriptor = new PropertyDescriptor(Account.PROP_USER_NAME, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_userName_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_userName_Description")
                ); // NOI18N
                descriptorList.add(descriptor);

                // password
                //				descriptor=new PropertyDescriptor(
                //					Account.PROP_PASSWORD,
                //					Account.class);
                //				descriptor.setDisplayName(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_password_DisplayName")); // NOI18N
                //				descriptor.setShortDescription(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_password_Description")); // NOI18N
                //				descriptor.setPropertyEditorClass(PasswordEditor.class);
                //				descriptorList.add(descriptor);
                // firstName
                //				descriptor=new PropertyDescriptor(
                //					Account.PROP_FIRST_NAME, // NOI18N
                //					Account.class);
                //				descriptor.setDisplayName(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_firstName_DisplayName")); // NOI18N
                //				descriptor.setShortDescription(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_firstName_Description")); // NOI18N
                //				descriptorList.add(descriptor);
                // lastName
                //				descriptor=new PropertyDescriptor(
                //					Account.PROP_LAST_NAME, // NOI18N
                //					Account.class);
                //				descriptor.setDisplayName(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_lastName_DisplayName")); // NOI18N
                //				descriptor.setShortDescription(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_lastName_Description")); // NOI18N
                //				descriptorList.add(descriptor);
                // email
                //				descriptor=new PropertyDescriptor(
                //					Account.PROP_EMAIL, // NOI18N
                //					Account.class);
                //				descriptor.setDisplayName(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_email_DisplayName")); // NOI18N
                //				descriptor.setShortDescription(
                //					NbBundle.getMessage(AccountBeanInfo.class,
                //					"PROP_AccountBeanInfo_email_Description")); // NOI18N
                //				descriptorList.add(descriptor);
                // server
                descriptor = new PropertyDescriptor(Account.PROP_SERVER, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_server_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_server_Description")
                ); // NOI18N
                descriptorList.add(descriptor);

                // proxyType
                descriptor = new PropertyDescriptor(Account.PROP_PROXY_TYPE, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyType_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyType_Description")
                ); // NOI18N
                descriptor.setExpert(true);
                descriptor.setPropertyEditorClass(ProxyTypeEditor.class);
                descriptorList.add(descriptor);

                // proxyServer
                descriptor = new PropertyDescriptor(Account.PROP_PROXY_SERVER, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyServer_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyServer_Description")
                ); // NOI18N
                descriptor.setExpert(true);
                descriptorList.add(descriptor);

                // proxyUserName
                descriptor = new PropertyDescriptor(Account.PROP_PROXY_USER_NAME, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyUserName_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyUserName_Description")
                ); // NOI18N
                descriptor.setExpert(true);
                descriptorList.add(descriptor);

                // proxyPassword
                descriptor = new PropertyDescriptor(Account.PROP_PROXY_PASSWORD, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyPassword_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_proxyPassword_Description")
                ); // NOI18N
                descriptor.setExpert(true);
                descriptor.setPropertyEditorClass(PasswordEditor.class);
                descriptorList.add(descriptor);

                // valid
                descriptor = new PropertyDescriptor(Account.PROP_VALID, Account.class, "isValid", null);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_valid_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_valid_Description")
                ); // NOI18N
                descriptorList.add(descriptor);

                // auto-login
                descriptor = new PropertyDescriptor(Account.PROP_AUTO_LOGIN, Account.class);
                descriptor.setDisplayName(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_autoLogin_DisplayName")
                ); // NOI18N
                descriptor.setShortDescription(
                    NbBundle.getMessage(AccountBeanInfo.class, "PROP_AccountBeanInfo_autoLogin_Description")
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
                smallIcon = Utilities.loadImage("org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
            }

            return smallIcon;
        } else {
            if (largeIcon == null) {
                largeIcon = Utilities.loadImage("org/netbeans/modules/collab/core/resources/account_png.gif"); // NOI18N
            }

            return largeIcon;
        }
    }
}
