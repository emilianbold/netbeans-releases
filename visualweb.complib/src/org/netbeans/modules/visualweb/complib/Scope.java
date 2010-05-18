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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.complib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class handles the persistence of complibs in different installation scopes. Conceptually
 * there is a USER scope, a SYSTEM scope (to be implemented), and a scope for each project.
 * Maintains sets of ExtensionComplib-s per scope and also maintains a list of directories in an XML
 * file.
 * 
 * @author Edwin Goei
 */
class Scope {
    /** Relative path from project lib directory */
    private static final String PATH_PROJECT_SCOPE_COMPLIB = "complibs"; // NOI18N

    private static final String LIBRARY_INDEX_FILENAME = "index.xml"; // NOI18N

    private static final String DIRECTORY_ELEMENT = "directory"; // NOI18N

    private static final String DIRECTORY_NAME_ATTR = "name"; // NOI18N

    private static final String VALID_DIRECTORY_LIST = "valid-directory-list"; // NOI18N

    /** Cache which maps installHome to Scope objects */
    private static WeakHashMap<File, Scope> registry = new WeakHashMap<File, Scope>();

    /** The directory containing an index file and all expanded complibs */
    private final File installHome;

    /** Set of String directory names in this scope */
    private HashSet<String> directorySet = new HashSet<String>();

    /** Set of ExtensionComplib-s in this scope */
    private HashSet<ExtensionComplib> complibSet = new HashSet<ExtensionComplib>();

    /**
     * @param installHome
     *            directory that contains index file and all expanded complib dirs in this scope
     */
    private Scope(File installHome) {
        this.installHome = installHome;
        loadComplibs();
    }

    static Scope createScope(File installHome) {
        return new Scope(installHome);
    }

    /**
     * Factory method to get a unique Scope object for a Project
     * 
     * @param project
     * @return
     * @throws IOException
     */
    public static Scope getScopeForProject(Project project) throws IOException {
        File installHome = getProjectInstallHome(project);

        Scope scope = (Scope) registry.get(installHome);
        if (scope == null) {
            scope = new Scope(installHome);
            registry.put(installHome, scope);
        }
        return scope;
    }

    public static void destroyScopeForProject(Project project) throws IOException {
        File installHome = getProjectInstallHome(project);
        registry.remove(installHome);
    }

    private static File getProjectInstallHome(Project project) throws IOException {
        File projectLibDir = IdeUtil.getProjectLibraryDirectory(project);
        File installHome = new File(projectLibDir, PATH_PROJECT_SCOPE_COMPLIB);
        return installHome;
    }

    /**
     * Load in any expanded complibs in this scope
     */
    private void loadComplibs() {
        Set<String> dirs = readDirectoryIndex();

        // Restore the valid list of ExtensionComplib-s
        FileObject complibFo = FileUtil.toFileObject(installHome);
        if (complibFo == null) {
            // Directory does not exist => no complibs
            return;
        }

        for (FileObject fo : complibFo.getChildren()) {
            File absFile = FileUtil.toFile(fo);
            String fileName = absFile.getName();

            // Skip the directory index file itself
            if (LIBRARY_INDEX_FILENAME.equals(fileName)) {
                continue;
            }

            if (dirs.contains(fileName)) {
                // Valid library
                ExtensionComplib complib;
                try {
                    complib = new ExtensionComplib(absFile);
                } catch (Exception e) {
                    // Unable to init existing library, warn, cleanup, and skip
                    IdeUtil.logWarning(e);

                    // Clean up index and persist the info
                    dirs.remove(fileName);
                    try {
                        persistDirectoryIndex();
                    } catch (Exception e2) {
                        // Cleanup failed but it is safe to ignore
                    }

                    continue;
                }
                complibSet.add(complib);
            } else {
                // Clean up old unused libraries from previous runs of IDE
                try {
                    fo.delete();
                } catch (IOException e) {
                    // Output warning to IDE log and continue
                    IdeUtil.logWarning(e);
                }
            }
        }
    }

    public long getTimeStamp(ExtensionComplib extCompLib) {
        // Current impl uses the modification time of the root dir of the
        // expanded complib
        return extCompLib.getDirectory().lastModified();
    }

    private File ensureInstallHome() throws FileNotFoundException {
        // Create installHome if it doesn't exist
        if (!installHome.exists()) {
            if (!installHome.mkdirs()) {
                throw new FileNotFoundException(installHome.getAbsolutePath());
            }
        }
        return installHome;
    }

    /**
     * Install a complib package into this scope and return the newly installed complib.
     * 
     * @param pkg
     * @return
     * @throws IOException
     * @throws ComplibException
     */
    ExtensionComplib installComplibPackage(ComplibPackage pkg) throws IOException, ComplibException {
        // Find a unique absolute lib dir in this scope
        File packageFile = pkg.getPackageFile();
        String baseName = packageFile.getName();
        String prefix = IdeUtil.removeWhiteSpace(IdeUtil.removeExtension(baseName));
        File dstDir = IdeUtil.findUniqueFile(ensureInstallHome(), prefix, "");

        // Expand the src complib into this scope
        IdeUtil.unzip(packageFile, dstDir);
        return createComplib(dstDir);
    }

    /**
     * Install an existing complib into this scope and return the newly installed complib.
     * 
     * @param srcComplib
     *            source complib
     * @return
     * @throws IOException
     * @throws ComplibException
     */
    ExtensionComplib installComplib(ExtensionComplib srcComplib) throws IOException,
            ComplibException {
        // Find a unique absolute lib dir in this scope
        String baseName = srcComplib.getDirectoryBaseName();
        File dstDir = IdeUtil.findUniqueFile(ensureInstallHome(), baseName, "");

        // Copy an already expanded complib into this scope
        IdeUtil.copyFileRecursive(srcComplib.getDirectory(), dstDir);
        return createComplib(dstDir);
    }

