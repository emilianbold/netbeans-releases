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
package org.netbeans.modules.cnd.ui.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 * @author Sergey Grinev
 */
public final class ToolsCacheManager {

    private ServerUpdateCache serverUpdateCache;
    private HashMap<ExecutionEnvironment, CompilerSetManager> copiedManagers =
            new HashMap<ExecutionEnvironment, CompilerSetManager>();

    public ToolsCacheManager() {
    }

    public ServerUpdateCache getServerUpdateCache() {
        return serverUpdateCache;
    }

    public Collection<? extends ServerRecord> getHosts() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getHosts();
        } else if (isRemoteAvailable()) {
            return ServerList.getRecords();
        } else {
            return null;
        }
    }

    public ServerRecord getDefaultHostRecord() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getDefaultRecord();
        } else {
            return ServerList.getDefaultRecord();
        }
    }

    public void setHosts(Collection<? extends ServerRecord> list) {
        if (serverUpdateCache == null) {
            serverUpdateCache = new ServerUpdateCache();
        }
        serverUpdateCache.setHosts(list);
    }

    public void setDefaultRecord(ServerRecord defaultRecord) {
        serverUpdateCache.setDefaultRecord(defaultRecord);
    }

    public boolean hasCache() {
        return serverUpdateCache != null;
    }

    private void saveCompileSetManagers(List<ExecutionEnvironment> liveServers) {
        Collection<CompilerSetManager> allCSMs = new ArrayList<CompilerSetManager>();
        for (ExecutionEnvironment copiedServer : copiedManagers.keySet()) {
            if (liveServers == null || liveServers.contains(copiedServer)) {
                allCSMs.add(copiedManagers.get(copiedServer));
            }
        }
        CompilerSetManager.setDefaults(allCSMs);
        copiedManagers.clear();
    }

    public void applyChanges() {
        applyChanges(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
    }
    
    public void applyChanges(ServerRecord selectedRecord) {
        List<ExecutionEnvironment> liveServers = null;
        if (isRemoteAvailable()) {
            if (serverUpdateCache != null) {
                liveServers = new ArrayList<ExecutionEnvironment>();
                ServerList.set(serverUpdateCache.getHosts(), serverUpdateCache.getDefaultRecord());
                for (ServerRecord rec : serverUpdateCache.getHosts()) {
                    liveServers.add(rec.getExecutionEnvironment());
                }
                serverUpdateCache = null;
            } else {
                ServerList.setDefaultRecord(selectedRecord);
            }
        }

        saveCompileSetManagers(liveServers);
    }

    public void clear() {
        serverUpdateCache = null;
        copiedManagers.clear();
    }

    //TODO: we should be ensured already....check
    public void ensureHostSetup(ExecutionEnvironment env) {
        if (env != null) {
            ServerList.get(env); // this will ensure the remote host is setup
        }
    }

    public boolean isDevHostValid(ExecutionEnvironment env) {
        if (isRemoteAvailable()) {
            ServerRecord record = ServerList.get(env);
            return record != null && record.isOnline();
        } else {
            return false;
        }
    }

    public boolean isRemoteAvailable() {
        return true;
    }

    public ExecutionEnvironment getDefaultHostEnvironment() {
        return ServerList.getDefaultRecord().getExecutionEnvironment();
    }

    public synchronized CompilerSetManager getCompilerSetManagerCopy(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManager out = copiedManagers.get(env);
        if (out == null) {
            out = CompilerSetManager.getDeepCopy(env, initialize);
            if (out.getCompilerSets().size() == 1 && out.getCompilerSets().get(0).getName().equals(CompilerSet.None)) {
                out.remove(out.getCompilerSets().get(0));
            }
            copiedManagers.put(env, out);
        }
        return out;
    }

    public void addCompilerSetManager(CompilerSetManager newCsm) {
        copiedManagers.put(newCsm.getExecutionEnvironment(), newCsm);
    }
}
