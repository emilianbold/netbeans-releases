/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * SPI Support class.
 * Provides factory method for creating instance of the default LibraryImplementation.
 */
public final class LibrariesSupport {

    private LibrariesSupport () {
    }

    /**
     * Creates default {@link LibraryImplementation3}
     * @param libraryType type of library
     * @param volumeTypes types of supported volumes
     * @return LibraryImplementation3
     * @since 1.39
     */
    @NonNull
    public static LibraryImplementation3 createLibraryImplementation3 (
            @NonNull final String libraryType,
            @NonNull final String... volumeTypes) {
        return new DefaultLibraryImplementation (libraryType, volumeTypes);
    }

    /**
     * Creates default LibraryImplementation
     * @param libraryType type of library
     * @param volumeTypes types of supported volumes
     * @return LibraryImplementation, never return null
     */
    public static LibraryImplementation createLibraryImplementation (String libraryType, String[] volumeTypes) {
        return createLibraryImplementation3(libraryType, volumeTypes);
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
    
    /**
     * Properly converts possibly relative file path to URI.
     * @param path file path to convert; can be relative; cannot be null
     * @return uri
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI convertFilePathToURI(final @NonNull String path) {
        Parameters.notNull("path", path);   //NOI18N
        try {
            File f = new File(path);
            if (f.isAbsolute()) {
                return Utilities.toURI(f);
            } else {
                // create hierarchical relative URI (that is no schema)
                return new URI(null, null, path.replace('\\', '/'), null);
            }

        } catch (URISyntaxException ex) {
	    IllegalArgumentException y = new IllegalArgumentException();
	    y.initCause(ex);
	    throw y;
        }
    }
    
    /**
     * Properly converts possibly relative URI to file path.
     * @param uri URI convert; can be relative URI; cannot be null
     * @return file path
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static String convertURIToFilePath(URI uri) {
        if (uri.isAbsolute()) {
            return Utilities.toFile(uri).getPath();
        } else {
            return uri.getPath().replace('/', File.separatorChar);
        }
    }
    
    /**
     * Helper method to resolve (possibly relative) library content URI to FileObject.
     * 
     * @param libraryLocation library location file; can be null for global libraries
     * @param libraryEntry library entry to resolve
     * @return file object
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static FileObject resolveLibraryEntryFileObject(URL libraryLocation, URI libraryEntry) {
        URI u = resolveLibraryEntryURI(libraryLocation, libraryEntry);
        try {
            return URLMapper.findFileObject(u.toURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    /**
     * Helper method to resolve (possibly relative) library content URI.
     * 
     * @param libraryLocation library location file
     * @param libraryEntry relative library entry to resolve
     * @return absolute URI
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI resolveLibraryEntryURI(URL libraryLocation, URI libraryEntry) {
        Parameters.notNull("libraryEntry", libraryEntry); //NOI18N
        if (libraryEntry.isAbsolute()) {
            return libraryEntry;
        } else {
            if (libraryLocation == null) {
                throw new IllegalArgumentException("cannot resolve relative URL without library location"); //NOI18N
            }
            if (!"file".equals(libraryLocation.getProtocol())) { //NOI18N
                throw new IllegalArgumentException("not file: protocol - "+libraryLocation.toExternalForm()); //NOI18N
            }
            if (!libraryLocation.getPath().endsWith(".properties")) { //NOI18N
                throw new IllegalArgumentException("library location must be a file - "+libraryLocation.toExternalForm()); //NOI18N
            }
            URI resolved;
            try {
                resolved = libraryLocation.toURI().resolve(libraryEntry);
            } catch (URISyntaxException x) {
                throw new AssertionError(x);
            }
            if (libraryEntry.getPath().contains("!/")) {
                return URI.create("jar:" + resolved);
            } else {
                return resolved;
            }
        }
    }
    
    /**
     * Returns the URI of the archive file containing the file
     * referred to by a <code>jar</code>-protocol URL.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * @param uri a URI; can be relative URI
     * @return the embedded archive URI, or null if the URI is not a
     *         <code>jar</code>-protocol URI containing <code>!/</code>
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI getArchiveFile(URI uri) {
        String u = uri.toString();
        int index = u.indexOf("!/"); //NOI18N
        if (index != -1) {
            try {
                return new URI(u.substring(u.startsWith("jar:") ? 4 : 0, index)); // NOI18N
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
        }
        return null;
    }
    
    /**
     * Returns a URI representing the root of an archive.
     * @param uri of a ZIP- (or JAR-) format archive file; can be relative
     * @return the <code>jar</code>-protocol URI of the root of the archive
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public static URI getArchiveRoot(URI uri) {
        assert !uri.toString().contains("!/") : uri;
        try {
            return new URI((uri.isAbsolute() ? "jar:" : "") + uri.toString() + "!/"); // NOI18N
        } catch (URISyntaxException ex) {
                throw new AssertionError(ex);
        }
    }
    
}
