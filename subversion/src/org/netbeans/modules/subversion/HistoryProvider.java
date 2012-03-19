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
package org.netbeans.modules.subversion;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.*;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryProvider implements VCSHistoryProvider {

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
    
    @Override
    public HistoryEntry[] getHistory(File[] files, Date fromDate) {
        
        try {
            SvnClient client = Subversion.getInstance().getClient(files[0]);

            List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
            Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
            Map<String, ISVNLogMessage> rev2LMMap = new HashMap<String, ISVNLogMessage>();
            Map<File, SVNUrl> file2Copy = new HashMap<File, SVNUrl>();
            SVNUrl repoUrl = null;
            for (File file : files) {
                FileInformation fi = Subversion.getInstance().getStatusCache().getStatus(file);
                if ((fi.getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
                    continue;
                }
                
                ISVNLogMessage[] messages;
                if ((fi.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) &&
                     fi.getEntry(file).isCopied()) 
                {
                    ISVNInfo info = SvnUtils.getInfoFromWorkingCopy(client, file);
                    SVNUrl copyUrl = info.getCopyUrl();
                    repoUrl = info.getRepository();
                    
                    messages = 
                        client.getLogMessages(
                                copyUrl, 
                                fromDate == null ? 
                                    new SVNRevision.Number(1) : 
                                    new SVNRevision.DateSpec(fromDate),
                                    SVNRevision.HEAD
                                );
                    file2Copy.put(file, copyUrl);
                    
                } else {
                    messages = 
                        client.getLogMessages(
                                    file, 
                                    fromDate == null ? 
                                        new SVNRevision.Number(1) : 
                                        new SVNRevision.DateSpec(fromDate),
                                        SVNRevision.HEAD
                                    );
                }
                
                for (ISVNLogMessage m : messages) {
                    String r = m.getRevision().toString();
                    rev2LMMap.put(r, m);
                    Set<File> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<File>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            }

            for(ISVNLogMessage m : rev2LMMap.values()) {
                Set<File> s = rev2FileMap.get(m.getRevision().toString());
                File[] involvedFiles = s.toArray(new File[s.size()]);
                HistoryEntry e = new HistoryEntry(
                    involvedFiles, 
                    m.getDate(), 
                    m.getMessage(), 
                    m.getAuthor(), 
                    m.getAuthor(), 
                    m.getRevision().toString(), 
                    m.getRevision().toString(), 
                    createActions(m.getRevision(), files), 
                    new RevisionProviderImpl(m.getRevision(), repoUrl, file2Copy));
                ret.add(e);
                
            }

            return ret.toArray(new HistoryEntry[ret.size()]);
        } catch (SVNClientException e) {
            if (SvnClientExceptionHandler.isCancelledAction(e.getMessage())) {
                Subversion.LOG.log(Level.FINE, null, e);
            } else {
                SvnClientExceptionHandler.notifyException(e, true, true);
            }
        }
        return null;
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
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HistoryProvider.this, files));
                }
            }
        });
    }
    
    private static class RevisionProviderImpl implements RevisionProvider {
        private final SVNRevision revision;
        private final Map<File, SVNUrl> file2Copy;
        private final SVNUrl repoUrl;

        public RevisionProviderImpl(SVNRevision svnRevision, SVNUrl repoUrl, Map<File, SVNUrl> file2Copy) {
            this.revision = svnRevision;
            this.file2Copy = file2Copy;
            this.repoUrl = repoUrl;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            try {
                File file;
                SVNUrl copyUrl = repoUrl != null ? file2Copy.get(originalFile) : null;
                if(copyUrl != null) {
                    file = VersionsCache.getInstance().getFileRevision(repoUrl, copyUrl, revision.toString(), originalFile.getName());
                } else {
                    file = VersionsCache.getInstance().getFileRevision(originalFile, revision.toString());
                }
                FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
            } catch (IOException e) {
                Exception ex = e;
                if (e.getCause() != null && e.getCause() instanceof SVNClientException) {
                    ex = (SVNClientException) e.getCause();
                }
                if (SvnClientExceptionHandler.isCancelledAction(ex.getMessage())) {
                    Subversion.LOG.log(Level.FINE, null, e);
                } else {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
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

        public void openHistory(final File[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            if(!org.netbeans.modules.subversion.api.Subversion.isClientAvailable(true)) {
                org.netbeans.modules.subversion.Subversion.LOG.log(Level.WARNING, "Subversion client is unavailable");
                return;
            }

            /**
            * Open in AWT
            */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    SearchHistoryAction.openHistory(files);
                }
            });
        }
    }
    
    private Action[] createActions(final SVNRevision.Number revision, final File... files) {
        return new Action[] {new AbstractAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_RollbackTo", revision.toString())) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                SVNUrl repository;
                try {
                    repository = SvnUtils.getRepositoryRootUrl(files[0]);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                    return;
                }
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
                SvnProgressSupport support = new SvnProgressSupport() {
                    @Override
                    public void perform() {
                        try {
                            SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(files[0]);
                            for(File file : files) {
                                SvnClient client = Subversion.getInstance().getClient(false);
                                ISVNInfo info = client.getInfo(file);
                                SVNUrl fileUrl = info.getUrl();
                                SvnUtils.rollback(file, repoUrl, fileUrl, revision, false, getLogger());
                            }
                        } catch (SVNClientException ex) {
                            SvnClientExceptionHandler.notifyException(ex, false, false);
                        }
                    }
                };
                support.start(rp, repository, NbBundle.getMessage(SearchHistoryAction.class, "MSG_Rollback_Progress")); // NOI18N
            }
        }, new AbstractAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, false, files);
            }
        }, new AbstractAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                view(revision, true, files);
            }
        }};
    }
    private void view(final SVNRevision revision, final boolean showAnnotations, final File... files) {
        if(files == null || files.length == 0) {
            return;
        }
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(files[0]);
                    for (File file : files) {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        ISVNInfo info = client.getInfo(file);
                        SVNUrl fileUrl = info.getUrl();
                        SvnUtils.openInRevision(file, repoUrl, fileUrl, revision, revision, showAnnotations);
                    }
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
        });
    }

}
