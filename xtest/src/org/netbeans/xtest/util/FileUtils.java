/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * File Utilities - this class contains only static methods, which 
 *                  perform various file operations, like move, copy, unzip ...
 */

package org.netbeans.xtest.util;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

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
    
    
    /** Normalizes name, so everythinh is in lower case
     * and spaces are converted to underscored
     *
     */
    public static String normalizeName(String name) {
        String newName = name.toLowerCase().replace(' ','_');
        return newName;
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
    
    
    public static boolean unpackZip(String zip, String dest) {
        return unpackZip(new File(zip), new File(dest),"");
    }
    
    public static boolean unpackZip(String zip, String dest, String fileToUnpack) {
        return unpackZip(new File(zip), new File(dest), fileToUnpack);
    }
    
    public static boolean unpackZip(File zipFile, File destFile) {
        return unpackZip(zipFile,destFile,"");
    }
    
    public static boolean unpackZip(File zipFile, File destFile, String fileToUnpack) {
        
        if (DEBUG) System.out.println("Unpacking zip:"+zipFile+" to:"+destFile+" fileToUnpack:"+fileToUnpack);
        
        
        if (!destFile.exists()) {
            destFile.mkdirs();
        }        
        if (!zipFile.exists()) {
            if (DEBUG) System.out.println("unpackZip: File "+zipFile.getName()+" does not exist");
            return false;
        }
        FileInputStream fis = null;
        ZipInputStream zis = null;
        FileOutputStream out = null;
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            ZipEntry entry = zis.getNextEntry();
            
            if (entry == null) {
                if (DEBUG) System.out.println("Not a valid zip file - does not have entry");
                fis.close();
                zis.close();
                return false;
            }
                   
                       
            while (entry != null) {
                String entryName = entry.getName();
                if (entryName.startsWith(fileToUnpack)) {
                    String outFilename = destFile.getAbsolutePath()+File.separator+entryName;                    
                    if (DEBUG) System.out.println("Extracting "+outFilename);
                    if (entry.isDirectory()) {
                        File dir  = new File(outFilename);
                        boolean result = false;
                        if (dir.isDirectory()) {
                            result = true;
                        } else {
                            result = dir.mkdirs();
                        }
                        if (DEBUG) System.out.println("Making directory");
                        if (result != true) {
                            // we have problem ---
                            throw new IOException("Directory cannot be created:"+outFilename);
                        }
                    } else {
                        if (DEBUG) System.out.println("Extracting file");
                        try {
                            out = new FileOutputStream(outFilename);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                out.write(buffer,0,bytesRead);
                            }
                            out.close();
                        } catch (IOException ioe) {
                            if (DEBUG) {
                                System.out.println("IOException "+ioe);
                                ioe.printStackTrace();
                            }
                            // we have problem ...
                            out.close();
                            zis.close();
                            fis.close();
                            return false;
                        }
                    }
                }
                // next entry
                entry = zis.getNextEntry();
                
            }
            zis.close();
            fis.close();
            
        } catch (IOException ioe) {
            if (DEBUG) {
                System.out.println("IOException "+ioe);
                ioe.printStackTrace();
            }
            try {
                if (DEBUG) System.out.println("Another try to close the files");
                zis.close();
                fis.close();
            } catch (Exception e) {
            }
            return false;
        }
        return true;
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
        
              
       
    
}
