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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private NbModuleProject project;

    public UnitTestForSourceQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, "src.dir", "test.unit.src.dir"); // NOI18N
    }
    
    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, "test.unit.src.dir", "src.dir"); // NOI18N
    }
    
    private URL[] find(FileObject file, String from, String to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        AntProjectHelper helper = project.getHelper();
        String val = project.evaluator().getProperty(from);
        assert val != null : "No value for " + from + " in " + project;
        FileObject fromRoot = helper.resolveFileObject(val);
        if (!file.equals(fromRoot)) {
            return null;
        }
        val = project.evaluator().getProperty(to);
        assert val != null : "No value for " + to + " in " + project;
        String path = helper.resolvePath(val);
        try {
            File f = helper.resolveFile(path);
            if (!f.exists()) {
                return null;
            }
            else {
                return new URL[] {f.toURI().normalize().toURL()};
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
