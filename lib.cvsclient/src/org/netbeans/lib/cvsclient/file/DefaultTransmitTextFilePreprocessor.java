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
 * @author  Thomas Singer
 * @version Sep 26, 2001
 */
public class DefaultTransmitTextFilePreprocessor
        implements TransmitTextFilePreprocessor {

    private static final int CHUNK_SIZE = 32768;

    private File tempDir;

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public File getPreprocessedTextFile(File originalTextFile) throws IOException {
        // must write file to temp location first because size might change
        // due to CR/LF changes
        File preprocessedTextFile = File.createTempFile("cvs", null, tempDir); // NOI18N

        byte[] newLine = System.getProperty("line.separator").getBytes();
        boolean doConversion = newLine.length != 1 || newLine[0] != '\n';
        
        OutputStream out = null;
        InputStream in = null;

        try {
            in = new BufferedInputStream(new FileInputStream(originalTextFile));
            out = new BufferedOutputStream(new FileOutputStream(preprocessedTextFile));

            byte[] fileChunk = new byte[CHUNK_SIZE];
            byte[] fileWriteChunk = new byte[CHUNK_SIZE];

            for (int readLength = in.read(fileChunk);
                 readLength > 0;
                 readLength = in.read(fileChunk)) {

                if (doConversion) {
                    int writeLength = 0;
                    for (int i = 0; i < readLength; ) {
                        int pos = findIndexOf(fileChunk, newLine, i);
                        if (pos >= i && pos < readLength) {
                            System.arraycopy(fileChunk, i, fileWriteChunk, writeLength, pos - i);
                            writeLength += pos - i;
                            i = pos + newLine.length;
                            fileWriteChunk[writeLength++] = '\n';
                        } else {
                            System.arraycopy(fileChunk, i, fileWriteChunk, writeLength, readLength - i);
                            writeLength += readLength - i;
                            i = readLength;
                        }
                    }
                    out.write(fileWriteChunk, 0, writeLength);
                } else {
                    out.write(fileChunk, 0, readLength);
                }
            }
            return preprocessedTextFile;
        }
        catch (IOException ex) {
            if (preprocessedTextFile != null) {
                cleanup(preprocessedTextFile);
            }
            throw ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
        
    private static int findIndexOf(byte[] array, byte[] pattern, int start) {
        int subPosition = 0;
        for (int i = start; i < array.length; i++) {
            if (array[i] == pattern[subPosition]) {
                if (++subPosition == pattern.length) {
                    return i - subPosition + 1;
                }
            } else {
                subPosition = 0;
            }
        }
        return -1;
    }

    public void cleanup(File preprocessedTextFile) {
        if (preprocessedTextFile != null) {
            preprocessedTextFile.delete();
        }
    }
    
}
