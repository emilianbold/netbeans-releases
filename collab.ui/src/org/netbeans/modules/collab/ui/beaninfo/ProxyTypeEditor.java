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
