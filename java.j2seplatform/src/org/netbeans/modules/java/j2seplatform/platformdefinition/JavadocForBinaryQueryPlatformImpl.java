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

package org.netbeans.modules.java.j2seplatform.platformdefinition;
import java.beans.PropertyChangeEvent;

import java.beans.PropertyChangeListener;


import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import org.openide.filesystems.URLMapper;

import org.openide.util.WeakListeners;

/**
 * Implementation of Javadoc query for the platform.
 */
public class JavadocForBinaryQueryPlatformImpl implements JavadocForBinaryQueryImplementation {
    
    private static int MAX_DEPTH = 4;   // Japanese Javadoc has subfolders /docs/ja/api

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryPlatformImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

            private JavaPlatform platform;
            private ArrayList listeners;
            private URL[] cachedRoots;

            public R (JavaPlatform plat) {
                this.platform = plat;
                this.platform.addPropertyChangeListener (WeakListeners.propertyChange(this,this.platform));
            }

            public synchronized URL[] getRoots() {
                if (this.cachedRoots == null) {
                    ArrayList l = new ArrayList();
                    for (Iterator i2 = this.platform.getJavadocFolders().iterator(); i2.hasNext();) {
                        URL u = (URL)i2.next();
                        l.add(getIndexFolder(u));
                    }
                    this.cachedRoots = (URL[])l.toArray(new URL[l.size()]);
                }
                return this.cachedRoots;
            }

            public synchronized void addChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";      //NOI18N
                if (this.listeners == null) {
                    this.listeners = new ArrayList ();
                }
                this.listeners.add (l);
            }
            public synchronized void removeChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";  //NOI18N
                if (this.listeners == null) {
                    return;
                }
                this.listeners.remove (l);
            }
            
            public void propertyChange (PropertyChangeEvent event) {
                if (JavaPlatform.PROP_JAVADOC_FOLDER.equals(event.getPropertyName())) {
                    synchronized (this) {
                        this.cachedRoots = null;
                    }
                    this.fireChange ();
                }
            }
            
            private void fireChange () {
                Iterator it = null;
                synchronized (this) {
                    if (this.listeners == null) {
                        return;
                    }
                    it = ((ArrayList)this.listeners.clone()).iterator();
                }
                ChangeEvent event = new ChangeEvent (this);
                while (it.hasNext()) {
                    ((ChangeListener)it.next()).stateChanged(event);
                }
            }
            
        }
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform platforms[] = jpm.getInstalledPlatforms();
        for (int i=0; i<platforms.length; i++) {
            JavaPlatform jp = platforms[i];
//Not valid assumption: May change in the future result should be returned, since the result is live.            
//            if (jp.getJavadocFolders().size() == 0) {
//                continue;
//            }
            Iterator it = jp.getBootstrapLibraries().entries().iterator();
            while (it.hasNext()) {
                ClassPath.Entry entry = (ClassPath.Entry)it.next();
                if (b.equals(entry.getURL())) {
                    return new R (jp);
                }
            }
        }
        return null;
    }
    
    /**
     * Search for the actual root of the Javadoc containing the index-all.html or 
     * index-files. In case when it is not able to find it, it returns the given Javadoc folder/file.
     * @param URL Javadoc folder/file
     * @return URL either the URL of folder containg the index or the given parameter if the index was not found.
     */
    private static URL getIndexFolder (URL rootURL) {
        if (rootURL == null) {
            return null;
        }
        FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return rootURL;
        }
        FileObject result = findIndexFolder (root,1);
        try {
            return result == null ? rootURL : result.getURL();        
        } catch (FileStateInvalidException e) {
            ErrorManager.getDefault().notify (e);
            return rootURL;
        }
    }
    
    private static FileObject findIndexFolder (FileObject fo, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        if (fo.getFileObject("index-files",null)!=null || fo.getFileObject("index-all.html",null)!=null) {  //NOI18N
            return fo;
        }
        FileObject[] children = fo.getChildren();
        for (int i=0; i< children.length; i++) {
            if (children[i].isFolder()) {
                FileObject result = findIndexFolder(children[i], depth+1);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
}
