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

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class FetchBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ListSelectionListener {
    private final FetchBranchesPanel panel;
    private String fetchUri;
    private GitRemoteConfig remote;
    private GitProgressSupport supp;
    private GitProgressSupport validatingSupp;
    private final Mode mode;
    private static final String REF_SPEC_PATTERN = "+refs/heads/{0}:refs/remotes/{1}/{0}"; //NOI18N
    private static final String BRANCH_MAPPING_LABEL = "{0} -> {1}/{0} [{2}]"; //NOI18N
    private final File repository;

    public static enum Mode {
        ACCEPT_EMPTY_SELECTION,
        ACCEPT_NON_EMPTY_SELECTION_ONLY
    }

    public FetchBranchesStep (File repository, Mode mode) {
        this.mode = mode;
        this.repository = repository;
        this.panel = new FetchBranchesPanel();
        this.panel.lstRemoteBranches.setCellRenderer(new BranchRenderer());
        attachListeners();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
    }
    
    private void attachListeners () {
        panel.lstRemoteBranches.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                int index = panel.lstRemoteBranches.locationToIndex(e.getPoint());
                if (index != -1) {
                    BranchMapping mapping = (BranchMapping) panel.lstRemoteBranches.getModel().getElementAt(index);
                    mapping.setSelected(!mapping.isSelected());
                    panel.lstRemoteBranches.repaint();
                    validateBeforeNext();
                }
            }
        });
        panel.lstRemoteBranches.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased (KeyEvent e) {
                int index = panel.lstRemoteBranches.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_SPACE && index != -1) {
                    BranchMapping mapping = (BranchMapping) panel.lstRemoteBranches.getModel().getElementAt(index);
                    mapping.setSelected(!mapping.isSelected());
                    panel.lstRemoteBranches.repaint();
                    validateBeforeNext();
                }
            }
        });
    }

    @Override
    protected final void validateBeforeNext () {
        setValid(true, null);
        boolean acceptEmptySelection = mode == Mode.ACCEPT_EMPTY_SELECTION;
        if (!acceptEmptySelection && getSelectedRefSpecs().isEmpty()) {
            setValid(false, new Message(NbBundle.getMessage(FetchBranchesPanel.class, "MSG_FetchRefsPanel.errorNoBranchSelected"), true)); //NOI18N
        } else if (acceptEmptySelection && panel.lstRemoteBranches.getModel().getSize() == 0) {
            setValid(true, new Message(NbBundle.getMessage(FetchBranchesPanel.class, "MSG_FetchRefsPanel.errorNoBranch"), true)); //NOI18N
        } else {
            setValid(true, null);
        }
    }

    @Override
    protected JComponent getJComponent () {
        return panel;
    }

    public void setFetchUri (String fetchUri, boolean loadRemoteBranches) {
        if (fetchUri != null && !fetchUri.equals(this.fetchUri) || fetchUri == null && this.fetchUri != null) {
            this.fetchUri = fetchUri;
            if (loadRemoteBranches) {
                refreshRemoteBranches();
            }
        }
    }

    public void setRemote (GitRemoteConfig remote) {
        if (this.remote != remote && (this.remote == null || remote == null)) {
            this.remote = remote;
        }
        validateBeforeNext();
    }

    public void fillRemoteBranches (final Map<String, GitBranch> branches) {
        if (repository == null) {
            fillRemoteBranches(branches, Collections.<String, GitBranch>emptyMap());
        } else {
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
            }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(FetchBranchesPanel.class, "MSG_FetchBranchesPanel.loadingLocalBranches")); //NOI18N
        }
    }
    
    private void fillRemoteBranches (Map<String, GitBranch> branches, Map<String, GitBranch> localBranches) {
        DefaultListModel model = new DefaultListModel();
        List<GitBranch> branchList = new ArrayList<GitBranch>(branches.values());
        Collections.sort(branchList, new Comparator<GitBranch>() {
            @Override
            public int compare (GitBranch b1, GitBranch b2) {
                return b1.getName().compareTo(b2.getName());
            }
        });
        for (GitBranch branch : branchList) {
            model.addElement(new BranchMapping(branch.getName(), remote, localBranches.get(remote.getRemoteName() + "/" + branch.getName())));
        }
        panel.lstRemoteBranches.setModel(model);
        panel.lstRemoteBranches.setEnabled(true);
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (e.getSource() == panel.lstRemoteBranches.getSelectionModel()) {
                validateBeforeNext();
            }
        }
    }

    private void refreshRemoteBranches () {
        assert EventQueue.isDispatchThread();
        cancelBackgroundTasks();
        DefaultListModel model = new DefaultListModel();
        panel.lstRemoteBranches.setModel(model);
        if (fetchUri != null) {
            final String uri = fetchUri;
            model.addElement(NbBundle.getMessage(FetchBranchesPanel.class, "MSG_FetchRefsPanel.loadingBranches")); //NOI18N
            panel.lstRemoteBranches.setEnabled(false);
            Utils.post(new Runnable() {
                @Override
                public void run () {
                    final File tempRepository = Utils.getTempFolder();
                    supp = new GitProgressSupport.NoOutputLogging() {
                        @Override
                        protected void perform () {
                            final Map<String, GitBranch> branches = new HashMap<String, GitBranch>();
                            final Map<String, GitBranch> localBranches = new HashMap<String, GitBranch>();
                            try {
                                GitClient client = getClient();
                                client.init(this);
                                branches.putAll(client.listRemoteBranches(uri, this));
                                if (repository != null) {
                                    RepositoryInfo info = RepositoryInfo.getInstance(repository);
                                    info.refresh();
                                    localBranches.putAll(info.getBranches());
                                }
                            } catch (GitException ex) {
                                GitClientExceptionHandler.notifyException(ex, true);
                            } finally {
                                Utils.deleteRecursively(tempRepository);
                                final GitProgressSupport supp = this;
                                EventQueue.invokeLater(new Runnable () {
                                    @Override
                                    public void run () {
                                        if (!supp.isCanceled()) {
                                            fillRemoteBranches(branches, localBranches);
                                        }
                                    }
                                });
                            }
                        }
                    };
                    supp.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(FetchBranchesPanel.class, "MSG_FetchRefsPanel.loadingBranches")); //NOI18N
                }
            });
        }
    }

    public void cancelBackgroundTasks () {
        if (supp != null) {
            supp.cancel();
        }
        if (validatingSupp != null) {
            validatingSupp.cancel();
        }
    }

    public List<String> getSelectedRefSpecs () {
        List<String> specs = new LinkedList<String>();
        for (Object o : ((DefaultListModel) panel.lstRemoteBranches.getModel()).toArray()) {
            BranchMapping mapping = (BranchMapping) o;
            if (mapping.isSelected()) {
                specs.add(mapping.getRefSpec());
            }
        }
        return specs;
    }

    @Override
    public boolean isFinishPanel () {
        return true;
    }
    
    private static class BranchMapping extends JCheckBox {
        private final String remoteName;
        private final String refSpec;
        private final String label;
        private static enum Mode {
            DELETED(new Color(0x99, 0x99, 0x99), NbBundle.getMessage(FetchBranchesPanel.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.deleted.description"), "D"), //NOI18N
            ADDED(new Color(0, 0x80, 0), NbBundle.getMessage(FetchBranchesPanel.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.added.description"), "A"), //NOI18N
            MODIFIED(new Color(0, 0, 0xff), NbBundle.getMessage(FetchBranchesPanel.class, "LBL_FetchBranchesPanel.BranchMapping.Mode.updated.description"), "U"); //NOI18N
            
            private final String label;
            private final String description;
            private final Color fgColor;
            
            Mode (Color color, String description, String label) {
                this.fgColor = color;
                this.label = label;
                this.description = description;
            }

            @Override
            public String toString () {
                return description;
            }
        }

        public BranchMapping (String remoteName, GitRemoteConfig remote, GitBranch localBranch) {
            this.remoteName = remoteName;
            this.refSpec = MessageFormat.format(REF_SPEC_PATTERN, remoteName, remote.getRemoteName());
            Mode mode;
            if (localBranch == null) {
                mode = Mode.ADDED;
            } else {
                mode = Mode.MODIFIED;
            }
            this.label = MessageFormat.format(BRANCH_MAPPING_LABEL, remoteName, remote.getRemoteName(), mode.label);
            setToolTipText(NbBundle.getMessage(FetchBranchesPanel.class, "LBL_FetchBranchesPanel.BranchMapping.description", new Object[] { localBranch == null ? remote.getRemoteName() + "/" + remoteName : localBranch.getName(), mode.toString() })); //NOI18N
            setForeground(mode.fgColor);
        }

        public String getRemoteName () {
            return remoteName;
        }

        public String getRefSpec () {
            return refSpec;
        }

        @Override
        public String getText () {
            return label;
        }
    }
    
    private static class BranchRenderer implements ListCellRenderer {
        private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox) value;
            checkbox.setBackground(list.getBackground());
            checkbox.setForeground(list.getForeground());
            checkbox.setEnabled(list.isEnabled());
            checkbox.setFont(list.getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return checkbox;
        }
    }

}
