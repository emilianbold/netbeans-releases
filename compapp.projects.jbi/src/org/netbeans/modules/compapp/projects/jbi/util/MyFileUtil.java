/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        
        assert old != null && nu != null;
        
        if (old.equals(nu)) {
            return;
        }
        
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
