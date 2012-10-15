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
package org.netbeans.modules.nativeexecution.support.hostinfo.impl;

import com.jcraft.jsch.JSchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.JschSupport;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.pty.NbStartUtility;
import org.netbeans.modules.nativeexecution.support.EnvReader;
import org.netbeans.modules.nativeexecution.support.InstalledFileLocatorProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 100)
public class UnixHostInfoProvider implements HostInfoProvider {

    private static final String PATH_VAR = "PATH"; // NOI18N
    private static final String PATH_TO_PREPEND = "/bin:/usr/bin"; // NOI18N
    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final File hostinfoScript;

    static {
        InstalledFileLocator fl = InstalledFileLocatorProvider.getDefault();
        hostinfoScript = fl.locate("bin/nativeexecution/hostinfo.sh", "org.netbeans.modules.dlight.nativeexecution", false); // NOI18N

        if (hostinfoScript == null) {
            log.severe("Unable to find hostinfo.sh script!"); // NOI18N
        }
    }

    @Override
    public HostInfo getHostInfo(ExecutionEnvironment execEnv) throws IOException {
        if (hostinfoScript == null) {
            return null;
        }

        boolean isLocal = execEnv.isLocal();

        if (isLocal && Utilities.isWindows()) {
            return null;
        }

        final Properties info = execEnv.isLocal()
                ? getLocalHostInfo()
                : getRemoteHostInfo(execEnv);

        final Map<String, String> environment = new HashMap<String, String>();

        HostInfo result = HostInfoFactory.newHostInfo(execEnv, info, environment);

        if (execEnv.isLocal()) {
            getLocalUserEnvironment(result, environment);
        } else {
            getRemoteUserEnvironment(execEnv, result, environment);
        }

        // Add /bin:/usr/bin
        String path = PATH_TO_PREPEND;

        if (environment.containsKey(PATH_VAR)) {
            path += ":" + environment.get(PATH_VAR); // NOI18N
        }

        environment.put(PATH_VAR, path); // NOI18N

        return result;
    }

    private Properties getLocalHostInfo() throws IOException {
        Properties hostInfo = new Properties();

        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", // NOI18N
                    hostinfoScript.getAbsolutePath());

            File tmpDirFile = new File(System.getProperty("java.io.tmpdir")); // NOI18N
            String tmpDirBase = tmpDirFile.getCanonicalPath();

            pb.environment().put("TMPBASE", tmpDirBase); // NOI18N
            pb.environment().put("NB_KEY", HostInfoFactory.getNBKey()); // NOI18N

            Process hostinfoProcess = pb.start();

            // In case of some error goes to stderr, waitFor() will not exit
            // until error stream is read/closed.
            // So this case sould be handled.

            // We safely can do this in the same thread (in this exact case)

            List<String> errorLines = ProcessUtils.readProcessError(hostinfoProcess);
            int result = hostinfoProcess.waitFor();

            for (String errLine : errorLines) {
                log.log(Level.WARNING, "UnixHostInfoProvider: {0}", errLine); // NOI18N
            }

            if (result != 0) {
                throw new IOException(hostinfoScript + " rc == " + result); // NOI18N
            }

            fillProperties(hostInfo, hostinfoProcess.getInputStream());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("HostInfo receiving for localhost interrupted " + ex); // NOI18N
        }

        return hostInfo;
    }

    private Properties getRemoteHostInfo(ExecutionEnvironment execEnv) throws IOException {
        Properties hostInfo = new Properties();
        ChannelStreams sh_channels = null;

        try {
            sh_channels = JschSupport.startCommand(execEnv, "/bin/sh -s", null); // NOI18N

            long localStartTime = System.currentTimeMillis();

            OutputStream out = sh_channels.in;
            InputStream err = sh_channels.err;
            InputStream in = sh_channels.out;

            // echannel.setEnv() didn't work, so writing this directly
            out.write(("NB_KEY=" + HostInfoFactory.getNBKey() + '\n').getBytes()); // NOI18N
            out.flush();

            BufferedReader scriptReader = new BufferedReader(new FileReader(hostinfoScript));
            String scriptLine = scriptReader.readLine();

            while (scriptLine != null) {
                out.write((scriptLine + '\n').getBytes());
                out.flush();
                scriptLine = scriptReader.readLine();
            }

            scriptReader.close();

            BufferedReader errReader = new BufferedReader(new InputStreamReader(err));
            String errLine;
            while ((errLine = errReader.readLine()) != null) {
                log.log(Level.WARNING, "UnixHostInfoProvider: {0}", errLine); // NOI18N
            }

            fillProperties(hostInfo, in);

            long localEndTime = System.currentTimeMillis();

            hostInfo.put("LOCALTIME", Long.valueOf((localStartTime + localEndTime) / 2)); // NOI18N
        } catch (JSchException ex) {
            throw new IOException("Exception while receiving HostInfo for " + execEnv.toString() + ": " + ex); // NOI18N
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (sh_channels != null) {
                if (sh_channels.channel != null) {
                    try {
                        ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(execEnv, sh_channels.channel);
                    } catch (JSchException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        return hostInfo;
    }

    private void fillProperties(Properties hostInfo, InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String s;
            while ((s = br.readLine()) != null) {
                String[] data = s.split("=", 2); // NOI18N
                if (data.length == 2) {
                    hostInfo.put(data[0], data[1]);
                }
            }
        } catch (IOException ex) {
        }
    }

    private void getRemoteUserEnvironment(ExecutionEnvironment execEnv, HostInfo hostInfo, Map<String, String> environmentToFill) {
        // If NbStartUtility is available for target host will invoke it for
        // dumping environment to a file ...
        // 
        // The only thing - we cannot use builders at this point, so
        // need to do everything here... 

        String nbstart = null;

        try {
            nbstart = NbStartUtility.getInstance().getPath(execEnv, hostInfo);
        } catch (IOException ex) {
            log.log(Level.WARNING, "Failed to get remote path of NbStartUtility", ex); // NOI18N
            Exceptions.printStackTrace(ex);
        }

        String envPath = hostInfo.getEnvironmentFile();

        ChannelStreams login_shell_channels = null;

        try {
            login_shell_channels = JschSupport.startLoginShellSession(execEnv);
            if (nbstart != null && envPath != null) {
                login_shell_channels.in.write((nbstart + " --dumpenv " + envPath + "\n").getBytes()); // NOI18N
            }
            login_shell_channels.in.write(("/usr/bin/env || /bin/env\n").getBytes()); // NOI18N
            login_shell_channels.in.flush();
            login_shell_channels.in.close();

            EnvReader reader = new EnvReader(login_shell_channels.out, true);
            environmentToFill.putAll(reader.call());
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to get getRemoteUserEnvironment for " + execEnv.getDisplayName(), ex); // NOI18N
        } finally {
            if (login_shell_channels != null) {
                if (login_shell_channels.channel != null) {
                    try {
                        ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(execEnv, login_shell_channels.channel);
                    } catch (JSchException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    private void getLocalUserEnvironment(HostInfo hostInfo, Map<String, String> environmentToFill) {
        environmentToFill.putAll(System.getenv());
    }
}
