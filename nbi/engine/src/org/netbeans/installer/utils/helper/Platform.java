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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.utils.exceptions.UnrecognizedObjectException;

public enum Platform {
    WINDOWS("windows", "Windows"),
    LINUX("linux", "Linux"),
    SOLARIS_X86("solaris-x86", "Solaris X86"),
    SOLARIS_SPARC("solaris-sparc", "Solaris Sparc"),
    MACOS_X_PPC("macosx-ppc", "MacOS X (PPC)"),
    MACOS_X_X86("macosx-x86", "MacOS X (Intel)");
    
    private String name;
    private String displayName;
    
    private Platform(final String name, final String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
    public boolean equals(Platform platform) {
        return name.equals(platform.name);
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String toString() {
        return name;
    }
}

