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
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * Creates two fifos for the process input and output and forward them to the specified io tab
 * @author eu155513
 */
public class InputProxy {
    private final File file;
    //private final InputOutput io;
    //private final OutputReaderThread ort;
    private final InputReaderThread irt;

    public InputProxy(String hkey, InputOutput io) {
        file = createNewFifo();
        file.deleteOnExit();
        //this.io = io;
        irt = new InputReaderThread(io.getIn());
        irt.start();
    }

    private static File createNewFifo() {
        try {
            // TODO : implement more correct way of generating unique filename
            File file = File.createTempFile("gdbFifo", null); // NOI18N
            file.delete();
            ProcessBuilder pb = new ProcessBuilder("mkfifo", file.getAbsolutePath()); // NOI18N
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

    public String getFilename() {
        return file.getAbsolutePath();
    }

    public void stop() {
        irt.cancel();
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        file.delete();
    }

    /*private final class OutputReaderThread  extends Thread {

        /** This is all output, not just stderr */
        /*private Writer output;
        private boolean cancel = false;

        public OutputReaderThread(Writer output) {
            this.output = output;
            setName("TTY OutputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        /**
         *  Reader proc to read the combined stdout and stderr from the build process.
         *  The output comes in on a single descriptor because the build process is started
         *  via a script which diverts stdout to stderr. This is because older versions of
         *  Java don't have a good way of interleaving stdout and stderr while keeping the
         *  exact order of the output.
         */
        /*@Override
        public void run() {
            InputStream is = null;
            try {
                is = new FileInputStream(outFile);
                int read;

                for (;;) {
                    int available = is.available();
                    if (available == 0) {
                        if (cancel) {
                            return;
                        }
                        try {
                            sleep(500);
                        } catch(InterruptedException e) {
                        }
                        continue;
                    } else {
                        while (available-- > 0) {
                            read = is.read();
                            if (read == 10) {
                                output.write("\n"); // NOI18N
                            } else {
                                output.write((char) read);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } finally {
                try {
                    output.flush();
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        public void cancel() {
            cancel = true;
        }
    }*/

    /** Helper class to read the input from the build */
    private final class InputReaderThread extends Thread {

        /** This is all output, not just stderr */
        private Reader in;
        private boolean cancel = false;

        public InputReaderThread(Reader in) {
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
                pout = new FileOutputStream(file);

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
}
