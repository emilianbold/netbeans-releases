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

package org.netbeans.modules.server.output;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.server.output.FileInputProvider;
import org.netbeans.api.server.output.LineProcessor;
import org.netbeans.api.server.output.LineReader;
import org.openide.util.Parameters;

/**
 *
 * This class is <i>NotThreadSafe</i>.
 * @author Petr Hejl
 */
public class FileLineReader implements LineReader {

    private static final Logger LOGGER = Logger.getLogger(FileLineReader.class.getName());

    private static final int BUFFER_SIZE = 128;

    private final LineParsingHelper helper = new LineParsingHelper();

    private final FileInputProvider fileProvider;

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private FileInputProvider.FileInput currentFile;

    private ReadableByteChannel channel;

    private long fileLength;

    private boolean closed;

    public FileLineReader(FileInputProvider fileProvider) {
        Parameters.notNull("fileProvider", fileProvider);

        this.fileProvider = fileProvider;
    }

    public int readLines(LineProcessor lineProcessor, boolean allAvailable) {
        if (closed) {
            throw new IllegalStateException("Already closed reader");
        }

        int fetched = 0;
        try {
            FileInputProvider.FileInput file = fileProvider.getFileInput();

            if ((currentFile != file && (currentFile == null || !currentFile.equals(file)))
                    || fileLength > currentFile.getFile().length() || channel == null) {

                if (channel != null) {
                    channel.close();
                }

                currentFile = file;

                if (currentFile != null && currentFile.getFile().exists()
                        && currentFile.getFile().canRead()) {

                    channel = Channels.newChannel(
                            new FileInputStream(currentFile.getFile()));
                }
                if (fileLength > 0) {
                    lineProcessor.reset();
                }
                fileLength = 0;
                // TODO trailing line flush (?)
            }

            if (channel == null) {
                return fetched;
            }

            buffer.clear();
            int size = channel.read(buffer);
            if (size > 0) {
                fileLength += size;
                buffer.position(0).limit(size);
                String[] lines = helper.parse(buffer, currentFile.getCharset());
                fetched += lines.length;

                if (lineProcessor != null) {
                    for (String line : lines) {
                        lineProcessor.processLine(line);
                    }
                }
            }
        } catch (IOException ex) {
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

        if (allAvailable) {
            String line = helper.getTrailingLine(true);
            if (line != null) {
                if (lineProcessor != null) {
                    lineProcessor.processLine(line);
                }
                fetched += 1;
            }
        }
        return fetched;
    }

    public void close() throws IOException {
        closed = true;
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

}
