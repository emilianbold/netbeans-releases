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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class PushBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {
    private final File repository;
    private final ItemSelector<PushMapping> localObjects;
    private boolean lastPanel;

    public PushBranchesStep (File repository) {
        this.repository = repository;
        this.localObjects = new ItemSelector<PushMapping>(NbBundle.getMessage(PushBranchesStep.class, "PushBranchesPanel.jLabel1.text")); //NOI18N
        this.localObjects.addChangeListener(this);
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
        if (localObjects.getSelectedBranches().isEmpty()) {
            setValid(false, new Message(NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.errorNoBranchSelected"), false)); //NOI18N
        }
    }

    @Override
    protected final JComponent getJComponent () {
        return localObjects.getPanel();
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(PushBranchesStep.class);
    }

    public void fillRemoteBranches (final Map<String, GitBranch> branches, final Map<String, String> tags) {
        new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                final Map<String, GitBranch> localBranches = new HashMap<String, GitBranch>();
                final Map<String, GitTag> localTags = new HashMap<String, GitTag>();
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                localBranches.putAll(info.getBranches());
                localTags.putAll(info.getTags());
                
                final List<PushMapping> l = new ArrayList<PushMapping>(branches.size());
                GitClient client;
                try {
                    client = getClient();
                } catch (GitException ex) {
                    client = null;
                }
                for (GitBranch branch : localBranches.values()) {
                    if (!branch.isRemote()) {
                        GitBranch remoteBranch = branches.get(branch.getName());
                        boolean conflicted = false;
                        if (client != null && remoteBranch != null) {
                            String idLocal = branch.getId();
                            String idRemote = remoteBranch.getId();
                            if (!idLocal.equals(idRemote)) {
                                try {
                                    GitRevisionInfo rev = client.getCommonAncestor(new String[] { idLocal, idRemote } , getProgressMonitor());
                                    conflicted = rev == null || !idRemote.equals(rev.getRevision());
                                } catch (GitException.MissingObjectException ex) {
                                    if (idRemote.equals(ex.getObjectName())) {
                                        conflicted = true;
                                    } else {
                                        Logger.getLogger(PushBranchesStep.class.getName()).log(Level.INFO, idLocal + ", " + idRemote, ex); //NOI18N
                                    }
                                } catch (GitException ex) {
                                    Logger.getLogger(PushBranchesStep.class.getName()).log(Level.INFO, idLocal + ", " + idRemote, ex); //NOI18N
                                }
                            }
                        }
                        boolean preselected = !conflicted && remoteBranch != null && !remoteBranch.getId().equals(branch.getId());
                        l.add(new PushMapping.PushBranchMapping(remoteBranch == null ? null : remoteBranch.getName(),
                                remoteBranch == null ? null : remoteBranch.getId(),
                                branch, conflicted, preselected));
                    }
                }
                for (GitTag tag : localTags.values()) {
                    if (!tags.containsKey(tag.getTagName())) {
                        l.add(new PushMapping.PushTagMapping(tag));
                    }
                }
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        fillLocalObjects(l);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.loadingLocalBranches")); //NOI18N
    }

    private void fillLocalObjects (List<PushMapping> mappings) {
        this.localObjects.setBranches(mappings);
        validateBeforeNext();
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    @Override
    public boolean isFinishPanel () {
        return lastPanel;
    }

    void setAsLastPanel (boolean isLastPanel) {
        this.lastPanel = isLastPanel;
    }

    Collection<PushMapping> getSelectedMappings () {
        return localObjects.getSelectedBranches();
    }
}
