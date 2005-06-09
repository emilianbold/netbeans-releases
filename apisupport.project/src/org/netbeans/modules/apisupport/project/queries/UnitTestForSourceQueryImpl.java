/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.apisupport.project.*;

public class UnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private NbModuleProject project;

    public UnitTestForSourceQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    public URL[] findUnitTests(FileObject source) {
        return find(source, "src.dir", "test.unit.src.dir");
    }
    
    public URL[] findSources(FileObject unitTest) {
        return find(unitTest, "test.unit.src.dir", "src.dir");
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
        if (!fromRoot.equals(file)) {
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
