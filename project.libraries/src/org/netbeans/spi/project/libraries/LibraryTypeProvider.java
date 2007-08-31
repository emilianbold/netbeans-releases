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

import java.beans.Customizer;
import org.openide.util.Lookup;

/**
 * SPI interface for provider of library type.
 * The LibraryTypeProvider is responsible for creating new libraries of given type
 * and for supplying the customizers of library's volumes.
 */
public interface LibraryTypeProvider extends Lookup.Provider {

    /**
     * Returns the UI name of the LibraryType.
     * This name is used in the UI while the libraryType is used as a system
     * identifier.
     * @return String the display name
     */
    public String getDisplayName ();

    /**
     * Get a unique identifier for the library type.
     * For example, <code>j2se</code>.
     * @return the unique library type identifier, never null
     */
    public String getLibraryType ();

    /**
     * Get identifiers for the volume types supported by the libraries created by this provider.
     * For example, <code>classpath</code>, <code>javadoc</code>, or <code>src</code>.
     * @return support volume type identifiers, never null, may be an empty array.
     */
    public String[] getSupportedVolumeTypes ();

    /**
     * Creates a new empty library implementation.
     * @return the created library model, never null
     */
    public LibraryImplementation createLibrary ();


    /**
     * This method is called by the libraries framework when the library was deleted.
     * If the LibraryTypeProvider implementation requires clean of
     * additional settings (e.g. remove properties in the build.properties)
     * it should be done in this method.
     * @param libraryImpl
     */
    public void libraryDeleted (LibraryImplementation libraryImpl);


    /**
     * This method is called by the libraries framework when the library was created
     * and fully initialized (all its properties have to be read).
     * If the LibraryTypeProvider implementation requires initialization of
     * additional settings (e.g. adding properties into the build.properties)
     * it should be done in this method.
     *
     */
    public void libraryCreated (LibraryImplementation libraryImpl);

    /**
     * Returns customizer for given volume's type, or null if the volume is not customizable.
     * The object of the LibraryImplementation type is
     * passed to the customizer's setObject method.
     * The customized object describes the library created by this
     * provider, but the customizer cannot assume that the customized
     * object is of the same type as the object created by {@link #createLibrary}.
     * @param volumeType a type of volume listed in {@link #getSupportedVolumeTypes}
     * @return a customizer (must extend {@link javax.swing.JComponent}) or null if such
     *  customizer doesn't exist.
     */
    public Customizer getCustomizer (String volumeType);
    
}
