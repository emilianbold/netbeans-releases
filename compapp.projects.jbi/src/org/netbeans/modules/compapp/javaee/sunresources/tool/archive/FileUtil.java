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

package org.netbeans.modules.compapp.javaee.sunresources.tool.archive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.openide.util.NbBundle;

/**
 * @author echou
 *
 */
public class FileUtil {
    
    private static final int BUFFER_SIZE = 0x10000; //64k
    private static byte[] buffer = new byte[BUFFER_SIZE];
    private static int count = 0;
    
    // return name of archive file without the extension
    public static String getArchiveName(String s) {
        return s.substring(0, s.lastIndexOf(".")); // NOI18N
    }

    // extract contents of source jar to dest dir, returns the path of result
    public static File explode(File source, File dest) throws Exception {
        // make sure source file exists and isFile
        if (!source.exists() || !source.isFile()) {
            throw new Exception(
                    NbBundle.getMessage(FileUtil.class, "EXC_invalid_srcfile", source.getCanonicalPath()));
        }

        // check status of destination directory
        if (dest.exists()) {
            if (!dest.isDirectory()) {
                throw new Exception(
                        NbBundle.getMessage(FileUtil.class, "EXC_dest_not_dir", dest.getCanonicalPath()));
            }
        } else {
            dest.mkdirs();
        }
        
        // get Application name
        String appName = getArchiveName(source.getName());
        
        // explode to this directory
        File outputDir = new File(dest, appName);
        if (outputDir.exists()) {
            if (!deleteTree(outputDir)) {
                // unsuccessful
                throw new Exception(
                        NbBundle.getMessage(FileUtil.class, "EXC_cannot_delete", outputDir.getCanonicalPath()));
            }
        } else {
            outputDir.mkdir();
        }
        
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ZipInputStream zis = null;
        try {
            fis = new FileInputStream(source);
            bis = new BufferedInputStream(fis, BUFFER_SIZE);
            zis = new ZipInputStream(bis);
            ZipEntry ze = null;
            while ( (ze = zis.getNextEntry()) != null ) {
                String curName = ze.getName();
                File curFile = new File(outputDir, curName);
                if (curName.endsWith("/") || curName.endsWith("\\")) { // NOI18N
                    continue;
                }
                curFile.getParentFile().mkdirs();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(curFile);
                    for (int numBytes = zis.read(buffer); numBytes > 0;
                    numBytes = zis.read(buffer)) {
                        fos.write(buffer, 0, numBytes);
                    }
                } finally {
                    safeclose(fos);
                }
            }
        } finally {
            safeclose(zis);
            safeclose(bis);
            safeclose(fis);
        }
        
        return outputDir;
    }
    
    /*
     * zip directory into target archive file, one level zipping
     */
    public static void compress(File sourceDir, File targetArchive) throws Exception {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;
        
        try {
            fos = new FileOutputStream(targetArchive);
            bos = new BufferedOutputStream(fos, BUFFER_SIZE);
            zos = new ZipOutputStream(bos);
            
            recurse(sourceDir, zos, ""); // NOI18N
            
        } finally {
            safeclose(zos);
            safeclose(bos);
            safeclose(fos);
        }
    }
    
    /*
     * helper method to recurse
     */
    private static void recurse(File source, ZipOutputStream zos, String entryName) throws Exception {
        File[] children = source.listFiles();
        for (int i = 0; i < children.length; i++) {
            File curFile = children[i];
            String curName = entryName.equals("") ? // NOI18N
                    curFile.getName() : entryName + "/" + curFile.getName(); // NOI18N
            if (curFile.isDirectory()) {
                recurse(curFile, zos, curName);
            } else {
                zos.putNextEntry(new ZipEntry(curName));
                FileInputStream fis = new FileInputStream(curFile);
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(fis, BUFFER_SIZE);
                    for (int numBytes = bis.read(buffer); numBytes > 0;
                        numBytes = bis.read(buffer)) {
                        zos.write(buffer, 0, numBytes);
                    }
                    zos.closeEntry();
                } finally {
                    safeclose(bis);
                    safeclose(fis);
                }
            }
        }
    }
    
    public static void safecloseJar(JarFile f) {
        try {
            if (f != null) {
                f.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }
    
    public static void safeclose(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // do nothing
        }
    }
    
    /*
     * recursively delete given file obejct, returns true if successful, false
     * otherwise.
     */
    private static boolean deleteTree(File f) {
        if (f.isFile()) {
            return f.delete();
        } else {
            File[] children = f.listFiles();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (child.isFile()) {
                    if (!child.delete()) {
                        try {
                            System.out.println("Unable to delete file " + child.getCanonicalPath());
                        } catch (IOException ioe) {
                            // do nothing
                        }
                        return false;
                    }
                } else {
                    if (!deleteTree(child)) {
                        return false;
                    }
                }
            }
            return f.delete();
        }
        
    }
}
