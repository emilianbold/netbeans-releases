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

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Delegates {@link SharabilityQuery} to implementations in project lookup.
 * @author Jesse Glick
 */
public class ProjectSharabilityQuery implements SharabilityQueryImplementation {
    
    /** Default constructor for lookup. */
    public ProjectSharabilityQuery() {}
    
    public int getSharability(File file) {
        FileObject[] fo = FileUtil.fromFile(file);
        if (fo.length > 0) {
            Project p = FileOwnerQuery.getOwner(fo[0]);
            if (p != null) {
                SharabilityQueryImplementation sqi = (SharabilityQueryImplementation)
                    p.getLookup().lookup(SharabilityQueryImplementation.class);
                if (sqi != null) {
                    return sqi.getSharability(file);
                }
            }
        }
        return SharabilityQuery.UNKNOWN;
    }
    
}
