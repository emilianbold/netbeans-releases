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
package org.netbeans.modules.web.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.web.project.SourceRoots;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;

    public UnitTestForSourceQueryImpl(SourceRoots sourceRoots, SourceRoots testRoots) {
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, sourceRoots, testRoots); // NOI18N
    }

    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, testRoots, sourceRoots); // NOI18N
    }
    
    private URL[] find(FileObject file, SourceRoots from, SourceRoots to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        FileObject[] fromRoots = from.getRoots();
        for (int i = 0; i < fromRoots.length; i++) {
            if (fromRoots[i].equals(file)) {
                return to.getRootURLs();
            }
        }
        return null;
    }
    
}
