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

import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.project.libraries.LibraryAccessor;

/**
 * A factory class to create {@link Library} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link LibraryImplementation} and use this factory.
 * See also {@link org.netbeans.spi.project.libraries.support.LibrariesSupport}
 * for easier ways to create {@link LibraryImplementation}.
 * @since org.netbeans.modules.project.libraries/1 1.14
 * @author Tomas Zezula
 */
public class LibraryFactory {
    
    private LibraryFactory() {
    }
    
    
    /**
     * Creates Library for LibraryImplementation
     * @param libraryImplementation the library SPI object
     * @return Library API instance
     */
    public static Library createLibrary (LibraryImplementation libraryImplementation) {
        assert libraryImplementation != null;
        return LibraryAccessor.DEFAULT.createLibrary(libraryImplementation);
    }
    
}
