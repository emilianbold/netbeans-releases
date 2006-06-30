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
package org.netbeans.modules.java.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Delegates {@link AccessibilityQueryImplementation} to the project which
 * owns the affected source folder.
 * @author Jesse Glick
 */
public class ProjectAccessibilityQuery implements AccessibilityQueryImplementation {

    /** Default constructor for lookup. */
    public ProjectAccessibilityQuery() {}

    public Boolean isPubliclyAccessible(FileObject pkg) {
        Project project = FileOwnerQuery.getOwner(pkg);
        if (project != null) {
            AccessibilityQueryImplementation aqi =
                (AccessibilityQueryImplementation)project.getLookup().lookup(
                    AccessibilityQueryImplementation.class);
            if (aqi != null) {
                return aqi.isPubliclyAccessible(pkg);
            }
        }
        return null;
    }
    
}
