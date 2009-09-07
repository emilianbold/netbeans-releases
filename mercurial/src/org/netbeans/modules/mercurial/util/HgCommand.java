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

package org.netbeans.modules.mercurial.util;

import java.awt.EventQueue;
import java.net.URISyntaxException;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.PasswordAuthentication;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.mercurial.HgKenaiSupport;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.UserCredentialsSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author jrice
 */
public class HgCommand {
    public static final String HG_COMMAND = "hg";  // NOI18N
    public static final String HG_WINDOWS_EXE = ".exe";  // NOI18N
    public static final String HG_WINDOWS_BAT = ".bat";  // NOI18N
    public static final String HG_WINDOWS_CMD = ".cmd";  // NOI18N
    public static final String[] HG_WINDOWS_EXECUTABLES = new String[] {
            HG_COMMAND + HG_WINDOWS_EXE,
            HG_COMMAND + HG_WINDOWS_BAT,
            HG_COMMAND + HG_WINDOWS_CMD,
    };
    public static final String HG_COMMAND_PLACEHOLDER = new String(HG_COMMAND);
    public static final String HGK_COMMAND = "hgk";  // NOI18N

    private static final String HG_STATUS_CMD = "status";  // NOI18N // need -A to see ignored files, specified in .hgignore, see man hgignore for details
    private static final String HG_OPT_REPOSITORY = "--repository"; // NOI18N
    private static final String HG_OPT_BUNDLE = "--bundle"; // NOI18N
    private static final String HG_OPT_CWD_CMD = "--cwd"; // NOI18N
    private static final String HG_OPT_USERNAME = "--user"; // NOI18N

    private static final String HG_OPT_FOLLOW = "--follow"; // NOI18N
    private static final String HG_STATUS_FLAG_ALL_CMD = "-marduicC"; // NOI18N
    private static final String HG_FLAG_REV_CMD = "--rev"; // NOI18N
    private static final String HG_STATUS_FLAG_TIP_CMD = "tip"; // NOI18N
    private static final String HG_STATUS_FLAG_REM_DEL_CMD = "-rd"; // NOI18N
    private static final String HG_STATUS_FLAG_INTERESTING_CMD = "-marduC"; // NOI18N
    private static final String HG_STATUS_FLAG_UNKNOWN_CMD = "-u"; // NOI18N
    private static final String HG_HEAD_STR = "HEAD"; // NOI18N
    private static final String HG_FLAG_DATE_CMD = "--date"; // NOI18N

    private static final String HG_COMMIT_CMD = "commit"; // NOI18N
    private static final String HG_COMMIT_OPT_LOGFILE_CMD = "--logfile"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME = "hgcommit"; // NOI18N
    private static final String HG_COMMIT_TEMPNAME_SUFFIX = ".hgm"; // NOI18N
    private static final String HG_COMMIT_DEFAULT_MESSAGE = "[no commit message]"; // NOI18N

    private static final String HG_REVERT_CMD = "revert"; // NOI18N
    private static final String HG_REVERT_NOBACKUP_CMD = "--no-backup"; // NOI18N
    private static final String HG_ADD_CMD = "add"; // NOI18N

    private static final String HG_BRANCH_CMD = "branch"; // NOI18N
    private static final String HG_BRANCH_REV_CMD = "tip"; // NOI18N
    private static final String HG_BRANCH_REV_TEMPLATE_CMD = "--template={rev}\\n"; // NOI18N
    private static final String HG_BRANCH_SHORT_CS_TEMPLATE_CMD = "--template={node|short}\\n"; // NOI18N
    private static final String HG_BRANCH_INFO_TEMPLATE_CMD = "--template={branches}:{rev}:{node|short}\\n"; // NOI18N

    private static final String HG_CREATE_CMD = "init"; // NOI18N
    private static final String HG_CLONE_CMD = "clone"; // NOI18N

    private static final String HG_UPDATE_ALL_CMD = "update"; // NOI18N
    private static final String HG_UPDATE_FORCE_ALL_CMD = "-C"; // NOI18N

    private static final String HG_REMOVE_CMD = "remove"; // NOI18N
    private static final String HG_REMOVE_FLAG_FORCE_CMD = "--force"; // NOI18N

    private static final String HG_LOG_CMD = "log"; // NOI18N
    private static final String HG_TIP_CMD = "tip"; // NOI18N
    private static final String HG_OUT_CMD = "out"; // NOI18N
    private static final String HG_LOG_LIMIT_ONE_CMD = "-l 1"; // NOI18N
    private static final String HG_LOG_LIMIT_CMD = "-l"; // NOI18N

    private static final String HG_LOG_NO_MERGES_CMD = "-M";
    private static final String HG_LOG_DEBUG_CMD = "--debug";
    private static final String HG_LOG_TEMPLATE_HISTORY_NO_FILEINFO_CMD =
            "--template=rev:{rev}\\nauth:{author}\\ndesc:{desc}\\ndate:{date|hgdate}\\nid:{node|short}\\n" + // NOI18N
            "\\nendCS:\\n"; // NOI18N
    private static final String HG_LOG_REV_TIP_RANGE = "tip:0"; // NOI18N
    private static final String HG_LOG_REVISION_OUT = "rev:"; // NOI18N
    private static final String HG_LOG_AUTHOR_OUT = "auth:"; // NOI18N
    private static final String HG_LOG_DESCRIPTION_OUT = "desc:"; // NOI18N
    private static final String HG_LOG_DATE_OUT = "date:"; // NOI18N
    private static final String HG_LOG_ID_OUT = "id:"; // NOI18N
    private static final String HG_LOG_PARENTS_OUT = "parents:"; // NOI18N
    private static final String HG_LOG_FILEMODS_OUT = "file_mods:"; // NOI18N
    private static final String HG_LOG_FILEADDS_OUT = "file_adds:"; // NOI18N
    private static final String HG_LOG_FILEDELS_OUT = "file_dels:"; // NOI18N
    private static final String HG_LOG_FILECOPIESS_OUT = "file_copies:"; // NOI18N
    private static final String HG_LOG_ENDCS_OUT = "endCS:"; // NOI18N

    private static final String HG_LOG_PATCH_CMD = "-p";
    private static final String HG_LOG_TEMPLATE_EXPORT_FILE_CMD =
        "--template=# Mercurial Export File Diff\\n# changeset: \\t{rev}:{node|short}\\n# user:\\t\\t{author}\\n# date:\\t\\t{date|isodate}\\n# summary:\\t{desc}\\n\\n";

    private static final String HG_CSET_TEMPLATE_CMD = "--template={rev}:{node|short}\\n"; // NOI18N
    private static final String HG_REV_TEMPLATE_CMD = "--template={rev}\\n"; // NOI18N
    private static final String HG_CSET_TARGET_TEMPLATE_CMD = "--template={rev} ({node|short})\\n"; // NOI18N

    private static final String HG_CAT_CMD = "cat"; // NOI18N
    private static final String HG_FLAG_OUTPUT_CMD = "--output"; // NOI18N

    private static final String HG_COMMONANCESTOR_CMD = "debugancestor"; // NOI18N

    private static final String HG_ANNOTATE_CMD = "annotate"; // NOI18N
    private static final String HG_ANNOTATE_FLAGN_CMD = "--number"; // NOI18N
    private static final String HG_ANNOTATE_FLAGU_CMD = "--user"; // NOI18N

    private static final String HG_EXPORT_CMD = "export"; // NOI18N
    private static final String HG_IMPORT_CMD = "import"; // NOI18N

    private static final String HG_RENAME_CMD = "rename"; // NOI18N
    private static final String HG_RENAME_AFTER_CMD = "-A"; // NOI18N
    private static final String HG_NEWEST_FIRST = "--newest-first"; // NOI18N

    private static final String HG_RESOLVE_CMD = "resolve";             //NOI18N
    private static final String HG_RESOLVE_MARK_RESOLVED = "--mark";   //NOI18N

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

    public static final String HG_HGK_PATH_SOLARIS10 = "/usr/demo/mercurial"; // NOI18N
    private static final String HG_HGK_PATH_SOLARIS10_ENV = "PATH=/usr/bin/:/usr/sbin:/bin:"+ HG_HGK_PATH_SOLARIS10; // NOI18N

    private static final String HG_PULL_CMD = "pull"; // NOI18N
    private static final String HG_UPDATE_CMD = "-u"; // NOI18N
    private static final String HG_PUSH_CMD = "push"; // NOI18N
    private static final String HG_UNBUNDLE_CMD = "unbundle"; // NOI18N
    private static final String HG_ROLLBACK_CMD = "rollback"; // NOI18N
    private static final String HG_BACKOUT_CMD = "backout"; // NOI18N
    private static final String HG_BACKOUT_MERGE_CMD = "--merge"; // NOI18N
    private static final String HG_BACKOUT_COMMIT_MSG_CMD = "-m"; // NOI18N
    private static final String HG_REV_CMD = "-r"; // NOI18N

    private static final String HG_STRIP_CMD = "strip"; // NOI18N
    private static final String HG_STRIP_EXT_CMD = "extensions.mq="; // NOI18N
    private static final String HG_STRIP_NOBACKUP_CMD = "-n"; // NOI18N
    private static final String HG_STRIP_FORCE_MULTIHEAD_CMD = "-f"; // NOI18N

    private static final String HG_VERIFY_CMD = "verify"; // NOI18N

    private static final String HG_VERSION_CMD = "version"; // NOI18N
    private static final String HG_INCOMING_CMD = "incoming"; // NOI18N
    private static final String HG_OUTGOING_CMD = "outgoing"; // NOI18N
    private static final String HG_VIEW_CMD = "view"; // NOI18N
    private static final String HG_VERBOSE_CMD = "-v"; // NOI18N
    private static final String HG_CONFIG_OPTION_CMD = "--config"; // NOI18N
    private static final String HG_FETCH_EXT_CMD = "extensions.fetch="; // NOI18N
    private static final String HG_FETCH_CMD = "fetch"; // NOI18N
    public static final String HG_PROXY_ENV = "http_proxy="; // NOI18N

    private static final String HG_MERGE_NEEDED_ERR = "(run 'hg heads' to see heads, 'hg merge' to merge)"; // NOI18N
    public static final String HG_MERGE_CONFLICT_ERR = "conflicts detected in "; // NOI18N
    public static final String HG_MERGE_CONFLICT_WIN1_ERR = "merging"; // NOI18N
    public static final String HG_MERGE_CONFLICT_WIN2_ERR = "failed!"; // NOI18N
    private static final String HG_MERGE_MULTIPLE_HEADS_ERR = "abort: repo has "; // NOI18N
    private static final String HG_MERGE_UNCOMMITTED_ERR = "abort: outstanding uncommitted merges"; // NOI18N

    private static final String HG_MERGE_UNAVAILABLE_ERR = "is not recognized as an internal or external command";

    private static final String HG_NO_CHANGES_ERR = "no changes found"; // NOI18N
    private final static String HG_CREATE_NEW_BRANCH_ERR = "abort: push creates new remote "; // NOI18N
    private final static String HG_HEADS_CREATED_ERR = "(+1 heads)"; // NOI18N
    private final static String HG_NO_HG_CMD_FOUND_ERR = "hg: not found";
    private final static String HG_ARG_LIST_TOO_LONG_ERR = "Arg list too long";
    private final static String HG_ARGUMENT_LIST_TOO_LONG_ERR = "Argument list too long"; //NOI18N

    private final static String HG_HEADS_CMD = "heads"; // NOI18N

