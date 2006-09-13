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
 *
 * $Id$
 */
package org.netbeans.installer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.zip.CRC32;
import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 *
 *
 *
 * @author ks152834
 */
public abstract class FileUtils {
    ////////////////////////////////////////////////////////////////////////////
    // Static
    private static FileUtils instance;
    
    public static synchronized FileUtils getInstance() {
        if (instance == null) {
            if (SystemUtils.getInstance().isWindows()) {
                instance = new GenericFileUtils();
            } else {
                instance = new UnixFileUtils();
            }
        }
        
        return instance;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Reads a file into a string.
     *
     * @param file The file to read
     * @throws java.io.IOException if an I/O error occurs
     * @return the contents of the file
     */
    public abstract String readFile(File file) throws IOException;
    
    /**
     * Writes the given contents to the file, overwriting its current contents.
     *
     * @param file the file to write to
     * @param string the string to write
     * @throws java.io.IOException if an I/O error occurs
     */
    public abstract void writeFile(File file, String string) throws IOException;
    
    public abstract void appendFile(File file, String string) throws IOException;
    
    /**
     * Writes the given contents to the file, wither overwriting or appending to
     * its current contents.
     *
     * @param file the file to write to
     * @param string the string to write
     * @param append whether to overwrite the current contents or append to them
     * @throws java.io.IOException if an I/O error occurs
     */
    public abstract void writeFile(File file, String string, boolean append) throws IOException;
    
    public abstract Date getLastModified(String fname);
    
    public abstract Date getLastModified(File f);
    
    /**
     * Returns size of <b>file</b>.
     *
     * @param file
     *      File
     * @return
     *  -1 if
     *         <b>file</b> is <i>null</i><br> or
     *         <b>file</b> is a directory<br> or
     *         <b>file</b> doesn`t exist<br><br>
     *  size of file, otherwise
     */
    public abstract long getFileSize(File file);
    
    public abstract long getFileSize(String filename);
    
    public abstract long getFreeSpace(File file);
    
    public abstract long getFileCRC32(File file) throws IOException;
    
    public abstract String getFileCRC32String(File file) throws IOException;
    
    public abstract byte[] getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException;
    
    public abstract byte[] getFileMD5(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract String readFirstLine(File file) throws IOException;
    
    public abstract List<String> readStringList(File file) throws IOException;
    
    public abstract void writeStringList(File file, List<String> list) throws IOException;
    
    public abstract void writeStringList(File file, List<String> list, boolean append) throws IOException;
    
    public abstract void deleteFile(File file) throws IOException;
    
    public abstract void deleteFile(File file, boolean followLinks) throws IOException;
    
    public abstract void deleteFile(File file, String mask) throws IOException;
    
    public abstract void deleteFiles(List<File> files) throws IOException;
    
    public abstract File createTempFile() throws IOException;
    
    public abstract File createTempFile(File parent) throws IOException;
    
    public abstract void modifyFile(File file, String token, String replacement) throws IOException;
    
    public abstract void modifyFile(File file, String token, String replacement, boolean useRE) throws IOException;
    
    public abstract void modifyFile(File file, Map<String, String> replacementMap) throws IOException;
    
    public abstract void modifyFile(File file, Map<String, String> replacementMap, boolean useRE) throws IOException;
    
    public abstract void modifyFile(File[] files, Map<String, String> replacementMap, boolean useRE) throws IOException;
    
    public abstract void moveFile(File source, File destination) throws IOException;
    
    public abstract void copyFile(File source, File destination) throws IOException;
    
    public abstract void copyFile(File source, File destination, boolean recurseToSubDirs) throws IOException;
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class GenericFileUtils extends FileUtils {
        private GenericFileUtils() {
            // nothing here
        }
        
        public String readFile(File file) throws IOException {
            String contents = "";
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
            char[] buffer = new char[1024];
            while (reader.ready()) {
                contents += new String(buffer, 0, reader.read(buffer));
            }
            reader.close();
            
            return contents;
        }
        
        public void writeFile(File file, String string) throws IOException {
            writeFile(file, string, false);
        }
        
        public void appendFile(File file, String string) throws IOException {
            writeFile(file, string, true);
        }
        
        public void writeFile(File file, String string, boolean append) throws IOException {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            
            String newContents = "";
            
            if (append) {
                newContents += readFile(file);
            }
            newContents += string;
            
            FileOutputStream outputStream = new FileOutputStream(file);
            
            outputStream.write(newContents.getBytes());
            
            outputStream.close();
        }
        
        public Date getLastModified(String fname) {
            return (fname==null) ? null : getLastModified(new File(fname));
        }
        
        public Date getLastModified(File f) {
            if(!f.exists()) {
                return null;
            }
            Date date=null;
            try {
                long modif = f.lastModified();
                date = new Date(modif);
            } catch (SecurityException ex) {
                ex=null;
            }
            return date;
        }
        
        public long getFileSize(File file) {
            if(file==null || file.isDirectory() || !file.exists()) {
                return -1;
            }
            try {
                return file.length();
            } catch(SecurityException ex) {
                return -1;
            }
        }
        
        public long getFileSize(String filename) {
            return (filename==null) ? -1 : getFileSize(new File(filename));
        }
        
        public long getFreeSpace(File file) {
            return -1;
        }
        
        public long getFileCRC32(File file) throws IOException {
            CRC32 crc = new CRC32();
            
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                
                for (int i = input.read(); i != -1; i = input.read()) {
                    crc.update((byte) i);
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
            
            return crc.getValue();
        }
        
        public String getFileCRC32String(File file) throws IOException {
            return Long.toString(getFileCRC32(file));
        }
        
        public byte[] getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.reset();
            
            InputStream input = null;
            try {
                input = new FileInputStream(file);
                
                byte[] buffer = new byte[10240];
                
                while (input.available() > 0) {
                    md.update(buffer, 0, input.read(buffer));
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
            
            return md.digest();
        }
        
        public byte[] getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
            return getFileDigest(file, "MD5");
        }
        
        public String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException {
            return StringUtils.getInstance().asHexString(getFileMD5(file));
        }
        
        public String readFirstLine(File file) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            
            try {
                return reader.readLine();
            } finally {
                reader.close();
            }
        }
        
        public List<String> readStringList(File file) throws IOException {
            List<String> vector = new LinkedList<String> ();
            
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                
                String string;
                while ((string = reader.readLine()) != null) {
                    vector.add(string);
                }
                
                reader.close();
            } catch (IOException e) {
                throw e;
            }
            
            return vector;
        }
        
        public void writeStringList(File file, List<String> list) throws IOException {
            writeStringList(file, list, false);
        }
        
        public void writeStringList(File file, List<String> list, boolean append) throws IOException {
            StringBuilder builder = new StringBuilder();
            
            for (String string: list) {
                builder.append(string).append(System.getProperty("line.separator"));
            }
            
            writeFile(file, builder.toString(), append);
        }
        
        public void deleteFile(File file) throws IOException {
            deleteFile(file, true);
        }
        
        public void deleteFile(File file, boolean followLinks) throws IOException {
            String type = "";
            if (file.isDirectory()) {
                if (followLinks) {
                    File[] children = file.listFiles();
                    
                    for (File child: children) {
                        deleteFile(child, true);
                    }
                }
                
                type = "directory"; //NOI18N
            } else {
                type = "file"; //NOI18N
            }
            
            LogManager.getInstance().log(ErrorLevel.MESSAGE, "    deleting " + type + ": " + file); //NOI18N
            
            if (!file.exists()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + type + " does not exist"); //NOI18N
            }
            
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
        
        public void deleteFile(File file, String mask) throws IOException {
            if (file.isDirectory()) {
                File[] children = file.listFiles(new MaskFileFilter(mask));
                
                for (File child: children) {
                    deleteFile(child, mask);
                }
            } else {
                if (file.getName().matches(mask)) {
                    deleteFile(file);
                }
            }
        }
        
        public void deleteFiles(List<File> files) throws IOException {
            for (File file: files) {
                deleteFile(file);
            }
        }
        
        public File createTempFile() throws IOException {
            File file = File.createTempFile("nbi-", ".tmp");
            
            file.deleteOnExit();
            
            return file;
        }
        
        public File createTempFile(File parent) throws IOException {
            File file = File.createTempFile("nbi-", ".tmp", parent);
            
            file.deleteOnExit();
            
            return file;
        }
        
        public void modifyFile(File file, String token, String replacement) throws IOException {
            modifyFile(file, token, replacement, false);
        }
        
        public void modifyFile(File file, String token, String replacement, boolean useRE) throws IOException {
            Map<String, String> replacementMap = new HashMap<String, String>();
            
            replacementMap.put(token, replacement);
            
            modifyFile(file, replacementMap, useRE);
        }
        
        public void modifyFile(File file, Map<String, String> replacementMap) throws IOException {
            modifyFile(file, replacementMap, false);
        }
        
        public void modifyFile(File file, Map<String, String> replacementMap, boolean useRE) throws IOException {
            if (!file.exists()) {
                return;
            }
            
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                
                for (File child: children) {
                    modifyFile(child, replacementMap, useRE);
                }
            } else {
                // if the file is larger than 100 Kb - skip it
                if (file.length() > 1024*100) {
                    return;
                }
                
                String originalContents = "";
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                char[] buffer = new char[1024];
                while (reader.ready()) {
                    originalContents += new String(buffer, 0, reader.read(buffer));
                }
                reader.close();
                
                String modifiedContents = new String(originalContents);
                for (String token: replacementMap.keySet()) {
                    String replacement = replacementMap.get(token);
                    if (useRE) {
                        modifiedContents = Pattern.compile(token, Pattern.MULTILINE).matcher(modifiedContents).replaceAll(replacement);
                    } else {
                        modifiedContents = modifiedContents.toString().replace(token, replacement);
                    }
                }
                
                if (!modifiedContents.equals(originalContents)) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE, "    modifying file: " + file.getAbsolutePath());
                    
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(file)));
                    writer.write(modifiedContents);
                    writer.close();
                }
            }
        }
        
        public void modifyFile(File[] files, Map<String, String> replacementMap, boolean useRE) throws IOException {
            for (File file: files) {
                modifyFile(file, replacementMap, useRE);
            }
        }
        
        public void moveFile(File source, File destination) throws IOException {
            copyFile(source, destination);
            deleteFile(source);
        }
        
        public void copyFile(File source, File destination) throws IOException {
            copyFile(source, destination, false);
        }
        
        public void copyFile(File source, File destination, boolean recurseToSubDirs) throws IOException {
            if (!source.exists()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + source + " does not exist"); //NOI18N
                return;
            }
            
            if (source.isFile()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    copying file: " + source + " to: " + destination);//NOI18N
                transferBytes(source, destination);
            } else {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    copying directory: " + source + " to: " + destination + (recurseToSubDirs ? " with recursion" : ""));//NOI18N
                if (!destination.mkdirs()) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... cannot create " + destination); //NOI18N
                    return;
                }
                
                if (recurseToSubDirs) {
                    for (File file: source.listFiles()) {
                        copyFile(file, new File(destination, file.getName()), recurseToSubDirs);
                    }
                }
            }
        }
        
        private boolean transferBytes(File source, File destination) throws IOException {
            if (!source.isFile()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + source + " is not a file"); //NOI18N
                return false;
            }
            
            if (!source.canRead()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + source + " is readable"); //NOI18N
                return false;
            }
            
            if (destination.exists() && !destination.isFile()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + destination + " is not a file"); //NOI18N
                return false;
            }
            
            if (!destination.exists()) {
                if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + destination.getParent() + " cannot be created"); //NOI18N
                    return false;
                }
                if (!destination.createNewFile()) {
                    LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + destination + " cannot be created"); //NOI18N
                    return false;
                }
            }
            
            if (!destination.canWrite()) {
                LogManager.getInstance().log(ErrorLevel.MESSAGE, "    ... " + destination + " is not writable"); //NOI18N
                return false;
            }
            
            FileInputStream inputStream = new FileInputStream(source);
            FileOutputStream outputStream = new FileOutputStream(destination);
            
            byte[] buffer = new byte[1024];
            
            while (inputStream.available() > 0) {
                outputStream.write(buffer, 0, inputStream.read(buffer));
            }
            
            outputStream.close();
            inputStream.close();
            
            return true;
        }
    }
    
    public static class UnixFileUtils extends GenericFileUtils {
        private UnixFileUtils() {
            // nothing here
        }
        
        public List<File> findExecutableFiles(File parent) throws IOException {
            List<File> files = new ArrayList<File>();
            
            if (!parent.exists()) {
                return files;
            }
            
            for (File child: parent.listFiles()) {
                if (child.isDirectory()) {
                    files.addAll(findExecutableFiles(child));
                } else {
                    // name based analysis
                    String name = child.getName();
                    if (name.endsWith(".sh")) { // shell script
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".pl")) { // perl script
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".py")) { // python script
                        files.add(child);
                        continue;
                    }
                    
                    // contents based analysis
                    String line = readFirstLine(child);
                    if (line != null) {
                        if (line.startsWith("#!")) { // a script of some sort
                            files.add(child);
                            continue;
                        }
                    }
                }
            }
            
            return files;
        }
        
        public List<File> findNonUnixFiles(File parent) throws IOException {
            List<File> files = new ArrayList<File>();
            
            if (!parent.exists()) {
                return files;
            }
            
            for (File child: parent.listFiles()) {
                if (child.isDirectory()) {
                    files.addAll(findNonUnixFiles(child));
                } else {
                    // name based analysis
                    String name = child.getName();
                    if (name.endsWith(".bat")) { // dos batch file
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".cmd")) { // windows batch file
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".dll")) { // windows library
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".exe")) { // windows executable
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".com")) { // windows executable
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".vbs")) { // windows script
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".vbe")) { // windows script
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".wsf")) { // windows script
                        files.add(child);
                        continue;
                    }
                    if (name.endsWith(".wsh")) { // windows script
                        files.add(child);
                        continue;
                    }
                    
                    // contents based analysis - none at this point
                }
            }
            
            return files;
        }
        
        public void chmod(File file, String mode) throws IOException {
            chmod(Arrays.asList(file), mode);
        }
        
        public void chmod(File file, int mode) throws IOException {
            chmod(file, Integer.toString(mode));
        }
        
        public void chmod(List<File> files, String mode) throws IOException {
            for (File file: files) {
                File   directory = file.getParentFile();
                String name      = file.getName();
                
                SystemUtils.getInstance().executeCommand(directory,
                        "chmod",
                        mode,
                        name);
            }
        }
        
        public void correctFiles(File parent) throws IOException {
            // all executables should be executable
            chmod(findExecutableFiles(parent), "ugo+x");
            
            // irrelevant files should be deleted
            deleteFiles(findNonUnixFiles(parent));
        }
        
        public void correctFiles(File... parents) throws IOException {
            for (File parent: parents) {
                correctFiles(parent);
            }
        }
    }
    
    /**
     * A file filter which accepts files whose names match a given mask.
     *
     * @author Kirill Sorokin
     */
    private static class MaskFileFilter implements FileFilter {
        private String mask = ".*";                 //NOI18N
        
        /**
         * Creates a new instance of MaskFileFilter.
         */
        public MaskFileFilter(String mask) {
            this.mask = mask;
        }
        
        /**
         * {@inheritDoc}
         */
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            
            if (file.getName().matches(mask)) {
                return true;
            }
            
            return false;
        }
    }
}
