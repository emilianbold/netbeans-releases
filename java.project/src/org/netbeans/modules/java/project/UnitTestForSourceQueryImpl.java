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
public class UnitTestForSourceQueryImpl implements UnitTestForSourceQueryImplementation, MultipleRootsUnitTestForSourceQueryImplementation {
    
    /** Default constructor for lookup. */
    public UnitTestForSourceQueryImpl() {
    }
    
    public URL findUnitTest(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            UnitTestForSourceQueryImplementation query =
                (UnitTestForSourceQueryImplementation)project.getLookup().lookup(
                    UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTest(source);
            }
        }
        return null;
    }

    public URL[] findUnitTests(FileObject source) {
        Project project = FileOwnerQuery.getOwner(source);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query =
                (MultipleRootsUnitTestForSourceQueryImplementation)project.getLookup().lookup(
                    MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findUnitTests(source);
            }
        }
        return null;
    }

    public URL findSource(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            UnitTestForSourceQueryImplementation query =
                (UnitTestForSourceQueryImplementation)project.getLookup().lookup(
                    UnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSource(unitTest);
            }
        }
        return null;
    }

    public URL[] findSources(FileObject unitTest) {
        Project project = FileOwnerQuery.getOwner(unitTest);
        if (project != null) {
            MultipleRootsUnitTestForSourceQueryImplementation query =
                (MultipleRootsUnitTestForSourceQueryImplementation)project.getLookup().lookup(
                    MultipleRootsUnitTestForSourceQueryImplementation.class);
            if (query != null) {
                return query.findSources(unitTest);
            }
        }
        return null;
    }

}
