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
package org.netbeans.modules.csl.source.usages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.modules.csl.api.IndexDocument;
import org.netbeans.modules.csl.api.Indexer;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 * Index SPI. Represents an index for usages data
 * @author Tomas Zezula
 */
// BEGIN TOR MODIFICATIONS
public abstract class Index extends org.netbeans.modules.csl.api.Index {
    protected Language language;
    
    protected Index(Language language) {
        this.language = language;
    }

// END TOR MODIFICATIONS
    
    public enum BooleanOperator {
        AND,
        OR
    };
    
    private static final int VERSION = 1;
    private static final int SUBVERSION = 119;
    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String SEGMENTS_FILE = "segments";      //NOI18N
    private static final String SLICE_PREFIX = "s";              //NOI18N    
    private static final String INDEX_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"gsf-index"+File.separatorChar+VERSION+'.'+SUBVERSION;    //NOI18N
    // BEGIN TOR MODIFICATIONS
    protected static final String PREINDEXED = "netbeans-index-" + VERSION + "." + SUBVERSION + "-"; // NOI18N
    private static final String PREINDEXED_MARKER = "static";
    private static final boolean COMPUTE_INDEX = Boolean.getBoolean("ruby.computeindex");
    public abstract Map<String,String> getTimeStamps() throws IOException;
    // Store map of class names, where each entry has a map of fields and values (fields might be "name", "fqn", "case insensitive name", etc.
    // The same fields can be looked up later.
    public abstract void store(String fileUrl, List<IndexDocument> documents) throws IOException;
    /** 
     * Update the index. If create is true, there is no need to look for existing
     * old documents for the same files to update them.
     */
    public abstract void batchStore(List<IndexBatchEntry> list, boolean create) throws IOException;
    // END TOR MODIFICATIONS
    
    public abstract boolean isValid (boolean tryOpen) throws IOException;    
    public abstract boolean isUpToDate (String resourceName, long timeStamp) throws IOException;
    public abstract void clear () throws IOException;
    public abstract void close () throws IOException;
    
    private static class LanguageContext {
        private Properties segments;
        private Map<String, String> invertedSegments;
        private File cacheFolder;
        private File segmentsFile;
        private int index = 0;
        private Language language;

        private LanguageContext(Language language) {
            this.language = language;
        }
    
        private void loadSegments () throws IOException {
            if (segments == null) {
                File cacheFolder = getCacheFolder();
                assert cacheFolder != null;           
                segments = new Properties ();
                invertedSegments = new HashMap<String,String> ();
                segmentsFile = FileUtil.normalizeFile(new File (cacheFolder, SEGMENTS_FILE));
                if (segmentsFile.exists()) {
                    InputStream in = new FileInputStream (segmentsFile);
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
                        ErrorManager.getDefault().notify(nfe);
                    }
                }
                assert segmentsFile != null;
            }        
        }


        private void storeSegments () throws IOException {
            assert segmentsFile != null;       
            OutputStream out = new FileOutputStream (segmentsFile);
            try {
                segments.store(out,null);
            } finally {
                out.close();
            }            
        }

        private synchronized File getCacheFolder () {
            if (cacheFolder == null) {
                final String nbUserDirProp = getNbUserDir();
                assert nbUserDirProp != null;
                final File nbUserDir = new File (nbUserDirProp);
                cacheFolder = FileUtil.normalizeFile(new File (nbUserDir, INDEX_DIR));
                Indexer indexer = language.getIndexer();
                assert indexer != null : language;
                cacheFolder = new File(cacheFolder, indexer.getIndexerName() + File.separator + indexer.getIndexVersion());
                if (!cacheFolder.exists()) {
                    boolean created = cacheFolder.mkdirs();                
                    assert created : "Cannot create cache folder";  //NOI18N
                }
                else {
                    assert cacheFolder.isDirectory() && cacheFolder.canRead() && cacheFolder.canWrite();
                }
            }
            return cacheFolder;
        }

