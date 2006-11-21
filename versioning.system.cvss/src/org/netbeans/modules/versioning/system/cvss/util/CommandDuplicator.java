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

package org.netbeans.modules.versioning.system.cvss.util;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.netbeans.lib.cvsclient.command.remove.RemoveCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;

/**
 * Utility class that can create copies of commands.
 *
 * @author Maros Sandor
 */
public abstract class CommandDuplicator {
    
    public static CommandDuplicator getDuplicator(Command src) {
        if (src instanceof CommitCommand) return new CommitCloner((CommitCommand) src);
        if (src instanceof UpdateCommand) return new UpdateCloner((UpdateCommand) src);
        if (src instanceof RemoveCommand) return new RemoveCloner((RemoveCommand) src);
        if (src instanceof RtagCommand) return new RtagCloner((RtagCommand) src);
        if (src instanceof RlogCommand) return new RlogCloner((RlogCommand) src);
        if (src instanceof TagCommand) return new TagCloner((TagCommand) src);
        throw new IllegalArgumentException("Clone not supported for command type: " + src.getClass().getName()); // NOI18N
    }

    public abstract Command duplicate();
    
    private static class CommitCloner extends CommandDuplicator {
        private final CommitCommand sample;

        public CommitCloner(CommitCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            CommitCommand c = new CommitCommand();
            c.setForceCommit(sample.isForceCommit());
            c.setLogMessageFromFile(sample.getLogMessageFromFile());
            c.setMessage(sample.getMessage());
            c.setNoModuleProgram(sample.isNoModuleProgram());
            c.setRecursive(sample.isRecursive());
            c.setToRevisionOrBranch(sample.getToRevisionOrBranch());
            return c;
        }
    }

    private static class UpdateCloner extends CommandDuplicator {
        private final UpdateCommand sample;

        public UpdateCloner(UpdateCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            UpdateCommand c = new UpdateCommand();
            c.setBuildDirectories(sample.isBuildDirectories());
            c.setCleanCopy(sample.isCleanCopy());
            c.setKeywordSubst(sample.getKeywordSubst());
            c.setMergeRevision1(sample.getMergeRevision1());
            c.setMergeRevision2(sample.getMergeRevision2());
            c.setPipeToOutput(sample.isPipeToOutput());
            c.setPruneDirectories(sample.isPruneDirectories());
            c.setResetStickyOnes(sample.isResetStickyOnes());
            c.setUpdateByDate(sample.getUpdateByDate());
            c.setUpdateByRevision(sample.getUpdateByRevision());
            c.setUseHeadIfNotFound(sample.isUseHeadIfNotFound());
            c.setRecursive(sample.isRecursive());
            return c;
        }
    }


    private static class RemoveCloner extends CommandDuplicator {
        private final RemoveCommand sample;

        public RemoveCloner(RemoveCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            RemoveCommand c = new RemoveCommand();
            c.setDeleteBeforeRemove(sample.isDeleteBeforeRemove());
            c.setIgnoreLocallyExistingFiles(sample.isIgnoreLocallyExistingFiles());
            c.setRecursive(sample.isRecursive());
            return c;
        }
    }

    private static class RtagCloner extends CommandDuplicator {
        private final RtagCommand sample;

        public RtagCloner(RtagCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            RtagCommand c = new RtagCommand();
            c.setClearFromRemoved(sample.isClearFromRemoved());
            c.setDeleteTag(sample.isDeleteTag());
            c.setMakeBranchTag(sample.isMakeBranchTag());
            c.setMatchHeadIfRevisionNotFound(sample.isMatchHeadIfRevisionNotFound());
            c.setModules(sample.getModules());
            c.setNoExecTagProgram(sample.isNoExecTagProgram());
            c.setOverrideExistingTag(sample.isOverrideExistingTag());
            c.setTag(sample.getTag());
            c.setTagByDate(sample.getTagByDate());
            c.setTagByRevision(sample.getTagByRevision());
            c.setRecursive(sample.isRecursive());
            return c;
        }
    }

    private static class TagCloner extends CommandDuplicator {
        private final TagCommand sample;

        public TagCloner(TagCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            TagCommand c = new TagCommand();
            c.setDeleteTag(sample.isDeleteTag());
            c.setMakeBranchTag(sample.isMakeBranchTag());
            c.setMatchHeadIfRevisionNotFound(sample.isMatchHeadIfRevisionNotFound());
            c.setOverrideExistingTag(sample.isOverrideExistingTag());
            c.setTag(sample.getTag());
            c.setTagByDate(sample.getTagByDate());
            c.setTagByRevision(sample.getTagByRevision());
            c.setRecursive(sample.isRecursive());
            c.setCheckThatUnmodified(sample.isCheckThatUnmodified());
            return c;
        }
    }
    
    private static class RlogCloner extends CommandDuplicator {
        private final RlogCommand sample;

        public RlogCloner(RlogCommand sample) {
            this.sample = sample;
        }

        public Command duplicate() {
            RlogCommand c = new RlogCommand();
            c.setDateFilter(sample.getDateFilter());
            c.setDefaultBranch(sample.isDefaultBranch());
            c.setHeaderAndDescOnly(sample.isHeaderAndDescOnly());
            c.setHeaderOnly(sample.isHeaderOnly());
            c.setNoTags(sample.isNoTags());
            c.setRevisionFilter(sample.getRevisionFilter());
            c.setStateFilter(sample.getStateFilter());
            c.setSuppressHeader(sample.isSuppressHeader());
            c.setUserFilter(sample.getUserFilter());
            c.setModules(sample.getModules());
            c.setRecursive(sample.isRecursive());
            return c;
        }
    }
}
