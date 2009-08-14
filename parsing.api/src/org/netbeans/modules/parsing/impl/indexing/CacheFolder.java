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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class CacheFolder {

    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String INDEX_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"index";    //NOI18N
    private static final String SEGMENTS_FILE = "segments";      //NOI18N
    private static final String SLICE_PREFIX = "s";              //NOI18N
    
    private static FileObject cacheFolder;
    private static Properties segments;
    private static Map<String, String> invertedSegments;
    private static int index = 0;


    private static void loadSegments(FileObject folder) throws IOException {
        if (segments == null) {
            assert folder != null;
            segments = new Properties ();
            invertedSegments = new HashMap<String,String> ();
            final FileObject segmentsFile =  folder.getFileObject(SEGMENTS_FILE);
            if (segmentsFile!=null) {
                final InputStream in = segmentsFile.getInputStream();
                try {
                    segments.load (in);
                } finally {
                    in.close();
                }
            }
            for (Map.Entry entry : segments.entrySet()) {
                String segment = (String) entry.getKey();
                String root = (String) entry.getValue();
                invertedSegments.put(root,segment);
                try {
                    index = Math.max (index,Integer.parseInt(segment.substring(SLICE_PREFIX.length())));
                } catch (NumberFormatException nfe) {
                    Exceptions.printStackTrace(nfe);
                }
            }
        }
    }


    private static void storeSegments(FileObject folder) throws IOException {
        assert folder != null;
        final FileObject segmentsFile = FileUtil.createData(folder, SEGMENTS_FILE);
        final OutputStream out = segmentsFile.getOutputStream();
        try {
            segments.store(out,null);
        } finally {
            out.close();
        }
    }

    public static synchronized URL getSourceRootForDataFolder (final FileObject dataFolder) {
        final FileObject segFolder = dataFolder.getParent();
        if (segFolder == null || !segFolder.equals(cacheFolder)) {
            return null;
        }
        String source = segments.getProperty(dataFolder.getName());
        if (source != null) {
            try {
                return new URL (source);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return null;
    }

    public static FileObject getDataFolder (final URL root) throws IOException {
        return getDataFolder(root, false);
    }

    public static FileObject getDataFolder (final URL root, final boolean onlyIfAlreadyExists) throws IOException {
        final FileObject _cacheFolder = getCacheFolder();
        final FileObject [] dataFolder = new FileObject[] { null };

        // #170182 - preventing filesystem events being fired from under the CacheFolder.class lock
        _cacheFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                synchronized (CacheFolder.class) {
                    loadSegments(_cacheFolder);
                    final String rootName = root.toExternalForm();
                    String slice = invertedSegments.get (rootName);
                    if ( slice == null) {
                        if (onlyIfAlreadyExists) {
                            return;
                        }
                        slice = SLICE_PREFIX + (++index);
                        while (segments.getProperty(slice) != null) {
                            slice = SLICE_PREFIX + (++index);
                        }
                        segments.put (slice,rootName);
                        invertedSegments.put(rootName, slice);
                        storeSegments(_cacheFolder);
                    }
                    if (onlyIfAlreadyExists) {
                        dataFolder[0] = _cacheFolder.getFileObject(slice);
                    } else {
                        dataFolder[0] = FileUtil.createFolder(_cacheFolder, slice);
                    }
                }
            }
        });

        return dataFolder[0];
    }

    private static String getNbUserDir () {
        final String nbUserProp = System.getProperty(NB_USER_DIR);
        return nbUserProp;
    }
    
    public static synchronized FileObject getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserDirProp = getNbUserDir();
            assert nbUserDirProp != null;
            final File nbUserDir = new File (nbUserDirProp);
            File cache = FileUtil.normalizeFile(new File (nbUserDir, INDEX_DIR));
            if (!cache.exists()) {
                boolean created = cache.mkdirs();                
                assert created : "Cannot create cache folder";  //NOI18N
            }
            else {
                assert cache.isDirectory() && cache.canRead() && cache.canWrite();
            }
            cacheFolder = FileUtil.toFileObject(cache);
            assert cacheFolder != null;
        }
        return cacheFolder;
    }


    /**
     * Only for unit tests! It's used also by CslTestBase, which is not in the
     * same package, hence the public keyword.
     *
     */
    public static synchronized void setCacheFolder (final FileObject folder) {
        assert folder != null && folder.canRead() && folder.canWrite();
        cacheFolder = folder;
        segments = null;
        invertedSegments = null;
        index = 0;
    }

    private CacheFolder() {
        // no-op
    }
}
