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
 */

package org.netbeans.modules.uihandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;
import org.netbeans.lib.uihandler.MultiPartHandler;
import org.w3c.dom.Document;

/**
 *
 * @author Jaroslav Tulach
 */
public class UploadLogsTest extends NbTestCase {
    public UploadLogsTest(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testSendsCorrectlyEncoded() throws Exception {
        List<LogRecord> recs = new ArrayList<LogRecord>();
        recs.add(new LogRecord(Level.WARNING, "MSG_MISTAKE"));
        MemoryURL.registerURL("memory://upload", "Ok");
        URL redir = Installer.uploadLogs(new URL("memory://upload"), "myId", Collections.<String,String>emptyMap(), recs);
        
        final byte[] content = MemoryURL.getOutputForURL("memory://upload");
        
        int head = new String(content, "utf-8").indexOf("\n\n");
        if (head == -1) {
            fail("There should be an empty line:\n" + content);
        }

        class RFImpl implements MultiPartHandler.RequestFacade, MultiPartHandler.InputFacade {
            private ByteArrayInputStream is = new ByteArrayInputStream(content);
            
            public int getContentLength() {
                return content.length;
            }

            public String getContentType() {
                return MemoryURL.getRequestParameter("memory://upload", "Content-Type");
            }

            public MultiPartHandler.InputFacade getInput() throws IOException {
                return this;
            }

            public int readLine(byte[] arr, int off, int len) throws IOException {
                int cnt = 0;
                for (; cnt < len; ) {
                    int ch = is.read();
                    if (ch == -1) {
                        return ch;
                    }
                    arr[off + cnt] = (byte)ch;
                    cnt++;
                    if (ch == '\n') {
                        break;
                    }
                }
                return cnt;
            }

            public InputStream getInputStream() {
                return is;
            }
        }
        RFImpl request = new RFImpl();
        
        File dir = new File(getWorkDir(), "ui");
        dir.mkdirs();
        
        MultiPartHandler handler = new MultiPartHandler(request, dir.getPath());
        handler.parseMultipartUpload();
        
        File[] files = dir.listFiles();
        assertEquals("One file created", 1, files.length);
        assertEquals("Current name is myId", "myId", files[0].getName());
        

        DataInputStream is = new DataInputStream(new FileInputStream(files[0]));
        LogRecord rec = LogRecords.read(is);
        
        assertEquals("Same msg", recs.get(0).getMessage(), rec.getMessage());
        
        
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = b.parse(files[0]);
        
        assertNotNull("Parsed", dom);
    }

}


