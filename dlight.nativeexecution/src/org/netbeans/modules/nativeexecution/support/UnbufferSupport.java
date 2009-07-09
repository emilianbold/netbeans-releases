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
package org.netbeans.modules.nativeexecution.support;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/*
 * Used to unbuffer application's output in case OutputWindow is used.
 * 
 */
public class UnbufferSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final HashMap<ExecutionEnvironment, String> cache =
            new HashMap<ExecutionEnvironment, String>();

    public static void initUnbuffer(final NativeProcessInfo info, final MacroMap env) throws IOException {
        // Setup LD_PRELOAD to load unbuffer library...
        if (!info.isUnbuffer()) {
            return;
        }

        final ExecutionEnvironment execEnv = info.getExecutionEnvironment();
        final HostInfo hinfo = HostInfoUtils.getHostInfo(execEnv);

        boolean isWindows = hinfo.getOSFamily() == HostInfo.OSFamily.WINDOWS;
        boolean isMacOS = hinfo.getOSFamily() == HostInfo.OSFamily.MACOSX;

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
            InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
            File file = fl.locate(unbufferPath + "/" + unbufferLib, null, false); // NOI18N

            log.fine("Look for unbuffer library here: " + unbufferPath + "/" + unbufferLib); // NOI18N

            if (file != null && file.exists()) {
                if (execEnv.isRemote()) {
                    String remotePath = null;

                    synchronized (cache) {
                        remotePath = cache.get(execEnv);

                        if (remotePath == null) {
                            remotePath = hinfo.getTempDir() + "/" + unbufferPath; // NOI18N
                            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                            npb.setExecutable("/bin/mkdir").setArguments("-p", remotePath, remotePath + "_64"); // NOI18N

                            try {
                                npb.call().waitFor();
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            try {
                                String remoteLib_32 = remotePath + "/" + unbufferLib; // NOI18N
                                String remoteLib_64 = remotePath + "_64/" + unbufferLib; // NOI18N

                                if (!HostInfoUtils.fileExists(execEnv, remoteLib_32)) { // NOI18N
                                    String fullLocalPath = file.getParentFile().getAbsolutePath(); // NOI18N
                                    Future<Integer> copyTask;
                                    copyTask = CommonTasksSupport.uploadFile(fullLocalPath + "/" + unbufferLib, execEnv, remoteLib_32, 0755, null); // NOI18N
                                    copyTask.get();
                                    copyTask = CommonTasksSupport.uploadFile(fullLocalPath + "_64/" + unbufferLib, execEnv, remoteLib_64, 0755, null); // NOI18N
                                    copyTask.get();
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            cache.put(execEnv, remotePath);
                        }
                    }

                    unbufferPath = remotePath;
                } else {
                    unbufferPath = new File(file.getParent()).getAbsolutePath();
                }

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

                if (isWindows) {
                    // TODO: FIXME (?) For Mac and Windows just put unbuffer
                    // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                    // Reason: no luck to make it work using PATH ;(
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ";")) + // NOI18N
                            new File(unbufferPath, unbufferLib).getAbsolutePath(); // NOI18N

                    ldPreload = WindowsSupport.getInstance().convertToAllShellPaths(ldPreload);
                } else if (isMacOS) {
                    // TODO: FIXME (?) For Mac and Windows just put unbuffer
                    // with path to it to LD_PRELOAD/DYLD_INSERT_LIBRARIES
                    // Reason: no luck to make it work using PATH ;(
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                            unbufferPath + "/" + unbufferLib; // NOI18N
                } else {
                    ldPreload = ((ldPreload == null) ? "" : (ldPreload + ":")) + // NOI18N
                            unbufferLib;
                }

                env.put(ldPreloadEnv, ldPreload);

                if (isMacOS) {
                    env.put("DYLD_FORCE_FLAT_NAMESPACE", "yes"); // NOI18N
                } else if (isWindows) {
//                    String ldLibPath = env.get(ldLibraryPathEnv);
//                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ";")) + // NOI18N
//                            unbufferPath + ";" + unbufferPath + "_64"; // NOI18N
//                    ldLibPath = CommandLineHelper.getInstance(execEnv).toShellPaths(ldLibPath);
//                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                } else {
                    String ldLibPath = env.get(ldLibraryPathEnv);
                    ldLibPath = ((ldLibPath == null) ? "" : (ldLibPath + ":")) + // NOI18N
                            unbufferPath + ":" + unbufferPath + "_64"; // NOI18N
                    env.put(ldLibraryPathEnv, ldLibPath); // NOI18N
                }
            }
        }
    }
}
