/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.localhistory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.Action;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.view.DeleteAction;
import org.netbeans.modules.localhistory.ui.view.RevertFileAction;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Tomas Stupka
 */
public class LocalHistoryProvider implements VCSHistoryProvider, VersioningListener {

    private final List<HistoryChangeListener> listeners = new LinkedList<HistoryChangeListener>();

    public LocalHistoryProvider() {
        LocalHistory.getInstance().getLocalHistoryStore().addVersioningListener(this);
    }
    
    @Override
    public void addHistoryChangeListener(HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeHistoryChangeListener(HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public HistoryEntry[] getHistory(File[] files, Date fromDate) {
        if(files == null || files.length == 0) {
            LocalHistory.LOG.log(Level.FINE, "LocalHistory requested for no files {0}", files != null ? files.length : null);
            return new HistoryEntry[0];
        }
        logFiles(files);
        
        Map<Long, HistoryEntry> storeEntries = new HashMap<Long, HistoryEntry>();
        for (File f : files) {
            StoreEntry[] ses = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntries(f);
            for(StoreEntry se : ses) {
                if(!storeEntries.keySet().contains(se.getTimestamp())) { 
                    HistoryEntry e = 
                        new HistoryEntry(
                            files, 
                            se.getDate(), 
                            se.getLabel(), 
                            "",                                                             // username         NOI18N
                            "",                                                             // username short   NOI18N 
                            NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision         NOI18N
                            NbBundle.getMessage(LocalHistoryProvider.class, "LBL_Local"),   // revision short   NOI18N
                            getActions(), 
                            new RevisionProviderImpl(se),
                            new MessageEditImpl(se));
                    storeEntries.put(se.getTimestamp(), e);
                }
            }
            
        }
        logEntries(storeEntries.values());
        return storeEntries.values().toArray(new HistoryEntry[storeEntries.size()]);
    }
    
    @Override
    public Action createShowHistoryAction(File[] files) {
        return null;
    }
    
    public void fireHistoryChange(File file) {
        HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[listeners.size()]);
        }
        for (HistoryChangeListener l : la) {
            l.fireHistoryChanged(new HistoryEvent(this, new File[] {file}));
        }
    }

    private Action[] getActions() {
        return new Action[] {
            SystemAction.get(RevertFileAction.class),
            SystemAction.get(DeleteAction.class)    
        };
    }

    @Override
    public void versioningEvent(VersioningEvent event) {
        Object[] params = event.getParams();
        if(params[0] != null) {
            fireHistoryChange((File) params[0]);
        }
    }
    
    private class RevisionProviderImpl implements VCSHistoryProvider.RevisionProvider {
        private final StoreEntry se;

        public RevisionProviderImpl(StoreEntry se) {
            this.se = se;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            assert originalFile != null;
            if(originalFile != null) {
                LocalHistory.LOG.log(Level.FINE, "revision {0} requested for null file", se.getDate().getTime()); // NOI18N
                return;
            }
            LocalHistory.LOG.log(Level.FINE, "revision {0} requested for file {1}", new Object[]{se.getDate().getTime(), originalFile.getAbsolutePath()}); // NOI18N
            try {
                // we won't use the member store entry as that might have been 
                // set for e.g. a stored .form while this is the according .java
                // file beeing requested. In case the storage can't find a revision it 
                // return next nearest in time 
                long ts = se.getTimestamp();
                StoreEntry storeEntry = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntry(originalFile, ts);
                FileUtils.copy(storeEntry.getStoreFileInputStream(), revisionFile);
                Utils.associateEncoding(originalFile, revisionFile);
            } catch (IOException e) {
                LocalHistory.LOG.log(Level.WARNING, "Error while retrieving history for file {0} stored as {1}", new Object[]{se.getFile(), se.getStoreFile()}); // NOI18N
            }
        }
    }
    
    private class MessageEditImpl implements VCSHistoryProvider.MessageEditProvider {
        private final StoreEntry se;
        public MessageEditImpl(StoreEntry se) {
            this.se = se;
        }
        @Override
        public void setMessage(String message) throws IOException {
            LocalHistory.getInstance().getLocalHistoryStore().setLabel(se.getFile(), se.getTimestamp(), message);
        }
    }

    private void logFiles(File[] files) {
        if(LocalHistory.LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("LocalHistory requested for files: "); // NOI18N
            sb.append(toString(files));
            LocalHistory.LOG.fine(sb.toString());
        }
    }

    private void logEntries(Collection<HistoryEntry> entries) {
        LocalHistory.LOG.log(Level.FINE, "LocalHistory returns {0} entries", entries.size()); // NOI18N
        if(LocalHistory.LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            Iterator<HistoryEntry> it = entries.iterator();
            while(it.hasNext()) {
                HistoryEntry entry = it.next();
                sb.append("["); // NOI18N
                sb.append(DateFormat.getDateTimeInstance().format(entry.getDateTime()));
                sb.append(",["); // NOI18N
                sb.append(toString(entry.getFiles()));
                sb.append("]]"); // NOI18N
                if(it.hasNext()) sb.append(","); // NOI18N
            }
            LocalHistory.LOG.finest(sb.toString());
        }
    }    
    
    private String toString(File[] files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i] != null ? files[i].getAbsolutePath() : "null"); // NOI18N
            if(i < files.length -1 ) sb.append(","); // NOI18N
        }
        return sb.toString();
    }
}
