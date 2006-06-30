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

/*
 * ZipUtils.java
 *
 * Created on September 17, 2002, 4:51 PM
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.*;

/**
 *
 * @author  mb115822
 */
public class ZipUtils {

    // buffer size for processing zips
    private static final int BUFFER_SIZE = 4096;

    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("ZipUtils."+message);
    }
    
    
    /** utility class with static methods only - no way to create it */
    private ZipUtils() {
    }
    
    public static void unpackZip(String zip, String dest) throws IOException {
        unpackZip(new File(zip), new File(dest),"");
    }
    
    public static void unpackZip(String zip, String dest, String fileToUnpack) throws IOException {
        unpackZip(new File(zip), new File(dest), fileToUnpack);
    }
    
    public static void unpackZip(File zipFile, File destFile) throws IOException {
        unpackZip(zipFile,destFile,"");
    }
    
    public static void unpackZip(File zipFile, File destFile, String fileToUnpack) throws IOException {
        
        debugInfo("unpackZip zip:"+zipFile+" to:"+destFile+" fileToUnpack:"+fileToUnpack);
        
        // buffer declaration
        byte[] buffer = new byte[BUFFER_SIZE];
        
        // create output dir
        if (!destFile.exists()) {
            destFile.mkdirs();
        }
        
        // create canonical destFile
        File canonicalDestFile = destFile.getCanonicalFile();
        
        if (!zipFile.exists()) {
             debugInfo("unpackZip: File "+zipFile.getName()+" does not exist");
            throw new IllegalArgumentException("Zip "+zipFile.getName()+" does not exist");
        }
        FileInputStream fis = null;
        ZipInputStream zis = null;
        FileOutputStream out = null;
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            ZipEntry entry = zis.getNextEntry();
            
            if (entry == null) {
                 debugInfo("unpackZip: Not a valid zip file - does not have entry");
                fis.close();
                zis.close();
                throw new IOException("File "+zipFile.getName()+"Not a valid zip file - does not have Zip entry");
            }
            
            
            while (entry != null) {
                String entryName = entry.getName();
                if (entryName.startsWith(fileToUnpack)) {
                    String outFilename = canonicalDestFile.getAbsolutePath()+File.separator+entryName;
                    debugInfo("unpackZip: Extracting "+outFilename);
                    if (entry.isDirectory()) {
                        File dir  = new File(outFilename);
                        boolean result = false;
                        if (dir.isDirectory()) {
                            result = true;
                        } else {
                            result = dir.mkdirs();
                        }
                        debugInfo("unpackZip: Making directory");
                        if (result != true) {
                            // we have problem ---
                            throw new IOException("Directory cannot be created:"+outFilename);
                        }
                    } else {
                        // check for parent dir existence ...
                        File outParentDir  = new File(outFilename).getParentFile();
                        if (!outParentDir.exists()) {
                            // create it
                            debugInfo("unpackZip: Making directory");
                            if (!outParentDir.mkdirs()) {
                                throw new IOException("Directory cannot be created:"+outFilename);
                            }
                        }
                        
                        debugInfo("unpackZip: Extracting file");                        
                        out = new FileOutputStream(outFilename);
                        
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer,0,bytesRead);
                        }
                        out.close();
                    }
                }
                // next entry
                entry = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
    
    // zip creation methods
    public static void createZip(File zipFile, File[] archivedFiles, File zipRootDir) throws IOException {
        debugInfo("createZip: Packcking zip:"+zipFile+" with "+archivedFiles.length+" files");
       
        // are we gona cut zipRootDir from archived files
        String zipRoot = null;
        if (zipRootDir != null) {
               if (zipRootDir.isDirectory()) {
                   zipRoot = zipRootDir.getAbsolutePath();
               } else {
                   throw new IOException("Specified ZIP root dir is not a valid directory: "+zipRootDir);
               }
        }
               
        // buffer declaration
         byte[] buffer = new byte[BUFFER_SIZE];
                                
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        
        ArrayList allArchivedFiles = scanAllFiles(archivedFiles);        
        Iterator i = allArchivedFiles.iterator();
        while (i.hasNext()) {
            File archivedFile = (File)i.next();
            FileInputStream is = new FileInputStream(archivedFile);
            
            // create a zip entry
            String zipEntryName;
            if (zipRoot != null) {
                
                String fileAbsolutePath = archivedFile.getAbsolutePath();
                int indexOfRoot = fileAbsolutePath.lastIndexOf(zipRoot);
                if (indexOfRoot == -1) {
                    zipEntryName = fileAbsolutePath;
                } else {
                    zipEntryName = fileAbsolutePath.substring(zipRoot.length()+1);
                }
            } else {
                zipEntryName = archivedFile.getAbsolutePath();
            }
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            // put it to zip stream
            debugInfo("createZip: adding entry "+zipEntry);
            zipOutputStream.putNextEntry(zipEntry);
            
            // archive the file
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                zipOutputStream.write(buffer,0,bytesRead);
            }
            // archiving done -> close the is
            is.close();
            
        } // next entry
        
        // close zip
        zipOutputStream.close();
    }
    
    // get all files, including those in directories
    private static ArrayList scanAllFiles(File[] inputFiles) throws IOException {        
        ArrayList fileList = new ArrayList();
        for (int i=0; i<inputFiles.length; i++) {
            File aFile = inputFiles[i];
            if (aFile != null) {
                if (aFile.isDirectory()) {
                    debugInfo("scanAllFiles: scanning directory "+aFile);
                    fileList.addAll(scanAllFiles(aFile.listFiles()));
                } else if (aFile.isFile()) {
                    //debugInfo("scanAllFiles: adding file "+aFile);
                    fileList.add(aFile);
                } else {
                    throw new IOException("File "+aFile.getPath()+" is not a normal file or a directory, cannot create a zip archive");
                }
            }
        }
        return fileList;
    }
        
    
    

}
