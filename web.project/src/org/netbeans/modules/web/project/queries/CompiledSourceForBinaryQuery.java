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
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.net.URL;
import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.modules.web.project.SourceRoots;

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

    public CompiledSourceForBinaryQuery (AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
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
        if (hasSources(binaryRoot, WebProjectProperties.BUILD_CLASSES_DIR)) {   //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources(binaryRoot, WebProjectProperties.BUILD_EAR_CLASSES_DIR)) {   //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot, WebProjectProperties.DIST_WAR)) {      //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot, WebProjectProperties.BUILD_TEST_CLASSES_DIR)) {    //NOI18N
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
    
    private static class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {
        
        private ArrayList listeners;
        private SourceRoots sourceRoots;

        public Result (SourceRoots sourceRoots) {
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
        }
        
        public FileObject[] getRoots () {
            return this.sourceRoots.getRoots(); //No need to cache it, SourceRoots does
        }
        
        public synchronized void addChangeListener (ChangeListener l) {
            if (this.listeners == null) {
                this.listeners = new ArrayList();
            }
            this.listeners.add (l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove (l);
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
