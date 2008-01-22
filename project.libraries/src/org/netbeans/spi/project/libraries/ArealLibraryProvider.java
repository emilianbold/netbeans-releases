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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.libraries;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Library provider which can define libraries in particular areas.
 * There is no explicit method to save a library; setters on {@link LibraryImplementation} should do this.
 * @param A the type of storage area used by this provider
 * @param L the type of library created by this provider
 * @since org.netbeans.modules.project.libraries/1 1.15
 */
public interface ArealLibraryProvider<A extends LibraryStorageArea, L extends LibraryImplementation> {

    /**
     * Property to fire when {@link #getOpenAreas} might have changed.
     */
    String PROP_OPEN_AREAS = "openAreas"; // NOI18N

    /**
     * Adds a listener to {@link #PROP_OPEN_AREAS}.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a listener to {@link #PROP_OPEN_AREAS}.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Gets the runtime type of the area used by this provider.
     * @return the area type
     */
    Class<A> areaType();

    /**
     * Gets the runtime type of the libraries created by this provider.
     * @return the library type
     */
    Class<L> libraryType();

    /**
     * Creates or otherwise picks a storage area interactively.
     * This might actually create a fresh area, or just load an existing one,
     * or even do nothing (and return null).
     * The implementor is free to show a dialog here.
     * @return a new or existing storage area, or null
     */
    A createArea();

    /**
     * Loads a storage area (which may or may exist yet).
     * @param location an abstract storage location which may or may not be recognized by this provider
     * @return an area whose {@link LibraryStorageArea#getLocation} matches the provided location,
     *         or null if this type of location is not recognized by this provider
     */
    A loadArea(URL location);

    /**
     * Looks for areas which should be somehow listed as open.
     * For example, a provider which refers to library areas from project metadata
     * could list all areas referred to from currently open projects.
     * It is <em>not</em> necessary to include areas recently mentioned e.g. by {@link #createArea}.
     * @return a (possibly empty) collection of areas
     */
    Set<A> getOpenAreas();

    /**
     * Gets all libraries defined in a given area.
     * No two libraries in this area may share a given name (as in {@link LibraryImplementation#getName},
     * though it is permitted for libraries from different areas to have the same name.
     * Changes in the set of libraries defined in this area should be fired through {@link LibraryProvider#PROP_LIBRARIES}.
     * Since {@link IOException} is not thrown either from this method or from {@link LibraryProvider#getLibraries},
     * it is expected that any problems loading library definitions will be logged and that those libraries will be skipped.
     * @param area some storage area (which might not even exist yet, in which case the set of libraries will initially be empty)
     * @return a listenable set of libraries in this area
     *         (it is permitted to return distinct objects from call to call on the same area,
     *         i.e. no caching by the implementation is necessary)
     */
    LibraryProvider<L> getLibraries(A area);

    /**
     * Creates a new library.
     * @param type the kind of library to make, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the library name, as in {@link LibraryImplementation#getName}
     * @param area the location to define the library
     * @param contents initial volume contents (keys must be contained in the appropriate {@link LibraryTypeProvider#getSupportedVolumeTypes})
     * @return a new library with matching type, name, area, and contents
     * @throws IOException if an error occurs creating the library definition     */
    L createLibrary(String type, String name, A area, Map<String,List<URL>> contents) throws IOException;

    /**
     * Deletes an existing library.
     * @param library a library produced by this provider
     * @throws IOException if a problem can encountered deleting the library definition
     */
    void remove(L library) throws IOException;

}
