/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss.ui.actions;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Maros Sandor
 */
public class ChangeCVSRootAction extends AbstractAction {
    
    private final String RECENT_CHANGED_ROOTS = "recentChangedRoots";

    private final VCSContext ctx;

    public ChangeCVSRootAction(String name, VCSContext ctx) {
        super(name);
        this.ctx = ctx;
    }

    public boolean isEnabled() {
        return ctx.getRootFiles().size() > 0 && allDirectories();
    }

    private boolean allDirectories() {
        for (File file : ctx.getRootFiles()) {
            if (!file.isDirectory()) return false;
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        ResourceBundle loc = NbBundle.getBundle(ChangeCVSRootAction.class);

        final ChangeCVSRootPanel panel = new ChangeCVSRootPanel();
        panel.getWorkingCopy().setText(getWorkingCopy());
        panel.getCurrentCVSRoot().setText(getCurrentRoot());
        
        List<String> roots = Utils.getStringList(CvsModuleConfig.getDefault().getPreferences(), RECENT_CHANGED_ROOTS);
        panel.getNewRootCombo().setModel(new DefaultComboBoxModel(roots.toArray()));
        if (roots.size() > 0) {
            panel.getNewRootCombo().setSelectedItem(roots.get(0));
        } else {
            panel.getNewRootCombo().setSelectedItem(getNewRoot());
        }
        
        final JButton changeRoot = new JButton(loc.getString("CTL_CvsRootForm_Action_Change"));
        changeRoot.setToolTipText(loc.getString("TT_CvsRootForm_Action_Change"));
        JButton cancel = new JButton(loc.getString("CTL_CvsRootForm_Action_Cancel"));
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ChangeCVSRootAction.class, "ACSD_CvsRootForm_Action_Cancel"));
        DialogDescriptor descriptor = new DialogDescriptor(
                panel, 
                loc.getString("CTL_CvsRootForm_Title"),
                true,
                new Object [] { changeRoot, cancel },
                changeRoot,
                DialogDescriptor.BOTTOM_ALIGN,
                null,
                null);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(new HelpCtx(ChangeCVSRootAction.class));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(loc.getString("ACSD_CvsRootForm"));
        
        Component c = panel.getNewRootCombo().getEditor().getEditorComponent();
        if (c instanceof JTextComponent) {
            ((JTextComponent) c).getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    validate(panel, changeRoot);
                }

                public void removeUpdate(DocumentEvent e) {
                    validate(panel, changeRoot);
                }

                public void changedUpdate(DocumentEvent e) {
                    validate(panel, changeRoot);
                }
            });
        }

        panel.getNewRootCombo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validate(panel, changeRoot);
            }
        });
        
        dialog.setVisible(true);
        if (descriptor.getValue() != changeRoot) return;
        
        final String newRoot = panel.getNewRootCombo().getEditor().getItem().toString();
        Utils.insert(CvsModuleConfig.getDefault().getPreferences(), RECENT_CHANGED_ROOTS, newRoot, 20);
            
        Utils.createTask(new Runnable() {
            public void run() {
                rewrite(newRoot);
            }
        }).schedule(0);
    }
    
    private void rewrite(String newRoot) {
        for (File root : ctx.getRootFiles()) {
            CvsRootRewriter rewriter = new CvsRootRewriter(root, newRoot);
            rewriter.rewrite();
        }
    }

    private String getCurrentRoot() {
        StringBuilder sb = new StringBuilder();
        for (File file : ctx.getRootFiles()) {
            try {
                String root = CvsRootRewriter.getCvsRoot(file);
                sb.append(root);
                sb.append(", ");
            } catch (IOException e) {
                Logger.getLogger(ChangeCVSRootAction.class.getName()).log(Level.WARNING, "", e);
                // ignore for now
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    private String getNewRoot() {
        File file = ctx.getRootFiles().iterator().next();
        try {
            return CvsRootRewriter.getCvsRoot(file);
        } catch (IOException e) {
            Logger.getLogger(ChangeCVSRootAction.class.getName()).log(Level.WARNING, "", e);
            return "";
        }
    }
    
    private String getWorkingCopy() {
        StringBuilder sb = new StringBuilder();
        for (File file : ctx.getRootFiles()) {
            sb.append(file.getAbsolutePath());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public void validate(ChangeCVSRootPanel panel, JButton ok) {
        try {
            CVSRoot.parse(panel.getNewRootCombo().getEditor().getItem().toString());
            ok.setEnabled(true);
        } catch (Exception e) {
            ok.setEnabled(false);
        }
    }
}
