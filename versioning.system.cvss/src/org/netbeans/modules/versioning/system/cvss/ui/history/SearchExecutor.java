/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.log.LogCommand;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.RLogExecutor;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.LogExecutor;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.net.MalformedURLException;

import org.openide.util.NbBundle;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputEvent;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;

/**
 * Executes searches in Search History panel.
 * 
 * @author Maros Sandor
 */
class SearchExecutor implements Runnable {

    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  // NOI18N
    
    private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  // NOI18N
    private static final DateFormat [] dateFormats = new DateFormat[] {
        fullDateFormat,
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        simpleDateFormat,
        new SimpleDateFormat("yyyy-MM-dd"), // NOI18N
    };
    
    /**
     * Collection of CVSRoots that do not support rXXX commands.
     */ 
    private static Set                  misconfiguredServers = Collections.synchronizedSet(new HashSet());
    
    private final SearchHistoryPanel    master;
    private File[]                      folders;
    private File[]                      files;
    private final SearchCriteriaPanel   criteria;
    
    private List                        results = new ArrayList();

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        File [] roots = master.getRoots();
        
        Set printedWarnings = new HashSet(1);
        Set foldersSet = new HashSet(roots.length); 
        Set filesSet = new HashSet(roots.length); 
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            boolean isMisconfiguredServer = false;
            try {
                String cvsRoot = Utils.getCVSRootFor(root);
                isMisconfiguredServer = misconfiguredServers.contains(cvsRoot);
                if (root.isDirectory() && isMisconfiguredServer && printedWarnings.add(cvsRoot)) {
                    showMisconfiguredServerWarning(cvsRoot);
                }
            } catch (IOException e) {
                // ignore
            }
            if (root.isFile() || isMisconfiguredServer) {
                filesSet.add(root);
            } else {
                foldersSet.add(root);
            }
        }
        files = (File[]) filesSet.toArray(new File[filesSet.size()]);
        folders = (File[]) foldersSet.toArray(new File[foldersSet.size()]);
        criteria = master.getCriteria();
    }

    public void run() {
        String from = criteria.getFrom();
        String to = criteria.getTo();
        Date fromDate = parseDate(from);
        Date toDate = parseDate(to);

        RlogCommand rcmd = new RlogCommand();
        LogCommand lcmd = new LogCommand();

        if (fromDate != null || toDate != null) {
            String dateFilter = ""; // NOI18N
            if (fromDate != null) {
                dateFilter = fullDateFormat.format(fromDate);
            }
            dateFilter += "<="; // NOI18N
            if (toDate != null) {
                dateFilter += fullDateFormat.format(toDate);
            }
            rcmd.setDateFilter(dateFilter);
            lcmd.setDateFilter(dateFilter);
        } else if (from != null || to != null) {
            String revFilter = ""; // NOI18N
            if (from != null) {
                revFilter = from;
            }
            revFilter += ":"; // NOI18N
            if (to != null) {
                revFilter += to;
            }
            rcmd.setRevisionFilter(revFilter);
            lcmd.setRevisionFilter(revFilter);
        }
        
        rcmd.setNoTags(!CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true));
        lcmd.setNoTags(!CvsModuleConfig.getDefault().getPreferences().getBoolean(CvsModuleConfig.PROP_SEARCHHISTORY_FETCHTAGS, true));
        rcmd.setUserFilter(criteria.getUsername());
        lcmd.setUserFilter(criteria.getUsername());

        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(SearchExecutor.class, "BK0001"), false);  // NOI18N
        RLogExecutor [] rexecutors;
        if (folders.length > 0) {
            rexecutors = RLogExecutor.splitCommand(rcmd, folders, null);
        } else {
            rexecutors = new RLogExecutor[0];
        }
        group.addExecutors(rexecutors);

        LogExecutor [] lexecutors;
        if (files.length > 0) {
            lcmd.setFiles(files);
            lexecutors = LogExecutor.splitCommand(lcmd, null);
        } else {
            lexecutors = new LogExecutor[0];
        }
        group.addExecutors(lexecutors);

        final RLogExecutor [] frexecutors = rexecutors;
        final LogExecutor [] flexecutors = lexecutors;
        Runnable action = new Runnable() {
            public void run() {
                List newResults = processResults(frexecutors, flexecutors);
                results.addAll(newResults);
                if (testForRLogFailures(frexecutors)) {
                    SearchExecutor.this.run();
                    return;
                }
                master.setResults(results);
            }
        };
        group.addBarrier(action);
        group.execute();

    }

    private boolean testForRLogFailures(RLogExecutor[] executors) {
        Set failedFiles = new HashSet();
        Set printedWarnings = new HashSet(1);
        for (int i = 0; i < executors.length; i++) {
            RLogExecutor executor = executors[i];
            if (executor.hasFailedOnSymbolicLink()) {
                try {
                    String cvsRoot = Utils.getCVSRootFor(executor.getFile());
                    if (printedWarnings.add(cvsRoot)) {
                        showMisconfiguredServerWarning(cvsRoot);
                    }
                    misconfiguredServers.add(cvsRoot);
                } catch (IOException e) {
                    // harmless + should never happen
                }
                failedFiles.add(executor.getFile());
            }
        }
        if (failedFiles.size() > 0) {
            files = (File[]) failedFiles.toArray(new File[failedFiles.size()]);
            folders = new File[0];
            return true;
        } else {
            return false;
        }
    }

    private void showMisconfiguredServerWarning(String cvsRoot) {
        final String relNotesUrl = "http://javacvs.netbeans.org/release/5.0"; // NOI18N
        ClientRuntime runtime = CvsVersioningSystem.getInstance().getClientRuntime(cvsRoot);
        runtime.log(NbBundle.getMessage(SearchExecutor.class, "MSG_SymlinkWarning1") + "\n", null);  // NOI18N
        runtime.log(NbBundle.getMessage(SearchExecutor.class, "MSG_SymlinkWarning2", relNotesUrl) + "\n", new OutputListener() {  // NOI18N
            public void outputLineSelected(OutputEvent ev) {
            }

            public void outputLineAction(OutputEvent ev) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(relNotesUrl));
                } catch (MalformedURLException e) {
                    // never happens
                }
            }

            public void outputLineCleared(OutputEvent ev) {
            }
        });
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(SearchExecutor.class, "MSG_StatusSymlinkWarning"));  // NOI18N
    }

    private List processResults(RLogExecutor[] rexecutors, LogExecutor[] lexecutors) {
        List log = new ArrayList(200);
        for (int i = 0; i < rexecutors.length; i++) {
            RLogExecutor executor = rexecutors[i];
            log.addAll(executor.getLogEntries());
        }
        for (int i = 0; i < lexecutors.length; i++) {
            LogExecutor executor = lexecutors[i];
            log.addAll(executor.getLogEntries());
        }
        String commitMessage = criteria.getCommitMessage();

        List newResults = new ArrayList(log.size());
        for (Iterator i = log.iterator(); i.hasNext();) {
            LogInformation info = (LogInformation) i.next();
            newResults.addAll(info.getRevisionList());
        }

        if (commitMessage != null) {
            for (Iterator i = newResults.iterator(); i.hasNext();) {
                LogInformation.Revision revision = (LogInformation.Revision) i.next();
                String msg = revision.getMessage();
                if (msg.indexOf(commitMessage) == -1) {
                    i.remove();
                }
            }
        }

        return newResults;
    }
    
    private Date parseDate(String s) {
        if (s == null) return null;
        for (int i = 0; i < dateFormats.length; i++) {
            DateFormat dateformat = dateFormats[i];
            try {
                return dateformat.parse(s);
            } catch (ParseException e) {
                // try the next one
            }
        }
        return null;
    }

}
