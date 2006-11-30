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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.refactoring.api.impl.RefactoringModule;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.*;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ProgressEvent;


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
public class ParametersPanel extends JPanel implements ProgressListener, ChangeListener{ //[retouche], InvalidationListener {
    // refactoring elements that will be returned as a result of showDialog method
    private RefactoringSession result;
    
    // corresponding implementation of RefactoringUI
    private final RefactoringUI rui;
    // refactoring-specific panel returned from RefactoringUI.getPanel
    private final CustomRefactoringPanel customPanel;
    
    // parent dialog
    private transient JDialog dialog = null;
    // disabled components that should be reenabled by a call to setPanelEnabled
    private ArrayList components = null;
    
    private Problem problem;
    
    private ErrorPanel errorPanel;
    
    private final int PRE_CHECK = 0;
    private final int INPUT_PARAMETERS = 1;
    private final int POST_CHECK = 2;
    
    private transient int currentState = INPUT_PARAMETERS;

    private boolean cancelRequest = false;
    
    /** Enables/disables Preview button of dialog. Can be used by refactoring-specific
     * parameters panel to disable accepting the parameters when needed (e.g. if
     * not all parameters were entered). When the dialog is displayed, the button
     * is disabled by default.
     * @param enabled <code>true</code> to enable preview button, <code>false</code>
     * to disable it.
     */
    public void setPreviewEnabled(boolean enabled) {
        RefactoringPanel.checkEventThread();
        next.setEnabled(enabled);
    }
    
    /** Creates ParametersPanel
     * @param rui Implementation of RefactoringUI for desired refactoring.
     */
    public ParametersPanel(RefactoringUI rui) {
        RefactoringPanel.checkEventThread();
        this.rui = rui;
        initComponents();
        innerPanel.setBorder(null);
        label.setText(" ");//NOI18N
        this.customPanel = rui.getPanel(this);
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
        next = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        help = new javax.swing.JButton();
        checkBoxPanel = new javax.swing.JPanel();
        previewAll = new javax.swing.JCheckBox();
        containerPanel = new javax.swing.JPanel();
        pleaseWait = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setName(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("LBL_FindUsagesDialog"));
        getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("LBL_FindUsagesDialog"));
        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_FindUsagesDialog"));
        progressPanel.setLayout(new java.awt.BorderLayout());

        progressPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 11));
        progressPanel.setOpaque(false);
        innerPanel.setLayout(new java.awt.BorderLayout());

        innerPanel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray));
        label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        innerPanel.add(label, java.awt.BorderLayout.WEST);

        progressPanel.add(innerPanel, java.awt.BorderLayout.CENTER);

        controlsPanel.setLayout(new java.awt.BorderLayout());

        controlsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        buttonsPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        back.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("MNEM_Back").charAt(0));
        org.openide.awt.Mnemonics.setLocalizedText(back, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Back"));
        back.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backActionPerformed(evt);
            }
        });

        buttonsPanel.add(back);
        back.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_Back"));

        org.openide.awt.Mnemonics.setLocalizedText(next, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Finish"));
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        buttonsPanel.add(next);
        next.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_finish"));

        org.openide.awt.Mnemonics.setLocalizedText(cancel, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Cancel"));
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        buttonsPanel.add(cancel);
        cancel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_cancel"));

        org.openide.awt.Mnemonics.setLocalizedText(help, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Help"));
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpActionPerformed(evt);
            }
        });

        buttonsPanel.add(help);
        help.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_help"));

        controlsPanel.add(buttonsPanel, java.awt.BorderLayout.EAST);

        checkBoxPanel.setLayout(new java.awt.BorderLayout());

        checkBoxPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        previewAll.setSelected(((Boolean) RefactoringModule.getOption(getPreviewKeyName(), Boolean.TRUE)).booleanValue());
        org.openide.awt.Mnemonics.setLocalizedText(previewAll, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_PreviewAll"));
        previewAll.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                previewAllItemStateChanged(evt);
            }
        });

        checkBoxPanel.add(previewAll, java.awt.BorderLayout.WEST);
        previewAll.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_PreviewAll"));

        controlsPanel.add(checkBoxPanel, java.awt.BorderLayout.CENTER);

        progressPanel.add(controlsPanel, java.awt.BorderLayout.SOUTH);

        add(progressPanel, java.awt.BorderLayout.SOUTH);

        containerPanel.setLayout(new java.awt.BorderLayout());

        containerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        pleaseWait.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(pleaseWait, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("LBL_PleaseWait"));
        containerPanel.add(pleaseWait, java.awt.BorderLayout.CENTER);

        add(containerPanel, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void previewAllItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_previewAllItemStateChanged
        // used for change default value for previewAll check-box. This value
        // is persisted and then used as default in next IDE run.
        Boolean b = evt.getStateChange() == ItemEvent.SELECTED ? Boolean.TRUE : Boolean.FALSE;
        RefactoringModule.setOption(getPreviewKeyName(), b);
    }//GEN-LAST:event_previewAllItemStateChanged

    /**
     * Constructs key name for the refactoring for previewAll option.
     *
     * @return Key name. It is constructed from "previewAll." string plus
     *         name of class.
     */
    private String getPreviewKeyName() {
        return "previewAll." + rui.getRefactoring().getClass().getName(); // NOI18N
    }

    private void helpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpActionPerformed
        Help help = (Help) Lookup.getDefault().lookup(Help.class);
        help.showHelp(getHelpCtx());
    }//GEN-LAST:event_helpActionPerformed
    
    private void backActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backActionPerformed
        placeCustomPanel();
    }//GEN-LAST:event_backActionPerformed
    
    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        synchronized (this) {
            rui.getRefactoring().cancelRequest();
            result = null;
            dialog.setVisible(false);
            cancelRequest = true;
        }
    }//GEN-LAST:event_cancelActionPerformed
    
    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        
        if (currentState == PRE_CHECK) {
            //next is "Next>"
            placeCustomPanel();
            return;
        }
        
        //next is Finish
        
        setPanelEnabled(false);
        cancel.setEnabled(true);
        
        RequestProcessor rp = new RequestProcessor();
        final int inputState = currentState;
        
        if (currentState != PRE_CHECK) {
            if (rui instanceof RefactoringUIBypass && ((RefactoringUIBypass) rui).isRefactoringBypassRequired()) {
                try{
                    ((RefactoringUIBypass)rui).doRefactoringBypass();
                } catch (final IOException ioe) {
                    currentState = POST_CHECK;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            placeErrorPanel(new Problem(true, ioe.getMessage()));
                        }});
                } finally {
                    if (inputState == currentState) {
                        setVisibleLater(false);
                    }
                    return;
                }
            } else if (currentState != POST_CHECK) {
                result = RefactoringSession.create(rui.getName());
                
                //setParameters and prepare is done asynchronously
                rp.post(new Prepare());
            }
        }
        
        //refactoring is done asynchronously
        
        rp.post(new Runnable() {
            public void run() {
                //inputState != currentState means, that panels changed and dialog will not be closed
                if (inputState == currentState) {
                    try {
                        if (!previewAll.isSelected()) {
//[retouche]                            UndoWatcher.watch(result, ParametersPanel.this);
                            result.addProgressListener(ParametersPanel.this);
                            result.doRefactoring(true);
//[retouche]                            UndoWatcher.stopWatching(ParametersPanel.this);
                        }
                    } finally {
                        if (!previewAll.isSelected()) {
                            result = null;
                        }
                        if (inputState == currentState) {
                            setVisibleLater(false);
                        }
                    }
                }
            }
        });
    }//GEN-LAST:event_nextActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton back;
    private javax.swing.JPanel buttonsPanel;
    public javax.swing.JButton cancel;
    private javax.swing.JPanel checkBoxPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JButton help;
    private javax.swing.JPanel innerPanel;
    private javax.swing.JLabel label;
    private javax.swing.JButton next;
    private javax.swing.JLabel pleaseWait;
    private javax.swing.JCheckBox previewAll;
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
            dialog.getAccessibleContext().setAccessibleDescription(ResourceBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("ACSD_FindUsagesDialog"));
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
                        customPanel.initialize();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            placeErrorPanel(problem);
                            dialog.setVisible(true);
                        }
                    });
                } else {
                    if (customPanel != null)
                        customPanel.initialize();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                placeCustomPanel();
                            }
                        });
                    if (!rui.hasParameters()) {
                        result = RefactoringSession.create(rui.getName());
                        try {
                            rui.getRefactoring().prepare(result);
                        } finally {
                            setVisibleLater(false);
                        }
                    } 
                    
                }
            }
        });
        
        dialog.setVisible(true);
        dialog.dispose(); 
        
        if (rui != null) { 
            rui.getRefactoring().removeProgressListener(this);
        }
        if (!cancelRequest)
            task.waitFinished(); 
        return result;
    }
    
    private final void setCancelStuff() {
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
    
    private void placeErrorPanel(Problem problem) {
        containerPanel.removeAll();
        errorPanel = new ErrorPanel(problem, rui);
        errorPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.add(errorPanel, BorderLayout.CENTER);
        
        next.setEnabled(!problem.isFatal()); 
        dialog.getRootPane().setDefaultButton(next);
        if (currentState == PRE_CHECK ) {
            //calculatePrefferedSize();
            org.openide.awt.Mnemonics.setLocalizedText(next, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Next"));
            back.setVisible(false);
        } else {
            back.setVisible(true);
            back.setEnabled(true);
            dialog.getRootPane().setDefaultButton(back);
        }
        cancel.setEnabled(true); 
        previewAll.setVisible(false);
        repaint();
    }
    
    private void placeCustomPanel() {
        if (customPanel == null) return;
        org.openide.awt.Mnemonics.setLocalizedText(next, org.openide.util.NbBundle.getBundle("org/netbeans/modules/refactoring/spi/ui/Bundle").getString("CTL_Finish"));
        customPanel.setBorder(new EmptyBorder(new Insets(12, 12, 11, 11)));
        containerPanel.removeAll();
        containerPanel.add(customPanel, BorderLayout.CENTER);
        back.setVisible(false);
        previewAll.setVisible(!rui.isQuery());
        currentState = INPUT_PARAMETERS;
        setPanelEnabled(true);
        cancel.setEnabled(true);
        dialog.getRootPane().setDefaultButton(next);
        //Initial errors are ignored by on-line error checker
        //stateChanged(null);
        customPanel.requestFocus();
        repaint();  
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
                        text = NbBundle.getMessage(CustomRefactoringPanel.class, "LBL_ParametersCheck");
                        break;
                    case AbstractRefactoring.PREPARE:
                        text = NbBundle.getMessage(CustomRefactoringPanel.class, "LBL_Prepare");
                        break;
                    case AbstractRefactoring.PRE_CHECK:
                        text = NbBundle.getMessage(CustomRefactoringPanel.class, "LBL_PreCheck");
                        break;
                    default:
                        text = NbBundle.getMessage(CustomRefactoringPanel.class, "LBL_Usages");
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
                    progressHandle.progress(isIndeterminate ? -2 : event.getCount());
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
       SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle.finish();
                progressPanel.remove(progressBar);
                progressPanel.add(innerPanel, BorderLayout.CENTER);
                //progressPanel.validate();
                //setButtonsEnabled(true); 
                //validate();
            }
        });
    }
    
    public void stateChanged(ChangeEvent e) {
        showProblem(rui.checkParameters());
    }
    
    private void showProblem(Problem problem) {
        if (problem == null) {
            label.setText(" "); // NOI18N
            innerPanel.setBorder(null);
            next.setEnabled(true);
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
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Error") + ": </font>" + error + "</html>"); //NOI18N
    }
    
    private void displayWarning(String warning) {
        next.setEnabled(true);
        label.setText("<html><font color=\"red\">" + NbBundle.getMessage(ParametersPanel.class, "LBL_Warning") + ": </font>" + warning + "</html>"); //NOI18N
    }
    
    private class Prepare implements Runnable {
        public void run() {
            if (currentState != POST_CHECK) {
                problem = rui.setParameters();
                if (problem != null) {
                    currentState = POST_CHECK;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            placeErrorPanel(problem);
                        }});
                        return;
                }
            }
            
            try {
                problem = rui.getRefactoring().prepare(result);
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
        next.setEnabled(enabled);
        //cancel.setEnabled(enabled);
        back.setEnabled(enabled);
        previewAll.setEnabled(enabled); 
    }
    
    public HelpCtx getHelpCtx() {
        return rui.getHelpCtx();
    }
    
    public void invalidateObject() {
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
