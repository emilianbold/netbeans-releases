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

package org.netbeans.modules.cnd.debugger.gdb.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.remote.InteractiveCommandProvider;
import org.netbeans.modules.cnd.api.remote.InteractiveCommandProviderFactory;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * Creates two fifos for the process input and output and forward them to the specified io tab
 * @author eu155513
 */
public abstract class InputProxy {
    private static final String FILENAME_PREFIX = "gdbFifo"; // NOI18N
    private static final String FILENAME_EXTENSION = ".fifo"; // NOI18N

    private InputWriterThread irt = null;
    private final Reader ioReader;
    
    public static InputProxy create(String hkey, InputOutput io) {
        InputProxy res;
        if (hkey == null || CompilerSetManager.LOCALHOST.equals(hkey)) {
            res = new LocalInputProxy(io.getIn());
        } else {
            res = new RemoteInputProxy(hkey, io.getIn());
        }
        res.start();
        return res;
    }

    private InputProxy(Reader ioReader) {
        this.ioReader = ioReader;
    }

    private void start() {
        irt = new InputWriterThread(ioReader);
        irt.start();
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
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
    }

    public abstract String getFilename();
    protected abstract OutputStream createStream() throws IOException;

    /** Helper class to read the input from the build */
    private class InputWriterThread extends Thread {

        /** This is all output, not just stderr */
        private final Reader in;
        private boolean cancel = false;

        public InputWriterThread(Reader in) {
            this.in = in;
            setName("TTY inputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        /**
         *  Reader proc to read input from Output2's input textfield and send it
         *  to the running process.
         */
        @Override
        public void run() {
            int ch;

            OutputStream pout = null;

            try {
                pout = createStream();

                while ((ch = in.read()) != -1) {
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
                    ex.printStackTrace();
                }
            }
        }
        
        public void cancel() {
            cancel = true;
        }
    }

    private static class LocalInputProxy extends InputProxy {
        private final File file;

        public LocalInputProxy(Reader ioReader) {
            super(ioReader);
            this.file = createNewFifo();
            this.file.deleteOnExit();
        }

        private static File createNewFifo() {
            try {
                // TODO : implement more correct way of generating unique filename
                File file = File.createTempFile(FILENAME_PREFIX, FILENAME_EXTENSION); // NOI18N
                file.delete();
                String tool = "mkfifo"; // NOI18N
                if (Utilities.isWindows()) {
                    tool += ".exe"; // NOI18N
                    File toolFile = new File(CppUtils.getCygwinBase() + "/bin", tool); // NOI18N
                    if (toolFile.exists()) {
                        tool = toolFile.getAbsolutePath();
                    } else {
                        toolFile = new File(CppUtils.getMSysBase() + "/bin", tool); // NOI18N
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
        protected OutputStream createStream() throws IOException {
            return new FileOutputStream(file);
        }

        @Override
        public String getFilename() {
            return file.getAbsolutePath();
        }

        @Override
        public void stop() {
            super.stop();
            file.delete();
        }
    }

    private static class RemoteInputProxy extends InputProxy {
        private final String filename;
        private final String hkey;
        private InteractiveCommandProvider provider = null;

        public RemoteInputProxy(String hkey, Reader ioReader) {
            super(ioReader);
            this.hkey = hkey;
            this.filename = createNewFifo(hkey);
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        protected OutputStream createStream() throws IOException {
            provider = InteractiveCommandProviderFactory.create(hkey);
            if (provider != null && provider.run(hkey, "cat > " + filename, null)) { // NOI18N
                return provider.getOutputStream();
            }
            return null;
        }

        private static String createNewFifo(String hkey) {
            // TODO: need to create unique file!!!
            String name = "/tmp/" + FILENAME_PREFIX; // NOI18N
            CommandProvider cp = Lookup.getDefault().lookup(CommandProvider.class);
            if (cp != null) {
                cp.run(hkey, "mkfifo " + name, null); // NOI18N
            }
            return name;
        }

        @Override
        public void stop() {
            super.stop();
            if (provider != null) {
                provider.disconnect();
            }
            // delete the file
            CommandProvider cp = Lookup.getDefault().lookup(CommandProvider.class);
            if (cp != null) {
                cp.run(hkey, "rm -f " + filename, null); // NOI18N
            }
        }
    }
}
