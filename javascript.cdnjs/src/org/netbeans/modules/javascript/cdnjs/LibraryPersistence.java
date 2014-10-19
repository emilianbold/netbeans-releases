/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.cdnjs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Persistence of libraries.
 *
 * @author Jan Stola
 */
class LibraryPersistence {
    /** Name-space used to store the library information. */
    private static final String NAMESPACE_URI = "http://www.netbeans.org/ns/cdnjs-libraries/1"; // NOI18N
    /** Name of the root element. */
    private static final String ELEMENT_LIBRARIES = "libraries"; // NOI18N
    /** Name of the element holding information about one library. */
    private static final String ELEMENT_LIBRARY = "library"; // NOI18N
    /** Name of the element holding information about one library file. */
    private static final String ELEMENT_FILE = "file"; // NOI18N
    /** Name of the attribute storing the name of the library. */
    private static final String ATTR_LIBRARY_NAME = "name"; // NOI18N
    /** Name of the attribute storing the name of the version of the library. */
    private static final String ATTR_VERSION_NAME = "version"; // NOI18N
    /** Name of the attribute storing the relative path to the library file. */
    private static final String ATTR_FILE_PATH = "path"; // NOI18N
    /** The default instance of this class. */
    private static final LibraryPersistence DEFAULT = new LibraryPersistence();

    /**
     * Creates a new {@code LibraryPersistence}.
     */
    LibraryPersistence() {
    }

    /**
     * Returns the default instance of this class.
     * 
     * @return default instance of this class.
     */
    static LibraryPersistence getDefault() {
        return DEFAULT;
    }

    /**
     * Loads library information for the given project.
     * 
     * @param project project whose library information should be loaded.
     * @return library information for the given project.
     */
    Library.Version[] loadLibraries(Project project) {
        Library.Version[] libraries;
        AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
        Element element = config.getConfigurationFragment(ELEMENT_LIBRARIES, NAMESPACE_URI, true);
        if (element == null) {
            libraries = new Library.Version[0];
        } else{
            NodeList libraryList = element.getElementsByTagNameNS(NAMESPACE_URI, ELEMENT_LIBRARY);
            libraries = new Library.Version[libraryList.getLength()];
            for (int i=0; i<libraryList.getLength(); i++) {
                Element libraryElement = (Element)libraryList.item(i);
                libraries[i] = loadLibrary(libraryElement);
            }
        }
        return libraries;
    }

    /**
     * Loads/parses information about one library from the given DOM element.
     * 
     * @param libraryElement element to load information from.
     * @return library version corresponding to the given DOM element.
     */
    private Library.Version loadLibrary(Element libraryElement) {
        String libraryName = libraryElement.getAttribute(ATTR_LIBRARY_NAME);
        String versionName = libraryElement.getAttribute(ATTR_VERSION_NAME);
        Library library = new Library();
        library.setName(libraryName);
        Library.Version version = new Library.Version(library);
        version.setName(versionName);
        library.setVersions(new Library.Version[] { version });
        NodeList fileList = libraryElement.getElementsByTagNameNS(NAMESPACE_URI, ELEMENT_FILE);
        String[] files = new String[fileList.getLength()];
        for (int i=0; i<fileList.getLength(); i++) {
            Element fileElement = (Element)fileList.item(i);
            String path = fileElement.getAttribute(ATTR_FILE_PATH);
            files[i] = path;
        }
        version.setFiles(files);
        return version;
    }

    /**
     * Stores the information about libraries used by the given project.
     * 
     * @param project project whose library information should be stored.
     * @param libraries libraries used by the project.
     */
    void storeLibraries(Project project, Library.Version[] libraries) {
        Arrays.sort(libraries, new LibraryVersionComparator());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element librariesElement = document.createElementNS(NAMESPACE_URI, ELEMENT_LIBRARIES);
            for (Library.Version library : libraries) {
                Element libraryElement = document.createElementNS(NAMESPACE_URI, ELEMENT_LIBRARY);
                String libraryName = library.getLibrary().getName();
                libraryElement.setAttribute(ATTR_LIBRARY_NAME, libraryName);
                String versionName = library.getName();
                libraryElement.setAttribute(ATTR_VERSION_NAME, versionName);
                for (String file : library.getFiles()) {
                    Element fileElement = document.createElementNS(NAMESPACE_URI, ELEMENT_FILE);
                    fileElement.setAttribute(ATTR_FILE_PATH, file);
                    libraryElement.appendChild(fileElement);
                }
                librariesElement.appendChild(libraryElement);
            }
            AuxiliaryConfiguration config = ProjectUtils.getAuxiliaryConfiguration(project);
            config.putConfigurationFragment(librariesElement, true);
        } catch (ParserConfigurationException pcex) {
            Logger.getLogger(LibraryPersistence.class.getName()).log(Level.SEVERE,
                    "Unable to store library information!", pcex); // NOI18N
        }
    }

    /**
     * Comparator of {@code Library.Version}s.
     */
    static class LibraryVersionComparator implements Comparator<Library.Version> {
        @Override
        public int compare(Library.Version o1, Library.Version o2) {
            String name1 = o1.getLibrary().getName();
            String name2 = o2.getLibrary().getName();
            return name1.compareTo(name2);
        }        
    }

}
