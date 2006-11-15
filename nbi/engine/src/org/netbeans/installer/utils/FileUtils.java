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
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 *
 *
 *
 * @author ks152834
 */
public final class FileUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int BUFFER_SIZE = 10240;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static String readFile(File file) throws IOException {
        final Reader reader = new BufferedReader(new FileReader(file));
        try {
            final char[] buffer = new char[BUFFER_SIZE];
            final StringBuilder stringBuilder = new StringBuilder();
            int readLength;
            while ((readLength = reader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, readLength);
            }
            return stringBuilder.toString();
        } finally {
            try {
                reader.close();
            } catch(IOException ignord) {}
        }
    }
    
    public static void writeFile(File file, CharSequence string) throws IOException {
        writeFile(file, string, false);
    }
    
    public static void appendFile(File file, CharSequence string) throws IOException {
        writeFile(file, string, true);
    }
    
    public static void writeFile(File file, CharSequence string, boolean append) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file, append);
            output.write(string.toString().getBytes());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, e);
                }
            }
        }
    }
    
    public static void writeFile(File file, InputStream input) throws IOException {
        writeFile(file, input, false);
    }
    
    public static void appendFile(File file, InputStream input) throws IOException {
        writeFile(file, input, true);
    }
    
    public static void writeFile(File file, InputStream input, boolean append) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file, append);
            StreamUtils.transferData(input, output);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    ErrorManager.notify(ErrorLevel.DEBUG, e);
                }
            }
        }
    }
    
    public static Date getLastModified(File f) {
        if(!f.exists()) {
            return null;
        }
        Date date = null;
        try {
            long modif = f.lastModified();
            date = new Date(modif);
        }  catch (SecurityException ex) {
            ex=null;
        }
        return date;
    }
    
    public static long getFileSize(File file) {
        if(file==null || file.isDirectory() || !file.exists()) {
            return -1;
        }
        try {
            return file.length();
        } catch (SecurityException ex) {
            return -1;
        }
    }
    
    public static long getFreeSpace(File file) {
        return -1;
    }
    
    public static long getFileCRC32(File file) throws IOException {
        CRC32 crc = new CRC32();
        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            final byte[] buffer = new byte[BUFFER_SIZE];
            int readLength;
            while ((readLength = input.read(buffer)) != -1) {
                crc.update(buffer, 0, readLength);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignord) {}
            }
        }
        
        return crc.getValue();
    }
    
    public static String getFileCRC32String(File file) throws IOException {
        return Long.toString(getFileCRC32(file));
    }
    
    public static byte[] getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            final byte[] buffer = new byte[BUFFER_SIZE];//todo: here was 10240?? discus
            int readLength;
            while ((readLength = input.read(buffer)) != -1) {
                md.update(buffer, 0, readLength);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignord) {}
            }
        }
        
        return md.digest();
    }
    
    public static byte[] getFileMD5(File file) throws IOException, NoSuchAlgorithmException {
        return getFileDigest(file, "MD5");
    }
    
    public static String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException {
        return StringUtils.asHexString(getFileMD5(file));
    }
    
    public static byte[] getFileSHA1(File file) throws IOException, NoSuchAlgorithmException {
        return getFileDigest(file, "SHA1");
    }
    
    public static String getFileSHA1String(File file) throws IOException, NoSuchAlgorithmException {
        return StringUtils.asHexString(getFileSHA1(file));
    }
    
    public static String readFirstLine(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
            return reader.readLine();
        } finally {
            try {
                reader.close();
            } catch (IOException ignord) {}
        }
    }
    
    public static List<String> readStringList(File file) throws IOException {
        final List<String> list = new LinkedList<String>();
        for (String line: readFile(file).split("[\n\r]|\r\n")) {
            list.add(line);
        }
        return list;
    }
    
    public static void writeStringList(File file, List<String> list) throws IOException {
        writeStringList(file, list, false);
    }
    
    public static void writeStringList(File file, List<String> list, boolean append) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        for(String string : list) {
            builder.append(string).append(System.getProperty("line.separator"));
        }
        
        writeFile(file, builder, append);
    }
    
    public static void deleteFile(File file) throws IOException {
        deleteFile(file, true);
    }
    
    public static void deleteFile(File file, boolean followLinks) throws IOException {
        String type = "";
        if (file.isDirectory()) {
            if (followLinks) {
                for(File child: file.listFiles()) {
                    deleteFile(child, true);
                }
            }
            
            type = "directory"; //NOI18N
        }  else {
            type = "file"; //NOI18N
        }
        
        LogManager.log(ErrorLevel.MESSAGE, "    deleting " + type + ": " + file); //NOI18N
        
        if (!file.exists()) {
            LogManager.log(ErrorLevel.MESSAGE, "    ... " + type + " does not exist"); //NOI18N
        }
       if(SystemUtils.isDeletingAllowed(file)) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }
    
    public static void deleteFile(File file, String mask) throws IOException {
        if (file.isDirectory()) {
            
            for(File child: file.listFiles(new MaskFileFilter(mask))) {
                deleteFile(child, mask);
            }
        }  else {
            if (file.getName().matches(mask)) {
                deleteFile(file);
            }
        }
    }
    
    public static void deleteFiles(List<File> files) throws IOException {
        for(File file : files) {
            deleteFile(file);
        }
    }
    
    public static void deleteEmptyParents(File directory) {
        if (directory == null) {
            return;
        }
        if(!directory.exists() || directory.isFile()) {
            deleteEmptyParents(directory.getParentFile());
        } else if(directory.isDirectory()) {
            File parent = directory;
            while (parent != null && isEmpty(parent) && parent.exists()) {
                directory = parent;
                parent = parent.getParentFile();
                directory.delete();
            }
        }
    }
    
    public static File createTempFile() throws IOException {
        File file = File.createTempFile("nbi-", ".tmp");
        
        file.deleteOnExit();
        
        return file;
    }
    
    public static File createTempFile(File parent) throws IOException {
        File file = File.createTempFile("nbi-", ".tmp", parent);
        
        file.deleteOnExit();
        
        return file;
    }
    
    public static void modifyFile(File file, String token, Object replacement) throws IOException {
        modifyFile(file, token, replacement, false);
    }
    
    public static void modifyFile(File file, String token, Object replacement, boolean useRE) throws IOException {
        Map<String, Object> replacementMap = new HashMap<String, Object>();
        
        replacementMap.put(token, replacement);
        
        modifyFile(file, replacementMap, useRE);
    }
    
    public static void modifyFile(File file, Map<String, Object> replacementMap) throws IOException {
        modifyFile(file, replacementMap, false);
    }
    
    public static void modifyFile(File file, Map<String, Object> replacementMap, boolean useRE) throws IOException {
        if (!file.exists()) {
            return;
        }
        
        if (file.isDirectory()) {
            for(File child: file.listFiles()) {
                modifyFile(child, replacementMap, useRE);
            }
        }  else {
            // if the file is larger than 100 Kb - skip it
            if (file.length() > 1024*100) {
                return;
            }
            
            String originalContents = readFile(file);
            
            String modifiedContents = new String(originalContents);
            for(String token : replacementMap.keySet()) {
                String replacement;
                
                Object object = replacementMap.get(token);
                if (object instanceof File) {
                    replacement = ((File) object).getAbsolutePath();
                }  else {
                    replacement = object.toString();
                }
                
                if (useRE) {
                    modifiedContents = Pattern.compile(token, Pattern.MULTILINE).matcher(modifiedContents).replaceAll(replacement);
                }  else {
                    modifiedContents = modifiedContents.toString().replace(token, replacement);
                }
            }
            
            if (!modifiedContents.equals(originalContents)) {
                LogManager.log(ErrorLevel.MESSAGE, "    modifying file: " + file.getAbsolutePath());
                writeFile(file, modifiedContents);
            }
        }
    }
    
    public static void modifyFiles(List<File> files, Map<String, Object> replacementMap, boolean useRE) throws IOException {
        for(File file : files) {
            modifyFile(file, replacementMap, useRE);
        }
    }
    
    public static void moveFile(File source, File destination) throws IOException {
        copyFile(source, destination);
        deleteFile(source);
    }
    
    public static void copyFile(File source, File destination) throws IOException {
        copyFile(source, destination, false);
    }
    
    public static void copyFile(File source, File destination, boolean recurseToSubDirs) throws IOException {
        if (!source.exists()) {
            LogManager.log(ErrorLevel.MESSAGE, "    ... " + source + " does not exist"); //NOI18N
            return;
        }
        
        if (source.isFile()) {
            LogManager.log(ErrorLevel.MESSAGE, "    copying file: " + source + " to: " + destination);//NOI18N
            
            if (!source.canRead()) {
                throw new IOException("source is not readable");
            }
            
            if (destination.exists() && !destination.isFile()) {
                throw new IOException("destination is not a file");
            }
            
            File parent = destination.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("destination parent cannot be created");
            }
            
            if (!destination.exists() && !destination.createNewFile()) {
                throw new IOException("destination cannot be created");
            }
            
            if (!destination.canWrite()) {
                throw new IOException("desctination is not writable");
            }
            
            FileInputStream inputStream = new FileInputStream(source);
            FileOutputStream outputStream = new FileOutputStream(destination);
            try {
                StreamUtils.transferData(inputStream, outputStream);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException ignored){}
                try {
                    inputStream.close();
                } catch (IOException ignored){}
            }
        }  else {
            LogManager.log(ErrorLevel.MESSAGE, "    copying directory: " + source + " to: " + destination + (recurseToSubDirs ? " with recursion" : ""));//NOI18N
            if (!destination.mkdirs()) {
                LogManager.log(ErrorLevel.MESSAGE, "    ... cannot create " + destination); //NOI18N
                return;
            }
            
            if (recurseToSubDirs) {
                for(File file : source.listFiles()) {
                    copyFile(file, new File(destination, file.getName()), recurseToSubDirs);
                }
            }
        }
    }
    
    public static boolean isEmpty(File file) {
        if (!file.exists()) {
            return true;
        }
        
        if (file.isDirectory()) {
            for(File child : file.listFiles()) {
                if (!isEmpty(child)) {
                    return false;
                }
            }
            
            return true;
        }  else {
            return false;
        }
    }
    
    public static boolean canRead(File file) {
        if (file.exists()) {
            return file.canRead();
        } else {
            File parent = file;
            do {
                parent = parent.getParentFile();
            } while ((parent != null) && !parent.exists());
            
            if ((parent == null) || !parent.isDirectory()) {
                return false;
            } else {
                return parent.canRead();
            }
        }
    }
    
    public static boolean canWrite(File file) {
        if (file.exists()) {
            return file.canWrite();
        } else {
            File parent = file;
            do {
                parent = parent.getParentFile();
            } while ((parent != null) && !parent.exists());
            
            if ((parent == null) || !parent.isDirectory()) {
                return false;
            } else {
                if (parent.canWrite()) {
                    return true;
                } else {
                    try {
                        createTempFile(parent).delete();
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                }
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private FileUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
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
