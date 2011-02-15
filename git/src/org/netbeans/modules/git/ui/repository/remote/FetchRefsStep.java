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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.AsynchronousValidatingPanel;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class FetchRefsStep extends AbstractWizardPanel implements AsynchronousValidatingPanel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ActionListener, DocumentListener, ListSelectionListener {
    private final FetchRefsPanel panel;
    private String fetchUri;
    private GitRemoteConfig remote;
    private GitProgressSupport supp;
    private GitProgressSupport validatingSupp;
    private static final String ALL_BRANCHES_FETCH_REF_SPEC = "+refs/heads/*:refs/remotes/{0}/*"; //NOI18N
    private static final String BRANCH_FETCH_REF_SPEC = "+refs/heads/{0}:refs/remotes/{1}/{2}"; //NOI18N
    private Map<JComponent, Boolean> inputComponents;
    private final Mode mode;

    public static enum Mode {
        ACCEPT_EMPTY_SELECTION,
        ACCEPT_NON_EMPTY_SELECTION_ONLY,
        ACCEPT_NON_EMPTY_SELECTION_ONLY_VALIDATE_SELECTED
    }

    public FetchRefsStep (Mode mode) {
        this.mode = mode;
        this.panel = new FetchRefsPanel();
        fillPanel();
        setInputComponents();
        attachListeners();
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
    }
    
    private void attachListeners () {
        panel.btnAddNew.addActionListener(this);
        panel.btnAddSelected.addActionListener(this);
        panel.btnAddAll.addActionListener(this);
        panel.btnRemoveSelected.addActionListener(this);
        panel.txtNewRef.getDocument().addDocumentListener(this);
        panel.lstRemoteBranches.getSelectionModel().addListSelectionListener(this);
        panel.lstRefs.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    protected final void validateBeforeNext () {
        if (EventQueue.isDispatchThread()) {
            boolean acceptEmptySelection = mode == Mode.ACCEPT_EMPTY_SELECTION;
            if (!acceptEmptySelection && panel.lstRefs.getSelectedValues().length == 0) {
                setValid(false, new Message(NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.errorNoSpecSelected"), false)); //NOI18N
            } else if (acceptEmptySelection && panel.lstRefs.getModel().getSize() == 0) {
                setValid(true, new Message(NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.errorNoSpec"), true)); //NOI18N
            } else {
                setValid(true, null);
            }
        } else {
            if (panel.lstRefs.getModel().getSize() != 0) {
                final File tempRepo = Utils.getTempFolder();
                final Message[] message = new Message[1];
                validatingSupp = new GitProgressSupport.NoOutputLogging() {
                    @Override
                    protected void perform () {
                        try {
                            GitClient client = getClient();
                            client.init(this);
                            RemoteConfig remote = new RemoteConfig(FetchRefsStep.this.remote);
                            List<String> refs = new LinkedList<String>();
                            Object[] toValidate = mode == Mode.ACCEPT_NON_EMPTY_SELECTION_ONLY_VALIDATE_SELECTED
                                    ? panel.lstRefs.getSelectedValues()
                                    : ((DefaultListModel) panel.lstRefs.getModel()).toArray();
                            for (Object o : toValidate) {
                                refs.add((String) o);
                            }
                            remote.setFetchRefSpecs(refs);
                            if (!isCanceled()) {
                                client.setRemote(remote, this);
                            }
                        } catch (GitException ex) {
                            Throwable t = ex;
                            while (t.getCause() != null) {
                                t = t.getCause();
                            }
                            message[0] = new Message(NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.errorInvalidSpec", t.getLocalizedMessage()), false); //NOI18N
                        } finally {
                            Utils.deleteRecursively(tempRepo);
                        }
                    }
                };
                validatingSupp.start(Git.getInstance().getRequestProcessor(tempRepo), tempRepo, NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.validatingSpecs")).waitFinished();
                if (message[0] != null) {
                    setValid(false, message[0]);
                }
                if (validatingSupp.isCanceled()) {
                    setValid(false, new Message(null, true));
                }
            }
            //enable input
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    setEnabled(true);
                }
            });
        }
    }

    @Override
    public void prepareValidation () {
        setEnabled(false);
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
        if (this.remote != remote && (this.remote == null || remote == null || !remote.getFetchRefSpecs().equals(this.remote.getFetchRefSpecs()))) {
            this.remote = remote;
            fillPanel();
        }
        validateBeforeNext();
    }

    public void fillRemoteBranches (Map<String, GitBranch> branches) {
        DefaultListModel model = new DefaultListModel();
        for (GitBranch branch : branches.values()) {
            model.addElement(branch.getName());
        }
        panel.lstRemoteBranches.setModel(model);
        panel.lstRemoteBranches.setEnabled(true);
    }
    
    private void fillPanel () {
        DefaultListModel model = new DefaultListModel();
        panel.btnAddAll.setEnabled(false);
        if (remote != null) {
            model.setSize(remote.getFetchRefSpecs().size());
            int i = 0;
            for (String refSpec : remote.getFetchRefSpecs()) {
                model.set(i++, refSpec);
            }
            panel.btnAddAll.setEnabled(true);
        }
        panel.lstRefs.setModel(model);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnRemoveSelected) {
            panel.lstRefs.getSelectionModel().setValueIsAdjusting(true);
            for (Object toRemove : panel.lstRefs.getSelectedValues()) {
                ((DefaultListModel) panel.lstRefs.getModel()).removeElement(toRemove);
            }
            panel.lstRefs.getSelectionModel().setValueIsAdjusting(false);
            validateBeforeNext();
        } else if (e.getSource() == panel.btnAddNew) {
            String candidate = panel.txtNewRef.getText().trim();
            addCandidate(candidate);
        } else if (e.getSource() == panel.btnAddAll) {
            String candidate = MessageFormat.format(ALL_BRANCHES_FETCH_REF_SPEC, remote.getRemoteName());
            addCandidate(candidate);
        } else if (e.getSource() == panel.btnAddSelected) {
            String candidate = (String) panel.lstRemoteBranches.getSelectedValue();
            candidate = MessageFormat.format(BRANCH_FETCH_REF_SPEC, candidate, remote.getRemoteName(), candidate);
            addCandidate(candidate);
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        validateAdd();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        validateAdd();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }
    
    private void validateAdd () {
        panel.btnAddNew.setEnabled(!panel.txtNewRef.getText().trim().isEmpty());
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            panel.btnAddSelected.setEnabled(panel.lstRemoteBranches.getSelectedIndices().length > 0);
            panel.btnRemoveSelected.setEnabled(panel.lstRefs.getSelectedIndices().length > 0);
            if (e.getSource() == panel.lstRefs.getSelectionModel() && mode != Mode.ACCEPT_EMPTY_SELECTION) {
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
            model.addElement(NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.loadingBranches")); //NOI18N
            panel.lstRemoteBranches.setEnabled(false);
            Utils.post(new Runnable() {
                @Override
                public void run () {
                    final File tempRepository = Utils.getTempFolder();
                    supp = new GitProgressSupport.NoOutputLogging() {
                        @Override
                        protected void perform () {
                            final Map<String, GitBranch> branches = new HashMap<String, GitBranch>();
                            try {
                                GitClient client = getClient();
                                client.init(this);
                                branches.putAll(client.listRemoteBranches(uri, this));
                            } catch (GitException ex) {
                                GitClientExceptionHandler.notifyException(ex, true);
                            } finally {
                                Utils.deleteRecursively(tempRepository);
                                final GitProgressSupport supp = this;
                                EventQueue.invokeLater(new Runnable () {
                                    @Override
                                    public void run () {
                                        if (!supp.isCanceled()) {
                                            fillRemoteBranches(branches);
                                        }
                                    }
                                });
                            }
                        }
                    };
                    supp.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(FetchRefsPanel.class, "MSG_FetchRefsPanel.loadingBranches")); //NOI18N
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
    
    List<String> getRefSpecs () {
        DefaultListModel m = (DefaultListModel) panel.lstRefs.getModel();
        List<String> specs = new LinkedList<String>();
        for (Object spec : m.toArray()) {
            specs.add((String) spec);
        }
        return specs;
    }
    
    public List<String> getSelectedRefSpecs () {
        List<String> specs = new LinkedList<String>();
        for (Object spec : panel.lstRefs.getSelectedValues()) {
            specs.add((String) spec);
        }
        return specs;
    }

    private void addCandidate (String addCandidate) {
        int row = ((DefaultListModel) panel.lstRefs.getModel()).indexOf(addCandidate);
        if (row == -1) {
            panel.lstRefs.getSelectionModel().setValueIsAdjusting(true);
            ((DefaultListModel) panel.lstRefs.getModel()).add(0, addCandidate);
            if (panel.lstRefs.isSelectionEmpty()) {
                panel.lstRefs.setSelectedIndex(0);
            } else {
                panel.lstRefs.setSelectedIndex(panel.lstRefs.getSelectedIndex() + 1);
            }
            panel.lstRefs.getSelectionModel().setValueIsAdjusting(false);
            row = 0;
        } else {
            panel.lstRefs.setSelectedIndex(row);
        }
        panel.lstRefs.scrollRectToVisible(panel.lstRefs.getCellBounds(row, row));
        validateBeforeNext();
    }

    private void setEnabled (boolean enabled) {
        for (Map.Entry<JComponent, Boolean> e : inputComponents.entrySet()) {
            JComponent comp = e.getKey();
            if (enabled) {
                comp.setEnabled(e.getValue());
            } else {
                e.setValue(comp.isEnabled());
                comp.setEnabled(false);
            }
        }
    }

    private void setInputComponents () {
        inputComponents = new HashMap<JComponent, Boolean>(7);
        inputComponents.put(panel.btnAddNew, false);
        inputComponents.put(panel.btnAddSelected, false);
        inputComponents.put(panel.btnAddAll, false);
        inputComponents.put(panel.btnRemoveSelected, false);
        inputComponents.put(panel.txtNewRef, false);
        inputComponents.put(panel.lstRemoteBranches, false);
        inputComponents.put(panel.lstRefs, false);
    }

    @Override
    public boolean isFinishPanel () {
        return true;
    }
}
