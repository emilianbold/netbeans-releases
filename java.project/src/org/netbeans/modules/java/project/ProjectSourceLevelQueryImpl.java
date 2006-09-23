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
            SourceLevelQueryImplementation slq = project.getLookup().lookup(SourceLevelQueryImplementation.class);
            if (slq != null) {
                return slq.getSourceLevel(javaFile);
            }
        }
        return null;
    }
    
}
