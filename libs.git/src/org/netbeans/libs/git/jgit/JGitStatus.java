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

package org.netbeans.libs.git.jgit;

import java.io.File;
import org.eclipse.jgit.diff.DiffEntry;
import org.netbeans.libs.git.GitStatus;

/**
 *
 * @author ondra
 */
public class JGitStatus implements GitStatus {
    private final Status statusHeadWC;
    private final DiffEntry diffEntry;
    
    private final boolean tracked;
    private final String relativePath;
    private final File correspondingFile;
    private final Status statusHeadIndex;
    private final Status statusIndexWC;
    private final boolean conflict;
    private final boolean isFolder;
    private final String workTreePath;

    public JGitStatus (boolean tracked, String relativePath, String workTreePath, File correspondingFile, Status statusHeadIndex, Status statusIndexWC, Status statusHeadWC, boolean conflict, boolean isFolder, DiffEntry diffEntry) {
        this.tracked = tracked;
        this.relativePath = relativePath;
        this.workTreePath = workTreePath;
        this.correspondingFile = correspondingFile;
        this.statusHeadIndex = statusHeadIndex;
        this.statusIndexWC = statusIndexWC;
        this.statusHeadWC = statusHeadWC;
        this.conflict = conflict;
        this.isFolder = isFolder;
        this.diffEntry = diffEntry;
    }

    @Override
    public File getFile() {
        return correspondingFile;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public Status getStatusHeadIndex() {
        return statusHeadIndex;
    }

    @Override
    public Status getStatusIndexWC() {
        return statusIndexWC;
    }

    @Override
    public Status getStatusHeadWC() {
        return statusHeadWC;
    }

    @Override
    public boolean isTracked() {
        return tracked;
    }

    @Override
    public boolean isConflict() {
        return conflict;
    }

    @Override
    public boolean isFolder () {
        return isFolder;
    }

    @Override
    public boolean isCopied() {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.COPY);
    }

    @Override
    public boolean isRenamed() {
        return diffEntry != null && diffEntry.getChangeType().equals(DiffEntry.ChangeType.RENAME);
    }

    @Override
    public File getOldPath() {
        if (isRenamed() || isCopied()) {
            return new File(workTreePath + File.separator + diffEntry.getOldPath());
        } else {
            return null;
        }
    }
}
