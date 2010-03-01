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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Creates two fifos for the process input and output and forward them to the specified io tab
 * @author eu155513
 */
public abstract class IOProxy {
    private static final String FILENAME_PREFIX = "debuggerFifo"; // NOI18N
    private static final String FILENAME_EXTENSION = ".fifo"; // NOI18N

    private InputWriterThread irt = null;
    private final Reader ioReader;

    private OutputReaderThread ort = null;
    private final Writer ioWriter;
    
    public static IOProxy create(ExecutionEnvironment execEnv, InputOutput io) {
        IOProxy res;
        if (execEnv == null || execEnv.isLocal()) {
            res = new LocalIOProxy(io.getIn(), io.getOut());
        } else {
            res = new RemoteIOProxy(execEnv, io.getIn(), io.getOut());
        }
        res.start();
        return res;
    }

    private IOProxy(Reader ioReader, Writer ioWriter) {
        this.ioReader = ioReader;
        this.ioWriter = ioWriter;
    }

    private void start() {
        irt = new InputWriterThread();
        irt.start();
        ort = new OutputReaderThread();
        ort.start();
    }

    public void stop() {
        if (irt != null) {
            irt.cancel();
            try {
                ioReader.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
        if (ort != null) {
            ort.cancel();
            try {
                ioWriter.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            stop();
        } finally {
            super.finalize();
        }
    }

    public abstract String getInFilename();
    protected abstract OutputStream createInStream() throws IOException;

    public abstract String getOutFilename();
    protected abstract InputStream createOutStream() throws IOException;

    /** Helper class forwarding input from the io tab to the file */
    private class InputWriterThread extends Thread {
        private boolean cancel = false;

        public InputWriterThread() {
            setName("TTY InputWriterThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        @Override
        public void run() {
            int ch;

            OutputStream pout = null;

            try {
                pout = createInStream();

                while ((ch = ioReader.read()) != -1) {
                    if (cancel) {
                        return;
                    }
                    pout.write((char) ch);
                    pout.flush();
                }
            } catch (IOException e) {
            } finally {
                // Handle EOF and other exits
                try {
                    pout.flush();
                    pout.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        public void cancel() {
            cancel = true;
        }
    }

    /** Helper class forwarding output from the file to the io tab */
    private class OutputReaderThread  extends Thread {
        private boolean cancel = false;

        public OutputReaderThread() {
            setName("TTY OutputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        @Override
        public void run() {
            InputStream in = null;
            try {
                int read;
                in = createOutStream();

                while ((read = in.read()) != (-1)) {
                    if (cancel) { // 131739
                        return;
                    }
                    if (read == 10) {
                        ioWriter.write("\n"); // NOI18N
                    } else {
                        ioWriter.write((char) read);
                    }
                    //output.flush(); // 135380
                }
            } catch (IOException e) {
            } finally {
                try {
                    ioWriter.flush();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void cancel() {
            cancel = true;
        }
    }

    private static class LocalIOProxy extends IOProxy {
        private final File inFile;
        private final File outFile;

        public LocalIOProxy(Reader ioReader, Writer ioWriter) {
            super(ioReader, ioWriter);
            this.inFile = createNewFifo();
            this.inFile.deleteOnExit();
            this.outFile = createNewFifo();
            this.outFile.deleteOnExit();
        }

        private static File createNewFifo() {
            try {
                // TODO : implement more correct way of generating unique filename
                File file = File.createTempFile(FILENAME_PREFIX, FILENAME_EXTENSION); // NOI18N
                file.delete();
                String tool = "mkfifo"; // NOI18N
                if (Utilities.isWindows()) {
                    tool += ".exe"; // NOI18N
                    File toolFile = new File(CompilerSetUtils.getCygwinBase() + "/bin", tool); // NOI18N
                    if (toolFile.exists()) {
                        tool = toolFile.getAbsolutePath();
                    } else {
                        toolFile = new File(CompilerSetUtils.getCommandFolder(null), tool); // NOI18N
                        if (toolFile.exists()) {
                            tool = toolFile.getAbsolutePath();
                        }
                    }
                }
                ProcessBuilder pb = new ProcessBuilder(tool, file.getAbsolutePath()); // NOI18N
                try {
                    Process p = pb.start();
                    // We need to wait for the end of this command, otherwise file may not be initialized
                    p.waitFor();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                return file;
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        @Override
        protected OutputStream createInStream() throws IOException {
            return new FileOutputStream(inFile);
        }

        @Override
        public String getInFilename() {
            return inFile.getAbsolutePath();
        }

        @Override
        protected InputStream createOutStream() throws IOException {
            return new FileInputStream(outFile);
        }

        @Override
        public String getOutFilename() {
            return outFile.getAbsolutePath();
        }

        @Override
        public void stop() {
            super.stop();
            inFile.delete();
            outFile.delete();
        }
    }

    private static class RemoteIOProxy extends IOProxy {
        private final String inFilename;
        private final String outFilename;
        private final ExecutionEnvironment execEnv;

        public RemoteIOProxy(ExecutionEnvironment execEnv, Reader ioReader, Writer ioWriter) {
            super(ioReader, ioWriter);
            this.execEnv = execEnv;
            this.inFilename = createNewFifo(execEnv);
            this.outFilename = createNewFifo(execEnv);
        }

        @Override
        public String getInFilename() {
            return inFilename;
        }

        @Override
        protected OutputStream createInStream() throws IOException {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setCommandLine("cat > " + inFilename); // NOI18N
            return npb.call().getOutputStream();
        }

        @Override
        public String getOutFilename() {
            return outFilename;
        }

        @Override
        protected InputStream createOutStream() throws IOException {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setCommandLine("cat " + outFilename); // NOI18N
            return npb.call().getInputStream();
        }

        private static String createNewFifo(ExecutionEnvironment execEnv) {
            String tmpDir;
            try {
                tmpDir = HostInfoUtils.getHostInfo(execEnv).getTempDir();
            } catch (Exception iOException) {
                tmpDir = "/tmp"; // NOI18N
            }
            String name = tmpDir + '/' + FILENAME_PREFIX + "$$" + FILENAME_EXTENSION; // NOI18N
            CommandProvider cp = Lookup.getDefault().lookup(CommandProvider.class);
            if (cp.run(execEnv, "sh -c \"mkfifo " + name + ";echo " + name + "\"", null) == 0) { // NOI18N
                return cp.getOutput().trim();
            }
            return null;
        }

        @Override
        public void stop() {
            super.stop();
            // delete files
            CommonTasksSupport.rmFile(execEnv, inFilename, null);
            CommonTasksSupport.rmFile(execEnv, outFilename, null);
        }
    }
}
