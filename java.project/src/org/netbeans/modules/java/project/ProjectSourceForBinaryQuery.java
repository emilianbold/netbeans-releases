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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
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

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        Project project = FileOwnerQuery.getOwner(URI.create(binaryRoot.toString()));
        if (project != null) {
            SourceForBinaryQueryImplementation sfbqi =
                    (SourceForBinaryQueryImplementation)project.getLookup().lookup(
                            SourceForBinaryQueryImplementation.class);
            if (sfbqi != null) {
                return sfbqi.findSourceRoots(binaryRoot);
            }
        }
        return null;
    }
    
}
