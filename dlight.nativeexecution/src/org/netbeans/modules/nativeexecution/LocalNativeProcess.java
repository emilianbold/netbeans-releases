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
package org.netbeans.modules.nativeexecution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.text.ParseException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.netbeans.modules.nativeexecution.support.WindowsSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

public final class LocalNativeProcess extends AbstractNativeProcess {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final static String shell;
    private final static boolean isWindows;
    private final static boolean isMacOS;
    private final InputStream processOutput;
    private final InputStream processError;
    private final OutputStream processInput;
    private final Process process;


    static {
        String sh = null;

        try {
            sh = HostInfoUtils.getShell(new ExecutionEnvironment());
        } catch (ConnectException ex) {
        }

        shell = sh;

        isWindows = Utilities.isWindows();
        isMacOS = Utilities.isMac();
    }

    // TODO: For now cygwin is the ONLY tested environment on Windows!
    public LocalNativeProcess(NativeProcessInfo info) throws IOException {
        super(info);

        if (Utilities.isWindows() && shell == null) {
            throw new IOException("CYGWIN currently is the ONLY supported env on Windows."); // NOI18N
        }

        final String workingDirectory = info.getWorkingDirectory(true);
        final File wdir =
                workingDirectory == null ? null : new File(workingDirectory);

        final ProcessBuilder pb;

        final MacroMap env = info.getEnvVariables();

        pb = new ProcessBuilder(shell, "-s"); // NOI18N

        if (info.isUnbuffer()) {
            String unbufferPath = null; // NOI18N
            String unbufferLib = null; // NOI18N

            try {
                unbufferPath = info.macroExpander.expandPredefinedMacros(
                        "bin/nativeexecution/$osname-$platform"); // NOI18N
                unbufferLib = info.macroExpander.expandPredefinedMacros(
                        "unbuffer.$soext"); // NOI18N
            } catch (ParseException ex) {
            }

            if (unbufferLib != null && unbufferPath != null) {
                InstalledFileLocator fl = InstalledFileLocator.getDefault();
                File file = fl.locate(unbufferPath + "/" + unbufferLib, null, false); // NOI18N


                if (file != null && file.exists()) {
                    unbufferPath = file.getParentFile().getAbsolutePath();

                    String ldPreloadEnv;
                    String ldLibraryPathEnv;
                    
                    if (isWindows) {
                        ldLibraryPathEnv = "PATH"; // NOI18N
                        ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                    } else if (isMacOS) {
                        ldLibraryPathEnv = "DYLD_LIBRARY_PATH"; // NOI18N
                        ldPreloadEnv = "DYLD_INSERT_LIBRARIES"; // NOI18N
                    } else {
                        ldLibraryPathEnv = "LD_LIBRARY_PATH"; // NOI18N
                        ldPreloadEnv = "LD_PRELOAD"; // NOI18N
                    }

                    String ldPreload = env.get(ldPreloadEnv);

                    if (isMacOS || isWindows) {
                        // TODO: FIXME (?) For Mac and Windows just put unbuffer 
                        // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                        // Reason: no luck to make it work using PATH ;(
                        ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                                unbufferPath + "/" + unbufferLib; // NOI18N
                        
                        if (isWindows) {
                            ldPreload = WindowsSupport.getInstance().normalizePath(ldPreload);
                        }
                    } else {
                        ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                                unbufferLib;
                    }

                    env.put(ldPreloadEnv, ldPreload);

                    if (isMacOS) {
                        env.put("DYLD_FORCE_FLAT_NAMESPACE", "yes"); // NOI18N
                    } else {
                        String ldLibPath = env.get(ldLibraryPathEnv);
                        if (isWindows) {
                            ldLibPath = WindowsSupport.getInstance().normalizeAllPaths(ldLibPath);
                        }
                        ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ":")) + // NOI18N
                                unbufferPath + ":" + unbufferPath + "_64"; // NOI18N
                        env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                    }
                }
            }
        }

        if (isWindows) {
            env.put("PATH", "/bin:$PATH"); // NOI18N
        }

        Process pr = null;

        try {
            pr = pb.start();
        } catch (IOException ex) {
            Logger.getInstance().warning(ex.getMessage());
            throw ex;
        }

        process = pr;

        processOutput = process.getInputStream();
        processError = process.getErrorStream();
        processInput = process.getOutputStream();

        processInput.write("/bin/echo $$\n".getBytes()); // NOI18N
        processInput.flush();

        EnvWriter ew = new EnvWriter(processInput);
        ew.write(env);


        if (wdir != null) {
            String dir = wdir.toString();
            
            if (isWindows) {
                dir = WindowsSupport.getInstance().normalizePath(dir);
            }
            
            processInput.write(("cd \"" + dir + "\"\n").getBytes()); // NOI18N
        }

        String cmd = "exec " + info.getCommandLine() + "\n"; // NOI18N

        if (isWindows) {
            cmd = cmd.replaceAll("\\\\", "/"); // NOI18N
        }

        processInput.write(cmd.getBytes());
        processInput.flush();

        readPID(processOutput);
    }

    @Override
    public OutputStream getOutputStream() {
        return processInput;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    @Override
    public final int waitResult() throws InterruptedException {
        return process.waitFor();
    }

    @Override
    public void cancel() {
        process.destroy();
    }
}
