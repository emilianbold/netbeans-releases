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
package org.netbeans.modules.javaee.wildfly.customizer;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.util.WildFlyProperties;
import org.openide.util.NbBundle;

public final class WildflyTabVisualPanel extends JPanel {
    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
    
    transient private final WildFlyProperties targetData;
    transient private final WildflyDeploymentManager dm;
    
    
    /**
     * Creates new form WildflyTabVisualPanel
     */
    public WildflyTabVisualPanel(DeploymentManager dm) {
        this.dm = (WildflyDeploymentManager)dm;
        targetData = new WildFlyProperties(this.dm);        
        initComponents();
        configFile.setText(targetData.getServerProfile());
        
        
        configFile.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                locationChanged();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                locationChanged();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                locationChanged();
            }
        });
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(WildflyTabVisualPanel.class,
                "StepName_EnterDomainDirectory");                                // NOI18N
    }
    
    
    
    String getParentDirectory() {
        return configFile.getText();
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
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
        JFileChooser chooser = new JFileChooser();
        
        decorateChooser(chooser,configFile.getText(),
                NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Choose_Domain"));                                          //NOI18N
        int returnValue = chooser.showDialog(this,
                NbBundle.getMessage(WildflyTabVisualPanel.class,
                "LBL_Choose_Button"));                                          //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            insLocation = chooser.getSelectedFile().getAbsolutePath();
        }
        return insLocation;
    }
    
    void decorateChooser(JFileChooser chooser,String fname,String title) {
        chooser.setDialogTitle(title);                                           //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || (file.isFile() && file.getName().endsWith(".xml"));
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(WildflyTabVisualPanel.class, 
                "Choose_Button_Mnemonic").charAt(0));                           //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(WildflyTabVisualPanel.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        
        chooser.getAccessibleContext().
                setAccessibleName(NbBundle.getMessage(WildflyTabVisualPanel.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        chooser.getAccessibleContext().
                setAccessibleDescription(NbBundle.getMessage(WildflyTabVisualPanel.class, 
                "LBL_Chooser_Name"));                                           //NOI18N
        if (null != fname && fname.length() > 0) {
            File sel = new File(fname);
            if (sel.isDirectory())
                chooser.setCurrentDirectory(sel);
            else
                chooser.setSelectedFile(sel);
        }
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        description = new javax.swing.JLabel();
        parentDirectoryLabel = new javax.swing.JLabel();
        configFile = new javax.swing.JTextField();
        openInstanceDirectorySelector = new javax.swing.JButton();
        spaceHack = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/javaee/wildfly/customizer/Bundle"); // NOI18N
        description.setText(bundle.getString("TXT_instanceDirectoryDescription2")); // NOI18N
        description.setEnabled(false);
        description.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        parentDirectoryLabel.setLabelFor(configFile);
        org.openide.awt.Mnemonics.setLocalizedText(parentDirectoryLabel, org.openide.util.NbBundle.getMessage(WildflyTabVisualPanel.class, "LBL_ParentFolder")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 6, 6);
        add(parentDirectoryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 6, 6);
        add(configFile, gridBagConstraints);
        configFile.getAccessibleContext().setAccessibleDescription(bundle.getString("DSC_instanceDirectory")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(openInstanceDirectorySelector, org.openide.util.NbBundle.getMessage(WildflyTabVisualPanel.class, "LBL_openInstanceDirectorySelector")); // NOI18N
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
        dm.getInstanceProperties().setProperty(WildflyPluginProperties.PROPERTY_CONFIG_FILE, val);      
        if (null != val && val.length() >=1)
            configFile.setText(val);
    }//GEN-LAST:event_openInstanceDirectorySelectorActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField configFile;
    private javax.swing.JLabel description;
    private javax.swing.JButton openInstanceDirectorySelector;
    private javax.swing.JLabel parentDirectoryLabel;
    private javax.swing.JLabel spaceHack;
    // End of variables declaration//GEN-END:variables
    
}

