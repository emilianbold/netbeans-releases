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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

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
    
    /**
     * Properly converts possibly relative file to URL.
     * @param f file to convert; can be relative; cannot be null
     * @return url
     * @since org.netbeans.modules.project.libraries/1 1.17
     */
    public static URL convertFilePathToURL(String path) {
        try {
            File f = new File(path);
            if (f.isAbsolute()) {
                return f.toURI().toURL();
            } else {
                // create hierarchical relative URI (that is no schema)
                // to encode OS characters
                URI uri = new URI(null, null, path.replace('\\', '/'), null);
                return new URL("file", null, uri.getRawPath());
            }

        } catch (URISyntaxException ex) {
	    IllegalArgumentException y = new IllegalArgumentException();
	    y.initCause(ex);
	    throw y;
        } catch (MalformedURLException ex) {
	    IllegalArgumentException y = new IllegalArgumentException();
	    y.initCause(ex);
	    throw y;
        }
    }
    
    /**
     * Properly converts possibly relative URL to file.
     * @param url file URL to convert; can be relative; cannot be null
     * @return url
     * @since org.netbeans.modules.project.libraries/1 1.17
     */
    public static String convertURLToFilePath(URL url) {
        if (!"file".equals(url.getProtocol())) {
            throw new IllegalArgumentException("not file URL "+url); //NOI18N
        }
        try {
            if (isAbsoluteURL(url)) {
                return new File(new URI(url.toExternalForm())).getPath();
            } else {
                // workaround to decode URL path - created fake absolute URI 
                // just to construct File instance and properly decoded path:
                URI uri3 = new URI("file:/"+url.getPath());
                return new File(uri3).getPath().substring(1);
            }
        } catch (URISyntaxException ex) {
	    IllegalArgumentException y = new IllegalArgumentException();
	    y.initCause(ex);
	    throw y;
        }
    }

    /**
     * Is given URL absolute?
     * 
     * @param url url to test; cannot be null
     * @return is absolute
     * @since org.netbeans.modules.project.libraries/1 1.17
     */
    public static boolean isAbsoluteURL(URL url) {
        if ("jar".equals(url.getProtocol())) { // NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return url.getPath().startsWith("/");
    }
    
    /**
     * Helper method to resolve (possibly relative) library content URL to FileObject.
     * 
     * @param libraryLocation library location file; can be null for global libraries
     * @param libraryEntry library entry to resolve
     * @return file object
     * @since org.netbeans.modules.project.libraries/1 1.17
     */
    public static FileObject resolveLibraryEntryFileObject(URL libraryLocation, URL libraryEntry) {
        URL u = resolveLibraryEntryURL(libraryLocation, libraryEntry);
        return URLMapper.findFileObject(u);
    }
    
    /**
     * Helper method to resolve (possibly relative) library content URL.
     * 
     * @param libraryLocation library location file; can be null for global libraries
     * @param libraryEntry library entry to resolve
     * @return absolute URL
     * @since org.netbeans.modules.project.libraries/1 1.17
     */
    public static URL resolveLibraryEntryURL(URL libraryLocation, URL libraryEntry) {
        Parameters.notNull("libraryEntry", libraryEntry); //NOI18N
        if (isAbsoluteURL(libraryEntry)) {
            return libraryEntry;
        } else {
            if (libraryLocation == null) {
                throw new IllegalArgumentException("cannot resolve relative URL without library location"); //NOI18N
            }
            if (!"file".equals(libraryLocation.getProtocol())) { //NOI18N
                throw new IllegalArgumentException("not file: protocol - "+libraryLocation.toExternalForm()); //NOI18N
            }
            File libLocation = new File(URI.create(libraryLocation.toExternalForm()));
            if (!libLocation.getName().endsWith(".properties")) { //NOI18N
                throw new IllegalArgumentException("library location must be a file - "+libraryLocation.toExternalForm()); //NOI18N
            }
            File libBase = libLocation.getParentFile();
            String jarFolder = null;
            if ("jar".equals(libraryEntry.getProtocol())) {
                assert libraryEntry.toExternalForm().indexOf("!/") != -1 : libraryEntry.toExternalForm(); //NOI18N
                jarFolder = libraryEntry.toExternalForm().substring(libraryEntry.toExternalForm().indexOf("!/")+2); //NOI18N
                libraryEntry = FileUtil.getArchiveFile(libraryEntry);
            }
            String path = convertURLToFilePath(libraryEntry);
            File f = FileUtil.normalizeFile(new File(libBase, path));
            URL u;
            try {
                u = f.toURI().toURL();
            } catch (MalformedURLException ex) {
                IllegalArgumentException y = new IllegalArgumentException();
                y.initCause(ex);
                throw y;
            }
            if (jarFolder != null) {
                u = FileUtil.getArchiveRoot(u);
                try {
                    u = new URL(u + jarFolder.replace('\\', '/')); //NOI18N
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }
            return u;
        }
    }

    // TODO: add method which compares two libraries: compare content and file sizes and ...
    
    // TODO: move some of these methods to openide.FileUtil
}
