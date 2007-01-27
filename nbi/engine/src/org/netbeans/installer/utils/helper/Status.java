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

import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;

public enum Status {
    NOT_INSTALLED("not-installed"),
    TO_BE_INSTALLED("to-be-installed"),
    INSTALLED("installed"),
    TO_BE_UNINSTALLED("to-be-uninstalled");
    
    private String name;
    
    private Status(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        switch (this) {
            case NOT_INSTALLED:
                return "Not installed";
            case TO_BE_INSTALLED:
                return "To be installed";
            case INSTALLED:
                return "Installed";
            case TO_BE_UNINSTALLED:
                return "To be uninstalled";
        }
        
        return null;
    }
    
    public String toString() {
        return name;
    }
}

