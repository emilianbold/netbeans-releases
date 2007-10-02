/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils.helper;

import org.netbeans.installer.utils.ResourceUtils;

public enum DetailedStatus {
    INSTALLED_SUCCESSFULLY,
    INSTALLED_WITH_WARNINGS,
    FAILED_TO_INSTALL,
    UNINSTALLED_SUCCESSFULLY,
    UNINSTALLED_WITH_WARNINGS,
    FAILED_TO_UNINSTALL;
    
    public String toString() {
        switch (this) {
            case INSTALLED_SUCCESSFULLY:
                return INSTALLED_SUCCESSFULLY_STRING;
            case INSTALLED_WITH_WARNINGS:
                return INSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_INSTALL:
                return FAILED_TO_INSTALL_STRING;
            case UNINSTALLED_SUCCESSFULLY:
                return UNINSTALLED_SUCCESSFULLY_STRING;
            case UNINSTALLED_WITH_WARNINGS:
                return UNINSTALLED_WITH_WARNINGS_STRING;
            case FAILED_TO_UNINSTALL:
                return FAILED_TO_UNINSTALL_STRING;
            default:
                return null;
        }
    }
    private static final String INSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.succes");//NOI18N
private static final String INSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.warning");//NOI18N
private static final String FAILED_TO_INSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.install.error");//NOI18N

private static final String UNINSTALLED_SUCCESSFULLY_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.success");//NOI18N
private static final String UNINSTALLED_WITH_WARNINGS_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.warning");//NOI18N
private static final String FAILED_TO_UNINSTALL_STRING = 
            ResourceUtils.getString(DetailedStatus.class,
            "DetailedStatus.uninstall.error");//NOI18N
}
