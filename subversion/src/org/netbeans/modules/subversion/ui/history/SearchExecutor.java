/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.tigris.subversion.svnclientadapter.*;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.*;
import java.io.File;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;

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
    private Map<SVNUrl, Set<File>>      workFiles;
    private Map<String,File>            pathToRoot;
    private final SearchCriteriaPanel   criteria;
    private boolean                     filterUsername;
    private boolean                     filterMessage;
    
    private int                         completedSearches;
    private boolean                     searchCanceled;
    private List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        criteria = master.getCriteria();
        filterUsername = criteria.getUsername() != null;
        filterMessage = criteria.getCommitMessage() != null;
        
        pathToRoot = new HashMap<String, File>(); 
        try {
            if (searchingUrl()) {
                String rootPath = SvnUtils.getRepositoryPath(master.getRoots()[0]);
                pathToRoot.put(rootPath, master.getRoots()[0]); 
            } else {
                workFiles = new HashMap<SVNUrl, Set<File>>();
                for (File file : master.getRoots()) {
                    String rootPath = SvnUtils.getRepositoryPath(file);
                    String fileAbsPath = file.getAbsolutePath().replace(File.separatorChar, '/');
                    int commonPathLength = getCommonPostfixLength(rootPath, fileAbsPath);                
                    pathToRoot.put(rootPath.substring(0, rootPath.length() - commonPathLength), 
                                   new File(fileAbsPath.substring(0, fileAbsPath.length() - commonPathLength)));
                    SVNUrl rootUrl = SvnUtils.getRepositoryRootUrl(file);
                    Set<File> set = workFiles.get(rootUrl);
                    if (set == null) {
                        set = new HashSet<File>(2);
                        workFiles.put(rootUrl, set);
                    }
                    set.add(file);
                }
            }                
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
    }
        
    private int getCommonPostfixLength(String a, String b) {        
        int ai = a.length() - 1;        
        int bi = b.length() - 1;
        int slash = -1;
        for (;;) {
            if (ai < 0 || bi < 0) break;         
            char ca = a.charAt(ai);
            char cb = b.charAt(bi);
            if(ca == '/') slash = ai;
            if ( ca != cb ) {
                if(slash > -1) {
                    return a.length() - slash;
                }
                break;
            }
            ai--; bi--;
        }
        return a.length() - ai - 1;
    }


    
    public void run() {

        final SVNRevision fromRevision = criteria.getFrom();
        final SVNRevision toRevision = criteria.getTo();

        completedSearches = 0;
        if (searchingUrl()) {
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(master.getRepositoryUrl());
            SvnProgressSupport support = new SvnProgressSupport() {
                public void perform() {                    
                    search(master.getRepositoryUrl(), null, fromRevision, toRevision, this);
                }
            };
            support.start(rp, master.getRepositoryUrl(), NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); // NOI18N
        } else {
            for (Iterator i = workFiles.keySet().iterator(); i.hasNext();) {
                final SVNUrl rootUrl = (SVNUrl) i.next();
                final Set<File> files = workFiles.get(rootUrl);
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(rootUrl);
                SvnProgressSupport support = new SvnProgressSupport() {
                    public void perform() {                    
                        search(rootUrl, files, fromRevision, toRevision, this);
                    }
                };
                support.start(rp, rootUrl, NbBundle.getMessage(SearchExecutor.class, "MSG_Search_Progress")); // NOI18N
            }
        }
    }

    private void search(SVNUrl rootUrl, Set<File> files, SVNRevision fromRevision, SVNRevision toRevision, SvnProgressSupport progressSupport) {
        SvnClient client;
        try {
            client = Subversion.getInstance().getClient(rootUrl, progressSupport);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if (progressSupport.isCanceled()) {
            searchCanceled = true;
            return;
        }        
        if (searchingUrl()) {
            try {
                ISVNLogMessage [] messages = client.getLogMessages(rootUrl, null, fromRevision, toRevision, false, true, 0);
                appendResults(rootUrl, messages);
            } catch (SVNClientException e) {
                if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                    progressSupport.annotate(e);
                }
            }
        } else {
            String [] paths = new String[files.size()];
            int idx = 0;
            try {            
                for (File file : files) {
                    paths[idx++] = SvnUtils.getRelativePath(rootUrl, file);
                }
                ISVNLogMessage [] messages = client.getLogMessages(rootUrl, paths, fromRevision, toRevision, false, true);
                appendResults(rootUrl, messages);
            } catch (SVNClientException e) {
                if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                    progressSupport.annotate(e);
                }
            }
        }
    }
            
  
    
    /**
     * Processes search results from a single repository. 
     * 
     * @param rootUrl repository root URL
     * @param logMessages events in chronological order
     */ 
    private synchronized void appendResults(SVNUrl rootUrl, ISVNLogMessage[] logMessages) {
        // /tags/tag-JavaAppX => /branches/brenc2-JavaAppX
        Map<String, String> historyPaths = new HashMap<String, String>();

        // traverse in reverse chronological order
        for (int i = logMessages.length - 1; i >= 0; i--) {
            ISVNLogMessage logMessage = logMessages[i];
            if (filterUsername && !criteria.getUsername().equals(logMessage.getAuthor())) continue;
            if (filterMessage && logMessage.getMessage().indexOf(criteria.getCommitMessage()) == -1) continue;
            RepositoryRevision rev = new RepositoryRevision(logMessage, rootUrl);
            for (RepositoryRevision.Event event : rev.getEvents()) {
                if (event.getChangedPath().getAction() == 'A' && event.getChangedPath().getCopySrcPath() != null) {
                    // this indicates that in this revision, the file/folder was copied to a new location
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
                File file = computeFile(originalFilePath);
                event.setFile(file);
            }
            results.add(rev);
        }
        checkFinished();
    }

    private boolean searchingUrl() {
        return master.getRepositoryUrl() != null;
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

  
}
