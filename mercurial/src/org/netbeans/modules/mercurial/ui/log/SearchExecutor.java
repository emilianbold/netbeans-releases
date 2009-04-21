/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.mercurial.ui.log;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.io.File;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 * Executes searches in Search History panel.
 * 
 * @author Maros Sandor
 */
class SearchExecutor implements Runnable {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // NOI18N
    
    static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  // NOI18N
    static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
    };
    
    private final SearchHistoryPanel    master;
    private Map<File, Set<File>>        workFiles;
    private Map<String,File>            pathToRoot;
    private final SearchCriteriaPanel   criteria;
    
    private int                         completedSearches;
    private boolean                     searchCanceled;

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        criteria = master.getCriteria();
        
        pathToRoot = new HashMap<String, File>(); 
        workFiles = new HashMap<File, Set<File>>();
        for (File file : master.getRoots()) {
            File root = Mercurial.getInstance().getRepositoryRoot(file);

            Set<File> set = workFiles.get(root);
            if (set == null) {
                set = new HashSet<File>(2);
                workFiles.put(root, set);
            }
            set.add(file);
        }

    }    
        
    public void run() {

        final String fromRevision = criteria.getFrom();
        final String toRevision = criteria.getTo();

        completedSearches = 0;
        for (Map.Entry<File, Set<File>> entry : workFiles.entrySet()) {
            final File root = entry.getKey();
            final Set<File> files = entry.getValue();
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
            HgProgressSupport support = new HgProgressSupport() {
                public void perform() {
                    OutputLogger logger = getLogger();
                    search(root, files, fromRevision, toRevision, this, logger);
                }
            };
            support.start(rp, root, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); // NOI18N
        }
    }

    private void search(File root, Set<File> files, String fromRevision,
            String toRevision, HgProgressSupport progressSupport, OutputLogger logger) {
        if (progressSupport.isCanceled()) {
            searchCanceled = true;
            return;
        }
        
        HgLogMessage[] messages;
        if (master.isIncomingSearch()) {
            messages = HgCommand.getIncomingMessages(root, toRevision, master.isShowMerges(), logger);
        }else if (master.isOutSearch()) {
            messages = HgCommand.getOutMessages(root, toRevision, master.isShowMerges(), logger);
        } else {
            if(!master.isShowInfo()) {
                messages = HgCommand.getLogMessagesNoFileInfo(root, files, fromRevision, toRevision, master.isShowMerges(), logger);
            } else {
                messages = HgCommand.getLogMessages(root, files, fromRevision, toRevision, master.isShowMerges(), logger);
            }
        }
        appendResults(root, messages);
    }
  
    
    /**
     * Processes search results from a single repository. 
     * 
     * @param root repository root
     * @param logMessages events in chronological order
     */ 
    private synchronized void appendResults(File root, HgLogMessage[] logMessages) {
        Map<String, String> historyPaths = new HashMap<String, String>();
        List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();
        // traverse in reverse chronological order
        for (int i = logMessages.length - 1; i >= 0; i--) {
            HgLogMessage logMessage = logMessages[i];
            String username = criteria.getUsername();
            if (username != null && logMessage.getAuthor().indexOf(username) == -1) continue;
            String msg = criteria.getCommitMessage();
            if (msg != null && logMessage.getMessage().indexOf(msg) == -1) continue;
            RepositoryRevision rev = new RepositoryRevision(logMessage, root);
            for (RepositoryRevision.Event event : rev.getEvents()) {
                if (event.getChangedPath().getAction() == 'A' && event.getChangedPath().getCopySrcPath() != null) {
                    // TBD: Need to handle Copy status
                    // http://www.selenic.com/mercurial/bts/Issue931 - should get it in HgCommand.getLogMessages()
                    String existingMapping = historyPaths.get(event.getChangedPath().getPath());
                    if (existingMapping == null) {
                        existingMapping = event.getChangedPath().getPath();
                    }
                    historyPaths.put(event.getChangedPath().getCopySrcPath(), existingMapping);
                }
                String originalFilePath = event.getChangedPath().getPath();
                for (String srcPath : historyPaths.keySet()) {
                    if ( originalFilePath.startsWith(srcPath) && 
                         (originalFilePath.length() == srcPath.length() || originalFilePath.charAt(srcPath.length()) == '/') ) 
                    {
                        originalFilePath = historyPaths.get(srcPath) + originalFilePath.substring(srcPath.length());
                        break;
                    }
                }
                File file = new File(root, originalFilePath);
                event.setFile(file);
            }
            results.add(rev);
        }                
        checkFinished(results);
    }

    private void checkFinished(final List<RepositoryRevision> results) {
        completedSearches++;
        if (workFiles.size() == completedSearches) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(results.isEmpty()) {
                        master.setResults(null);
                    } else {
                        master.setResults(results);
                    }

                }
            });
        }
    }
  
}
