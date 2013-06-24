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
package org.netbeans.modules.web.clientproject.api;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.util.JsLibUtilities;
import org.netbeans.modules.web.clientproject.libraries.CDNJSLibrariesProvider;
import org.netbeans.modules.web.clientproject.libraries.EnhancedLibraryProvider;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;


/**
 * Manager for web client libraries.
 * <p>
 * Rewritten since 1.22.
 * @see JsLibUtilities
 */
public final class WebClientLibraryManager {

    private static final Logger LOGGER = Logger.getLogger(WebClientLibraryManager.class.getName());

    /**
     * Library TYPE.
     */
    public static final String TYPE = "javascript"; // NOI18N

    /**
     * Volume for regular JS files.
     */
    public static final String VOL_REGULAR = "regular"; // NOI18N

    /**
     * Volume for minified JS files.
     */
    public static final String VOL_MINIFIED = "minified"; // NOI18N

    /**
     * Volume for documented JS files.
     */
    public static final String VOL_DOCUMENTED = "documented"; // NOI18N

    /**
     * Real name of the library, that is without CND source prefix .
     */
    public static final String PROPERTY_REAL_NAME = "name"; // NOI18N

    /**
     * Real display name of the library, that is without CND source prefix and without version in the name.
     */
    public static final String PROPERTY_REAL_DISPLAY_NAME = "displayname"; // NOI18N

    /**
     * Name of CDN this library is comming from.
     */
    public static final String PROPERTY_CDN = "cdn"; // NOI18N

    /**
     * Homepage of the library.
     */
    public static final String PROPERTY_SITE = "site"; // NOI18N

    /**
     * Library version.
     */
    public static final String PROPERTY_VERSION = "version"; // NOI18N

    /**
     * Property that is fired if libraries change. It delegates on {@link LibraryProvider#PROP_LIBRARIES}.
     */
    public static final String PROPERTY_LIBRARIES = LibraryProvider.PROP_LIBRARIES;

    /**
     * Default relative path for libraries folder.
     */
    public static final String LIBS = "js/libs";       // NOI18N

    private static final WebClientLibraryManager INSTANCE = WebClientLibraryManager.create();

