/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProblemDetails;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.impl.UndoWatcher;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.*;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.impl.ProblemComponent.CallbackAction;
import org.openide.awt.Mnemonics;


/** Main panel for refactoring parameters dialog. This panel is automatically displayed
 * by refactoring action. It handles all the generic logic for displaying progress,
 * checking for problems (during parameters validation and refactoring preparation) and
 * accepting/canceling the refactoring. Refactoring-specific parameters panel is
 * requested from {@link RefactoringUI} implementation (passed to this panel by {@link
 * AbstractRefactoringAction}) and then displayed in the upper part of this panel.
 * Refactoring-specific panel can use setPreviewEnabled method to enable/disable
 * button that accepts the parameters. The button is disabled by default.
 *
 * @author Martin Matula, Jan Becicka
 */
public class ParametersPanel extends JPanel implements ProgressListener, ChangeListener, InvalidationListener {
    private static final Logger LOGGER = Logger.getLogger(ParametersPanel.class.getName());
    /** @see #result */
    private final Object RESULT_LOCK = new Object();
    // refactoring elements that will be returned as a result of showDialog method
    private RefactoringSession result;
    
    // corresponding implementation of RefactoringUI
    private final RefactoringUI rui;
    // refactoring-specific panel returned from RefactoringUI.getPanel
    private final JPanel customPanel;
    private final CustomRefactoringPanel customComponent;
    
    // parent dialog
    private transient JDialog dialog = null;
    // disabled components that should be reenabled by a call to setPanelEnabled
    private ArrayList components = null;
    
    private Problem problem;
    
    private ErrorPanel errorPanel;
    
    private final int PRE_CHECK = 0;
    private final int INPUT_PARAMETERS = 1;
    private final int POST_CHECK = 2;
    private final int CHECK_PARAMETERS = 3;
    
    private transient int currentState = INPUT_PARAMETERS;

    private boolean cancelRequest = false;
    private boolean canceledDialog;
    
    /** Enables/disables Preview button of dialog. Can be used by refactoring-specific
     * parameters panel to disable accepting the parameters when needed (e.g. if
     * not all parameters were entered). When the dialog is displayed, the button
     * is disabled by default.
     * @param enabled <code>true</code> to enable preview button, <code>false</code>
     * to disable it.
     */
    public void setPreviewEnabled(boolean enabled) {
        RefactoringPanel.checkEventThread();
        next.setEnabled(enabled && !isPreviewRequired());
    }
    
