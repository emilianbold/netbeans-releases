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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.SignalSupport;

/**
 * An utility class that simplifies usage of Native Execution Support Module
 * for common tasks like files copying.
 */
public final class CommonTasksSupport {

    private final static HashMap<ExecutionEnvironment, SignalSupport> ssMap =
            new HashMap<ExecutionEnvironment, SignalSupport>();

    private CommonTasksSupport() {
    }

    /** TODO: move it to some common place within nativeexecution */
    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    /**
     * Starts <tt>srcFileName</tt> file download from the host,
     * specified by the <tt>srcExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param dstFileName destination filename on the local host
     * @param error if not <tt>NULL</tt> and some error occurs during download,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the download task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully downlodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> downloadFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final String dstFileName,
            final Writer error) {

        return SftpSupport.downloadFile(srcFileName, srcExecEnv, dstFileName, error);
    }

    /**
     * Starts <tt>srcFileName</tt> file download from the host,
     * specified by the <tt>srcExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param dstFile destination file on the local host
     * @param error if not <tt>NULL</tt> and some error occurs during download,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the download task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully downlodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> downloadFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final File dstFile,
            final Writer error) {

        return SftpSupport.downloadFile(srcFileName, srcExecEnv, dstFile.getAbsolutePath(), error);
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal)
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the upload task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully uplodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, final Writer error) {

        return SftpSupport.uploadFile(srcFileName, dstExecEnv, dstFileName, mask, error, false);
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFileName full path to the source file with file name
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal)
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the upload task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully uplodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, final Writer error, boolean checkMd5) {

        return SftpSupport.uploadFile(srcFileName, dstExecEnv, dstFileName, mask, error, checkMd5);
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFile the source file that reside on the local host
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal)
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the upload task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully uplodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, final Writer error) {

        return SftpSupport.uploadFile(srcFile.getAbsolutePath(), dstExecEnv, dstFileName, mask, error, false);
    }

    /**
     * Starts <tt>srcFileName</tt> file upload from the localhost to the host,
     * specified by the <tt>dstExecEnv</tt> saving it in the
     * <tt>dstFileName</tt> file with the given file mode creation mask. <p>
     * In the case of some error, message with a reason is written to the supplied
     * <tt>error</tt> (is not <tt>NULL</tt>).
     *
     * @param srcFile the source file that reside on the local host
     * @param dstExecEnv execution environment that describes destination host
     * @param dstFileName destination filename on the host, specified by
     *        <tt>dstExecEnv</tt>
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal)
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the upload task. The result of this Future is the exit
     *         code of the copying routine. 0 indicates that the file was
     *         successfully uplodaded. Result other than 0 indicates an error.
     */
    public static Future<Integer> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, final Writer error, boolean checkMd5) {

        return SftpSupport.uploadFile(srcFile.getAbsolutePath(), dstExecEnv, dstFileName, mask, error, checkMd5);
    }

    /**
     * Creates a task for removing a file <tt>fname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete file from
     * @param fname the file name with the full path to it
     * @param error if not <tt>NULL</tt> and some error occurs during file
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the file removing task. The result of this Future indicates
     *         whether the file was removed (0) or not.
     */
    public static Future<Integer> rmFile(ExecutionEnvironment execEnv,
            String fname, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "rm", "-f", fname), // NOI18N
                "rm -f " + fname); // NOI18N
    }

    private static class CommandRunner implements Callable<Integer> {

        private final static Logger log = org.netbeans.modules.nativeexecution.support.Logger.getInstance();
        private final ExecutionEnvironment execEnv;
        private final String cmd;
        private final String[] args;
        private final Writer error;

        public CommandRunner(ExecutionEnvironment execEnv, Writer error, String cmd, String... args) {
            this.execEnv = execEnv;
            this.cmd = cmd;
            this.args = args;
            this.error = error;
        }

        public Integer call() throws Exception {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(cmd).setArguments(args);
            Process p = npb.call();

            int exitStatus = p.waitFor();

            if (exitStatus != 0) {
                if (error != null) {
                    ProcessUtils.writeError(error, p);
                } else {
                    ProcessUtils.logError(Level.FINE, log, p);
                }
            }

            return exitStatus;
        }
    }

    /**
     * Changes file permissions.
     *
     * @param execEnv  execution environment where the file is located
     * @param file  file to change permissions for
     * @param mode  new file permissions in octal form, e.g. <tt>0755</tt>
     * @param error if not <tt>null</tt> and some error occurs,
    an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the chmod task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> chmod(final ExecutionEnvironment execEnv,
            final String file, final int mode, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "chmod", String.format("0%03o", mode), file), // NOI18N
                "chmod " + String.format("0%03o ", mode) + file); // NOI18N
    }

    /**
     * Creates a task for removing a directory <tt>dirname</tt> from the host
     * identified by the <tt>execEnv</tt>.
     * @param execEnv execution environment to delete the directory from
     * @param dirname the file name with the full path to it
     * @param recursively if set to <tt>true</tt> then directory is to be
     *        removed recursively.
     * @param error if not <tt>NULL</tt> and some error occurs during directory
     *        removing, an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;Integer&gt;</tt> representing pending completion
     *         of the directory removing task. The result of this Future indicates
     *         whether the directory was removed (0) or not.
     */
    public static Future<Integer> rmDir(final ExecutionEnvironment execEnv,
            String dirname, boolean recursively, final Writer error) {
        String cmd = recursively ? "rm" : "rmdir"; // NOI18N

        String[] args = recursively
                ? new String[]{"-rf", dirname} : new String[]{"-f", dirname}; // NOI18N

        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, cmd, args),
                cmd + ' ' + Arrays.toString(args));
    }

    /**
     * Creates a directory (and parent directories if needed).
     *
     * @param execEnv  execution environment to create directory in
     * @param dirname  absolute path of created directory
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the mkdir task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> mkDir(final ExecutionEnvironment execEnv,
            final String dirname, final Writer error) {
        return NativeTaskExecutorService.submit(
                new CommandRunner(execEnv, error, "mkdir", "-p", dirname), // NOI18N
                "mkdir -p " + dirname); // NOI18N
    }

    /**
     * Sends the signal to the process.
     *
     * @param execEnv  execution environment of the process
     * @param pid  pid of the process
     * @param signal  signal name, e.g. "SIGKILL", "SIGUSR1"
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the signal task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> sendSignal(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        final String descr = "Sending signal " + signal + " to " + pid; // NOI18N

        return NativeTaskExecutorService.submit(new Callable<Integer>() {

            public Integer call() throws Exception {
                SignalSupport support = null;
                synchronized (ssMap) {
                    support = ssMap.get(execEnv);
                    if (support == null) {
                        support = SignalSupport.getSignalSupportFor(execEnv);
                        ssMap.put(execEnv, support);
                    }
                }

                return support.kill(signal, pid);
            }
        }, descr);
    }
}
