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
package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.UnitTestForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class UnitTestForSourceQueryImpl implements UnitTestForSourceQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public UnitTestForSourceQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public URL findUnitTest(FileObject source) {
        return find(source, "src.dir", "test.src.dir"); // NOI18N
    }
    
    public URL findSource(FileObject unitTest) {
        return find(unitTest, "test.src.dir", "src.dir"); // NOI18N
    }
    
    private URL find(FileObject file, String from, String to) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        FileObject fromRoot = helper.resolveFileObject(evaluator.getProperty(from));
        if (!fromRoot.equals(file)) {
            return null;
        }
        String path = helper.resolvePath(evaluator.getProperty(to));
        try {
            URL url = helper.resolveFile(path).toURI().normalize().toURL();
            if (!url.toExternalForm().endsWith("/")) {
                url = new URL (url.toExternalForm()+'/');
            }
            return url;
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, e.toString());
            return null;
        }
    }
    
}
