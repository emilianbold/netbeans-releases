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
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Finds sources corresponding to binaries.
 * Assumes an instance of SourceForBinaryQueryImplementation is in project's lookup.
 * @author Jesse Glick, Tomas Zezula
 */
public class ProjectSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    
    /** Default constructor for lookup. */
    public ProjectSourceForBinaryQuery() {}

    public FileObject[] findSourceRoot(URL binaryRoot) {
        Project project = FileOwnerQuery.getOwner(URI.create(binaryRoot.toString()));
        if (project != null) {
            SourceForBinaryQueryImplementation sfbqi =
                    (SourceForBinaryQueryImplementation)project.getLookup().lookup(
                            SourceForBinaryQueryImplementation.class);
            if (sfbqi != null) {
                return sfbqi.findSourceRoot(binaryRoot);
            }
        }
        return new FileObject[0];
    }
    
}
