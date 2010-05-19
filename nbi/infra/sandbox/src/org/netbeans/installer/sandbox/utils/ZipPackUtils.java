/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.sandbox.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public class ZipPackUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String PACK_SUFFIX = ".pack.gz"; //NOI18N
    public static final String ZIP_SUFFIX  = ".zip"; //NOI18N
    
    private static final PackedFilter PACKED_FILTER = new PackedFilter(PACK_SUFFIX);
    private static final JarsFilter   JAR_FILTER    = new JarsFilter();
    
    private static final int BUFFER_SIZE = 102400;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Packer   packer   = null;
    private static Unpacker unpacker = Pack200.newUnpacker();
    
    /**
     * Unzip file <b>filename</b> to the directory <b>outpuDir</b>.
     *
     * @return   List of unzipped files (<i>null</i> if filename or outputDir is <i>null</i>)
     * @param    zipfile ZIP file
     * @param    outputDir Directory for ZIP file to be extracted
     * @param    unpack Unpack or not packed files (.pack.gz)
     * @param    useZipModificationTime Set modification time of unzipped file to zip entry time
     * @param    progress
     *            Progress instance to display detail and set percentage.
     *           <br>If <b>progress</b> is <i>null</i> then nothing would be displayed
     * @param    conditionList List of conditions to be added to every item of
     *           returning InstallationFileObject
     */
    public static List<File> unzip(File zipfile, String outputDir,  boolean unpack, boolean useZipModificationTime, Progress progress) {
        return unzip(zipfile.getPath(),outputDir,
                unpack, useZipModificationTime, progress);
    }
    
    /**
     * Unzip file <b>filename</b> to the directory <b>outpuDir</b>.
     *
     * @return   List of unzipped files.
     *           If filename or outputDir is <i>null</i> then return <i>null</i>.
     * @param    zipfile ZIP file name
     * @param    outputDir Directory for ZIP file to be extracted
     * @param    unpack Unpack or not packed files (.pack.gz)
     * @param    useZipModificationTime
     *           Set modification time of unzipped file to zip entry time
     * @param    progress
     *           Progress instance to display detail and set percentage.
     *           <br>If <b>progress</b> is <i>null</i> then nothing would be
     *           displayed.
     */
    public static List<File> unzip(File zipfile, File outputDir, boolean unpack, boolean useZipModificationTime, Progress progress) {
        return unzip(zipfile.getPath(),outputDir.getAbsolutePath(),
                unpack, useZipModificationTime, progress);
    }
    
    /**
     * Unzip file <b>filename</b> to the directory <b>outpuDir</b>.
     *
     * @return   List of unzipped files (<i>null</i> if filename or outputDir is <i>null</i>)
     * @param    filename ZIP file name
     * @param    outputDir Directory for ZIP file to be extracted
     * @param    unpack Unpack or not packed files (.pack.gz)
     * @param    useZipModificationTime Set modification time of unzipped file to zip entry time
     * @param    progress Progress instance to display detail and set percentage.
     *           <br>If <b>progress</b> is <i>null</i> then nothing would be displayed
     */
    public static List<File> unzip(String filename, File outputDir,  boolean unpack, boolean useZipModificationTime, Progress progress) {
        
        return unzip(filename,outputDir.getAbsolutePath(),
                unpack, useZipModificationTime, progress);
    }
    
    /**
     * Unzip file <b>filename</b> to the directory <b>outpuDir</b>.
     *
     * @return   List of unzipped files.
     *           If filename or outputDir is <i>null</i> then return <i>null</i>
     * @param    filename ZIP file name
     * @param    outputDir Directory for ZIP file to be extracted
     * @param    unpack Unpack or not packed files (.pack.gz)
     * @param    useZipModificationTime
     *           If set modification time of unzipped file to zip entry time
     * @param    progress Progress instance to display detail and set percentage.
     * @param    conditionList List of conditions
     *           <br>If <b>progress</b> is <i>null</i> then nothing would be displayed
     */
    public static List<File> unzip(String filename, String outputDir, boolean unpack, boolean useZipModificationTime, Progress progress) {
        List<File> entriesList = new ArrayList<File> ();
        
        if ( !isInputParametrsOK(filename,outputDir)) {
            return entriesList;
        }
        ZipInputStream zis=null;
        
        try {
            LogManager.log(ErrorLevel.MESSAGE,
                    "Extract files from " +  //NOI18N
                    (new File(filename)).getName()+" ..."); //NOI18N
            
            zis = new ZipInputStream(new FileInputStream(filename));
            ZipFile zf = new ZipFile(filename);
            double entriesTotalNumber = zf.size();
            double entriesUnzipped = 0.0;
            
            // create output dir if it does not exist
            File outputDirFile = new File(outputDir);
            createOutputDir(outputDirFile,entriesList);            
            
            //extract each entry
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry())!= null) {
                if (progress.isCanceled()) return entriesList;
                
                String name = zipentry.getName();
                String entryName = new File(outputDir + File.separator + name).
                        getCanonicalPath();
                
                LogManager.log(ErrorLevel.MESSAGE,
                        "    unzipping " + name + " to " + //NOI18N
                        outputDir);
                
                setProgressDetail(progress,entryName, entriesUnzipped,
                        entriesTotalNumber);
                entriesUnzipped++;
                
                File createdFile;
                
                if(zipentry.isDirectory()) {
                    createdFile = new File(entryName);
                    createOutputDir(createdFile, entriesList);
                } else {
                    createOutputDir(new File(entryName).getParentFile(), entriesList);
                    createdFile = new File(unpack(zis,entryName,unpack));                    
                    entriesList.add(createdFile);
                    zis.closeEntry();
                }
                
                // set last modification time from the source
                if(useZipModificationTime) {
                    createdFile.setLastModified(zipentry.getTime());
                }
            }
            zis.close();
        } catch (ZipException e) {
            LogManager.log(ErrorLevel.MESSAGE,
                    "Wrong zip file: " + filename); //NOI18N
        } catch (FileNotFoundException e) {
            LogManager.log(ErrorLevel.MESSAGE,
                    "File " + filename + "was not found"); //NOI18N
        } catch (IOException e) {
            LogManager.log(ErrorLevel.MESSAGE,
                    "I/O Error on file: " + filename); //NOI18N
        } finally {
            try {
                LogManager.log(ErrorLevel.MESSAGE,
                        "Extract finished."); //NOI18N
                zis.closeEntry();
                zis.close();
            } catch(IOException e) {
                e = null;// do nothing
            } catch(NullPointerException e) {
                e = null;// do nothing
            }
        }
        return entriesList;
    }

    /**
     * Packs the file. If the file is a directory is will be navigated recursively.
     *
     * @param file File to pack.
     * @param includes Includes list for packer.
     */
    public static void packFile(File file, List<String> includes) {
        if (packer == null) {
            packer = Pack200.newPacker();
            
            Map<String, String> properties = packer.properties();
            
            // properties.put(Packer.SEGMENT_LIMIT, "-1");
            // properties.put(Packer.EFFORT, "9");
            properties.put(Packer.KEEP_FILE_ORDER, Packer.TRUE);
            properties.put(Packer.DEFLATE_HINT, Packer.KEEP);
            properties.put(Packer.MODIFICATION_TIME, Packer.KEEP);
            properties.put(Packer.UNKNOWN_ATTRIBUTE, Packer.PASS);
        }
        
        if (file.isDirectory()) {
            File[] children = file.listFiles(JAR_FILTER);
            
            for (File child: children) {
                packFile(child, includes);
            }
        } else {
            if (includes.contains(file.getName())) {
                try {
                    // correct path
                    File canonicalFile = file.getCanonicalFile();
                    
                    LogManager.log(ErrorLevel.MESSAGE,
                            "    compressing file: " + //NOI18N
                            canonicalFile.getAbsolutePath());
                    
                    JarFile jarFile = new JarFile(canonicalFile);
                    FileOutputStream outputStream =
                            new FileOutputStream(
                            new File(canonicalFile.getAbsolutePath() + PACK_SUFFIX));
                    
                    packer.pack(jarFile, outputStream);
                    
                    jarFile.close();
                    outputStream.close();
                    
                    if (!canonicalFile.delete()) {
                        LogManager.log(ErrorLevel.MESSAGE,
                                "    ... cannot delete, deleting on exit"); //NOI18N
                        canonicalFile.deleteOnExit();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Unpacks the file. If the file is a directory it is browsed recursively.
     *
     * @param file Fiel to unpack
     * @throws java.io.IOException if an I/O error occurs
     * @return List of unpacked files
     */
    public static List<String> unpackFile(File file) throws IOException {
        List<String> unpackedFilesList = new ArrayList<String>();
        
        if (file.isDirectory()) {
            for (File child: file.listFiles(PACKED_FILTER)) {
                unpackedFilesList.addAll(unpackFile(child));
            }
        } else {
            String path = file.getCanonicalPath();
            
            if(!path.endsWith(PACK_SUFFIX)) {
                unpackedFilesList.add(path);
                return unpackedFilesList;
            }
            
            LogManager.log("    decompressing file: " + path);
            
            String target = path.substring(0, path.length() - PACK_SUFFIX.length());
            JarOutputStream output = null;
            try {
                output = new JarOutputStream(new FileOutputStream(new File(target)));
                unpacker.unpack(file, output);
                unpackedFilesList.add(target);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        ErrorManager.notify(ErrorLevel.DEBUG, e);
                    }
                }
            }
            
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
        
        return unpackedFilesList;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static void createOutputDir(File outputDirFile, List<File> list) {
        if(!outputDirFile.exists()) {
            createOutputDir(outputDirFile.getParentFile(),list);
            if(outputDirFile.mkdir()) {
                list.add(outputDirFile);
            }
        }
        return;
        
    }
    
    private static boolean isInputParametrsOK(String filename, String outputDir) {
        if(filename == null) {
            LogManager.log(ErrorLevel.WARNING,
                    "Null filename for unzipping"); //NOI18N
            return false;
        }
        if(outputDir == null) {
            LogManager.log(ErrorLevel.WARNING,
                    "Null output dir for unzipping"); //NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Set progress values.
     * @param progress The <code>Progress</code> instance
     */
    private static void setProgressDetail(Progress progress, String entryName, double unzipped, double total) {
        if (progress != null) {
            int percentage = (int) ((unzipped * Progress.COMPLETE) / total);
            
            progress.setDetail(StringUtils.format("Extracting {0}", entryName));
            progress.setPercentage(percentage);
        }
    }
    
    /** 
     * Unpack data from <b>zis</b> tp file with path <b>name</b>.
     *
     * @param zis ZipInputStream for unpacking
     * @param path Output file path
     * @param unpack If unpack .pack.gz files automatically
     *
     * @return Written file name
     */
    private static String unpack(ZipInputStream zis, String path, boolean unpack) throws IOException {
        int n;
        String writedFile;
        boolean needUnpack = path.endsWith(PACK_SUFFIX) && unpack;
        OutputStream os = needUnpack ? 
            new ByteArrayOutputStream(BUFFER_SIZE) : new FileOutputStream(path);
        
        byte[] buf = new byte[BUFFER_SIZE];
        
        while ((n = zis.read(buf, 0, BUFFER_SIZE)) > -1) {
            os.write(buf, 0, n);
        }
        writedFile = needUnpack ?
            unpackPackedJar((ByteArrayOutputStream)os,path) :
            path;
        
        os.close();
        
        return writedFile;
    }
    
    /** Unpack packed jar from <b>baos</b> to file with path <b>filename</b>.
     *  @param baos ByteArrayOutputStream with packed data
     *  @param filename Packed Jar file name
     *  @return  Unpacked jar filename
     */
    private static String unpackPackedJar(ByteArrayOutputStream baos,String filename) throws IOException {
        String unpackedFile = filename.substring( 0,
                filename.length() - PACK_SUFFIX.length());
        
        JarOutputStream outputStream = new JarOutputStream(
                new FileOutputStream(
                new File(unpackedFile)));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        unpacker.unpack(bais,outputStream);
        outputStream.close();
        return unpackedFile;
        
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private ZipPackUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    /**
     * A file filter that accepts only jar files and directories.
     */
    private static class JarsFilter implements FileFilter {
        /**
         * Checks whether the file is a jar file.
         *
         * @param file File to examine
         * @return true if the file is a jar file or a directory
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            
            return file.getName().endsWith(DOT_JAR) ||
                    file.getName().endsWith(DOT_WAR) ||
                    file.getName().endsWith(DOT_EAR);
            
        }
        
        public static final String DOT_JAR = ".jar"; //NOI18N
        public static final String DOT_WAR = ".war"; //NOI18N
        public static final String DOT_EAR = ".ear"; //NOI18N
    }
    
    /**
     * A file filter that accepts only packed jar files and directories.
     */
    private static class PackedFilter implements FileFilter {
        private String suffix;
        
        /**
         * Creates a new instance of PackedJarsFilter.
         *
         * @param aSuffix
         *          The suffix for the packed jar files.
         */
        public PackedFilter(String aSuffix) {
            suffix = aSuffix;
        }
        
        /**
         * Checks whether the file is a packed jar file.
         *
         * @param file File to examine
         * @return true if the file is a jar file or a directory
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            
            return file.getName().endsWith(suffix);
        }
    }
}