    private final PropertyChangeListener libraryChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (LibraryProvider.PROP_LIBRARIES.equals(evt.getPropertyName())) {
                resetLibraries();
            }
        }
    };
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    // @GuardedBy("this")
    private List<Library> libraries = null;


    private WebClientLibraryManager() {
    }

    private static WebClientLibraryManager create() {
        WebClientLibraryManager webClientLibraryManager = new WebClientLibraryManager();
        // listeners
        for (EnhancedLibraryProvider<LibraryImplementation> libraryProvider : getLibraryProviders()) {
            libraryProvider.addPropertyChangeListener(webClientLibraryManager.libraryChangeListener);
        }
        return webClientLibraryManager;
    }

    private static List<EnhancedLibraryProvider<LibraryImplementation>> getLibraryProviders() {
        return Collections.<EnhancedLibraryProvider<LibraryImplementation>>singletonList(CDNJSLibrariesProvider.getDefault());
    }

    /**
     * Gets default instance of manager for web client libraries.
     * @return default instance of manager for web client libraries
     */
    public static WebClientLibraryManager getDefault() {
        return INSTANCE;
    }

    /**
     * Adds property change listener.
     * @param listener listener to be added, can be {@code null}
     */
    public void addPropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes property change listener.
     * @param listener listener to be removed, can be {@code null}
     */
    public void removePropertyChangeListener(@NullAllowed PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Returns all JavaScript libraries. They are not registered in global libraries
     * repository for now.
     */
    public synchronized List<Library> getLibraries() {
        assert Thread.holdsLock(this);
        if (libraries == null) {
            List<Library> libs2 = new ArrayList<>();
            for (EnhancedLibraryProvider<LibraryImplementation> libraryProvider : getLibraryProviders()) {
                addLibraries(libs2, libraryProvider);
            }
            libraries = new CopyOnWriteArrayList<>(libs2);
        }
        return libraries;
    }

    void resetLibraries() {
        synchronized (this) {
            assert Thread.holdsLock(this);
            libraries = null;
        }
        propertyChangeSupport.firePropertyChange(PROPERTY_LIBRARIES, null, null);
    }

    /**
     * Update web client libraries.
     * <p>
     * This method must be called only in a background thread. To cancel the update, interrupt the current thread.
     * @throws NetworkException if any network error occurs
     * @throws IOException if any error occurs
     * @throws InterruptedException if the update is cancelled
     * @since 1.25
     */
    public void updateLibraries() throws NetworkException, IOException, InterruptedException {
        updateLibraries(false);
    }

    /**
     * Update web client libraries with possibly showing its progress.
     * <p>
     * This method must be called only in a background thread. To cancel the update, interrupt the current thread.
     * @param showProgress whether to show progress or not
     * @throws NetworkException if any network error occurs
     * @throws IOException if any error occurs
     * @throws InterruptedException if the update is cancelled
     * @since 1.25
     */
    @NbBundle.Messages("WebClientLibraryManager.progress.update=Updating JavaScript libraries...")
    public void updateLibraries(boolean showProgress) throws NetworkException, IOException, InterruptedException {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Cannot run in UI thread");
        }
        ProgressHandle progressHandle = null;
        if (showProgress) {
            final Thread downloadThread = Thread.currentThread();
            progressHandle = ProgressHandleFactory.createHandle(Bundle.WebClientLibraryManager_progress_update(), new Cancellable() {
                @Override
                public boolean cancel() {
                    downloadThread.interrupt();
                    return true;
                }
            });
            progressHandle.start();
        }
        try {
            for (EnhancedLibraryProvider<LibraryImplementation> libraryProvider : getLibraryProviders()) {
                libraryProvider.updateLibraries(progressHandle);
            }
        } finally {
            if (progressHandle != null) {
                progressHandle.finish();
            }
        }
    }

    /**
     * Get last time of JS libraries update.
     * <p>
     * This method returns exactly one time which represents <b>last (newest) successful update of any
     * JavaScript library</b>. In other words, for more JS libraries, if JS library <i>A</i> is successfully
     * updated but JS library <i>B</i> is not, the time of the JS library <i>A</i> is returned.
     * @return last time of JS libraries update; can be {@code null} if not updated yet or the time cannot be retrieved
     * @since 1.32
     */
    @CheckForNull
    public FileTime getLibrariesLastUpdatedTime() {
        FileTime updateTime = null;
        for (EnhancedLibraryProvider<LibraryImplementation> libraryProvider : getLibraryProviders()) {
            FileTime libraryUpdateTime = libraryProvider.getLibrariesLastUpdatedTime();
            if (libraryUpdateTime == null) {
                continue;
            }
            if (updateTime == null
                    || updateTime.compareTo(libraryUpdateTime) > 0) {
                updateTime = libraryUpdateTime;
            }
        }
        return updateTime;
    }

    private void addLibraries(List<Library> libs, LibraryProvider<LibraryImplementation> provider) {
        for (LibraryImplementation li : provider.getLibraries()) {
            libs.add(LibraryFactory.createLibrary(li));
        }
    }

    /**
     * Finds library with the specified <code>name</code> and <code>version</code>.
     * <code>version</code> could be null. In the latter case most recent version
     * will be returned.
     * @param name library name
     * @param version library version
     * @return library
     */
    public Library findLibrary(String name, String version) {
        SpecificationVersion lastVersion = null;
        Library lib = null;
        for (Library library : getLibraries()) {
            if (library.getType().equals(TYPE)) {
                String libName = library.getProperties().get(PROPERTY_REAL_NAME);
                String libVersion = library.getProperties().get(PROPERTY_VERSION);
                if (name.equals(libName)) {
                    if (version != null
                            && version.equals(libVersion)) {
                        return library;
                    }
                    int index = libVersion.indexOf(' '); // NOI18N
                    if (index != -1) {
                        libVersion = libVersion.substring(0, index);
                    }
                    try {
                        SpecificationVersion specVersion = new SpecificationVersion(libVersion);
                        if (lastVersion == null
                                || specVersion.compareTo(lastVersion) > 0) {
                            lastVersion = specVersion;
                            lib = library;
                        }
                    } catch(NumberFormatException e) {
                        continue;
                    }
                }
            }
        }
        return lib;
    }

    /**
     * Get all versions of library with name <code>libraryName</code>
     * @param libraryName library name
     * @return all version of library
     */
    public String[] getVersions(String libraryName) {
        List<String> result = new LinkedList<>();
        for (Library library : getLibraries()) {
            if (library.getType().equals(TYPE)) {
                String libName = library.getProperties().get(PROPERTY_REAL_NAME);
                if (libName.equals(libraryName)) {
                    String libVersion = library.getProperties().get(PROPERTY_VERSION);
                    int index = libVersion.indexOf(' '); // NOI18N
                    if (index != -1) {
                        libVersion = libVersion.substring(0, index);
                    }
                    result.add(libVersion);
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Get relative file paths of the given library and the given volume.
     * @param library library to get file paths for
     * @param volume volume, can be {@code null}
     * @return list of relative file paths of the given library and the given volume
     */
    public List<String> getLibraryFilePaths(@NonNull Library library, @NullAllowed String volume) {
        String libRootName = getLibraryRootName(library);
        List<URL> urls = getLibraryUrls(library, volume);
        List<String> filePaths = new ArrayList<>(urls.size());
        for (URL url : urls) {
            StringBuilder sb = new StringBuilder(30);
            sb.append(libRootName);
            sb.append('/'); // NOI18N
            sb.append(getLibraryFilePath(url));
            filePaths.add(sb.toString());
        }
        return filePaths;
    }

    private static String getLibraryRootName(Library library) {
        return library.getProperties()
                .get(PROPERTY_REAL_NAME).replace(' ', '-') // NOI18N
                + '-' // NOI18N
                + library.getProperties().get(PROPERTY_VERSION);
    }

    private static String getLibraryRootURL(Library library) {
        return CDNJSLibrariesProvider.getLibraryRootURL(library.getProperties().get(PROPERTY_REAL_NAME),
                library.getProperties().get(PROPERTY_VERSION));
    }

    private static List<URL> getLibraryUrls(Library library, String volume) {
        List<URL> urls;
        if (volume != null) {
            urls = library.getContent(volume);
        } else {
            urls = library.getContent(VOL_MINIFIED);
            if (urls.isEmpty()) {
                urls = library.getContent(VOL_REGULAR);
            }
            if (urls.isEmpty()) {
                urls = library.getContent(VOL_DOCUMENTED);
            }
        }
        assert !urls.isEmpty() : "Library should not be empty: " + library + " " + volume;
        return urls;
    }

    private static String getLibraryFilePath(URL url) {
        String name = url.getPath();
        return name.substring(name.lastIndexOf('/') + 1);
    }

    /**
     * Add libraries to the given {@ code folder}. If {@code volume} is {@code null},
     * undefined available volume will be used.
     * <p>
     * <b>Note:</b> Instead of this method, one might want to use
     * {@link JsLibUtilities#applyJsLibraries(java.util.List, java.lang.String, org.openide.filesystems.FileObject, org.netbeans.api.progress.ProgressHandle)}.
     * @param libraries libraries to add
     * @param folder directory in the project where libraries should be added
     * @param volume library volume, can be {@code null}
     * @return list of added files
     * @since 1.34
     */
    public List<FileObject> addLibraries(Library[] libraries, FileObject folder, String volume) throws IOException, MissingLibResourceException {
        boolean anyMissingFiles = false;
        List<FileObject> allCreatedFiles = new ArrayList<>();
        List<FileObject> libraryCreatedFiles = new ArrayList<>();
        for (Library library : libraries) {
            libraryCreatedFiles.clear();
            boolean libraryMissingFiles = false;
            String libRootName = getLibraryRootName(library);
            FileObject libRoot = folder.getFileObject(libRootName);
            if (libRoot == null) {
                libRoot = folder.createFolder(libRootName);
            } else if (libRoot.isData()) {
                throw new IOException("File '" + libRootName + "' already exists and is not a folder");
            }
            List<URL> urls = getLibraryUrls(library, volume);
            String rootURL = getLibraryRootURL(library);
            for (URL url : urls) {
                FileObject destinationFolder = getDestinationFolder(libRoot, url, rootURL);
                FileObject fileObject = copySingleFile(url, getLibraryFilePath(url), destinationFolder);
                if (fileObject == null) {
                    libraryMissingFiles = true;
                    break;
                } else {
                    libraryCreatedFiles.add(fileObject);
                }
            }
            if (libraryMissingFiles) {
                anyMissingFiles = true;
                for (FileObject fileObject : libraryCreatedFiles) {
                    silentDelete(fileObject);
                }
            } else {
                allCreatedFiles.addAll(libraryCreatedFiles);
            }
            // possible cleanup
            if (libRoot.getChildren().length == 0) {
                silentDelete(libRoot);
            }
        }
        if (anyMissingFiles) {
            throw new MissingLibResourceException(allCreatedFiles);
        }
        return allCreatedFiles;
    }

    private void silentDelete(FileObject fileObject) {
        try {
            fileObject.delete();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot delete file " + fileObject, ex);
        }
    }

    private FileObject getDestinationFolder(FileObject libRoot, URL url, String rootURL) throws IOException {
        if (rootURL == null) {
            return libRoot;
        }
        String s = WebUtils.urlToString(url);
        if (!s.startsWith(rootURL)) {
            return libRoot;
        }
        s = s.substring(rootURL.length());
        int index = s.lastIndexOf('/');
        if (index == -1) {
            return libRoot;
        } else {
            s = s.substring(0, index);
            return FileUtil.createFolder(libRoot, s);
        }
    }

    private static FileObject copySingleFile(URL url, String name, FileObject libRoot) {
        FileObject fo = null;
        try {
            int timeout = 15000; // default timeout
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            try (InputStream is = connection.getInputStream()) {
                fo = libRoot.createData(name);
                try (OutputStream os = fo.getOutputStream()) {
                    FileUtil.copy(is, os);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return fo;
    }

}
