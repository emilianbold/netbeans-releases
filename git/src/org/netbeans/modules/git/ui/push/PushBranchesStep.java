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

import java.awt.EventQueue;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.selectors.ItemSelector.Item;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class PushBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {
    private final File repository;
    private final ItemSelector<BranchMapping> branches;
    private boolean lastPanel;

    public PushBranchesStep (File repository) {
        this.repository = repository;
        this.branches = new ItemSelector<BranchMapping>(NbBundle.getMessage(PushBranchesStep.class, "PushBranchesPanel.jLabel1.text")); //NOI18N
        this.branches.addChangeListener(this);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
        getJComponent().setName(NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranches.localBranches")); //NOI18N
    }
    
    @Override
    protected final void validateBeforeNext () {
        setValid(true, null);
        if (branches.getSelectedBranches().isEmpty()) {
            setValid(false, new Message(NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.errorNoBranchSelected"), false)); //NOI18N
        }
    }

    @Override
    protected final JComponent getJComponent () {
        return branches.getPanel();
    }

    public void fillRemoteBranches (final Map<String, GitBranch> branches) {
        new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                final Map<String, GitBranch> localBranches = new HashMap<String, GitBranch>();
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                localBranches.putAll(info.getBranches());
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        fillRemoteBranches(branches, localBranches);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.loadingLocalBranches")); //NOI18N
    }

    private void fillRemoteBranches (Map<String, GitBranch> branches, Map<String, GitBranch> localBranches) {
        List<BranchMapping> l = new ArrayList<BranchMapping>(branches.size());
        for (GitBranch branch : localBranches.values()) {
            if (!branch.isRemote()) {
                l.add(new BranchMapping(branches.get(branch.getName()), branch));
            }
        }
        this.branches.setBranches(l);
        validateBeforeNext();
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    public List<String> getSelectedRefSpecs () {
        List<String> specs = new LinkedList<String>();
        for (BranchMapping b : branches.getSelectedBranches()) {
            specs.add(b.getRefSpec());
        }
        return specs;
    }

    @Override
    public boolean isFinishPanel () {
        return lastPanel;
    }

    void setAsLastPanel (boolean isLastPanel) {
        this.lastPanel = isLastPanel;
    }

    Collection<BranchMapping> getSelectedMappings () {
        return branches.getSelectedBranches();
    }
    
    class BranchMapping extends ItemSelector.Item {
        private final String label;
        private final String tooltip;
        private final GitBranch localBranch;
        private final GitBranch remoteBranch;
        private static final String BRANCH_MAPPING_LABEL = "{0} -> {1} [{2}]"; //NOI18N

        public BranchMapping (GitBranch remoteBranch, GitBranch localBranch) {
            super(false);
            this.localBranch = localBranch;
            this.remoteBranch = remoteBranch;
            if(remoteBranch == null) {
                // added
                label = MessageFormat.format(BRANCH_MAPPING_LABEL, localBranch.getName(), localBranch.getName(), "<font color=\"#00b400\">A</font>");
                tooltip = NbBundle.getMessage(
                    PushBranchesStep.class, 
                    "LBL_PushBranchesPanel.BranchMapping.description", //NOI18N
                    new Object[] { 
                        localBranch.getName(),
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchesPanel.BranchMapping.Mode.added.description") //NOI18N
                    }); //NOI18N
            } else {
                // modified
                label = MessageFormat.format(BRANCH_MAPPING_LABEL, localBranch.getName(), remoteBranch.getName(), "<font color=\"#0000FF\">U</font>"); //NOI18N                 
                tooltip = NbBundle.getMessage(
                    PushBranchesStep.class, 
                    "LBL_PushBranchesPanel.BranchMapping.description", //NOI18N
                    new Object[] { 
                        remoteBranch.getName(), 
                        NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranchesPanel.BranchMapping.Mode.updated.description") //NOI18N
                    }); 
            }
        }
        
        public String getRemoteRepositoryBranchName () {
            return remoteBranch == null ? localBranch.getName() : remoteBranch.getName();
        }

        public String getRefSpec () {
            return org.netbeans.libs.git.utils.Utils.getPushRefSpec(localBranch.getName(), (remoteBranch == null ? localBranch : remoteBranch).getName());
        }

        @Override
        public String getText () {
            return label;
        }

        @Override
        public String getTooltipText() {
            return tooltip;
        }

        @Override
        public int compareTo(Item t) {
            if(t == null) {
                return 1;
            }
            if(t instanceof BranchMapping) {
                return localBranch.getName().compareTo(((BranchMapping)t).localBranch.getName());
            }
            return 0;            
        }
    }
}