    private static final String HG_NO_REPOSITORY_ERR = "There is no Mercurial repository here"; // NOI18N
    private static final String HG_NO_RESPONSE_ERR = "no suitable response from remote hg!"; // NOI18N
    private static final String HG_NOT_REPOSITORY_ERR = "does not appear to be an hg repository"; // NOI18N
    private static final String HG_REPOSITORY = "repository"; // NOI18N
    private static final String HG_NOT_FOUND_ERR = "not found!"; // NOI18N
    private static final String HG_UPDATE_SPAN_BRANCHES_ERR = "abort: update spans branches"; // NOI18N
    private static final String HG_ALREADY_TRACKED_ERR = " already tracked!"; // NOI18N
    private static final String HG_NOT_TRACKED_ERR = " no tracked!"; // NOI18N
    private static final String HG_CANNOT_READ_COMMIT_MESSAGE_ERR = "abort: can't read commit message"; // NOI18N
    private static final String HG_CANNOT_RUN_ERR = "Cannot run program"; // NOI18N
    private static final String HG_ABORT_ERR = "abort: "; // NOI18N
    //#132984: range of issues with upgrade to Hg 1.0, error string changed from branches to heads, just removed ending
    private static final String HG_ABORT_PUSH_ERR = "abort: push creates new remote "; // NOI18N
    private static final String HG_ABORT_NO_FILES_TO_COPY_ERR = "abort: no files to copy"; // NOI18N
    private static final String HG_ABORT_NO_DEFAULT_PUSH_ERR = "abort: repository default-push not found!"; // NOI18N
    private static final String HG_ABORT_NO_DEFAULT_ERR = "abort: repository default not found!"; // NOI18N
    private static final String HG_ABORT_POSSIBLE_PROXY_ERR = "abort: error: node name or service name not known"; // NOI18N
    private static final String HG_ABORT_UNCOMMITTED_CHANGES_ERR = "abort: outstanding uncommitted changes"; // NOI18N
    private static final String HG_BACKOUT_MERGE_NEEDED_ERR = "(use \"backout --merge\" if you want to auto-merge)";
    private static final String HG_ABORT_BACKOUT_MERGE_CSET_ERR = "abort: cannot back out a merge changeset without --parent"; // NOI18N"
    private static final String HG_COMMIT_AFTER_MERGE_ERR = "abort: cannot partially commit a merge (do not specify files or patterns)"; // NOI18N"

    private static final String HG_NO_CHANGE_NEEDED_ERR = "no change needed"; // NOI18N
    private static final String HG_NO_ROLLBACK_ERR = "no rollback information available"; // NOI18N
    private static final String HG_NO_UPDATES_ERR = "0 files updated, 0 files merged, 0 files removed, 0 files unresolved"; // NOI18N
    private static final String HG_NO_VIEW_ERR = "hg: unknown command 'view'"; // NOI18N
    private static final String HG_HGK_NOT_FOUND_ERR = "sh: hgk: not found"; // NOI18N
    private static final String HG_NO_SUCH_FILE_ERR = "No such file"; // NOI18N

    private static final String HG_NO_REV_STRIP_ERR = "abort: unknown revision"; // NOI18N
    private static final String HG_LOCAL_CHANGES_STRIP_ERR = "abort: local changes found"; // NOI18N
    private static final String HG_MULTIPLE_HEADS_STRIP_ERR = "no rollback information available"; // NOI18N

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

    private static final String HG_EPOCH_PLUS_ONE_YEAR = "1971-01-01"; // NOI18N

    private static final String HG_AUTHORIZATION_REQUIRED_ERR = "authorization required"; // NOI18N
    private static final String HG_AUTHORIZATION_FAILED_ERR = "authorization failed"; // NOI18N
    public static final String COMMIT_AFTER_MERGE = "commitAfterMerge"; //NOI18N

    private static final String HG_LOG_FULL_CHANGESET_NAME = "log-full-changeset.tmpl"; //NOI18N
    private static final String HG_LOG_ONLY_FILES_CHANGESET_NAME = "log-only-files-changeset.tmpl"; //NOI18N
    private static final String HG_LOG_CHANGESET_GENERAL_NAME = "changeset.tmpl"; //NOI18N
    private static final String HG_LOG_STYLE_NAME = "log.style";        //NOI18N
    private static final String HG_ARGUMENT_STYLE = "--style=";         //NOI18N
    private static final int WINDOWS_MAX_COMMANDLINE_SIZE = 8000;
    private static final int MAC_MAX_COMMANDLINE_SIZE = 64000;
    private static final int UNIX_MAX_COMMANDLINE_SIZE = 128000;
    private static final int MAX_COMMANDLINE_SIZE;
    static {
        String maxCmdSizeProp = System.getProperty("mercurial.maxCommandlineSize");
        if (maxCmdSizeProp == null) {
            maxCmdSizeProp = "";                                            //NOI18N
        }
        int maxCmdSize = 0;
        try {
            maxCmdSize = Integer.parseInt(maxCmdSizeProp);
        } catch (NumberFormatException e) {
            maxCmdSize = 0;
        }
        if (maxCmdSize < 1024) {
            if (Utilities.isWindows()) {
                maxCmdSize = WINDOWS_MAX_COMMANDLINE_SIZE;
            } else if (Utilities.isMac()) {
                maxCmdSize = MAC_MAX_COMMANDLINE_SIZE;
            } else {
                maxCmdSize = UNIX_MAX_COMMANDLINE_SIZE;
            }
        }
        MAX_COMMANDLINE_SIZE = maxCmdSize;
    }

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

