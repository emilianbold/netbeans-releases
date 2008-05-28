/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class BuildArtifactMapperImpl {

    private static final Logger LOG = Logger.getLogger(BuildArtifactMapperImpl.class.getName());
    
    private static final String TAG_FILE_NAME = ".netbeans_automatic_build";
//    private static final Map<URL, File> source2Target = new HashMap<URL, File>();
    private static final Map<URL, Set<ArtifactsUpdated>> source2Listener = new HashMap<URL, Set<ArtifactsUpdated>>();

    public static synchronized void addArtifactsUpdatedListener(URL sourceRoot, ArtifactsUpdated listener) {
        Set<ArtifactsUpdated> listeners = source2Listener.get(sourceRoot);
        
        if (listeners == null) {
            source2Listener.put(sourceRoot, listeners = new HashSet<ArtifactsUpdated>());
        }
        
        listeners.add(listener);
    }

    public static synchronized void removeArtifactsUpdatedListener(URL sourceRoot, ArtifactsUpdated listener) {
        Set<ArtifactsUpdated> listeners = source2Listener.get(sourceRoot);
        
        if (listeners == null) {
            return ;
        }
        
        listeners.remove(listener);
        
        if (listeners.isEmpty()) {
            source2Listener.remove(sourceRoot);
        }
    }
    
    private static File getTarget(URL source) {
        Result binaryRoots = BinaryForSourceQuery.findBinaryRoots(source);
        
        File result = null;
        
        for (URL u : binaryRoots.getRoots()) {
            if (FileUtil.isArchiveFile(u)) {
                continue;
            }
            
            File f = FileUtil.archiveOrDirForURL(u);
            
            if (f != null && result != null) {
                Logger.getLogger(BuildArtifactMapperImpl.class.getName()).log(Level.WARNING, "More than one binary directory for root: {0}", source.toExternalForm());
                return null;
            }
            
            result = f;
        }
        
        return result;
    }
    
    public static boolean ensureBuilt(URL sourceRoot) throws IOException {
        File targetFolder = getTarget(sourceRoot);
        
        if (targetFolder == null) {
            return false;
        }
        
        File tagFile = new File(targetFolder, TAG_FILE_NAME);

        if (tagFile.exists()) {
            return true;
        }
        
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw new IOException("Cannot create destination folder: " + targetFolder.getAbsolutePath());
        }
        
        File index = Index.getClassFolder(sourceRoot, true);
        
        if (index == null) {
            return false;
        }
        
        copyRecursively(index, targetFolder);
        
        new FileOutputStream(tagFile).close();
        
        return true;
    }
    
    public static void classCacheUpdated(URL sourceRoot, File cacheRoot, Iterable<File> deleted, Iterable<File> updated) {
        File targetFolder = getTarget(sourceRoot);
        
        if (targetFolder == null) {
            return ;
        }
        
        if (!new File(targetFolder, TAG_FILE_NAME).exists()) {
            return ;
        }
        
        List<File> updatedFiles = new LinkedList<File>();

        assert targetFolder != null;
        
        for (File deletedFile : deleted) {
            File toDelete = resolveFile(targetFolder, relativizeFile(cacheRoot, deletedFile));
            
            toDelete.delete();
            updatedFiles.add(toDelete);
        }
        
        for (File updatedFile : updated) {
            File target = resolveFile(targetFolder, relativizeFile(cacheRoot, updatedFile));

            try {
                copyFile(updatedFile, target);
                updatedFiles.add(target);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        Set<ArtifactsUpdated> listeners;
        
        synchronized (BuildArtifactMapperImpl.class) {
            listeners = source2Listener.get(sourceRoot);
            
            if (listeners != null) {
                listeners = new HashSet<ArtifactsUpdated>(listeners);
            }
        }
        
        if (listeners != null) {
            for (ArtifactsUpdated listener : listeners) {
                listener.artifactsUpdated(updatedFiles);
            }
        }
    }

    private static void copyFile(File updatedFile, File target) throws IOException {
        InputStream ins = null;
        OutputStream out = null;

        try {
            ins = new FileInputStream(updatedFile);
            out = new FileOutputStream(target);

            FileUtil.copy(ins, out);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    private static void copyRecursively(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                if (!target.mkdirs()) {
                    throw new IOException("Cannot create folder: " + target.getAbsolutePath());
                }
            } else {
                if (!target.isDirectory()) {
                    throw new IOException("Cannot create folder: " + target.getAbsolutePath() + ", already exists as a file.");
                }
            }
            
            File[] listed = source.listFiles();
            
            if (listed == null) {
                return ;
            }
            
            for (File f : listed) {
                copyRecursively(f, new File(target, f.getName()));
            }
        } else {
            if (target.isDirectory()) {
                throw new IOException("Cannot create file: " + target.getAbsolutePath() + ", already exists as a folder.");
            }
            
            copyFile(source, target);
        }
    }
    
    //XXX: copied from PropertyUtils:
    private static final Pattern RELATIVE_SLASH_SEPARATED_PATH = Pattern.compile("[^:/\\\\.][^:/\\\\]*(/[^:/\\\\.][^:/\\\\]*)*"); // NOI18N
    
    /**
     * Find an absolute file path from a possibly relative path.
     * @param basedir base file for relative filename resolving; must be an absolute path
     * @param filename a pathname which may be relative or absolute and may
     *                 use / or \ as the path separator
     * @return an absolute file corresponding to it
     * @throws IllegalArgumentException if basedir is not absolute
     */
    public static File resolveFile(File basedir, String filename) throws IllegalArgumentException {
        if (basedir == null) {
            throw new NullPointerException("null basedir passed to resolveFile"); // NOI18N
        }
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        if (!basedir.isAbsolute()) {
            throw new IllegalArgumentException("nonabsolute basedir passed to resolveFile: " + basedir); // NOI18N
        }
        File f;
        if (RELATIVE_SLASH_SEPARATED_PATH.matcher(filename).matches()) {
            // Shortcut - simple relative path. Potentially faster.
            f = new File(basedir, filename.replace('/', File.separatorChar));
        } else {
            // All other cases.
            String machinePath = filename.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            f = new File(machinePath);
            if (!f.isAbsolute()) {
                f = new File(basedir, machinePath);
            }
            assert f.isAbsolute();
        }
        return FileUtil.normalizeFile(f);
    }
    
    /**
     * Produce a machine-independent relativized version of a filename from a basedir.
     * Unlike {@link URI#relativize} this will produce "../" sequences as needed.
     * @param basedir a directory to resolve relative to (need not exist on disk)
     * @param file a file or directory to find a relative path for
     * @return a relativized path (slash-separated), or null if it is not possible (e.g. different DOS drives);
     *         just <samp>.</samp> in case the paths are the same
     * @throws IllegalArgumentException if the basedir is known to be a file and not a directory
     */
    public static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            if (base.equals(file)) {
                // #61687: file is a parent of basedir
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }

    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }
}