        /**
         * Only for unit tests!
         *
         */
        synchronized void setCacheFolder (final File folder) {
            assert folder != null && folder.exists() && folder.canRead() && folder.canWrite();
            cacheFolder = folder;
        }
    
    } 

    // BEGIN TOR MODIFICTIONS
    private static Map<Language,LanguageContext> contexts = new HashMap<Language, Index.LanguageContext>();
    static synchronized LanguageContext getContext(Language language) {
        LanguageContext context = contexts.get(language);
        if (context == null) {
            context = new LanguageContext(language);
            contexts.put(language, context);
        }

        return context;
    }
    // END TOR MODIFICTIONS
    
    public static URL getSourceRootForClassFolder (final Language language, final URL classFolder) {
        if ("file".equals(classFolder.getProtocol())) {           //NOI18N
            try {
                final File file = FileUtil.normalizeFile(new File (classFolder.toURI()));            
                final File segFolder = file.getParentFile();
                if (segFolder == null) {
                    return null;
                }
                LanguageContext context = getContext(language);
                final Object cFolder = segFolder.getParentFile();
                if (cFolder == null || !cFolder.equals(context.cacheFolder)) {
                    return null;
                }   
                String source = context.segments.getProperty(segFolder.getName());
                if (source != null) {
                    try {            
                        return new URL (source);
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);            
                    }
                }
            } catch (URISyntaxException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return null;
    }
      
    // BEGIN TOR MODIFICATIONS
    private static List<FileObject> preindexRoots;
    public static void addPreindexRoot(FileObject root) {
        getPreindexRoots();
        if (!preindexRoots.contains(root)) {
            preindexRoots.add(root);
        }
    }
    
    private static List<FileObject> getPreindexRoots() {
        if (preindexRoots == null) {
             preindexRoots = new ArrayList<FileObject>();
             // Add in the libraries
             for (FileObject fo : LanguageRegistry.getInstance().getLibraryFos()) {
                preindexRoots.add(fo);
             }
        }
        
        return preindexRoots;
    }
    // END TOR MODIFICATIONS
    
    public static synchronized File getDataFolder (Language language, final URL root) throws IOException {
        // BEGIN TOR MODIFICTIONS
        //loadSegments ();
        LanguageContext context = getContext(language);
        context.loadSegments();
        // END TOR MODIFICTIONS
        final String rootName = root.toExternalForm();
        String slice = context.invertedSegments.get (rootName);
        // BEGIN TOR MODIFICATIONS
        FileObject extract = null;
        // END TOR MODIFICATIONS
        if ( slice == null) {
            slice = SLICE_PREFIX + (++context.index);
            while (context.segments.getProperty(slice) != null) {                
                slice = SLICE_PREFIX + (++context.index);
            }
            context.segments.put (slice,rootName);
            context.invertedSegments.put(rootName, slice);
            
            // BEGIN TOR MODIFICATIONS
            // See if I have pre-indexed data for this file
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                Indexer indexer = language.getIndexer();
                String indexedFileName = PREINDEXED + indexer.getIndexerName() + "-" + indexer.getIndexVersion();
                extract = rootFo.getFileObject(indexedFileName, "zip"); // NOI18N
                if (extract == null && !COMPUTE_INDEX) {
                    // There's no co-located index data, but perhaps we have
                    // it within the larger preindexed bundles (these are
                    // zip files which contain a bunch of indices for other
                    // versions, such as native ruby 1.8.6, rails 1.1.6, 1.2.3, etc.

                    // Compute relative path
                    rootSearch:
                    for (FileObject fo : getPreindexRoots()) {
                        if (fo != null && FileUtil.isParentOf(fo, rootFo)) {
                            // getRelativePath performs a isParentOf check and returns null if not
                            String relative = FileUtil.getRelativePath(fo, rootFo);
                            if (relative != null && relative.length() > 0) {
                                FileObject db = indexer.getPreindexedDb();
                                if (db != null) {
                                    extract = db.getFileObject(relative + "/" + indexedFileName + ".zip"); // NOI18N
                                }
                                break rootSearch;
                            }
                        }
                    }
                }
            }
            // END TOR MODIFICATIONS
            context.storeSegments ();
        }        
        File result = FileUtil.normalizeFile (new File (context.cacheFolder, slice));
        if (!result.exists()) {
            result.mkdir();
            // BEGIN TOR MODIFICATIONS
            if (extract != null) {
                File extractFile = FileUtil.toFile(extract);
                FileObject dest = FileUtil.toFileObject(result);
                if (dest != null) {
                    extractZip(dest, new BufferedInputStream(new FileInputStream(extractFile)));
                }
            }
            // END TOR MODIFICATIONS
        }
        return result;
    }
    
    // BEGIN TOR MODIFICATIONS
    static boolean isPreindexed(File dataDir) {
       return new File(dataDir, PREINDEXED_MARKER).exists();
    }

    // Based on openide/fs' FileUtil.extractJar
    // NOTE: This code is already duplicated in the Ruby editing module, in the NbUtilities module
    private static void extractZip(final FileObject fo, final InputStream is)
    throws IOException {
        FileSystem fs = fo.getFileSystem();

        fs.runAtomicAction(
            new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    extractZipImpl(fo, is);
                }
            }
        );
    }

    /** Does the actual extraction of the Jar file.
     */
    // Based on openide/fs' FileUtil.extractJarImpl
    private static void extractZipImpl(FileObject fo, InputStream is)
    throws IOException {
        ZipEntry je;

        ZipInputStream jis = new ZipInputStream(is);

        while ((je = jis.getNextEntry()) != null) {
            String name = je.getName();

            if (name.toLowerCase().startsWith("meta-inf/")) {
                continue; // NOI18N
            }

            if (je.isDirectory()) {
                FileUtil.createFolder(fo, name);

                continue;
            }

            // copy the file
            FileObject fd = FileUtil.createData(fo, name);
            FileLock lock = fd.lock();

            try {
                OutputStream os = fd.getOutputStream(lock);

                try {
                    FileUtil.copy(jis, os);
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }

    // Only done at build time / ahead of time.
    static void preindex(Language language, URL root) {
        try {
            Indexer indexer = language.getIndexer();
            if (!indexer.acceptQueryPath(root.toExternalForm())) {
                return;
            }
            FileObject rootFo = URLMapper.findFileObject(root);
            File dataFile = getDataFolder(language, root);
            // Create "preindexed" file
            // Zip contents of data folder up and store it as a preindexed file in the rootFo
            String indexedFileName = PREINDEXED + indexer.getIndexerName() + "-" + indexer.getIndexVersion();
            File output = new File(FileUtil.toFile(rootFo), indexedFileName + ".zip"); // NOI18N
            OutputStream os = new BufferedOutputStream(new FileOutputStream(output));

            ZipEntry je;

            ZipOutputStream jis = new ZipOutputStream(os);

            je = new ZipEntry(PREINDEXED_MARKER);
            jis.putNextEntry(je);
            jis.closeEntry();
            
            File gsf = new File(dataFile, LuceneIndex.REFERENCES); // NOI18N
            assert gsf.exists();
            
            ClassIndexImpl classIndexImpl = ClassIndexManager.get(language).getUsagesQuery(root);
            classIndexImpl.close();
            if (gsf.list().length == 0) {
                // Hmmm, empty... We need -something- in there to avoid reindexing next time
                classIndexImpl.storeEmpty();
                classIndexImpl.close();
            }
            
            File[] files = gsf.listFiles();
            for (File f : files) {
                ZipEntry ze = new ZipEntry(LuceneIndex.REFERENCES + "/" + f.getName()); // NOI18N
                jis.putNextEntry(ze);
                
                // Copy data
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                FileUtil.copy(is, jis);
                
                jis.closeEntry();
            }
            
            jis.finish();
            jis.close();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    // END TOR MODIFICATIONS
    
    /**
     *  Returns non cached netbeans user dir.
     *  For performance reasons the returned {@link File} is not normalized.
     *  Client is responsible to call {@link FileUtil.normalizeFile}
     *  before using the returned value.
     *  @return netbeans user dir.
     */
    static String getNbUserDir () {
        final String nbUserProp = System.getProperty(NB_USER_DIR);
        return nbUserProp;
    }
}
