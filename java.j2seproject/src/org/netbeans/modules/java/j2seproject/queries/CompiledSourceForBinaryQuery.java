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
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Finds sources corresponding to binaries in a J2SE project.
 * @author Jesse Glick, Tomas Zezula
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;

    public CompiledSourceForBinaryQuery(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
    }

    public FileObject[] findSourceRoot(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
        }
        FileObject result = getSources(binaryRoot, "build.classes.dir","src.dir");   //NOI18N
        if (result != null)
            return new FileObject[] {result};
        result = getSources (binaryRoot,"dist.jar","src.dir");                             //NOI18N
        if (result != null)
            return new FileObject[] {result};
        result = getSources (binaryRoot,"build.test.classes.dir","test.src.dir");           //NOI18N
        if (result != null)
            return new FileObject[] {result};
        return new FileObject[0];
    }


    private FileObject getSources (URL binaryRoot, String binaryProperty, String sourceProperty) {
        try {
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile (outDir);
                URL url = f.toURI().toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) {
                    // non-existing 
                    assert !url.toExternalForm().endsWith("/") : f;
                    url = new URL(url.toExternalForm() + "/");
                }
                if (url.equals (binaryRoot)) {
                    String srcDir = evaluator.getProperty(sourceProperty);
                    if (srcDir != null) {
                        FileObject srcFile = helper.resolveFileObject(srcDir);
                        if (FileUtil.isArchiveFile(srcFile)) {
                            srcFile = FileUtil.getArchiveRoot (srcFile);
                        }
                        return srcFile;
                    }
                }
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return null;
    }
    
}
