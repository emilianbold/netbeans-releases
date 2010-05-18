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

package org.netbeans.modules.iep.project.anttasks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Bing Lu
 * @todo Document this class
 */
public class DirectoryUtil extends Object {
    /**
     * Creates new DirectoryUtil
     */
    public DirectoryUtil() {
    }


    /**
     * This method will return an arraylist containing all the file/s name/s in
     * the provided directory name
     *
     * @param fromDir Current directory where files are located.
     * @return ArrayList - could be zero in length if directory is empty or no
     *      found.
     */
    public static List getFiles(String fromDir) {
        // create the object to be return as a result
        ArrayList filesList = new ArrayList();
        if (dirExists(fromDir)) {
            // yes
            File aDir = new File(fromDir);
            // create the directory class
            String files[] = aDir.list();
            // list all the file in the directory
            int numFiles = files.length;
            // get the file count
            for (int i = 0; i < numFiles; i++) {
                // update the array list with the file names
                String fileName = fromDir
                         + System.getProperty("file.separator")
                         + files[i];
                if (dirExists(fileName) == false) {
                    // do not add sub directory to the list
                    filesList.add(files[i]);
                }
            }
        }
        return filesList;
    }


    /**
     * @param sourceDir source dir to be moved
     * @param targetDir target : where it will be moved to
     * @param excludeExt exclude files with these extension for the move
     * @todo Document this method
     */
    public static void moveTree(File sourceDir, File targetDir, String[] excludeExt) {
        FileFilter filter = new ExtensionFilter(excludeExt, false);
        moveFilesInDir(sourceDir, targetDir, filter, true);
    }


    /**
     * this util method construct the absolute path from its parts. it will
     * remove any preamble to the file name prior to concatenate the path
     * elements.
     *
     * @param aDir - the directory part of the absolute path
     * @param aFileName - the file part of the absolute path
     * @return string absolute path of the parameters provided.
     */
    public static String buildFullPath(String aDir, String aFileName) {
        String currentDir = ".";
        String fullPath = "";
        if (aFileName.startsWith(currentDir)) {
            fullPath = aDir + aFileName.substring(1);
        } else {
            if (aFileName.startsWith(System.getProperty("file.separator"))) {
                fullPath = aDir + aFileName;
            } else {
                fullPath = aDir + System.getProperty("file.separator") + aFileName;
            }
        }
        return fullPath;
    }


