/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
