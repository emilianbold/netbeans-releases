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
package org.netbeans.libs.git.remote.jgit;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.remote.GitBlameResult;
import org.netbeans.libs.git.remote.GitBranch;
import org.netbeans.libs.git.remote.GitCherryPickResult;
import org.netbeans.libs.git.remote.GitConflictDescriptor;
import org.netbeans.libs.git.remote.GitConflictDescriptor.Type;
import org.netbeans.libs.git.remote.GitMergeResult;
import org.netbeans.libs.git.remote.GitPullResult;
import org.netbeans.libs.git.remote.GitPushResult;
import org.netbeans.libs.git.remote.GitRevertResult;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.remote.GitStatus;
import org.netbeans.libs.git.remote.GitStatus.GitDiffEntry;
import org.netbeans.libs.git.remote.GitStatus.Status;
import org.netbeans.libs.git.remote.GitTransportUpdate;
import org.netbeans.libs.git.remote.GitUser;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public abstract class GitClassFactory {
    
    public abstract GitBlameResult createBlameResult (VCSFileProxy file, Map<String, GitBlameResult.GitBlameContent> result, JGitRepository repository);
    
    public abstract GitBranch createBranch (String name, boolean remote, boolean active, String id);

    public abstract GitCherryPickResult createCherryPickResult (
            GitCherryPickResult.CherryPickStatus status, List<VCSFileProxy> conflicts,
            List<VCSFileProxy> failures, GitRevisionInfo head, List<GitRevisionInfo> cherryPickedCommits);

    public abstract GitConflictDescriptor createConflictDescriptor (Type type);

    public abstract GitFileInfo createFileInfo (VCSFileProxy file, String oldPath, GitFileInfo.Status status, VCSFileProxy originalFile, String originalPath);
    
    //public abstract GitMergeResult createMergeResult (MergeResult mergeResult, VCSFileProxy workTree);

    public abstract GitPullResult createPullResult (Map<String, GitTransportUpdate> fetchUpdates, GitMergeResult mergeResult);

    public abstract GitPushResult createPushResult (Map<String, GitTransportUpdate> remoteRepositoryUpdates, Map<String, GitTransportUpdate> localRepositoryUpdates);
    
    //public abstract GitRebaseResult createRebaseResult (RebaseResult rebaseResult, List<VCSFileProxy> rebaseConflicts, List<VCSFileProxy> failures, String newHead);

    //public abstract GitRemoteConfig createRemoteConfig (RemoteConfig remoteConfig);

    public abstract GitRevertResult createRevertResult (GitRevertResult.Status status, GitRevisionInfo createRevisionInfo, List<VCSFileProxy> conflicts, List<VCSFileProxy> failures);
        
    //public final GitRevisionInfo createRevisionInfo (RevCommit commit, JGitRepository repository) {
    //    return createRevisionInfo(commit, Collections.<String, GitBranch>emptyMap(), repository);
    //}
    
    //public abstract GitRevisionInfo createRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, JGitRepository repository);
    
    public final GitRevisionInfo createRevisionInfo(GitRevisionInfo.GitRevCommit status, JGitRepository repository) {
        return createRevisionInfo(status, Collections.<String, GitBranch>emptyMap(), repository);
    }

    public abstract GitRevisionInfo createRevisionInfo(GitRevisionInfo.GitRevCommit status, Map<String, GitBranch> affectedBranches, JGitRepository repository);

    public abstract GitStatus createStatus (boolean tracked, String path, String workTreePath, VCSFileProxy file, 
                Status statusHeadIndex, Status statusIndexWC, Status statusHeadWC, 
                GitConflictDescriptor conflictDescriptor, boolean folder, GitDiffEntry diffEntry,
                long indexEntryTimestamp);
    
    //public abstract GitSubmoduleStatus createSubmoduleStatus (SubmoduleStatus status, VCSFileProxy folder);

    //public abstract GitTag createTag (RevTag revTag);

    //public abstract GitTag createTag (String tagName, RevObject revObject);

    //public abstract GitTag createTag (String tagName, GitRevisionInfo revCommit);

    //public abstract GitTransportUpdate createTransportUpdate (URIish urI, TrackingRefUpdate update);

    //public abstract GitTransportUpdate createTransportUpdate (URIish urI, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches);

    public abstract GitUser createUser (String name, String mail);

    public abstract void setBranchTracking (GitBranch branch, GitBranch trackedBranch);

}
