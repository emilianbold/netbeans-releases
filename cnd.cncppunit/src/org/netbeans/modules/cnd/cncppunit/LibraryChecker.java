/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.cncppunit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.LinkerDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 * @author Alexey Vladykin
 */
public class LibraryChecker {

    private LibraryChecker() {
    }

    /**
     * Checks if compiler can find given library, i.e. that compilation with
     * <code>-l<i>lib</i></code> can succeed.
     *
     * @param lib  library to check
     * @param compiler  compiler to check
     * @return <code>true</code> if compiler can find the library,
     *      <code>false</code> otherwise
     * @throws IOException if there is a problem launching compiler,
     *      or creating temp files, or connecting to remote host
     * @throws IllegalArgumentException if compiler is not a C or C++ compiler
     */
    public static boolean isLibraryAvailable(String lib, AbstractCompiler compiler) throws IOException {
        ExecutionEnvironment execEnv = compiler.getExecutionEnvironment();
        LinkerDescriptor linker = compiler.getCompilerSet().getCompilerFlavor().getToolchainDescriptor().getLinker();
        String dummyFile = createDummyFile(execEnv, compiler.getKind());
        try {
            NativeProcessBuilder processBuilder = NativeProcessBuilder.newProcessBuilder(execEnv);
            processBuilder.setExecutable(compiler.getPath());
            processBuilder.setArguments(
                    linker.getOutputFileFlag(),
                    dummyFile + ".out", // NOI18N
                    linker.getLibraryFlag() + lib,
                    dummyFile);

            NativeProcess process = processBuilder.call();
            try {
                return process.waitFor() == 0;
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            } finally {
                process.destroy();
            }
        } finally {
            CommonTasksSupport.rmFile(execEnv, dummyFile, null);
            CommonTasksSupport.rmFile(execEnv, dummyFile + ".out", null); // NOI18N
        }
    }

    private static String createDummyFile(ExecutionEnvironment execEnv, ToolKind compilerKind) throws IOException {
        String ext;
        if (compilerKind == PredefinedToolKind.CCompiler) {
            ext = ".c"; // NOI18N
        } else if (compilerKind == PredefinedToolKind.CCCompiler) {
            ext = ".cpp"; // NOI18N
        } else {
            throw new IllegalArgumentException("Illegal tool kind " + compilerKind); // NOI18N
        }

        HostInfo localHostInfo = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
        File dummyFile = File.createTempFile("dummy", ext, localHostInfo.getTempDirFile()); // NOI18N
        try {
            FileWriter writer = new FileWriter(dummyFile);
            try {
                writer.write("int main(int argc, char** argv) { return 0; }\n"); // NOI18N
            } finally {
                writer.close();
            }

            if (execEnv.isLocal()) {
                return localHostInfo.getTempDir() + '/' + dummyFile.getName();
            }

            HostInfo remoteHostInfo = HostInfoUtils.getHostInfo(execEnv);
            String remoteDummyPath = remoteHostInfo.getTempDir() + '/' + dummyFile.getName();
            CommonTasksSupport.uploadFile(dummyFile, execEnv, remoteDummyPath, 0644, null).get();
            dummyFile.delete();
            return remoteDummyPath;

        } catch (Throwable ex) {
            dummyFile.delete();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        }
    }
}