    /**
     * Copy one file's contents to another file.
     *
     * @param source The file to copy.
     * @param dest The file to copy to.
     * @return true if the copy succeeded, false otherwise.
     */
    public static boolean copyFile(File source, File dest) {
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            copyStream(in, out);
        } catch (Exception e) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // empty!
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // empty!
                }
            }
        }

        return true;
    }


    /**
     * @param source source directory to copy
     * @param destDir target directory where it will be copied to
     * @return whether copy was successfule or not
     * @todo Document this method
     */
    public static boolean copyFile(String source, String destDir) {
        FileInputStream fisource = null;
        FileOutputStream fodest = null;

        try {
            File fsource = new File(source);
            fisource = new FileInputStream(fsource);
            File fdest = new File(destDir, fsource.getName());
            fodest = new FileOutputStream(fdest);
            copyStream(fisource, fodest);
        } catch (Throwable e) {
            return false;
        } finally {
            try {
                fisource.close();
            } catch (Throwable e) {
                // empty!
            }
            try {
                fodest.close();
            } catch (Throwable e) {
                // empty!
            }
        }
        return true;
    }


    /**
     * @param sourceDir source directory
     * @param targetDir target directory
     * @param filter file filter
     * @param recursive TODO: document me!
     * @todo Document this method
     */
    public static void copyFilesInDir(File sourceDir, File targetDir, FileFilter filter, boolean recursive) {
        // Create target directory
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File[] children = sourceDir.listFiles(filter);
        for (int i = 0; i < children.length; ++i) {
            if (children[i].isDirectory() && recursive) {
                copyFilesInDir(children[i], new File(targetDir, children[i].getName()), filter, recursive);
            } else {
                copyFile(children[i], new File(targetDir, children[i].getName()));
            }
        }
    }


    /**
     * @param is input stream
     * @param os output stream
     * @exception IOException rethrow exception from streams
     * @todo Document this method
     */
    public static void copyStream(InputStream is, OutputStream os)
        throws IOException {
        int bytesRead = 0;
        byte[] memBuf = new byte[32768];

        while (bytesRead != -1) {
            bytesRead = is.read(memBuf);
            if (bytesRead != -1) {
                os.write(memBuf, 0, bytesRead);
            }
        }
        os.flush();
    }


    /**
     * @param r reader object
     * @param w writer object
     * @exception IOException rethrow exception from streams
     * @todo Document this method
     */
    public static void copyStream(Reader r, Writer w)
        throws IOException {
        int nRead;
        char[] memBuf = new char[32768];
        while ((nRead = r.read(memBuf)) != -1) {
            w.write(memBuf, 0, nRead);
        }
        w.flush();
    }


    /**
     * This method copies recursively copies all the files in a directory to
     * another directory, maintaining the source directory's child directory
     * structure. If any particular file type should be excluded, it's extension
     * may be specified using the excludeExt argument.
     *
     * @param sourceDir The directory to copy files from.
     * @param targetDir The directory to copy files to.
     * @param excludeExt Array of string extensions of form .ext to exclude from
     *      the copy.
     */
    public static void copyTree(File sourceDir, File targetDir, String[] excludeExt) {
        FileFilter filter = new ExtensionFilter(excludeExt, false);
        copyFilesInDir(sourceDir, targetDir, filter, true);
    }


    /**
     * This method will create a directory and its parent directories. The
     * specified directory cannot be created if a file exists with the same
     * name.
     *
     * @param dirName Full pathname of directory to be created.
     * @return boolean Returns true if directory already exists or was created
     *      sucessfully, false otherwise.
     */
    public static boolean createDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            // create the directories if they don't exist
            return dir.mkdirs();
        } else {
            // directory already exists
            return true;
        }
    }


    /**
     * This method will recursively delete a directory and all its files and
     * subdirectories.
     *
     * @param dirName The full path of the directory to delete
     * @return Returns true if directory was deleted, false otherwise.
     */
    public static boolean deleteDir(String dirName) {
        return deleteDir(new File(dirName));
    }


    /**
     * This method will recursively delete a directory and all its files and
     * subdirectories.
     *
     * @param dir The directory to delete
     * @return Returns true if directory was deleted, false otherwise.
     */
    public static boolean deleteDir(File dir) {
        return deleteDir(dir, true);
    }


    /**
     * This method will recursively delete a directory and all its files and
     * subdirectories.
     *
     * @param dir The directory to delete
     * @param deleteOriginalDir Whether or not to delete the original directory.
     * @return Returns true if directory was deleted, false otherwise.
     */
    public static boolean deleteDir(File dir, boolean deleteOriginalDir) {
        if (!dir.isDirectory() || !dir.exists()) {
            return false;
        }

        // go through files in directory and delete one at a time
        File file[] = dir.listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory()) {
                deleteDir(file[i]);
            }
            file[i].delete();
        }
        // try to delete directory itself
        if (deleteOriginalDir) {
            return dir.delete();
        }

        return true;
    }


    /**
     * This method will delete a single file.
     *
     * @param filename Full pathname of the file to delete.
     * @return boolean Returns true if file was deleted, false otherwise.
     */
    public static boolean deleteFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return false;
        }

        if (file.isFile() && file.canRead()) {
            return file.delete();
        }
        return false;
    }


    /**
     * This method will delete a single file.
     *
     * @param filename Name of the file.
     * @param dirName Directory where the file can be found.
     * @return boolean Returns true if file was deleted, false otherwise.
     */
    public static boolean deleteFile(String filename, String dirName) {
        return deleteFile(dirName + File.separator + filename);
    }


    /**
     * This method will check if a file exists.
     *
     * @param dirName Full pathname of the directory.
     * @return boolean Returns true if the directory exists, false otherwise
     */
    public static boolean dirExists(String dirName) {
        File dir = new File(dirName);
        return (dir.exists() && dir.isDirectory());
    }


    /**
     * This method will check if a file exists.
     *
     * @param filename Full pathname of the file.
     * @return boolean Returns true if the file exists and is not a directory,
     *      false otherwise
     */
    public static boolean fileExists(String filename) {
        File file = new File(filename);
        return (file.exists() && !file.isDirectory());
    }


    /**
     * This method will move a single file from one directory to another.
     *
     * @param filename Full path name of the file.
     * @param toDir Directory where to move file to.
     * @return boolean Returns true if file was moved successfully, false
     *      otherwise.
     */
    public static boolean moveFile(String filename, String toDir) {
        char fs = System.getProperty("file.separator").charAt(0);
        File file = new File(filename);
        // check if the source file exist
        if (!file.exists()) {
            return false;
        }
        // check to see if the destination file exist
        String destinationFile = toDir + fs + file.getName();
        File desFile = new File(destinationFile);
        if (desFile.exists()) {
            // file exists, remove it to allow the move to complete
            deleteFile(file.getName(), toDir);
        }
        // attempt to create the destination directory if it doesn't exist
        createDir(toDir);
        // attempt to move the file
        if (file.isFile() && file.canRead()) {
            return file.renameTo(new File(toDir, file.getName()));
        }
        return false;
    }


    /**
     * This method will move a single file from one directory to another.
     *
     * @param toDir Directory where to move file to.
     * @param file file that will be moved
     * @return boolean Returns true if file was moved successfully, false
     *      otherwise.
     */
    public static boolean moveFile(File file, String toDir) {
        char fs = System.getProperty("file.separator").charAt(0);
        //File file = new File(filename);
        // check if the source file exist
        if (!file.exists()) {
            return false;
        }

        // check to see if the destination file exist

        String destinationFile = toDir + fs + file.getName();
        File desFile = new File(destinationFile);

        if (desFile.exists()) {
            // file exists, remove it to allow the move to complete
            deleteFile(file.getName(), toDir);
        }
        // attempt to create the destination directory if it doesn't exist
        createDir(toDir);
        // attempt to move the file
        if (file.isFile() && file.canRead()) {
            return file.renameTo(new File(toDir, file.getName()));
        }
        return false;
    }


    /**
     * This method will more a single file from one directory to another.
     *
     * @param filename Name of the file.
     * @param fromDir Current directory where file is located.
     * @param toDir Directory where to move file to.
     * @return boolean Returns true if file was moved successfully, false
     *      otherwise.
     */
    public static boolean moveFile(String filename, String fromDir, String toDir) {
        return moveFile(fromDir + File.separator + filename, toDir);
    }


    /**
     * @param sourceDir source directory
     * @param targetDir target directory
     * @param filter file filter
     * @param recursive TODO: document me!
     * @todo Document this method
     */
    public static void moveFilesInDir(File sourceDir, File targetDir, FileFilter filter, boolean recursive) {

        char fs = System.getProperty("file.separator").charAt(0);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File[] children = sourceDir.listFiles(filter);
        // Create target directory

        for (int i = 0; i < children.length; ++i) {
            if (children[i].isDirectory() && recursive) {
                moveFilesInDir(children[i], new File(targetDir,
                        children[i].getName()), filter, recursive);
            } else {
                moveFile(children[i], targetDir.getAbsolutePath());
            }
        }

    }

    public static List getFilesRecursively(File dir, FileFilter filter) {
        List ret = new ArrayList();
        if (!dir.isDirectory()) {
            return ret;
        }
        File[] fileNdirs = dir.listFiles(filter);
        for (int i = 0, I = fileNdirs.length; i < I; i++) {
            if (fileNdirs[i].isDirectory()) {
                ret.addAll(getFilesRecursively(fileNdirs[i], filter));
            } else {
                ret.add(fileNdirs[i]);
            }
        }
        return ret;
    }

    public static List getFilesRecursively(File dir, String[] extensions) {
        ExtensionFilter filter = new ExtensionFilter(extensions);
        return getFilesRecursively(dir, filter);
    }


}
