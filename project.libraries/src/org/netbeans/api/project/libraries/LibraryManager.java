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

package org.netbeans.api.project.libraries;


import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

// XXX make getLibraries return Set not array

/**
 * LibraryManager provides registry of the installed libraries.
 * LibraryManager can be used to list all installed libraries or to
 * query library by its system name.
 */
public final class LibraryManager {

    public static final String PROP_LIBRARIES = "libraries"; //NOI18N

    private static LibraryManager instance;

    private Lookup.Result result;
    private Collection currentStorages = new ArrayList ();
    private PropertyChangeListener plistener;
    private PropertyChangeSupport listeners;
    private Collection cache;


    private LibraryManager () {
        this.listeners = new PropertyChangeSupport(this);
    }

    /**
     * Returns library by its name.
     * @param name of the library, must not be null
     * @return library or null if the library is not found
     */
    public Library getLibrary(String name) {
        assert name != null;
        Library[] libs = this.getLibraries();
        for (int i = 0; i < libs.length; i++) {
            if (name.equals(libs[i].getName())) {
                return libs[i];
            }
        }
        return null;
    }

    /**
     * List all library defined in the IDE.
     *
     * @return Library[] library definitions never <code>null</code>
     */
    public synchronized Library[] getLibraries() {
        if (this.cache == null) {
            if (this.result == null) {
                plistener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        resetCache ();
                    }
                };
                result = Lookup.getDefault().lookup (new Lookup.Template(LibraryProvider.class));
                result.addLookupListener (new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                            resetCache ();
                    }
                });
            }
            List l = new ArrayList ();
            Collection instances = result.allInstances();
            Collection added = new HashSet (instances);
            added.removeAll (currentStorages);
            Collection removed = new HashSet (currentStorages);
            removed.removeAll (instances);
            currentStorages.clear();
            for (Iterator it = instances.iterator(); it.hasNext();) {
                LibraryProvider storage = (LibraryProvider) it.next ();
                this.currentStorages.add (storage);
                LibraryImplementation[] impls = storage.getLibraries();
                for (int i=0; i<impls.length; i++) {                    
                    l.add(new Library (impls[i]));
                }                
            }
            for (Iterator it = removed.iterator(); it.hasNext();) {
                ((LibraryProvider)it.next()).removePropertyChangeListener(this.plistener);
            }
            for (Iterator it = added.iterator(); it.hasNext();) {
                ((LibraryProvider)it.next()).addPropertyChangeListener(this.plistener);
            }            
            this.cache = l;
        }
        return (Library[]) this.cache.toArray(new Library[this.cache.size()]);
    }

    /**
     * Adds PropertyChangeListener.
     * The listener is notified when library is added or removed.
     * @param listener to be notified
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        assert listener != null;
        this.listeners.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        assert listener != null;
        this.listeners.removePropertyChangeListener (listener);
    }


    private synchronized void resetCache () {
        this.cache = null;
        this.listeners.firePropertyChange(PROP_LIBRARIES, null, null);
    }


    /**
     * Get the default instance of the library manager.
     * @return the singleton instance
     */
    public static synchronized LibraryManager getDefault () {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }



} // end LibraryManager

