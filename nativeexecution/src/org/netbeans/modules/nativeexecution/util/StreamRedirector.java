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
package org.netbeans.modules.nativeexecution.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;

public class StreamRedirector extends Thread {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private Reader reader;
    private Writer writer;
    private String name;

    private static final void stopIfInterrupted() throws InterruptedException {
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Stopped by stopIfInterrupted()"); // NOI18N
        }
    }

    public StreamRedirector(Reader reader, Writer writer, String name) {
        super("Stream redirection thread " + name);
        this.reader = reader;
        this.writer = writer;
        this.name = name;
    }

    public StreamRedirector(InputStream is, OutputStream os, String name) {
        this(Channels.newReader(Channels.newChannel(is), "UTF-8"), // NOI18N
                new PrintWriter(os), name);
    }

    public StreamRedirector(InputStream is, Writer writer, String name) {
        this(Channels.newReader(Channels.newChannel(is), "UTF-8"),
                writer, name);
    }

    public StreamRedirector(Reader reader, OutputStream os, String name) {
        this.reader = reader;
        this.writer = new PrintWriter(os);
        this.name = name;
    }

    @Override
    public void run() {
        if (writer == null || reader == null) {
            return;
        }

        log.fine("StreamRedirector " + name + " started in thread " + Thread.currentThread().toString()); // NOI18N

//      PrintWriter bwriter = new PrintWriter(writer);
        BufferedReader breader = new BufferedReader(reader);
        try {
            String line;
            while ((line = breader.readLine()) != null) {
                if (!"".equals(line)) {
                    writer.write(line);
                    writer.append('\n');
                    writer.flush();
                }
            }
        } catch (IOException ex) {
            if (ex.getCause() instanceof InterruptedException) {
                // OK ... expected
            } else {
                log.severe("IOException in StreamRedirector " + name + ": " + ex.toString()); // NOI18N
            }
        } catch (Exception ex) {
            log.severe("Exception in StreamRedirector " + name + ": " + ex.toString()); // NOI18N
        }

    }
}
