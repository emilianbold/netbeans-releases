/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.platform.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.ErrorManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.util.WeakListeners;


/**
 * This implementation of the SourceForBinaryQueryImplementation
 * provides sources for the active platform and project libraries
 */

public class PlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private Map/*<URL, SourceForBinaryQuery.Result>*/ cache = new HashMap ();

    public PlatformSourceForBinaryQuery () {
    }

    /**
     * Tries to locate the source root for given classpath root.
     * @param binaryRoot the URL of a classpath root (platform supports file and jar protocol)
     * @return FileObject[], never returns null
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        SourceForBinaryQuery.Result res = (SourceForBinaryQuery.Result) this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        JavaPlatform[] platforms = mgr.getInstalledPlatforms();
        for (int i=0; i< platforms.length; i++) {
            ClassPath cp = platforms[i].getBootstrapLibraries();
            for (Iterator it = cp.entries().iterator(); it.hasNext();) {
                ClassPath.Entry entry = (ClassPath.Entry) it.next();
                if (entry.getURL().equals (binaryRoot)) {
                    res = new Result (platforms[i]);
                    this.cache.put (binaryRoot, res);
                    return res;
                }
            }
        }
        return null;
    }
    
    private static class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {
                        
        private JavaPlatform platform;
        private ArrayList listeners;
                        
        public Result (JavaPlatform platform) {
            this.platform = platform;
            this.platform.addPropertyChangeListener ((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,this,this.platform));
        }
                        
        public FileObject[] getRoots () {       //No need for caching, platforms does.
            ClassPath sources = this.platform.getSourceFolders();
            return sources.getRoots();
        }
                        
        public synchronized void addChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            if (this.listeners == null) {
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }
                        
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove (l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (JavaPlatform.PROP_SOURCE_FOLDER.equals(event.getPropertyName())) {
                this.fireChange ();
            }
        }
        
        private void fireChange () {
            Iterator it = null;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                it = ((ArrayList)this.listeners.clone()).iterator ();
            }
            ChangeEvent event = new ChangeEvent (this);
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }
    }
}
