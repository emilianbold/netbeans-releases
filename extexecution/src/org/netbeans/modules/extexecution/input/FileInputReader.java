/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.extexecution.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputReader;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr Hejl
 */
public class FileInputReader implements InputReader {

    private static final Logger LOGGER = Logger.getLogger(FileInputReader.class.getName());

    private static final int BUFFER_SIZE = 512;

    private final Callable<File> fileGenerator;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private File currentFile;

    private ReadableByteChannel channel;

    private long fileLength;

    private boolean closed;

    public FileInputReader(Callable<File> fileGenerator) {
        assert fileGenerator != null;

        this.fileGenerator = fileGenerator;
    }

    public int readOutput(InputProcessor outputProcessor) {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        int fetched = 0;
        try {
            File file = fileGenerator.call();

            if ((currentFile != file && (currentFile == null || !currentFile.equals(file)))
                    || fileLength > currentFile.length() || channel == null) {

                if (channel != null) {
                    channel.close();
                }

                currentFile = file;

                if (currentFile != null && currentFile.exists()
                        && currentFile.canRead()) {

                    channel = Channels.newChannel(
                            new BufferedInputStream(new FileInputStream(currentFile)));
                }
                if (fileLength > 0) {
                    outputProcessor.reset();
                }
                fileLength = 0;
            }

            if (channel == null || !channel.isOpen()) {
                return fetched;
            }

            buffer.clear();
            int size = channel.read(buffer);
            if (size > 0) {
                fileLength += size;
                buffer.position(0).limit(size);
                fetched += size;

                if (outputProcessor != null) {
                    byte[] toProcess = new byte[size];
                    buffer.get(toProcess);
                    outputProcessor.processInput(toProcess);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, null, ex);
            // we will try the next loop (if any)
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException iex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }

        return fetched;
    }

    public void close() throws IOException {
        closed = true;
        if (channel != null && channel.isOpen()) {
            channel.close();
            channel = null;
        }
    }

}
