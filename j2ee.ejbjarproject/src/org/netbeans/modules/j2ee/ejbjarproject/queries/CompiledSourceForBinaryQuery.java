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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.j2ee.ejbjarproject.SourceRoots;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import java.util.Map;
import java.util.HashMap;
import org.openide.filesystems.FileUtil;

/**
 * Finds sources corresponding to binaries in a J2SE project.
 * @author Jesse Glick, Tomas Zezula
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Map/*<URL,SourceForBinaryQuery.Result>*/  cache = new HashMap ();

    public CompiledSourceForBinaryQuery (AntProjectHelper helper,PropertyEvaluator evaluator, 
            SourceRoots srcRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = srcRoots;
        this.testRoots = testRoots;
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
        SourceRoots src = null;
        if (hasSources(binaryRoot, EjbJarProjectProperties.BUILD_CLASSES_DIR)) {   //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot, EjbJarProjectProperties.DIST_JAR)) {      //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot, EjbJarProjectProperties.BUILD_TEST_CLASSES_DIR)) {    //NOI18N
            src = this.testRoots;
        }
        if (src == null) {
            return null;
        }
        else {
            res = new Result (src);
            cache.put (binaryRoot, res);
            return res;
        }
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
    
    private static class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {
        
        FileObject[] cache;
        AntProjectHelper helper;
        String propertyName;
        private ArrayList listeners;
        private SourceRoots sourceRoots;
        
        public Result (SourceRoots sourceRoots) {
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
        }
        
        public FileObject[] getRoots () {
             return this.sourceRoots.getRoots(); //No need to cache it, SourceRoots does
        }
        
        public void addChangeListener (ChangeListener l) {
            //TODO: Implement this if needed (source folder can be changed)
        }
        
        public void removeChangeListener (ChangeListener l) {
            //TODO: Implement this if needed (source folder can be changed)
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                this.fireChange ();
            }
        }

        private void fireChange() {
            Iterator it;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                it = ((ArrayList)this.listeners.clone()).iterator();
            }
            ChangeEvent event = new ChangeEvent(this);
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }
        
    }
    
}
