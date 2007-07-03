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
package org.netbeans.modules.gsfpath.platform.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.netbeans.spi.gsfpath.queries.SourceForBinaryQueryImplementation;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import org.netbeans.api.gsfpath.platform.JavaPlatformManager;
import org.netbeans.api.gsfpath.platform.JavaPlatform;
import org.netbeans.api.gsfpath.queries.SourceForBinaryQuery;
import org.openide.util.ChangeSupport;
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

    private Map<URL,SourceForBinaryQuery.Result> cache = new HashMap<URL,SourceForBinaryQuery.Result>();

    public PlatformSourceForBinaryQuery () {
    }

    /**
     * Tries to locate the source root for given classpath root.
     * @param binaryRoot the URL of a classpath root (platform supports file and jar protocol)
     * @return FileObject[], never returns null
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        SourceForBinaryQuery.Result res = this.cache.get (binaryRoot);
        if (res != null) {
            return res;
        }
        JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        for (JavaPlatform platform : mgr.getInstalledPlatforms()) {
            for (ClassPath.Entry entry : platform.getBootstrapLibraries().entries()) {
                if (entry.getURL().equals (binaryRoot)) {
                    res = new Result(platform);
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
        private final ChangeSupport cs = new ChangeSupport(this);
                        
        public Result (JavaPlatform platform) {
            this.platform = platform;
            this.platform.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, platform));
        }
                        
        public FileObject[] getRoots () {       //No need for caching, platforms does.
            ClassPath sources = this.platform.getSourceFolders();
            return sources.getRoots();
        }
                        
        public void addChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            cs.addChangeListener(l);
        }
                        
        public void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener can not be null";  //NOI18N
            cs.removeChangeListener(l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (JavaPlatform.PROP_SOURCE_FOLDER.equals(event.getPropertyName())) {
                cs.fireChange();
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

