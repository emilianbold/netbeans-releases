/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.codeviation.commons.utils;


import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author phrebejk
 */
public class ZipUtil {

    private static final int BUFFER_SIZE = 2048;

    /**
     * Zips file or folder to given output stream
     *
     * XXX Add file filter and recursive flag
     *
     */



    static public void zip(File file, OutputStream os) throws IOException {
        zip( file, os, true );
    }
    
    static public void zip(File file, OutputStream os, boolean addRoot) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);

        if (!addRoot && file.isDirectory()) {
            for( File f : file.listFiles() ) {
                add( "", f, zos);
            }
        }
        else {
            add("", file, zos);
        }

        zos.flush();
        zos.close();

    }

    /** Unzips given InputStream into a folder
     *
     * @param is
     * @param target
     * @throws java.io.IOException
     */
    public static void unzip( InputStream is, File target ) throws IOException {

        ZipInputStream zis = new ZipInputStream(is);

        byte buffer[] = new byte[BUFFER_SIZE];

        ZipEntry ze = zis.getNextEntry();
        while( ze != null ) {
            System.out.println("" + ze.getName() + ":" + " " + ze.getSize() );

            if ( ze.isDirectory() ) {
                new File(target, ze.getName()).mkdirs();
            }
            else {
                File f = new File(target, ze.getName());
                if ( !f.getParentFile().isDirectory() ) {
                    f.getParentFile().mkdirs();
                }
                f.createNewFile();
                OutputStream os = new FileOutputStream(f);
                int read = -1;
                do {
                    read = zis.read(buffer);
                    if ( read != -1 ) {
                        os.write(buffer, 0, read);
                        os.flush();
                    }
                }
                while( read != -1 );
                os.close();
            }

            zis.closeEntry();
            ze = zis.getNextEntry();
        }

    }

    private static void add(String path, File file, ZipOutputStream zip) throws IOException {

        path = path + "/" + file.getName();

        if ( file.isFile() ) {
            zip.putNextEntry( new ZipEntry(path));
            StreamUtil.copy(new FileInputStream(file), zip);
        }
        else if ( file.isDirectory() ) {
            File files[] = file.listFiles();
            if ( files == null || files.length == 0 ) {
                zip.putNextEntry(new ZipEntry(path + "/"));
            }
            else {
                for( File f : files ) {
                    add( path, f, zip);
                }
            }
        }        
    }

}
