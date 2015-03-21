/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Response;

/**
 *
 * @author Martin Entlicher
 */
public class ClientConnectionTest {
    
    private static final String CHUNK1 = "{\"seq\":11,\"request_seq\":2,\"type\":\"response\",\"command\":\"source\",\"success\":false,\"message\":\"";
    private static final String CHUNK2 = "\",\"running\":true}";
    private static final int MIN_CHUNK_LENGTH = CHUNK1.length() + CHUNK2.length();
    
    @Test
    public void bigChunkTest() throws IOException, InterruptedException {
        PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream(pos);
        OutputStream dummyOS = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
        };
        final boolean[] success = new boolean[] { true };
        final ClientConnection cc = new ClientConnection(pis, dummyOS);
        Thread t = new Thread() {
            @Override public void run() {
                try {
                    cc.runEventLoop(new DummyListener());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    success[0] = false;
                }
            }
        };
        t.start();
        pos.write("Protocol-Version: 1\r\n".getBytes());
        for (int nch = 1; nch < 10; nch++) {
            System.out.println("Num chunks: "+nch);
            for (int chs = MIN_CHUNK_LENGTH; chs < 20000; chs++) {
                pos.write(generateChunk(nch, chs));
            }
        }
        Thread.sleep(200);
        boolean retSuccess = success[0];
        cc.close();
        Assert.assertTrue(retSuccess);
    }

    private byte[] generateChunk(int numChunks, int chunkSize) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < numChunks; c++) {
            sb.append(generateChunk(chunkSize));
        }
        return sb.toString().getBytes();
    }
    
    private String generateChunk(int chunkSize) {
        StringBuilder sb = new StringBuilder("Content-Length: ");
        sb.append(chunkSize);
        sb.append("\r\n\r\n");
        sb.append(CHUNK1);
        int n = chunkSize - MIN_CHUNK_LENGTH;
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            sb.append((char) ('a' + rnd.nextInt(26)));
        }
        sb.append(CHUNK2);
        return sb.toString();
    }

    private static class DummyListener implements ClientConnection.Listener {

        public DummyListener() {
        }

        @Override
        public void header(Map<String, String> properties) {}

        @Override
        public void response(V8Response response) {}

        @Override
        public void event(V8Event event) {}
    }
}
