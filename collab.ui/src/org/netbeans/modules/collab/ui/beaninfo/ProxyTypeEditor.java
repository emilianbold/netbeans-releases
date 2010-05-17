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