    /** Creates ParametersPanel
     * @param rui Implementation of RefactoringUI for desired refactoring.
     */
    public ParametersPanel(RefactoringUI rui) {
        RefactoringPanel.checkEventThread();
        this.rui = rui;
        initComponents();

        // #143551 
        HelpCtx helpCtx = getHelpCtx();
        help.setEnabled(helpCtx != null && helpCtx != HelpCtx.DEFAULT_HELP);

        innerPanel.setBorder(null);
        label.setText(" ");//NOI18N
        this.customComponent = rui.getPanel(this);
        if (this.customComponent!=null) {
            this.customPanel = (JPanel) this.customComponent.getComponent();
        } else {
            customPanel = null;
        }
        errorPanel = new ErrorPanel(rui);
        calculatePrefferedSize();
        setButtonsEnabled(false);
        //cancel.setEnabled(false);
    }
    
    
    private void calculatePrefferedSize() {
        Dimension cpDim = new Dimension(0,0);
        Dimension ppDim = progressPanel.getPreferredSize();
        Dimension epDim = new Dimension(0,0);
        if (customPanel != null) {
            cpDim = customPanel.getPreferredSize();
        }
        if (errorPanel != null) {
            epDim = errorPanel.getPreferredSize();
        }
        
        setPreferredSize(new Dimension(Math.max(Math.max(cpDim.width, ppDim.width),epDim.width) , Math.max(cpDim.height, epDim.height) + ppDim.height));
        //validate();
        if (dialog != null && rui.isQuery()) {
            dialog.setSize(getPreferredSize());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        progressPanel = new javax.swing.JPanel();
        innerPanel = new javax.swing.JPanel();
        label = new javax.swing.JLabel();
        controlsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        back = new javax.swing.JButton();
        previewButton = new javax.swing.JButton();
        previewButton.setVisible(false);
        next = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        help = new javax.swing.JButton();
        containerPanel = new javax.swing.JPanel();
        pleaseWait = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle"); // NOI18N
        setName(bundle.getString("LBL_FindUsagesDialog")); // NOI18N
        setLayout(new java.awt.BorderLayout());

        progressPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 11));
        progressPanel.setOpaque(false);
        progressPanel.setLayout(new java.awt.BorderLayout());

        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(128, 128, 128)));
        innerPanel.setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        innerPanel.add(label, java.awt.BorderLayout.WEST);

        progressPanel.add(innerPanel, java.awt.BorderLayout.CENTER);

        controlsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        controlsPanel.setLayout(new java.awt.BorderLayout());

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonsPanel.setLayout(new java.awt.GridLayout(1, 0, 4, 0));

        org.openide.awt.Mnemonics.setLocalizedText(back, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Back")); // NOI18N
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });
        buttonsPanel.add(back);
        back.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Back")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(previewButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "CTL_PreviewAll")); // NOI18N
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preview(evt);
            }
        });
        buttonsPanel.add(previewButton);
        previewButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.previewButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(next, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Finish")); // NOI18N
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refactor(evt);
            }
        });
        buttonsPanel.add(next);
        next.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_finish")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cancel, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Cancel")); // NOI18N
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        buttonsPanel.add(cancel);
        cancel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_cancel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(help, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("CTL_Help")); // NOI18N
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });
        buttonsPanel.add(help);
        help.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_help")); // NOI18N

        controlsPanel.add(buttonsPanel, java.awt.BorderLayout.EAST);

        progressPanel.add(controlsPanel, java.awt.BorderLayout.SOUTH);

        add(progressPanel, java.awt.BorderLayout.SOUTH);

        containerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        containerPanel.setLayout(new java.awt.BorderLayout());

        pleaseWait.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(pleaseWait, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/impl/Bundle").getString("LBL_PleaseWait")); // NOI18N
        containerPanel.add(pleaseWait, java.awt.BorderLayout.CENTER);
        pleaseWait.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_PleaseWait")); // NOI18N

        add(containerPanel, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(bundle.getString("LBL_FindUsagesDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_FindUsagesDialog")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void preview(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preview
        refactor(true);
}//GEN-LAST:event_preview

    private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
        Help help = (Help) Lookup.getDefault().lookup(Help.class);
        help.showHelp(getHelpCtx());
    }//GEN-LAST:event_helpActionPerformed
    
    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        placeCustomPanel();
    }//GEN-LAST:event_backActionPerformed
    
    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        synchronized (this) {
            canceledDialog = true;
            if (evt!=null && evt.getSource() instanceof Cancellable) {
                putResult(null);
                dialog.setVisible(false);
            } else {
                rui.getRefactoring().cancelRequest();
                putResult(null);
                dialog.setVisible(false);
                cancelRequest = true;
            }
        }
    }//GEN-LAST:event_cancelActionPerformed
    private void refactor(final boolean previewAll) {
        LOGGER.finest("refactor - currentState="+currentState);
        if (currentState == PRE_CHECK) {
            LOGGER.finest("refactor - PRE_CHECK");
            //next is "Next>"
            placeCustomPanel();
            return;
        }
        
        if (currentState == POST_CHECK && previewAll && currentProblemAction!=null) {
            LOGGER.finest("refactor - POST_CHECK - problems");
            Cancellable doCloseParent = new Cancellable() {
                public boolean cancel() {
                    cancelActionPerformed(new ActionEvent(this,0,null));
                    return true;
                }
            };
            currentProblemAction.showDetails(new CallbackAction(rui), doCloseParent);
            return;
        }
        
        //next is Finish
        
        setPanelEnabled(false);
        cancel.setEnabled(true);
        
        RequestProcessor rp = new RequestProcessor();
        final int inputState = currentState;
        
        if (currentState != PRE_CHECK && currentState != POST_CHECK) {
            if (rui instanceof RefactoringUIBypass && ((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
                LOGGER.finest("refactor - bypass");
                try{
                    ((RefactoringUIBypass)rui).doRefactoringBypass();
                } catch (final IOException ioe) {
                    currentState = POST_CHECK;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String message = ioe.getMessage();
                            message = message != null ? message : ""; // NOI18N
                            placeErrorPanel(new Problem(true, message));
                        }});
                } finally {
                    if (inputState == currentState) {
                        setVisibleLater(false);
                    }
                    return;
                }
            } else if (currentState != POST_CHECK && currentState != CHECK_PARAMETERS) {
                putResult(RefactoringSession.create(rui.getName()));
                //setParameters and prepare is done asynchronously
                rp.post(new Prepare());
            } else if(currentState == CHECK_PARAMETERS) {
                rp.post(new Prepare());
            }
        }
        
        //refactoring is done asynchronously
        LOGGER.finest("refactor - asynchronously");
        rp.post(new Runnable() {
            public void run() {
                //inputState != currentState means, that panels changed and dialog will not be closed
                LOGGER.finest("refactor - inputState=" + inputState + ", currentState=" + currentState);
                if (inputState == currentState) {
                    try {
                        RefactoringSession session = getResult();
                        if (!previewAll && session != null) {
                            UndoWatcher.watch(session, ParametersPanel.this);
                            session.addProgressListener(ParametersPanel.this);
                            session.doRefactoring(true);
                            UndoWatcher.stopWatching(ParametersPanel.this);
                        }
                    } finally {
                        if (!previewAll) {
                            putResult(null);
                        }
                        if (inputState == currentState) {
                            setVisibleLater(false);
                        }
                    }
                }
            }
        });        
    }
        
    private void refactor(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refactor
        refactor(rui.isQuery());    
}//GEN-LAST:event_refactor
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back;
    private javax.swing.JPanel buttonsPanel;
    public javax.swing.JButton cancel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton help;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JLabel label;
    private javax.swing.JButton next;
    private javax.swing.JLabel pleaseWait;
    private javax.swing.JButton previewButton;
    private javax.swing.JPanel progressPanel;
    // End of variables declaration//GEN-END:variables
    
    /**
     * dialog is closed asynchronously on the AWT event thread
     */
    private void setVisibleLater(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(visible);
            }
        });
    }
    
    // disables/re-enables components in the custom panel
    private void setPanelEnabled(boolean enabled) {
        RefactoringPanel.checkEventThread();
        setButtonsEnabled(enabled);
        if (enabled) {
            if (components == null) return;
            for (Iterator it = components.iterator(); it.hasNext();) {
                ((Component) it.next()).setEnabled(true);
            }
            components = null;
        } else {
            if (components != null) return;
            components = new ArrayList();
            disableComponents(customPanel);
        }
    }
    
    // disables all components in the custom panel
    private void disableComponents(Container c) {
        RefactoringPanel.checkEventThread();
        Component children[] = c.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isEnabled()) {
                children[i].setEnabled(false);
                components.add(children[i]);
            }
            if (children[i] instanceof Container) {
                disableComponents((Container) children[i]);
            }
        }
    }
    
    /** Method used by AbstractRefactoringAction to display refactoring parameters
     * dialog. Constructs a dialog consisting of this panel and Preview and Cancel
     * buttons. Let's user to enter refactoring parameters.
     * @return Collection of refactoring elements returned from the refactoring
     * operation or <code>null</code> if the operation was cancelled.
     */
    public synchronized RefactoringSession showDialog() {
        RefactoringPanel.checkEventThread();
        if (rui != null) {
            rui.getRefactoring().addProgressListener(this);
        }
        String title = (customPanel != null && customPanel.getName()!=null && !"".equals(customPanel.getName()))?customPanel.getName() : rui.getName();
        DialogDescriptor descriptor = new DialogDescriptor(this, title, true, new Object[]{}, null, 0, null, null);
        
        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.validate();
        if (customPanel!=null) {
            dialog.getAccessibleContext().setAccessibleName(rui.getName());
            dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ParametersPanel.class, "ACSD_FindUsagesDialog"));
        }
        
        setCancelStuff();
        
        dialog.pack();
        
        RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    problem = rui.getRefactoring().preCheck();
                } catch (RuntimeException e) {
                    setVisibleLater(false);
                    throw e;
                }
                if (problem != null) {
                    currentState = PRE_CHECK;
                    if (!problem.isFatal()) {
                        customComponent.initialize();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            placeErrorPanel(problem);
                            dialog.setVisible(true);
                        }
                    });
                } else {
                    if (customPanel != null)
                        customComponent.initialize();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                placeCustomPanel();
                            }
                        });
                    if (!rui.hasParameters()) {
                        RefactoringSession session = putResult(RefactoringSession.create(rui.getName()));
                        try {
                            rui.getRefactoring().prepare(session);
                        } finally {
                            setVisibleLater(false);
                        }
                    } 
                    
                }
            }
        });
        
        if (!(customComponent==null && !rui.hasParameters() && !APIAccessor.DEFAULT.hasPluginsWithProgress(rui.getRefactoring())))
            dialog.setVisible(true);
        dialog.dispose();
        descriptor.setMessage("");
        
        if (rui != null) { 
            rui.getRefactoring().removeProgressListener(this);
        }
        if (!cancelRequest)
            task.waitFinished(); 
        RefactoringSession temp = getResult();
        putResult(null);
        return temp;
    }
    
    private final void setCancelStuff() {
        canceledDialog = false;
        KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        Object actionKey = "cancel"; // NOI18N
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(k, actionKey);
        Action cancelAction = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {
                if (cancel.isEnabled())
                    cancelActionPerformed(ev);
            }
        }; 
        
        getRootPane().getActionMap().put(actionKey, cancelAction);
        
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                if (cancel.isEnabled())
                    cancelActionPerformed(null);
            }
        });
    }

    boolean isCanceledDialog() {
        return canceledDialog;
    }
    
    private ProblemDetails getDetails(Problem problem) {
        if (problem.getNext()==null) {
            return problem.getDetails();
        }
        return null;
    }
    private ProblemDetails currentProblemAction;
    private void placeErrorPanel(Problem problem) {
        containerPanel.removeAll();
        errorPanel = new ErrorPanel(problem, rui);
        errorPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.add(errorPanel, BorderLayout.CENTER);
        
        next.setEnabled(!problem.isFatal() && !isPreviewRequired()); 
        dialog.getRootPane().setDefaultButton(next);
        if (currentState == PRE_CHECK ) {
            //calculatePrefferedSize();
            Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class,"CTL_Next"));
            back.setVisible(false);
        } else {
            ProblemDetails details = getDetails(problem);
            if (details!=null) {
                Mnemonics.setLocalizedText(previewButton, details.getDetailsHint());            
                previewButton.setVisible(true);
                previewButton.setEnabled(true);
                currentProblemAction = details;
            }
            back.setVisible(true);
            back.setEnabled(true);
            dialog.getRootPane().setDefaultButton(back);
            if (details!=null) {
                setPreferredSize(null);
                dialog.pack();
            }
        }
        cancel.setEnabled(true); 
        previewButton.setEnabled(!problem.isFatal());
        if (progressHandle!=null) {
            stop(new ProgressEvent(this, ProgressEvent.STOP));
        }
        repaint();
    }
    
    private void placeCustomPanel() {
        if (customPanel == null) return;
        Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, rui.isQuery()?"CTL_Find": "CTL_Finish"));
        Mnemonics.setLocalizedText(previewButton, NbBundle.getMessage(ParametersPanel.class, "CTL_PreviewAll"));
        customPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.removeAll();
        containerPanel.add(customPanel, BorderLayout.CENTER);
        back.setVisible(false);
        previewButton.setVisible(!rui.isQuery());
        Boolean b = rui.getRefactoring().getContext().lookup(Boolean.class);
        next.setEnabled(!isPreviewRequired());
        currentState = INPUT_PARAMETERS;
        setPanelEnabled(true);
        cancel.setEnabled(true);
        dialog.getRootPane().setDefaultButton(next);
        //Initial errors are ignored by on-line error checker
        //stateChanged(null);
        if (customPanel.isEnabled()) 
            customPanel.requestFocus();
        setOKorRefactor();
        repaint();  
    }
    
    private boolean isPreviewRequired() {
        UI.Constants b = rui.getRefactoring().getContext().lookup(UI.Constants.class);
        return b!=null && b==UI.Constants.REQUEST_PREVIEW;
    }
    
    private ProgressBar progressBar;
    private ProgressHandle progressHandle;
    private boolean isIndeterminate;
    
    /** Implementation of ProgressListener.start method. Displays progress bar and
     * sets progress label and progress bar bounds.
     * @param event Event object.
     */
    public void start(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressPanel.remove(innerPanel);
                progressBar = ProgressBar.create(progressHandle = ProgressHandleFactory.createHandle("")); //NOI18N
                progressPanel.add(progressBar, BorderLayout.CENTER);
                //progressPanel.validate();
                if (event.getCount()==-1) {
                    isIndeterminate = true;
                    progressHandle.start();
                    progressHandle.switchToIndeterminate();
                } else {
                    isIndeterminate = false;                    
                    progressHandle.start(event.getCount());
                }
                
                String text;
                switch (event.getOperationType()) {
                    case AbstractRefactoring.PARAMETERS_CHECK:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_ParametersCheck");
                        break;
                    case AbstractRefactoring.PREPARE:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_Prepare");
                        break;
                    case AbstractRefactoring.PRE_CHECK:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_PreCheck");
                        break;
                    default:
                        text = NbBundle.getMessage(ParametersPanel.class, "LBL_Usages");
                        break;
                }
                progressBar.setString(text); //NOI18N
                
                progressPanel.setVisible(true);
                
                setButtonsEnabled(false);
            }
        });
    }
    
    /** Implementation of ProgressListener.step method. Increments progress bar value
     * by 1.
     * @param event Event object.
     */
    public void step(final ProgressEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (progressHandle == null) {
                        return;
                    }

                    if (isIndeterminate && event.getCount() > 0) {
                        progressHandle.switchToDeterminate(event.getCount());
                        isIndeterminate = false;
                    } else {
                        progressHandle.progress(isIndeterminate ? -2 : event.getCount());
                    }
                } catch (Throwable e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        });
    }
    
    /** Implementation of ProgressListener.stop method. Sets progress bar value to
     * its maximum.
     * @param event Event object.
     */
    public void stop(ProgressEvent event) {
        Runnable run = new Runnable() {
            public void run() {
                if (progressHandle == null) {
                    return;
                }
                progressHandle.finish();
                progressPanel.remove(progressBar);
                progressPanel.add(innerPanel, BorderLayout.CENTER);
                //progressPanel.validate();
                //setButtonsEnabled(true); 
                //validate();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        if (rui instanceof RefactoringUIBypass && ((RefactoringUIBypass)rui).isRefactoringBypassRequired()) {
                showProblem(null);
        } else {
            showProblem(rui.checkParameters());
        }
        setOKorRefactor();
    }
    
    private void setOKorRefactor() {
        if (rui instanceof RefactoringUIBypass) {
            if (((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
                next.setText(NbBundle.getMessage(DialogDisplayer.class, "CTL_OK"));
                previewButton.setVisible(false);
            } else {
                Mnemonics.setLocalizedText(next, NbBundle.getMessage(ParametersPanel.class, rui.isQuery()?"CTL_Find": "CTL_Finish"));
                previewButton.setVisible(true);
            }
        }
    }
    
    
    private void showProblem(Problem problem) {
        if (problem == null) {
            label.setText(" "); // NOI18N
            innerPanel.setBorder(null);
            next.setEnabled(!isPreviewRequired());
            previewButton.setEnabled(true);
            return;
        }
        innerPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.gray));
        progressPanel.setVisible(true);
        if (problem.isFatal()) {
            displayError(problem.getMessage());
        } else {
            displayWarning(problem.getMessage());
        }
    }
    
    private void displayError(String error) {
        next.setEnabled(false);
        previewButton.setEnabled(false);
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Error") + ": </font>" + error + "</html>"); //NOI18N
    }
    
    private void displayWarning(String warning) {
        next.setEnabled(!isPreviewRequired());
        previewButton.setEnabled(true);
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Warning") + ": </font>" + warning + "</html>"); //NOI18N
    }
    
    private class Prepare implements Runnable {
        public void run() {
            if (currentState != POST_CHECK && currentState != CHECK_PARAMETERS) {
                problem = rui.setParameters();
                if (problem != null && currentState!=POST_CHECK) {
                    currentState = CHECK_PARAMETERS;
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                placeErrorPanel(problem);
                            }});
                    } catch (Exception ie) {
                        throw (RuntimeException) new RuntimeException().initCause(ie);
                    }
                    return;
                }
            }
            
            try {
                problem = rui.getRefactoring().prepare(getResult());
            } catch (RuntimeException e) {
                setVisibleLater(false);
                throw e;
            }
	
            if (problem != null) {
                currentState = POST_CHECK;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        placeErrorPanel(problem);
                    }});
            }
        }
    }
    
    private void setButtonsEnabled(boolean enabled) {
        next.setEnabled(enabled && !isPreviewRequired());
        //cancel.setEnabled(enabled);
        back.setEnabled(enabled);
        previewButton.setEnabled(enabled); 
    }
    
    public HelpCtx getHelpCtx() {
        return rui.getHelpCtx();
    }
    
    public void invalidateObject() {
    }

    private RefactoringSession getResult() {
        synchronized (RESULT_LOCK) {
            return result;
        }
    }

    private RefactoringSession putResult(RefactoringSession session) {
        synchronized (RESULT_LOCK) {
            this.result = session;
        }
        return session;
    }
    
    private static class ProgressBar extends JPanel {
        
        private JLabel label;
        
        private static ProgressBar create(ProgressHandle handle) {
            ProgressBar instance = new ProgressBar();
            instance.setLayout(new BorderLayout());
            instance.label = new JLabel();
            instance.label.setBorder(new EmptyBorder(0, 0, 2, 0));
            instance.add(instance.label, BorderLayout.NORTH);
            JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
            instance.add(progress, BorderLayout.CENTER);
            return instance;
        }
        
        public void setString(String value) {
            label.setText(value);
        }
        
        private ProgressBar() {
        }
        
    }
    
}
