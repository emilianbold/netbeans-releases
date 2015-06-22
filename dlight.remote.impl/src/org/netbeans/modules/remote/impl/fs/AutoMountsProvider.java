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
package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;

/**
 * @author vkvashin
 */
public class AutoMountsProvider {

    private final ExecutionEnvironment env;
    private final TreeSet<String> autoMounts;

    public AutoMountsProvider(ExecutionEnvironment env) {
        this.env = env;
        autoMounts = new TreeSet<>();
    }

    public boolean analyze() {
        try {
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                return false;
            }
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            switch (hostInfo.getOSFamily()) {
                case SUNOS:
                    return analyzeSolarisAutoMounts();
                case LINUX:
                    return analyzeLinuxAutoMounts();
                case WINDOWS:
                case MACOSX:
                case UNKNOWN:
                default:
                    return false;
            }
        } catch (IOException | ConnectionManager.CancellationException | InterruptedException | ExecutionException ex) {
            RemoteLogger.fine(ex);
        }
        return false;
    }

    public List<String> getAutoMounts() {
        List<String> res = new ArrayList<>();
        res.addAll(autoMounts);
        return res;
    }

    private List<String> readFile(String path) throws IOException, InterruptedException, ExecutionException {
        File dstFile = File.createTempFile(path.length() < 3 ? path + "___" : path, ".tmp"); //NOI18N
        try {
            Future<Integer> task = CommonTasksSupport.downloadFile(path, env, dstFile, null);
            Integer rc = task.get();
            if (rc != 0) {
                throw new IOException("Error reading file " + path + " rc=" + rc); //NOI18N
            }
            List<String> result = new ArrayList<>();
            try (BufferedReader rdr = new BufferedReader(new FileReader(dstFile))) {
                String line;
                while ((line = rdr.readLine()) != null) {
                    result.add(line);
                }
            }
            if (Boolean.getBoolean("remote.dump.automounts")) {
                StringBuilder sb = new StringBuilder("AutoMounts analyzer: the content of "); //NOI18N
                sb.append(env).append(':').append(path).append(" [comments filtered out]:"); // NOI18N
                for (String l : result) {
                    if (!l.startsWith("#")) { //NOI18N
                        sb.append('\n').append(l);
                    }
                }
                System.out.println(sb);
            }
            return result;
        } finally {
            dstFile.delete();
        }
    }

    private boolean analyzeLinuxAutoMounts() throws IOException, InterruptedException, ExecutionException {
        List<String> lines = readFile("/etc/auto.master"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("/")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 0) {
                    String path = words[0];
                    if (!path.equals("/-")) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        return true;
    }

    private boolean analyzeSolarisAutoMounts() throws IOException, InterruptedException, ExecutionException {
        List<String> lines = readFile("/etc/auto_master"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("/")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 0) {
                    String path = words[0];
                    if (!path.equals("/-")) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        lines = readFile("/etc/mnttab"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("auto_")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 1) {
                    String path = words[1];
                    if (!path.equals("/-") && !containsParent(path)) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        return true;
    }

    private boolean containsParent(String path) {
        for (String parent = PathUtilities.getDirName(path);
                parent != null && !parent.isEmpty();
                parent = PathUtilities.getDirName(parent)) {
            if (autoMounts.contains(parent)) {
                return true;
            }
        }
        return false;
    }
}
