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

package org.netbeans.modules.projectapi;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Delegates {@link FileBuiltQuery} to implementations in project lookup.
 * @author Jesse Glick
 */
public class ProjectFileBuiltQuery implements FileBuiltQueryImplementation {
    
    /** Default constructor for lookup. */
    public ProjectFileBuiltQuery() {}
    
    public FileBuiltQuery.Status getStatus(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            FileBuiltQueryImplementation fbqi = (FileBuiltQueryImplementation)
                p.getLookup().lookup(FileBuiltQueryImplementation.class);
            if (fbqi != null) {
                return fbqi.getStatus(file);
            }
        }
        return null;
    }
    
}
