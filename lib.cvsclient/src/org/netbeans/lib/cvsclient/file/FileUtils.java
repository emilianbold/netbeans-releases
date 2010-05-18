/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.lib.cvsclient.file;

import java.io.*;

/**
 * A utility class for file based operations.
 *
 * @author  Thomas Singer
 * @version Nov 23, 2001
 */
public class FileUtils {
    private static FileReadOnlyHandler fileReadOnlyHandler;

    /**
     * Returns the current FileReadOnlyHandler used by setFileReadOnly().
     */
    public static FileReadOnlyHandler getFileReadOnlyHandler() {
        return fileReadOnlyHandler;
    }

    /**
     * Sets the specified fileReadOnlyHandler to be used with setFileReadOnly().
     */
    public static void setFileReadOnlyHandler(FileReadOnlyHandler fileReadOnlyHandler) {
        FileUtils.fileReadOnlyHandler = fileReadOnlyHandler;
    }

    /**
     * Sets the specified file read-only (readOnly == true) or writable (readOnly == false).
     * If no fileReadOnlyHandler is set, nothing happens.
     *
     * @throws IOException if the operation failed
     */
    public static void setFileReadOnly(File file, boolean readOnly) throws IOException {
        if (getFileReadOnlyHandler() == null) {
            return;
        }

        getFileReadOnlyHandler().setFileReadOnly(file, readOnly);
    }

    /**
     * Copies the specified sourceFile to the specified targetFile.
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        if (sourceFile == null || targetFile == null) {
            throw new NullPointerException("sourceFile and targetFile must not be null"); // NOI18N
        }

        // ensure existing parent directories
        File directory = targetFile.getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory '" + directory + "'"); // NOI18N
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

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
}
