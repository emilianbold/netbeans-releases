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
package org.netbeans.modules.web.project.queries;

import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.modules.web.project.SourceRoots;
import org.openide.filesystems.FileObject;

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
