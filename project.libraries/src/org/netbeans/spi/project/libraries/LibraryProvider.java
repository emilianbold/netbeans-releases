/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.project.libraries;

import org.netbeans.spi.project.libraries.LibraryImplementation;

import java.beans.PropertyChangeListener;

/**
 * Provider interface for implementing the read only library storage.
 * Library storage is a source of libraries used by LibraryManager.
 * LibraryManager allows existence of multiple LibraryProviders registered in
 * the default lookup.
 */
public interface LibraryProvider {

    /**
     * Name of libraries property
     */
    public static final String PROP_LIBRARIES = "libraries";        //NOI18N

    /**
     * Returns libraries provided by the implemented provider.
     * @return LibraryImplementation[] never return null, may return empty array.
     */
    public LibraryImplementation[] getLibraries();

    /**
     * Adds property change listener, the listener is notified when the libraries changed
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener);

    /**
     * Removes property change listener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener);
}
