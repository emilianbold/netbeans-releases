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

package org.netbeans.modules.mercurial.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.mercurial.HgModuleConfig;


/**
 *
 * @author jrice
 */
public class HgCommand {
    private static final String HG_COMMAND = "hg";  // NOI18N
    private static final String HG_STATUS_CMD = "status";  // NOI18N // need -A to see ignored files, specified in .hgignore, see man hgignore for details
    private static final String HG_OPT_REPOSITORY = "--repository"; // NOI18N
    private static final String HG_OPT_BUNDLE = "--bundle"; // NOI18N
    private static final String HG_OPT_CWD_CMD = "--cwd"; // NOI18N
    private static final String HG_STATUS_FLAG_ALL_CMD = "-marduicC"; // NOI18N
    private static final String HG_FLAG_REV_CMD = "--rev"; // NOI18N
    private static final String HG_STATUS_FLAG_TIP_CMD = "tip"; // NOI18N
    private static final String HG_STATUS_FLAG_REM_DEL_CMD = "-rd"; // NOI18N
    private static final String HG_STATUS_FLAG_INCLUDE_CMD = "-I"; // NOI18N
    private static final String HG_STATUS_FLAG_INCLUDE_GLOB_CMD = "glob:"; // NOI18N
    private static final String HG_STATUS_FLAG_INCLUDE_END_CMD = "*"; // NOI18N
    private static final String HG_STATUS_FLAG_INTERESTING_CMD = "-mardui"; // NOI18N
    private static final String HG_STATUS_FLAG_UNKNOWN_CMD = "-u"; // NOI18N
    
    private static final String HG_COMMIT_CMD = "commit"; // NOI18N
    private static final String HG_COMMIT_OPT_LOGFILE_CMD = "--logfile"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME = "hgcommit"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME_SUFFIX = ".hgm"; // NOI18N
    private static final String HG_COMMIT_DEFAULT_MESSAGE = "[no commit message]"; // NOI18N
    
    private static final String HG_REVERT_CMD = "revert"; // NOI18N
    private static final String HG_ADD_CMD = "add"; // NOI18N
    
    private static final String HG_BRANCH_CMD = "branch"; // NOI18N
    private static final String HG_BRANCH_REV_CMD = "tip"; // NOI18N
    private static final String HG_BRANCH_REV_TEMPLATE_CMD = "--template={rev}\\n"; // NOI18N
    private static final String HG_BRANCH_SHORT_CS_TEMPLATE_CMD = "--template={node|short}\\n"; // NOI18N
    
    private static final String HG_CREATE_CMD = "init"; // NOI18N
    private static final String HG_CLONE_CMD = "clone"; // NOI18N
    
    private static final String HG_UPDATE_ALL_CMD = "update"; // NOI18N
    private static final String HG_UPDATE_FORCE_ALL_CMD = "-C"; // NOI18N
    private static final String HG_UPDATE_REVISION_CMD = "-R"; // NOI18N
    
    private static final String HG_REMOVE_CMD = "remove"; // NOI18N
    private static final String HG_REMOVE_FLAG_FORCE_CMD = "--force"; // NOI18N
    
    private static final String HG_LOG_CMD = "log"; // NOI18N
    private static final String HG_LOG_LIMIT_CMD = "-l 1"; // NOI18N
    private static final String HG_LOG_TEMPLATE_CMD = "--template={rev}\\n{desc}\\n{date|hgdate}\\n{node|short}\\n"; // NOI18N
    private static final String HG_CSET_TEMPLATE_CMD = "--template={rev}:{node|short}\\n"; // NOI18N
    private static final String HG_REV_TEMPLATE_CMD = "--template={rev}\\n"; // NOI18N
    private static final String HG_CSET_TARGET_TEMPLATE_CMD = "--template={rev} ({node|short})\\n"; // NOI18N
    
    private static final String HG_CAT_CMD = "cat"; // NOI18N
    private static final String HG_FLAG_OUTPUT_CMD = "--output"; // NOI18N
    
    private static final String HG_ANNOTATE_CMD = "annotate"; // NOI18N
    private static final String HG_ANNOTATE_FLAGN_CMD = "--number"; // NOI18N
    private static final String HG_ANNOTATE_FLAGU_CMD = "--user"; // NOI18N
    
    private static final String HG_EXPORT_CMD = "export"; // NOI18N
    private static final String HG_IMPORT_CMD = "import"; // NOI18N

    private static final String HG_RENAME_CMD = "rename"; // NOI18N
    private static final String HG_RENAME_AFTER_CMD = "-A"; // NOI18N
    private static final String HG_PATH_DEFAULT_CMD = "paths"; // NOI18N
    private static final String HG_PATH_DEFAULT_OPT = "default"; // NOI18N
    private static final String HG_PATH_DEFAULT_PUSH_OPT = "default-push"; // NOI18N
 
    
    // TODO: replace this hack 
    // Causes /usr/bin/hgmerge script to return when a merge
    // has conflicts with exit 0, instead of throwing up EDITOR. 
    // Problem is after this Hg thinks the merge succeded and no longer
    // marks repository with a merge needed flag. So Plugin needs to 
    // track this merge required status by changing merge conflict file
    // status. If the cache is removed this information would be lost.
    //
    // Really need Hg to give us back merge status information, 
    // which it currently does not
    private static final String HG_MERGE_CMD = "merge"; // NOI18N
    private static final String HG_MERGE_FORCE_CMD = "-f"; // NOI18N
    private static final String HG_MERGE_ENV = "EDITOR=success || $TEST -s"; // NOI18N

