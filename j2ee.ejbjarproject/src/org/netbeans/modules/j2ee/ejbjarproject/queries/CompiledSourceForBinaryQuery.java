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
package org.netbeans.modules.j2ee.ejbjarproject.queries;

import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import java.util.Map;
import java.util.HashMap;

/**
 * Finds sources corresponding to binaries in a J2SE project.
 * @author Jesse Glick, Tomas Zezula
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private AntProjectHelper helper;
    private Map/*<URL,SourceForBinaryQuery.Result>*/  cache = new HashMap ();

    public CompiledSourceForBinaryQuery (AntProjectHelper helper) {
        this.helper = helper;
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        SourceForBinaryQuery.Result res = (SourceForBinaryQuery.Result) this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        if (hasSources(binaryRoot, "build.classes.dir") || hasSources (binaryRoot,"dist.jar")) {      //NOI18N
            res = new Result (this.helper, "src.dir");                      //NOI18N
            this.cache.put (binaryRoot, res);
            return res;
        }
        return null;
    }


    private boolean hasSources (URL binaryRoot, String binaryProperty) {
        try {
            //TODO: Fix this. Use FileUtil.getArchiveFile.
            if (binaryRoot.getProtocol().equals("jar")) {  // NOI18N
                // We are interested in the JAR file itself.
                // Note that this impl therefore accepts *both* file:/tmp/foo.jar
                // and jar:file:/tmp/foo.jar!/ as equivalent (like URLClassLoader).
                String surl = binaryRoot.toExternalForm();
                if (surl.endsWith("!/")) { // NOI18N
                    binaryRoot = new URL(surl.substring(4, surl.length() - 2));
                } else if (surl.lastIndexOf("!/") == -1) { // NOI18N
                    // Legal??
                    binaryRoot = new URL(surl.substring(4));
                } else {
                    // Some specific path, e.g. jar:file:/tmp/foo.jar!/foo/,
                    // which we do not support.
                }
            }
            String outDir = helper.getStandardPropertyEvaluator ().getProperty (binaryProperty);
            if (outDir != null) {
                URL url = helper.resolveFile (outDir).toURI().toURL();
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
        
        FileObject[] cache;
        AntProjectHelper helper;
        String propertyName;
        
        public Result (AntProjectHelper helper, String propertyName) {
            this.helper = helper;
            this.propertyName = propertyName;
        }
        
        public synchronized FileObject[] getRoots () {
            if (this.cache == null) {
                String srcDir = this.helper.getStandardPropertyEvaluator ().getProperty (propertyName);
                FileObject fo = null;
                if (srcDir != null) {                
                    fo = helper.resolveFileObject(srcDir);                    
                }
                if (fo != null) {
                    this.cache = new FileObject[] {fo};
                }
                else {
                    this.cache = new FileObject[0];
                }
            }
            return this.cache;
        }
        
        public void addChangeListener (ChangeListener l) {
            //TODO: Implement this if needed (source folder can be changed)
        }
        
        public void removeChangeListener (ChangeListener l) {
            //TODO: Implement this if needed (source folder can be changed)
        }
        
    }
    
}
