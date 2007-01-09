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
package org.netbeans.modules.java.platform.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;


/**
 * This implementation of the SourceForBinaryQueryImplementation
 * provides sources for the active platform and project libraries
 */

public class PlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    
    private static final String JAR_FILE = "jar:file:";                 //NOI18N
    private static final String RTJAR_PATH = "/jre/lib/rt.jar!/";       //NOI18N
    private static final String SRC_ZIP = "/src.zip";                    //NOI18N

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
        String binaryRootS = binaryRoot.toExternalForm();
        if (binaryRootS.startsWith(JAR_FILE)) {
            if (binaryRootS.endsWith(RTJAR_PATH)) {
                //Unregistered platform
                String srcZipS = binaryRootS.substring(4,binaryRootS.length() - RTJAR_PATH.length()) + SRC_ZIP;
                try {
                    URL srcZip = FileUtil.getArchiveRoot(new URL(srcZipS));
                    FileObject fo = URLMapper.findFileObject(srcZip);
                    if (fo != null) {
                        return new UnregisteredPlatformResult (fo);
                    }
                } catch (MalformedURLException mue) {
                    Exceptions.printStackTrace(mue);
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
    
    private static class UnregisteredPlatformResult implements SourceForBinaryQuery.Result {
        
        private FileObject srcRoot;
        
        private UnregisteredPlatformResult (FileObject fo) {
            assert fo != null;
            srcRoot = fo;
        }
    
        public FileObject[] getRoots() {            
            return srcRoot.isValid() ? new FileObject[] {srcRoot} : new FileObject[0];
        }
        
        public void addChangeListener(ChangeListener l) {
            //Not supported, no listening.
        }
        
        public void removeChangeListener(ChangeListener l) {
            //Not supported, no listening.
        }
}}

