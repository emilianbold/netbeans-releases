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
package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.ServerListImplementation;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * The cnd.remote implementation of ServerList.
 * 
 * @author gordonp
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.remote.ServerListImplementation.class)
public class RemoteServerList implements ServerListImplementation {

    private static final String CND_REMOTE = "cnd.remote"; // NOI18N
    private static final String REMOTE_SERVERS = CND_REMOTE + ".servers"; // NOI18N
    private static final char SERVER_RECORD_SEPARATOR = '|'; //NOI18N
    private static final String SERVER_LIST_SEPARATOR = ","; //NOI18N
    private static final String DEFAULT_INDEX = CND_REMOTE + ".default"; // NOI18N
    private int defaultIndex;
    private final PropertyChangeSupport pcs;
    private final ChangeSupport cs;
    private final ArrayList<RemoteServerRecord> unlisted;
    private final ArrayList<RemoteServerRecord> items = new ArrayList<RemoteServerRecord>();

    public RemoteServerList() {
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        defaultIndex = getPreferences().getInt(DEFAULT_INDEX, 0);
        pcs = new PropertyChangeSupport(this);
        cs = new ChangeSupport(this);
        unlisted = new ArrayList<RemoteServerRecord>();

        // Creates the "localhost" record and any remote records cached in remote.preferences
        addServer(ExecutionEnvironmentFactory.getLocal(), null,
                RemoteSyncFactory.getDefault(), // doesn't make a lot of sense here... but anyhow better than null
                false, RemoteServerRecord.State.ONLINE);
        if (slist != null) {
            for (String serverString : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
                // there moght be to forms:
                // 1) user@host:port
                // 2) user@host:port|DisplayName
                // 3) user@host:port|DisplayName|syncID
                // 4) user@host:port|DisplayName|syncID|x11possible|x11
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
                        RemoteUtil.LOGGER.warning("Unsupported synchronization mode \"" + syncId + "\" for " + env.toString() + //NOI18N
                                ". Switching to default one."); //NOI18N
                    }
                }
                if (env.isRemote()) {
                    RemoteServerRecord record = addServer(env, displayName, syncFactory, false, RemoteServerRecord.State.OFFLINE);
                    if (arr.length > 3) {
                        record.setX11Forwarding(Boolean.parseBoolean(arr[3]));
                    }
//                    if (arr.length > 4) {
//                        record.setX11forwardingPossible(Boolean.parseBoolean(arr[4]));
//                    }
                }
            }
        }
        defaultIndex = Math.min(defaultIndex, items.size() - 1);
        refresh();
    }

    /**
     * Get a ServerRecord pertaining to env. If needed, create the record.
     * 
     * @param env specvifies the host
     * @return A RemoteServerRecord for env
     */
    @Override
    public synchronized ServerRecord get(ExecutionEnvironment env) {

        // Search the active server list
        for (RemoteServerRecord record : items) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
        }

        // Search the unlisted servers list. These are records created by Tools->Options
        // which haven't been added yet (and won't until/unless OK is pressed in T->O).
        for (RemoteServerRecord record : unlisted) {
            if (env.equals(record.getExecutionEnvironment())) {
                return record;
            }
        }

        // Create a new unlisted record and return it
        RemoteServerRecord record = new RemoteServerRecord(env, null, RemoteSyncFactory.getDefault(), false);
        unlisted.add(record);
        return record;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("UG") // since get(ExecutionEnvironment) is synchronized
    public ServerRecord get(Project project) {
        ExecutionEnvironment execEnv = RemoteProjectSupport.getExecutionEnvironment(project);
        if( execEnv != null) {
            return get(execEnv);
        }
        return null;
    }

    @Override
    public synchronized ServerRecord getDefaultRecord() {
        return items.get(defaultIndex);
    }

    private synchronized void setDefaultIndexImpl(int defaultIndex) {
        int oldValue = this.defaultIndex;
        this.defaultIndex = defaultIndex;
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
        firePropertyChange(ServerList.PROP_DEFAULT_RECORD, oldValue, defaultIndex);
    }

    @Override
    public void setDefaultRecord(ServerRecord record) {
        assert record != null;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(record)) {
                setDefaultIndexImpl(i);
                return;
            }
        }
        CndUtils.assertTrue(false, "Can not set nonexistent record as default");
    }

    @Override
    public synchronized List<ExecutionEnvironment> getEnvironments() {
        List<ExecutionEnvironment> result = new ArrayList<ExecutionEnvironment>(items.size());
        for (RemoteServerRecord item : items) {
            result.add(item.getExecutionEnvironment());
        }
        return result;
    }

    private RemoteServerRecord addServer(ExecutionEnvironment execEnv, String displayName,
            RemoteSyncFactory syncFactory, boolean asDefault, RemoteServerRecord.State state) {
        RemoteServerRecord record = (RemoteServerRecord) addServer(execEnv, displayName, syncFactory, asDefault, false);
        record.setState(state);
        return record;
    }

    @Override
    public synchronized ServerRecord addServer(final ExecutionEnvironment execEnv, String displayName,
            RemoteSyncFactory syncFactory, boolean asDefault, boolean connect) {

        RemoteServerRecord record = null;
        if (syncFactory == null) {
            syncFactory = RemoteSyncFactory.getDefault();
        }

        // First off, check if we already have this record
        for (RemoteServerRecord r : items) {
            if (r.getExecutionEnvironment().equals(execEnv)) {
                if (asDefault) {
                    defaultIndex = items.indexOf(r);
                    getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
                }
                return r;
            }
        }

        // Now see if its unlisted (created in Tools->Options but cancelled with no OK)
        for (RemoteServerRecord r : unlisted) {
            if (r.getExecutionEnvironment().equals(execEnv)) {
                record = r;
                break;
            }
        }

        if (record == null) {
            record = new RemoteServerRecord(execEnv, displayName, syncFactory, connect);
        } else {
            record.setDeleted(false);
            record.setDisplayName(displayName);
            record.setSyncFactory(syncFactory);
            unlisted.remove(record);
        }
        ArrayList<RemoteServerRecord> oldItems = new ArrayList<RemoteServerRecord>(items);
        items.add(record);
        Collections.sort(items, RECORDS_COMPARATOR);
        if (asDefault) {
            defaultIndex = items.indexOf(record);
        }
        refresh();
        storePreferences(record);
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
        firePropertyChange(ServerList.PROP_RECORD_LIST, oldItems, new ArrayList<RemoteServerRecord>(items));
        return record;
    }

    public static void storePreferences(RemoteServerRecord record) {
        String displayName = record.getRawDisplayName();
        // Register the new server
        // TODO: Save the state as well as name. On restart, only try connecting to
        // ONLINE hosts.
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        String hostKey = ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment());
        String preferencesKey = hostKey + SERVER_RECORD_SEPARATOR +
                ((displayName == null) ? "" : displayName) + SERVER_RECORD_SEPARATOR +
                record.getSyncFactory().getID()  + SERVER_RECORD_SEPARATOR +
