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
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.project.libraries.WritableLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;

// XXX make getLibraries return Set not array

/**
 * LibraryManager provides registry of the installed libraries.
 * LibraryManager can be used to list all installed libraries or to
 * query library by its system name.
 */
public final class LibraryManager {

    public static final String PROP_LIBRARIES = "libraries"; //NOI18N

    private static LibraryManager instance;

    private Lookup.Result<LibraryProvider> result;
    private Collection<LibraryProvider> currentStorages = new ArrayList<LibraryProvider>();
    private PropertyChangeListener plistener;
    private PropertyChangeSupport listeners;
    private Collection<Library> cache;


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
                result = Lookup.getDefault().lookupResult(LibraryProvider.class);
                result.addLookupListener (new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                            resetCache ();
                    }
                });
            }
            List<Library> l = new ArrayList<Library>();
            Collection<? extends LibraryProvider> instances = result.allInstances();
            Collection<LibraryProvider> added = new HashSet<LibraryProvider>(instances);
            added.removeAll (currentStorages);
            Collection<LibraryProvider> removed = new HashSet<LibraryProvider>(currentStorages);
            removed.removeAll (instances);
            currentStorages.clear();
            for (LibraryProvider storage : instances) {
                this.currentStorages.add (storage);
                for (LibraryImplementation impl : storage.getLibraries()) {
                    l.add(LibraryFactory.createLibrary(impl));
                }
            }
            for (LibraryProvider p : removed) {
                p.removePropertyChangeListener(this.plistener);
            }
            for (LibraryProvider p : added) {
                p.addPropertyChangeListener(this.plistener);
            }            
            this.cache = l;
        }
        return this.cache.toArray(new Library[this.cache.size()]);
    }
    
    
    /**
     * Installs a new library into the library manager.
     * <div class="nonnormative">
     * <p>
     * A typical usage would be:
     * </p>
     * LibraryManager libraryManager = LibraryManager.getDefault();
     * LibraryImplementation libImpl = LibrariesSupport.getLibraryTypeProvider("j2se").createLibrary();        
     * libImpl.setName("FooLibTest");
     * libImpl.setContent ("classpath",listOfResources);
     * libraryManager.addLibrary(LibraryFactory.createLibrary(libImpl));
     * </div>
     * @param library to be installed, the library has to be created
     * with registered {@link org.netbeans.spi.project.libraries.LibraryTypeProvider}.
     * @throws IOException when the library cannot be stored
     * @throws IllegalArgumentException if the library is not recognized by any 
     * {@link org.netbeans.spi.project.libraries.LibraryTypeProvider} or the library
     * of the same name already exists.
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public void addLibrary (final Library library) throws IOException, IllegalArgumentException {
        assert library != null;
        if (LibrariesSupport.getLibraryTypeProvider(library.getType()) == null) {
            throw new IllegalArgumentException ("Trying to add a library of unknown type: " + library.getType()); //NOI18N
        }
        String newLibraryName = library.getName();
        if ( newLibraryName == null || getLibrary(newLibraryName)!= null) {
            throw new IllegalArgumentException ("Library hasn't name or the name is already used: " + newLibraryName); //NOI18N
        }
        final Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template (WritableLibraryProvider.class));
        final Collection/*<WriteableLibraryProvider>*/ providers = result.allInstances();
        assert providers.size() == 1;        
        ((WritableLibraryProvider)providers.iterator().next()).addLibrary(library.getLibraryImplementation());
    }
    
    /**
     * Removes installed library 
     * @param library to be removed. 
     * @throws IOException when library cannot be deleted.
     * @throws IllegalArgumentException when library is not installed in a writeable 
     * {@link org.netbeans.spi.project.libraries.LibraryProvider}
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public void removeLibrary (final Library library) throws IOException, IllegalArgumentException {
        assert library != null;
        final Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template (WritableLibraryProvider.class));
        final Collection/*<WriteableLibraryProvider>*/ providers = result.allInstances();
        assert providers.size() == 1;
        ((WritableLibraryProvider)providers.iterator().next()).removeLibrary(library.getLibraryImplementation());
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

