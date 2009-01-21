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
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Implementation of files copying routines
 *
 * @author ak119685
 */
public class CopyTask extends NativeTask {

    private final StringBuffer cmdOut;

    private CopyTask(ExecutionEnvironment execEnv, String cmd) {
        super(execEnv, cmd, null);
        cmdOut = new StringBuffer();
        redirectOutTo(new StringBufferWriter(cmdOut));
    }

    /**
     * CopyTask gets scp output by default. In case of error (permission denied,
     * etc.) user can get this message using <tt>getError()</tt>
     *
     * @return error message returned by scp
     */
    public String getError() {
        // every line contains special symbols???
        // filter them out
        char[] chars = cmdOut.toString().toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : chars) {
            if (c >= 32 || c == 10 || c == 13) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Creates and starts asynchronous task that copies local file to remote
     * destination via ssh channel.
     *
     * It works using /bin/scp utility that is started in specified
     * <tt>ExecutionEnvironment</tt> (locally or remote)
     *
     * @param execEnv - <tt>ExecutionEnvironment</tt> to be used to copy file
     * @param srcFileName - path to local file
     * @param dstFileName - path to remote file
     * @param mask - file mode creation mask. The mask is treated in the same
     *               way as the mode operand described in the chmod(1) manual
     *               page.
     * @param showProgress - flag that indicates whether to show copying
     *                       progress or not.
     * @return Returns started <tt>NativeTask</tt>. It can be used to get copy
     *          status/result or to wait for completion.
     *
     * @throws FileNotFoundException if local file is not found or is not
     *         readable
     * @see org.netbeans.modules.nativeexecution.NativeTask
     */
    public static CopyTask copyLocalFile(
            ExecutionEnvironment execEnv,
            String srcFileName, String dstFileName,
            int mask, boolean showProgress) throws FileNotFoundException {
        File localFile = new File(srcFileName);

        if (!(localFile.exists() && localFile.canRead())) {
            String msg = "File " + srcFileName + " not found!"; // NOI18N
            throw new FileNotFoundException(msg);
        }

        String cmd = "/bin/scp -p -t " + dstFileName; // NOI18N
        CopyTask copyTask = new CopyTask(execEnv, cmd);
        copyTask.setShowProgress(showProgress);
        copyTask.submit();

        new CopyRoutine(copyTask, localFile, mask).start();

        return copyTask;
    }

    private static final class CopyRoutine extends Thread {

        private final CopyTask task;
        private final File srcFile;
        private final int mask;

        public CopyRoutine(CopyTask task, File srcFile, int mask) {
            this.task = task;
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
                out = Channels.newChannel(task.getOutputStream());
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

                task.getExecutor().setProgressLimit(workUnitsLimit);
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
                    task.getExecutor().setProgress(progress);
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
}
