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
 * A data output stream that also logs everything sent to a Writer (via the
 * logger).
 * @author  Robert Greig
 */
public class LoggedDataOutputStream extends FilterOutputStream {

    private long counter;

    /**
     * Construct a logged stream using the specified underlying stream
     * @param out the stream
     */
    public LoggedDataOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Write a line to the stream, logging it too. For compatibility reasons
     * only. Does exactly the same what writeBytes() does.
     *
     * @deprecated Line to to bytes conversion is host specifics.
     * Use raw byte access methods insted.
     *
     */
    public void writeChars(String line) throws IOException {
        writeBytes(line);
    }
    
    /**
     * Write a line to the stream, logging it too.
     *
     * Line to to bytes conversion is host specifics. Use {@link #writeBytes(String, String)} if possible.
     */
    public void writeBytes(String line) throws IOException {
        byte[] bytes = line.getBytes();
        out.write(bytes);
        Logger.logOutput(bytes);
        counter += bytes.length;
    }

    /**
     * Write a line to the stream, logging it too.
     */
    public void writeBytes(String line, String encoding) throws IOException {
        byte[] bytes = line.getBytes(encoding);
        out.write(bytes);
        Logger.logOutput(bytes);
        counter += bytes.length;
    }

    public void write(int b) throws IOException {
        super.write(b);
        counter++;
    }

    public void write(byte b[]) throws IOException {
        super.write(b);
        counter += b.length;
    }

    public void write(byte b[], int off, int len) throws IOException {
        super.write(b, off, len);
        counter += len;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     */
    public void close() throws IOException {
        out.close();
    }

    public OutputStream getUnderlyingStream() {
        return out;
    }

    public void setUnderlyingStream(OutputStream os) {
        out = os;
    }

    public long getCounter() {
        return counter;
    }
}
