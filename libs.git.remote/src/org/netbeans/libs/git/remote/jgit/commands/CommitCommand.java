/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.remote.jgit.commands;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRevisionInfo;
import org.netbeans.libs.git.remote.GitRevisionInfo.GitRevCommit;
import org.netbeans.libs.git.remote.GitUser;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class CommitCommand extends GitCommand {
    public static final boolean KIT = false;
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final String message;
    private final GitUser author;
    private final GitUser commiter;
    public GitRevisionInfo revision;
    private final boolean amend;

    public CommitCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, String message, GitUser author, GitUser commiter, boolean amend, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.message = message;
        this.monitor = monitor;
        this.author = author;
        this.commiter = commiter;
        this.amend = amend;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            RepositoryState state = getRepository().getRepository().getRepositoryState();
            if (amend && !state.canAmend()) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_CannotAmend"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            }
            if (RepositoryState.MERGING.equals(state) || RepositoryState.CHERRY_PICKING.equals(state)) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_ConflictsInIndex"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            } else if ((RepositoryState.MERGING_RESOLVED.equals(state)
                    || RepositoryState.CHERRY_PICKING_RESOLVED.equals(state)) && roots.length > 0) {
                boolean fullWorkingTree = false;
                VCSFileProxy repositoryRoot = getRepository().getLocation();
                for (VCSFileProxy root : roots) {
                    if (root.equals(repositoryRoot)) {
                        fullWorkingTree = true;
                        break;
                    }
                }
                if (!fullWorkingTree) {
                    String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_PartialCommitAfterMerge"); //NOI18N
                    monitor.preparationsFailed(errorMessage);
                    throw new GitException(errorMessage);
                }
            } else if (!state.canCommit()) {
                String errorMessage = Utils.getBundle(CommitCommand.class).getString("MSG_Error_Commit_NotAllowedInCurrentState"); //NOI18N
                monitor.preparationsFailed(errorMessage);
                throw new GitException(errorMessage);
            }
        }
        return retval;
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            runKit();
        } else {
            runCLI();
        }
    }

//<editor-fold defaultstate="collapsed" desc="KIT">
    private void runKit() throws GitException {
        Repository repository = getRepository().getRepository();
        try {
            DirCache backup = repository.readDirCache();
            try {
                prepareIndex();
                org.eclipse.jgit.api.CommitCommand commit = new Git(repository).commit();
                
                if(author != null) {
                    commit.setAuthor(author.getName(), author.getEmailAddress());
                } else {
                    commit.setAuthor(new PersonIdent(repository));
                }
                if(commiter != null) {
                    commit.setCommitter(commiter.getName(), commiter.getEmailAddress());
                }
                setAuthorshipIfNeeded(repository, commit);
                
                commit.setMessage(message);
                commit.setAmend(amend);
                RevCommit rev = commit.call();
                revision = getClassFactory().createRevisionInfo(rev, getRepository());
            } finally {
                if (backup.lock()) {
                    try {
                        backup.write();
                        backup.commit();
                    } catch (IOException ex) {
                        Logger.getLogger(CommitCommand.class.getName()).log(Level.INFO, null, ex);
                    } finally {
                        backup.unlock();
                    }
                }
            }
        } catch (GitAPIException | JGitInternalException | NoWorkTreeException | IOException ex) {
            throw new GitException(ex);
        }
    }
    
    private void setAuthorshipIfNeeded (Repository repository, org.eclipse.jgit.api.CommitCommand cmd)
            throws GitException, NoWorkTreeException, IOException {
        if (amend) {
            RevCommit lastCommit = Utils.findCommit(repository, "HEAD^{commit}");
            transferTimestamp(cmd, lastCommit);
        }
        if (repository.getRepositoryState() == RepositoryState.CHERRY_PICKING_RESOLVED) {
            RevCommit lastCommit = Utils.findCommit(repository, repository.readCherryPickHead(), null);
            transferTimestamp(cmd, lastCommit);
        }
    }
    
    private void transferTimestamp (org.eclipse.jgit.api.CommitCommand commit, RevCommit lastCommit) {
        PersonIdent lastAuthor = lastCommit.getAuthorIdent();
        if (lastAuthor != null) {
            PersonIdent author = commit.getAuthor();
            commit.setAuthor(lastAuthor.getTimeZone() == null
                    ? new PersonIdent(author, lastAuthor.getWhen())
                    : new PersonIdent(author, lastAuthor.getWhen(), lastAuthor.getTimeZone()));
        }
    }
    
    private void prepareIndex () throws NoWorkTreeException, CorruptObjectException, IOException {
        Repository repository = getRepository().getRepository();
        DirCache cache = repository.lockDirCache();
        try {
            TreeWalk treeWalk = new TreeWalk(repository);
            TreeFilter filter = Utils.getExcludeExactPathsFilter(getRepository().getLocation(), roots);
            if (filter != null) {
                DirCacheEditor edit = cache.editor();
                treeWalk.setFilter(filter);
                treeWalk.setRecursive(true);
                treeWalk.reset();
                ObjectId headId = repository.resolve(Constants.HEAD);
                if (headId != null) {
                    treeWalk.addTree(new RevWalk(repository).parseTree(headId));
                } else {
                    treeWalk.addTree(new EmptyTreeIterator());
                }
                // Index
                treeWalk.addTree(new DirCacheIterator(cache));
                final int T_HEAD = 0;
                final int T_INDEX = 1;
                List<DirCacheEntry> toAdd = new LinkedList<DirCacheEntry>();
                while (treeWalk.next() && !monitor.isCanceled()) {
                    String path = treeWalk.getPathString();
                    int mHead = treeWalk.getRawMode(T_HEAD);
                    int mIndex = treeWalk.getRawMode(T_INDEX);
                    if (mHead == FileMode.MISSING.getBits() && mIndex != FileMode.MISSING.getBits()) {
                        edit.add(new DirCacheEditor.DeletePath(path));
                    } else if (mIndex == FileMode.MISSING.getBits() && mHead != FileMode.MISSING.getBits() || mHead != mIndex
                            || (mIndex != FileMode.TREE.getBits() && !treeWalk.idEqual(T_HEAD, T_INDEX))) {
                        edit.add(new DirCacheEditor.DeletePath(path));
                        DirCacheEntry e = new DirCacheEntry(path);
                        e.setFileMode(treeWalk.getFileMode(T_HEAD));
                        e.setObjectId(treeWalk.getObjectId(T_HEAD));
                        e.smudgeRacilyClean();
                        toAdd.add(e);
                    }
                }
                if (!monitor.isCanceled()) {
                    edit.finish();
                    DirCacheBuilder builder = cache.builder();
                    if (cache.getEntryCount() > 0) {
                        builder.keep(0, cache.getEntryCount());
                    }
                    for (DirCacheEntry e : toAdd) {
                        builder.add(e);
                    }
                    builder.finish();
                    builder.commit();
                }
            }
        } finally {
            cache.unlock();
        }
    }
