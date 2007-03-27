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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * Implementation of Javadoc query for the library.
 */
public class JavadocForBinaryQueryLibraryImpl implements JavadocForBinaryQueryImplementation {
    
    private static int MAX_DEPTH = 3;
    private final Map<URL,URL> normalizedURLCache = new HashMap<URL, URL>();

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryLibraryImpl() {
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

            private Library lib;
            private final ChangeSupport cs = new ChangeSupport(this);
            private URL[] cachedRoots;
            

            public R (Library lib) {
                this.lib = lib;
                this.lib.addPropertyChangeListener (WeakListeners.propertyChange(this,this.lib));
            }

            public synchronized URL[] getRoots() {
                if (this.cachedRoots == null) {
                    List<URL> result = new ArrayList<URL>();
                    for (URL u : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC)) {
                        result.add (getIndexFolder(u));
                    }
                    this.cachedRoots = result.toArray(new URL[result.size()]);
                }
                return this.cachedRoots;
            }
            
            public synchronized void addChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                cs.addChangeListener(l);
            }
            
            public synchronized void removeChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                cs.removeChangeListener(l);
            }
            
            public void propertyChange (PropertyChangeEvent event) {
                if (Library.PROP_CONTENT.equals(event.getPropertyName())) {
                    synchronized (this) {
                        this.cachedRoots = null;
                    }
                    cs.fireChange();
                }
            }
            
        }

        boolean isNormalizedURL = isNormalizedURL(b);
        for (Library lib : LibraryManager.getDefault().getLibraries()) {
            String type = lib.getType();
            if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(type)) {
                continue;
            }
            for (URL entry : lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH)) {
                URL normalizedEntry;
                if (isNormalizedURL) {
                    normalizedEntry = getNormalizedURL(entry);
                }
                else {
                    normalizedEntry = entry;
                }
                if (normalizedEntry != null && normalizedEntry.equals(b)) {
                    return new R(lib);
                }                
            }
        }
        return null;
    }

    private URL getNormalizedURL (URL url) {
        //URL is already nornalized, return it
        if (isNormalizedURL(url)) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache
        // in this case the search can use the cache onle and can be faster
        // from O(n) to O(ln(n))
        URL normalizedURL = normalizedURLCache.get(url);
        if (normalizedURL == null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                try {
                    normalizedURL = fo.getURL();
                    this.normalizedURLCache.put (url, normalizedURL);
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return normalizedURL;
    }

    /**
     * Returns true if the given URL is file based, it is already
     * resolved either into file URL or jar URL with file path.
     * @param URL url
     * @return true if  the URL is normal
     */
    private static boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }
    
    
    
    /**
     * Tests if the query accepts the root as valid JavadocRoot,
     * the query accepts the JavaDoc root, if it can find the index-files
     * or index-all.html in the root.
     * @param rootURL the javadoc root
     * @return true if the root is a valid Javadoc root
     */
    static boolean isValidLibraryJavadocRoot (final URL rootURL) {
        assert rootURL != null && rootURL.toExternalForm().endsWith("/");
        final FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return false;
        }
        return findIndexFolder (root,1) != null;        
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
            ErrorManager.getDefault().notify(e);
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
        for (FileObject child : fo.getChildren()) {
            if (child.isFolder()) {
                FileObject result = findIndexFolder(child, depth+1);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

}
