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
public class DefaultWriteTextFilePreprocessor
        implements WriteTextFilePreprocessor {

    private static final int CHUNK_SIZE = 32768;

    public void copyTextFileToLocation(InputStream processedInputStream, File fileToWrite, OutputStreamProvider customOutput) throws IOException {
        // Here we read the temp file in again, doing any processing required
        // (for example, unzipping). We must not convert the bytes to characters
        // because the file may not be written in the current encoding.
        // We would corrupt it's content when characters would be written!
        InputStream tempInput = null;
        OutputStream out = null;
        byte[] newLine = System.getProperty("line.separator").getBytes();
        try {
            tempInput = new BufferedInputStream(processedInputStream);
            out = new BufferedOutputStream(customOutput.createOutputStream());
            // this chunk is directly read from the temp file
            byte[] cchunk = new byte[CHUNK_SIZE];
            for (int readLength = tempInput.read(cchunk);
                 readLength > 0;
                 readLength = tempInput.read(cchunk)) {

                // we must perform our own newline conversion. The file will
                // definitely have unix style CRLF conventions, so if we have
                // a \n this code will write out a \n or \r\n as appropriate for
                // the platform we are running on
                for (int i = 0; i < readLength; i++) {
                    if (cchunk[i] == '\n') {
                        out.write(newLine);
                    }
                    else {
                        out.write(cchunk[i]);
                    }
                }
            }
        }
        finally {
            if (tempInput != null) {
                try {
                    tempInput.close();
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
}
