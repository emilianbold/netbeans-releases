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

package org.netbeans.spi.project.libraries;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;

/**
 * Base SPI interface for library. This SPI class is used as a model by the libraries framework.
 * The LibraryTypeProvider implementor should rather use
 * org.netbeans.spi.project.libraries.support.LibrariesSupport.createLibraryImplementation
 * factory method to create default LibraryImplementation than to implement this interface.
 */
public interface LibraryImplementation {

    String PROP_NAME = "name";                  //NOI18N
    String PROP_DESCRIPTION = "description";    //NOI18N
    String PROP_CONTENT = "content";            //NOI18N

    /**
     * Returns type of library, the LibraryTypeProvider creates libraries
     * of given unique type.
     * @return String unique identifier, never returns null.
     */
    String getType();

    /**
     * Returns name of the library
     * @return String unique name of the library, never returns null.
     */
    String getName();

    /**
     * Get a description of the library.
     * The description provides more detailed information about the library.
     * @return String the description or null if the description is not available
     */
    String getDescription();


    /**
     * Returns the resource name of the bundle which is used for localizing
     * the name and description. The bundle is located using the system ClassLoader.
     * @return String, the resource name of the bundle. If null in case when the
     * name and description is not localized.
     */
    String getLocalizingBundle();

    /**
     * Returns List of resources contained in the given volume.
     * The returned list is unmodifiable. To change the content of
     * the given volume use setContent method.
     * @param volumeType the type of volume for which the content should be returned.
     * @return list of resource URLs (never null)
     * @throws IllegalArgumentException if the library does not support given type of volume
     */
    List<URL> getContent(String volumeType) throws IllegalArgumentException;

    /**
     * Sets the name of the library, called by LibrariesStorage while reading the library
     * @param name -  the unique name of the library, can't be null.
     */
    void setName(String name);

    /**
     * Sets the description of the library, called by LibrariesStorage while reading the library
     * The description is more detailed information about the library.
     * @param text - the description of the library, may be null.
     */
    void setDescription(String text);

    /**
     * Sets the localizing bundle. The bundle is used for localizing the name and description.
     * The bundle is located using the system ClassLoader.
     * Called by LibrariesStorage while reading the library.
     * @param resourceName of the bundle without extension, may be null.
     */
    void setLocalizingBundle(String resourceName);

    /**
     * Adds PropertyChangeListener
     * @param l - the PropertyChangeListener
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes PropertyChangeListener
     * @param l - - the PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Sets content of given volume
     * @param volumeType the type of volume for which the content should be set
     * @param path the list of resource URLs
     * @throws IllegalArgumentException if the library does not support given volumeType
     */
    void setContent(String volumeType, List<URL> path) throws IllegalArgumentException;

}
