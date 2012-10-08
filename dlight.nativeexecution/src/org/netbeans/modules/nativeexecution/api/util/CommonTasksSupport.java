/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.pty.NbKillUtility;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.SignalSupport;
import org.openide.util.Exceptions;

/**
 * An utility class that simplifies usage of Native Execution Support Module
 * for common tasks like files copying.
 */
public final class CommonTasksSupport {

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

        return SftpSupport.getInstance(srcExecEnv).downloadFile(srcFileName, dstFileName, error);
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

        return SftpSupport.getInstance(srcExecEnv).downloadFile(srcFileName, dstFile.getAbsolutePath(), error);
    }

    /**
     * Read remote file content
     *
     * @param srcFileName full path to the source file with file name
     * @param srcExecEnv execution environment that describes the host to copy file from
     * @param offset in source file
     * @param count number of reading bytes
     * @param error if not <tt>NULL</tt> and some error occurs during reading,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return byte array with file content. Returns byte[0] in case error. Result can be less of count in case end of file.
     */
    public static byte[] readFile(
            final String srcFileName,
            final ExecutionEnvironment srcExecEnv,
            final long offset, final int count,
            final Writer error) {

        int bs = 512;
        long iseek = offset / bs;
        long endOffset = offset + count;
        long cnt = (endOffset / bs);
        if (endOffset%bs > 0) {
            cnt++;
        }
        cnt -= iseek;
        byte[] buffer = new byte[(int)cnt * bs];

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(srcExecEnv);
        if (iseek > 0) {
            // iseek paremeter is Solaris and Mac OS X only.
            //npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "iseek=" + iseek, "count=" + cnt); // NOI18N
            npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "skip=" + iseek, "count=" + cnt); // NOI18N
        } else {
            npb.setExecutable("/bin/dd").setArguments("if=" + srcFileName, "ibs=" + bs, "count=" + cnt); // NOI18N
        }

        int actual;

        try {
            NativeProcess process = npb.call();
            if (process.getState() == State.ERROR) {
                String err = ProcessUtils.readProcessErrorLine(process);
                if (iseek > 0) {
                    throw new IOException("Cannot start /bin/dd if=" + srcFileName + " ibs=" + bs + " skip=" + iseek + " count=" + cnt + ": " + err); // NOI18N
                } else {
                    throw new IOException("Cannot start /bin/dd if=" + srcFileName + " ibs=" + bs + " count=" + cnt + ": " + err); // NOI18N
                }
            }

            int start = 0;
            int rest = buffer.length;
            actual = 0;
            while(true) {
                int readed = process.getInputStream().read(buffer, start, rest);
                if (readed <= 0) {
                    break;
                }
                start += readed;
                rest -= readed;
                actual += readed;
                if (rest <= 0) {
                    break;
                }
            }
            int rc = 0;

            try {
                rc = process.waitFor();
            } catch (InterruptedException ex) {
                throw new IOException("/bin/dd was interrupted"); // NOI18N
            }

            if (rc != 0) {
                String err = ProcessUtils.readProcessErrorLine(process);
                throw new IOException("Error while reading " + srcFileName + ": " + err); // NOI18N
            }
        } catch (IOException ex) {
            if (error != null) {
                try {
                    error.write(ex.getMessage());
                    error.flush();
                } catch (IOException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
            }
            return new byte[0];
        }
        int extra = (int) (offset % bs);
        if (actual - extra < 0) {
            return new byte[0];
        }
        int res_size = Math.min(actual - extra, count);
        byte[] result = new byte[res_size];
        System.arraycopy(buffer, extra, result, 0, res_size);
        return result;
    }

    public static Future<UploadStatus> uploadFile(UploadParameters parameters) {
        parameters = parameters.copy();
        return SftpSupport.getInstance(parameters.dstExecEnv).uploadFile(parameters);
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
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask) {
        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                new File(srcFileName), dstExecEnv, dstFileName, mask, false, null));
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
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task. 
     */
    public static Future<UploadStatus> uploadFile(
            final String srcFileName,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, boolean checkMd5) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                new File(srcFileName), dstExecEnv, dstFileName, mask, checkMd5, null));
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
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(srcFile, dstExecEnv, dstFileName, mask, false, null));
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
     * @param mask file mode creation mask (see uname(1), chmod(1)) (in octal);
     * if it is less than zero, the default mask is used (for existent files, it stays the same it was, for new files as specified by umask command))
     * @param error if not <tt>NULL</tt> and some error occurs during upload,
     *        an error message will be written to this <tt>Writer</tt>.
     * @param checkMd5 if true, then the source file will be copied to destination only if
     *        destination does not exist or exists but its md5 sum differs from local one
     * @return a <tt>Future&lt;UploadStatus&gt;</tt> representing pending completion
     *         of the upload task.
     */
    public static Future<UploadStatus> uploadFile(
            final File srcFile,
            final ExecutionEnvironment dstExecEnv,
            final String dstFileName,
            final int mask, boolean checkMd5) {

        return SftpSupport.getInstance(dstExecEnv).uploadFile(new UploadParameters(
                srcFile, dstExecEnv, dstFileName, mask, checkMd5, null));
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

        @Override
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

    public enum SIGNAL_SCOPE {
        PROCESS,
        GROUP,
        SESSION
    }

    public static Future<Integer> sendSignal(final ExecutionEnvironment execEnv, final SIGNAL_SCOPE scope, final int id, final Signal signal, final Writer error) {
        final String descr = "Sending signal " + signal + " to " + scope + " " + id; // NOI18N
        return NativeTaskExecutorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                switch (scope) {
                    case PROCESS:
                        return NbKillUtility.getInstance().signalProcess(execEnv, signal, id);
                    case GROUP:
                        return NbKillUtility.getInstance().signalGroup(execEnv, signal, id);
                    case SESSION:
                        return NbKillUtility.getInstance().signalSession(execEnv, signal, id);
                    default:
                        return -1;
                }
            }
        }, descr);
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
        return sendSignal(execEnv, SIGNAL_SCOPE.PROCESS, pid, signal, error);
    }

    /**
     * Sends the signal to the process group.
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
    public static Future<Integer> sendSignalGrp(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        return sendSignal(execEnv, SIGNAL_SCOPE.GROUP, pid, signal, error);
    }

    public static Future<Integer> sendSignalSession(final ExecutionEnvironment execEnv, final int pid, final Signal signal, final Writer error) {
        return sendSignal(execEnv, SIGNAL_SCOPE.SESSION, pid, signal, error);
    }

    public static class UploadStatus {
        private final int exitCode;
        private final String error;
        private final FileInfoProvider.StatInfo statInfo;

        /*packge*/ UploadStatus(int exitCode, String error, StatInfo statInfo) {
            this.exitCode = exitCode;
            this.error = error;
            this.statInfo = statInfo;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
        
        public int getExitCode() {
            return exitCode;
        }
       
        public String getError() {
            return error;
        }

        public StatInfo getStatInfo() {
            return statInfo;
        }
    }
    
    /**
     * A class (an analog of C struct) that contains upload parameters.
     */
    public static class UploadParameters  {
        
        /**
         * specifies full path to the source file on the local host
         */
        public final File srcFile;

        /** */
        public final ExecutionEnvironment dstExecEnv;

        /**
         * destination filename on the host, specified by <tt>dstExecEnv</tt>
         */
        public final String dstFileName;

        /**
         * File mode creation mask (see uname(1), chmod(1)), in octal.
         * iIf it is less than zero (which is the default),
         * then the default mask is used (for existent files, it stays the same it was,
         * for new files as specified by umask command))
         */
        public int mask;

        /** */
        public ChangeListener callback;

        /**
         * Of true, then the source file will be copied to destination only if
         * destination does not exist or exists but its md5 sum differs from local one
         */
        public boolean checkMd5;

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName) {
            this.srcFile = srcFile;
            this.dstExecEnv = dstExecEnv;
            this.dstFileName = dstFileName;
            mask = -1;
            callback = null;
            checkMd5 = false;
        }

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName, int mask) {
            this(srcFile, dstExecEnv, dstFileName, mask, false, null);
        }

        public UploadParameters(File srcFile, ExecutionEnvironment dstExecEnv, String dstFileName, int mask, boolean checkMd5, ChangeListener callback) {
            this(srcFile, dstExecEnv, dstFileName);
            this.mask = mask;
            this.checkMd5 = checkMd5;
            this.callback = callback;
        }

        /*package*/ UploadParameters copy() {
            return new UploadParameters(srcFile, dstExecEnv, dstFileName, mask, checkMd5, callback);
        }
    }

    /**
     * Queue a signal to a process (sigqueue)
     *
     * @param execEnv  execution environment of the process
     * @param pid  pid of the process
     * @param signo  signal number
     * @param value  signal value
     * @param error  if not <tt>null</tt> and some error occurs,
     *        an error message will be written to this <tt>Writer</tt>
     * @return a <tt>Future&lt;Integer&gt;</tt> representing exit code
     *         of the signal task. <tt>0</tt> means success, any other value
     *         means failure.
     */
    public static Future<Integer> sigqueue(final ExecutionEnvironment execEnv,
            final int pid,
            final int signo,
            final int value,
            final Writer error) {
        final String descr = "Sigqueue " + signo + " with value " + value + " to " + pid; // NOI18N

        return NativeTaskExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                SignalSupport support = SignalSupport.getSignalSupportFor(execEnv);
                return support.sigqueue(pid, signo, value);
            }
        }, descr);
    }
}
