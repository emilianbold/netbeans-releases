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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class DockerStreamResult implements StreamResult {

    private static final Logger LOGGER = Logger.getLogger(DockerStreamResult.class.getName());

    private final Socket s;

    private final OutputStream outputStream;

    private final Demuxer demultiplexer;

    private final InputStream stdOut;

    private final InputStream stdErr;

    private Demuxer.Result last = Demuxer.Result.EMPTY;

    private int remaining;

    public DockerStreamResult(Socket s, InputStream is) throws IOException {
        this.s = s;
        this.outputStream = s.getOutputStream();
        this.demultiplexer = new Demuxer(is == null ? s.getInputStream() : is);
        this.stdOut = new ResultInputStream(false);
        this.stdErr = new ResultInputStream(true);
    }

    public OutputStream getStdIn() {
//        return new FilterOutputStream(outputStream) {
//            @Override
//            public void write(byte[] b, int off, int len) throws IOException {
//                out.write(b, off, len);
//                out.flush();
//            }
//
//            @Override
//            public void write(int b) throws IOException {
//                out.write(b);
//                out.flush();
//            }
//        };
        return outputStream;
    }

    public InputStream getStdOut() {
        return stdOut;
    }

    public InputStream getStdErr() {
        return stdErr;
    }

    @Override
    public boolean hasTty() {
        return false;
    }

    @Override
    public void close() {
        LOGGER.log(Level.INFO, "Closing", new Exception());
        try {
            s.close();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    private class ResultInputStream extends InputStream {

        private final boolean error;

        public ResultInputStream(boolean error) {
            this.error = error;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            synchronized (DockerStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                int limit = Math.min(len, remaining);
                System.arraycopy(last.getData(), last.getData().length - remaining, b, off, limit);
                remaining -= limit;
                return limit;
            }
        }

        @Override
        public int read() throws IOException {
            synchronized (DockerStreamResult.this) {
                int size = fetchData();
                if (size <= 0) {
                    return size;
                }

                int value = last.getData()[last.getData().length - remaining];
                remaining--;
                return value;
            }
        }

        private int fetchData() {
            synchronized (DockerStreamResult.this) {
                if (last == null) {
                    return -1;
                }
                while (remaining == 0) {
                    last = demultiplexer.getNext();
                    if (last == null) {
                        return -1;
                    }
                    remaining = last.getData().length;
                }

                DockerStreamResult.this.notifyAll();
                try {
                    while (remaining == 0 || last.isError() != error) {
                        DockerStreamResult.this.wait();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return remaining;
            }
        }
    }
}
