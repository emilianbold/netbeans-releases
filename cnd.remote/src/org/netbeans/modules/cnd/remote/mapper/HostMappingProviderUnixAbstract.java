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
package org.netbeans.modules.cnd.remote.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.support.RunFacade;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

/**
 *
 * @author Sergey Grinev
 */
public abstract class HostMappingProviderUnixAbstract implements HostMappingProvider {
    private static final Logger log = RemoteUtil.LOGGER;

    protected abstract String getShareCommand();

    protected abstract String fetchPath(String[] values);

    @Override
    public Map<String, String> findMappings(ExecutionEnvironment execEnv, ExecutionEnvironment otherExecEnv) {
        Map<String, String> mappings = new HashMap<String, String>();
        String hostName = execEnv.isLocal() ? getLocalHostName() : execEnv.getHost();
        log.log(Level.FINE, "Find Mappings for {0}", execEnv);
        if (hostName != null) {
            RunFacade runner = RunFacade.getInstance(execEnv);
            if (runner.run(getShareCommand())) { //NOI18N
                List<String> paths = parseOutput(execEnv, new StringReader(runner.getOutput()));
                for (String path : paths) {
                    log.log(Level.FINE, "Path {0}", path);
                    assert path != null && path.length() > 0 && path.charAt(0) == '/';
                    String netPath = NET + hostName + path;
                    if (HostInfoProvider.fileExists(otherExecEnv, netPath)) {
                        if (execEnv.isLocal()) {
                            log.log(Level.FINE, "{0}->{1}", new Object[]{path, netPath});
                            mappings.put(path, netPath);
                        } else {
                            log.log(Level.FINE, "{0}->{1}", new Object[]{netPath, path});
                            mappings.put(netPath, path);
                        }
                    }
                    if (!mappings.containsKey(path) && execEnv.isLocal()) {
                        String host = getIP();
                        if (host != null && host.length()>0) {
                            log.log(Level.FINE, "IP={0}", host);
                            netPath = NET + host + path;
                            if (HostInfoProvider.fileExists(otherExecEnv, netPath)) {
                                mappings.put(path, netPath);
                                log.log(Level.FINE, "{0}->{1}", new Object[]{path, netPath});
                            }
                        }
                    }
                }
            }
        }
        return mappings;
    }

    private String getIP() {
        String host = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface nextElement = networkInterfaces.nextElement();
                if (!nextElement.isLoopback()) {
                    for (InterfaceAddress addr : nextElement.getInterfaceAddresses()) {
                        String s = addr.getAddress().getHostAddress();
                        if (s.indexOf('.') > 0 && s.indexOf('.') < 5) {
                            host = s;
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Exceptions.printStackTrace(ex);
        }
        return host;
    }

    private static final String NET = "/net/"; // NOI18N
    private static final Pattern pattern = Pattern.compile("\t+| +"); // NOI18N

    /**
     * This method parses lines like
     * -               /export1/sside   rw   "sside"
     * TODO: It assumes 2nd param is always path we want
     * @param outputReader
     * @return
     */
    private List<String> parseOutput(ExecutionEnvironment execEnv, Reader outputReader) {
        List<String> paths = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(outputReader);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String path = fetchPath(pattern.split(line));
                if (path != null && HostInfoProvider.fileExists(execEnv, path)) {
                    paths.add(path); // NOI18N
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return paths;
    }

    private static String getLocalHostName() {
        String hostName = null;
        RunFacade runner = RunFacade.getInstance(ExecutionEnvironmentFactory.getLocal());
        if (runner.run("uname -a")) { //NOI18N
            String result = runner.getOutput();
            if (result != null) {
                String[] values = result.split(" +"); // NOI18N
                if (values.length > 1) {
                    hostName = values[1];
                    //TODO: validation?
                    //TODO: add smth for Windows and move to HostInfoProv or RemoteUtils?
                    //TODO: should ExecutionEnvironment be responsible for this?
                }
            }
        }
        return hostName;
    }
}
