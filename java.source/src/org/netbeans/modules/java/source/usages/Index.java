/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.ClassIndex;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 * Index SPI. Represents an index for usages data
 * @author Tomas Zezula
 */
public abstract class Index {    
    
    public enum BooleanOperator {
        AND,
        OR
    };
    
    private static final int VERSION = 0;
    private static final int SUBVERSION = 4;
    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String SEGMENTS_FILE = "segments";      //NOI18N
    private static final String CLASSES = "classes";             //NOI18N
    private static final String SLICE_PREFIX = "s";              //NOI18N    
    private static final String INDEX_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"index"+File.separatorChar+VERSION+'.'+SUBVERSION;    //NOI18N
    
    private static Properties segments;
    private static Map<String, String> invertedSegments;
    private static File cacheFolder;
    private static File segmentsFile;
    private static int index = 0;
    
    public static final ThreadLocal<AtomicBoolean> cancel = new ThreadLocal<AtomicBoolean> () {
        protected synchronized AtomicBoolean initialValue() {
             return new AtomicBoolean ();
         }
    };    
    
    public abstract boolean isValid (boolean tryOpen) throws IOException;   
    public abstract List<String> getUsagesFQN (String resourceName, Set<ClassIndexImpl.UsageType> mask, BooleanOperator operator) throws IOException, InterruptedException;
    public abstract <T> void getDeclaredTypes (String simpleName, ClassIndex.NameKind kind, ResultConvertor<T> convertor, Set<? super T> result) throws IOException, InterruptedException;
    public abstract void getPackageNames (String prefix, boolean directOnly, Set<String> result) throws IOException, InterruptedException;
    public abstract void store (Map<String,Pair<String,List<String>>> refs, Set<String> toDelete) throws IOException;
    public abstract void store (Map<String,Pair<String,List<String>>> refs, List<String> topLevels) throws IOException;
    public abstract boolean isUpToDate (String resourceName, long timeStamp) throws IOException;
    public abstract String getSourceName (String binaryName) throws IOException;
    public abstract void clear () throws IOException;
    public abstract void close () throws IOException;
    
    
    private static void loadSegments () throws IOException {
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
    
    
    private static void storeSegments () throws IOException {
        assert segmentsFile != null;       
        OutputStream out = new FileOutputStream (segmentsFile);
        try {
            segments.store(out,null);
        } finally {
            out.close();
        }            
    }
    
    
    public static URL getSourceRootForClassFolder (final URL classFolder) {
        if ("file".equals(classFolder.getProtocol())) {           //NOI18N
            try {
                final File file = FileUtil.normalizeFile(new File (classFolder.toURI()));            
                final File segFolder = file.getParentFile();
                if (segFolder == null) {
                    return null;
                }
                final Object cFolder = segFolder.getParentFile();
                if (cFolder == null || !cFolder.equals(cacheFolder)) {
                    return null;
                }   
                String source = segments.getProperty(segFolder.getName());
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
        
    
    public static synchronized File getDataFolder (final URL root) throws IOException {
        return getDataFolder(root, false);
    }
    
    public static synchronized File getDataFolder (final URL root, boolean onlyIfAlreadyExists) throws IOException {
        loadSegments ();
        final String rootName = root.toExternalForm();
        String slice = invertedSegments.get (rootName);
        if ( slice == null) {
            if (onlyIfAlreadyExists)
                return null;
            slice = SLICE_PREFIX + (++index);
            while (segments.getProperty(slice) != null) {                
                slice = SLICE_PREFIX + (++index);
            }
            segments.put (slice,rootName);
            invertedSegments.put(rootName, slice);
            storeSegments ();
        }        
        File result = FileUtil.normalizeFile (new File (cacheFolder, slice));
        if (!result.exists()) {
            if (onlyIfAlreadyExists)
                return null;
            result.mkdir();
        }
        return result;
    }
    
    /**returns null if onlyIfAlreadyExists == true and the cache folder for the given url does not exist.
     */
    public static File getClassFolder (final URL url, boolean onlyIfAlreadyExists) throws IOException {
        return getClassFolderImpl(url, onlyIfAlreadyExists);
    }
    
    public static File getClassFolder (final URL url) throws IOException {                
        return getClassFolder(url, false);
    }
    
    public static File getClassFolder (final File root) throws IOException {
        return getClassFolderImpl(root.toURI().toURL(), false);
    }        
    
    private static File getClassFolderImpl (final URL url, boolean onlyIfAlreadyExists) throws IOException {
        final File dataFolder = getDataFolder (url, onlyIfAlreadyExists);
        if (onlyIfAlreadyExists && dataFolder == null)
            return null;
        final File result = FileUtil.normalizeFile(new File (dataFolder, CLASSES));
        if (!result.exists()) {
            if (onlyIfAlreadyExists)
                return null;
            result.mkdir();
        }
        return result;
    }
    
    
    
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
    
    public static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserDirProp = getNbUserDir();
            assert nbUserDirProp != null;
            final File nbUserDir = new File (nbUserDirProp);
            cacheFolder = FileUtil.normalizeFile(new File (nbUserDir, INDEX_DIR));
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
    
    public static boolean isTest () {
        return getNbUserDir() == null;
    }
    
    /**
     * Only for unit tests!
     *
     */
    static synchronized void setCacheFolder (final File folder) {
        assert folder != null && folder.exists() && folder.canRead() && folder.canWrite();
        cacheFolder = folder;
    }
    
}
