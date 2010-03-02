/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class JavaIndex {

    public static final String NAME = "java"; //NOI18N
    public static final int VERSION = 14;
    static final Logger LOG = Logger.getLogger(JavaIndex.class.getName());
    private static final String CLASSES = "classes"; //NOI18N
    private static final String APT_SOURCES = "sources";    //NOI18N
    private static final String ATTR_FILE_NAME = "attributes.properties"; //NOI18N

    public static File getIndex(Context c) {
        return FileUtil.toFile(c.getIndexFolder());
    }

    public static File getIndex(URL url) throws IOException {
        FileObject indexBaseFolder = CacheFolder.getDataFolder(url);
        String path = SPIAccessor.getInstance().getIndexerPath(NAME, VERSION);
        FileObject indexFolder = FileUtil.createFolder(indexBaseFolder, path);
        return FileUtil.toFile(indexFolder);
    }

    public static File getClassFolder(Context c) {
        return getClassFolder(c, false);
    }
    
    public static File getClassFolder(Context c, boolean onlyIfExists) {
        return processCandidate(new File(getIndex(c), CLASSES), onlyIfExists);
    }

    public static File getClassFolder(File root) throws IOException {
        return getClassFolder(root.toURI().toURL()); //XXX
    }

    public static File getClassFolder(URL url) throws IOException {
        return getClassFolder(url, false);
    }

    public static File getClassFolder(URL url, boolean onlyIfExists) throws IOException {
        return processCandidate(new File(getIndex(url), CLASSES), onlyIfExists);
    }

    public static File getAptFolder(final URL sourceRoot, final boolean create) throws IOException {
        final File aptSources = new File (getIndex(sourceRoot), APT_SOURCES);
        if (create) {
            aptSources.mkdirs();
        }
        return aptSources;
    }

    public static URL getSourceRootForClassFolder(URL binaryRoot) {
        FileObject folder = URLMapper.findFileObject(binaryRoot);
        if (folder == null || !CLASSES.equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null || !String.valueOf(VERSION).equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null || !NAME.equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null)
            return null;
        return CacheFolder.getSourceRootForDataFolder(folder);
    }

    public static boolean ensureAttributeValue(final URL root, final String attributeName, final String attributeValue) throws IOException {
        Properties p = loadProperties(root);
        final String current = p.getProperty(attributeName);
        if (current == null) {
            if (attributeValue != null) {
                p.setProperty(attributeName, attributeValue);
                storeProperties(root, p);
                return true;
            } else {
                return false;
            }
        }
        if (current.equals(attributeValue)) {
            return false;
        }
        if (attributeValue != null) {
            p.setProperty(attributeName, attributeValue);
        } else {
            p.remove(attributeName);
        }
        storeProperties(root, p);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine ("ensureAttributeValue attr: " + attributeName + " current: " + current + " new: " + attributeValue); //NOI18N
        }
        return true;
    }

    public static void setAttribute(URL root, String attributeName, String attributeValue) throws IOException {
        Properties p = loadProperties(root);
        if (attributeValue != null) {
            p.setProperty(attributeName, attributeValue);
        } else {
            p.remove(attributeName);
        }
        storeProperties(root, p);
    }

    public static String getAttribute(URL root, String attributeName, String defaultValue) throws IOException {
        Properties p = loadProperties(root);
        return p.getProperty(attributeName, defaultValue);
    }

    private static Properties loadProperties(URL root) throws IOException {
        File f = getAttributeFile(root);
        Properties result = new Properties();
        if (!f.exists())
            return result;
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
            result.load(in);
        } catch (IllegalArgumentException iae) {
            //Issue #138704: Invalid unicode encoding in attribute file.
            //Return newly constructed Properties, the result
            //may already contain some pairs.
            LOG.warning("Broken attribute file: " + f.getAbsolutePath()); //NOI18N
            return new Properties();
        } finally {
            in.close();
        }
        return result;
    }

    private static void storeProperties(URL root, Properties p) throws IOException {
        File f = getAttributeFile(root);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            p.store(out, ""); //NOI18N
        } finally {
            out.close();
        }
    }

    private static File getAttributeFile(URL root) throws IOException {
        return new File(JavaIndex.getIndex(root), ATTR_FILE_NAME);
    }

    private static File processCandidate(File result, boolean onlyIfExists) {
        if (onlyIfExists) {
            if (!result.exists()) {
                return null;
            } else {
                return result;
            }
        }
        result.mkdirs();
        return result;
    }

    public static boolean isLibrary (final ClassPath cp) {
        assert cp != null;
        for (FileObject fo : cp.getRoots()) {
            if (isLibrary (fo)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLibrary (final FileObject root) {
        assert root != null;
        try {
            return isLibrary(root.getURL());
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
            return true;    //Safer
        }
    }

    public static boolean isLibrary (final URL root) {
        assert root != null;
        ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(root);
        return uq == null || !uq.isSource();
    }

    private JavaIndex() {
        
    }
}
