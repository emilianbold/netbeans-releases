/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.sendto.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 *
 */
public final class ScriptsRegistry {

    private static final Object lock = new Object();
    // cfgID => map: Script => map: env => file
    private static final HashMap<Integer, HashMap<String, HashMap<ExecutionEnvironment, String>>> cfgToMap =
            new HashMap<Integer, HashMap<String, HashMap<ExecutionEnvironment, String>>>();
    private static final AtomicReference<File> localTmpDir = new AtomicReference<File>();

    /**
     * Clean files cache for the specified
     * <code>Configuration</code>. Files that were added to a cache with this
     * <code>Configuration</code> are also deleted.
     *
     * @param cfg -
     * <code>Configuration</code> to clean the cache for
     */
    public static void invalidate(Configuration cfg) {
        synchronized (lock) {
            if (cfgToMap.containsKey(cfg.getID())) {
                HashMap<String, HashMap<ExecutionEnvironment, String>> map = cfgToMap.remove(cfg.getID());

                for (Map.Entry<String, HashMap<ExecutionEnvironment, String>> map1 : map.entrySet()) {
                    HashMap<ExecutionEnvironment, String> map2 = map1.getValue();
                    for (ExecutionEnvironment env : map2.keySet()) {
                        if (env.isLocal()) {
                            removeLocalFiles(cfg);
                        } else {
                            removeRemoteFiles(env, cfg);
                        }
                    }
                }
            }
        }
    }

    public static String getScriptFile(Configuration config, ExecutionEnvironment env, String script) {
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }

        String result;

        synchronized (lock) {
            Integer configID = config.getID();

            HashMap<String, HashMap<ExecutionEnvironment, String>> map;
            if (!cfgToMap.containsKey(configID)) {
                map = new HashMap<String, HashMap<ExecutionEnvironment, String>>();
                cfgToMap.put(configID, map);
            } else {
                map = cfgToMap.get(configID);
            }

            HashMap<ExecutionEnvironment, String> map2;
            if (!map.containsKey(script)) {
                map2 = new HashMap<ExecutionEnvironment, String>();
                map.put(script, map2);
            } else {
                map2 = map.get(script);
            }

            result = map2.get(env);

            if (result == null) {
                if (env.isLocal()) {
                    result = createLocalFile(config, script);
                } else {
                    result = createRemoteFile(env, config, script);
                }

                if (result != null) {
                    map2.put(env, result);
                }
            }
        }

        return result;
    }

    /**
     * Removes all script files for given Configuration.
     *
     * @param cfg
     */
    private static void removeLocalFiles(Configuration cfg) {
        File tmpDir = getLocalTmpDir();

        if (tmpDir == null) {
            return;
        }

        try {
            final String cfgPrefix = getFilePrefix(cfg);
            File[] files = tmpDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(cfgPrefix);
                }
            });

            for (File file : files) {
                file.delete();
            }
        } catch (Exception ex) {
        }
    }

    private static void removeRemoteFiles(ExecutionEnvironment env, Configuration cfg) {
        try {
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                return;
            }

            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            if (hostInfo == null) {
                return;
            }

            npb.setExecutable("/bin/sh"); // NOI18N
            npb.setArguments("-c", "/bin/rm " + hostInfo.getTempDir() + "/" + getFilePrefix(cfg) + "*"); // NOI18N
            ProcessUtils.execute(npb);
        } catch (Exception ex) {
        }
    }

    private static String getFilePrefix(Configuration cfg) {
        return "sendto." + Math.abs((long) cfg.getName().hashCode()) + '.'; // NOI18N
    }

    private static String createLocalFile(Configuration config, String script) {
        File tmpDir = getLocalTmpDir();

        if (tmpDir == null) {
            return null;
        }

        try {
            File file = File.createTempFile(getFilePrefix(config), "", tmpDir); // NOI18N
            file.deleteOnExit();
            BufferedWriter w = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8")); //NOI18N
            w.write(script);
            w.flush();
            w.close();
            return file.getAbsolutePath();
        } catch (Exception ex) {
        }

        return null;
    }

    private static String createRemoteFile(ExecutionEnvironment env, Configuration config, String script) {
        try {
            String tmpDir = getRemoteTmpDir(env);
            if (tmpDir == null) {
                return null;
            }

            String fname = tmpDir + '/' + getFilePrefix(config) + Math.abs((long) script.hashCode());
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
            npb.setExecutable("/bin/sh"); // NOI18N
            npb.setArguments("-c", "/bin/cat > " + fname); // NOI18N
            ProcessUtils.execute(npb, script.getBytes(ProcessUtils.getRemoteCharSet()));
            return fname;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private static File getLocalTmpDir() {
        File tmpDir = localTmpDir.get();

        if (tmpDir == null) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());
                if (hostInfo == null) {
                    return null;
                }
                tmpDir = hostInfo.getTempDirFile();
                localTmpDir.compareAndSet(null, tmpDir);
            } catch (Exception ex) {
            }
        }

        return tmpDir;
    }

    private static String getRemoteTmpDir(ExecutionEnvironment env) {
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            if (hostInfo != null) {
                return hostInfo.getTempDir();
            }
        } catch (Exception ex) {
        }

        return null;
    }
}
