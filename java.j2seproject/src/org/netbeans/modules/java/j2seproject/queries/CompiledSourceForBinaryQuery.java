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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.netbeans.api.java.queries.SourceForBinaryQuery;
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

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        String srcPropName = null;
        if (hasSources(binaryRoot,"build.classes.dir")) {   //NOI18N
            srcPropName = "src.dir";                        //NOI18N
        }
        else if (hasSources (binaryRoot,"dist.jar")) {      //NOI18N
            srcPropName = "src.dir";                        //NOI18N
        }
        else if (hasSources (binaryRoot,"build.test.classes.dir")) {    //NOI18N
            srcPropName = "test.src.dir";                               //NOI18N
        }
        return srcPropName == null ? null : new Result (this.helper, this.evaluator, srcPropName);
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

        private FileObject[] cache;
        private AntProjectHelper helper;
        private PropertyEvaluator evaluator;
        private String propName;
        private ArrayList listeners;

        public Result (AntProjectHelper helper, PropertyEvaluator evaluator, String propName) {
            this.helper = helper;
            this.evaluator = evaluator;
            this.propName = propName;
            this.evaluator.addPropertyChangeListener(this);
        }
        
        public synchronized FileObject[] getRoots () {
            if (this.cache == null) {
                String srcDir = this.evaluator.getProperty(this.propName);
                FileObject srcFile = null;
                if (srcDir != null) {
                    srcFile = this.helper.resolveFileObject(srcDir);
                    if (srcFile != null) {
                        if (!srcFile.isValid()) {
                            srcFile = null;
                        }
                        else if (FileUtil.isArchiveFile(srcFile)) {
                            srcFile = FileUtil.getArchiveRoot (srcFile);
                        }
                    }
                }               
                this.cache = (srcFile == null ? new FileObject[0] : new FileObject[] {srcFile});
            }
            return this.cache;
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
            if (propName.equals(evt.getPropertyName())) {
                synchronized(this) {
                    this.cache = null;
                }
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
