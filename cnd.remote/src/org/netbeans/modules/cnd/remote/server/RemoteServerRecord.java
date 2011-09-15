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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The definition of a remote server and login. 
 * 
 * @author gordonp
 */
public class RemoteServerRecord implements ServerRecord {

    public static enum State {
        UNINITIALIZED, INITIALIZING, ONLINE, OFFLINE, CANCELLED;
    }
    
    public static final String PROP_STATE_CHANGED = "stateChanged"; // NOI18N

    private final ExecutionEnvironment executionEnvironment;
    private final boolean editable;
    private boolean deleted;
    private State state;
    private final Object stateLock;
    private String reason;
    private String problems;
    private String displayName;
    private RemoteSyncFactory syncFactory;
    private boolean x11forwarding;
//    private boolean x11forwardingPossible;

    HostInfo.OSFamily cachedOsFamily = null;
    HostInfo.CpuFamily cachedCpuFamily = null;
    String cachedOsVersion = null;
    
    /**
     * Create a new ServerRecord. This is always called from RemoteServerList.get, but can be
     * in the AWT Event thread if called while adding a node from ToolsPanel, or in a different
     * thread if called during startup from cached information.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    /*package-local*/ RemoteServerRecord(final ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory, boolean connect) {
        CndUtils.assertTrue(env != null);
        CndUtils.assertTrue(syncFactory != null);
        this.executionEnvironment = env;
        this.syncFactory = syncFactory;
        stateLock = new String("RemoteServerRecord state lock for " + toString()); // NOI18N
        reason = null;
        deleted = false;
        this.displayName = escape(displayName);
        
        if (env.isLocal()) {
            editable = false;
            state = State.ONLINE;
        } else {
            editable = true;
            state = connect ? State.UNINITIALIZED : State.OFFLINE;
        }
        x11forwarding = Boolean.getBoolean("cnd.remote.X11"); //NOI18N;
//        x11forwardingPossible = true;
        
        checkHostInfo(); // is this a paranoya?
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }

    @Override
    public String toString() {
        return executionEnvironment.toString();
    }

    @Override
    public synchronized void validate(final boolean force) {
        if (isOnline()) {
            return;
        }
        RemoteUtil.LOGGER.log(Level.FINE, "RSR.validate2: Validating {0}", toString());
        if (force) {
            ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(RemoteServerRecord.class, "PBAR_ConnectingTo", getDisplayName())); // NOI18N
            ph.start();
            init(null);
            ph.finish();
        }
        String msg;
        if (isOnline()) {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_OK", getDisplayName());// NOI18N
        } else {
            msg = NbBundle.getMessage(RemoteServerRecord.class, "Validation_ERR", getDisplayName(), getStateAsText(), getReason());// NOI18N
        }
        StatusDisplayer.getDefault().setStatusText(msg);        
    }

    @Override
    public void checkSetupAfterConnection(Runnable task) {
        if (!isOnline()) {
            resetOfflineState();
            init(null);
            if (isOnline()) {
                if (task != null) {
                    task.run();
                }
            }
        }
    }

    
    /**
     * Start the initialization process. This should <b>never</b> be done from the AWT Event
     * thread. Parts of the initialization use this thread and will block.
     */
    public synchronized void init(PropertyChangeSupport pcs) {
        assert !SwingUtilities.isEventDispatchThread() : "RemoteServer initialization must be done out of EDT"; // NOI18N
        try {
            ConnectionManager.getInstance().connectTo(executionEnvironment);
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, "Error connecting to " + executionEnvironment, ex);
            state = State.OFFLINE;
            reason = ex.getMessage();
            return;
        } catch (CancellationException ex) {
            state = State.CANCELLED;
            return;
        }
        Object ostate = state;
        state = State.INITIALIZING;
        RemoteServerSetup rss = new RemoteServerSetup(getExecutionEnvironment());
        if (!Boolean.getBoolean("cnd.remote.skip.setup")) {
            if (rss.needsSetupOrUpdate()) {
                rss.setup();
            }
        }
//        if (ostate == State.UNINITIALIZED) {
//            checkX11Forwarding();
//        }
        boolean initPathMap = false;

        synchronized (stateLock) {
            if (rss.isCancelled()) {
                state = State.CANCELLED;
            } else if (rss.isFailed()) {
                state = State.OFFLINE;
                reason = rss.getReason();
            } else {
                initPathMap = true;
                state = State.ONLINE;
                if (rss.hasProblems()) {
                    problems = rss.getReason();
                }
            }
        }
        if (initPathMap) {
            RemotePathMap.getPathMap(getExecutionEnvironment()).init();
        }
        if (pcs != null) {
            pcs.firePropertyChange(RemoteServerRecord.PROP_STATE_CHANGED, ostate, state);
        }
    }

