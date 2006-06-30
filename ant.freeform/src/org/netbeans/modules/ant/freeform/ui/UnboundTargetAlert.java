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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ant.freeform.Actions;
import org.netbeans.modules.ant.freeform.FreeformProject;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.Util;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Alert in case a common global action is invoked by the user but there is no binding.
 * The user is prompted to pick one.
 * @see "#46886"
 * @see <a href="http://projects.netbeans.org/buildsys/freeform-project-ui-spec-promoe.html#Alert_Run_Project">UI Spec</a>
 * @author Jesse Glick
 */
public final class UnboundTargetAlert extends JPanel {
    
    private final FreeformProject project;
    private final String command;
    /** display label of the command */
    private final String label;
    
    /**
     * Create an alert.
     * @param command an action as in {@link ActionProvider}
     */
    public UnboundTargetAlert(FreeformProject project, String command) {
        this.project = project;
        this.command = command;
        label = NbBundle.getMessage(Actions.class, "CMD_" + command);
        initComponents();
        listTargets();
    }
    
    /** Just for testing. */
    public static void main(String[] args) {
        UnboundTargetAlert alert = new UnboundTargetAlert("Twiddle Project", new String[] {"targ1", "targ2", "targ3"}); // NOI18N
        boolean accepted = alert.displayAlert("BazzBuilder"); // NOI18N
        System.out.println("accepted=" + accepted + " value=" + alert.selectCombo.getSelectedItem()); // NOI18N
        System.exit(0);
    }
    private UnboundTargetAlert(String label, String[] targets) {
        project = null;
        command = null;
        this.label = label;
        initComponents();
        selectCombo.setModel(new DefaultComboBoxModel(targets));
        selectCombo.setSelectedItem("");
    }
    
    /**
     * Populate the combo box with (eligible) build targets.
     */
    private void listTargets() {
        FileObject script = FreeformProjectGenerator.getAntScript(project.helper(), project.evaluator());
        if (script != null) {
            List/*<String>*/ targets = Util.getAntScriptTargetNames(script);
            if (targets != null) {
                selectCombo.setModel(new DefaultComboBoxModel((String[]) targets.toArray(new String[targets.size()])));
                selectCombo.setSelectedItem("");
            }
        }
    }

    /**
     * Just show the dialog but do not do anything about it.
     */
    private boolean displayAlert(String projectDisplayName) {
        String title = NbBundle.getMessage(UnboundTargetAlert.class, "UTA_TITLE", label, projectDisplayName);
        final DialogDescriptor d = new DialogDescriptor(this, title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setMessageType(NotifyDescriptor.ERROR_MESSAGE);
        d.setValid(false);
        selectCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                d.setValid(((String) selectCombo.getSelectedItem()).trim().length() > 0);
            }
        });
        Dialog dlg = DialogDisplayer.getDefault().createDialog(d);
        selectCombo.requestFocusInWindow();
        // XXX combo box gets cut off at the bottom unless you do something - why??
        Dimension sz = dlg.getSize();
        dlg.setSize(sz.width, sz.height + 30);
        dlg.setVisible(true);
        return d.getValue() == NotifyDescriptor.OK_OPTION;
    }
    
    /**
     * Show the alert.
     * If accepted, generate a binding for the command (and add a context menu item for the project).
     * @return true if the alert was accepted and there is now a binding, false if cancelled
     * @throws IOException if there is a problem writing bindings
     */
    public boolean accepted() throws IOException {
        String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        if (displayAlert(projectDisplayName)) {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                    public Object run() throws IOException {
                        generateBindingAndAddContextMenuItem();
                        ProjectManager.getDefault().saveProject(project);
                        return null;
                    }
                });
            } catch (MutexException e) {
                throw (IOException) e.getException();
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Generate the action binding itself.
     * Also add the context menu item for the new command.
     */
    void generateBindingAndAddContextMenuItem() {
        List/*<FreeformProjectGenerator.TargetMapping>*/ mappings = FreeformProjectGenerator.getTargetMappings(project.helper());
        FreeformProjectGenerator.TargetMapping mapping = new FreeformProjectGenerator.TargetMapping();
        mapping.name = command;
        mapping.script = TargetMappingPanel.defaultAntScript(project.evaluator());
        mapping.targets = Collections.list(new StringTokenizer((String) selectCombo.getSelectedItem()));
        mappings.add(mapping);
        FreeformProjectGenerator.putTargetMappings(project.helper(), mappings);
        FreeformProjectGenerator.putContextMenuAction(project.helper(), mappings);
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        introLabel = new javax.swing.JLabel();
        explanation = new javax.swing.JTextArea();
        selectLabel = new javax.swing.JLabel();
        selectCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(introLabel, org.openide.util.NbBundle.getMessage(UnboundTargetAlert.class, "UTA_LBL_intro", label));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(introLabel, gridBagConstraints);

        explanation.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        explanation.setEditable(false);
        explanation.setLineWrap(true);
        explanation.setText(org.openide.util.NbBundle.getMessage(UnboundTargetAlert.class, "UTA_TEXT_explanation", label));
        explanation.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(explanation, gridBagConstraints);

        selectLabel.setLabelFor(selectCombo);
        org.openide.awt.Mnemonics.setLocalizedText(selectLabel, org.openide.util.NbBundle.getMessage(UnboundTargetAlert.class, "UTA_LBL_select", label));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(selectLabel, gridBagConstraints);

        selectCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(selectCombo, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea explanation;
    private javax.swing.JLabel introLabel;
    private javax.swing.JComboBox selectCombo;
    private javax.swing.JLabel selectLabel;
    // End of variables declaration//GEN-END:variables

    /** For unit testing only. */
    void simulateTargetSelection(String comboText) {
        selectCombo.setSelectedItem(comboText);
    }
    
}
