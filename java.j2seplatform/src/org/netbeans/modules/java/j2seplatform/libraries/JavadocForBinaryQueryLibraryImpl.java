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

package org.netbeans.modules.java.j2seplatform.libraries;
import java.beans.PropertyChangeEvent;

import java.beans.PropertyChangeListener;



import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;

import javax.swing.event.ChangeListener;

import org.openide.ErrorManager;
import org.openide.util.WeakListeners;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;



/**
 * Implementation of Javadoc query for the library.
 */
public class JavadocForBinaryQueryLibraryImpl implements JavadocForBinaryQueryImplementation {
    
    private static int MAX_DEPTH = 3;

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryLibraryImpl() {
    }

    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {

            private Library lib;
            private ArrayList listeners;
            private URL[] cachedRoots;
            

            public R (Library lib) {
                this.lib = lib;
                this.lib.addPropertyChangeListener (WeakListeners.propertyChange(this,this.lib));
            }

            public synchronized URL[] getRoots() {
                if (this.cachedRoots == null) {
                    List l = lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_JAVADOC);
                    List result = new ArrayList ();
                    for (Iterator it = l.iterator(); it.hasNext();) {
                        URL u = (URL) it.next ();
                        result.add (getIndexFolder(u));
                    }
                    this.cachedRoots =  (URL[])result.toArray(new URL[result.size()]);
                }
                return this.cachedRoots;
            }
            
            public synchronized void addChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                if (this.listeners == null) {
                    this.listeners = new ArrayList ();
                }
                this.listeners.add (l);
            }
            
            public synchronized void removeChangeListener(ChangeListener l) {
                assert l != null : "Listener can not be null";
                if (this.listeners == null) {
                    return;
                }
                this.listeners.remove (l);
            }
            
            public void propertyChange (PropertyChangeEvent event) {
                if (Library.PROP_CONTENT.equals(event.getPropertyName())) {
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
                    it = ((ArrayList)this.listeners.clone()).iterator ();
                }
                ChangeEvent event = new ChangeEvent (this);
                while (it.hasNext()) {
                    ((ChangeListener)it.next()).stateChanged(event);
                }
            }
        }

        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        for (int i=0; i<libs.length; i++) {
            String type = libs[i].getType();
            if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(type)) {
                continue;
            }
            List jars = libs[i].getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);    //NOI18N
            Iterator it = jars.iterator();
            while (it.hasNext()) {
                URL entry = (URL)it.next();
                FileObject file = URLMapper.findFileObject (entry);
                if (file != null) {
                    try {
                        if (b.equals(file.getURL())) {
                            return new R(libs[i]);
                        }
                    } catch (FileStateInvalidException e) {
                        ErrorManager.getDefault().notify (e);
                    }
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
