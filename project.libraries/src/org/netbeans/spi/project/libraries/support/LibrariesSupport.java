/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
