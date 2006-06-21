/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.tigris.subversion.svnclientadapter.*;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.io.File;
import org.netbeans.modules.subversion.client.ExceptionHandler;

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
    
    private final SearchHistoryPanel    master;
    private Map<SVNUrl, Set<File>>      workFiles;
    private Map<String,File>            pathToRoot;
    private final SearchCriteriaPanel   criteria;
    private boolean                     filterUsername;
    private boolean                     filterMessage;
    
    private int                         completedSearches;
    private boolean                     searchCanceled;
    private List<LogInformation.Revision> results = new ArrayList<LogInformation.Revision>();

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        criteria = master.getCriteria();
        filterUsername = criteria.getUsername() != null;
        filterMessage = criteria.getCommitMessage() != null;
        
        pathToRoot = new HashMap<String, File>(); 
        if (searchingUrl()) {
            String rootPath = SvnUtils.getRepositoryPath(master.getRoots()[0]);
            pathToRoot.put(rootPath, master.getRoots()[0]); 
        } else {
            workFiles = new HashMap<SVNUrl, Set<File>>();
            for (File file : master.getRoots()) {
                String rootPath = SvnUtils.getRepositoryPath(file);
                pathToRoot.put(rootPath, file);
                SVNUrl rootUrl = SvnUtils.getRepositoryRootUrl(file);
                Set<File> set = workFiles.get(rootUrl);
                if (set == null) {
                    set = new HashSet<File>(2);
                    workFiles.put(rootUrl, set);
                }
                set.add(file);
            }
        }
    }

    private SVNRevision toRevision(String s, SVNRevision def) {
        Date date = parseDate(s);
        if (date != null) {
            return new SVNRevision.DateSpec(date);
        } else if (s != null) {
            if ("BASE".equals(s)) { // NOI18N
                return SVNRevision.BASE;
            } else if ("HEAD".equals(s)) { // NOI18N
                return SVNRevision.HEAD;
            } else {
                return new SVNRevision.Number(Long.parseLong(s));
            }
        } else {
            return def;
        }
    }
    
    public void run() {
        String from = criteria.getFrom();
        String to = criteria.getTo();
        
        final SVNRevision fromRevision = toRevision(from, new SVNRevision.Number(1));
        final SVNRevision toRevision = toRevision(to, SVNRevision.HEAD);

        completedSearches = 0;
        if (searchingUrl()) {
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(master.getRepositoryUrl());
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    search(master.getRepositoryUrl(), null, fromRevision, toRevision, this);
                }
            };
            support.start(rp, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); // NOI18N
        } else {
            for (Iterator i = workFiles.keySet().iterator(); i.hasNext();) {
                final SVNUrl url = (SVNUrl) i.next();
                final Set<File> files = workFiles.get(url);  
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
                SvnProgressSupport support = new SvnProgressSupport() {
                    public void perform() {                    
                        search(url, files, fromRevision, toRevision, this);
                    }
                };
                support.start(rp, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); // NOI18N
            }
        }
    }

    private void search(SVNUrl url, Set<File> files, SVNRevision fromRevision, SVNRevision toRevision, SvnProgressSupport progressSupport) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(url, progressSupport);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return;
        }
        if (progressSupport.isCanceled()) {
            searchCanceled = true;
            return;
        }
        
        if (searchingUrl()) {
            try {
                ISVNLogMessage [] messages = client.getLogMessages(url, null, fromRevision, toRevision, false, true, 0);
                appendResults(url, messages);
            } catch (SVNClientException e) {
                ExceptionHandler eh = new ExceptionHandler(e);
                eh.annotate();
            }
        } else {
            String [] paths = new String[files.size()];
            int idx = 0;
            for (File file : files) {
                paths[idx++] = SvnUtils.getRelativePath(url, file);
            }
            try {
                ISVNLogMessage [] messages = client.getLogMessages(url, paths, fromRevision, toRevision, false, true);
                appendResults(url, messages);
            } catch (SVNClientException e) {
                ExceptionHandler eh = new ExceptionHandler(e);
                eh.annotate();
            }
        }
    }
    
    private Map<SVNUrl, LogInformation> urlToLoginfo = new HashMap<SVNUrl, LogInformation>(); 
    
    private synchronized void appendResults(SVNUrl url, ISVNLogMessage[] logMessages) {
        for (int i = 0; i < logMessages.length; i++) {
            ISVNLogMessage logMessage = logMessages[i];
            if (filterUsername && !criteria.getUsername().equals(logMessage.getAuthor())) continue;
            if (filterMessage && logMessage.getMessage().indexOf(criteria.getCommitMessage()) == -1) continue;
            ISVNLogMessageChangePath [] paths = logMessage.getChangedPaths();
            for (int j = 0; j < paths.length; j++) {
                ISVNLogMessageChangePath path = paths[j];
                SVNUrl fileUrl = url.appendPath(path.getPath());
                LogInformation logInfo = urlToLoginfo.get(fileUrl);
                if (logInfo == null) {
                    File file = computeFile(path.getPath());
                    if (file == null) continue;
                    file = FileUtil.normalizeFile(file);
                    if (!underSearchRoots(file)) continue;
                    logInfo = new LogInformation();
                    logInfo.setRepositoryFilename(fileUrl.toString());
                    logInfo.setFile(file);
                    urlToLoginfo.put(fileUrl, logInfo);
                }
                LogInformation.Revision rev = logInfo.new Revision();
                rev.setNumber(logMessage.getRevision().toString());
                rev.setAuthor(logMessage.getAuthor());
                rev.setDate(logMessage.getDate());
                rev.setMessage(logMessage.getMessage());
                results.add(rev);
            }
        }
        checkFinished();
    }

    private boolean searchingUrl() {
        return master.getRepositoryUrl() != null;
    }
    
    private boolean underSearchRoots(File file) {
        if (searchingUrl()) return true;
        for (File root : master.getRoots()) {
            if (SvnUtils.isParentOrEqual(root, file)) return true;
        }
        return false;
    }

    private File computeFile(String path) {
        for (String s : pathToRoot.keySet()) {
            if (path.startsWith(s)) {
                return new File(pathToRoot.get(s), path.substring(s.length()));
            }
        }
        return null;
    }

    private void checkFinished() {
        completedSearches++;
        if (searchingUrl() && completedSearches >= 1 || workFiles.size() == completedSearches) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    master.setResults(results);
                }
            });
        }
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
