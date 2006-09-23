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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Delegates {@link UnitTestForSourceQueryImplementation} to the project which
 * owns the binary file.
 */
@SuppressWarnings("deprecation")
public class UnitTestForSourceQueryImpl implements UnitTestForSourceQueryImplementation, MultipleRootsUnitTestForSourceQueryImplementation {
    
    /** Default constructor for lookup. */
    public UnitTestForSourceQueryImpl() {
    }
    
    public URL findUnitTest(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            UnitTestForSourceQueryImplementation query = project.getLookup().lookup(UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTest(source);
            }
        }
        return null;
    }

    public URL[] findUnitTests(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query = project.getLookup().lookup(MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTests(source);
            }
        }
        return null;
    }

    public URL findSource(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            UnitTestForSourceQueryImplementation query = project.getLookup().lookup(UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSource(unitTest);
            }
        }
        return null;
    }

    public URL[] findSources(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query = project.getLookup().lookup(MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSources(unitTest);
            }
        }
        return null;
    }

}
