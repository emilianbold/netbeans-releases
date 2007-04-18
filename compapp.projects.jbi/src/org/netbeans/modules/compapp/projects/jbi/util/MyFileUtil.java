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

package org.netbeans.modules.compapp.projects.jbi.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jqian
 */
public class MyFileUtil {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); // NOI18N
    
    public static List<File> listFiles(File directory,
            FilenameFilter filter,
            boolean recursive) {
        
        List<File> files = new ArrayList<File>();
        
        File[] entries = directory.listFiles();
        
        if (entries != null) {
            for (File entry : entries) {
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }
                
                if (recursive && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, recursive));
                }
            }
        }
        
        return files;
    }
          
    public static String getRelativePath(File from, File to) {
        String fromPath = from.getAbsolutePath().replaceAll("\\\\", "/");
        String toPath = to.getAbsolutePath().replaceAll("\\\\", "/");
        while (true) {
            int fromSlashIndex = fromPath.indexOf("/");
            int toSlashIndex = toPath.indexOf("/");
            if (fromSlashIndex != -1 && toSlashIndex != -1 &&
                    fromPath.substring(0, fromSlashIndex).equals(toPath.substring(0, toSlashIndex))) {
                fromPath = fromPath.substring(fromSlashIndex + 1);
                toPath = toPath.substring(toSlashIndex + 1);
            } else {
                break;
            }
        }
        
        String ret = "";
        
        while (fromPath != null) {
            int fromSlashIndex = fromPath.indexOf("/");
            ret = ret + "../";
            if (fromSlashIndex == -1) {
                break;
            } else {
                fromPath = fromPath.substring(fromSlashIndex);
            }
        }
        
        if (toPath != null) {
            ret = ret + toPath;
        }
        
        return ret;
    }     
    
    /**
     * Replaces all the instances of old strings in a file by a new string.
     */
    public static void replaceAll(FileObject fileObject,
            String old, String nu, boolean isRegex)
            throws FileNotFoundException, IOException {
        File file = FileUtil.toFile(fileObject);
        replaceAll(file, old, nu, isRegex);
    }
   
    public static void replaceAll(File file,
            String old, String nu, boolean isRegex)
            throws FileNotFoundException, IOException {
        
        String fileName = file.getName();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        File tempFile = File.createTempFile(fileName, "tmp"); // NOI18N
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        
        String line;
        if (isRegex) {
            while ((line = reader.readLine()) != null) {
                line.replaceAll(old, nu);
                writer.write(line + LINE_SEPARATOR);
            }
        } else {
            while ((line = reader.readLine()) != null) {
                while (true) {
                    int index = line.indexOf(old);
                    if (index != -1) {
                        line = line.substring(0, index) + nu +
                                line.substring(index + old.length());
                    } else {
                        break;
                    }
                }
                writer.write(line + LINE_SEPARATOR);
            }            
        }
        reader.close();
        writer.close();
        
        move(tempFile, file);
    }
    
    public static void move(File srcFile, File destFile) 
    throws FileNotFoundException, IOException {
        copy(srcFile, destFile);
        srcFile.delete();
    }
    
    public static void copy(File srcFile, File destFile) 
    throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(srcFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line + LINE_SEPARATOR);
        }
        reader.close();
        writer.close();
    }    
}
