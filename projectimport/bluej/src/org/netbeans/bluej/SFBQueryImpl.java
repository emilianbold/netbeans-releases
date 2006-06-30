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

package org.netbeans.bluej;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
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
public class SFBQueryImpl implements SourceForBinaryQueryImplementation, JavadocForBinaryQueryImplementation {
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private Map/*<URL,SourceForBinaryQuery.Result>*/  cache = new HashMap ();
    private DocResult docResult;

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
        if (matches(binaryRoot,"build.classes.dir")) {   // NOI18N
            src = project.getProjectDirectory();
        }
        else if (matches (binaryRoot,"dist.jar")) {      // NOI18N
            src = project.getProjectDirectory();
        }
        else if (matches (binaryRoot,"build.test.classes.dir")) {    // NOI18N
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


    private boolean matches (URL binaryRoot, String binaryProperty) {
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

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        if (matches (binaryRoot, "build.classes.dir") || matches (binaryRoot, "dist.jar") ||  // NOI18N
                matches (binaryRoot, "build.test.classes.dir")) {   // NOI18N
            if (docResult == null) {
                //TODO make this relative to property?? the location should not be changed anyway because then
                // it stops working against bluej itself..
                File fil = new File(FileUtil.toFile(project.getProjectDirectory()), "doc");  // NOI18N
                try {
                    docResult = new DocResult(fil.toURI().toURL());
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return docResult;
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

    private static class DocResult implements JavadocForBinaryQuery.Result {

        private URL[] urls;
        public DocResult(URL url) {
            if (!url.toExternalForm().endsWith("/")) {  // NOI18N
                try {
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                } // NOI18N
            }
            urls = new URL[] {url};

        }

        public URL[] getRoots() {
            return urls;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }
    
}
