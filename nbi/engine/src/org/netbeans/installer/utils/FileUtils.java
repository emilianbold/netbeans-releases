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
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.FileEntry;
import org.netbeans.installer.utils.helper.NativeLauncher;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Kirill Sorokin
 */
public final class FileUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int BUFFER_SIZE = 4096;
    
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String METAINF_MASK = "META-INF.*";
    
    public static final String JAR_EXTENSION = ".jar";
    
    public static final String SUN_MICR_RSA = "META-INF/SUN_MICR.RSA";
    public static final String SUN_MICR_SF = "META-INF/SUN_MICR.SF";
    
    public static final String CURRENT = ".";
    public static final String PARENT = "..";
    
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
    
    public static FilesList writeFile(File file, CharSequence string) throws IOException {
        return writeFile(file, string, Charset.defaultCharset().name(), false);
    }
    
    public static FilesList writeFile(File file, CharSequence string, String charset) throws IOException {
        return writeFile(file, string, charset, false);
    }
    
    public static FilesList appendFile(File file, CharSequence string) throws IOException {
        return writeFile(file, string, Charset.defaultCharset().name(), true);
    }
    
    public static FilesList appendFile(File file, CharSequence string, String charset) throws IOException {
        return writeFile(file, string, charset, true);
    }
    
    public static FilesList writeFile(File file, CharSequence string, boolean append) throws IOException {
        return writeFile(
                file,
                string,
                Charset.defaultCharset().name(),
                append);
    }
    
    public static FilesList writeFile(File file, CharSequence string, String charset, boolean append) throws IOException {
        return writeFile(
                file,
                new ByteArrayInputStream(string.toString().getBytes(charset)),
                append);
    }
    
    public static FilesList writeFile(File file, InputStream input) throws IOException {
        return writeFile(file, input, false);
    }
    
    public static FilesList appendFile(File file, InputStream input) throws IOException {
        return writeFile(file, input, true);
    }
    
    public static FilesList writeFile(File file, InputStream input, boolean append) throws IOException {
        FilesList list = new FilesList();
        
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                list.add(mkdirs(file.getParentFile()));
            }
            
            file.createNewFile();
            list.add(file);
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
        
        return list;
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
    
    public static long getSize(File file) {
        long size = -1;
        
        if ((file != null) && !file.isDirectory() && file.exists()) {
            try {
                size = file.length();
            } catch (SecurityException e) {
                ErrorManager.notify(ErrorLevel.DEBUG, e);
            }
        }
        
        return size;
    }
    
    public static long getFreeSpace(File file) {
        long freeSpace = 0;
        
        try {
            freeSpace = SystemUtils.getNativeUtils().getFreeSpace(file);
        } catch (NativeException e) {
            ErrorManager.notify(ErrorLevel.ERROR, "Cannot get free disk space amount", e);
        }
        
        return freeSpace;
    }
    
    public static long getCrc32(File file) throws IOException {
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
    
    public static String getMd5(File file) throws IOException {
        return StringUtils.asHexString(getMd5Bytes(file));
    }
    
    public static byte[] getMd5Bytes(File file) throws IOException {
        try {
            return getDigestBytes(file, "MD5");
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.notifyCritical("Holy crap, this jvm does not support MD5", e);
        }
        
        return null;
    }
    
    public static String getSha1(File file) throws IOException {
        return StringUtils.asHexString(getSha1Bytes(file));
    }
    
    public static byte[] getSha1Bytes(File file) throws IOException {
        try {
            return getDigestBytes(file, "SHA1");
        } catch (NoSuchAlgorithmException e) {
            ErrorManager.notifyCritical("Holy crap, this jvm does not support SHA1", e);
        }
        
        return null;
    }
    
    public static byte[] getDigestBytes(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
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
        for (String line: readFile(file).split(StringUtils.NEW_LINE_PATTERN)) {
            list.add(line);
        }
        return list;
    }
    
    public static FilesList writeStringList(File file, List<String> list) throws IOException {
        return writeStringList(file, list, Charset.defaultCharset().name(), false);
    }
    
    public static FilesList writeStringList(File file, List<String> list, String charset) throws IOException {
        return writeStringList(file, list, charset, false);
    }
    
    public static FilesList writeStringList(File file, List<String> list, boolean append) throws IOException {
        return writeStringList(file, list, Charset.defaultCharset().name(), append);
    }
    
    public static FilesList writeStringList(File file, List<String> list, String charset, boolean append) throws IOException {
        StringBuilder builder = new StringBuilder();
        
        for(String string : list) {
            builder.append(string).append(SystemUtils.getLineSeparator());
        }
        
        return writeFile(file, builder, charset, append);
    }
    
    public static void deleteFile(File file) throws IOException {
        deleteFile(file, false);
    }
    
    public static void deleteFile(File file, boolean recurse) throws IOException {
        if (SystemUtils.isDeletingAllowed(file)) {
            String type = "";
            if (file.isDirectory()) {
                if (recurse) {
                    File [] list = file.listFiles();
                    if(list!=null) {
                        for(File child: list) {
                            deleteFile(child, true);
                        }
                    }
                }
                
                type = "directory";
            } else {
                type = "file";
            }
            
            LogManager.log("    deleting " + type + ": " + file);
            
            if (!file.exists()) {
                LogManager.log("    ... " + type + " does not exist");
            } else {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        }
    }
    
    public static void deleteFile(File file, String mask) throws IOException {
        if (file.isDirectory()) {
            File [] list = file.listFiles(new MaskFileFilter(mask));
            if(list!=null) {
                for(File child: list) {
                    deleteFile(child, mask);
                }
            }
        } else {
            if (file.getName().matches(mask)) {
                deleteFile(file);
            }
        }
    }
    
    public static void deleteFiles(List<File> files) throws IOException {
        for (File file : files) {
            deleteFile(file);
        }
    }
    
    public static void deleteFiles(File... files) throws IOException {
        for (File file : files) {
            deleteFile(file);
        }
    }
    
    public static void deleteFiles(FilesList files) throws IOException {
        for (FileEntry entry: files) {
            deleteFile(entry.getFile());
        }
    }
    
    public static void deleteEmptyParents(File file) throws IOException {
        if (!file.exists() || file.isFile()) {
            deleteEmptyParents(file.getParentFile());
        } else if (file.isDirectory()) {
            File parent = file;
            while (parent != null && isEmpty(parent) && parent.exists()) {
                file = parent;
                parent = parent.getParentFile();
                deleteFile(parent);
            }
        }
    }
    
    public static File createTempFile() throws IOException {
        return createTempFile(SystemUtils.getTempDirectory());
    }
    
    public static File createTempFile(File parent) throws IOException {
        return createTempFile(parent, true);
    }
    
    public static File createTempFile(File parent, boolean create) throws IOException {
        File file = File.createTempFile("nbi-", ".tmp", parent);
        
        if (!create) {
            file.delete();
        }
        
        file.deleteOnExit();
        
        return file;
    }
    
    public static void modifyFile(File file, String token, Object replacement) throws IOException {
        modifyFile(file, token, replacement, false);
    }
    
    public static void modifyFile(File file, String token, Object replacement, boolean regexp) throws IOException {
        Map<String, Object> replacementMap = new HashMap<String, Object>();
        
        replacementMap.put(token, replacement);
        
        modifyFile(file, replacementMap, regexp);
    }
    
    public static void modifyFile(File file, Map<String, Object> map) throws IOException {
        modifyFile(file, map, false);
    }
    
    public static void modifyFile(File file, Map<String, Object> map, boolean regexp) throws IOException {
        if (!file.exists()) {
            return;
        }
        
        if (file.isDirectory()) {
            for(File child: file.listFiles()) {
                modifyFile(child, map, regexp);
            }
        }  else {
            // if the file is larger than 100 Kb - skip it
            if (file.length() > 1024*100) {
                return;
            }
            
            String originalContents = readFile(file);
            
            String modifiedContents = new String(originalContents);
            for(String token : map.keySet()) {
                String replacement;
                
                Object object = map.get(token);
                if (object instanceof File) {
                    replacement = ((File) object).getAbsolutePath();
                }  else {
                    replacement = object.toString();
                }
                
                if (regexp) {
                    modifiedContents = Pattern.compile(token, Pattern.MULTILINE).matcher(modifiedContents).replaceAll(replacement);
                }  else {
                    modifiedContents = modifiedContents.toString().replace(token, replacement);
                }
            }
            
            if (!modifiedContents.equals(originalContents)) {
                LogManager.log("    modifying file: " + file.getAbsolutePath());
                writeFile(file, modifiedContents);
            }
        }
    }
    
    public static void modifyFiles(List<File> files, Map<String, Object> map, boolean regexp) throws IOException {
        for (File file: files) {
            modifyFile(file, map, regexp);
        }
    }
    
    public static FilesList moveFile(File source, File target) throws IOException {
        FilesList list = new FilesList();
        
        if (!source.renameTo(target)) {
            list = copyFile(source, target);
            deleteFile(source);
        }
        
        return list;
    }
    
    public static FilesList copyFile(File source, File target) throws IOException {
        return copyFile(source, target, false);
    }
    
    public static FilesList copyFile(File source, File target, boolean recurse) throws IOException {
        FilesList list = new FilesList();
        
        if (!source.exists()) {
            LogManager.log("    ... " + source + " does not exist");
            return list;
        }
        
        if (source.isFile()) {
            LogManager.log("    copying file: " + source + " to: " + target);
            
            if (!source.canRead()) {
                throw new IOException("source is not readable");
            }
            
            if (target.exists() && !target.isFile()) {
                throw new IOException("destination is not a file");
            }
            
            File parent = target.getParentFile();
            if (!parent.exists()) {
                list.add(mkdirs(parent));
            }
            
            if (!target.exists() && !target.createNewFile()) {
                throw new IOException("destination cannot be created");
            }
            
            if (!target.canWrite()) {
                throw new IOException("desctination is not writable");
            }
            
            FileInputStream  in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(target);
            try {
                StreamUtils.transferData(in, out);
                list.add(target);
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    LogManager.log(ErrorLevel.DEBUG, e);
                }
                try {
                    in.close();
                } catch (IOException e) {
                    LogManager.log(ErrorLevel.DEBUG, e);
                }
            }
        }  else {
            LogManager.log("    copying directory: " + source + " to: " + target + (recurse ? " with recursion" : ""));
            
            list.add(mkdirs(target));
            if (recurse) {
                for (File file: source.listFiles()) {
                    copyFile(file, new File(target, file.getName()), recurse);
                }
            }
        }
        
        return list;
    }
    
    public static boolean isEmpty(File file) {
        if (!file.exists()) {
            return true;
        }
        
        if (file.isDirectory()) {
            File [] list = file.listFiles();
            if (list != null) {
                for(File child : list) {
                    if (!isEmpty(child)) {
                        return false;
                    }
                }
            }
            return true;
        }  else {
            return false;
        }
    }
    
    public static boolean canRead(File file) {
        return canAccessFile(file,true);
    }
    
    public static boolean canWrite(File file) {
        return canAccessFile(file,false);
    }
    
    public static boolean isJarFile(File file) {
        if (file.getName().endsWith(JAR_EXTENSION)) {
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                return true;
            } catch (IOException e) {
                LogManager.log(ErrorLevel.DEBUG, e);
                return false;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        LogManager.log(ErrorLevel.DEBUG, e);
                    }
                }
            }
        } else {
            return false;
        }
    }
    
    public static boolean isSigned(File file) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            if (jar.getEntry(SUN_MICR_RSA) == null) {
                return false;
            }
            if (jar.getEntry(SUN_MICR_SF) == null) {
                return false;
            }
            return true;
        } finally {
            jar.close();
        }
    }
    
    public static FilesList unzip(File source, File target) throws IOException {
        return extract(source, target, null, new Progress());
    }
    
    public static FilesList unzip(File source, File target, Progress progress) throws IOException {
        return extract(source, target, null, progress);
    }
    
    public static FilesList unjar(File source, File target) throws IOException, XMLException {
        return unjar(source, target, new Progress());
    }
    
    public static FilesList unjar(File source, File target, Progress progress) throws IOException, XMLException {
        return extract(source, target, METAINF_MASK, progress);
    }
    
    public static boolean zipEntryExists(File file, String entry) throws IOException {
        ZipFile zip = new ZipFile(file);
        
        try {
            return zip.getEntry(entry) != null;
        } finally {
            zip.close();
        }
    }
    
    public static boolean jarEntryExists(File file, String entry) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            return jar.getEntry(entry) != null;
        } finally {
            jar.close();
        }
    }
    
    public static File extractJarEntry(String entry, File source) throws IOException {
        return extractJarEntry(entry, source, FileUtils.createTempFile());
    }
    
    public static File extractJarEntry(String entry, File source, File target) throws IOException {
        JarFile jar = new JarFile(source);
        FileOutputStream out = new FileOutputStream(target);
        
        try {
            StreamUtils.transferData(jar.getInputStream(jar.getEntry(entry)), out);
            
            return target;
        } finally {
            jar.close();
            out.close();
        }
    }
    
    public static File pack(File source) throws IOException {
        final File target = new File(source.getParentFile(),
                source.getName() + ".pack.gz");
        
        SystemUtils.executeCommand(
                SystemUtils.getPacker().getAbsolutePath(),
                target.getAbsolutePath(),
                source.getAbsolutePath());
        
        return target;
    }
    
    public static File unpack(File source) throws IOException {
        final String name = source.getName();
        final File target = new File(source.getParentFile(),
                name.substring(0, name.length() - ".pack.gz".length()));
        
        SystemUtils.executeCommand(
                SystemUtils.getUnpacker().getAbsolutePath(),
                source.getAbsolutePath(),
                target.getAbsolutePath());
        
        return target;
    }
    
    public static String getJarAttribute(File file, String name) throws IOException {
        JarFile jar = new JarFile(file);
        
        try {
            return jar.getManifest().getMainAttributes().getValue(name);
        } finally {
            try {
                jar.close();
            } catch (IOException e) {
                ErrorManager.notifyDebug("Cannot close jar", e);
            }
        }
    }
    
    public static FilesList mkdirs(File file) throws IOException {
        FilesList list = new FilesList();
        
        if (!file.getParentFile().exists()) {
            list.add(mkdirs(file.getParentFile()));
        }
        
        if (file.exists() && file.isFile()) {
            throw new IOException("Cannot create directory " + file + " it is an existing file");
        }
        
        if (!file.exists()) {
            if (file.mkdir()) {
                list.add(file);
            } else {
                throw new IOException("Cannot create directory " + file);
            }
        }
        
        return list;
    }
    
    public static String getRelativePath(File source, File target) {
        String path;
        
        if (source.equals(target)) { // simplest - source equals target
            path = source.isDirectory() ? CURRENT : target.getName();
        } else if (isParent(source, target)) { // simple - source is target's parent
            final String sourcePath =
                    source.getAbsolutePath().replace(BACKSLASH, SLASH);
            final String targetPath =
                    target.getAbsolutePath().replace(BACKSLASH, SLASH);
            
            if (sourcePath.endsWith(SLASH)) {
                path = targetPath.substring(sourcePath.length());
            } else {
                path = targetPath.substring(sourcePath.length() + 1);
            }
        } else if (isParent(target, source)) { // simple - target is source's parent
            path = source.isDirectory() ? PARENT : CURRENT;
            
            File parent = source.getParentFile();
            while (!parent.equals(target)) {
                path  += SLASH + PARENT;
                parent = parent.getParentFile();
            }
        } else { // tricky - the files are unrelated
            // first we need to find a common parent for the files
            File parent = source.getParentFile();
            while ((parent != null) && !isParent(parent, target)) {
                parent = parent.getParentFile();
            }
            
            // if there is no common parent, we cannot deduct a relative path
            if (parent == null) {
                return null;
            }
            
            path =  getRelativePath(source, parent) +
                    SLASH +
                    getRelativePath(parent, target);
        }
        
        // some final beautification
        if (path.startsWith(CURRENT + SLASH)) {
            if (path.length() > 2) {
                path = path.substring(2);
            } else {
                path = path.substring(0, 1);
            }
        }
        path = path.replace(SLASH + CURRENT + SLASH, SLASH);
        
        return path;
    }
    
    public static boolean isParent(File candidate, File file) {
        File parent = file.getParentFile();
        
        while ((parent != null) && !candidate.equals(parent)) {
            parent = parent.getParentFile();
        }
        
        return (parent != null) && candidate.equals(parent);
    }
    
    public static File createLauncher(NativeLauncher nl, Platform platform, Progress progress) throws IOException {
        return nl.createLauncher(platform,
                (progress==null) ? new Progress() : progress);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static boolean canAccessDirectoryReal(File file, boolean isReadNotWrite) {
        if(isReadNotWrite) {
            boolean result = (file.listFiles()!=null);
            //            LogManager.indent();
            //            LogManager.log(ErrorLevel.DEBUG, "READ: Real Level Access DIR: " + ((result) ? "TRUE" : "FALSE"));
            //            LogManager.unindent();
            return result;
        } else {
            try {
                FileUtils.createTempFile(file).delete();
                //                LogManager.indent();
                //                LogManager.log(ErrorLevel.DEBUG, "WRITE: Real Level Access DIR: TRUE");
                //                LogManager.unindent();
                return true;
            } catch (IOException e) {
                //                LogManager.indent();
                //                LogManager.log(ErrorLevel.DEBUG, "WRITE: Real Level Access DIR: FALSE");
                //                LogManager.unindent();
                return false;
            }
        }
    }
    
    private static boolean canAccessFileReal(File file, boolean isReadNotWrite) {
        Closeable stream = null;
        LogManager.indent();
        try {
            stream = (isReadNotWrite) ? new FileInputStream(file) :
                new FileOutputStream(file) ;
            //LogManager.log(ErrorLevel.DEBUG,
            //        ((isReadNotWrite) ? "READ:" : "WRITE:") + "Real Level Access File: TRUE");
            return true;
        } catch (IOException ex) {
            //LogManager.log(ErrorLevel.DEBUG,
            //        ((isReadNotWrite) ? "READ:" : "WRITE:") + "Real Level Access File: FALSE");
            return false;
        } finally {
            LogManager.unindent();
            if(stream!=null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    LogManager.log(ex);
                }
            }
        }
    }
    
    private static boolean canAccessFile(File checkingFile, boolean isReadNotWrite) {
        File file = checkingFile;
        boolean existsingFile = file.exists();
        //if file doesn`t exist then get it existing parent
        if(!existsingFile) {
            File parent = file;
            do {
                parent = parent.getParentFile();
            } while ((parent != null) && !parent.exists());
            
            if ((parent == null) || !parent.isDirectory()) {
                return false;
            } else {
                file = parent;
            }
        }
        
        //first of all check java implementation
        //LogManager.log("");
        //LogManager.log( ((isReadNotWrite) ? "READ " : "WRITE ") + "Checking file(dir): " + file);
        boolean javaAccessCheck = (isReadNotWrite) ? file.canRead() : file.canWrite();
        // don`t treat read-only attributes for directories as "can`t write" on windows
        if(SystemUtils.isWindows() && !isReadNotWrite && file.isDirectory()) {
            javaAccessCheck = true;
        }
        if (javaAccessCheck) {
            boolean result = true;
            boolean needCheckDirectory = true;
            
            try {
                // Native checking
                result = SystemUtils.getNativeUtils().checkFileAccess(file, isReadNotWrite);
                //LogManager.indent();
                //LogManager.log(ErrorLevel.DEBUG, "OS Level Access File: " + ((result) ? "TRUE" : "FALSE"));
                if(!isReadNotWrite) {
                    // we don`t want to check for writing if OS says smth specific
                    needCheckDirectory = false;
                }
            } catch (NativeException ex) {
                // most probably there is smth wrong with OS
                //LogManager.log(ErrorLevel.DEBUG, "OS Level Access File: ERROR!!!");
                LogManager.log(ex);
            }
            //LogManager.unindent();
            if(!result) { // some limitations by OS
                return false;
            }
            
            if(file.isFile()) {
                return canAccessFileReal(file,isReadNotWrite);
            } else if(file.isDirectory() && (needCheckDirectory)) {
                return canAccessDirectoryReal(file,isReadNotWrite);
            } else { // file is directory, access==read || (access==write & OSCheck==true)
                return true;
            }
        } else {
            LogManager.log(ErrorLevel.DEBUG, "Java Level Access: FALSE");
            return false;
        }
    }
    
    private static FilesList extract(File file, File target, String excludes, Progress progress) throws IOException {
        FilesList list = new FilesList();
        
        // first some basic validation of the destination directory
        if (target.exists() && target.isFile()) {
            throw new IOException("Directory is an existing file, cannot unjar.");
        } else if (!target.exists()) {
            list.add(mkdirs(target));
        }
        
        ZipFile zip = new ZipFile(file);
        
        try {
            FilesList extracted = null;
            boolean extractedWithList = false;
            
            // first we try to extract with the given list
            if (zipEntryExists(file, "META-INF/files.list")) {
                try {
                    final File initialList =
                            extractJarEntry("META-INF/files.list", file);
                    final FilesList toExtract =
                            new FilesList().loadXml(initialList, target);
                    
                    deleteFile(initialList);
                    extracted = extractList(zip, target, toExtract, progress);
                    toExtract.clear();
                    
                    extractedWithList = true;
                } catch (XMLException e) {
                    ErrorManager.notifyDebug("Could not load xml files list for extraction", e);
                }
            }
            
            if (!extractedWithList) {
                extracted = extractNormal(zip, target, excludes, progress);
            }
            
            list.add(extracted);
            extracted.clear();
        } finally {
            zip.close();
        }
        
        return list;
    }
    
    private static FilesList extractList(ZipFile zip, File target, FilesList list, Progress progress) throws IOException {
        FilesList newList = new FilesList();
        
        String targetPath = target.getAbsolutePath();
        
        int total     = list.getSize();
        int extracted = 0;
        
        for (FileEntry listEntry: list) {
            final String listEntryName = listEntry.getName();
            final File listEntryFile = listEntry.getFile();
            
            final String zipEntryName =
                    listEntryName.substring(targetPath.length() + 1);
            
            // increase the extracted files count and update the progress percentage
            extracted++;
            progress.setPercentage(Progress.COMPLETE * extracted / total);
            
            // set the progress detail and add a log entry
            progress.setDetail("Extracting " + listEntryFile);
            LogManager.log("extracting " + listEntryFile);
            
            if (listEntry.isDirectory()) {
                newList.add(mkdirs(listEntryFile));
            } else {
                final ZipEntry zipEntry = zip.getEntry(zipEntryName);
                
                newList.add(mkdirs(listEntryFile.getParentFile()));
                
                // actual data transfer
                InputStream  in  = null;
                OutputStream out = null;
                try {
                    in  = zip.getInputStream(zipEntry);
                    out = new FileOutputStream(listEntryFile);
                    
                    StreamUtils.transferData(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
                
                if (listEntry.isPackedJarFile()) {
                    final File packed   = listEntry.getFile();;
                    final File unpacked = unpack(packed);
                    
                    deleteFile(packed);
                    
                    listEntry = new FileEntry(
                            unpacked,
                            listEntry.getSize(),
                            listEntry.getMd5(),
                            listEntry.isJarFile(),
                            false,
                            listEntry.isSignedJarFile(),
                            listEntry.getLastModified(),
                            listEntry.getPermissions());
                }
                
                listEntryFile.setLastModified(listEntry.getLastModified());
            }
            
            newList.add(listEntry);
        }
        
        return newList;
    }
    
    private static FilesList extractNormal(ZipFile zip, File target, String excludes, Progress progress) throws IOException {
        final FilesList list = new FilesList();
        
        Enumeration<? extends ZipEntry> entries;
        
        int total     = 0;
        int extracted = 0;
        
        // then we count the entries, to correctly display progress
        entries = (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            total++;
            entries.nextElement();
        }
        
        // and only after that we actually extract them
        entries = (Enumeration<? extends ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            
            // increase the extracted files count and update the progress percentage
            extracted++;
            progress.setPercentage(Progress.COMPLETE * extracted / total);
            
            // if the entry name matches the excludes pattern, we skip it
            if ((excludes != null) && entry.getName().matches(excludes)) {
                continue;
            }
            
            // create the target file for this entry
            final File file = new File(target, entry.getName()).getAbsoluteFile();
            
            // set the progress detail and add a log entry
            progress.setDetail("Extracting " + file);
            LogManager.log("extracting " + file);
            
            if (entry.getName().endsWith(SLASH)) {
                // some validation (this is a directory entry and thus an existing
                // file will definitely break things)
                if (file.exists() && !file.isDirectory()) {
                    throw new IOException(
                            "An directory entry exists and is not a directory");
                }
                
                // if the directory does not exist, it will be created and added to
                // the extracted files list (if it exists already, it will not
                // appear in the list)
                if (!file.exists()) {
                    list.add(mkdirs(file));
                }
            } else {
                // some validation of the file's parent directory
                final File parent = file.getParentFile();
                if (!parent.exists()) {
                    list.add(mkdirs(parent));
                }
                
                // some validation of the file itself
                if (file.exists() && !file.isFile()) {
                    throw new IOException("An file entry exists and is not a file");
                }
                
                // actual data transfer
                InputStream  in  = null;
                OutputStream out = null;
                try {
                    in  = zip.getInputStream(entry);
                    out = new FileOutputStream(file);
                    
                    StreamUtils.transferData(in, out);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
                
                // as opposed to directories, we always add files to the list, as
                // even if they exist, they will be overwritten
                list.add(file);
            }
            
            // correct the entry's modification time, so it corresponds to the real
            // time of the file in archive
            file.setLastModified(entry.getTime());
        }
        
        return list;
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
        private String mask = ".*";
        
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
