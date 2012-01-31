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
package org.netbeans.modules.mercurial;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessageChangedPath;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryRegistry {
    private static HistoryRegistry instance;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry"); // NOI18N
    
    private Map<File, List<HgLogMessage>> logs = new HashMap<File, List<HgLogMessage>>();
    private Map<File, Map<String, List<HgLogMessageChangedPath>>> changesets = new HashMap<File, Map<String, List<HgLogMessageChangedPath>>>();
    
    private HistoryRegistry() {}
    
    public synchronized static HistoryRegistry getInstance() {
        if(instance == null) {
            instance = new HistoryRegistry();
        }
        return instance;
    }
    
    public HgLogMessage[] getLogs(File repository, File[] files, String fromRevision, String toRevision) {
        HgLogMessage[] history = 
                HgCommand.getLogMessages(
                    repository,
                    new HashSet(Arrays.asList(files)), 
                    fromRevision, 
                    toRevision, 
                    false, // show merges
                    false, // get files info
                    false, // get parents
                    -1,    // limit 
                    Collections.<String>emptyList(),                          // branch names
                    OutputLogger.getLogger(repository.getAbsolutePath()), // logger
                    false); // asc order
        if(history.length > 0) {
            for (File f : files) {
                logs.put(f, Arrays.asList(history));
            }
        }
        return history;
    }
    
    public synchronized File getHistoryFile(final File repository, final File originalFile, final String revision, final boolean dryTry) {
        long t = System.currentTimeMillis();
        String originalPath = HgUtils.getRelativePath(originalFile);
        try {
            final List<HgLogMessage> history = logs.get(originalFile);
            final String path = originalPath;
            Map<String, List<HgLogMessageChangedPath>> fileChangesets = changesets.get(originalFile);
            if(fileChangesets == null) {
                fileChangesets = new HashMap<String, List<HgLogMessageChangedPath>>();
                changesets.put(originalFile, fileChangesets);
            }
            final Map<String, List<HgLogMessageChangedPath>> fcs = fileChangesets;
            final String[] ret = new String[] {null};
            if(history != null) {
                HgProgressSupport support = new HgProgressSupport(NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUp"), null) { // NOI18N
                    @Override
                    protected void perform() {
                        ret[0] = getRepositoryPathIntern(history, revision, fcs, repository, originalFile, path, dryTry, this);
                    }
                };
                support.start(Mercurial.getInstance().getRequestProcessor(repository)).waitFinished();
            }
            if(ret[0] != null && !ret[0].equals(originalPath)) {
                return new File(repository, ret[0]);
            }
            return null;

        } finally { 
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " resolving historyFile for {0} took {1}", new Object[]{originalPath, System.currentTimeMillis() - t}); // NOI18N
            }
        }
    }

    private String getRepositoryPathIntern(List<HgLogMessage> history, String revision, Map<String, List<HgLogMessageChangedPath>> fileChangesets, File repository, File originalFile, String path, boolean dryTry, HgProgressSupport support) {
        int count = 0;
        Iterator<HgLogMessage> it = history.iterator();
        while(it.hasNext() && !revision.equals(it.next().getHgRevision().getChangesetId())) {
            count++;
        }
        support.getProgressHandle().switchToDeterminate(count);
        
        // XXX try dry first, might be it will lead to the in in the revision
        for (int i = 0; i < history.size() ; i ++) {
            HgLogMessage lm = history.get(i);
            String historyRevision = lm.getHgRevision().getChangesetId();
            if(historyRevision.equals(revision)) {
                break;
            }
            support.getProgressHandle().progress(NbBundle.getMessage(HistoryRegistry.class, "LBL_LookingUpAtRevision", originalFile.getName(), historyRevision), i); // NOI18N
            List<HgLogMessageChangedPath> changePaths = fileChangesets.get(historyRevision);
            if(changePaths == null && !dryTry) {
                long t1 = System.currentTimeMillis();
                HgLogMessage[] lms = 
                    HgCommand.getLogMessages(
                        repository,
                        new HashSet(Arrays.asList(originalFile)), 
                        historyRevision, 
                        historyRevision, 
                        false, // show merges
                        true, // get files info
                        false, // get parents
                        -1,    // limit 
                        Collections.<String>emptyList(),                      // branch names
                        OutputLogger.getLogger(repository.getAbsolutePath()), // logger
                        false); // asc order
                assert lms != null && lms.length == 1;
                HgLogMessageChangedPath[] cps = lms[0].getChangedPaths();
                changePaths = Arrays.asList(cps != null ? cps : new HgLogMessageChangedPath[0]);
                fileChangesets.put(historyRevision, changePaths);
                if(LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, " loading changePaths for {0} took {1}", new Object[]{historyRevision, System.currentTimeMillis() - t1}); // NOI18N
                }
            }
            if(changePaths != null) {
                for (HgLogMessageChangedPath cp : changePaths) {
                    String copy = cp.getCopySrcPath();
                    if(copy != null) {
                        if(path.equals(cp.getPath())) {
                            path = copy;
                            break;
                        }
                    }
                }
            }
        }
        // XXX check if found path exists in the revision we search for ...
        return path;
    }
}
