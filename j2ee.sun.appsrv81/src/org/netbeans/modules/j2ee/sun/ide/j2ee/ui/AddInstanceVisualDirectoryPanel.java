/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class AddInstanceVisualDirectoryPanel extends JPanel {

    final private boolean createPersonalInstance;

    /**
     * Creates new form AddInstanceVisualDirectoryPanel
     */
    public AddInstanceVisualDirectoryPanel(boolean createPersonalInstance) {
        initComponents();
        setAdminPort(AddDomainWizardIterator.BLANK);
        this.createPersonalInstance = createPersonalInstance;
        if (createPersonalInstance) {
            description.setText(
                    NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, 
                    "TXT_instanceDirectoryDescription2"));
        }
        adminPortLabel.setVisible(!createPersonalInstance);
        adminPortDisplay.setVisible(!createPersonalInstance);
            
        instanceDirectory.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }                    
        });
    }

    public String getName() {
        return NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, 
                "StepName_EnterDomainDirectory");                                // NOI18N
    }
    
    void setAdminPort(String uri) {
        if (!createPersonalInstance) {
            adminPortDisplay.setText(uri);
            detectedLabel.setVisible(uri.length() > 0);
        }
    }
    
    String getInstanceDirectory() {
        return instanceDirectory.getText();
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected void fireChangeEvent() {
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    void locationChanged() {
        fireChangeEvent();
    }
    
    
    private String browseDomainLocation(){
        String insLocation = null;
        JFileChooser chooser;
        if (createPersonalInstance) {
            chooser = new JFileChooser();
        }
        else {
            chooser = new DomainChooser();
        }
        Util.decorateChooser(chooser,instanceDirectory.getText(),
                NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, 
                "LBL_Choose_Domain"));                                          //NOI18N
        int returnValue = chooser.showDialog(this,
                NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class,
                "LBL_Choose_Button"));                                          //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    static private class DomainChooser extends JFileChooser {
        public void approveSelection() {
            File dir = FileUtil.normalizeFile(getSelectedFile());
            String mess = Util.rootOfUsableDomain(dir);
            if (null == mess) {
                super.approveSelection();
            }
            else {
                setCurrentDirectory( dir );
            }
            
        }
        
        
    
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        description = new javax.swing.JLabel();
        instanceDirectoryLabel = new javax.swing.JLabel();
        instanceDirectory = new javax.swing.JTextField();
        openInstanceDirectorySelector = new javax.swing.JButton();
        adminPortLabel = new javax.swing.JLabel();
        adminPortDisplay = new javax.swing.JTextField();
        detectedLabel = new javax.swing.JLabel();
        spaceHack = new javax.swing.JLabel();

        setMaximumSize(null);
        setMinimumSize(null);
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_instanceDirectoryDescription1")); // NOI18N
        description.setEnabled(false);
        description.setFocusable(false);
        description.setMaximumSize(null);
        description.setMinimumSize(null);
        description.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        instanceDirectoryLabel.setLabelFor(instanceDirectory);
        org.openide.awt.Mnemonics.setLocalizedText(instanceDirectoryLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, "LBL_instanceDirectoryLabel")); // NOI18N
        instanceDirectoryLabel.setMaximumSize(null);
        instanceDirectoryLabel.setMinimumSize(null);
        instanceDirectoryLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 6);
        add(instanceDirectoryLabel, gridBagConstraints);

        instanceDirectory.setMaximumSize(null);
        instanceDirectory.setMinimumSize(null);
        instanceDirectory.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(instanceDirectory, gridBagConstraints);
        instanceDirectory.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_instanceDirectory")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openInstanceDirectorySelector, org.openide.util.NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, "LBL_openInstanceDirectorySelector")); // NOI18N
        openInstanceDirectorySelector.setMaximumSize(null);
        openInstanceDirectorySelector.setMinimumSize(null);
        openInstanceDirectorySelector.setPreferredSize(null);
        openInstanceDirectorySelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openInstanceDirectorySelectorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 0);
        add(openInstanceDirectorySelector, gridBagConstraints);
        openInstanceDirectorySelector.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_openInstanceDirectorySelector")); // NOI18N

        adminPortLabel.setLabelFor(adminPortDisplay);
        org.openide.awt.Mnemonics.setLocalizedText(adminPortLabel, org.openide.util.NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, "LBL_adminlPortLabel")); // NOI18N
        adminPortLabel.setMaximumSize(null);
        adminPortLabel.setMinimumSize(null);
        adminPortLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 6);
        add(adminPortLabel, gridBagConstraints);

        adminPortDisplay.setEditable(false);
        adminPortDisplay.setMaximumSize(null);
        adminPortDisplay.setMinimumSize(null);
        adminPortDisplay.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 6);
        add(adminPortDisplay, gridBagConstraints);
        adminPortDisplay.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddInstanceVisualDirectoryPanel.class, "ACSD_ADMIN_PORT")); // NOI18N

        detectedLabel.setText(bundle.getString("LBL_detectedLabel")); // NOI18N
        detectedLabel.setFocusable(false);
        detectedLabel.setMaximumSize(null);
        detectedLabel.setMinimumSize(null);
        detectedLabel.setPreferredSize(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 6);
        add(detectedLabel, gridBagConstraints);

        spaceHack.setEnabled(false);
        spaceHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        add(spaceHack, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void openInstanceDirectorySelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openInstanceDirectorySelectorActionPerformed
        String val = browseDomainLocation();
        if (null != val && val.length() >=1)
            instanceDirectory.setText(val);
    }//GEN-LAST:event_openInstanceDirectorySelectorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField adminPortDisplay;
    private javax.swing.JLabel adminPortLabel;
    private javax.swing.JLabel description;
    private javax.swing.JLabel detectedLabel;
    private javax.swing.JTextField instanceDirectory;
    private javax.swing.JLabel instanceDirectoryLabel;
    private javax.swing.JButton openInstanceDirectorySelector;
    private javax.swing.JLabel spaceHack;
    // End of variables declaration//GEN-END:variables

}

