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
package org.netbeans.modules.cnd.toolchain.ui.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerAccessorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

/**
 *
 * @author Sergey Grinev
 */
public final class ToolsCacheManagerImpl extends ToolsCacheManager {

    private static final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private ServerUpdateCache serverUpdateCache;
    private HashMap<ExecutionEnvironment, CompilerSetManager> copiedManagers =
            new HashMap<ExecutionEnvironment, CompilerSetManager>();

    public ToolsCacheManagerImpl(boolean initialize) {
        if (initialize) {
            for (ServerRecord record : ServerList.getRecords()) {
                CompilerSetManager csm = CompilerSetManager.get(record.getExecutionEnvironment());
                addCompilerSetManager(csm);
            }
        }
    }

    @Override
    public ServerUpdateCache getServerUpdateCache() {
        return serverUpdateCache;
    }

    @Override
    public void setHosts(Collection<? extends ServerRecord> list) {
        if (serverUpdateCache == null) {
            serverUpdateCache = new ServerUpdateCache();
        }
        serverUpdateCache.setHosts(list);
    }

    @Override
    public void setDefaultRecord(ServerRecord defaultRecord) {
        serverUpdateCache.setDefaultRecord(defaultRecord);
    }

    @Override
    public void applyChanges() {
        applyChanges(ServerList.get(ExecutionEnvironmentFactory.getLocal()));
    }
    
    @Override
    public synchronized CompilerSetManager getCompilerSetManagerCopy(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManagerImpl out = (CompilerSetManagerImpl) copiedManagers.get(env);
        if (out == null) {
            out = (CompilerSetManagerImpl) CompilerSetManagerAccessorImpl.getDeepCopy(env, initialize);
            if (out.getCompilerSets().size() == 1 && out.getCompilerSets().get(0).getName().equals(CompilerSetImpl.None)) {
                out.remove(out.getCompilerSets().get(0));
            }
            copiedManagers.put(env, out);
        }
        return out;
    }

    @Override
    public synchronized void addCompilerSetManager(CompilerSetManager newCsm) {
        copiedManagers.put(((CompilerSetManagerImpl)newCsm).getExecutionEnvironment(), newCsm);
    }

    public void applyChanges(ServerRecord selectedRecord) {
        List<ExecutionEnvironment> liveServers = null;
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

        saveCompileSetManagers(liveServers);
        fireChange(this);
    }

    public Collection<? extends ServerRecord> getHosts() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getHosts();
        }
        return ServerList.getRecords();
    }

    public ServerRecord getDefaultHostRecord() {
        if (serverUpdateCache != null) {
            return serverUpdateCache.getDefaultRecord();
        } else {
            return ServerList.getDefaultRecord();
        }
    }

    public boolean hasCache() {
        return serverUpdateCache != null;
    }

    public synchronized void clear() {
        serverUpdateCache = null;
        copiedManagers.clear();
    }

    private synchronized void saveCompileSetManagers(List<ExecutionEnvironment> liveServers) {
        Collection<CompilerSetManager> allCSMs = new ArrayList<CompilerSetManager>();
        for (ExecutionEnvironment copiedServer : copiedManagers.keySet()) {
            if (liveServers == null || liveServers.contains(copiedServer)) {
                allCSMs.add(copiedManagers.get(copiedServer));
            }
        }
        CompilerSetManagerAccessorImpl.setManagers(allCSMs, liveServers);
        copiedManagers.clear();
    }

    public static void addChangeListener(ChangeListener l) {
        synchronized (changeListeners) {
            changeListeners.add(l);
        }
    }

    public static void removeChangeListener(ChangeListener l) {
        synchronized (changeListeners) {
            changeListeners.remove(l);
        }
    }

    private static void fireChange(ToolsCacheManagerImpl manager) {
        ChangeListener[] listenersCopy;
        synchronized (changeListeners) {
            listenersCopy = new ChangeListener[changeListeners.size()];
            changeListeners.toArray(listenersCopy);
        }
        ChangeEvent ev = new ChangeEvent(manager);
        for (ChangeListener l : listenersCopy) {
            l.stateChanged(ev);
        }
    }
}
