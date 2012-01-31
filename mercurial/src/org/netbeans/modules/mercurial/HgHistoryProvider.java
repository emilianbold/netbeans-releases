/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.log.LogAction;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class HgHistoryProvider implements VCSHistoryProvider {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<VCSHistoryProvider.HistoryChangeListener>();

    @Override
    public void addHistoryChangeListener(VCSHistoryProvider.HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeHistoryChangeListener(VCSHistoryProvider.HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    
    
    private Map<File, List<HgLogMessage>> paths = new HashMap<File, List<HgLogMessage>>();
    @Override
    public synchronized HistoryEntry[] getHistory(File[] files, Date fromDate) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        
        if(!isClientAvailable()) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable");
            return null;
        }
        
        Set<File> repositories = getRepositoryRoots(files);
        if(repositories == null) {
            return null;
        }
        
        List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
        Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
        Map<String, HgLogMessage> rev2LMMap = new HashMap<String, HgLogMessage>();
            
        String fromRevision;
        String toRevision;
        if(fromDate == null) {
            fromRevision = "0";
            toRevision = "BASE";
        } else {
            fromRevision = dateFormat.format(fromDate);
            toRevision = dateFormat.format(new Date(System.currentTimeMillis()));
        }

        for (File file : files) {
            FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(file);
            int status = info.getStatus();
            if ((status & FileInformation.STATUS_VERSIONED) == 0) {
                continue;
            }
            File repositoryRoot = repositories.iterator().next();
            HgLogMessage[] history = HistoryRegistry.getInstance().getLogs(repositoryRoot, files, fromRevision, toRevision);
            for (HgLogMessage h : history) {
                String r = h.getHgRevision().getRevisionNumber();
                rev2LMMap.put(r, h);
                Set<File> s = rev2FileMap.get(r);
                if(s == null) {
                    s = new HashSet<File>();
                    rev2FileMap.put(r, s);
                }
                s.add(file);
            }
        }    

        for(HgLogMessage h : rev2LMMap.values()) {
            Set<File> s = rev2FileMap.get(h.getHgRevision().getRevisionNumber());
            File[] involvedFiles = s.toArray(new File[s.size()]);
            String username = h.getUsername();
            String author = h.getAuthor();
            if(username == null || "".equals(username.trim())) {
                username = author;
            }
            
            HistoryEntry e = new HistoryEntry(
                    involvedFiles, 
                    h.getDate(), 
                    h.getMessage(), 
                    author, 
                    username, 
                    h.getHgRevision().getRevisionNumber() + ":" + h.getHgRevision().getChangesetId(), 
                    h.getHgRevision().getRevisionNumber(), 
                    createActions(h.getHgRevision(), files), 
                    new RevisionProviderImpl(h.getHgRevision()));
            ret.add(e);
        }
        return ret.toArray(new HistoryEntry[ret.size()]);
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange(final File[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[listeners.size()]);
        }
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HgHistoryProvider.this, files));
                }
            }
        });
    }

    private class RevisionProviderImpl implements RevisionProvider {
        private HgRevision hgRevision;

        public RevisionProviderImpl(HgRevision hgRevision) {
            this.hgRevision = hgRevision;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
            
            if(!isClientAvailable()) {
                Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable");
                return;
            }

            try {
                FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(originalFile);
                if (info != null && (info.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0
                        && info.getStatus(null) != null && info.getStatus(null).getOriginalFile() != null) 
                {
                    originalFile = info.getStatus(null).getOriginalFile();
                }
                
                Set<File> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    Mercurial.LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                File repository = repositories.iterator().next();
                File historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), true);
                if(historyFile != null) {
                    // ah! we already now the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile= historyFile;
                }
                File file = VersionsCache.getInstance().getFileRevision(originalFile, hgRevision, false);
                if(file != null) {
                    FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    Mercurial.LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, hgRevision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, hgRevision, false);
                        if(file != null) {
                            FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                        }
                    }
                }
            } catch (IOException e) {
                if(e.getCause() instanceof HgException.HgCommandCanceledException)  {
                    Mercurial.LOG.log(Level.FINE, null, e);
                } else {
                    Mercurial.LOG.log(Level.WARNING, null, e);
                }
            }        
        }
    }

    private static class OpenHistoryAction extends AbstractAction {
        private final File[] files;

        public OpenHistoryAction(File[] files) {
            this.files = files;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            openHistory(files);
        }
        private void openHistory(File[] files) {
            if(!isClientAvailable()) {
                org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable"); // NOI18N
                return;
            }

            if(files == null || files.length == 0) {
                return;
            }
            Set<File> repositories = getRepositoryRoots(files);
            if(repositories == null) {
                return;
            }
            LogAction.openHistory(repositories.iterator().next(), files);
        }
        
    }

    private Action[] createActions(final HgRevision revision, final File... files) {
        return new Action[] {new AbstractAction(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_RollbackTo", "" + revision.getRevisionNumber())) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                final File root = Mercurial.getInstance().getRepositoryRoot(files[0]);
                RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                HgProgressSupport support = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        RevertModificationsAction.performRevert(
                            root,   
                            revision.getRevisionNumber(),                           
                            files, 
                            HgModuleConfig.getDefault().getBackupOnRevertModifications(), 
                            false, 
                            this.getLogger());
                    }
                };
                support.start(rp, root, NbBundle.getMessage(LogAction.class, "MSG_Revert_Progress")); // NOI18N
            }    
        }, 
        new AbstractAction(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_View")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, false, files);
            }

        },
        new AbstractAction(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, true, files);
            }
        }};
    }
    
    private void view(final HgRevision revision, final boolean showAnnotations, final File... files) {
        final File root = Mercurial.getInstance().getRepositoryRoot(files[0]);
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        rp.post(new Runnable() {
            @Override
            public void run() {
                for (File f : files) {
                    try {
                        HgUtils.openInRevision(f, -1, revision, showAnnotations);
                    } catch (IOException ex) {
                        // Ignore if file not available in cache
                    }
                }
            }
        });
    }
    
    /**
     * Returns true if mercurial client is installed and has a supported version.<br/>
     * Does not show any warning dialog.
     * @return true if mercurial client is available.
     */
    private static boolean isClientAvailable() {
        return isClientAvailable(false);
    }

    private static boolean isClientAvailable (boolean notifyUI) {
        return org.netbeans.modules.mercurial.Mercurial.getInstance().isAvailable(true, notifyUI);
    }

    private static Set<File> getRepositoryRoots(File... files) {
        Set<File> repositories = HgUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
}

