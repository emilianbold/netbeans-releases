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

package org.netbeans.installer.cluster;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.wizard.awt.*;
import com.installshield.wizard.swing.*;

public class NetBeansDirChooserPanelSwingImpl extends DefaultSwingWizardPanelImpl implements ActionListener, DocumentListener {
    private JTextField nbdir = null;
    private JButton browse = null;
    
    public void initialize(WizardBeanEvent event) {
        super.initialize(event);
        
        getContentPane().add(Spacing.createVerticalSpacing(6));
        getContentPane().setLayout(new ColumnLayout());
        
        nbdir = new JTextField();
        
        JPanel captionPane  = new JPanel();
        getContentPane().add(captionPane, ColumnConstraints.createHorizontalFill());
        captionPane.setLayout(new ColumnLayout());
        
        JLabel caption = new JLabel();
        
        MnemonicString capMn = new MnemonicString(resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.nbdirCaption)"));
        String capStr = capMn.toString();
        caption.setText(capStr);
        if (capMn.isMnemonicSpecified()) {
            caption.setDisplayedMnemonic(capMn.getMnemonicChar());
            caption.setLabelFor(nbdir);
        } else {
            caption.setDisplayedMnemonic(0);
            caption.setLabelFor(null);
        }
        captionPane.add(caption, ColumnConstraints.createHorizontalFill());
        captionPane.add(Spacing.createVerticalSpacing(4));
        
        // destination field
        getContentPane().add(nbdir, ColumnConstraints.createHorizontalFill());
        nbdir.getDocument().addDocumentListener(this);
        nbdir.getAccessibleContext().setAccessibleName(resolveString(getPanel().getDescription()));
        
        
        // browse button
        getContentPane().add(Spacing.createVerticalSpacing(4));
        getContentPane().add(browse = new JButton(), ColumnConstraints.createRightAlign());
        browse.addActionListener(this);
        MnemonicString browseMn = new MnemonicString(resolveString(resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.nbdirBrowseCaption)")));
        browse.setText("  " + browseMn.toString() + "  ");
        if (browseMn.isMnemonicSpecified()) {
            browse.setMnemonic(browseMn.getMnemonicChar());
        } else {
            browse.setMnemonic(0);
        }
    }
    
    public void entering(WizardBeanEvent event) {
        nbdir.setText(resolveString(getNbDirPanel().getNbDir()));
    }
    
    public void actionPerformed(ActionEvent event) {
        SwingWizardUI wizardUI = (SwingWizardUI)getPanel().getWizard().getUI();
        if (wizardUI != null) {
            wizardUI.restoreDefaultColors();
        }
        JFileChooser fc = new JFileChooser() {
            public boolean accept(java.io.File f) {
                return f.isDirectory();
                
            }
            public void setCurrentDirectory(java.io.File f) {
                
                super.setCurrentDirectory(f);
                
                FileChooserUI ui = getUI();
                
                if (ui instanceof BasicFileChooserUI) {
                    ((BasicFileChooserUI)ui).setFileName("");
                }
            }
        };
        
        fc.setDialogTitle(resolveString("$L(org.netbeans.installer.cluster.Bundle, NetBeansDirChooser.dirChooserDialogTitle)"));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        Dimension prefSize = fc.getPreferredSize();
        fc.setSelectedFile(new java.io.File(nbdir.getText()));
        fc.setPreferredSize(prefSize);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                nbdir.setText(fc.getSelectedFile().getCanonicalPath());
            } catch (java.io.IOException e) {
                nbdir.setText(fc.getSelectedFile().getPath());
            }
            nbdir.requestFocus();
            nbdir.selectAll();
        }
        if (wizardUI != null) {
            wizardUI.setWizardColors();
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        getNbDirPanel().setNbDir(nbdir.getText());
    }
    
    public void removeUpdate(DocumentEvent e) {
        getNbDirPanel().setNbDir(nbdir.getText());
    }
    
    public void changedUpdate(DocumentEvent e) {
        getNbDirPanel().setNbDir(nbdir.getText());
    }
    
    private NetBeansDirChooserPanel getNbDirPanel() {
        // assert safe cast
        return (NetBeansDirChooserPanel)getPanel();
    }
}
