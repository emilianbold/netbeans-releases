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

package org.netbeans.bluej;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class SFBQueryImpl implements SourceForBinaryQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private Map/*<URL,SourceForBinaryQuery.Result>*/  cache = new HashMap ();

    private BluejProject project;

    public SFBQueryImpl(BluejProject proj, AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        project = proj;
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        SourceForBinaryQuery.Result res = (SourceForBinaryQuery.Result) cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        FileObject src = null;
        if (hasSources(binaryRoot,"build.classes.dir")) {   //NOI18N
            src = project.getProjectDirectory();
        }
        else if (hasSources (binaryRoot,"dist.jar")) {      //NOI18N
            src = project.getProjectDirectory();
        }
        else if (hasSources (binaryRoot,"build.test.classes.dir")) {    //NOI18N
            src = project.getProjectDirectory();
        }
        if (src == null) {
            return null;
        }
        else {
            res = new Result(src);
            cache.put (binaryRoot, res);
            return res;
        }
    }


    private boolean hasSources (URL binaryRoot, String binaryProperty) {
        try {
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile (outDir);
                URL url = f.toURI().toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    // non-existing 
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                if (url.equals (binaryRoot)) {
                    return true;
                }
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return false;
    }
    
    private static class Result implements SourceForBinaryQuery.Result {

        private FileObject[] sourceRoots;

        public Result(FileObject fo) {
            this.sourceRoots = new FileObject[] {fo};
        }
        
        public FileObject[] getRoots () {
            return this.sourceRoots; 
        }
        
        public synchronized void addChangeListener (ChangeListener l) {
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
        }

    }
    
}
