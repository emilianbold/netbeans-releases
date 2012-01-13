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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.util.SvnSearchHistorySupport;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.util.ChangeSupport;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

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

            for (File file : files) {
                ISVNLogMessage[] messages = 
                    client.getLogMessages(
                                file, 
                                fromDate == null ? 
                                    new SVNRevision.Number(1) : 
                                    new SVNRevision.DateSpec(fromDate),
                                    SVNRevision.HEAD
                                );
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
                    createActions(m.getRevision()), 
                    new RevisionProviderImpl(m.getRevision()));
                ret.add(e);
                
            }

            return ret.toArray(new HistoryEntry[ret.size()]);
        } catch (SVNClientException e) {
            Subversion.LOG.log(Level.WARNING, null, e);
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
        private SVNRevision revision;

        public RevisionProviderImpl(SVNRevision svnRevision) {
            this.revision = svnRevision;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            try {
                File file = VersionsCache.getInstance().getFileRevision(originalFile, revision.toString());
                FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
            } catch (IOException e) {
                Subversion.LOG.log(Level.WARNING, null, e);
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

        public void openHistory(File[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            // XXX do not use searchsupport, it diff against local state instead of previous revision
            SvnSearchHistorySupport support = new SvnSearchHistorySupport(files[0]);
            try {
                support.searchHistory(-1);
            } catch (IOException ex) {
                Subversion.LOG.log(Level.WARNING, null, ex);
            }
        }
    }
    
    private Action[] createActions(SVNRevision revision) {
        return new Action[] {new AbstractAction("Revert ...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        }, new AbstractAction("Diff to previous ...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        }, new AbstractAction("Open " + revision) {
            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        }};
    }

}
