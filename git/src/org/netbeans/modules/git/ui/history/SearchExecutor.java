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
package org.netbeans.modules.git.ui.history;

import java.awt.EventQueue;
import java.io.File;
import org.netbeans.libs.git.GitException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;

/**
 * Executes searches in Search History panel.
 * 
 * @author Maros Sandor
 */
class SearchExecutor extends GitProgressSupport {

    static final DateFormat [] dateFormats = new DateFormat[] {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"),  // NOI18N
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),  // NOI18N
        new SimpleDateFormat("yyyy-MM-dd HH:mm"),  // NOI18N
        new SimpleDateFormat("yyyy-MM-dd") // NOI18N
    };
    
    private final SearchHistoryPanel    master;
    private final int limitRevisions;
    private final boolean showMerges;
    private final String message;
    private final String username;
    private final String fromRevision;
    private final String toRevision;
    private final Date from;
    private final Date to;
    static final int DEFAULT_LIMIT = 10;
    static final int UNLIMITTED = -1;
    private final SearchCriteria sc;

    public SearchExecutor (SearchHistoryPanel master) {
        this.master = master;
        assert EventQueue.isDispatchThread();
        SearchCriteriaPanel criteria = master.getCriteria();
        from = parseDate(criteria.getFrom());
        fromRevision = from == null ? criteria.getFrom() : null;
        to = parseDate(criteria.getTo());
        toRevision = to == null ? criteria.getTo() : null;
        username = criteria.getUsername();
        message = criteria.getCommitMessage();
        int limit = criteria.getLimit();
        limitRevisions = limit <= 0 ? UNLIMITTED : limit;
        showMerges = criteria.isIncludeMerges();

        sc = new SearchCriteria();
        File[] files = master.getRoots();
        sc.setFiles(files);
        if (files != null && files.length == 1 && files[0].isFile()) {
            sc.setFollowRenames(true);
        }
        sc.setUsername(username);
        sc.setMessage(message);
        sc.setIncludeMerges(showMerges);
        sc.setRevisionFrom(fromRevision);
        sc.setRevisionTo(toRevision);
        sc.setFrom(from);
        sc.setTo(to);
    }    
        
    @Override
    public void perform () {
        if (isCanceled()) {
            return;
        }
        try {
            List<RepositoryRevision> results = search(limitRevisions, getClient(), getProgressMonitor());
            setResults(results);
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
    }

    List<RepositoryRevision> search (int limit, GitClient client, ProgressMonitor monitor) throws GitException {
        sc.setLimit(limit);
        List<RepositoryRevision> retval = Collections.<RepositoryRevision>emptyList();
        GitRevisionInfo[] messages = client.log(sc, monitor);
        if (!monitor.isCanceled()) {
            RepositoryInfo info = RepositoryInfo.getInstance(getRepositoryRoot());
            retval = appendResults(messages, info.getBranches().values(), info.getTags().values(), monitor);
        }
        return retval;
    }

    private List<RepositoryRevision> appendResults (GitRevisionInfo[] logMessages, Collection<GitBranch> allBranches, Collection<GitTag> allTags, ProgressMonitor monitor) {
        List<RepositoryRevision> results = new ArrayList<RepositoryRevision>();
        File dummyFile = null;
        String dummyFileRelativePath = null;
        if (master.getRoots().length == 1) {
            // dummy event must be implemented
            dummyFile = master.getRoots()[0];
            dummyFileRelativePath = GitUtils.getRelativePath(getRepositoryRoot(), dummyFile);
        }
        for (int i = 0; i < logMessages.length && !monitor.isCanceled(); ++i) {
            GitRevisionInfo logMessage = logMessages[i];
            RepositoryRevision rev;
            Set<GitBranch> branches = new HashSet<GitBranch>();
            Set<GitTag> tags = new HashSet<GitTag>();
            for (GitBranch b : allBranches) {
                if (b.getId().equals(logMessage.getRevision())) {
                    branches.add(b);
                }
            }
            for (GitTag t : allTags) {
                if (t.getTaggedObjectId().equals(logMessage.getRevision())) {
                    tags.add(t);
                }
            }
            rev = new RepositoryRevision(logMessage, master.getRepository(), master.getRoots(), tags, branches, dummyFile, dummyFileRelativePath);
            results.add(rev);
        }
        return results;
    }

    private void setResults (final List<RepositoryRevision> results) {
        final Map<String, VCSKenaiAccessor.KenaiUser> kenaiUserMap = SearchHistoryPanel.createKenaiUsersMap(results);
        EventQueue.invokeLater(new Runnable() {
        @Override
            public void run() {
                if(results.isEmpty()) {
                    master.setResults(null, kenaiUserMap, -1);
                } else {
                    master.setResults(results, kenaiUserMap, limitRevisions);
                }
            }
        });
    }

    private Date parseDate (String strDate) {
        Date date = null;
        if (strDate != null) {
            for (DateFormat fd : dateFormats) {
                try {
                    date = fd.parse(strDate);
                } catch (ParseException ex) { }
            }
        }
        return date;
    }
}
