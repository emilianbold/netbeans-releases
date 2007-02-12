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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
    private static final int SUBVERSION = 1;
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
    
    public abstract boolean isValid (boolean tryOpen) throws IOException;
    public abstract List<String> getUsagesData (String resourceName, Set<ClassIndexImpl.UsageType> mask, BooleanOperator operator) throws IOException;
    public abstract List<String> getUsagesFQN (String resourceName, Set<ClassIndexImpl.UsageType> mask, BooleanOperator operator) throws IOException;
    public abstract List<String> getReferencesData (String resourceName) throws IOException;
    public abstract <T> void getDeclaredTypes (String simpleName, ClassIndex.NameKind kind, ResultConvertor<T> convertor, Set<? super T> result) throws IOException;
    public abstract void getPackageNames (String prefix, boolean directOnly, Set<String> result) throws IOException;
    public abstract void store (Map<String,List<String>> refs, Set<String> toDelete) throws IOException;
    public abstract void store (Map<String,List<String>> refs, List<String> topLevels) throws IOException;
    public abstract boolean isUpToDate (String resourceName, long timeStamp) throws IOException;
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
        loadSegments ();
        final String rootName = root.toExternalForm();
        String slice = invertedSegments.get (rootName);
        if ( slice == null) {
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
            result.mkdir();
        }
        return result;
    }
    
    public static URL getClassFolder (final URL url) throws IOException {                
        URI u = getClassFolderImpl(url).toURI();
        String us = u.toString();
        if (!us.endsWith("/")) {
            u = URI.create(us + "/");
        }
        try {            
            return u.toURL();
        } catch (MalformedURLException mue) {
            //Should never happen
            ErrorManager.getDefault().notify (mue);
            return null;
        }
    }
    
    public static File getClassFolder (final File root) throws IOException {
        try {
            return getClassFolderImpl(root.toURI().toURL());
        } catch (MalformedURLException mue) {
            ErrorManager.getDefault().notify (mue);
            return null;
        }
    }
    
    private static File getClassFolderImpl (final URL url) throws IOException {
        final File dataFolder = getDataFolder (url);
        final File result= new File (dataFolder, CLASSES);
        if (!result.exists()) {
            result.mkdir();
        }
        return result;
    }
    
    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserProp = System.getProperty(NB_USER_DIR);        
            assert nbUserProp != null;
            final File nbUserDir = new File (nbUserProp);        
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
    
    /**
     * Only for unit tests!
     *
     */
    static synchronized void setCacheFolder (final File folder) {
        assert folder != null && folder.exists() && folder.canRead() && folder.canWrite();
        cacheFolder = folder;
    }
    
}