//                record.isX11forwardingPossible() + SERVER_RECORD_SEPARATOR +
                record.getX11Forwarding();

        if (slist == null) {
            getPreferences().put(REMOTE_SERVERS, preferencesKey);
        } else {
            StringBuilder sb = new StringBuilder(preferencesKey);
            for (String server : slist.split(SERVER_LIST_SEPARATOR)) { // NOI18N
                int sepPos = server.indexOf(SERVER_RECORD_SEPARATOR);
                String serverKey = (sepPos > 0) ? server.substring(0, sepPos) : server;
                if (!serverKey.equals(hostKey)) {
                    sb.append(SERVER_LIST_SEPARATOR);
                    sb.append(server);
                }
            }
            getPreferences().put(REMOTE_SERVERS, sb.toString());
        }
    }

    @Override
    public synchronized void set(List<ServerRecord> records, ServerRecord defaultRecord) {
        ArrayList<RemoteServerRecord> oldItems = new ArrayList<RemoteServerRecord>(items);
        RemoteUtil.LOGGER.finest("ServerList: set " + records);
        Collection<ExecutionEnvironment> removed = clear();
        for (ServerRecord rec : records) {
            addServer(rec.getExecutionEnvironment(), rec.getDisplayName(), rec.getSyncFactory(), false, false);
            removed.remove(rec.getExecutionEnvironment());
        }
        setDefaultRecord(defaultRecord);
        firePropertyChange(ServerList.PROP_RECORD_LIST, oldItems, new ArrayList<RemoteServerRecord>(items));
    }

    private Collection<ExecutionEnvironment> clear() {
        Collection<ExecutionEnvironment> removed = new ArrayList<ExecutionEnvironment>();
        for (RemoteServerRecord record : items) {
            record.setDeleted(true);
            removed.add(record.getExecutionEnvironment());
        }
        getPreferences().remove(REMOTE_SERVERS);
        unlisted.addAll(items);
        items.clear();
        return removed;
    }

    private void removeFromPreferences(ServerRecord recordToRemove) {
        StringBuilder sb = new StringBuilder();
        for (RemoteServerRecord record : items) {
            if (!recordToRemove.equals(record)) {
                sb.append(record.getDisplayName());
                sb.append(',');
            }
        }
        getPreferences().put(REMOTE_SERVERS, sb.toString());
    }

    protected void refresh() {
        cs.fireChange();
    }

    public synchronized RemoteServerRecord getLocalhostRecord() {
        return items.get(0);
    }

    //TODO: why this is here?
    //TODO: deprecate and remove
    @Override
    public boolean isValidExecutable(ExecutionEnvironment env, String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            RemoteUtil.LOGGER.warning("RemoteServerList.isValidExecutable from EDT"); // NOI18N
        }
        int exit_status = RemoteCommandSupport.run(env, "test", "-x", path); // NOI18N
        if (exit_status != 0 && !CndPathUtilitities.isPathAbsolute(path)) {
            // Validate 'path' against user's PATH.
            exit_status = RemoteCommandSupport.run(env, "test", "-x", "`which " + path + "`"); // NOI18N
        }
        return exit_status == 0;
    }

    @Override
    public synchronized Collection<? extends ServerRecord> getRecords() {
        return new ArrayList<RemoteServerRecord>(items);
    }

    // TODO: Are these still needed?
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private void firePropertyChange(String property, Object oldValue, Object newValue) {
        pcs.firePropertyChange(property, oldValue, newValue);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(RemoteServerList.class);
    }

    private static final Comparator<RemoteServerRecord> RECORDS_COMPARATOR = new Comparator<RemoteServerRecord> () {
        public int compare(RemoteServerRecord o1, RemoteServerRecord o2) {
            if (o1 == o2) {
                return 0;
            }

            // make localhosts first in the list
            boolean o1local = o1.getExecutionEnvironment().isLocal();
            boolean o2local = o2.getExecutionEnvironment().isLocal();
            if (o1local != o2local) {
                if (o1local) {
                    return -1;
                } else if (o2local) {
                    return 1;
                }
            }

            // others sort in alphabetical order
            return o1.getServerName().compareTo(o2.getServerName());
        }
    };

    public ServerRecord createServerRecord(ExecutionEnvironment env, String displayName, RemoteSyncFactory syncFactory) {
        return new RemoteServerRecord(env, displayName, syncFactory, false);
    }
}
