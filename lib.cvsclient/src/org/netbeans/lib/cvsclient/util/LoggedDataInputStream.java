/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.util;

import java.io.*;

/**
 * This input stream worked exactly like the normal DataInputStream except that
 * it logs anything read to a file
 * @author  Robert Greig
 */
public class LoggedDataInputStream extends FilterInputStream {

    private long counter;

    /**
     * Construct a logged stream using the specified underlying stream
     * @param in the stream
     */
    public LoggedDataInputStream(InputStream in) {
        super(in);
    }

    /**
     * Read a line (up to the newline character) from the stream, logging
     * it too.
     *
     * @deprecated It converts input data to string using {@link ByteArray#getStringFromBytes}
     * that works only for ASCII without <tt>0</tt>. Use <tt>byte</tt> access methods instead.
     */
    public String readLine() throws IOException {
        return readLineBytes().getStringFromBytes();
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws EOFException at stream end
     */
    public ByteArray readLineBytes() throws IOException {
        int ch;
        boolean throwEOF = true;
        ByteArray byteArray = new ByteArray();
        loop: while (true) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            if (in.available() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    break loop;
                }
                continue;
            }
            ch = in.read();
            counter++;
            switch (ch) {
                case -1:
                    if (throwEOF) {
                        throw new EOFException();
                    }
                case '\n':
                    break loop;
                default:
                    byteArray.add((byte) ch);
            }
            throwEOF = false;
        }
        byte[] bytes = byteArray.getBytes();
        Logger.logInput(bytes);
        Logger.logInput('\n'); //NOI18N
        return byteArray;
    }

    /**
     * Synchronously reads fixed chunk from the stream, logging it too.
     *
     * @param len blocks until specifid number of bytes is read.
     */
    public byte[] readBytes(int len) throws IOException {
        int ch;
        ByteArray byteArray = new ByteArray();
        loop: while (len != 0) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                break;
            }
            if (in.available() == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException iex) {
                    Thread.currentThread().interrupt();
                    break loop;
                }
                continue;
            }
            ch = in.read();
            counter++;
            switch (ch) {
                case -1:
                    break loop;
                default:
                    byteArray.add((byte) ch);
                    len--;
            }
        }
        byte[] bytes = byteArray.getBytes();
        Logger.logInput(bytes);
        return bytes;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * Reads up to byte.length bytes of data from this input stream into an
     * array of bytes.
     */
    public int read(byte[] b) throws IOException {
        int read = in.read(b);
        if (read != -1) {
            Logger.logInput(b, 0, read);
            counter += read;
        }
        return read;
    }

    /**
     * Reads up to len bytes of data from this input stream into an array of
     * bytes
     */
    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        if (read != -1) {
            Logger.logInput(b, off, read);
            counter += read;
        }
        return read;
    }

    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        if (skip > 0) {
            Logger.logInput(new String("<skipped " + skip + " bytes>").getBytes("utf8")); // NOI18N
            counter += skip;
        }
        return skip;
    }

    /**
     * Interruptible read.
     * @throws InterruptedIOException on thread interrupt
     */
    public int read() throws IOException {
        while (in.available() == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException iex) {
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
        }

        int i = super.read();
        if (i != -1) {
            Logger.logInput((char) i);
            counter++;
        }
        return i;
    }

    public InputStream getUnderlyingStream() {
        return in;
    }

    public void setUnderlyingStream(InputStream is) {
        in = is;
    }

    public long getCounter() {
        return counter;
    }
}