    private ExtensionComplib createComplib(File dstDir) throws ComplibException, IOException {
        ExtensionComplib dstComplib = new ExtensionComplib(dstDir);
        complibSet.add(dstComplib);
        directorySet.add(dstComplib.getDirectoryBaseName());
        persistDirectoryIndex();
        return dstComplib;
    }

    static void copyFile(File source, File dest) throws IOException {
        File newItem = null;
        if (dest.isDirectory()) {
            newItem = new File(dest, source.getName());
        } else {
            newItem = dest;
        }

        if (source.isDirectory()) {
            newItem.mkdir();
            File[] contents = source.listFiles();

            for (int i = 0; i < contents.length; i++) {
                copyFile(contents[i], newItem);
            }
        } else {
            BufferedInputStream in = null;
            BufferedOutputStream out = null;

            try {
                in = new BufferedInputStream(new FileInputStream(source));
                out = new BufferedOutputStream(new FileOutputStream(newItem));
                int c;
                while ((c = in.read()) != -1)
                    out.write(c);

            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
        }
    }

    /**
     * Remove an installed component library
     * 
     * @param library
     * @param scope
     */
    public void remove(ExtensionComplib library) {
        directorySet.remove(library.getDirectoryBaseName());
        complibSet.remove(library);

        // Persist info on new component lib
        try {
            persistDirectoryIndex();
        } catch (Exception e) {
            // This failure mode will be handled up when IDE starts up again
        }

        // Try to remove the library dir. If it fails, it will be cleaned up
        // when IDE starts up.
        File libDir = library.getDirectory();
        // ProjectUtil.recursiveDelete(libDir);
        recursiveDelete(libDir);
    }

    // Exercise with caution!
    private static boolean recursiveDelete(File f) {
        // Basic safeguard - bail if we're asked to delete the root directory
        if (f.getParentFile() == null)
            return false;
        if (f.isDirectory()) {
            File[] contents = f.listFiles();
            for (int i = 0; i < contents.length; i++) {
                recursiveDelete(contents[i]);
            }
        }
        return f.delete();
    }

    /**
     * Returns an existing complib in this scope with a matching complib Id or null.
     * 
     * @param complib
     * @return
     */
    public ExtensionComplib getExistingComplib(Complib complib) {
        Complib.Identifier id = complib.getIdentifier();
        for (ExtensionComplib iComplib : complibSet) {
            if (iComplib.getIdentifier().equals(id)) {
                return iComplib;
            }
        }
        return null;
    }

    /**
     * Returns true iff scope contains a complib with the same Id
     * 
     * @param complib
     * @return
     */
    boolean contains(ExtensionComplib complib) {
        return getExistingComplib(complib) != null;
    }

    /**
     * Return the time this Scope was last modified
     * 
     * @return
     */
    public long getLastModified() {
        return getIndexFile().lastModified();
    }

    /**
     * Return the component libraries in this scope
     * 
     * @return
     */
    public Set<ExtensionComplib> getComplibs() {
        return complibSet;
    }

    public String toString() {
        return "{installHome='" + installHome + "', directorySet=" // NOI18N
                + directorySet + ", complibSet=" + complibSet // NOI18N
                + "}"; // NOI18N
    }

    /**
     * @return The index.xml File object for this scope.
     * @throws FileNotFoundException
     *             if scope dir is not found or cannot be created which should not normally occur
     */
    private File getIndexFile() {
        return new File(installHome, LIBRARY_INDEX_FILENAME);
    }

    /**
     * Read directories from an index XML file
     * 
     * @return Set of String-s, possibly empty. Never null.
     */
    private Set<String> readDirectoryIndex() {
        File indexFile = getIndexFile();
        if (!indexFile.exists()) {
            // No index file so return previously initialized empty set
            return directorySet;
        }

        XmlUtil xmlIn = new XmlUtil();
        Document doc;
        try {
            doc = xmlIn.read(indexFile);
        } catch (XmlException e) {
            // Assume list is empty
            return directorySet;
        }
        Element docElement = doc.getDocumentElement();
        NodeList nl = docElement.getElementsByTagName(DIRECTORY_ELEMENT);
        for (int i = 0; i < nl.getLength(); i++) {
            Element dirElement = (Element) nl.item(i);
            String dirName = dirElement.getAttribute(DIRECTORY_NAME_ATTR);
            directorySet.add(dirName);
        }
        return directorySet;
    }

    /**
     * Persist set of directories to this scope in an XML file
     * 
     * @throws Exception
     */
    private void persistDirectoryIndex() throws IOException {
        File indexFile = getIndexFile();
        XmlUtil xmlOut = new XmlUtil();
        Document doc = xmlOut.createDocument();
        Comment docComment = doc
                .createComment(" Directory list containing valid component libraries which is" // NOI18N
                        + " a workaround for Win32 file deletion problems."); // NOI18N
        doc.appendChild(docComment);
        Element docElement = doc.createElement(VALID_DIRECTORY_LIST);
        doc.appendChild(docElement);

        for (String dirName : directorySet) {
            Element dirElm = doc.createElement(DIRECTORY_ELEMENT);
            dirElm.setAttribute(DIRECTORY_NAME_ATTR, dirName);
            docElement.appendChild(dirElm);
        }

        xmlOut.write(indexFile);
    }
}
