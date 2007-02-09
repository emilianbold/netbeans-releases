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
            FileBuiltQueryImplementation fbqi = p.getLookup().lookup(FileBuiltQueryImplementation.class);
            if (fbqi != null) {
                return fbqi.getStatus(file);
            }
        }
        return null;
    }
    
}
