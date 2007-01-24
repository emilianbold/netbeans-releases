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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper;

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
                return "Installed Successfully";
            case INSTALLED_WITH_WARNINGS:
                return "Installed With Warnings";
            case FAILED_TO_INSTALL:
                return "Failed to Install";
            case UNINSTALLED_SUCCESSFULLY:
                return "Uninstalled Successfully";
            case UNINSTALLED_WITH_WARNINGS:
                return "Uninstalled With Warnings";
            case FAILED_TO_UNINSTALL:
                return "Failed to Uninstall";
            default:
                return null;
        }
    }
}