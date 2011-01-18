/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitMergeResult.MergeStatus;

/**
 *
 * @author ondra
 */
public class JGitMergeResult implements GitMergeResult {
    private final MergeStatus mergeStatus;
    private final File workDir;
    private final List<File> conflicts;
    private final String newHead;
    private final String base;
    private final String[] mergedCommits;

    public JGitMergeResult (MergeResult result, File workDir) {
        this.mergeStatus = GitMergeResult.MergeStatus.valueOf(result.getMergeStatus().name());
        this.workDir = workDir;
        this.newHead = result.getNewHead() == null ? null : result.getNewHead().getName();
        this.base = result.getBase() == null ? null : result.getBase().getName();
        this.mergedCommits = getMergedCommits(result);
        this.conflicts = getConflicts(result);
    }

    @Override
    public MergeStatus getMergeStatus() {
        return mergeStatus;
    }

    @Override
    public String getBase () {
        return base;
    }

    @Override
    public String[] getMergedCommits () {
        return mergedCommits;
    }

    @Override
    public String getNewHead () {
        return newHead;
    }

    @Override
    public Collection<File> getConflicts () {
        return conflicts;
    }

    private String[] getMergedCommits (MergeResult result) {
        ObjectId[] mergedObjectIds = result.getMergedCommits();
        String[] commits = new String[mergedObjectIds.length];
        for (int i = 0; i < mergedObjectIds.length; ++i) {
            commits[i] = mergedObjectIds[i].getName();
        }
        return commits;
    }

    private List<File> getConflicts(MergeResult result) {
        List<File> files = new LinkedList<File>();
        Map<String, int[][]> mergeConflicts = result.getConflicts();
        if (mergeConflicts != null) {
            for (Map.Entry<String, int[][]> conflict : mergeConflicts.entrySet()) {
                files.add(new File(workDir, conflict.getKey()));
            }
        }
        return files;
    }
}
