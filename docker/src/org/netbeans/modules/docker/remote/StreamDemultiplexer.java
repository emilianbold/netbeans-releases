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
package org.netbeans.modules.docker.remote;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class StreamDemultiplexer implements Runnable, StreamResult {

    private static final Logger LOGGER = Logger.getLogger(StreamDemultiplexer.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor(StreamDemultiplexer.class);

    private final Socket s;

    private final OutputStream outputStream;

    private final InputStream inputStream;

    private final PipedOutputStream outSource = new PipedOutputStream();

    private final PipedOutputStream errSource = new PipedOutputStream();

    private final PipedInputStream stdOut;

    private final PipedInputStream stdErr;
    
    // GuardedBy("this")
    private boolean running;

    public StreamDemultiplexer(Socket s, boolean chunked) throws IOException {
        this.s = s;
        this.outputStream = s.getOutputStream();
        this.inputStream = chunked ? new ChunkedInputStream(s.getInputStream()) : s.getInputStream();
        this.stdOut = new PipedInputStream(outSource);
        this.stdErr = new PipedInputStream(errSource);
    }

    public OutputStream getStdIn() {
        return new FilterOutputStream(outputStream) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byte[] buffer = new byte[8];
                ByteBuffer.wrap(buffer).putInt(4, len);
                out.write(buffer);
                out.write(b, off, len);
                out.flush();
            }

            @Override
            public void write(int b) throws IOException {
                byte[] buffer = new byte[9];
                ByteBuffer.wrap(buffer).putInt(4, 1);
                buffer[8] = (byte) b;
                out.write(buffer);
                out.flush();
            }
        };
//        return outputStream;
    }

    public InputStream getStdOut() {
        synchronized (this) {
            if (!running) {
                requestProcessor.post(this);
                running = true;
            }
        }
        return stdOut;
    }

    public InputStream getStdErr() {
        synchronized (this) {
            if (!running) {
                requestProcessor.post(this);
                running = true;
            }
        }
        return stdErr;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[8];
        byte[] content = new byte[1024];
        for (;;) {
            try {
                int sum = 0;
                do {
                    int read = inputStream.read(buffer, sum, buffer.length);
                    if (read < 0) {
                        close();
                        return;
                    }
                    sum += read;
                } while (sum < 8);
                // now we have 8 bytes
                assert buffer.length == 8;

                OutputStream out;
                int size = ByteBuffer.wrap(buffer).getInt(4);
                if (buffer[0] == 0 || buffer[0] == 1) {
                    out = outSource;
                } else if (buffer[0] == 2) {
                    out = errSource;
                } else {
                    throw new IOException("Unparsable stream " + buffer[0]);
                }

                sum = 0;
                do {
                    int read = inputStream.read(content, 0, Math.min(size, content.length));
                    if (read < 0) {
                        close();
                        return;
                    }
                    int last = 0;
                    for (int i = 0; i < read; i++) {
                        if (content[i] == '\n') {
                            if (i < 1 || content[i - 1] != '\r') {
                                out.write(content, last, i - last);
                                out.write('\r');
                                out.write('\n');
                                last = i + 1;
                            }
                        }
                    }
                    out.write(content, last, read);
                    sum += read;
                } while (sum < size);
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
                close();
                return;
            }
        }
    }

    @Override
    public void close() {
        LOGGER.log(Level.INFO, "Closing");
        try {
            s.close();
            stdOut.close();
            stdErr.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }
}
