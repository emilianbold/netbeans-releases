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

import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

/**
 * Finds project owning given file, SourceLevelQueryImplementation impl in its
 * lookup and delegates question to it.
 * @author David Konecny
 */
public class ProjectSourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    /** Default constructor for lookup. */
    public ProjectSourceLevelQueryImpl() {}

    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {
        Project project = FileOwnerQuery.getOwner(javaFile);
        if (project != null) {
            SourceLevelQueryImplementation slq =
                    (SourceLevelQueryImplementation)project.getLookup().lookup(
                            SourceLevelQueryImplementation.class);
            if (slq != null) {
                return slq.getSourceLevel(javaFile);
            }
        }
        return null;
    }
    
}
