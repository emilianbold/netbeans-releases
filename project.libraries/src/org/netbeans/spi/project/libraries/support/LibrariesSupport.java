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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.netbeans.modules.project.libraries.LibrariesModule;
import org.netbeans.modules.project.libraries.LibraryTypeRegistry;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * SPI Support class.
 * Provides factory method for creating instance of the default LibraryImplementation.
 * @author David Konecny
 * @author Tomas Zezula
 */
public final class LibrariesSupport {

    private static final Logger LOG = Logger.getLogger(LibrariesSupport.class.getName());

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
                return BaseUtilities.toURI(f);
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
            return BaseUtilities.toFile(uri).getPath();
        } else {
            return uri.getPath().replace('/', File.separatorChar);
        }
    }

    /**
     * Converts a list of {@link URI}s into a list of {@link URL}s.
     * The unmappable URIs are omitted from result.
     * @param uris the list of {@link URI}s to be converted
     * @return the list of {@link URL}s
     * @since 1.48
     */
    public static List<URL> convertURIsToURLs(List<? extends URI> uris) {
        List<URL> content = new ArrayList<>();
        for (URI uri : uris) {
            try {
                content.add(uri.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return content;
    }

    /**
     * Converts a list of {@link URL}s into a list of {@link URI}s.
     * The unmappable URLs are omitted from result.
     * @param urls the list of {@link URL}s to be converted
     * @return the list of {@link URI}s
     * @since 1.48
     */
    public static List<URI> convertURLsToURIs(List<URL> urls) {
        List<URI> content = new ArrayList<>();
        for (URL url : urls) {
            final URI uri = URI.create(url.toExternalForm());
            if (uri != null) {
                content.add(uri);
            }
        }
        return content;
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

    @NonNull
    public static String getLocalizedName(@NonNull final LibraryImplementation impl) {
        Parameters.notNull("impl", impl);   //NOI18N
        if (supportsDisplayName(impl) && ((NamedLibraryImplementation)impl).getDisplayName() != null) {
            return ((NamedLibraryImplementation)impl).getDisplayName();
        }
        final FileObject src = LibrariesModule.getFile(impl);
        if (src != null) {
            Object obj = src.getAttribute("displayName"); // NOI18N
            if (obj instanceof String) {
                return (String)obj;
            }
        }
        if (impl instanceof ForwardingLibraryImplementation) {
            String proxiedName = getLocalizedName(((ForwardingLibraryImplementation)impl).getDelegate());
            if (proxiedName != null) {
                return proxiedName;
            }
        }

        return getLocalizedString(impl.getLocalizingBundle(), impl.getName());
    }

    public static boolean supportsDisplayName(final @NonNull LibraryImplementation impl) {
        assert impl != null;
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsDisplayName(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof NamedLibraryImplementation;
    }

    public static @CheckForNull String getDisplayName (final @NonNull LibraryImplementation impl) {
        return supportsDisplayName(impl) ?
                ((NamedLibraryImplementation)impl).getDisplayName() :
                null;
    }

    public static boolean setDisplayName(
            final @NonNull LibraryImplementation impl,
            final @NullAllowed String name) {
        if (supportsDisplayName(impl)) {
            final NamedLibraryImplementation nimpl = (NamedLibraryImplementation) impl;
            if (!BaseUtilities.compareObjects(nimpl.getDisplayName(), name)) {
                nimpl.setDisplayName(name);
                return true;
            }
        }
        return false;
    }

    public static boolean supportsProperties(final @NonNull LibraryImplementation impl) {
        assert impl != null;
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsProperties(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof LibraryImplementation3;
    }

    @NonNull
    public static Map<String,String> getProperties (final @NonNull LibraryImplementation impl) {
        return supportsProperties(impl) ?
                ((LibraryImplementation3)impl).getProperties() :
                Collections.<String,String>emptyMap();
    }

    public static boolean setProperties(
        final @NonNull LibraryImplementation impl,
        final @NonNull Map<String,String>  props) {
        if (supportsProperties(impl)) {
            final LibraryImplementation3 impl3 = (LibraryImplementation3)impl;
            if (!BaseUtilities.compareObjects(impl3.getProperties(), props)) {
                impl3.setProperties(props);
                return true;
            }
        }
        return false;
    }

    public static boolean supportsURIContent(@NonNull final LibraryImplementation impl) {
        if (impl instanceof ForwardingLibraryImplementation) {
            return supportsURIContent(((ForwardingLibraryImplementation)impl).getDelegate());
        }
        return impl instanceof LibraryImplementation2;
    }

    @NonNull
    public static List<URI> getURIContent(
        @NonNull final LibraryImplementation impl,
        @NonNull final String volumeType) throws IllegalArgumentException {
        return supportsURIContent(impl) ?
            ((LibraryImplementation2)impl).getURIContent(volumeType) :
            convertURLsToURIs(impl.getContent(volumeType));
    }

    public static boolean setURIContent(
        @NonNull final LibraryImplementation impl,
        @NonNull final String volumeType,
        @NonNull final List<URI> path) {
        if (supportsURIContent(impl)) {
            final LibraryImplementation2 impl2 = (LibraryImplementation2)impl;
            if (!BaseUtilities.compareObjects(impl2.getURIContent(volumeType), path)) {
                impl2.setURIContent(volumeType, path);
                return true;
            }
        } else {
            impl.setContent(volumeType, convertURIsToURLs(path));
            return true;
        }
        return false;
    }

    private static String getLocalizedString (
            final @NullAllowed String bundleResourceName,
            final @NullAllowed String key) {
        if (key == null) {
            return null;
        }
        if (bundleResourceName == null) {
            return key;
        }
        final ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleResourceName);
        } catch (MissingResourceException mre) {
            // Bundle should have existed.
            LOG.log(Level.INFO, "Wrong resource bundle", mre);      //NOI18N
            return key;
        }
        try {
            return bundle.getString (key);
        } catch (MissingResourceException mre) {
            // No problem, not specified.
            return key;
        }
    }
}
