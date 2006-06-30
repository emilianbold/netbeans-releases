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
 * File Utilities - this class contains only static methods, which
 *                  perform various file operations, like move, copy, unzip ...
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.*;


/**
 *
 * @author  breh
 */
public class FileUtils {

    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("FileUtils."+message);
    }

    /** private constructor, since all methods are static  */
    private FileUtils() {
    }
        
    
    // we depend on ant on for copying a file
    private static org.apache.tools.ant.util.FileUtils antFileUtils;
    

    /** checks if given file exists
     * @param aFile file to check
     * @throws IOException when file does not exist
     */    
    public static void checkFileExists(File aFile) throws IOException {
        if (!aFile.exists()) {
            throw new IOException("File "+aFile+" does not exist");
        }
    }
    

    /** check whether given file is a normal file
     * @param aFile file to check
     * @throws IOException when file is not a normal file
     */    
    public static void checkFileIsFile(File aFile) throws IOException {
        checkFileExists(aFile);
        if (!aFile.isFile()) {
            throw new IOException("File "+aFile+" is not a normal file");
        }
    }
    
    /** check whether file is a directory
     * @param aFile file to check
     * @throws IOException when file is not a directory
     */    
    public static void checkFileIsDirectory(File aFile) throws IOException {
        checkFileExists(aFile);
        if (!aFile.isDirectory()) {
            throw new IOException("File "+aFile+" is not a directory");
        }        
    }
    
    
    /** Normalizes name, so everything is in lower case and spaces are converted to
     * underscores
     * @param name filename to normalize
     * @return 'normalized' name
     */
    public static String normalizeName(String name) {
        String newName = name.toLowerCase().replace(' ','_');
        return newName;
    }
    
    
    public static File[] listFiles(File directory, final String prefix, final String suffix) {
        return listFiles(directory,prefix,suffix,0);
    }
    
    // list all files with given prefix and/or suffix
    public static File[] listFiles(File directory, final String prefix, final String suffix, final long modTime) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory argument is not a directory: "+directory.getPath());
        }
        // do the scan
        File files[] = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                if (prefix != null) {
                    if (!name.startsWith(prefix)) {
                        return false;
                    }
                }
                if (suffix != null) {
                    if (!name.endsWith(suffix)) {
                        return false;
                    }
                }
                if (modTime > 0 ) {
                    long currentTime = System.currentTimeMillis();
                    long fileModTime = file.lastModified();
                     // accept files older than modTime
                     if ((currentTime - fileModTime) <= modTime) {
                         return false;
                     }
                }
                return true;
            }
        });
        return files;
    }
    
    
    // list all subdirectories
    public static File[] listSubdirectories(File rootDir) {
        if (rootDir == null) {
            throw new IllegalArgumentException("rootDir cannot be null");
        }
        return rootDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });        
    }
    
    /** Copies files
     * @param fromFile from File object
     * @param toFile to File object
     * @paran move if move instead of copy
     * @throws IOException when copying is not succesfull
     */    
    public static void copyFile(File fromFile, File toFile, boolean move) throws IOException {
        if (antFileUtils==null) {
            antFileUtils = org.apache.tools.ant.util.FileUtils.newFileUtils();
        }
        antFileUtils.copyFile(fromFile,toFile);
        if (move) {
            if (!fromFile.delete()) {
                throw new IOException("cannot delete "+fromFile);
            }
        }
    }
    
    public static void copyFileToDir(File fromFile, File toDir, boolean move) throws IOException {
         File toFile = new File(toDir,fromFile.getName());
         copyFile(fromFile,toFile,move);
     }
    
    /**
     *
     *
     */
     public static void copyFile(File fromFile, File toFile) throws IOException {
        copyFile(fromFile,toFile,false);
    }
     
     public static void copyFileToDir(File fromFile, File toFile) throws IOException {
        copyFileToDir(fromFile,toFile,false);
    }
     

    
    /** moves directory
     * @param fromDir from Directory
     * @param toDir to Directory
     * @throws IOException
     */    
    public static void moveDir(File fromDir, File toDir) throws IOException {
        copyDir(fromDir, toDir, true);
    }
    
    public static void moveFile(File from, File to) throws IOException {
        copyFile(from, to, true);
    }
    
    public static void moveFileToDir(File fromFile, File toFile) throws IOException {
        copyFileToDir(fromFile,toFile,true);
    }
     

    public static void copyDir(File fromDir, File toDir, boolean move) throws IOException {
        if (!fromDir.isDirectory()) {
            throw new IOException("fromDir is not a directory:"+fromDir);
        }
        if (toDir.exists()) {
            if (!toDir.isDirectory()) {
                throw new IOException("toDir exists, but is not a directory:"+fromDir);
            }
        } else {
            if (!toDir.mkdirs()) {
                throw new IOException("cannot create toDir directory:"+toDir);
            }
        }
                
        
        // scan the files and copy them !!!
        String[] fileList = fromDir.list();
        for (int i=0; i< fileList.length; i++) {
            String source = fileList[i];
            File sourceFile = new File(fromDir,source);
            File outputFile = new File(toDir,source);
            if (!sourceFile.isDirectory()) {
                copyFile(sourceFile,outputFile,move);
            } else {
                copyDir(sourceFile,outputFile,move);
            }            
        }
    }
    
    
  
    
    /** delete directory including its contents
         * @param dirFile directory to delete (as File) 
         * @return false in the case of any problem
         */        
        public static boolean deleteDirectory(File dir, boolean onlySubdirectories) {
            if (!dir.isDirectory()) {
		throw new IllegalArgumentException(dir.getName()+" is not a directory.");
	    }
            File[] files = dir.listFiles();
            for (int i=0;i<files.length; i++) {
                File aFile = files[i];
                if (aFile.isDirectory()) {
                    deleteDirectory(aFile, false);                                        
                } else {
                    aFile.delete();
                }
            }
            if (!onlySubdirectories) {
                return dir.delete();
            } else {
                return true;
            }
        }
        
        /** delete file
         * @param dirFile directory to delete (as String) 
         * @return false in the case of any problem
         */        
        public static boolean deleteFile(File file) {           
            return file.delete();
        }
        
        
        public static boolean delete(String filename)  {
            File file = new File(filename);
            return delete(file);
        }
        
        
        public static boolean delete(File file)  {
            if (!file.exists()) {
                // well, it doesn't exist, so the work is already done :-)
                return true;
            }
            if (file.isDirectory()) {
                return deleteDirectory(file, false);
            } else {
                return deleteFile(file);
            }
        }
        
        
        // libor's method .... do we still use it ?
        public static void deleteDirContent(File dir) {
            File subfiles[] = dir.listFiles();
            boolean warning = false;
            if (subfiles != null && subfiles.length > 0) {
                for (int i=0; i<subfiles.length; i++) {
                    if (subfiles[i].isDirectory())
                        deleteDirContent(subfiles[i]);
                    else {
                        warning = true;
                        subfiles[i].delete();
                    }
                }
                if (warning) {
                    File warn_file = new File(dir,"content_of_this_directory_was_deleted");
                    try { warn_file.createNewFile(); }
                    catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
        
        public static boolean deleteFiles(File[] files) {
            boolean result = true;
            for (int i=0; i<files.length; i++) {
                if (files[i] != null) {
                    result &= delete(files[i]);
                }
            }
            return result;
        }
        
              
       
    
}
