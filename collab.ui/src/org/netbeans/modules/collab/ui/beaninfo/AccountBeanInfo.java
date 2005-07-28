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
package org.netbeans.modules.collab.ui.beaninfo;

import com.sun.collablet.Account;

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