    private static final String HG_PULL_CMD = "pull"; // NOI18N
    private static final String HG_UPDATE_CMD = "-u"; // NOI18N
    private static final String HG_PUSH_CMD = "push"; // NOI18N
    private static final String HG_PUSH_FORCE_CMD = "-f"; // NOI18N
    private static final String HG_UNBUNDLE_CMD = "unbundle"; // NOI18N
    private static final String HG_ROLLBACK_CMD = "rollback"; // NOI18N
    private static final String HG_VERSION_CMD = "version"; // NOI18N
    private static final String HG_INCOMING_CMD = "incoming"; // NOI18N
    private static final String HG_OUTGOING_CMD = "outgoing"; // NOI18N
    private static final String HG_VERBOSE_CMD = "-v"; // NOI18N
    
    private static final String HG_MERGE_NEEDED_ERR = "(run 'hg heads' to see heads, 'hg merge' to merge)"; // NOI18N
    public static final String HG_MERGE_CONFLICT_ERR = "conflicts detected in "; // NOI18N
    private static final String HG_MERGE_MULTIPLE_HEADS_ERR = "abort: repo has "; // NOI18N
    private static final String HG_MERGE_UNCOMMITTED_ERR = "abort: outstanding uncommitted merges"; // NOI18N


    private static final String HG_NO_CHANGES_ERR = "no changes found"; // NOI18N
    private final static String HG_CREATE_NEW_BRANCH_ERR = "abort: push creates new remote branches!"; // NOI18N
    private final static String HG_HEADS_CREATED_ERR = "(+1 heads)"; // NOI18N
    
    private final static String HG_HEADS_CMD = "heads"; // NOI18N
    
    private static final String HG_NO_REPOSITORY_ERR = "abort: There is no Mercurial repository here"; // NOI18N
    private static final String HG_UPDATE_SPAN_BRANCHES_ERR = "abort: update spans branches"; // NOI18N
    private static final String HG_ALREADY_TRACKED_ERR = " already tracked!"; // NOI18N
    private static final String HG_NOT_TRACKED_ERR = " no tracked!"; // NOI18N
    private static final String HG_NOT_FOUND_ERR = "not found!"; // NOI18N
    private static final String HG_CANNOT_READ_COMMIT_MESSAGE_ERR = "abort: can't read commit message"; // NOI18N
    private static final String HG_UNABLE_EXECUTE_COMMAND_ERR = "unable to execute hg command"; // NOI18N
    private static final String HG_UNABLE_CLONE_ERR = "abort: destination "; // NOI18N
    private static final String HG_NODE_NAME_ERR = "abort: node name or service name not known"; // NOI18N
    private static final String HG_NO_CHANGE_NEEDED_ERR = "no change needed"; // NOI18N
    private static final String HG_NO_ROLLBACK_ERR = "no rollback information available"; // NOI18N
    private static final String HG_NO_UPDATES_ERR = "0 files updated, 0 files merged, 0 files removed, 0 files unresolved"; // NOI18N
    
    private static final char HG_STATUS_CODE_MODIFIED = 'M' + ' ';    // NOI18N // STATUS_VERSIONED_MODIFIEDLOCALLY
    private static final char HG_STATUS_CODE_ADDED = 'A' + ' ';      // NOI18N // STATUS_VERSIONED_ADDEDLOCALLY
    private static final char HG_STATUS_CODE_REMOVED = 'R' + ' ';   // NOI18N  // STATUS_VERSIONED_REMOVEDLOCALLY - still tracked, hg update will recover, hg commit
    private static final char HG_STATUS_CODE_CLEAN = 'C' + ' ';     // NOI18N  // STATUS_VERSIONED_UPTODATE
    private static final char HG_STATUS_CODE_DELETED = '!' + ' ';    // NOI18N // STATUS_VERSIONED_DELETEDLOCALLY - still tracked, hg update will recover, hg commit no effect
    private static final char HG_STATUS_CODE_NOTTRACKED = '?' + ' '; // NOI18N // STATUS_NOTVERSIONED_NEWLOCALLY - not tracked
    private static final char HG_STATUS_CODE_IGNORED = 'I' + ' ';     // NOI18N // STATUS_NOTVERSIONED_EXCLUDE - not shown by default
    private static final char HG_STATUS_CODE_CONFLICT = 'X' + ' ';    // NOI18N // STATUS_VERSIONED_CONFLICT - TODO when Hg status supports conflict markers
    
    private static final char HG_STATUS_CODE_ABORT = 'a' + 'b';    // NOI18N
    public static final String HG_STR_CONFLICT_EXT = ".conflict~"; // NOI18N

