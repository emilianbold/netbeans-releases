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
import org.netbeans.installer.utils.file.GenericFileUtils;
import org.netbeans.installer.utils.file.UnixFileUtils;

/**
 *
 *
 *
 *
 * @author ks152834
 */
public abstract class FileUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static FileUtils instance;
    
    public static synchronized FileUtils getInstance() {
        if (instance == null) {
            if (SystemUtils.Platform.isWindows()) {
                instance = new GenericFileUtils();
            } else {
                instance = new UnixFileUtils();
            }
        }
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public abstract String readFile(File file) throws IOException;
    
    public abstract void writeFile(File file, CharSequence string) throws IOException;
    
    public abstract void appendFile(File file, CharSequence string) throws IOException;
    
    public abstract void writeFile(File file, CharSequence string, boolean append) throws IOException;
    
    public abstract Date getLastModified(File f);
    
    public abstract long getFileSize(File file);
    
    public abstract long getFreeSpace(File file);
    
    public abstract long getFileCRC32(File file) throws IOException;
    
    public abstract String getFileCRC32String(File file) throws IOException;
    
    public abstract byte[] getFileDigest(File file, String algorithm) throws IOException, NoSuchAlgorithmException;
    
    public abstract byte[] getFileMD5(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract String getFileMD5String(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract byte[] getFileSHA1(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract String getFileSHA1String(File file) throws IOException, NoSuchAlgorithmException;
    
    public abstract String readFirstLine(File file) throws IOException;
    
    public abstract List<String> readStringList(File file) throws IOException;
    
    public abstract void writeStringList(File file, List<String> list) throws IOException;
    
    public abstract void writeStringList(File file, List<String> list, boolean append) throws IOException;
    
    public abstract void deleteFile(File file) throws IOException;
    
    public abstract void deleteFile(File file, boolean followLinks) throws IOException;
    
    public abstract void deleteFile(File file, String mask) throws IOException;
    
    public abstract void deleteEmptyParents(File file);
    
    public abstract void deleteFiles(List<File> files) throws IOException;
    
    public abstract File createTempFile() throws IOException;
    
    public abstract File createTempFile(File parent) throws IOException;
    
    public abstract void modifyFile(File file, String token, Object replacement) throws IOException;
    
    public abstract void modifyFile(File file, String token, Object replacement, boolean useRE) throws IOException;
    
    public abstract void modifyFile(File file, Map<String, Object> replacementMap) throws IOException;
    
    public abstract void modifyFile(File file, Map<String, Object> replacementMap, boolean useRE) throws IOException;
    
    public abstract void modifyFiles(File[] files, Map<String, Object> replacementMap, boolean useRE) throws IOException;
    
    public abstract void moveFile(File source, File destination) throws IOException;
    
    public abstract void copyFile(File source, File destination) throws IOException;
    
    public abstract void copyFile(File source, File destination, boolean recurseToSubDirs) throws IOException;
    
    public abstract boolean isEmpty(File file);
    
    public abstract boolean canRead(File file);
    
    public abstract boolean canWrite(File file);
    
    public abstract void removeIrrelevantFiles(File parent) throws IOException;
    
    public abstract void correctFilesPermissions(File parent) throws IOException;
    
    // helper overloading implementations //////////////////////////////////////
    public void removeIrrelevantFiles(File... parents) throws IOException {
        for (File file: parents) {
            removeIrrelevantFiles(file);
        }
    }
    
    public void correctFilesPermissions(File... parents) throws IOException {
        for (File file: parents) {
            correctFilesPermissions(file);
        }
    }
}
