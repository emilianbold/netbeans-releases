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
package org.netbeans.spi.project.libraries.support;

import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;

/**
 * SPI Support class.
 * Provides factory method for creating instance of the default LibraryImplementation.
 */
public final class LibrariesSupport {

    private LibrariesSupport () {
    }


    /**
     * Creates default LibraryImplementation
     * @param libraryType type of library
     * @param volumeTypes types of supported volumes
     * @return LibraryImplementation, never return null
     */
    public static LibraryImplementation createLibraryImplementation (String libraryType, String[] volumeTypes) {
        return new DefaultLibraryImplementation (libraryType, volumeTypes);
    }
    
    /**
     * Returns registered {@link LibraryTypeProvider} for given library type. This method 
     * is mostly used by {@link org.netbeans.spi.project.libraries.LibraryProvider} implementators.
     * @param libraryType  the type of library for which the provider should be returned.
     * @return {@link LibraryTypeProvider} for given library type or null, if none is registered.
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public static LibraryTypeProvider getLibraryTypeProvider (String libraryType) {
        return LibraryTypeRegistry.getDefault().getLibraryTypeProvider(libraryType);
    }
    
    /**
     * Returns all registered {@link LibraryTypeProvider}s. This method 
     * is mostly used by {@link org.netbeans.spi.project.libraries.LibraryProvider} implementators.
     * @return an array of {@link LibraryTypeProvider}, never returns null.
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public static LibraryTypeProvider[] getLibraryTypeProviders () {
        return LibraryTypeRegistry.getDefault().getLibraryTypeProviders();
    }
}