    /**
     * Merge working directory with the head revision
     * Merge the contents of the current working directory and the
     * requested revision. Files that changed between either parent are
     * marked as changed for the next commit and a commit must be
     * performed before any further updates are allowed.
     *
     * @param File repository of the mercurial repository's root directory
     * @param Revision to merge with, if null will merge with default tip rev
     * @return hg merge output
     * @throws HgException
     */
    public static List<String> doMerge(File repository, String revStr) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();
        List<String> env = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_MERGE_CMD);
        command.add(HG_MERGE_FORCE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if(revStr != null)
             command.add(revStr);
        env.add(HG_MERGE_ENV);
        
        return execEnv(command, env);
    }

    /**
     * Update the working directory to the tip revision.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param Boolean force an Update and overwrite any modified files in the  working directory
     * @param String revision to be updated to
     * @return hg update output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doUpdateAll(File repository, boolean bForce, String revision) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_UPDATE_ALL_CMD);
        if (bForce) command.add(HG_UPDATE_FORCE_ALL_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(revision);
        }

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorUpdateSpansBranches(list.get(0)))
            throw new HgException( list.get(0));
        
        return list;
    }
    
    /**
     * Roll back the last transaction in this repository
     * Transactions are used to encapsulate the effects of all commands
     * that create new changesets or propagate existing changesets into a
     * repository. For example, the following commands are transactional,
     * and their effects can be rolled back:
     * commit, import, pull, push (with this repository as destination)
     * unbundle
     * There is only one level of rollback, and there is no way to undo a rollback.
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg update output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doRollback(File repository) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_ROLLBACK_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (list.isEmpty())
            throw new HgException( list.get(0));
        
        return list;
    }
    
    /**
     * Return the version of hg, e.g. "0.9.3". // NOI18N
     *
     * @return String
     */
    public static String getHgVersion() {
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_VERSION_CMD);

        List<String> list = new LinkedList<String>();
        try {
            list = exec(command);
        } catch (HgException ex) {
            // Ignore Exception
            return null;
        }
        if (!list.isEmpty()) {
            int start = list.get(0).indexOf('(');
            int end = list.get(0).indexOf(')');
            if (start != -1 && end != -1) {
                return list.get(0).substring(start + 9, end);
            }
        }
        return null;
    }
    
    /**
     * Pull changes from the default pull locarion and update working directory.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg pull output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPull(File repository) throws HgException {
        return doPull(repository, null);
    }

    /**
     * Pull changes from the specified repository and
     * update working directory.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to pull from
     * @return hg pull output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPull(File repository, String from) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_PULL_CMD);
        command.add(HG_UPDATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (from != null) {
            command.add(from);
        }

        return exec(command);
    }
 
    /**
     * Unbundle changes from the specified local source repository and
     * update working directory.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File bundle identfies the compressed changegroup file to be applied
     * @return hg unbundle output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doUnbundle(File repository, File bundle) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_UNBUNDLE_CMD);
        command.add(HG_UPDATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (bundle != null) {
            command.add(bundle.getAbsolutePath());
        }

        return exec(command);
    }
 
    /**
     * Show the changesets that would be pulled if a pull
     * was requested from the default pull location
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg incoming output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncoming(File repository) throws HgException {
        return doIncoming(repository, null, null);
    }

    /**
     * Show the changesets that would be pulled if a pull
     * was requested from the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to query
     * @param File bundle to store downloaded changesets.
     * @return hg incoming output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncoming(File repository, String from, File bundle) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (bundle != null) {
            command.add(HG_OPT_BUNDLE);
            command.add(bundle.getAbsolutePath());
        }
        if (from != null) {
            command.add(from);
        }

        return exec(command);

    }
    
    /**
     * Show the changesets that would be pushed if a push
     * was requested to the specified local source repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param File source repository to query
     * @return hg outgoing output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doOutgoing(File repository, File to) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_OUTGOING_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        return exec(command);
    }

    /**
     * Push changes to the local repository to the specified repository
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File source repository to push to
     * @return hg push output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPush(File repository, File to) throws HgException {
        if (repository == null || to == null ) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_PUSH_CMD);
        command.add(HG_PUSH_FORCE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        return exec(command);
    }

     /**
     * Determines whether repository requires a merge - has more than 1 heads
     *
     * @param File repository of the mercurial repository's root directory
     * @return Boolean which is true if the repository needs a merge
     */
    public static Boolean isMergeRequired(File repository) {
        if (repository == null ) return false;
        
        try {
            List<String> list = getHeadRevisions(repository);

            if (!list.isEmpty() && list.size() > 1){
                Mercurial.LOG.log(Level.FINE, "isMergeRequired(): TRUE " + list); // NOI18N
                return true;
            }else{
                Mercurial.LOG.log(Level.FINE, "isMergeRequired(): FALSE " + list); // NOI18N
                return false;
            }
        } catch (HgException e) {
            return false;
        }
    }
  
    /**
     * Determines whether anything has been committed to the repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return Boolean which is true if the repository has revision history.
     */
    public static Boolean hasHistory(File repository) {
        if (repository == null ) return false;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        try {
            List<String> list = exec(command);
            if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
                return false;
            else
                return !list.isEmpty();
        } catch (HgException e) {
            return false;
        }
    }
    
    /**
     * Retrives all the log information for the specified repository.
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> list of all the log entries
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doLogAll(File repository) throws HgException {
        if (repository == null ) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException( list.get(0));
        return list;
    }
    
    /**
     * Retrives the log information for the specified file.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of file which revision history is to be retrieved.
     * @return List<String> list of the log entries for the specified file.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doLog(File repository, File file) throws HgException {
        if (repository == null ) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_LOG_TEMPLATE_CMD);
        command.add(file.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException( list.get(0));
        return list;
    }
    
    /**
     * Retrives the log information for the specified files.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<File> of files which revision history is to be retrieved.
     * @return List<String> list of the log entries for the specified file.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doLog(File repository, List<File> files) throws HgException {
        if (repository == null ) return null;
        if (files.isEmpty()) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        for(File f: files){
            command.add(f.getAbsolutePath());
        }
        
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException( list.get(0));
        return list;
    }
    
    /**
     * Retrieves the base revision of the specified file to the
     * specified output file.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file in the mercurial repository
     * @param File outFile to contain the contents of the file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCat(File repository, File file, File outFile) throws HgException {
        doCat(repository, file, outFile, "tip"); //NOI18N
    }
    
    /**
     * Retrieves the specified revision of the specified file to the
     * specified output file.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file in the mercurial repository
     * @param File outFile to contain the contents of the file
     * @param String of revision for the revision of the file to be
     * printed to the output file.
     * @return List<String> list of all the log entries
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCat(File repository, File file, File outFile, String revision) throws HgException {
        if (repository == null) return;
        if (file == null) return;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_CAT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_OUTPUT_CMD);
        command.add(outFile.getAbsolutePath());

        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }
        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException( list.get(0));
    }
    
    /**
     * Initialize a new repository in the given directory.  If the given
     * directory does not exist, it is created. Will throw a HgException
     * if the repository already exists.
     *
     * @param root for the mercurial repository
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCreate(File root) throws HgException {
        if (root == null ) return;
        List<String> command = new ArrayList();
        
        command.add(getHgCommand());
        command.add(HG_CREATE_CMD);
        command.add(root.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty())
            throw new HgException( list.get(0));
    }
    
    /**
     * Clone an exisiting repository to the specified target directory
     *
     * @param File repository of the mercurial repository's root directory
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(File repository, String target) throws HgException {
        if (repository == null) return null;
        return doClone(repository.getAbsolutePath(), target);
    }
    
    /**
     * Clone a repository to the specified target directory
     *
     * @param String repository of the mercurial repository
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(String repository, String target) throws HgException {
        if (repository == null || target == null) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_CLONE_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(repository);
        command.add(target);

        List<String> list = exec(command);
        if (!list.isEmpty() && 
             isErrorUnableClone(list.get(0)) || isErrorNodeName(list.get(0)))
            throw new HgException( list.get(0));
        return list;
    }
    
    /**
     * Commits the list of Locally Changed files to the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<files> of files to be committed to hg
     * @param String for commitMessage
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCommit(File repository, List<File> commitFiles, String commitMessage)  throws HgException {
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_COMMIT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        File tempfile = null;
        
        try {
            if (commitMessage == null || commitMessage.length() == 0) {
                commitMessage = HG_COMMIT_DEFAULT_MESSAGE;
            }
            // Create temporary file.
            tempfile = File.createTempFile(HG_COMMIT_TEMPNAME, HG_COMMIT_TEMPNAME_SUFFIX);
                
            // Write to temp file
            BufferedWriter out = new BufferedWriter(new FileWriter(tempfile));
            out.write(commitMessage);
            out.close();
              
            command.add(HG_COMMIT_OPT_LOGFILE_CMD);
            command.add(tempfile.getAbsolutePath());

            for(File f: commitFiles){
                command.add(f.getAbsolutePath());
            }
            List<String> list = exec(command);
            
            if (!list.isEmpty()
                    && (isErrorNotTracked(list.get(0)) || isErrorCannotReadCommitMsg(list.get(0))))
                throw new HgException( list.get(0));
            
        }catch (IOException ex){
            throw new HgException(HG_CANNOT_READ_COMMIT_MESSAGE_ERR);
        }finally{
            if (commitMessage != null && tempfile != null){
                tempfile.delete();
            }
        }
    }
    
    
    /**
     * Mark a source file as having been renamed to a destination file.
     * mercurial hg rename -A.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of sourceFile which was renamed
     * @param File of destFile to which sourceFile has been renaned
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRenameAfter(File repository, File sourceFile, File destFile)  throws HgException {
        if (repository == null) return;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_RENAME_CMD);
        command.add(HG_RENAME_AFTER_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        command.add(sourceFile.getAbsolutePath().substring(repository.getAbsolutePath().length()+1));
        command.add(destFile.getAbsolutePath().substring(repository.getAbsolutePath().length()+1));
        
        List<String> list = exec(command);
        if (!list.isEmpty())
            throw new HgException( list.get(0));
    }
    
    /**
     * Adds the list of Locally New files to the mercurial Repository
     * Their status will change to added and they will be added on the next
     * mercurial hg add.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be added to hg
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doAdd(File repository, List<File> addFiles)  throws HgException {
        if (repository == null) return;
        if (addFiles.size() == 0) return;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_ADD_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        for(File f: addFiles){
            if(f.isDirectory())
                continue;
            if(HgUtils.isIgnored(f) || SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE){
                Mercurial.LOG.log(Level.FINE, "doAdd(addFiles): Ignored File not added: {0}", new Object[] {f.getAbsolutePath()}); // NOI18N
                continue;
            }
            command.add(f.getAbsolutePath());
        }
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            throw new HgException( list.get(0));
    }

    /**
     * Reverts the list of files in the mercurial Repository to the specified revision
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be reverted
     * @param String revision to be reverted to
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRevert(File repository, List<File> revertFiles, String revision)  throws HgException {
        if (repository == null) return;
        if (revertFiles.size() == 0) return;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_REVERT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }

        for(File f: revertFiles){
            command.add(f.getAbsolutePath());
        }
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoChangeNeeded(list.get(0)))
            throw new HgException(list.get(0));
    }

    /**
     * Adds a Locally New file to the mercurial Repository
     * The status will change to added and they will be added on the next
     * mercurial hg commit.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of file to be added to hg
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doAdd(File repository, File file)  throws HgException {
        if (repository == null) return;
        if (file == null) return;
        if (file.isDirectory()) return;
        if(HgUtils.isIgnored(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE){
            Mercurial.LOG.log(Level.FINE, "doAdd(): Ignored File not added: {0}", // NOI18N
                    new Object[] {file.getAbsolutePath()});
            return;
        }
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_ADD_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            throw new HgException( list.get(0));
    }
    
    /**
     * Get the annotations for the specified file
     *
     * @param File repository of the mercurial repository's root directory
     * @param file path to mercurial repository's root directory
     * @return List<String> list of the annotated lines of the file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doAnnotate(File repository, File file) throws HgException {
        if (repository == null) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_ANNOTATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        command.add(HG_ANNOTATE_FLAGN_CMD);
        command.add(HG_ANNOTATE_FLAGU_CMD);
        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException( list.get(0));
        return list;
    }
  
    /**
     * Get the revisions this file has been modified in.
     *
     * @param File repository of the mercurial repository's root directory
     * @param files to query revisions for
     * @return List<String> list of the revisions of the file - {<rev>:<short cset hash>}
     *         or null if no commits made yet.
     */
    public static List<String> getAllRevisionsForFile(File repository, File[] files) {
        if (repository == null) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_CSET_TARGET_TEMPLATE_CMD);
        if(files != null) {
            for (File file : files) {
                command.add(file.getAbsolutePath());
            }
        }

        List<String> list = new ArrayList();
        try {
            list = exec(command);
        } catch (HgException ex) {
            // Ignore Exception
        }
        return list == null || list.isEmpty()? null: list;
    }

    /**
     * Get all the revisions for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> list of the revisions of the repository - {<rev>:<short cset hash>}
     *         or null if no commits made yet.
     */
    public static List<String> getAllRevisions(File repository) {
        if (repository == null) return null;
        return getAllRevisionsForFile(repository, null);
    }
    
    /**
     * Get the pull default for the specified repository, i.e. the default
     * destination for hg pull commmands.
     *
     * @param File repository of the mercurial repository's root directory
     * @return File for pull default
     */
    public static File getPullDefault(File repository) {
        return getPathDefault(repository, HG_PATH_DEFAULT_OPT);
    }

    /**
     * Get the push default for the specified repository, i.e. the default
     * destination for hg push commmands.
     *
     * @param File repository of the mercurial repository's root directory
     * @return File for push default
     */
    public static File getPushDefault(File repository) {
        return getPathDefault(repository, HG_PATH_DEFAULT_PUSH_OPT);
    }

    private static File getPathDefault(File repository, String type) {
        if (repository == null) return null;
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_PATH_DEFAULT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(type);

        String res = null;
        
        List<String> list = new LinkedList<String>();
        try {
            list = exec(command);
        } catch (HgException ex) {
            // Ignore Exception
        }
        if( !list.isEmpty()
                    && (!isErrorNotFound(list.get(0)))) {
            res = list.get(0);
        }
        return res != null ? new File(res): null;
    }
    
    /**
     * Returns the mercurial branch name if any for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return String branch name or null if not named
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getBranchName(File repository) throws HgException {
        if (repository == null) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_BRANCH_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return list.get(0);
        }else{
            return null;
        }
    }
    
    /**
     * Returns the mercurial branch revision for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return int value of revision for repository tip
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static int getBranchRev(File repository) throws HgException {
        if (repository == null) return -1;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_BRANCH_REV_CMD);
        command.add(HG_BRANCH_REV_TEMPLATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return Integer.parseInt(list.get(0)); 
        }else{
            return -1;
        }
    }
    
    /**
     * Returns the mercurial branch name if any for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return String branch short change set hash
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getBranchShortChangesetHash(File repository) throws HgException {
        if (repository == null) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_BRANCH_REV_CMD);
        command.add(HG_BRANCH_SHORT_CS_TEMPLATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return list.get(0);
        }else{
            return null;
        }
    }

    /**
     * Returns the revision number for the heads in a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last revision number is to be returned.
     * @return List<String> of revision numbers.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadRevisions(File repository) throws HgException {
        return  getHeadInfo(repository, HG_REV_TEMPLATE_CMD);
    }

    /**
     * Returns the changeset for the the heads in a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last changeset is to be returned.
     * @return List<String> of changeset ids.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadChangeSetIds(File repository) throws HgException {
        return  getHeadInfo(repository, HG_CSET_TARGET_TEMPLATE_CMD);
    }

    private static List<String> getHeadInfo(File repository, String template) throws HgException {

        if (repository == null) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_HEADS_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(template);

        return exec(command);
    }

    /**
     * Returns the revision number for the last change to a file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last revision number is to be returned.
     * @return String in the form of a revision number.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getLastRevision(File repository, File file) throws HgException {
        return  getLastChange(repository, file, HG_REV_TEMPLATE_CMD);
    }

    /**
     * Returns the changeset for the last change to a file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last changeset is to be returned.
     * @return String in the form of a changeset id.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getLastChangeSetId(File repository, File file) throws HgException {
        return  getLastChange(repository, file, HG_CSET_TEMPLATE_CMD);
    }

    private static String getLastChange(File repository, File file, String template) throws HgException {

        if (repository == null) return null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(template);
        command.add(file.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()){
            return new StringBuffer(list.get(0)).toString();
        }else{
            return null;
        }
    }
    
    
    /**
     * Returns the mercurial status for a given file
     *
     * @param File repository of the mercurial repository's root directory
     * @param cwd current working directory containing file to be checked
     * @param filename name of file whose status is to be checked
     * @return FileInformation for the given filename
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static FileInformation getSingleStatus(File repository, String cwd, String filename)  throws HgException{
        
        FileInformation info = null;
        List<String> list = doSingleStatusCmd(repository, cwd, filename);
        if(list == null || list.isEmpty())
            return new FileInformation(FileInformation.STATUS_UNKNOWN,null, false);
        
        if(SharabilityQuery.getSharability(new File(cwd)) == SharabilityQuery.NOT_SHARABLE){
            Mercurial.LOG.log(Level.FINE, "getSingleStatus(): Excluded File - StatusLine: {0} Status: EXCLUDED  RepoPath:{2} cwd:{3}", // NOI18N
                    new Object[] {list.get(0), filename, repository.getAbsolutePath(), cwd} );
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED,null, false);
        }
        
        info =  getFileInformationFromStatusLine(list.get(0));
        // Handles Copy status
        // Could save copy source in FileStatus but for now we don't need it.
        // FileStatus used in Fileinformation.java:getStatusText() and getShortStatusText() to check if
        // file is Locally Copied when it's status is Locally Added
        if(list.size() == 2) {
            if (list.get(1).length() > 0){
                if (list.get(1).charAt(0) == ' '){
                
                    info =  new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY,
                            new FileStatus(new File(new File(cwd), filename), true), false);
                    Mercurial.LOG.log(Level.FINE, "getSingleStatus() - Copied: Locally Added {0}, Copy Source {1}", // NOI18N
                            new Object[] {list.get(0), list.get(1)} );
                }
            } else {
                Mercurial.LOG.log(Level.FINE, "getSingleStatus() - Second line empty: first line: {0}", list.get(0)); // NOI18N
            }
        }
        
        // Handle Conflict Status
        // TODO: remove this if Hg status supports Conflict marker
        if(existsConflictFile(cwd + File.separator + filename)){
            info =  new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, null, false);
            Mercurial.LOG.log(Level.FINE, "getSingleStatus(): CONFLICT StatusLine: {0} Status: {1}  {2} RepoPath:{3} cwd:{4} CONFLICT {5}", // NOI18N
                new Object[] {list.get(0), info.getStatus(), filename, repository.getAbsolutePath(), cwd,
                cwd + File.separator + filename + HgCommand.HG_STR_CONFLICT_EXT} );
        }
        
        Mercurial.LOG.log(Level.FINE, "getSingleStatus(): StatusLine: {0} Status: {1}  {2} RepoPath:{3} cwd:{4}", // NOI18N
                new Object[] {list.get(0), info.getStatus(), filename, repository.getAbsolutePath(), cwd} );
        return info;
    }
    
    /**
     * Returns the mercurial status for all files in a given repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return Map of files and status for all files under the repository root
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getAllStatus(File repository)  throws HgException{
        return getAllStatusWithFlags(repository, HG_STATUS_FLAG_ALL_CMD, true);
    }
    
    /**
     * Returns the mercurial status for only files of interest to us in a given repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @return Map of files and status for all files of interest under the repository root
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getAllInterestingStatus(File repository)  throws HgException{
        return getAllStatusWithFlags(repository, HG_STATUS_FLAG_INTERESTING_CMD, true);
    }
    
    /**
    /**
     * Returns the mercurial status for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File dir of the directory of interest
     * @return Map of files and status for all files of interest in the directory of interest
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getInterestingStatus(File repository, File dir)  throws HgException{
        return getDirStatusWithFlags(repository, dir, HG_STATUS_FLAG_INTERESTING_CMD, true);
    }
    
    /**
     * Returns the mercurial status for only files of interest to us in a given repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @return Map of files and status for all files of interest under the repository root
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getAllRemovedDeletedStatus(File repository)  throws HgException{
        return getAllStatusWithFlags(repository, HG_STATUS_FLAG_REM_DEL_CMD, true);
    }
    
    /**
     * Returns the mercurial status for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File dir of the directory of interest
     * @return Map of files and status for all files of interest in the specified directory
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getRemovedDeletedStatus(File repository, File dir)  throws HgException{
        return getDirStatusWithFlags(repository, dir, HG_STATUS_FLAG_REM_DEL_CMD, true);
    }
    
    /**
     * Returns the unknown files under a mercurial repository root
     *
     * @param File repository of the mercurial repository's root directory
     * @return Map of files and status for all files under the repository root
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getAllUnknownStatus(File repository)  throws HgException{
        Map<File, FileInformation> files = getAllStatusWithFlags(repository, HG_STATUS_FLAG_UNKNOWN_CMD, false);
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if(HgUtils.isIgnored(file) || SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE)
                i.remove();
        }
        return files;
    }
    
    /**
     * Remove the specified file from the mercurial Repository
     * mercurial hg commit.
     *
     * @param File repository of the mercurial repository's root directory
     * @param f path to be removed from the repository
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRemove(File repository, File f)  throws HgException {
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_REMOVE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_REMOVE_FLAG_FORCE_CMD);
        command.add(f.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            throw new HgException( list.get(0));
    }
    
    /**
     * Export the diffs for the specified revision to the specified output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revStr the revision whose diffs are to be exported
     * @param outputFileName path of the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doExport(File repository, String revStr, String outputFileName)  throws HgException {
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_EXPORT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_OUTPUT_CMD);
        command.add(outputFileName);
        command.add(revStr);

        List<String> list = exec(command);
        if (!list.isEmpty())
            throw new HgException( list.get(0));
    }
    
    /**
     * Imports the diffs from the specified file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File patchFile of the patch file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doImport(File repository, File patchFile)  throws HgException {
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_IMPORT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(patchFile.getAbsolutePath());

        List<String> list = exec(command);
        // The first line of output is "applying <filename>" // NOI18N
        if (!list.isEmpty() && list.size() > 1) {
            throw new HgException( list.get(1));
        }
    }
    
    /**
     * Returns Map of mercurial file and status for files in a given repository as specified by the status flags
     */
    private static Map<File, FileInformation> getAllStatusWithFlags(File repository, String statusFlags, boolean bIgnoreUnversioned)  throws HgException{
        return getDirStatusWithFlags(repository, null, statusFlags, bIgnoreUnversioned);
    }
    
    private static Map<File, FileInformation> getDirStatusWithFlags(File repository, File dir, String statusFlags, boolean bIgnoreUnversioned)  throws HgException{
        if (repository == null) return null;
        
        List<FileStatus> statusList = new ArrayList();
        FileInformation info = null;
        List<String> list = doRepositoryDirStatusCmd(repository, dir, statusFlags);
        
        Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>(list.size());
        
        for(String statusLine: list){
            info =  getFileInformationFromStatusLine(statusLine);
            if(bIgnoreUnversioned){
                if(info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED ||
                        info.getStatus() == FileInformation.STATUS_UNKNOWN) continue;
            }else{
                if(info.getStatus() == FileInformation.STATUS_UNKNOWN) continue;
            }
            StringBuffer filePath = new StringBuffer(repository.getAbsolutePath()).append(File.separatorChar);
            StringBuffer sb = new StringBuffer(statusLine);
            sb.delete(0,2); // Strip status char and following 2 spaces: [MARC\?\!I][ ][ ]
            filePath.append(sb.toString());
            
            // Handle Conflict Status
            // TODO: remove this if Hg status supports Conflict marker
            if (existsConflictFile(filePath.toString())) {
                info = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, null, false);
                Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): CONFLICT repository path: {0} status flags: {1} status list {2} CONFLICT {3}", new Object[]{repository.getAbsolutePath(), statusFlags, list, filePath.toString() + HgCommand.HG_STR_CONFLICT_EXT}); // NOI18N
            }
            repositoryFiles.put(new File(filePath.toString()), info);
        }
        
        Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): repository path: {0} status flags: {1} status list {2}", // NOI18N
                new Object[] {repository.getAbsolutePath(), statusFlags, list} );
        return repositoryFiles;
    }
    
    /**
     * Gets file information for a given hg status output status line
     */
    private static FileInformation getFileInformationFromStatusLine(String status){
        FileInformation info = null;
        if (status == null || (status.length() == 0)) return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, null, false);
        
        char c0 = status.charAt(0);
        char c1 = status.charAt(1);
        switch(c0 + c1) {
        case HG_STATUS_CODE_MODIFIED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_ADDED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_REMOVED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_CLEAN:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE,null, false);
            break;
        case HG_STATUS_CODE_DELETED:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY,null, false);
            break;
        case HG_STATUS_CODE_IGNORED:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED,null, false);
            break;
        case HG_STATUS_CODE_NOTTRACKED:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY,null, false);
            break;
        // Leave this here for whenever Hg status suports conflict markers
        case HG_STATUS_CODE_CONFLICT:
            info = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT,null, false);
            break;
        case HG_STATUS_CODE_ABORT:
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED,null, false);
            break;
        default:
            info = new FileInformation(FileInformation.STATUS_UNKNOWN,null, false);
            break;
        }
        
        return info;
    }
    
    /**
     * Gets hg status command output line for a given file
     */
    private static List<String> doSingleStatusCmd(File repository, String cwd, String filename)  throws HgException{
        String statusLine = null;
        
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_STATUS_CMD);
        command.add(HG_STATUS_FLAG_ALL_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        // In 0.9.3 hg status does not give back copy information unless we 
        // use relative paths from repository. This is fixed in 0.9.4.
        // See http://www.selenic.com/mercurial/bts/issue545.
        command.add(new File(cwd, filename).getAbsolutePath().substring(repository.getAbsolutePath().length()+1));
        
        return exec(command);
    }
    
    /**
     * Gets hg status command output list for the specified status flags for a given repository and directory
     */
    private static List<String> doRepositoryDirStatusCmd(File repository, File dir, String statusFlags)  throws HgException{
        List<String> command = new ArrayList();

        command.add(getHgCommand());
        command.add(HG_STATUS_CMD);

        command.add(statusFlags);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        if (dir != null) {
            command.add(dir.getAbsolutePath());
        } else {
            command.add(repository.getAbsolutePath());
        }
        
        List<String> list =  exec(command);
        if (!list.isEmpty() && isErrorNoRepository(list.get(0)))
            throw new HgException(list.get(0));
        return list;
    }
    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */

    private static List<String> execEnv(List<String> command, List<String> env) throws HgException{
        assert ( command != null && command.size() > 0);
        List<String> list = new ArrayList<String>();
        BufferedReader input = null;
        try{
            Mercurial.LOG.log(Level.FINE, "execEnv(): " + command); // NOI18N
            Process proc;
            if(env != null && env.size() > 0){
                proc = Runtime.getRuntime().exec(
                    command.toArray(new String[command.size()]),
                    env.toArray(new String[env.size()]));
            }else{
                proc = Runtime.getRuntime().exec(
                    command.toArray(new String[command.size()]));               
            }
 
            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            
            String line;
            while ((line = input.readLine()) != null){
                list.add(line);
            }
            input.close();
            input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = input.readLine()) != null){
                list.add(line);
            }
            input.close();
            input = null;
        }catch(IOException e){
            // Hg does not seem to be returning error status != 0
            // even when it fails when for instance adding an already tracked file to
            // the repository - we will have to examine the output in the cpntext of the
            // calling func and raise exceptions there if needed
            throw new HgException(HG_UNABLE_EXECUTE_COMMAND_ERR);
        }finally{
            // TODO: should handle the input.close() if input != null
            input = null;
        }
        return list;
    }
    
    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */
    private static List<String> exec(List<String> command) throws HgException{
        return execEnv(command, null);
    }
    
    private static String getHgCommand() {
        String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath();
        if (defaultPath == null || defaultPath.length() == 0) 
            return HG_COMMAND;
        else
            return defaultPath + File.separatorChar + HG_COMMAND;
    }

    public static boolean isMergeNeededMsg(String msg) {
        return msg.indexOf(HG_MERGE_NEEDED_ERR) > -1;                                   // NOI18N
    }
    
    public static boolean isMergeConflictMsg(String msg) {
        return msg.indexOf(HG_MERGE_CONFLICT_ERR) > -1;                                   // NOI18N
    }

    public static boolean isMergeAbortMultipleHeadsMsg(String msg) {
        return msg.indexOf(HG_MERGE_MULTIPLE_HEADS_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMergeAbortUncommittedMsg(String msg) {
        return msg.indexOf(HG_MERGE_UNCOMMITTED_ERR) > -1;                                   // NOI18N
    }
     
    public static boolean isNoChanges(String msg) {
        return msg.indexOf(HG_NO_CHANGES_ERR) > -1;                                   // NOI18N
    }
    
    private static boolean isErrorNoRepository(String msg) {
        return msg.indexOf(HG_NO_REPOSITORY_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorUpdateSpansBranches(String msg) {
        return msg.indexOf(HG_UPDATE_SPAN_BRANCHES_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorAlreadyTracked(String msg) {
        return msg.indexOf(HG_ALREADY_TRACKED_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorNotTracked(String msg) {
        return msg.indexOf(HG_NOT_TRACKED_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNotFound(String msg) {
        return msg.indexOf(HG_NOT_FOUND_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorCannotReadCommitMsg(String msg) {
        return msg.indexOf(HG_CANNOT_READ_COMMIT_MESSAGE_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorUnableClone(String msg) {
        return msg.indexOf(HG_UNABLE_CLONE_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorNodeName(String msg) {
        return msg.indexOf(HG_NODE_NAME_ERR) > -1; // NOI18N
    }
    
    private static boolean isErrorNoChangeNeeded(String msg) {
        return msg.indexOf(HG_NO_CHANGE_NEEDED_ERR) > -1;    // NOI18N
    }
    
    public static boolean isCreateNewBranch(String msg) {
        return msg.indexOf(HG_CREATE_NEW_BRANCH_ERR) > -1;                                   // NOI18N
    }
    
    public static boolean isHeadsCreated(String msg) {
        return msg.indexOf(HG_HEADS_CREATED_ERR) > -1;                                   // NOI18N
    }
    
    public static boolean isNoRollbackPossible(String msg) {
        return msg.indexOf(HG_NO_ROLLBACK_ERR) > -1;                                   // NOI18N
    }
    
    public static boolean isNoUpdates(String msg) {
        return msg.indexOf(HG_NO_UPDATES_ERR) > -1;                                   // NOI18N
    }
    
    public static void createConflictFile(String path) {
        try {
            File file = new File(path + HG_STR_CONFLICT_EXT);

            boolean success = file.createNewFile();
            Mercurial.LOG.log(Level.FINE, "createConflictFile(): File: {0} {1}", // NOI18N
                new Object[] {path + HG_STR_CONFLICT_EXT, success? "Created": "Not Created"} ); // NOI18N
        } catch (IOException e) {
        }
    }
    
    public static void deleteConflictFile(String path) {
        boolean success = (new File(path + HG_STR_CONFLICT_EXT)).delete();

        Mercurial.LOG.log(Level.FINE, "deleteConflictFile(): File: {0} {1}", // NOI18N
                new Object[] {path + HG_STR_CONFLICT_EXT, success? "Deleted": "Not Deleted"} ); // NOI18N
    }

    public static boolean existsConflictFile(String path) {        
        File file = new File(path + HG_STR_CONFLICT_EXT);
        boolean bExists = file.canWrite();
        
        Mercurial.LOG.log(Level.FINE, "existsConflictFile(): File: {0} {1}", // NOI18N
                new Object[] {path + HG_STR_CONFLICT_EXT, bExists? "Exists": "Does Not Exist"} ); // NOI18N
        return bExists;
    }

    /**
     * This utility class should not be instantiated anywhere.
     */
    private HgCommand() {
    }
    
    
}
