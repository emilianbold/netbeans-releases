/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.web.client.javascript.debugger.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openide.util.NbBundle;

/**
 *
 * @author quynguyen
 */
public class BufferedURLContent implements URLContent {
    private static final int DEFAULT_SIZE = 10240;
    
    private URLContent baseContent;
    private byte[] contentBuffer;
    private InputStream inputStream;
    
    public BufferedURLContent(URLContent baseContent) {
        this.baseContent = baseContent;
        contentBuffer = null;
        inputStream = null;
    }
    
    public BufferedURLContent(InputStream inputStream) {
        this.inputStream = inputStream;
        this.baseContent = null;
        this.contentBuffer = null;
    }
    
    public synchronized InputStream getInputStream() throws IOException {
        if (baseContent == null && inputStream == null) {
            return new ByteArrayInputStream(contentBuffer);
        } else {
            byte[] tempBuffer = new byte[0];
            int totalBytes = 0;
            int currentBytes = 0;
            
            try {
                InputStream stream = (inputStream != null) ? inputStream : baseContent.getInputStream();
                
                if (stream == null) {
                    String defaultMsg = NbBundle.getMessage(BufferedURLContent.class, "NO_CONTENT_MSG");
                    contentBuffer = defaultMsg.getBytes();
                    
                    return new ByteArrayInputStream(contentBuffer);
                }
                
                int bytesRead = 0;
                
                do {
                    if (tempBuffer.length == 0) {
                        tempBuffer = new byte[DEFAULT_SIZE];
                    }else {
                        byte[] buf = new byte[tempBuffer.length * 2];
                        for (int i = 0; i < tempBuffer.length; i++) {
                            buf[i] = tempBuffer[i];
                        }
                        
                        tempBuffer = buf;
                    }
                    
                    bytesRead = stream.read(tempBuffer, totalBytes, tempBuffer.length-totalBytes);
                    
                    currentBytes = totalBytes;
                    if (bytesRead != -1) {
                        totalBytes += bytesRead;
                    }
                } while (bytesRead == tempBuffer.length - currentBytes);
                
                
            }catch (IOException ex) {
                throw ex;
            }
            
            if (totalBytes > 0) {
                baseContent = null;
                inputStream = null;
                contentBuffer = new byte[totalBytes];
                for (int i = 0; i < totalBytes; i++) {
                    contentBuffer[i] = tempBuffer[i];
                }

                return new ByteArrayInputStream(contentBuffer);
            } else if (inputStream != null) {
                    contentBuffer = new byte[0];
                    inputStream = null;
                    return new ByteArrayInputStream(contentBuffer);
            } else {
                return new ByteArrayInputStream(new byte[0]);
            }
        }
        
    }
}
