/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import org.netbeans.modules.subversion.FileStatusCache;
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
    
    private int                         completedSearches;
    private boolean                     searchCanceled;
    private List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();

    public SearchExecutor(SearchHistoryPanel master) {
        this.master = master;
        criteria = master.getCriteria();
    }

    private void populatePathToRoot() {
        pathToRoot = new HashMap<String, File>();
        try {
            if (searchingUrl()) {
                String rootPath = SvnUtils.getRepositoryPath(master.getRoots()[0]);
                pathToRoot.put(rootPath, master.getRoots()[0]);
            } else {
                workFiles = new HashMap<SVNUrl, Set<File>>();
                for (File file : master.getRoots()) {
                    populatePathToRoot(file);

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
    
    private void populatePathToRoot(File file) throws SVNClientException {
        
        String rootPath = SvnUtils.getRepositoryPath(file);
        String fileAbsPath = file.getAbsolutePath().replace(File.separatorChar, '/');
        int commonPathLength = getCommonPostfixLength(rootPath, fileAbsPath);
        pathToRoot.put(rootPath.substring(0, rootPath.length() - commonPathLength),
                       new File(fileAbsPath.substring(0, fileAbsPath.length() - commonPathLength)));

        File[] files = file.listFiles();
        if(files == null || files.length == 0) {
            return; 
        }
        
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for(File f : files) {
            if(SvnUtils.isAdministrative(f) || !SvnUtils.isManaged(f)) {
                continue;
            }
            int status = cache.getStatus(f).getStatus();
            if( ( f.isDirectory() && (status & SearchHistoryAction.DIRECTORY_ENABLED_STATUS) != 0)  ||                    
                (                    (status & SearchHistoryAction.FILE_ENABLED_STATUS)      != 0) )
            {
                populatePathToRoot(f);    
            }            
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
        populatePathToRoot();

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
                    String p = SvnUtils.getRelativePath(file);
                    if(p != null && p.startsWith("/")) {
                        p = p.substring(1, p.length());
                    }
                    paths[idx++] = p;
                }                
                ISVNLogMessage [] messages = SvnUtils.getLogMessages(client, rootUrl, paths, fromRevision, toRevision, false, true);
                appendResults(rootUrl, messages);
            } catch (SVNClientException e) {                
                try {    
                    // WORKAROUND issue #110034 
                    // the client.getLogMessages(rootUrl, paths[] ... seems to touch also the repository root even if it's not 
                    // listed in paths[]. This causes problems when the given user has restricted access only to a specific folder.
                    if(SvnClientExceptionHandler.isHTTP403(e.getMessage())) { // 403 forbidden
                        for(String path : paths) {                        
                            ISVNLogMessage [] messages = client.getLogMessages(rootUrl.appendPath(path), null, fromRevision, toRevision, false, true, 0);
                            appendResults(rootUrl, messages);
                        }
                        return;
                    }                      
                } catch (SVNClientException ex) {                    
                    if(!SvnClientExceptionHandler.handleLogException(rootUrl, toRevision, e)) {
                        progressSupport.annotate(ex);
                    }    
                }
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
            if(logMessage == null) continue;
            String username = criteria.getUsername();
            String msg = criteria.getCommitMessage();
            String logMsg = logMessage.getMessage();
            if (username != null && !username.equals(logMessage.getAuthor())) continue;
            if (msg != null && logMsg != null && logMsg.indexOf(msg) == -1) continue;
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