        List<String> list = execEnv(command, env);
        return list;
    }

    /**
     * Update the working directory to the tip revision.
     * By default, update will refuse to run if doing so would require
     * merging or discarding local changes.
     *
     * @param File repository of the mercurial repository's root directory
     * @param Boolean force an Update and overwrite any modified files in the  working directory
     * @param String revision to be updated to
     * @param Boolean throw exception on error
     * @return hg update output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doUpdateAll(File repository, boolean bForce, String revision, boolean bThrowException) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_UPDATE_ALL_CMD);
        command.add(HG_VERBOSE_CMD);
        if (bForce) command.add(HG_UPDATE_FORCE_ALL_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(revision);
        }

        List<String> list = exec(command);
        if (bThrowException) {
            if (!list.isEmpty()) {
                if  (isErrorUpdateSpansBranches(list.get(0))) {
                    throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_UPDATE_MERGE_TEXT"));
                } else if (isMergeAbortUncommittedMsg(list.get(0))) {
                    throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_UPDATE_COMMIT_TEXT"));
                }
            }
        }
        return list;
    }

    public static List<String> doUpdateAll(File repository, boolean bForce, String revision) throws HgException {
        return doUpdateAll(repository, bForce, revision, true);
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
    public static List<String> doRollback(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ROLLBACK_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ROLLBACK_FAILED"), logger);

        return list;
    }
    public static List<String> doBackout(File repository, String revision,
            boolean doMerge, String commitMsg, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> env = new ArrayList<String>();
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BACKOUT_CMD);
        if(doMerge){
            command.add(HG_BACKOUT_MERGE_CMD);
            env.add(HG_MERGE_ENV);
        }

        if (commitMsg != null && !commitMsg.equals("")) { // NOI18N
            command.add(HG_BACKOUT_COMMIT_MSG_CMD);
            command.add(commitMsg);
        } else {
            command.add(HG_BACKOUT_COMMIT_MSG_CMD);
            command.add(NbBundle.getMessage(HgCommand.class, "MSG_BACKOUT_MERGE_COMMIT_MSG", revision));  // NOI18N
        }

        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(HG_REV_CMD);
            command.add(revision);
        }

        List<String> list;
        if(doMerge){
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_BACKOUT_FAILED"), logger);

        return list;
    }

    public static List<String> doStrip(File repository, String revision,
            boolean doForceMultiHead, boolean doBackup, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CONFIG_OPTION_CMD);
        command.add(HG_STRIP_EXT_CMD);
        command.add(HG_STRIP_CMD);
        if(doForceMultiHead){
            command.add(HG_STRIP_FORCE_MULTIHEAD_CMD);
        }
        if(!doBackup){
            command.add(HG_STRIP_NOBACKUP_CMD);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (revision != null){
            command.add(revision);
        }

        List<String> list = exec(command);
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_STRIP_FAILED"), logger);

        return list;
    }

        public static List<String> doVerify(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VERIFY_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_VERIFY_FAILED"), logger);

        return list;
    }

    /**
     * Return the version of hg, e.g. "0.9.3". // NOI18N
     *
     * @return String
     */
    public static String getHgVersion() {
        List<String> list;
        try {
            list = execForVersionCheck();
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
    public static List<String> doPull(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_PULL_CMD);
        command.add(HG_UPDATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list;
        String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
        String proxy = getGlobalProxyIfNeeded(defaultPull, true, logger);
        if(proxy != null){
            List<String> env = new ArrayList<String>();
            env.add(HG_PROXY_ENV + proxy);
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }

        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return list;
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
    public static List<String> doPull(File repository, HgURL from, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || from == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_PULL_CMD;
        command.logger = logger;
        command.remoteUrl = from;
        command.repository = repository;
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.additionalOptions.add(HG_UPDATE_CMD);

        List<String> retval = command.invoke();
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL);

        return retval;
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
    public static List<String> doUnbundle(File repository, File bundle, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_UNBUNDLE_CMD);
        command.add(HG_UPDATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if (bundle != null) {
            command.add(bundle.getAbsolutePath());
        }

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return list;
    }

    /**
     * Show the changesets that would be pulled if a pull
     * was requested from the default pull location
     *
     * @param File repository of the mercurial repository's root directory
     * @return hg incoming output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncoming(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        List<Object> command = new ArrayList<Object>();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> cmdOutput;
        String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
        String proxy = getGlobalProxyIfNeeded(defaultPull, false, null);
        if(proxy != null){
            List<String> env = new ArrayList<String>();
            env.add(HG_PROXY_ENV + proxy);
            cmdOutput = execEnv(command, env);
        }else{
            cmdOutput = exec(command);
        }

        if (!cmdOutput.isEmpty() &&
             isErrorAbort(cmdOutput.get(cmdOutput.size() -1))) {
            handleError(command, cmdOutput, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
        }
        return cmdOutput;
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
    public static List<String> doIncoming(File repository, HgURL from, File bundle, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || from == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_INCOMING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = from;
        command.repository = repository;
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.showSaveOption = showSaveCredentialsOption;
        if (bundle != null) {
            command.additionalOptions.add(HG_OPT_BUNDLE);
            command.additionalOptions.add(bundle.getAbsolutePath());
        }

        List<String> retval = command.invoke();
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL);

        return retval;
    }

    /**
     * Show the changesets that would be pushed if a push
     * was requested to the specified local source repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to query
     * @return hg outgoing output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doOutgoing(File repository, HgURL toUrl, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || toUrl == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPush(false);
        command.hgCommandType = HG_OUTGOING_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = toUrl;
        command.repository = repository;
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.additionalOptions.add(HG_LOG_TEMPLATE_HISTORY_NO_FILEINFO_CMD);
        command.showSaveOption = showSaveCredentialsOption;

        List<String> retval = command.invoke();
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PUSH);

        return retval;
    }

    /**
     * Push changes to the specified repository
     * By default, push will refuse to run if doing so would create multiple heads
     *
     * @param File repository of the mercurial repository's root directory
     * @param String source repository to push to
     * @return hg push output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doPush(File repository, final HgURL toUrl, OutputLogger logger, boolean showSaveCredentialsOption) throws HgException {
        if (repository == null || toUrl == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.acquireCredentialsFirst = true;
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPush(false);
        command.hgCommandType = HG_PUSH_CMD;
        command.logger = logger;
        command.remoteUrl = toUrl;
        command.repository = repository;

        List<String> retval = command.invoke();
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PUSH);

        return retval;
    }

    /**
     * Run the command hg view for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doView(File repository, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        List<String> command = new ArrayList<String>();
        List<String> env = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VIEW_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list;

        if(HgUtils.isSolaris()){
            env.add(HG_HGK_PATH_SOLARIS10_ENV);
            list = execEnv(command, env);
        }else{
            list = exec(command);
        }

        if (!list.isEmpty()) {
            if (isErrorNoView(list.get(list.size() -1))) {
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_NO_VIEW_TEXT"));
             }
            else if (isErrorHgkNotFound(list.get(0)) || isErrorNoSuchFile(list.get(0))) {
                OutputLogger.getLogger(repository.getAbsolutePath()).outputInRed(list.toString());
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_WARN_HGK_NOT_FOUND_TEXT"));
            } else if (isErrorAbort(list.get(list.size() -1))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
            }
        }
        return list;
    }

    private static String getGlobalProxyIfNeeded(String defaultPath, boolean bOutputDetails, OutputLogger logger){
        String proxy = null;
        if(defaultPath != null &&
                (defaultPath.startsWith("http:") || defaultPath.startsWith("https:"))){ // NOI18N
            HgProxySettings ps = new HgProxySettings();
            if (ps.isManualSetProxy()) {
                if ((defaultPath.startsWith("http:") && !ps.getHttpHost().equals(""))||
                    (defaultPath.startsWith("https:") && !ps.getHttpHost().equals("") && ps.getHttpsHost().equals(""))) { // NOI18N
                    proxy = ps.getHttpHost();
                    if (proxy != null && !proxy.equals("")) {
                        proxy += ps.getHttpPort() > -1 ? ":" + Integer.toString(ps.getHttpPort()) : ""; // NOI18N
                    } else {
                        proxy = null;
                    }
                } else if (defaultPath.startsWith("https:") && !ps.getHttpsHost().equals("")) { // NOI18N
                    proxy = ps.getHttpsHost();
                    if (proxy != null && !proxy.equals("")) {
                        proxy += ps.getHttpsPort() > -1 ? ":" + Integer.toString(ps.getHttpsPort()) : ""; // NOI18N
                    } else {
                        proxy = null;
                    }
                }
            }
        }
        if(proxy != null && bOutputDetails){
            logger.output(NbBundle.getMessage(HgCommand.class, "MSG_USING_PROXY_INFO", proxy)); // NOI18N
        }
        return proxy;
    }
    /**
     * Run the fetch extension for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doFetch(File repository, HgURL from, OutputLogger logger) throws HgException {
        if (repository == null || from == null) return null;

        InterRepositoryCommand command = new InterRepositoryCommand();
        command.defaultUrl = new HgConfigFiles(repository).getDefaultPull(false);
        command.hgCommandType = HG_FETCH_CMD;
        command.logger = logger;
        command.outputDetails = false;
        command.remoteUrl = from;
        command.repository = repository;
        command.additionalOptions.add(HG_VERBOSE_CMD);
        command.additionalOptions.add(HG_CONFIG_OPTION_CMD);
        command.additionalOptions.add(HG_FETCH_EXT_CMD);
        command.showSaveOption = true;

        List<String> retval = command.invoke();
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        command.saveCredentials(HgConfigFiles.HG_DEFAULT_PULL);

        return retval;
    }

    public static List<HgLogMessage> processLogMessages(File root, List<File> files, List<String> list, final List<HgLogMessage> messages) {
        return processLogMessages(root, files, list, messages, false);
    }

    public static List<HgLogMessage> processLogMessages(File root, List<File> files, List<String> list, final List<HgLogMessage> messages, boolean revertOrder) {
        String rev, author, desc, date, id, parents, fm, fa, fd, fc;
        List<String> filesShortPaths = new ArrayList<String>();

        final String rootPath = root.getAbsolutePath();

        if (list != null && !list.isEmpty()) {
            if (files != null) {
                for (File f : files) {
                    String shortPath = f.getAbsolutePath();
                    if (shortPath.startsWith(rootPath) && shortPath.length() > rootPath.length()) {
                        if (Utilities.isWindows()) {
                            filesShortPaths.add(shortPath.substring(rootPath.length() + 1).replace(File.separatorChar, '/')); // NOI18N
                        } else {
                            filesShortPaths.add(shortPath.substring(rootPath.length() + 1)); // NOI18N
                        }
                    }
                }
            }

            rev = author = desc = date = id = parents = fm = fa = fd = fc = null;
            boolean bEnd = false;
            boolean stillInMessage = false; // commit message can have multiple lines !!!
            for (String s : list) {
                if (s.indexOf(HG_LOG_REVISION_OUT) == 0) {
                    rev = s.substring(HG_LOG_REVISION_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_AUTHOR_OUT) == 0) {
                    author = s.substring(HG_LOG_AUTHOR_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_DESCRIPTION_OUT) == 0) {
                    desc = s.substring(HG_LOG_DESCRIPTION_OUT.length()).trim();
                    stillInMessage = true;
                } else if (s.indexOf(HG_LOG_DATE_OUT) == 0) {
                    date = s.substring(HG_LOG_DATE_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_ID_OUT) == 0) {
                    id = s.substring(HG_LOG_ID_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_PARENTS_OUT) == 0) {
                    parents = s.substring(HG_LOG_PARENTS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEMODS_OUT) == 0) {
                    fm = s.substring(HG_LOG_FILEMODS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEADDS_OUT) == 0) {
                    fa = s.substring(HG_LOG_FILEADDS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILEDELS_OUT) == 0) {
                    fd = s.substring(HG_LOG_FILEDELS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_FILECOPIESS_OUT) == 0) {
                    fc = s.substring(HG_LOG_FILECOPIESS_OUT.length()).trim();
                    stillInMessage = false;
                } else if (s.indexOf(HG_LOG_ENDCS_OUT) == 0) {
                    stillInMessage = false;
                    bEnd = true;
                } else {
                    if (stillInMessage) {
                        // add next lines of commit message
                        desc += "\n" + s;
                    }
                }

                if (rev != null & bEnd) {
                    HgLogMessage hgMsg = new HgLogMessage(rootPath, filesShortPaths, rev, author, desc, date, id, parents, fm, fa, fd, fc);
                    if (revertOrder) {
                        messages.add(0, hgMsg);
                    } else {
                        messages.add(hgMsg);
                    }
                    rev = author = desc = date = id = parents = fm = fa = fd = fc = null;
                    bEnd = false;
                }
            }
        }
        return messages;
    }

    public static HgLogMessage[] getIncomingMessages(final File root, String toRevision, boolean bShowMerges,  OutputLogger logger) {
        final List<HgLogMessage> messages = new ArrayList<HgLogMessage>(0);

        try {
            List<String> list = HgCommand.doIncomingForSearch(root, toRevision, bShowMerges, logger);
            processLogMessages(root, null, list, messages, true);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.closeLog();
        }

        return messages.toArray(new HgLogMessage[0]);
    }

    public static HgLogMessage[] getOutMessages(final File root, String toRevision, boolean bShowMerges, OutputLogger logger) {
        final List<HgLogMessage> messages = new ArrayList<HgLogMessage>(0);

        try {
            List<String> list = HgCommand.doOutForSearch(root, toRevision, bShowMerges, logger);
            processLogMessages(root, null, list, messages, true);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.closeLog();
        }

        return messages.toArray(new HgLogMessage[0]);
    }

    public static HgLogMessage[] getLogMessages(final File root, final Set<File> files, String fromRevision, String toRevision, boolean bShowMerges, OutputLogger logger) {
         return getLogMessages(root, files, fromRevision, toRevision,
                                bShowMerges, true, -1, logger, true);
    }

    public static HgLogMessage[] getLogMessagesNoFileInfo(final File root, final Set<File> files, String fromRevision, String toRevision, boolean bShowMerges, int limitRevisions, OutputLogger logger) {
         return getLogMessages(root, files, fromRevision, toRevision, bShowMerges, false, limitRevisions, logger, true);
    }

    public static HgLogMessage[] getLogMessagesNoFileInfo(final File root, final Set<File> files, int limit, OutputLogger logger) {
         return getLogMessages(root, files, "0", HG_STATUS_FLAG_TIP_CMD, true, false, limit, logger, false);
    }

    public static HgLogMessage[] getLogMessages(final File root,
            final Set<File> files, String fromRevision, String toRevision,
            boolean bShowMerges,  boolean bGetFileInfo, int limit, OutputLogger logger, boolean ascOrder) {
        final List<HgLogMessage> messages = new ArrayList<HgLogMessage>(0);

        try {
            String headRev = HgCommand.getLastRevision(root, null);
            if (headRev == null) {
                return messages.toArray(new HgLogMessage[0]);
            }

            List<String> list = new LinkedList<String>();
            List<File> filesList = files != null ? new ArrayList<File>(files) : null;
            list = HgCommand.doLog(root,
                    filesList,
                    fromRevision, toRevision, headRev, bShowMerges, bGetFileInfo, limit, logger);
            processLogMessages(root, filesList, list, messages, ascOrder);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            logger.closeLog();
        }

        return messages.toArray(new HgLogMessage[0]);
   }

    /**
     * Determines whether anything has been committed to the repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return Boolean which is true if the repository has revision history.
     */
    public static Boolean hasHistory(File repository) {
        if (repository == null ) return false;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_ONE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        try {
            List<String> list = exec(command);
            if (!list.isEmpty() && isErrorAbort(list.get(0)))
                return false;
            else
                return !list.isEmpty();
        } catch (HgException e) {
            return false;
        }
    }

    /**
     * Determines the previous name of the specified file
     * We make the assumption that the previous file name is in the
     * cmdOutput of files returned by hg log command immediately befor
     * the file we started with.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose previous name is required
     * @param String revision which the revision to start from.
     * @return File for the previous name of the file
     */
    private static File getPreviousName(File repository, File file, String revision) throws HgException {
        if (repository == null ) return null;
        if (revision == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_FOLLOW);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_REV_CMD);
        command.add(revision);

        List<String> list = null;
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_ONLY_FILES_CHANGESET_NAME));
            command.add(file.getAbsolutePath());
            list = exec(command);
            if (list.isEmpty() || isErrorAbort(list.get(0))) {
                return null;
            }
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } catch (HgException e) {
            Mercurial.LOG.log(Level.WARNING, "command: " + HgUtils.replaceHttpPassword(command)); // NOI18N
            Mercurial.LOG.log(Level.WARNING, null, e); // NOI18N
            throw new HgException(e.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
        String[] fileNames = list.get(0).split("\t");
        for (int j = fileNames.length -1 ; j > 0; j--) {
            File name = new File(repository, fileNames[j]);
            if (name.equals(file)) {
               return new File(repository, fileNames[j-1]);
            }
        }
        return null;
    }

    /**
     * Retrives the log information for the specified files.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<File> of files which revision history is to be retrieved.
     * @param String Template specifying how output should be returned
     * @param boolean flag indicating if debug param should be used - required to get all file mod, add, del info
     * @return List<String> cmdOutput of the log entries for the specified file.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    private static List<String> doLog(File repository, List<File> files,
            String from, String to, String headRev, boolean bShowMerges, boolean bGetFileInfo, int limit, OutputLogger logger) throws HgException {
        if (repository == null ) return null;
        if (files != null && files.isEmpty()) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_VERBOSE_CMD);
        command.add(HG_LOG_CMD);
        if (limit >= 0) {
                command.add(HG_LOG_LIMIT_CMD);
                command.add(Integer.toString(limit));
        }
        boolean doFollow = true;
        if( files != null){
            for (File f : files) {
                if (f.isDirectory()) {
                    doFollow = false;
                    break;
                }
            }
        }
        if (doFollow) {
            command.add(HG_OPT_FOLLOW);
        }
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        if(bGetFileInfo){
            command.add(HG_LOG_DEBUG_CMD);
        }

        String dateStr = handleRevDates(from, to);
        if(dateStr != null){
            command.add(HG_FLAG_DATE_CMD);
            command.add(dateStr);
        }
        String revStr = handleRevNumbers(from, to, headRev);
        if(dateStr == null && revStr != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revStr);
        }

        // Make sure revsions listed from "tip" down to "tip - limit"
        if(limit >= 0 && dateStr == null && revStr == null){
            command.add(HG_FLAG_REV_CMD);
            command.add(HG_LOG_REV_TIP_RANGE);
        }

        File tempFolder = Utils.getTempFolder(false);
        try {
            if (bGetFileInfo) {
                command.add(prepareLogTemplate(tempFolder, HG_LOG_FULL_CHANGESET_NAME));
            } else {
                command.add(HG_LOG_TEMPLATE_HISTORY_NO_FILEINFO_CMD);
            }

            if (files != null) {
                for (File f : files) {
                    command.add(f.getAbsolutePath());
                }
            }
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    /**
     * Retrives the tip information for the specified repository, as defined by the LOG_TEMPLATE.
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the log entries for the tip
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static HgLogMessage doTip(File repository, OutputLogger logger) throws HgException {
        if (repository == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_TIP_CMD);
        command.add(HG_LOG_TEMPLATE_HISTORY_NO_FILEINFO_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
             } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
             }
        }
        List<HgLogMessage> messages = new ArrayList<HgLogMessage>(1);
        messages = processLogMessages(repository, null, list, messages, false);

        return messages.get(0);
    }


    /**
     * Retrives the Out information for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the out entries for the specified repo.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doOutForSearch(File repository, String to, boolean bShowMerges, OutputLogger logger) throws HgException {
        if (repository == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_OUT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        command.add(HG_LOG_DEBUG_CMD);
        String revStr = handleIncomingRevNumber(to);
        if(revStr != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revStr);
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_FULL_CHANGESET_NAME));
            List<String> list;
            String defaultPush = new HgConfigFiles(repository).getDefaultPush(false);
            String proxy = getGlobalProxyIfNeeded(defaultPush, false, null);
            if(proxy != null){
                List<String> env = new ArrayList<String>();
                env.add(HG_PROXY_ENV + proxy);
                list = execEnv(command, env);
            }else{
                list = exec(command);
            }
            if (!list.isEmpty()) {
                if(isErrorNoDefaultPush(list.get(0))){
                    // Ignore
                }else if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

        /**
     * Retrives the Incoming changeset information for the specified repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return List<String> cmdOutput of the out entries for the specified repo.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doIncomingForSearch(File repository, String to, boolean bShowMerges, OutputLogger logger) throws HgException {
        if (repository == null ) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_INCOMING_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_NEWEST_FIRST);
        if(!bShowMerges){
            command.add(HG_LOG_NO_MERGES_CMD);
        }
        command.add(HG_LOG_DEBUG_CMD);
        String revStr = handleIncomingRevNumber(to);
        if(revStr != null){
            command.add(HG_FLAG_REV_CMD);
            command.add(revStr);
        }
        File tempFolder = Utils.getTempFolder(false);
        try {
            command.add(prepareLogTemplate(tempFolder, HG_LOG_FULL_CHANGESET_NAME));
            List<String> list;
            String defaultPull = new HgConfigFiles(repository).getDefaultPull(false);
            String proxy = getGlobalProxyIfNeeded(defaultPull, false, null);
            if (proxy != null) {
                List<String> env = new ArrayList<String>();
                env.add(HG_PROXY_ENV + proxy);
                list = execEnv(command, env);
            } else {
                list = exec(command);
            }

            if (!list.isEmpty()) {
                if (isErrorNoDefaultPath(list.get(0))) {
                    // Ignore
                } else if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorAbort(list.get(0)) || isErrorAbort(list.get(list.size() - 1))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                }
            }
            return list;
        } catch (IOException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
            throw new HgException(ex.getMessage());
        } finally {
            Utils.deleteRecursively(tempFolder);
        }
    }

    private static String handleRevDates(String from, String to){
        // Check for Date range:
        Date fromDate = null;
        Date toDate = null;
        Date currentDate = new Date(); // Current Date
        Date epochPlusOneDate = null;

        try {
            epochPlusOneDate = new SimpleDateFormat("yyyy-MM-dd").parse(HG_EPOCH_PLUS_ONE_YEAR); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // Set From date
        try {
            if(from != null)
                fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(from); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // Set To date
        try {
            if(to != null)
                toDate = new SimpleDateFormat("yyyy-MM-dd").parse(to); // NOI18N
        } catch (ParseException ex) {
            // Ignore invalid dates
        }

        // If From date is set, but To date is not - default To date to current date
        if( fromDate != null && toDate == null && to == null){
            toDate = currentDate;
            to = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
        }
        // If To date is set, but From date is not - default From date to 1971-01-01
        if (fromDate == null && from == null  && toDate != null) {
            fromDate = epochPlusOneDate;
            from = HG_EPOCH_PLUS_ONE_YEAR; // NOI18N
        }

        // If using dates make sure both From and To are set to dates
        if( (fromDate != null && toDate == null && to != null) ||
                (fromDate == null && from != null && toDate != null)){
            HgUtils.warningDialog(HgCommand.class,"MSG_SEARCH_HISTORY_TITLE",// NOI18N
                    "MSG_SEARCH_HISTORY_WARN_BOTHDATES_NEEDED_TEXT");   // NOI18N
            return null;
        }

        if(fromDate != null && toDate != null){
            // Check From date - default to 1971-01-01 if From date is earlier than this
            if(epochPlusOneDate != null && fromDate.before(epochPlusOneDate)){
                fromDate = epochPlusOneDate;
                from = HG_EPOCH_PLUS_ONE_YEAR; // NOI18N
            }
            // Set To date - default to current date if To date is later than this
            if(currentDate != null && toDate.after(currentDate)){
                toDate = currentDate;
                to = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
            }

            // Make sure the From date is before the To date
            if( fromDate.after(toDate)){
                HgUtils.warningDialog(HgCommand.class,"MSG_SEARCH_HISTORY_TITLE",// NOI18N
                        "MSG_SEARCH_HISTORY_WARN_FROM_BEFORE_TODATE_NEEDED_TEXT");   // NOI18N
                return null;
            }
            return from + " to " + to; // NOI18N
        }
        return null;
    }

    private static String handleIncomingRevNumber(String to) {
        int toInt = -1;

        // Handle users entering head or tip for revision, instead of a number
        if (to != null && (to.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || to.equalsIgnoreCase(HG_HEAD_STR))) {
            to = HG_STATUS_FLAG_TIP_CMD;
        }
        try {
            toInt = Integer.parseInt(to);
        } catch (NumberFormatException e) {
            // ignore invalid numbers
        }

        return (toInt > -1) ? to : HG_STATUS_FLAG_TIP_CMD;
    }

    private static String handleRevNumbers(String from, String to, String headRev){
        int fromInt = -1;
        int toInt = -1;
        int headRevInt = -1;

        // Handle users entering head or tip for revision, instead of a number
        if(from != null && (from.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || from.equalsIgnoreCase(HG_HEAD_STR)))
            from = headRev;
        if(to != null && (to.equalsIgnoreCase(HG_STATUS_FLAG_TIP_CMD) || to.equalsIgnoreCase(HG_HEAD_STR)))
            to = headRev;

        try{
            fromInt = Integer.parseInt(from);
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }
        try{
            toInt = Integer.parseInt(to);
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }
        try{
            headRevInt = Integer.parseInt(headRev);
        }catch (NumberFormatException e){
            // ignore invalid numbers
        }

        // Handle out of range revisions
        if (headRevInt > -1 && toInt > headRevInt) {
            to = headRev;
            toInt = headRevInt;
        }
        if (headRevInt > -1 && fromInt > headRevInt) {
            from = headRev;
            fromInt = headRevInt;
        }

        // Handle revision ranges
        String revStr = null;
        if (fromInt > -1 && toInt > -1){
            revStr = to + ":" + from;
        }else if (fromInt > -1){
            revStr = (headRevInt != -1 ? headRevInt + ":" : "tip:") + from;
        }else if (toInt > -1){
            revStr = to + ":0";
        }

        if(revStr == null) {
            if(to == null) {
                to = HG_STATUS_FLAG_TIP_CMD;
            }
            if(from == null) {
                from = "0";
            }
            revStr = to + ":" + from;
        }

        return revStr;
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
    public static void doCat(File repository, File file, File outFile, OutputLogger logger) throws HgException {
        doCat(repository, file, outFile, null, false, logger); //NOI18N
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
     * @return List<String> cmdOutput of all the log entries
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCat(File repository, File file, File outFile, String revision, OutputLogger logger) throws HgException {
        doCat(repository, file, outFile, revision, true, logger); //NOI18N
    }

    public static void doCat(File repository, File file, File outFile, String revision, boolean retry, OutputLogger logger) throws HgException {
        if (repository == null) return;
        if (file == null) return;

        List<String> command = new ArrayList<String>();

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
        try {
            // cmd returns error if there are simlinks in absolute path and file is deleted
            // abort: /path/file not under root
            command.add(file.getCanonicalPath());
        } catch (IOException e) {
            Mercurial.LOG.log(Level.WARNING, "command: " + HgUtils.replaceHttpPassword(command)); // NOI18N
            Mercurial.LOG.log(Level.WARNING, null, e); // NOI18N
            throw new HgException(e.getMessage());
        }
        List<String> list = exec(command);

        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
             } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
             }
        }
        if (outFile.length() == 0 && retry) {
            // Perhaps the file has changed its name
            String newRevision = Integer.toString(Integer.parseInt(revision)+1);
            File prevFile = getPreviousName(repository, file, newRevision);
            if (prevFile != null) {
                doCat(repository, prevFile, outFile, revision, false, logger); //NOI18N
            }
        }
    }

    /**
     * Get common ancestor for two provided revisions.
     *
     * @param root for the mercurial repository
     * @param first rev to get ancestor for
     * @param second rev to get ancestor for
     * @param output logger
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getCommonAncestor(String rootURL, String rev1, String rev2, OutputLogger logger) throws HgException {
        String res = getCommonAncestor(rootURL, rev1, rev2, false, logger);
        if( res == null){
            res = getCommonAncestor(rootURL, rev1, rev2, true, logger);
        }
        return res;
    }

    private static String getCommonAncestor(String rootURL, String rev1, String rev2, boolean bUseIndex, OutputLogger logger) throws HgException {
        if (rootURL == null ) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_COMMONANCESTOR_CMD);
        if(bUseIndex){
            command.add(".hg/store/00changelog.i"); //NOI18N
        }
        command.add(rev1);
        command.add(rev2);
        command.add(HG_OPT_REPOSITORY);
        command.add(rootURL);
        command.add(HG_OPT_CWD_CMD);
        command.add(rootURL);

        List<String> list = exec(command);
        if (!list.isEmpty()){
            String splits[] = list.get(0).split(":"); // NOI18N
            String tmp = splits != null && splits.length >= 1 ? splits[0]: null;
            int tmpRev = -1;
            try{
                tmpRev = Integer.parseInt(tmp);
            }catch(NumberFormatException ex){
                // Ignore
            }
            return tmpRev > -1? tmp: null;
        }else{
            return null;
        }
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
    public static void doCreate(File root, OutputLogger logger) throws HgException {
        if (root == null ) return;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CREATE_CMD);
        command.add(root.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty())
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_CREATE_FAILED"), logger);
    }

    /**
     * Clone an exisiting repository to the specified target directory
     *
     * @param File repository of the mercurial repository's root directory
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(File repository, File target, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        return doClone(new HgURL(repository), target, logger);
    }

    /**
     * Clone a repository to the specified target directory
     *
     * @param String repository of the mercurial repository
     * @param target directory to clone to
     * @return clone output
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doClone(HgURL repository, File target, OutputLogger logger) throws HgException {
        if (repository == null || target == null) return null;

        // Ensure that parent directory of target exists, creating if necessary
        File parentTarget = target.getParentFile();
        try {
            if (!parentTarget.mkdirs()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : " + parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : " + parentTarget.getAbsolutePath() + " threw SecurityException " + e.getMessage()); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }

        List<String> list = null;
        boolean retry = true;
        // acquire credentials for kenai
        PasswordAuthentication credentials = null;
        HgKenaiSupport supp = HgKenaiSupport.getInstance();
        String rawUrl = repository.toUrlStringWithoutUserInfo();
        if (supp.isKenai(rawUrl) && supp.isLoggedIntoKenai()) {
            credentials = supp.getPasswordAuthentication(rawUrl, false);
        }

        HgURL url = repository;
        while (retry) {
            retry = false;
            try {
                if (credentials != null) {
                    url = new HgURL(repository.toHgCommandUrlString(), credentials.getUserName(), new String(credentials.getPassword()));
                }
            } catch (URISyntaxException ex) {
                // this should NEVER happen
                Mercurial.LOG.log(Level.SEVERE, null, ex);
                break;
            }
            List<Object> command = new ArrayList<Object>();

            command.add(getHgCommand());
            command.add(HG_CLONE_CMD);
            command.add(HG_VERBOSE_CMD);
            command.add(url);
            command.add(target);

            list = exec(command);
            if (!list.isEmpty()) {
                if (isErrorNoRepository(list.get(0))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
                } else if (isErrorNoResponse(list.get(list.size() - 1))) {
                    handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_RESPONSE_ERR"), logger);
                } else if (isErrorAbort(list.get(list.size() - 1))) {
                    if ((credentials = handleAuthenticationError(list, target, rawUrl, credentials == null ? "" : credentials.getUserName(), new UserCredentialsSupport())) != null) { //NOI18N
                        // try again with new credentials
                        retry = true;
                    } else {
                        handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Commits the cmdOutput of Locally Changed files to the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<files> of files to be committed to hg
     * @param String for commitMessage
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doCommit(File repository, List<File> commitFiles, String commitMessage, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_COMMIT_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        String projectUserName = new HgConfigFiles(repository).getUserName(false);
        String globalUsername = HgModuleConfig.getDefault().getSysUserName();
        String username = null;
        if(projectUserName != null && projectUserName.length() > 0)
            username = projectUserName;
        else if (globalUsername != null && globalUsername.length() > 0)
           username = globalUsername;

        if(username != null ){
            command.add(HG_OPT_USERNAME);
            command.add(username);
        }

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
                if (f.getAbsolutePath().length() >= repository.getAbsolutePath().length()) {
                    // list contains the root itself
                    command.add(f.getAbsolutePath());
                } else {
                    command.add(f.getAbsolutePath().substring(repository.getAbsolutePath().length()+1));
                }
            }
            if(Utilities.isWindows()) {
                int size = 0;
                // Count size of command
                for (String line : command) {
                    size += line.length();
                }
                if (isTooLongCommand(size)) {
                    throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_ARG_LIST_TOO_LONG_ERR", command.get(1), command.size() -2 )); //NOI18N
                }
            }
            List<String> list = exec(command);
            //#132984: range of issues with upgrade to Hg 1.0, new restriction whereby you cannot commit using explicit file names after a merge.
            if (!list.isEmpty() && isCommitAfterMerge(list.get(list.size() -1))) {
                throw new HgException(COMMIT_AFTER_MERGE);
            }

            if (!list.isEmpty()
                    && (isErrorNotTracked(list.get(0)) ||
                    isErrorCannotReadCommitMsg(list.get(0)) ||
                    isErrorAbort(list.get(list.size() -1)) ||
                    isErrorAbort(list.get(0))))
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMIT_FAILED"), logger);

        }catch (IOException ex){
            throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_FAILED_TO_READ_COMMIT_MESSAGE"));
        }finally{
            if (commitMessage != null && tempfile != null){
                tempfile.delete();
            }
        }
    }


    /**
     * Rename a source file to a destination file.
     * mercurial hg rename
     *
     * @param File repository of the mercurial repository's root directory
     * @param File of sourceFile which was renamed
     * @param File of destFile to which sourceFile has been renaned
     * @param boolean whether to do a rename --after
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRename(File repository, File sourceFile, File destFile, OutputLogger logger)  throws HgException {
        doRename(repository, sourceFile, destFile, false, logger);
    }

    private static void doRename(File repository, File sourceFile, File destFile, boolean bAfter, OutputLogger logger)  throws HgException {
        if (repository == null) return;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_RENAME_CMD);
        if (bAfter) command.add(HG_RENAME_AFTER_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());

        command.add(sourceFile.getAbsolutePath().substring(repository.getAbsolutePath().length()+1));
        command.add(destFile.getAbsolutePath().substring(repository.getAbsolutePath().length()+1));

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            if (!bAfter || !isErrorAbortNoFilesToCopy(list.get(list.size() -1))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_RENAME_FAILED"), logger);
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
    public static void doRenameAfter(File repository, File sourceFile, File destFile, OutputLogger logger)  throws HgException {
       doRename(repository, sourceFile, destFile, true, logger);
    }

    /**
     * Adds the cmdOutput of Locally New files to the mercurial Repository
     * Their status will change to added and they will be added on the next
     * mercurial hg add.
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be added to hg
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doAdd(File repository, List<File> addFiles, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (addFiles.size() == 0) return;
        List<String> basicCommand = new ArrayList<String>();
        int basicCommandSize = 0, commandSize = 0; // limitation for windows max command line size - #168155
        basicCommand.add(getHgCommand());
        basicCommand.add(HG_ADD_CMD);
        basicCommand.add(HG_OPT_REPOSITORY);
        basicCommand.add(repository.getAbsolutePath());
        for (String s : basicCommand) {
            basicCommandSize += s.length() + 1;
        }
        // iterating through all files, cannot add all files immediately, too many files can cause troubles
        // adding files to the command one by one and testing if the command's size doesn't exceed OS limits
        ListIterator<File> iterator = addFiles.listIterator();
        while (iterator.hasNext()) {
            // each loop will call one add command
            List<String> command = new LinkedList<String>(basicCommand);
            commandSize = basicCommandSize;
            boolean fileAdded = false;
            while (iterator.hasNext()) {
                File f = iterator.next();
                if (f.isDirectory()) {
                    continue;
                }
                // test if limits aren't exceeded
                commandSize += f.getAbsolutePath().length() + 1;
                if (fileAdded // at least one file must be added
                        && isTooLongCommand(commandSize)) {
                    Mercurial.LOG.fine("doAdd: adding files in loop");  //NOI18N
                    iterator.previous();
                    break;
                }
                // We do not look for files to ignore as we should not here
                // with a file to be ignored.
                command.add(f.getAbsolutePath());
                fileAdded = true;
            }
            List<String> list = exec(command);
            if (!list.isEmpty() && !isErrorAlreadyTracked(list.get(0))) {
                handleError(command, list, list.get(0), logger);
            }
        }
    }

    /**
     * Reverts the cmdOutput of files in the mercurial Repository to the specified revision
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be reverted
     * @param String revision to be reverted to
     * @return void
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRevert(File repository, List<File> revertFiles,
            String revision, boolean doBackup, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (revertFiles.size() == 0) return;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_REVERT_CMD);
        if(!doBackup){
            command.add(HG_REVERT_NOBACKUP_CMD);
        }
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
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_REVERT_FAILED"), logger);
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
    public static void doAdd(File repository, File file, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (file == null) return;
        if (file.isDirectory()) return;
        // We do not look for file to ignore as we should not here
        // with a file to be ignored.

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ADD_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ALREADY_TRACKED"), logger);
    }

    /**
     * Get the annotations for the specified file
     *
     * @param File repository of the mercurial repository
     * @param File file to be annotated
     * @param String revision of the file to be annotated
     * @return List<String> cmdOutput of the annotated lines of the file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doAnnotate(File repository, File file, String revision, OutputLogger logger) throws HgException {
        if (repository == null) return null;
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_ANNOTATE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());

        if (revision != null) {
            command.add(HG_FLAG_REV_CMD);
            command.add(revision);
        }
        command.add(HG_ANNOTATE_FLAGN_CMD);
        command.add(HG_ANNOTATE_FLAGU_CMD);
        command.add(HG_OPT_FOLLOW);
        command.add(file.getAbsolutePath());
        List<String> list = exec(command);
        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
            } else if (isErrorNoSuchFile(list.get(0))) {
                // This can happen if we have multiple heads and the wrong
                // one was picked by default hg annotation
                if (revision == null) {
                    String rev = getLastRevision(repository, file);
                    if (rev != null) {
                        list = doAnnotate(repository, file, rev, logger);
                    } else {
                        list = null;
                    }
                } else {
                    list = null;
                }
            }
        }
        return list;
    }

    public static List<String> doAnnotate(File repository, File file, OutputLogger logger) throws HgException {
        return doAnnotate(repository, file, null, logger);
    }

    /**
     * Returns the mercurial branch info for a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @return String of form :<branch>:<rev>:<shortchangeset>:
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getBranchInfo(File repository) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_BRANCH_REV_CMD);
        command.add(HG_BRANCH_INFO_TEMPLATE_CMD);
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
     * @return List<String> of revision numbers.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadRevisions(File repository) throws HgException {
        return  getHeadInfo(repository, HG_REV_TEMPLATE_CMD);
    }

    /**
     * Returns the revision number for the heads in a repository
     *
     * @param String repository of the mercurial repository
     * @return List<String> of revision numbers.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> getHeadRevisions(String repository) throws HgException {
        return  getHeadInfo(repository, HG_REV_TEMPLATE_CMD);
    }

    private static List<String> getHeadInfo(String repository, String template) throws HgException {
        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_HEADS_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository);
        command.add(template);

        return exec(command);
    }

    private static List<String> getHeadInfo(File repository, String template) throws HgException {
        if (repository == null) return null;
        return getHeadInfo(repository.getAbsolutePath(), template);
    }

    /**
     * Returns the revision number for the last change to a file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File file of the file whose last revision number is to be returned, if null test for repo
     * @return String in the form of a revision number.
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static String getLastRevision(File repository, File file) throws HgException {
        return  getLastChange(repository, file, HG_REV_TEMPLATE_CMD);
    }

    private static String getLastChange(File repository, File file, String template) throws HgException {

        if (repository == null) return null;

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_LOG_LIMIT_ONE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(template);
        if( file != null)
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
        long startTime = 0;
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINER)) {
            Mercurial.STATUS_LOG.finer("getSingleStatus: starting for " + filename); //NOI18N
            startTime = System.currentTimeMillis();
        }
        List<String> list = doSingleStatusCmd(repository, cwd, filename);
        if(list == null || list.isEmpty())
            return new FileInformation(FileInformation.STATUS_UNKNOWN,null, false);

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
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINER)) {
            Mercurial.STATUS_LOG.finer("getSingleStatus for " + filename + " lasted " + (System.currentTimeMillis() - startTime));
        }
        return info;
    }

    /**
     * Returns the mercurial status for all files in a given  subdirectory of
     * a repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param File dir of the subdirectoy of interest.
     * @return Map of files and status for all files in the specified subdirectory, map contains normalized files as keys
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getAllStatus(File repository, File dir)  throws HgException{
        return getDirStatusWithFlags(repository, dir, HG_STATUS_FLAG_ALL_CMD, true);
    }

    /**
     * Returns the mercurial status for only files of interest to us in a given directory in a repository
     * that is modified, locally added, locally removed, locally deleted, locally new and ignored.
     *
     * @param File repository of the mercurial repository's root directory
     * @param File dir of the directory of interest
     * @return Map of files and status for all files of interest in the directory of interest, map contains normalized files as keys
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getInterestingStatus(File repository, File dir)  throws HgException{
        return getDirStatusWithFlags(repository, dir, HG_STATUS_FLAG_INTERESTING_CMD, true);
    }

    /**
     * Returns the unknown files in a specified directory under a mercurial repository root
     *
     * @param File of the mercurial repository's root directory
     * @param File of the directory whose files are required
     * @return Map of files and status for all files under the repository root, map contains normalized files as keys
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static Map<File, FileInformation> getUnknownStatus(File repository, File dir)  throws HgException{
        Map<File, FileInformation> files = getDirStatusWithFlags(repository, dir, HG_STATUS_FLAG_UNKNOWN_CMD, false);
        int share = SharabilityQuery.getSharability(dir == null ? repository : dir);
        for (Iterator i = files.keySet().iterator(); i.hasNext();) {
            File file = (File) i.next();
            if((share == SharabilityQuery.MIXED && SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE) ||
               (share == SharabilityQuery.NOT_SHARABLE)) {
                i.remove();
             }
        }
        return files;
    }

    /**
     * Remove the specified file from the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param List<Files> of files to be added to hg
     * @param f path to be removed from the repository
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRemove(File repository, List<File> removeFiles, OutputLogger logger)  throws HgException {
        if (repository == null) return;
        if (removeFiles.size() == 0) return;
        List<String> basicCommand = new ArrayList<String>();
        int basicCommandSize = 0, commandSize = 0; // limitation for windows max command line size - #168155
        basicCommand.add(getHgCommand());
        basicCommand.add(HG_REMOVE_CMD);
        basicCommand.add(HG_OPT_REPOSITORY);
        basicCommand.add(repository.getAbsolutePath());
        basicCommand.add(HG_REMOVE_FLAG_FORCE_CMD);
        for (String s : basicCommand) {
            basicCommandSize += s.length() + 1;
        }
        // iterating through all files, cannot remove all files at once, too many files can cause troubles
        // removing files to the command one by one and testing if the command's size doesn't exceed OS limits
        ListIterator<File> iterator = removeFiles.listIterator();
        while (iterator.hasNext()) {
            // each loop will call one remove command
            List<String> command = new LinkedList<String>(basicCommand);
            commandSize = basicCommandSize;
            boolean fileAdded = false;
            while (iterator.hasNext()) {
                File f = iterator.next();
                if (f.isDirectory()) {
                    continue;
                }
                String filePath;
                try {
                    filePath = f.getCanonicalPath();
                } catch (IOException ioe) {
                    Mercurial.LOG.log(Level.INFO, null, ioe); // NOI18N
                    filePath = f.getAbsolutePath(); // don't give up
                }
                // test if limits aren't exceeded
                commandSize += filePath.length() + 1;
                if (fileAdded // at least one file must be added
                        && isTooLongCommand(commandSize)) {
                    Mercurial.LOG.fine("doAdd: removing files in a loop"); //NOI18N
                    iterator.previous();
                    break;
                }
                command.add(filePath);
                fileAdded = true;
            }
            List<String> list = exec(command);
            if (!list.isEmpty()) {
                handleError(command, list, list.get(0), logger);
            }
        }
    }

    /**
     * Remove the specified files from the mercurial Repository
     *
     * @param File repository of the mercurial repository's root directory
     * @param f path to be removed from the repository
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static void doRemove(File repository, File f, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_REMOVE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_REMOVE_FLAG_FORCE_CMD);
        try {
            command.add(f.getCanonicalPath());
        } catch (IOException ioe) {
            Mercurial.LOG.log(Level.WARNING, null, ioe); // NOI18N
            command.add(f.getAbsolutePath()); // don't give up
        }

        List<String> list = exec(command);
        if (!list.isEmpty() && isErrorAlreadyTracked(list.get(0)))
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_ALREADY_TRACKED"), logger);
    }

    /**
     * Export the diffs for the specified revision to the specified output file
    /**
     * Export the diffs for the specified revision to the specified output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revStr the revision whose diffs are to be exported
     * @param outputFileName path of the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doExport(File repository, String revStr, String outputFileName, OutputLogger logger)  throws HgException {
        // Ensure that parent directory of target exists, creating if necessary
        File fileTarget = new File (outputFileName);
        File parentTarget = fileTarget.getParentFile();
        try {
            if (!parentTarget.mkdir()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : " + parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : " + parentTarget.getAbsolutePath() + " threw SecurityException " + e.getMessage()); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_EXPORT_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_FLAG_OUTPUT_CMD);
        command.add(outputFileName);
        if(revStr != null) command.add(revStr);

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_EXPORT_FAILED"), logger);
        }
        return list;
    }

        /**
     * Export the diffs for the specified revision to the specified output file
    /**
     * Export the diffs for the specified revision to the specified output file
     *
     * @param File repository of the mercurial repository's root directory
     * @param revStr the revision whose diffs are to be exported
     * @param outputFileName path of the output file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doExportFileDiff(File repository, File file, String revStr, String outputFileName, OutputLogger logger)  throws HgException {
        // Ensure that parent directory of target exists, creating if necessary
        File fileTarget = new File (outputFileName);
        File parentTarget = fileTarget.getParentFile();
        try {
            if (!parentTarget.mkdir()) {
                if (!parentTarget.isDirectory()) {
                    Mercurial.LOG.log(Level.WARNING, "File.mkdir() failed for : " + parentTarget.getAbsolutePath()); // NOI18N
                    throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
                }
            }
        } catch (SecurityException e) {
            Mercurial.LOG.log(Level.WARNING, "File.mkdir() for : " + parentTarget.getAbsolutePath() + " threw SecurityException " + e.getMessage()); // NOI18N
            throw (new HgException (NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_TO_CREATE_PARENT_DIR"))); // NOI18N
        }
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_LOG_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_REV_CMD);
        command.add(revStr);
        command.add(HG_LOG_TEMPLATE_EXPORT_FILE_CMD);
        command.add(HG_LOG_PATCH_CMD);
        command.add(file.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_EXPORT_FAILED"), logger);
        }else{
            writeOutputFileDiff(list, outputFileName);
        }
        return list;
    }
    private static void writeOutputFileDiff(List<String> list, String outputFileName) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(outputFileName));
            for(String s: list){
                pw.println(s);
                pw.flush();
            }
        } catch (IOException ex) {
            // Ignore
        } finally {
            if(pw != null) pw.close();
        }
    }

    /**
     * Imports the diffs from the specified file
     *
     * @param File repository of the mercurial repository's root directory
     * @param File patchFile of the patch file
     * @throws org.netbeans.modules.mercurial.HgException
     */
    public static List<String> doImport(File repository, File patchFile, OutputLogger logger)  throws HgException {
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_IMPORT_CMD);
        command.add(HG_VERBOSE_CMD);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(HG_OPT_CWD_CMD);
        command.add(repository.getAbsolutePath());
        command.add(patchFile.getAbsolutePath());

        List<String> list = exec(command);
        if (!list.isEmpty() &&
             isErrorAbort(list.get(list.size() -1))) {
            logger.output(list); // need the failure info from import
            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_IMPORT_FAILED"), logger);
        }
        return list;
    }

    private static Map<File, FileInformation> getDirStatusWithFlags(File repository, File dir, String statusFlags, boolean bIgnoreUnversioned)  throws HgException{
        if (repository == null) return null;
        long startTime = 0;
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINER)) {
            Mercurial.STATUS_LOG.finer("getDirStatusWithFlags: starting for " + dir.getAbsolutePath()); //NOI18N
            startTime = System.currentTimeMillis();
        }
        FileInformation prev_info = null;
        List<String> list = doRepositoryDirStatusCmd(repository, dir, statusFlags);

        Map<File, FileInformation> repositoryFiles = new HashMap<File, FileInformation>(list.size());

        File file = null;
        for(String statusLine: list){
            FileInformation info = getFileInformationFromStatusLine(statusLine);
            Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): status line {0}  info {1}", new Object[]{statusLine, info}); // NOI18N
            if (statusLine.length() > 0) {
                if (statusLine.charAt(0) == ' ') {
                    // Locally Added but Copied
                    if (file != null) {
                        prev_info =  new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY,
                                new FileStatus(file, true), false);
                        Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): prev_info {0}  filePath {1}", new Object[]{prev_info, file}); // NOI18N
                    } else {
                        Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): repository path: {0} status flags: {1} status line {2} filepath == nullfor prev_info ", new Object[]{repository.getAbsolutePath(), statusFlags, statusLine}); // NOI18N
                    }
                    continue;
                } else {
                    if (file != null) {
                        repositoryFiles.put(file, prev_info);
                    }
                }
            }
            if(bIgnoreUnversioned){
                if(info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED ||
                        info.getStatus() == FileInformation.STATUS_UNKNOWN) continue;
            }else{
                if(info.getStatus() == FileInformation.STATUS_UNKNOWN) continue;
            }
            StringBuffer sb = new StringBuffer(statusLine);
            sb.delete(0,2); // Strip status char and following 2 spaces: [MARC\?\!I][ ][ ]
            if(Utilities.isWindows() && sb.toString().startsWith(repository.getAbsolutePath())) {
                file = new File(sb.toString());  // prevent bogus paths (C:\tmp\hg\C:\tmp\hg\whatever) - see issue #139500
            } else {
                file = new File(repository, sb.toString());
            }
            file = FileUtil.normalizeFile(file);

            // Handle Conflict Status
            // TODO: remove this if Hg status supports Conflict marker
            if (existsConflictFile(file.getAbsolutePath())) {
                info = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, null, false);
                Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): CONFLICT repository path: {0} status flags: {1} status line {2} CONFLICT {3}", new Object[]{repository.getAbsolutePath(), statusFlags, statusLine, file + HgCommand.HG_STR_CONFLICT_EXT}); // NOI18N
            }
            prev_info = info;
        }
        if (prev_info != null) {
            repositoryFiles.put(file, prev_info);
        }

        if (Mercurial.LOG.isLoggable(Level.FINE)) {
            if (list.size() < 10) {
                Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): repository path: {0} status flags: {1} status list {2}", // NOI18N
                    new Object[] {repository.getAbsolutePath(), statusFlags, list} );
            } else {
                Mercurial.LOG.log(Level.FINE, "getDirStatusWithFlags(): repository path: {0} status flags: {1} status list has {2} elements", // NOI18N
                    new Object[] {repository.getAbsolutePath(), statusFlags, list.size()} );
            }
        }

        if (Mercurial.STATUS_LOG.isLoggable(Level.FINER)) {
            Mercurial.STATUS_LOG.finer("getDirStatusWithFlags for " + dir.getAbsolutePath() + " lasted " + (System.currentTimeMillis() - startTime)); //NOI18N
        }
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
        List<String> command = new ArrayList<String>();
        
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
        String filePath = new File(cwd, filename).getAbsolutePath();
        String repoPath = repository.getAbsolutePath();
        if(repoPath.length() >= filePath.length()) {
            Mercurial.LOG.log(Level.WARNING, "Please report! Wrong repository path: {0}, {1}, {2}", new Object[] {repository, cwd, filename});
            command.add(filePath);
        } else {
            command.add(filePath.substring(repoPath.length() + 1));
        }

        return exec(command);
    }

    /**
     * Gets hg status command output cmdOutput for the specified status flags for a given repository and directory
     */
    private static List<String> doRepositoryDirStatusCmd(File repository, File dir, String statusFlags)  throws HgException{
        List<String> command = new ArrayList<String>();

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
        if (!list.isEmpty() && isErrorNoRepository(list.get(0))) {
            OutputLogger logger = OutputLogger.getLogger(repository.getAbsolutePath());
            try {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
            } finally {
                logger.closeLog();
            }
        }
        return list;
    }

    private static List<String> execEnv(List<? extends Object> command, List<String> env) throws HgException{
        return execEnv(command, env, true);
    }

    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */
    private static List<String> execEnv(List<? extends Object> command, List<String> env, boolean logUsage) throws HgException{
        if( EventQueue.isDispatchThread()){
            Mercurial.LOG.log(Level.FINE, "WARNING execEnv():  calling Hg command in AWT Thread - could stall UI"); // NOI18N
        }
        assert ( command != null && command.size() > 0);
        if(logUsage) {
            Utils.logVCSClientEvent("HG", "CLI");
        }
        final List<String> list = Collections.synchronizedList(new ArrayList<String>());
        BufferedReader input = null;
        BufferedReader error = null;
        Process proc = null;
        File outputStyleFile = null;
        try{
            if (command.size() > 10)  {
                List<String> smallCommand = new ArrayList<String>();
                int count = 0;
                for (Iterator i = command.iterator(); i.hasNext();) {
                    smallCommand.add((String)i.next());
                    if (count++ > 10) break;
                }
                Mercurial.LOG.log(Level.FINE, "execEnv(): " + smallCommand); // NOI18N
            } else {
                Mercurial.LOG.log(Level.FINE, "execEnv(): " + command); // NOI18N
            }
            try {
                outputStyleFile = createOutputStyleFile(command);
            } catch (IOException ex) {
                Mercurial.LOG.log(Level.WARNING, "Failed to create temporary file defining Hg output style."); //NOI18N
                //ignore - outputStyleFile will remain <null>
            }
            List<String> commandLine = toCommandList(command, outputStyleFile);
            ProcessBuilder pb = new ProcessBuilder(commandLine);
            if(env != null && env.size() > 0){
                Map<String, String> envOrig = pb.environment();
                for(String s: env){
                    envOrig.put(s.substring(0,s.indexOf('=')), s.substring(s.indexOf('=')+1));
                }
            }
            proc = pb.start();

            input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            final BufferedReader errorReader = error;
            Thread errorThread = new Thread(new Runnable () {
                public void run() {
                    try {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            list.add(line);
                        }
                    } catch (IOException ex) {
                        // not interested
                    }
                }
            });
            errorThread.start();
            String line;
            while ((line = input.readLine()) != null){
                list.add(line);
            }
            input.close();
            input = null;
            try {
                errorThread.join();
            } catch (InterruptedException ex) {
                // not interested
            }
            error.close();
            error = null;
            try {
                proc.waitFor();
                // By convention we assume that 255 (or -1) is a serious error.
                // For instance, the command line could be too long.
                if (proc.exitValue() == 255) {
                    Mercurial.LOG.log(Level.FINE, "execEnv():  process returned 255"); // NOI18N
                    if (list.isEmpty()) {
                        Mercurial.LOG.log(Level.SEVERE, "command: " + command); // NOI18N
                        throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_EXECUTE_COMMAND"));
                    }
                }
            } catch (InterruptedException e) {
                Mercurial.LOG.log(Level.FINE, "execEnv():  process interrupted " + e); // NOI18N
            }
        }catch(InterruptedIOException e){
            // We get here is we try to cancel so kill the process
            Mercurial.LOG.log(Level.FINE, "execEnv():  execEnv(): InterruptedIOException " + e); // NOI18N
            if (proc != null)  {
                try {
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                    proc.getErrorStream().close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
                proc.destroy();
            }
            throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_CANCELLED"));
        }catch(IOException e){
            // Hg does not seem to be returning error status != 0
            // even when it fails when for instance adding an already tracked file to
            // the repository - we will have to examine the output in the context of the
            // calling func and raise exceptions there if needed
            Mercurial.LOG.log(command.contains(HG_VERSION_CMD) ? Level.INFO : Level.SEVERE,
                    "execEnv():  execEnv(): IOException " + e); // NOI18N

            // Handle low level Mercurial failures
            if (isErrorArgsTooLong(e.getMessage())){
                assert(command.size()> 2);
                throw new HgException.HgTooLongArgListException(NbBundle.getMessage(HgCommand.class, "MSG_ARG_LIST_TOO_LONG_ERR",
                            command.get(1), command.size() -2 ));
            }else if (isErrorNoHg(e.getMessage()) || isErrorCannotRun(e.getMessage())){
                throw new HgException(NbBundle.getMessage(Mercurial.class, "MSG_VERSION_NONE_MSG"));
            }else{
                throw new HgException(NbBundle.getMessage(HgCommand.class, "MSG_UNABLE_EXECUTE_COMMAND"));
            }
        }finally{
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
                input = null;
            }
            if (error != null) {
                try {
                    error.close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
            }
            if (outputStyleFile != null) {
                outputStyleFile.delete();
            }
        }
        return list;
    }

    private static File createOutputStyleFile(List<? extends Object> cmdLine) throws IOException {
        File result = null;

        for (Object obj : cmdLine) {
            if (obj == null) {
                assert false;
                continue;
            }

            if (obj.getClass() == String.class) {
                String str = (String) obj;
                if (str.startsWith("--template=")) {                    //NOI18N
                    if (result != null) {
                        assert false : "implementation not ready for multiple templates on one command line"; //NOI18N
                        continue;
                    }

                    String template = str.substring("--template=".length()); //NOI18N

                    File tempFile = File.createTempFile(
                                                "hg-output-style",      //NOI18N
                                                null);    //extension (default)
                    Writer writer = new OutputStreamWriter(
                                                new FileOutputStream(tempFile),
                                                "ISO-8859-1");          //NOI18N
                    try {
                        writer.append("changeset = ")                   //NOI18N
                              .append('"').append(template).append('"');
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException ex) {
                                //ignore
                            }
                        }
                    }

                    /*
                     * only store the reference to the file to variable 'result'
                     * if the file's content was successfully written:
                     */
                    result = tempFile;
                }
            }
        }
        return result;
    }

    private static List<String> toCommandList(List<? extends Object> cmdLine, File styleFile) {
        if (cmdLine.isEmpty()) {
            return (List<String>) cmdLine;
        }

        List<String> result = new ArrayList<String>(cmdLine.size() + 2);
        boolean first = true;
        for (Object obj : cmdLine) {
            if (obj == null) {
                assert false;
                continue;
            }
            if (obj == HG_COMMAND_PLACEHOLDER) {
                result.addAll(makeHgLauncherCommandLine());
            } else if (obj.getClass() == String.class) {
                String str = (String) obj;
                if (str.startsWith("--template=") && (styleFile != null)) { //NOI18N
                    result.add("--style");                              //NOI18N
                    result.add(styleFile.getAbsolutePath());
                } else {
                    result.add(str);
                }
            } else if (obj instanceof HgURL) {
                if (first) {
                    assert false;
                    result.add(obj.toString());
                } else {
                    result.add(((HgURL) obj).toHgCommandUrlString());
                }
            } else if (obj instanceof File) {
                result.add(((File) obj).getPath());
            } else {
                assert false;
                result.add(obj.toString());
            }
            first = false;
        }
        assert !result.isEmpty();
        return result;
    }

    /**
     * Returns the ouput from the given command
     *
     * @param command to execute
     * @return List of the command's output or an exception if one occured
     */
    private static List<String> exec(List<? extends Object> command) throws HgException{
        if(!Mercurial.getInstance().isAvailable()){
            return new ArrayList<String>();
        }
        return execEnv(command, null);
    }
    private static List<String> execForVersionCheck() throws HgException{
        List<String> command = new ArrayList<String>();
        command.add(getHgCommand());
        command.add(HG_VERSION_CMD);

        return execEnv(command, null, false);
    }

    private static String getHgCommand() {
        return HG_COMMAND_PLACEHOLDER;
    }

    private static List<String> makeHgLauncherCommandLine() {
        String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath();

        if (defaultPath == null || defaultPath.length() == 0) {
            return Collections.singletonList(HG_COMMAND);
        }

        File f = new File(defaultPath);
        File launcherFile;
        if(f.isFile()) {
            launcherFile = f;
        } else {
            if(Utilities.isWindows()){
                launcherFile = null;
                for (String hgExecutable : HG_WINDOWS_EXECUTABLES) {
                    File executableFile = new File(f, hgExecutable);
                    if (executableFile.isFile()) {
                        launcherFile = executableFile;
                        break;
                    }
                }
                if (launcherFile == null) {
                    launcherFile = new File(f, HG_COMMAND + HG_WINDOWS_EXE);
                }
            } else {
                launcherFile = new File(f, HG_COMMAND);
            }
        }
        String launcherPath = launcherFile.getAbsolutePath();

        List<String> result;
        if (Utilities.isWindows() && !launcherPath.endsWith(HG_WINDOWS_EXE)) {
            /* handle .bat and .cmd files: */
            result = new ArrayList<String>(3);
            result.add("cmd.exe");                                      //NOI18N
            result.add("/C");                                           //NOI18N
            result.add(launcherPath);
            return result;
        } else {
            result = Collections.singletonList(launcherPath);
        }
        return result;
    }

    private static void handleError(List<? extends Object> command, List<String> cmdOutput, String message, OutputLogger logger) throws HgException{
        if (command != null && cmdOutput != null && logger != null){
            Mercurial.LOG.log(Level.WARNING, "command: " + command); // NOI18N
            Mercurial.LOG.log(Level.WARNING, "output: " + HgUtils.replaceHttpPassword(cmdOutput)); // NOI18N
            logger.outputInRed(NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ERR")); // NOI18N
            logger.output(NbBundle.getMessage(
                                HgCommand.class,
                                "MSG_COMMAND_INFO_ERR",                 //NOI18N
                                command,
                                HgUtils.replaceHttpPassword(cmdOutput)));
        }

        if (cmdOutput != null && (isErrorPossibleProxyIssue(cmdOutput.get(0)) || isErrorPossibleProxyIssue(cmdOutput.get(cmdOutput.size() - 1)))) {
            boolean bConfirmSetProxy;
            bConfirmSetProxy = HgUtils.confirmDialog(HgCommand.class, "MSG_POSSIBLE_PROXY_ISSUE_TITLE", "MSG_POSSIBLE_PROXY_ISSUE_QUERY"); // NOI18N
            if(bConfirmSetProxy){
                OptionsDisplayer.getDefault().open("General");              // NOI18N
            }
        } else {
            throw new HgException(message);
        }
    }

    private static PasswordAuthentication handleAuthenticationError(List<String> cmdOutput, File repository, String url, String userName, UserCredentialsSupport credentialsSupport) {
        return handleAuthenticationError(cmdOutput, repository, url, userName, credentialsSupport, true);
    }

    private static PasswordAuthentication handleAuthenticationError(List<String> cmdOutput, File repository, String url, String userName, UserCredentialsSupport credentialsSupport, boolean showKenaiLoginDialog) {
        PasswordAuthentication credentials = null;
        String msg = cmdOutput.get(cmdOutput.size() - 1).toLowerCase();
        if (isAuthMsg(msg)) {
            HgKenaiSupport support = HgKenaiSupport.getInstance();
            if(support.isKenai(url) && showKenaiLoginDialog) {
                // try to login
                credentials = handleKenaiAuthorisation(support, url);
            } else {
                credentials = credentialsSupport.getUsernamePasswordCredentials(repository, url, userName);
            }
        }
        return credentials;
    }

    private static PasswordAuthentication handleKenaiAuthorisation(HgKenaiSupport support, String url) {
        PasswordAuthentication pa = support.getPasswordAuthentication(url, true);
        return pa;
    }

    public static boolean isAuthMsg(String msg) {
        return msg.contains(HG_AUTHORIZATION_REQUIRED_ERR)
                || msg.contains(HG_AUTHORIZATION_FAILED_ERR);
    }

    public static boolean isMergeNeededMsg(String msg) {
        return msg.indexOf(HG_MERGE_NEEDED_ERR) > -1;                       // NOI18N
    }

    public static boolean isBackoutMergeNeededMsg(String msg) {
        return msg.indexOf(HG_BACKOUT_MERGE_NEEDED_ERR) > -1;                       // NOI18N
    }

    public static boolean isMergeConflictMsg(String msg) {
        if(Utilities.isWindows() ) {
            return (msg.indexOf(HG_MERGE_CONFLICT_WIN1_ERR) > -1) &&        // NOI18N
                    (msg.indexOf(HG_MERGE_CONFLICT_WIN2_ERR) > -1);         // NOI18N
        }else{
            return msg.indexOf(HG_MERGE_CONFLICT_ERR) > -1;                 // NOI18N
        }
    }

    public static boolean isMergeUnavailableMsg(String msg) {
        return msg.indexOf(HG_MERGE_UNAVAILABLE_ERR) > -1;                 // NOI18N
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

    private static boolean isErrorNoDefaultPush(String msg) {
        return msg.indexOf(HG_ABORT_NO_DEFAULT_PUSH_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNoDefaultPath(String msg) {
        return msg.indexOf(HG_ABORT_NO_DEFAULT_ERR) > -1; // NOI18N
    }

    private static boolean isErrorPossibleProxyIssue(String msg) {
        return msg.indexOf(HG_ABORT_POSSIBLE_PROXY_ERR) > -1; // NOI18N
    }

    private static boolean isErrorNoRepository(String msg) {
        return msg.indexOf(HG_NO_REPOSITORY_ERR) > -1 ||
                 msg.indexOf(HG_NOT_REPOSITORY_ERR) > -1 ||
                 (msg.indexOf(HG_REPOSITORY) > -1 && msg.indexOf(HG_NOT_FOUND_ERR) > -1); // NOI18N
    }

    private static boolean isErrorNoHg(String msg) {
        return msg.indexOf(HG_NO_HG_CMD_FOUND_ERR) > -1; // NOI18N
    }
    private static boolean isErrorArgsTooLong(String msg) {
        return msg.indexOf(HG_ARG_LIST_TOO_LONG_ERR) > -1
                || msg.contains(HG_ARGUMENT_LIST_TOO_LONG_ERR);
    }

    private static boolean isErrorCannotRun(String msg) {
        return msg.indexOf(HG_CANNOT_RUN_ERR) > -1; // NOI18N
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

    private static boolean isErrorAbort(String msg) {
        return msg.indexOf(HG_ABORT_ERR) > -1; // NOI18N
    }

    public static boolean isErrorAbortPush(String msg) {
        return msg.indexOf(HG_ABORT_PUSH_ERR) > -1; // NOI18N
    }

    public static boolean isErrorAbortNoFilesToCopy(String msg) {
        return msg.indexOf(HG_ABORT_NO_FILES_TO_COPY_ERR) > -1; // NOI18N
    }

    public static boolean isCommitAfterMerge(String msg) {
        return msg.indexOf(HG_COMMIT_AFTER_MERGE_ERR) > -1;                                   // NOI18N
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
    public static boolean isNoRevStrip(String msg) {
        return msg.indexOf(HG_NO_REV_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isLocalChangesStrip(String msg) {
        return msg.indexOf(HG_LOCAL_CHANGES_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMultipleHeadsStrip(String msg) {
        return msg.indexOf(HG_MULTIPLE_HEADS_STRIP_ERR) > -1;                                   // NOI18N
    }
    public static boolean isUncommittedChangesBackout(String msg) {
        return msg.indexOf(HG_ABORT_UNCOMMITTED_CHANGES_ERR) > -1;                                   // NOI18N
    }
    public static boolean isMergeChangesetBackout(String msg) {
        return msg.indexOf(HG_ABORT_BACKOUT_MERGE_CSET_ERR) > -1;                                   // NOI18N
    }

    public static boolean isNoUpdates(String msg) {
        return msg.indexOf(HG_NO_UPDATES_ERR) > -1;                                   // NOI18N
    }

    private static boolean isErrorNoView(String msg) {
        return msg.indexOf(HG_NO_VIEW_ERR) > -1;                                     // NOI18N
    }

    private static boolean isErrorHgkNotFound(String msg) {
        return msg.indexOf(HG_HGK_NOT_FOUND_ERR) > -1;                               // NOI18N
    }

    private static boolean isErrorNoSuchFile(String msg) {
        return msg.indexOf(HG_NO_SUCH_FILE_ERR) > -1;                               // NOI18N
    }

    private static boolean isErrorNoResponse(String msg) {
        return msg.indexOf(HG_NO_RESPONSE_ERR) > -1;                               // NOI18N
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

    /**
     * Marks the given file as resolved if the resolve command is available
     * @param repository
     * @param file
     * @param logger
     * @throws HgException
     */
    public static void markAsResolved (File repository, File file, OutputLogger logger) throws HgException {
        if (file == null) return;
        if (!HgUtils.hasResolveCommand(Mercurial.getInstance().getVersion())) {
            return;
        }

        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_RESOLVE_CMD);
        command.add(HG_RESOLVE_MARK_RESOLVED);
        command.add(HG_OPT_REPOSITORY);
        command.add(repository.getAbsolutePath());
        command.add(FileUtil.normalizeFile(file).getAbsolutePath());
        List<String> list = exec(command);

        if (!list.isEmpty()) {
            if (isErrorNoRepository(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_NO_REPOSITORY_ERR"), logger);
             } else if (isErrorAbort(list.get(0))) {
                handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
             }
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

        if (bExists) {
            Mercurial.LOG.log(Level.FINE, "existsConflictFile(): File: {0} {1}", // NOI18N
                    new Object[] {path + HG_STR_CONFLICT_EXT, "Exists"} ); // NOI18N
        }
        return bExists;
    }

    /**
     * Tries to determine if the given url represents real mercurial repository
     * @param hgUrl
     * @return true if the given url represents real mercurial repository
     */
    public static boolean checkRemoteRepository(String repository) {
        boolean retval = false;
        if (repository == null || "".equals(repository)) {
            return retval;
        }
        // temporary folder will be deleted manually
        File tmpFolder = Utils.getTempFolder(false);
        File tmpTarget = new File(tmpFolder, "rep");                    //NOI18N
        List<String> command = new ArrayList<String>();

        command.add(getHgCommand());
        command.add(HG_CLONE_CMD);
        command.add(repository);
        command.add(tmpTarget.getAbsolutePath());

        retval = execCheckClone(command);
        Utils.deleteRecursively(tmpFolder);
        return retval;
    }

    /**
     * This is a repulsive piece of code.
     * Have no other idea how to determine if the target is a real mercurial repository.
     * So this tries to clone into a temporary folder. If the command does not finish any time soon (e.g. huge repos as the netbeans repository)
     * or returns some recognizable messages in its output, then the target is assumed to be a real repository.
     * @param command
     * @return
     */
    private static boolean execCheckClone(List<String> command) {
        final boolean[] isRepository = new boolean[] {false};
        if(!Mercurial.getInstance().isAvailable(true, false)){
            Mercurial.LOG.info("Unsupported hg version");
            return isRepository[0];
        }

        Process proc = null;
        try{
            Mercurial.LOG.log(Level.FINE, "execCheckClone(): " + command); // NOI18N
            ProcessBuilder b = new ProcessBuilder(command);
            b.redirectErrorStream(true);
            // start the clone
            proc = b.start();
            final Process procf = proc;
            final Thread t1 = new Thread(new Runnable() {
                public void run() {
                    BufferedReader in = new BufferedReader(new InputStreamReader(procf.getInputStream()));
                    String line;
                    try {
                        // if the clone command returns any recognized messages, consider the target a real repository
                        while ((line = in.readLine()) != null) {
                            line = line.toLowerCase();
                            if (line.contains("requesting all changes") //NOI18N
                                    || line.contains("adding changesets") //NOI18N
                                    || line.contains("no changes found") //NOI18N
                                    || line.contains("updating working directory")) { //NOI18N
                                isRepository[0] = true;
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        Mercurial.LOG.log(Level.FINE, null, ex); // NOI18N
                    } finally {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Mercurial.LOG.log(Level.FINE, null, ex); // NOI18N
                        }
                    }
                }
            });
            t1.start();
            // wait for the clone to finish
            int rounds = HgUtils.getNumberOfRoundsForRepositoryValidityCheck();

            while (t1.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // ignore
                }
                if (--rounds == 0) {
                    // assume hg is cloning, we've been waiting for a long time, it has not thrown any error
                    proc.destroy();
                    isRepository[0] = true;
                }
            }
        } catch(InterruptedIOException e){
            // We get here is we try to cancel so kill the process
            Mercurial.LOG.log(Level.FINE, "execCheckClone():  InterruptedIOException " + e); // NOI18N
            if (proc != null)  {
                try {
                    proc.getInputStream().close();
                    proc.getOutputStream().close();
                    proc.getErrorStream().close();
                } catch (IOException ioex) {
                //Just ignore. Closing streams.
                }
                proc.destroy();
            }
        } catch(IOException e){
            Mercurial.LOG.log(Level.INFO, null, e);
        } finally {
            if (proc != null) {
                try {
                    proc.getInputStream().close();
                    proc.getErrorStream().close();
                    proc.getOutputStream().close();
                } catch (IOException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return isRepository[0];
    }

    /**
     * Commands are limited by size
     * @param commandSize
     * @return
     */
    private static boolean isTooLongCommand(int commandSize) {
        return (Utilities.isWindows() || Utilities.isMac()) && commandSize > MAX_COMMANDLINE_SIZE;
    }

    /**
     * This utility class should not be instantiated anywhere.
     */
    private HgCommand() {
    }

    /**
     * Command working with a remote repository.
     * If a hg command fails because of authentication failure, login dialog is raised and the command is ovoked again with
     * entered credentials.
     */
    private static class InterRepositoryCommand {
        protected File repository;
        protected HgURL remoteUrl;
        protected OutputLogger logger;
        protected String hgCommand;
        protected String hgCommandType;
        protected String defaultUrl;
        protected boolean acquireCredentialsFirst;
        protected boolean outputDetails;
        protected List<String> additionalOptions;
        protected UserCredentialsSupport credentialsSupport;
        protected boolean showSaveOption;
        private PasswordAuthentication credentials;

        public InterRepositoryCommand () {
            hgCommand = getHgCommand();
            outputDetails = true;
            additionalOptions = new LinkedList<String>();
        }

        /**
         * This will save the credentials along with URLs into the hgrc config file if user checked 'Save values' in a login dialog
         * @param propertyName property to be saved (default, default-push/pull)
         */
        public void saveCredentials (String propertyName) {
            if (credentials != null && credentialsSupport != null && credentialsSupport.shallSaveValues()) {
                try {
                    // user logged-in successfully during the process and checked 'Save values'
                    HgModuleConfig.getDefault().setProperty(repository, propertyName, new HgURL(remoteUrl.toHgCommandUrlString(), credentials.getUserName(), new String(credentials.getPassword())).toCompleteUrlString());
                } catch (URISyntaxException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                } catch (IOException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }
        }

        public List<String> invoke() throws HgException {
            List<String> list = null;
            boolean retry = true;
            boolean showLoginWindow = true;
            credentials = null;
            HgKenaiSupport supp = HgKenaiSupport.getInstance();
            String rawUrl = remoteUrl.toUrlStringWithoutUserInfo();
            acquireCredentialsFirst |= supp.isLoggedIntoKenai();
            if (supp.isKenai(rawUrl) && acquireCredentialsFirst) {
                // will force user to login into kenai, if he isn't yet
                credentials = supp.getPasswordAuthentication(rawUrl, false);
                if (credentials == null) {
                    // show log window only once, user probably canceled
                    showLoginWindow = false;
                }
            }

            HgURL url = remoteUrl;
            credentialsSupport = new UserCredentialsSupport();
            credentialsSupport.setShowSaveOption(showSaveOption);
            while (retry) {
                retry = false;
                try {
                    if (credentials != null) {
                        url = new HgURL(remoteUrl.toHgCommandUrlString(), credentials.getUserName(), new String(credentials.getPassword()));
                    }
                } catch (URISyntaxException ex) {
                    // this should NEVER happen
                    Mercurial.LOG.log(Level.SEVERE, null, ex);
                    break;
                }
                List<Object> command = new ArrayList<Object>();

                command.add(hgCommand);
                command.add(hgCommandType);
                for (String s : additionalOptions) {
                    command.add(s);
                }
                command.add(HG_OPT_REPOSITORY);
                command.add(repository.getAbsolutePath());
                command.add(url);

                String proxy = getGlobalProxyIfNeeded(defaultUrl, outputDetails, logger);
                if (proxy != null) {
                    List<String> env = new ArrayList<String>();
                    env.add(HG_PROXY_ENV + proxy);
                    list = execEnv(command, env);
                } else {
                    list = exec(command);
                }

                if (!list.isEmpty() &&
                        isErrorAbort(list.get(list.size() - 1))) {
                    if (HG_PUSH_CMD.equals(hgCommandType) && isErrorAbortPush(list.get(list.size() - 1))) {
                        //
                    } else {
                        if ((credentials = handleAuthenticationError(list, repository, rawUrl, credentials == null ? "" : credentials.getUserName(), credentialsSupport, showLoginWindow)) != null) { //NOI18N
                            // auth redone, try again
                            retry = true;
                        } else {
                            handleError(command, list, NbBundle.getMessage(HgCommand.class, "MSG_COMMAND_ABORTED"), logger);
                        }
                    }
                }
            }
            return list;
        }
    }

    private static String prepareLogTemplate (File temporaryFolder, String changesetFileName) throws IOException {
        InputStream isChangeset = HgCommand.class.getResourceAsStream(changesetFileName);
        InputStream isStyle = HgCommand.class.getResourceAsStream(HG_LOG_STYLE_NAME);
        File styleFile = new File(temporaryFolder, HG_LOG_STYLE_NAME);
        File changesetFile = new File(temporaryFolder, HG_LOG_CHANGESET_GENERAL_NAME);
        Utils.copyStreamsCloseAll(new FileOutputStream(changesetFile), isChangeset);
        Utils.copyStreamsCloseAll(new FileOutputStream(styleFile), isStyle);

        return HG_ARGUMENT_STYLE + styleFile.getAbsolutePath();
    }
}
