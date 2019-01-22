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

package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.cdc.platform.CDCDevice;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
final class PanelConfigurePlatformVisual extends javax.swing.JPanel {
    
    protected PanelConfigurePlatform panel;
    private PlatformComboBoxModel model;
    private CDCDevice[] devices;
    private String platformType;
    
    /** Creates new form PanelConfigurePlatformVisual */
    public PanelConfigurePlatformVisual(final PanelConfigurePlatform panel, String platformType) {
        this.panel = panel;
        this.platformType = platformType;
        setName(NbBundle.getMessage(PanelConfigurePlatformVisual.class,"LAB_SelectPlatform"));
        initComponents();        
    }
        
    protected void selectPlatformPanel(){
        String platformType = ((PlatformKey)model.getSelectedItem()).getPlatform().getType();
    }
    
    protected void selectDevice(){
        CDCPlatform platform = ((PlatformKey)model.getSelectedItem()).getPlatform();
        devices = platform.getDevices();
        Vector<String> devcs = new Vector<String>();
        for (CDCDevice device : devices) {
            devcs.add(device.getName());
        }
        jComboBoxDevice.setModel(new DefaultComboBoxModel(devcs));
    }
    
    protected void selectProfile(){
        String name = null;
        Object o = jComboBoxDevice.getSelectedItem();
        for (CDCDevice device : devices) {
            if (device.getName().equals(o)){
                CDCDevice.CDCProfile[] profiles = device.getProfiles();
                Vector<String> profs = new Vector<String>();
                for (int j = 0; j < profiles.length; j++) {
                    String s = profiles[j].getName(); 
                    profs.add(s);
                    if (profiles[j].isDefault())
                        name = s;
                }
                jComboProfile.setModel(new DefaultComboBoxModel(profs));
                jComboProfile.setSelectedItem(name);
            }
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboPlatform = new javax.swing.JComboBox();
        jComboBoxDevice = new javax.swing.JComboBox();
        jComboProfile = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        containerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jComboPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class,"LBL_WizardsPlatform_Platform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSN_WizardsPlatform_Platform")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSD_WizardsPlatform_Platform")); // NOI18N

        jLabel2.setLabelFor(jComboBoxDevice);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class,"LBL_WizardsPlatform_Device")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSN_WizardsPlatform_Device")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSD_WizardsPlatform_Device")); // NOI18N

        jLabel3.setLabelFor(jComboProfile);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class,"LBL_WizardsPlatform_Profile")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSN_WizardsPlatform_Profile")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSD_WizardsPlatform_Profile")); // NOI18N

        jComboPlatform.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jComboPlatform, gridBagConstraints);

        jComboBoxDevice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jComboBoxDevice, gridBagConstraints);

        jComboProfile.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(jComboProfile, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jSeparator1, gridBagConstraints);

        containerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(containerPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSN_WizardsPlatform")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelConfigurePlatformVisual.class, "ACSD_WizardsPlatform")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    void read(WizardDescriptor wizardDescriptor) {
        
        jComboPlatform.setModel(model = new PlatformComboBoxModel(platformType));//todo
        selectPlatformPanel();
        selectDevice();
        selectProfile();
        jComboPlatform.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                selectPlatformPanel();
                selectDevice();
                selectProfile();
                panel.fireChangeEvent();

            }
        });
        
        jComboBoxDevice.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                selectProfile();
                panel.fireChangeEvent();
            }
        });

        panel.fireChangeEvent();

        String s = (String)wizardDescriptor.getProperty("activePlatform");
        
        if (s != null) {
            for (int i = 0; i < jComboPlatform.getItemCount(); i++){
                if (((PlatformKey)jComboPlatform.getItemAt(i)).getDisplayName().equals(s))
                    jComboPlatform.setSelectedIndex(i);
            }
        }
        s = (String)wizardDescriptor.getProperty("activeDevice");
        if (s != null) jComboBoxDevice.setSelectedItem(s);
        s = (String)wizardDescriptor.getProperty("activeProfile");
        if (s != null) jComboProfile.setSelectedItem(s);        
    }

    void store(WizardDescriptor d) {
        d.putProperty("activePlatform", ((PlatformKey)jComboPlatform.getSelectedItem()).getDisplayName());
        d.putProperty("activeDevice",   jComboBoxDevice.getSelectedItem());
        d.putProperty("activeProfile",  jComboProfile.getSelectedItem());
    }
 
    boolean valid( WizardDescriptor wizardDescriptor ) {
        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, " " ); //NOI18N
        return true;
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JComboBox jComboBoxDevice;
    private javax.swing.JComboBox jComboPlatform;
    private javax.swing.JComboBox jComboProfile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
 
    private static class PlatformKey implements Comparable {
        
        private String name;
        private JavaPlatform platform;
                
        public PlatformKey (JavaPlatform platform) {
            //assert platform != null;
            this.platform = platform;
        }
        
        public int compareTo(Object o) {
            return this.getDisplayName().compareTo(((PlatformKey)o).getDisplayName());
        }
        
        public boolean equals (Object other) {
            if (other instanceof PlatformKey) {
                PlatformKey otherKey = (PlatformKey)other;
                return (this.platform == null ? otherKey.platform == null : this.platform.equals(otherKey.platform)) &&
                        otherKey.getDisplayName().equals (this.getDisplayName());
            }
            return false;
        }
        
        public int hashCode () {
            return getDisplayName ().hashCode ();
        }
        
        public String toString () {
            return getDisplayName ();
        }
        
        public synchronized String getDisplayName () {
            if (this.name == null) {
                this.name = this.platform.getDisplayName();
            }
            return this.name;
        }

        public synchronized CDCPlatform getPlatform() {
            return (CDCPlatform)this.platform;
        }        
    }
    
    private static class PlatformComboBoxModel extends AbstractListModel implements ComboBoxModel {
        
        private JavaPlatformManager pm;
        private PlatformKey[] platformNamesCache;
        private PlatformKey selectedPlatform;
        private String platformType;
        
        public PlatformComboBoxModel (String platformType) {
            this.pm = JavaPlatformManager.getDefault();
            this.platformType = platformType;
        }
        
        public int getSize () {
            PlatformKey[] platformNames = getPlatformNames ();
            return platformNames.length;
        }
        
        public Object getElementAt (int index) {
            PlatformKey[] platformNames = getPlatformNames ();
            assert index >=0 && index< platformNames.length;
            return platformNames[index];
        }
        
        public Object getSelectedItem () {
            this.getPlatformNames(); //Force setting of selectedPlatform if it is not alredy done
            return this.selectedPlatform;
        }
        
        public void setSelectedItem (Object obj) {
            this.selectedPlatform = (PlatformKey) obj;
            this.fireContentsChanged(this, -1, -1);
        }
                
        private synchronized PlatformKey[] getPlatformNames () {
            Set<String> accepted = null;
            if (platformType != null){
                accepted = new HashSet<String>();
                StringTokenizer st = new StringTokenizer(platformType, ",");
                while(st.hasMoreTokens()){
                    accepted.add(st.nextToken());
                }
            }
            
            if (this.platformNamesCache == null) {
                JavaPlatform[] platforms = pm.getPlatforms (null, new Specification(CDCPlatform.PLATFORM_CDC, null));    //NOI18N
                Set<PlatformKey> orderedNames = new TreeSet<PlatformKey> ();
                for (JavaPlatform platform : platforms) {
                    if (platform.getInstallFolders().size()>0 && (accepted == null || accepted.contains(((CDCPlatform)platform).getType()))) {
                        PlatformKey pk = new PlatformKey(platform);
                        orderedNames.add (pk);
                    }
                }
                // Fix for IZ#146204 - Platform selected by default in "New CDC Project" wizard is not always the first
                if ( !orderedNames.isEmpty() && selectedPlatform == null ){
                    selectedPlatform = orderedNames.iterator().next();
                        
                }
                this.platformNamesCache = orderedNames.toArray(new PlatformKey[orderedNames.size()]);
            }
            return this.platformNamesCache;
        }        
    }    
}
