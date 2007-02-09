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
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                SharabilityQueryImplementation sqi = p.getLookup().lookup(SharabilityQueryImplementation.class);
                if (sqi != null) {
                    return sqi.getSharability(file);
                }
            }
        }
        return SharabilityQuery.UNKNOWN;
    }
    
}
