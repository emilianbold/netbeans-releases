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
    private Map<SVNUrl, File>           urlToRoot;
    private final SearchCriteriaPanel   criteria;
    private boolean                     filterUsername;
    private boolean                     filterMessage;
    
    private int                         completedSearches;
    private boolean                     searchCanceled;
    private List<LogInformation.Revision> results = new ArrayList<LogInformation.Revision>();

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        File [] roots = master.getRoots();
        criteria = master.getCriteria();
        filterUsername = criteria.getUsername() != null;
        filterMessage = criteria.getCommitMessage() != null;
        
        workFiles = new HashMap<SVNUrl, Set<File>>();
        urlToRoot = new HashMap<SVNUrl, File>();
        for (File file : roots) {
            SVNUrl url = SvnUtils.getRepositoryUrl(file);           // svn://localhost/sbs/src/sbs/Main.java
            SVNUrl rootUrl = SvnUtils.getRepositoryRootUrl(file);   // svn://localhost
            String urlPath = SVNUrlUtils.getRelativePath(rootUrl, url, true);         // /sbs/src/sbs/Main.java
            String rootPath = file.getAbsolutePath();
            rootPath = rootPath.substring(0, rootPath.length() - urlPath.length());
            urlToRoot.put(rootUrl, new File(rootPath));
            Set<File> set = workFiles.get(rootUrl);
            if (set == null) {
                set = new HashSet<File>(2);
                workFiles.put(rootUrl, set);
            }
            set.add(file);
        }
    }

    private SVNRevision toRevision(String s, SVNRevision def) {
        Date date = parseDate(s);
        if (date != null) {
            return new SVNRevision.DateSpec(date);
        } else if (s != null) {
            if ("BASE".equals(s)) {
                return SVNRevision.BASE;
            } else if ("HEAD".equals(s)) {
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
        for (Iterator i = workFiles.keySet().iterator(); i.hasNext();) {
            final SVNUrl url = (SVNUrl) i.next();
            final Set<File> files = workFiles.get(url);  
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    search(url, files, fromRevision, toRevision, this);
                }
            };
            support.start(rp, "Searching History...");
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
        String [] paths = new String[files.size()];
        int idx = 0;
        for (File file : files) {
            paths[idx++] = SvnUtils.getRelativePath(url, file);
        }
        try {
            ISVNLogMessage [] messages = client.getLogMessages(url, paths, fromRevision, toRevision, false, true);
            appendResults(url, messages);
        } catch (SVNClientException e) {
            ErrorManager.getDefault().notify(e);
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
                    File file = FileUtil.normalizeFile(computeFile(url, path.getPath()));
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

    private boolean underSearchRoots(File file) {
        for (File root : master.getRoots()) {
            if (SvnUtils.isParentOrEqual(root, file)) return true;
        }
        return false;
    }

    private File computeFile(SVNUrl url, String path) {
        File rootFile = urlToRoot.get(url);
        return new File(rootFile, path);
    }

    private void checkFinished() {
        completedSearches++;
        if (workFiles.size() == completedSearches) {
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
