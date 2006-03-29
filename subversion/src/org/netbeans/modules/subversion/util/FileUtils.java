/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.util;

import java.io.*;

/**
 *
 * @author pkuzel
 */
public class FileUtils {
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        InputStream inputStream = null;
        try {
            inputStream = createInputStream(sourceFile);            
            copyStreamToFile(inputStream, targetFile);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    /**
     * Copies the specified sourceFile to the specified targetFile.
     * It <b>closes</b> the input stream.
     */
    public static void copyStreamToFile(InputStream inputStream, File targetFile) throws IOException {
        if (inputStream == null || targetFile == null) {
            throw new NullPointerException("sourcStream and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        OutputStream outputStream = null;
        try {            
            outputStream = createOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }
            catch (IOException ex) {
                targetFile.delete();
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }


    /**
     * Do the best to rename the file.
     * @param orig regular file
     * @param dest regular file (if exists it's rewritten)
     */
    public static void renameFile(File orig, File dest) throws IOException {
        boolean destExists = dest.exists();
        if (destExists) {
            for (int i = 0; i<3; i++) {
                if (dest.delete()) {
                    destExists = false;
                    break;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        if (destExists == false) {
            for (int i = 0; i<3; i++) {
                if (orig.renameTo(dest)) {
                    return;
                }
                try {
                    Thread.sleep(71);
                } catch (InterruptedException e) {
                }
            }
        }

        // requires less permisions than renameTo
        FileUtils.copyFile(orig, dest);

        for (int i = 0; i<3; i++) {
            if (orig.delete()) {
                return;
            }
            try {
                Thread.sleep(71);
            } catch (InterruptedException e) {
            }
        }
        throw new IOException("Can not delete: " + orig.getAbsolutePath());  // NOI18N
    }

    /**
     * This utility class needs not to be instantiated anywhere.
     */
    private FileUtils() {
    }
    
    public static BufferedInputStream createInputStream(File file) throws IOException {
        int retry = 0;
        while (true) {   
            try {
                return new BufferedInputStream(new FileInputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
    public static BufferedOutputStream createOutputStream(File file) throws IOException {
        int retry = 0;
        while (true) {            
            try {
                return new BufferedOutputStream(new FileOutputStream(file));                
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }       
    }
    
}
