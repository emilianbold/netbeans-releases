/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.VersioningManager;

import java.io.File;

/**
 * Collection of utility methods for Versioning systems implementors. 
 * 
 * @author Maros Sandor
 */
public class VersioningSupport {
    
    /**
     * Queries the Versioning infrastructure for file ownership.
     * 
     * @param file a file to examine
     * @return VersioningSystem a system that owns (manages) the file or null if the file is not versioned
     */
    public static VersioningSystem getOwner(File file) {
        return VersioningManager.getInstance().getOwner(file);
    }
}
