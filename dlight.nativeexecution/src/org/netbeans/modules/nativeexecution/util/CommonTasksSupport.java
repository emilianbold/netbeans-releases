/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.Listener;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.windows.InputOutput;

/**
 *
 */
public final class CommonTasksSupport {

    public static Future<Integer> copyLocalFile(
            final ExecutionEnvironment execEnv,
            final String srcFileName, final String dstFileName,
            final int mask, final Writer error) {

        final File localFile = new File(srcFileName);

        if (!localFile.exists()) {
            if (error != null) {
                try {
                    error.append("File " + srcFileName + " not found!"); // NOI18N
                } catch (IOException ex) {
                }
            }
            return null;
        }

        if (!localFile.canRead()) {
            if (error != null) {
                try {
                    error.append("File " + srcFileName + " is not readable!"); // NOI18N
                } catch (IOException ex) {
                }
            }
            return null;
        }

        final String cmd = "/bin/scp -p -t " + dstFileName; // NOI18N

        Listener processListener = new Listener() {

            public void processStateChanged(NativeProcess p,
                    State oldState, State newState) {
                if (newState == State.RUNNING) {
                    new CopyRoutine(p.getOutputStream(), localFile, mask).start();
                }
            }
        };

        NativeProcessBuilder npb = new NativeProcessBuilder(execEnv, cmd).addNativeProcessListener(processListener);

        ExecutionDescriptor descriptor =
                new ExecutionDescriptor().inputOutput(
                InputOutput.NULL).outProcessorFactory(
                new InputProcessorFactory() {

                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return new FilterInputProcessor(InputProcessors.copying(error));
                    }
                });

        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "scp " + cmd); // NOI18N

        return execService.run();
    }

    private static final class CopyRoutine extends Thread {

        private final OutputStream outStream;
        private final File srcFile;
        private final int mask;

        public CopyRoutine(OutputStream in, File srcFile, int mask) {
            this.outStream = in;
            this.srcFile = srcFile;
            this.mask = mask;
        }

        @Override
        public void run() {
            WritableByteChannel out = null;
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            boolean interrupted = false;
            ReadableByteChannel in = null;

            try {
                out = Channels.newChannel(outStream);
                long filesize = srcFile.length();
                int workUnitsLimit = 1;
                double workUnitFactor = 1;
                if (filesize > Integer.MAX_VALUE) {
                    workUnitsLimit = Integer.MAX_VALUE;
                    workUnitFactor = Integer.MAX_VALUE / filesize;
                } else {
                    workUnitsLimit = (int) filesize;
                    workUnitFactor = 1;
                }

                String command = String.format("C0%03d %d %s\n", // NOI18N
                        mask, filesize, srcFile.getName());
                buffer.put(command.getBytes(), 0, command.length());

                // send a content of srcFile
                final FileInputStream fis = new FileInputStream(srcFile);
                in = Channels.newChannel(fis);
                long sendBytes = 0;
                int progress = 0;

                while (true) {
                    int len = in.read(buffer);
                    if (len <= 0) {
                        break;
                    }
                    buffer.flip();
                    try {
                        out.write(buffer);
                    } catch (Exception ex) {
                        throw new InterruptedException();
                    }
                    buffer.clear();
                    sendBytes += len;
                    progress = (int) (sendBytes * workUnitFactor);
                }

                // Finally write 0 byte
                buffer.clear();
                buffer.put((byte) 0);
                buffer.flip();
                out.write(buffer);
            } catch (InterruptedException ie) {
                interrupted = true;
            } catch (IOException ex) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                    }
                }

                /* Here I have faced with the following problem:
                 * If some error occured (permission denied, as an example),
                 * an attempt to close out leads to JVM crash...
                 * Interesting thing is that out is not null and even is not
                 * closed (isOpen() returns true)...
                 * So this was overcomed using interrupted flag ...
                 *
                 * TODO: I don't like this solution.
                 */

                if (!interrupted && out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    private static class FilterInputProcessor implements InputProcessor {

        private final InputProcessor delegate;
        private boolean closed;

        public FilterInputProcessor(InputProcessor delegate) {
            this.delegate = delegate;
        }

        public void processInput(char[] chars) throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor"); // NOI18N
            }

            StringBuilder sb = new StringBuilder(chars.length);
            for (char c : chars) {
                if (c >= 32 || c == 10 || c == 13) {
                    sb.append(c);
                }
            }

            delegate.processInput(sb.toString().toCharArray());
        }

        public void reset() throws IOException {
            if (closed) {
                throw new IllegalStateException("Already closed processor"); // NOI18N
            }
            delegate.reset();
        }

        public void close() throws IOException {
            closed = true;
            delegate.close();
        }
    }
}
