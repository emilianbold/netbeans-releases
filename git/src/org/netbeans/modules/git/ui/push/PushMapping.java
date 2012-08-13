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
package org.netbeans.modules.git.ui.push;

import java.text.MessageFormat;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public abstract class PushMapping extends ItemSelector.Item {

    private final String label;
    private final String tooltip;
    private static final String BRANCH_MAPPING_LABEL = "{0} -> {1} [{2}]"; //NOI18N
    private static final String BRANCH_MAPPING_LABEL_UPTODATE = "{0} -> {1}"; //NOI18N
    private final String localName;
    
    protected PushMapping (String localName, String localId, String remoteName, String remoteId, boolean conflict, boolean preselected) {
        super(preselected);
        this.localName = localName;
        if (remoteName == null) {
            // added
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, localName, "<font color=\"#00b400\">A</font>");
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.description", //NOI18N
                    new Object[]{
                        localName,
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchMapping.Mode.added.description") //NOI18N
                    }); //NOI18N
        } else if (localId.equals(remoteId)) {
            // up to date
            label = MessageFormat.format(BRANCH_MAPPING_LABEL_UPTODATE, localName, remoteName);
            tooltip = NbBundle.getMessage(PushBranchesStep.class,
                    "LBL_PushBranchMapping.Mode.uptodate.description", //NOI18N
                    remoteName);
        } else if (conflict) {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, remoteName, "<font color=\"#FF0000\">C</font>"); //NOI18N                 
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.Mode.conflict.description", //NOI18N
                    new Object[]{
                        remoteName
                    });
        } else {
            // modified
            label = MessageFormat.format(BRANCH_MAPPING_LABEL, localName, remoteName, "<font color=\"#0000FF\">U</font>"); //NOI18N                 
            tooltip = NbBundle.getMessage(
                    PushBranchesStep.class,
                    "LBL_PushBranchMapping.description", //NOI18N
                    new Object[]{
                        remoteName,
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchMapping.Mode.updated.description") //NOI18N
                    });
        }
    }


    public abstract String getRefSpec ();

    @Override
    public String getText () {
        return label;
    }

    @Override
    public String getTooltipText () {
        return tooltip;
    }

    @Override
    public int compareTo (Item t) {
        if (t == null) {
            return 1;
        }
        if (t instanceof PushMapping) {
            return localName.compareTo(((PushMapping) t).localName);
        }
        return 0;
    }
    
    public static final class PushBranchMapping extends PushMapping {
        private final GitBranch localBranch;
        private final GitBranch remoteBranch;
        
        public PushBranchMapping (GitBranch remoteBranch, GitBranch localBranch, boolean conflict, boolean preselected) {
            super(localBranch.getName(), localBranch.getId(), 
                    remoteBranch == null ? null : remoteBranch.getName(), 
                    remoteBranch == null ? null : remoteBranch.getId(),
                    conflict,
                    preselected);
            this.localBranch = localBranch;
            this.remoteBranch = remoteBranch;
        }

        public String getRemoteRepositoryBranchName () {
            return remoteBranch == null ? localBranch.getName() : remoteBranch.getName();
        }

        public String getRemoteRepositoryBranchHeadId () {
            return remoteBranch == null ? null : remoteBranch.getId();
        }

        public String getLocalRepositoryBranchHeadId () {
            return localBranch == null ? null : localBranch.getId();
        }

        @Override
        public String getRefSpec () {
            return GitUtils.getPushRefSpec(localBranch.getName(), (remoteBranch == null ? localBranch : remoteBranch).getName());
        }
    }
    
    public static final class PushTagMapping extends PushMapping {
        private final GitTag tag;
        
        public PushTagMapping (GitTag tag) {
            super("tags/" + tag.getTagName(), tag.getTaggedObjectId(), null, null, false, false); //NOI18N
            this.tag = tag;
        }

        @Override
        public String getRefSpec () {
            return GitUtils.getPushTagRefSpec(tag.getTagName());
        }
    }
}
