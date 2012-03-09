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
package org.netbeans.modules.git;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka, Ondra Vrabec
 */
public class HistoryProvider implements VCSHistoryProvider {
    
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<VCSHistoryProvider.HistoryChangeListener>();
    private static final Logger LOG = Logger.getLogger(HistoryProvider.class.getName());

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
    
    @Override
    public synchronized HistoryEntry[] getHistory(File[] files, Date fromDate) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        
        Set<File> repositories = getRepositoryRoots(files);
        if(repositories == null) {
            return null;
        }
        
        List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
        Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
        Map<String, GitRevisionInfo> rev2LMMap = new HashMap<String, GitRevisionInfo>();
            
        Date toDate = null;
        if (fromDate != null) {
            toDate = new Date(System.currentTimeMillis());
        }

        for (File file : files) {
            FileInformation info = Git.getInstance().getFileStatusCache().getStatus(file);
            if (!info.containsStatus(FileInformation.STATUS_MANAGED)) {
                continue;
            }
            File repositoryRoot = repositories.iterator().next();
            GitRevisionInfo[] history;
            try {
                history = HistoryRegistry.getInstance().getLogs(repositoryRoot, files, fromDate, toDate, GitUtils.NULL_PROGRESS_MONITOR);
                for (GitRevisionInfo h : history) {
                    String r = h.getRevision();
                    rev2LMMap.put(r, h);
                    Set<File> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<File>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }    

        for (GitRevisionInfo h : rev2LMMap.values()) {
            Set<File> s = rev2FileMap.get(h.getRevision());
            File[] involvedFiles = s.toArray(new File[s.size()]);
            String username = h.getCommitter().toString();
            String author = h.getAuthor().toString();
            if (author == null || author.trim().isEmpty()) {
                author = username;
            }
            
            HistoryEntry e = new HistoryEntry(
                    involvedFiles, 
                    new Date(h.getCommitTime()),
                    h.getFullMessage(), 
                    author, 
                    username, 
                    h.getRevision(), 
                    h.getRevision().length() > 7 ? h.getRevision().substring(0, 7) : h.getRevision(), 
                    createActions(h.getRevision(), files), 
                    new RevisionProviderImpl(h.getRevision()));
            ret.add(e);
        }
        return ret.toArray(new HistoryEntry[ret.size()]);
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange (final File[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[listeners.size()]);
        }
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HistoryProvider.this, files));
                }
            }
        });
    }

    private class RevisionProviderImpl implements RevisionProvider {
        private String revision;

        public RevisionProviderImpl(String revision) {
            this.revision = revision;
        }
        
        @Override
        public void getRevisionFile (File originalFile, File revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

            try {
                FileInformation info = Git.getInstance().getFileStatusCache().getStatus(originalFile);
                if (info.containsStatus(FileInformation.Status.NEW_HEAD_INDEX) && info.getOldFile() != null) {
                    originalFile = info.getOldFile();
                }
                
                Set<File> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                File repository = repositories.iterator().next();
                File historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, true);
                if(historyFile != null) {
                    // ah! we already know the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile = historyFile;
                }
                File file = VersionsCache.getInstance().getFileRevision(originalFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                if(file != null) {
                    FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, revision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                        if(file != null) {
                            FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                        }
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
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
            if(files == null || files.length == 0) {
                return;
            }
            Set<File> repositories = getRepositoryRoots(files);
            if(repositories == null || repositories.isEmpty()) {
                return;
            }
            List<Node> nodes = new ArrayList<Node>(files.length);
            for (File f : files) {
                nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(f)));
            }
            SearchHistoryAction.openSearch(repositories.iterator().next(), files, Utils.getContextDisplayName(VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]))));
        }
        
    }

    private Action[] createActions(final String revision, final File... files) {
        return new Action[] {
        new AbstractAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, false, files);
            }

        },
        new AbstractAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, true, files);
            }
        }};
    }
    
    private void view(final String revision, final boolean showAnnotations, final File... files) {
        final File root = Git.getInstance().getRepositoryRoot(files[0]);
        new GitProgressSupport() {
            @Override
            protected void perform () {
                for (File f : files) {
                    File original = HistoryRegistry.getInstance().getHistoryFile(root, f, revision, true);
                    if (original != null) {
                        f = original;
                    }
                    try {
                        GitUtils.openInRevision(f, -1, revision, showAnnotations, getProgressMonitor());
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }.start(Git.getInstance().getRequestProcessor(), root, NbBundle.getMessage(SearchHistoryAction.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
    }
    
    private static Set<File> getRepositoryRoots(File... files) {
        Set<File> repositories = GitUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
}

