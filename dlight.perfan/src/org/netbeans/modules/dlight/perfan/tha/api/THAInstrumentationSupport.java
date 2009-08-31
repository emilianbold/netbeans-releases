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
package org.netbeans.modules.dlight.perfan.tha.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class THAInstrumentationSupport {

    private final static CollectVersion minSupportedVersion = CollectVersion.getCollectVersion("6.7"); // NOI18N
    private final static ConcurrentHashMap<SSLocation, THAInstrumentationSupport> hash = new ConcurrentHashMap<SSLocation, THAInstrumentationSupport>();
    private final ExecutionEnvironment execEnv;
    private final Future<CollectVersion> version;
    private final String collectCMD;
    private final String binDir;

    public static THAInstrumentationSupport getSupport(ExecutionEnvironment execEnv, String sunstudioBinDir) {
        SSLocation location = new SSLocation(execEnv, sunstudioBinDir);
        THAInstrumentationSupport support = hash.get(location);

        if (support != null) {
            return support;
        }

        support = new THAInstrumentationSupport(execEnv, sunstudioBinDir);
        THAInstrumentationSupport existentSupport = hash.putIfAbsent(location, support);

        if (existentSupport != null) {
            return existentSupport;
        }

        return support;
    }

    private THAInstrumentationSupport(final ExecutionEnvironment execEnv, final String sunstudioBinDir) {
        this.binDir = sunstudioBinDir;
        this.execEnv = execEnv;
        collectCMD = sunstudioBinDir + "collect"; // NOI18N
        version = getVersion();
    }

    public boolean isSupported() {
        boolean result = false;
        try {
            CollectVersion vers = version.get();
            return vers.compareTo(minSupportedVersion) > 0;
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
        }

        return result;
    }

    public boolean isInstrumentationNeeded(ExecutionEnvironment env){
        try {
            if (HostInfoUtils.getHostInfo(env).getCpuFamily().equals(CpuFamily.SPARC)) {
                return false;
            }
            return true;
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }
        return true;
    }

    public Future<Boolean> isInstrumented(final String executable) {
        return new FutureTask<Boolean>(new Callable<Boolean>() {

            public Boolean call() throws Exception {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                npb.setExecutable(collectCMD).setArguments("-n", "-r races", executable); // NOI18N
                Process p = npb.call();
                List<String> out = ProcessUtils.readProcessOutput(p);
                int result = p.waitFor();

                if (result == 0) {
                    String identifier = loc("THA_InstrumentedIdentifier"); // NOI18N
                    for (String s : out) {
                        if (s.contains(identifier)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    private Future<CollectVersion> getVersion() {
        FutureTask<CollectVersion> getBinDir = new FutureTask<CollectVersion>(new Callable<CollectVersion>() {

            public CollectVersion call() throws Exception {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
                npb.setExecutable(binDir + "version").setArguments(collectCMD); // NOI18N
                NativeProcess np = npb.call();
                List<String> output = ProcessUtils.readProcessOutput(np);
                int result = np.waitFor();
                if (result == 0) {
                    return CollectVersion.getCollectVersion(output.get(0));
                }

                return CollectVersion.UNKNOWN;
            }
        });

        getBinDir.run();

        return getBinDir;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(THAInstrumentationSupport.class, key, params);
    }

    public String getLinkerOptions() {
        return "-xinstrument=datarace"; // NOI18N
    }

    public String getCompilerOptions() {
        return "-xinstrument=datarace"; // NOI18N
    }

    private static class CollectVersion implements Comparable<CollectVersion> {

        public static final CollectVersion UNKNOWN = new CollectVersion(0, 0);
        private final int major;
        private final int minor;

        public static CollectVersion getCollectVersion(String versionString) {
            Pattern versionPattern = Pattern.compile("version [^:]+: Sun Analyzer ([1-9]+)\\.([1-9]+).*"); // NOI18N
            Matcher m = versionPattern.matcher(versionString);
            if (m.matches()) {
                return new CollectVersion(Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)));
            } else {
                versionPattern = Pattern.compile("version [^:]+: Sun .*Analyzer ([1-9]+)\\.([1-9]+).*"); // NOI18N
                m = versionPattern.matcher(versionString);
                if (m.matches()) {
                    return new CollectVersion(Integer.parseInt(m.group(1)),
                            Integer.parseInt(m.group(2)));
                } else {
                    versionPattern = Pattern.compile("([1-9]+)\\.([1-9]+).*"); // NOI18N
                    m = versionPattern.matcher(versionString);
                    if (m.matches()) {
                        return new CollectVersion(Integer.parseInt(m.group(1)),
                                Integer.parseInt(m.group(2)));
                    }
                }
            }

            return UNKNOWN;
        }

        private CollectVersion(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        public int compareTo(CollectVersion other) {
            if (major == other.major) {
                return Integer.valueOf(minor).compareTo(other.minor);
            } else {
                return Integer.valueOf(major).compareTo(other.major);
            }
        }
    }

    private final static class SSLocation {

        private final ExecutionEnvironment execEnv;
        private final String binDir;

        public SSLocation(ExecutionEnvironment execEnv, String binDir) {
            this.execEnv = execEnv;
            this.binDir = binDir;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SSLocation)) {
                return false;
            }
            SSLocation other = (SSLocation) obj;

            return execEnv.equals(other.execEnv) && binDir.equals(other.binDir);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (this.execEnv != null ? this.execEnv.hashCode() : 0);
            hash = 23 * hash + (this.binDir != null ? this.binDir.hashCode() : 0);
            return hash;
        }
    }
}
