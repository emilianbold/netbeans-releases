/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class ProcessUtils  {

    public static final Logger LOG = Logger.getLogger("nativeexecution.support.logger"); // NOI18N
    
    private ProcessUtils() {
    }
    private final static String remoteCharSet = System.getProperty("cnd.remote.charset", "UTF-8"); // NOI18N

    public static ExitStatus execute(final ProcessBuilder builder, final String executable, final String... args) {
        builder.setExecutable(executable);
        builder.setArguments(Arrays.asList(args));
        return execute(builder);
    }

    public static ExitStatus executeInDir(final String workingDir, final ProcessBuilder builder, final String executable, final String... args) {
        builder.setWorkingDirectory(workingDir);
        builder.setExecutable(executable);
        builder.setArguments(Arrays.asList(args));
        return execute(builder);
    }

    public static ExitStatus execute(final ProcessBuilder processBuilder) {
        ExitStatus result;
        Future<String> error;
        Future<String> output;

        if (processBuilder == null) {
            throw new NullPointerException("NULL process builder!"); // NOI18N
        }

        try {
            final Process process = processBuilder.call();
            error = NativeTaskExecutorService.submit(new Callable<String>() {

                @Override
                public String call() throws Exception {
                    return readProcessErrorLine(process);
                }
            }, "e"); // NOI18N
            output = NativeTaskExecutorService.submit(new Callable<String>() {

                @Override
                public String call() throws Exception {
                    return readProcessOutputLine(process);
                }
            }, "o"); // NOI18N

            result = new ExitStatus(process.waitFor(), output.get(), error.get());
        } catch (InterruptedException ex) {
            result = new ExitStatus(-100, "", ex.getMessage());
        } catch (Throwable th) {
            LOG.log(Level.INFO, th.getMessage(), th);
            result = new ExitStatus(-200, "", th.getMessage());
        }

        return result;
    }

    private static String getRemoteCharSet() {
        return remoteCharSet;
    }

    private static BufferedReader getReader(final InputStream is, String charSet) {
        // set charset
        try {
            return new BufferedReader(new InputStreamReader(is, charSet));
        } catch (UnsupportedEncodingException ex) {
            String msg = getRemoteCharSet() + " encoding is not supported, try to override it with cnd.remote.charset"; //NOI18N
            Exceptions.printStackTrace(new IllegalStateException(msg, ex));
        }
        return new BufferedReader(new InputStreamReader(is));
    }

    private static String readProcessErrorLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getErrorStream(), getCharSet(p));
    }

    private static String readProcessOutputLine(final Process p) throws IOException {
        if (p == null) {
            return ""; // NOI18N
        }

        return readProcessStreamLine(p.getInputStream(), getCharSet(p));
    }

    private static String getCharSet(Process process) {
        return remoteCharSet;
    }

    private static String readProcessStreamLine(final InputStream stream, String charSet) throws IOException {
        if (stream == null) {
            return ""; // NOI18N
        }

        final StringBuilder result = new StringBuilder();
        final BufferedReader br = getReader(stream, charSet);

        try {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (!first) {
                    result.append('\n');
                }
                result.append(line);
                first = false;
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return result.toString();
    }

    public static final class ExitStatus {

        public final int exitCode;
        public final String error;
        public final String output;

        private ExitStatus(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
        }

        public boolean isOK() {
            return exitCode == 0;
        }

        @Override
        public String toString() {
            return "ExitStatus " + "exitCode=" + exitCode + "\nerror=" + error + "\noutput=" + output; // NOI18N
        }
    }

    public static interface PostExecutor {

        public void processFinished(ExitStatus status);
    }
}
