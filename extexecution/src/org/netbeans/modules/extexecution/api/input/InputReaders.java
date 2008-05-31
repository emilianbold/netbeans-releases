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

package org.netbeans.modules.extexecution.api.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import org.netbeans.modules.extexecution.input.FileInputReader;
import org.netbeans.modules.extexecution.input.StreamInputReader;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 */
public final class InputReaders {

    private InputReaders() {
        super();
    }

    public static InputReader forReader(Reader reader, Charset charset) {
        return forStream(new ReaderAdapter(reader, charset));
    }

    public static InputReader forStream(InputStream stream) {
        Parameters.notNull("stream", stream);

        return new StreamInputReader(stream, true);
    }

    public static InputReader forFile(final File file) {
        Parameters.notNull("file", file);

        return forFileGenerator(new Callable<File>() {

            public File call() throws Exception {
                return file;
            }
        });
    }

    public static InputReader forFileGenerator(Callable<File> fileGenerator) {
        Parameters.notNull("fileGenerator", fileGenerator);

        return new FileInputReader(fileGenerator);
    }

    private static class ReaderAdapter extends InputStream {

        private static final int BUFFER_SIZE = 1024;

        private final Reader reader;

        private final Charset charset;

        private final CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);

        private ByteBuffer byteBuffer;

        public ReaderAdapter(Reader reader, Charset charset) {
            this.reader = reader;
            this.charset = charset;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = loadBuffer();
            if (read < 0) {
                return read;
            }
            int ret = Math.min(len, byteBuffer.remaining());
            byteBuffer.get(b, off, ret);
            return ret;
        }

        @Override
        public int read() throws IOException {
            int read = loadBuffer();
            if (read < 0) {
                return read;
            }
            return byteBuffer.get();
        }

        @Override
        public int available() throws IOException {
            if (byteBuffer == null || !byteBuffer.hasRemaining()) {
                return reader.ready() ? 1 : 0;
            }
            return byteBuffer.remaining();
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }

        private int loadBuffer() throws IOException {
            int count = 0;
            if (byteBuffer == null || !byteBuffer.hasRemaining()) {
                charBuffer.clear();
                do {
                     int read = reader.read();
                     if (read < 0) {
                         return read;
                     }
                     charBuffer.put((char) read);
                     count++;
                } while (reader.ready() && count < charBuffer.capacity());

                charBuffer.position(0).limit(count);
                byteBuffer = charset.encode(charBuffer);
            }
            return count;
        }
    }
}
