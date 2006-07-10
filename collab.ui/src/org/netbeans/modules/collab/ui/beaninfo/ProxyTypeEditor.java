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
package org.netbeans.modules.collab.ui.beaninfo;

import java.beans.*;

import org.openide.util.NbBundle;

import com.sun.collablet.Account;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class ProxyTypeEditor extends PropertyEditorSupport {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final String TAG_NONE = NbBundle.getMessage(
            AccountBeanInfo.class, "TAG_AccountBeanInfo_ProxyTypeEditor_NONE"
        );
    public static final String TAG_HTTPS = NbBundle.getMessage(
            AccountBeanInfo.class, "TAG_AccountBeanInfo_ProxyTypeEditor_HTTPS"
        );
    public static final String TAG_SOCKS_5 = NbBundle.getMessage(
            AccountBeanInfo.class, "TAG_AccountBeanInfo_ProxyTypeEditor_SOCKS_5"
        );
    public static final String[] TAGS = new String[] { TAG_NONE, TAG_HTTPS, TAG_SOCKS_5 };

    /**
     *
     *
     */
    public ProxyTypeEditor() {
        super();
    }

    /**
     *
     *
     */
    public String[] getTags() {
        return TAGS;
    }

    /**
     *
     *
     */
    public String getAsText() {
        // TODO - should this be the default? REQUEST
        if (getValue() == null) {
            return TAG_NONE;
        }

        switch (((Integer) getValue()).intValue()) {
        case Account.PROXY_NONE:
            return TAG_NONE;

        case Account.PROXY_HTTPS:
            return TAG_HTTPS;

        case Account.PROXY_SOCKS_5:
            return TAG_SOCKS_5;
        }

        throw new IllegalArgumentException();
    }

    /**
     *
     *
     */
    public void setAsText(String value) {
        if (value.equals(TAG_NONE)) {
            setValue(new Integer(Account.PROXY_NONE));
        } else if (value.equals(TAG_HTTPS)) {
            setValue(new Integer(Account.PROXY_HTTPS));
        } else if (value.equals(TAG_SOCKS_5)) {
            setValue(new Integer(Account.PROXY_SOCKS_5));
        } else {
            throw new IllegalArgumentException();
        }
    }
}