//</editor-fold>
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "commit"); //NOI18N
        addArgument(0, "--status"); //NOI18N
        addArgument(0, "-m"); //NOI18N
        addArgument(0, message);
        if (amend) {
            addArgument(0, "--amend"); //NOI18N
        }
        if(author != null){
            addArgument(0, "--author="+author.toString());
        }
        if (commiter != null) {
            addArgument(0, "--author="+commiter.toString());
        }
        addArgument(0, "--"); //NOI18N
        addFiles(0, roots);
        addArgument(1, "log"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, "--pretty=raw"); //NOI18N
        addArgument(1, "-1"); //NOI18N
        // place holder for revision
    }

    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            GitRevCommit status = new GitRevCommit();
            runner(canceled, 0, status, new Parser() {

                @Override
                public void outputParser(String output, GitRevCommit revision) {
                    parseCommit(output, revision);
                }
            });
            if (status.revisionCode != null) {
                addArgument(1, status.revisionCode);
            } else {
                addArgument(1, "HEAD"); //NOI18N
            }
            runner(canceled, 1, status, new Parser() {

                @Override
                public void outputParser(String output, GitRevCommit revision) {
                    parseLog(output, revision);
                }
            });
            if (canceled.canceled()) {
                return;
            }
            revision = getClassFactory().createRevisionInfo(status, getRepository());
            
            //command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }
    }
    
    private void parseCommit(String output, GitRevCommit status) {
        //[master (root-commit) 68fbfb0] initial commit
        // 1 file changed, 1 insertion(+)
        // create mode 100644 testnotadd.txt
        //=========================
        //[master (root-commit) ae05df4] initial commit
        // Committer: Alexander Simon <alsimon@beta.(none)>
        //Your name and email address were configured automatically based
        //on your username and hostname. Please check that they are accurate.
        //You can suppress this message by setting them explicitly:
        //
        //    git config --global user.name "Your Name"
        //    git config --global user.email you@example.com
        //
        //After doing this, you may fix the identity used for this commit with:
        //
        //    git commit --amend --reset-author
        //
        // 1 file changed, 1 insertion(+)
        // create mode 100644 testnotadd.txt
        //System.err.println(exitStatus.output);
        for (String line : output.split("\n")) { //NOI18N
            line = line.trim();
            if (line.startsWith("[")) {
                int i = line.indexOf(' ');
                if (i > 0) {
                    status.branch = line.substring(1, i);
                }
                int j = line.indexOf(']');
                if (j > 0) {
                    String[] s = line.substring(i,j).split(" ");
                    status.revisionCode = s[s.length-1];
                }
                status.message = line.substring(j+1).trim();
                continue;
            }
            if (line.startsWith("Committer:")) {
                status.autorAndMail = line.substring(10).trim();
                continue;
            }
            if (line.startsWith("create mode")) {
                String[] s = line.substring(11).trim().split(" ");
                if (s.length == 2) {
                    status.commitedFiles.put(s[1], GitRevisionInfo.GitFileInfo.Status.ADDED);
                }
                continue;
            }
            if (line.startsWith("delete mode")) {
                String[] s = line.substring(11).trim().split(" ");
                if (s.length == 2) {
                    status.commitedFiles.put(s[1], GitRevisionInfo.GitFileInfo.Status.REMOVED);
                }
                continue;
            }
        }
    }
    
    private void parseLog(String output, GitRevCommit status) {
        //#git log --raw --pretty=raw -1 4644eabd   
        //commit 4644eabd50d2b49b1631e9bc613818b2a9b8d87f
        //tree 9b2ab9e89b019b008f10a29762f05c38b05d8cdb
        //parent 5406bff9015700d2353436360d98301aa7941b56
        //author John <john@git.com> 1423815945 +0300
        //committer John <john@git.com> 1423815945 +0300
        //
        //    second commit
        //
        //:100644 100644 dd954e7... a324cf1... M  testdir/test.txt
        //#git log --raw --pretty=raw -1 HEAD
        //commit 18d0fec24027ac226dc2c4df2b955eef2a16462a
        //tree 0e46518195860092ea185af77886c71b73823b33
        //parent bb831db6774aaa733199360dc7af6f3ce375fc20
        //author Junio C Hamano <gitster@pobox.com> 1423691643 -0800
        //committer Junio C Hamano <gitster@pobox.com> 1423691643 -0800
        //
        //    Post 2.3 cycle (batch #1)
        //    
        //    Signed-off-by: Junio C Hamano <gitster@pobox.com>
        //
        //:120000 100644 9257c74... 0fbbabb... T  RelNotes
        //#git log --raw --pretty=raw -1 HEAD^1
        //commit bb831db6774aaa733199360dc7af6f3ce375fc20
        //tree 4d4befdb8dfc6b9ddafec4550a6e44aaacd89dd9
        //parent afa3ccbf44cb47cf988c6f40ce3ddb10829a9e7b
        //parent 9c9b4f2f8b7f27f3984e80d053106d5d41cbb03b
        //author Junio C Hamano <gitster@pobox.com> 1423691059 -0800
        //committer Junio C Hamano <gitster@pobox.com> 1423691060 -0800
        //
        //    Merge branch 'ah/usage-strings'
        //    
        //    * ah/usage-strings:
        //      standardize usage info string format
        status.commitedFiles.clear();
        StringBuilder buf = new StringBuilder();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("committer")) {
                String s = line.substring(9).trim();
                int i = s.indexOf(">");
                if (i > 0) {
                    status.commiterAndMail = s.substring(0,i+1);
                    status.commiterTime = s.substring(i+1).trim();
                }
                continue;
            }
            if (line.startsWith("commit")) {
                status.revisionCode = line.substring(6).trim();
                continue;
            }
            if (line.startsWith("tree")) {
                status.treeCode = line.substring(4).trim();
                continue;
            }
            if (line.startsWith("parent")) {
                status.parents.add(line.substring(6).trim());
                continue;
            }
            if (line.startsWith("author")) {
                String s = line.substring(6).trim();
                int i = s.indexOf(">");
                if (i > 0) {
                    status.autorAndMail = s.substring(0,i+1);
                    status.autorTime = s.substring(i+1).trim();
                }
                continue;
            }
            if (line.startsWith(" ")) {
                //if (buf.length() > 0) {
                //    buf.append('\n');
                //}
                buf.append(line.trim());
                buf.append('\n');
                continue;
            }
            if (line.startsWith(":")) {
                String[] s = line.split("\\s");
                if (s.length > 2) {
                    String file = s[s.length-1];
                    String st = s[s.length-2];
                    GitRevisionInfo.GitFileInfo.Status gitSt =  GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
                    if ("A".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.ADDED;
                    } else if ("M".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.MODIFIED;
                    } else if ("R".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    } else if ("C".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.COPIED;
                    } else if ("D".equals(st)) {
                        gitSt =  GitRevisionInfo.GitFileInfo.Status.REMOVED;
                    }
                    VCSFileProxy vcsFile = VCSFileProxy.createFileProxy(getRepository().getLocation(), file);
                    status.commitedFiles.put(file, gitSt);
                }
                continue;
            }
            status.message = buf.toString();
        }
    }
    
    private void runner(ProcessUtils.Canceler canceled, int command, GitRevCommit list, Parser parser) {
        if(canceled.canceled()) {
            return;
        }
        ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), null, false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output, list);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error, list);
        }
    }
    

    private abstract class Parser {
        public abstract void outputParser(String output, GitRevCommit revision);
        public void errorParser(String error,GitRevCommit revision){
        }
    }
}
