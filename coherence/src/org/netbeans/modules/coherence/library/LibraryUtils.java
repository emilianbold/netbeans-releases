/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.library;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.util.Version;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class LibraryUtils {

    private static final Logger LOGGER = Logger.getLogger(LibraryUtils.class.getName());

    public static final String LIBRARY_BASE_NAME = "Coherence"; //NOI18N
    public static final String COHERENCE_CLASS_NAME = "com.tangosol.net.DefaultCacheServer"; //NOI18N

    /**
     * Suggests Coherence library name.
     *
     * @param version Coherence server version
     * @return Coherence library name
     */
    public static String getCoherenceLibraryDisplayName(Version version) {
        if (version == null) {
            return LIBRARY_BASE_NAME;
        }

        StringBuilder nameSB = new StringBuilder(LIBRARY_BASE_NAME);
        nameSB.append(" ").append(version.getReleaseVersion()); //NOI18N
        return nameSB.toString().trim();
    }

    /**
     * Creates new Coherence library if not exists library of the same name.
     *
     * @param libraryDisplayName display name of the Coherence library (library name is parsed from it)
     * @param serverRoot directory root of Coherence server
     * @return {@code true} if new library was created in the IDE, {@code false} otherwise
     */
    protected static boolean registerCoherenceLibrary(String libraryDisplayName, File serverRoot) {
        String libraryName = parseLibraryName(libraryDisplayName);
        if (LibraryManager.getDefault().getLibrary(libraryName) != null) {
            return false;
        }

        URI coherenceServerURI = CoherenceProperties.getCoherenceJar(serverRoot).toURI();
        Map<String, List<URI>> content = new HashMap<String, List<URI>>();
        content.put("classpath", Collections.<URI>singletonList(coherenceServerURI)); //NOI18N
        File coherenceDocDir = CoherenceProperties.getCoherenceJavadocDir(serverRoot);
        if (coherenceDocDir != null) {
            content.put("javadoc", Collections.<URI>singletonList(coherenceDocDir.toURI())); //NOI18N
        }
        try {
            LibraryManager.getDefault().createURILibrary(
                    "j2se", //NOI18N
                    libraryName,
                    libraryDisplayName,
                    NbBundle.getMessage(LibraryUtils.class, "DESC_CoherenceLibraryDescription"), //NOI18N
                    content);
            LOGGER.log(Level.FINE, "Created Coherence library: name={0}, displayName={1}, cp={2}.",
                    new Object[]{libraryName, libraryDisplayName, coherenceServerURI.toString()});
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Creates Coherence library with JAR from given server root.
     * Also informs about creation result users.
     *
     * @param serverRoot Coherence server root
     */
    public static void createCoherenceLibrary(File serverRoot) {
        // create coherence library if not exists in this version yet
        Version coherenceVersion = CoherenceProperties.getServerVersion(serverRoot);
        String libraryName = LibraryUtils.getCoherenceLibraryDisplayName(coherenceVersion);
        if (LibraryUtils.registerCoherenceLibrary(libraryName, serverRoot)) {
            LOGGER.log(
                    Level.INFO,
                    "Coherence library created; libraryDisplayName={0}, version={1}", //NOI18N
                    new Object[]{libraryName, coherenceVersion});
        }
    }

    /**
     * Says whether the given library is Coherence one or not.
     * @param libraryContent content of library
     * @return {@code true} if the library is Coherence library, {@code false} otherwise
     */
    public static boolean isCoherenceLibrary(List<URL> libraryContent) {
        try {
            if (!containsClass(libraryContent, COHERENCE_CLASS_NAME)) {
                return false;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Gets all Coherence libraries registered in the NetBeans IDE.
     * @return {@code List} of all registered Coherence libraries
     */
    public static List<Library> getRegisteredCoherenceLibraries() {
        List<Library> coherenceLibraries = new ArrayList<Library>();
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        for (Library library : libraries) {
            if (!"j2se".equals(library.getType())) { //NOI18N
                continue;
            }
            List<URL> content = library.getContent("classpath"); //NOI18N
            if (LibraryUtils.isCoherenceLibrary(content)) {
                coherenceLibraries.add(library);
            }
        }
        return coherenceLibraries;
    }

    /**
     * Gets library name for library display name.
     * @param displayName display name processed for getting the name
     * @return library name
     */
    protected static String parseLibraryName(String displayName) {
        return displayName.replace(" ", "-").toLowerCase(); //NOI18N
    }

    // Copied from j2ee.common module
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     *
     * @param classpath consists of jar urls and folder urls containing classes
     * @param className the name of the class
     *
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     *
     * @throws IOException if an I/O error has occurred
     */
    private static boolean containsClass(List<URL> classPath, String className) throws IOException {
        Parameters.notNull("classpath", classPath); //NOI18N
        Parameters.notNull("className", className); //NOI18N

        List<File> diskFiles = new ArrayList<File>();
        for (URL url : classPath) {
            URL archiveURL = FileUtil.getArchiveFile(url);

            if (archiveURL != null) {
                url = archiveURL;
            }

            if ("nbinst".equals(url.getProtocol())) { //NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo != null) {
                    URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                    if (localURL != null) {
                        url = localURL;
                    }
                }
            }

            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                File diskFile = FileUtil.toFile(fo);
                if (diskFile != null) {
                    diskFiles.add(diskFile);
                }
            }
        }

        return containsClass(diskFiles, className);
    }

    // Copied from j2ee.common module
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     *
     * @param classpath consists of jar files and folders containing classes
     * @param className the name of the class
     *
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     *
     * @throws IOException if an I/O error has occurred
     */
    public static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath); //NOI18N
        Parameters.notNull("driverClassName", className); //NOI18N
        String classFilePath = className.replace('.', '/') + ".class"; //NOI18N
        for (File file : classpath) {
            if (file.isFile()) {
                JarFile jf = new JarFile(file);
                try {
                    Enumeration entries = jf.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    jf.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }
}
