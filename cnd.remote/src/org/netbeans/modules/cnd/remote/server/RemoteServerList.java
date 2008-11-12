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

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.ui.EditServerListDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * The cnd.remote implementation of ServerList.
 * 
 * @author gordonp
 */
public class RemoteServerList implements ServerList {
    
    private static final String CND_REMOTE = "cnd.remote"; // NOI18N
    private static final String REMOTE_SERVERS = CND_REMOTE + ".servers"; // NOI18N
    private static final String DEFAULT_INDEX = CND_REMOTE + ".default"; // NOI18N
    
    private static RemoteServerList instance = null;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    private int defaultIndex;
    private final PropertyChangeSupport pcs;
    private final ChangeSupport cs;
    private final ArrayList<RemoteServerRecord> unlisted;
    private final ArrayList<RemoteServerRecord> items = new ArrayList<RemoteServerRecord>();
    
    public synchronized static ServerList getInstance() {
        if (instance == null) {
            instance = new RemoteServerList();
        }
        return instance;
    }
    
    private RemoteServerList() {
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        defaultIndex = getPreferences().getInt(DEFAULT_INDEX, 0);
        pcs = new PropertyChangeSupport(this);
        cs = new ChangeSupport(this);
        unlisted = new ArrayList<RemoteServerRecord>();
        
        // Creates the "localhost" record and any remote records cached in remote.preferences
        addServer(CompilerSetManager.LOCALHOST, false, RemoteServerRecord.State.ONLINE);
        if (slist != null) {
            for (String hkey : slist.split(",")) { // NOI18N
                if (!CompilerSetManager.LOCALHOST.equals(hkey)) {
                    addServer(hkey, false, RemoteServerRecord.State.OFFLINE);
                }
            }
        }
        refresh();
    }

    /**
     * Get a ServerRecord pertaining to hkey. If needed, create the record.
     * 
     * @param hkey The host key (either "localhost" or "user@host")
     * @return A RemoteServerRecord for hkey
     */
    public synchronized ServerRecord get(String hkey) {

        // Search the active server list
	for (RemoteServerRecord record : items) {
            if (hkey.equals(record.getName())) {
                return record;
            }
	}
        
        // Search the unlisted servers list. These are records created by Tools->Options
        // which haven't been added yet (and won't until/unless OK is pressed in T->O).
	for (RemoteServerRecord record : unlisted) {
            if (hkey.equals(record.getName())) {
                return record;
            }
	}
        
        // Create a new unlisted record and return it
        RemoteServerRecord record = new RemoteServerRecord(hkey);
        unlisted.add(record);
        return record;
    }

    public synchronized ServerRecord getDefaultRecord() {
        return items.get(defaultIndex);
    }

    public synchronized int getDefaultIndex() {
        return defaultIndex;
    }

    public synchronized void setDefaultIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
    }
    
    public synchronized String[] getServerNames() {
        String[] sa;
        sa = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            sa[i] = items.get(i).getName();
        }
        return sa;
    }
    
    private void addServer(final String name, boolean asDefault, RemoteServerRecord.State state) {
        RemoteServerRecord addServer = (RemoteServerRecord) addServer(name, asDefault, false);
        addServer.setState(state);
    }


    public synchronized ServerRecord addServer(final String name, boolean asDefault, boolean connect) {
        RemoteServerRecord record = null;
        
        // First off, check if we already have this record
        for (RemoteServerRecord r : items) {
            if (r.getName().equals(name)) {
                if (asDefault) {
                    defaultIndex = items.indexOf(r);
                    getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
                }
                return r;
            }
        }
        
        // Now see if its unlisted (created in Tools->Options but cancelled with no OK)
        for (RemoteServerRecord r : unlisted) {
            if (r.getName().equals(name)) {
                record = r;
                break;
            }
        }
        
        if (record == null) {
            record = new RemoteServerRecord(name, connect);
        } else {
            record.setDeleted(false);
            unlisted.remove(record);
        }
        items.add(record);
        if (asDefault) {
            defaultIndex = items.size() - 1;
        }
        refresh();

        // Register the new server
        // TODO: Save the state as well as name. On restart, only try connecting to
        // ONLINE hosts.
        String slist = getPreferences().get(REMOTE_SERVERS, null);
        if (slist == null) {
            getPreferences().put(REMOTE_SERVERS, name);
        } else {
            boolean do_add = true;
            for (String server : slist.split(",")) { // NOI18N
                if (server.equals(name)) {
                    do_add = false;
                    break;
                }
            }
            if (do_add) {
                getPreferences().put(REMOTE_SERVERS, slist + ',' + name);
            }
        }
        getPreferences().putInt(DEFAULT_INDEX, defaultIndex);
        return record;
    }

    public synchronized void removeServer(int idx) {
        if (idx >= 0 && idx < items.size()) {
            RemoteServerRecord record = items.remove(idx);
            removeFromPreferences(record.getName());
            refresh();
        }
    }

    public synchronized void removeServer(ServerRecord record) {
        if (items.remove(record)) {
            removeFromPreferences(record.getName());
            refresh();
        }
    }
    
    public synchronized void clear() {
        for (RemoteServerRecord record : items) {
            record.setDeleted(true);
        }
        getPreferences().remove(REMOTE_SERVERS);
        unlisted.addAll(items);
        items.clear();
    }

    private void removeFromPreferences(String hkey) {
        StringBuilder sb = new StringBuilder();
        
        for (RemoteServerRecord record : items) {
            sb.append(record.getName());
            sb.append(',');
        }
        getPreferences().put(REMOTE_SERVERS, sb.substring(0, sb.length() - 1));
    }
    
    public ServerUpdateCache show(ServerUpdateCache serverUpdateCache) {
        EditServerListDialog dlg = new EditServerListDialog(serverUpdateCache);
        
        DialogDescriptor dd = new DialogDescriptor(dlg, NbBundle.getMessage(RemoteServerList.class, "TITLE_EditServerList"), true, 
                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
        dlg.setDialogDescriptor(dd);
        dd.addPropertyChangeListener(dlg);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            if (serverUpdateCache == null) {
                serverUpdateCache = new ServerUpdateCache();
            }
            serverUpdateCache.setDefaultIndex(dlg.getDefaultIndex());
            serverUpdateCache.setHostKeyList(dlg.getHostKeyList());
            return serverUpdateCache;
        } else {
            return null;
        }
    }
    
    protected void refresh() {
        cs.fireChange();
    }
    
    public synchronized boolean contains(String hkey) {
        for (RemoteServerRecord record : items) {
            if (hkey.equals(record.getName())) {
                return true;
            }
        }
        return false;
    }

    public synchronized RemoteServerRecord getLocalhostRecord() {
        return items.get(0);
    }

    //TODO: why this is here?
    public boolean isValidExecutable(String hkey, String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            log.warning("RemoteServerList.isValidExecutable from EDT"); // NOI18N
        }
        String cmd = "test -x " + path; // NOI18N
        int exit_status = RemoteCommandSupport.run(hkey, cmd);
        if (exit_status != 0 && !IpeUtils.isPathAbsolute(path)) {
            // Validate 'path' against user's PATH.
            cmd = "test -x " + "`which " + path + "`"; // NOI18N
            exit_status = RemoteCommandSupport.run(hkey, cmd);
        }
        return exit_status == 0;
    }
    
    public synchronized Collection<? extends ServerRecord> getRecords() {
        return Collections.unmodifiableCollection(items);
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
    
    public void firePropertyChange(String property, Object n) {
        pcs.firePropertyChange(property, null, n);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(RemoteServerList.class);
    }

    
}