//    private void checkX11Forwarding() {
//        X11ForwardingChecker x11checker = new X11ForwardingChecker(executionEnvironment);
//        try {
//            x11forwardingPossible = x11checker.check();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } catch (CancellationException ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public String getProblems() {
        return problems;
    }
    
    public boolean hasProblems() {
        return problems != null;
    }
    
    public boolean resetOfflineState() {
        synchronized (stateLock) {
            if (this.state != State.INITIALIZING && state != State.ONLINE) {
                state = State.UNINITIALIZED;
                return true;
            }
        }
        return false;
    }
    
    public String getStateAsText() {
        // TODO: not good to use object's toString as resource key
        return NbBundle.getMessage(RemoteServerRecord.class, state.toString());
    }

    @Override
    public boolean isOnline() {
        if (state == State.ONLINE && !ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            state = State.OFFLINE;
        }
        return state == State.ONLINE;
    }

    @Override
    public boolean isOffline() {
        if (state == State.ONLINE && !ConnectionManager.getInstance().isConnectedTo(executionEnvironment)) {
            state = State.OFFLINE;
        }
        return state == State.OFFLINE;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
    
    public boolean isEditable() {
        return editable;
    }

    @Override
    public boolean isRemote() {
        return executionEnvironment.isRemote();
    }

    @Override
    public String getDisplayName() {
        return (displayName != null && displayName.length() > 0) ? displayName : executionEnvironment.getDisplayName();
    }

    /* package-local */ String getRawDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteServerRecord other = (RemoteServerRecord) obj;
        if (this.executionEnvironment != other.executionEnvironment && (this.executionEnvironment == null || !this.executionEnvironment.equals(other.executionEnvironment))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.executionEnvironment != null ? this.executionEnvironment.hashCode() : 0);
        return hash;
    }

    @Override
    public String getServerDisplayName() {
        if (displayName == null || displayName.length() == 0) {
            // TODO: should we add ExecutionEnvironment.getHostDisplayName() ?
            if (executionEnvironment.isLocal()) {
                return "localhost"; //NOI18N
            } else {
                return executionEnvironment.getHost();
            }
        } else {
            return displayName;
        }
    }

    public void setDisplayName(String displayName) {
        this.displayName = escape(displayName);
    }

    // #164242 Remote gets in trouble in the case user uses "," in host display name
    private String escape(String text) {
        if (text != null) {
            text = text.replace('|', '_'); //NOI18N
            text = text.replace(',', '_'); //NOI18N
        }
        return text;
    }

    @Override
    public String getServerName() {
        return executionEnvironment.getHost();
    }

    @Override
    public String getUserName() {
        return executionEnvironment.getUser();
    }

    @Override
    public boolean isRememberPassword() {
        return PasswordManager.getInstance().isRememberPassword(executionEnvironment);
    }

    public void setRememberPassword(boolean rememberPassword) {
        PasswordManager.getInstance().setRememberPassword(executionEnvironment, rememberPassword);
    }
    
    public String getReason() {
        return reason == null ? "" : reason;
    }

    @Override
    public RemoteSyncFactory getSyncFactory() {
        return this.syncFactory;
    }

    public void setSyncFactory(RemoteSyncFactory factory) {
        this.syncFactory = factory;
    }

    /*package*/void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean isSetUp() {
        final Collection<? extends HostSetupProvider> providers = Lookup.getDefault().lookupAll(HostSetupProvider.class);
        for (HostSetupProvider provider : providers) {
            if (provider.canCheckSetup(executionEnvironment)) {
                return provider.isSetUp(executionEnvironment);
            }
        }
        return true;
    }

    @Override
    public boolean setUp() {
        final Collection<? extends HostSetupProvider> providers = Lookup.getDefault().lookupAll(HostSetupProvider.class);
        for (HostSetupProvider provider : providers) {
            if (provider.canCheckSetup(executionEnvironment)) {
                return provider.setUp(executionEnvironment);
            }
        }
        return true;
    }

    public boolean getX11Forwarding() {
        return x11forwarding;
    }

    public void setX11Forwarding(boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

//    public boolean isX11forwardingPossible() {
//        return x11forwardingPossible;
//    }
//
//    public void setX11forwardingPossible(boolean x11forwardingPossible) {
//        this.x11forwardingPossible = x11forwardingPossible;
//    }

    private static final char SERVER_RECORD_SEPARATOR = '|'; //NOI18N
    private static final String SERVER_LIST_SEPARATOR = ","; //NOI18N

    /*package-local*/ static List<RemoteServerRecord> fromString(String slist) {
        List<RemoteServerRecord> result = new ArrayList<RemoteServerRecord>();

        for (String serverString : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
            // there moght be to forms:
            // 1) user@host:port
            // 2) user@host:port|DisplayName
            // 3) user@host:port|DisplayName|syncID
            // 4) user@host:port|DisplayName|syncID|x11possible|x11
            if (serverString.trim().isEmpty()) {
                continue;
            }
            String displayName = null;
            RemoteSyncFactory syncFactory = RemoteSyncFactory.getDefault();
            final String[] arr = serverString.split("\\" + SERVER_RECORD_SEPARATOR); // NOI18N
            CndUtils.assertTrue(arr.length > 0);
            String hostKey = arr[0];
            if (arr.length > 1) {
                displayName = arr[1];
            }
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(hostKey);
            if (arr.length > 2) {
                final String syncId = arr[2];
                syncFactory = RemoteSyncFactory.fromID(syncId);
                if (syncFactory == null) {
                    syncFactory = RemoteSyncFactory.getDefault();
                    RemoteUtil.LOGGER.log(Level.WARNING, "Unsupported synchronization mode \"{0}\" for {1}. Switching to default one.", new Object[]{syncId, env.toString()}); //NOI18N
                }
            }
            if (env.isRemote()) {
                RemoteServerRecord record = new RemoteServerRecord(env, displayName, syncFactory, false);
                record.setState(RemoteServerRecord.State.OFFLINE);
                result.add(record);
                if (arr.length > 3) {
                    record.setX11Forwarding(Boolean.parseBoolean(arr[3]));
                }
                if (arr.length > 4) {
                    if (arr[4].length() > 0) {
                        try {
                            record.cachedOsFamily = HostInfo.OSFamily.valueOf(arr[4]);
                        } catch (IllegalArgumentException ex) {
                            RemoteUtil.LOGGER.log(Level.WARNING, "Error restoring OS family", ex);
                        }
                    }
                }
                if (arr.length > 5) {
                    if (arr[5].length() > 0) {
                        try {
                            record.cachedCpuFamily = HostInfo.CpuFamily.valueOf(arr[5]);
                        } catch (IllegalArgumentException ex) {
                            RemoteUtil.LOGGER.log(Level.WARNING, "Error restoring CPU family", ex);
                        }
                    }
                }
                if (arr.length > 6) {
                    if (arr[5].length() > 0) {
                        record.cachedOsVersion = arr[6];
                    }
                }
            }
        }

        return result;
    }

    /*package-local*/ static String toString(List<RemoteServerRecord> records) {
        StringBuilder result = new StringBuilder();
        for (RemoteServerRecord record : records) {
            if (result.length() > 0) {
                result.append(SERVER_LIST_SEPARATOR);
            }
            String displayName = record.getRawDisplayName();
            String hostKey = ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment());

            HostInfo.CpuFamily cpuFamily = record.getCpuFamily();
            HostInfo.OSFamily osFamily = record.getOsFamily();
            String osVersion = record.getOsVersion();

            String preferencesKey = hostKey + SERVER_RECORD_SEPARATOR +
                    ((displayName == null) ? "" : displayName) + SERVER_RECORD_SEPARATOR +
                    record.getSyncFactory().getID()  + SERVER_RECORD_SEPARATOR +
                    record.getX11Forwarding() + SERVER_RECORD_SEPARATOR +
                    ((osFamily == null) ? "" : osFamily.name()) + SERVER_RECORD_SEPARATOR +
                    ((cpuFamily == null) ? "" : cpuFamily.name()) + SERVER_RECORD_SEPARATOR +
                    ((osVersion == null) ? "" : osVersion);

            result.append(preferencesKey);

        }
        return result.toString();
    }

    public String getOsVersion() {
        return cachedOsVersion;
    }
    
    public HostInfo.CpuFamily getCpuFamily() {
        return cachedCpuFamily;
    }

    public HostInfo.OSFamily getOsFamily() {
        return cachedOsFamily;
    }

    /*package-local*/ final void checkHostInfo() {
        if (HostInfoUtils.isHostInfoAvailable(executionEnvironment)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                HostInfo.OSFamily osFamily = hostInfo.getOSFamily();
                HostInfo.CpuFamily cpuFamily = hostInfo.getCpuFamily();
                String osVersion = hostInfo.getOS().getVersion();
                if (!osFamily.equals(cachedOsFamily) || !cpuFamily.equals(cachedCpuFamily) || !osVersion.equals(cachedOsVersion)) {
                    cachedOsFamily = osFamily;
                    cachedCpuFamily = cpuFamily;
                    cachedOsVersion = osVersion;
                    if (executionEnvironment.isRemote() && !syncFactory.isApplicable(executionEnvironment)) {
                        for (RemoteSyncFactory newFactory : RemoteSyncFactory.getFactories()) {
                            if (newFactory.isApplicable(executionEnvironment)) {
                                RemoteUtil.LOGGER.log(Level.WARNING, "Inapplicable factory for {0} : {1}; changing to {2}",
                                        new Object[] { executionEnvironment.getDisplayName(), syncFactory.getDisplayName(), newFactory.getDisplayName() });
                                syncFactory = newFactory;
                                break;
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                // don't report
            } catch (CancellationException ex) {
                // don't report
            }
        }

    }
}
