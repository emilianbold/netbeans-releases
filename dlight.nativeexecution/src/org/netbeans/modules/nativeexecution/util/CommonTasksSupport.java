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

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.NativeTaskListener;
import org.netbeans.modules.nativeexecution.support.NativeTaskAccessor;

/**
 * This is an utility class that is a factory for several common native tasks
 * such as file copying or removing.
 * The common usage of the class is as follows:
 * <pre>
 *       StringBuilder rmTaskError = new StringBuilder();
 *       boolean forceRemoveReadOnlyFile = false;
 *       String fileToRemove = "/path/to/the/file/to/remove";
 *       ExecutionEnvironment env = new ExecutionEnvironment();
 *
 *       NativeTask rmTask = CommonTasksSupport.getRemoveFileTask(
 *               env, fileToRemove, forceRemoveReadOnlyFile, rmTaskError);
 *
 *       try {
 *           Integer result = rmTask.submit(false);
 *           System.out.println("RESULT: " + result);
 *           if (result != 0) {
 *               System.out.println("ERROR: " + rmTaskError);
 *           }
 *       } catch (Exception ex) {
 *           Exceptions.printStackTrace(ex);
 *       }
 * </pre>
 * or just
 * <pre>
 *       try {
 *           CommonTasksSupport.getRemoveFileTask(new ExecutionEnvironment(),
 *               "/path/to/the/file/to/remove", true, null).submit(false);
 *       } catch (Exception ex) {
 *           Exceptions.printStackTrace(ex);
 *       }
 * </pre>
 */
public final class CommonTasksSupport {

    /**
     * Returns <tt>NativeTask</tt> that copies a single file <tt>srcFileName</tt>
     * from the localhost to the destination (<tt>dstFileName</tt>) on the host,
     * specified with <tt>execEnv</tt>, setting destination file permissions
     * mode to <tt>mask</tt>. The implementation is based on scp(1) utility and
     * any error messages from scp automatically goes to the provided
     * <tt>Appendable</tt> (if any).
     *
     * @param execEnv destination execution environment.
     * @param srcFileName full path to the source file (on the localhost).
     * @param dstFileName full path to the destination (on the <tt>execEnv</tt>).
     * @param mask destination file creation permissions mode.
     * @param error in case of error, error message will be appended to this
     * <tt>Appendable</tt>. Could be null.
     * @return <tt>NativeTask</tt> that performs file copying.
     */
    public final static NativeTask getCopyLocalFileTask(
            final ExecutionEnvironment execEnv,
            final String srcFileName, final String dstFileName,
            final int mask, final Appendable error) {

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

        String cmd = "/bin/scp -p -t " + dstFileName; // NOI18N
        final NativeTask copyTask = new NativeTask(execEnv, cmd, null);
        final CopyRoutine copyWorker = new CopyRoutine(copyTask, localFile, mask);
        final FilteredCharArrayWriter scpOutWriter = new FilteredCharArrayWriter();
        copyTask.redirectOutTo(scpOutWriter);

        copyTask.addListener(new CommonTaskListener(error) {

            public void taskStarted(NativeTask task) {
                copyWorker.start();
            }

            public void taskFinished(NativeTask task, int result) {
                if (result != 0) {
                    String[] errLines = scpOutWriter.toString().split("\n"); // NOI18N
                    StringBuilder sb = new StringBuilder();
                    for (String line : errLines) {

                        if (line.contains("scp: protocol error")) { // NOI18N
                            continue;
                        }

                        sb.append(line).append("\n"); // NOI18N
                    }

                    if (error != null) {
                        try {
                            error.append(sb.toString().trim());
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        });

        return copyTask;
    }

    /**
     * Returns <tt>NativeTask</tt> that removes a single file (<tt>fileName</tt>)
     * from the host, specified by <tt>execEnv</tt>. Implementation is based on
     * rm(1) and in case of any error, it's output is redirected to the provided
     * <tt>Appendable</tt> (if any).
     *
     * @param execEnv execution environment where task will be executed.
     * @param fileName path to the file to be removed.
     * @param force if set to true, even read-only files will be removed
     * (see rm(1). This is an equivalent of calling /bin/rm -f).
     * @param error in case of error, error message will be appended to this
     * <tt>Appendable</tt>. Could be null.
     *
     * @return <tt>NativeTask</tt> that performs file removing.
     */
    public final static NativeTask getRemoveFileTask(
            final ExecutionEnvironment execEnv,
            final String fileName, final boolean force, final Appendable error) {

        return getRemoveTask(execEnv, fileName, error, force ? "-f" : ""); // NOI18N
    }

    /**
     * Returns <tt>NativeTask</tt> that recursively removes a directory
     * (<tt>dirName</tt>) and it's content from the host, specified by
     * <tt>execEnv</tt>. Implementation is based on rm(1) and in case of any
     * error, it's output is redirected to the provided <tt>Appendable</tt> (if
     * any).
     *
     * @param execEnv execution environment where task will be executed.
     * @param dirName path to the directory to be removed.
     * @param force if set to true, even read-only files will be removed
     * (see rm(1). This is an equivalent of calling /bin/rm -rf).
     * @param error in case of error, error message will be appended to this
     * <tt>Appendable</tt>. Could be null.
     *
     * @return <tt>NativeTask</tt> that performs directory removing.
     */
    public final static NativeTask getRemoveDirectoryTask(
            final ExecutionEnvironment execEnv,
            final String dirName, final boolean force, final Appendable error) {

        return getRemoveTask(execEnv, dirName, error, force ? "-rf" : "-r"); // NOI18N
    }

    private static NativeTask getRemoveTask(
            final ExecutionEnvironment execEnv,
            final String nodeName, final Appendable error,
            final String flags) {

        NativeTask removeTask = new NativeTask(execEnv,
                "/bin/rm", // NOI18N
                new String[]{flags, nodeName});

        final FilteredCharArrayWriter rmErrWriter = new FilteredCharArrayWriter();
        removeTask.redirectErrTo(rmErrWriter);

        if (error != null) {
            removeTask.addListener(new CommonTaskListener(error) {

                public void taskStarted(NativeTask task) {
                }

                public void taskFinished(NativeTask task, int result) {
                    if (result != 0 && error != null) {
                        try {
                            error.append(rmErrWriter.toString());
                        } catch (IOException ex) {
                        }
                    }
                }
            });
        }

        return removeTask;
    }

    private static class FilteredCharArrayWriter extends CharArrayWriter {

        @Override
        public String toString() {
            char[] chars = this.toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : chars) {
                if (c >= 32 || c == 10 || c == 13) {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    private static final class CopyRoutine extends Thread {

        private final NativeTask task;
        private final File srcFile;
        private final int mask;

        public CopyRoutine(NativeTask task, File srcFile, int mask) {
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

                NativeTaskAccessor taskInfo = NativeTaskAccessor.getDefault();
                taskInfo.getExecutor(task).setProgressLimit(workUnitsLimit);

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

                    taskInfo.getExecutor(task).setProgress(progress);
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

    private abstract static class CommonTaskListener
            implements NativeTaskListener {

        private Appendable error;

        public CommonTaskListener(Appendable error) {
            this.error = error;
        }

        public void taskCancelled(NativeTask task, CancellationException cex) {
            if (error != null) {
                try {
                    error.append("Cancelled"); // NOI18N
                } catch (IOException ex) {
                }
            }
        }

        public void taskError(NativeTask task, Throwable t) {
            if (error != null) {
                try {
                    error.append(t.toString());
                } catch (IOException ex) {
                }
            }
        }
    }
}
