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

/*
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.deployment.MobilityDeploymentManagerPanel;
import org.netbeans.modules.mobility.project.deployment.MobilityDeploymentProperties;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class CustomizerDeploy extends JPanel implements CustomizerPanel, VisualPropertyGroup, ActionListener {
    
    private VisualPropertySupport vps;
    private boolean useDefault;
    private final String[] propertyGroup;
    private final String[] methodNames;
    private final Map<String,DeploymentPlugin> plugins;
    private MobilityDeploymentProperties mdp = new MobilityDeploymentProperties(new RequestProcessor());
    
    private String config;
    private ProjectProperties pp;
    private Component cComp = null;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerDeploy() {
        initComponents();
        initAccessibility();
        ArrayList<String> propNames = new ArrayList<String>();
        plugins = new TreeMap<String,DeploymentPlugin>();
        propNames.add(DefaultPropertiesDescriptor.DEPLOYMENT_METHOD);
        propNames.add(DefaultPropertiesDescriptor.DEPLOYMENT_INSTANCE);
        for ( DeploymentPlugin p : Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class)).allInstances() )
        {
            plugins.put(p.getDeploymentMethodDisplayName(), p);
            propNames.addAll(p.getProjectPropertyDefaultValues().keySet());
        }
        propertyGroup = propNames.toArray(new String[propNames.size()]);
        methodNames = plugins.keySet().toArray(new String[plugins.size()]);
        jComboBoxMethod.addActionListener(this);
        jComboBoxInstance.addActionListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheck = new javax.swing.JCheckBox();
        jLabelMethod = new javax.swing.JLabel();
        jComboBoxMethod = new javax.swing.JComboBox();
        jLabelInstance = new javax.swing.JLabel();
        jComboBoxInstance = new javax.swing.JComboBox();
        jButtonManager = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        customPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheck, NbBundle.getMessage(CustomizerDeploy.class, "LBL_Use_Default")); // NOI18N
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(defaultCheck, gridBagConstraints);
        defaultCheck.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerDeploy.class, "ACSN_CustDeploy_UseDefault")); // NOI18N
        defaultCheck.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerDeploy.class, "ACSD_CustDeploy_UseDefault")); // NOI18N

        jLabelMethod.setLabelFor(jComboBoxMethod);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMethod, NbBundle.getMessage(CustomizerDeploy.class, "LBL_CustDeploy_Method")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelMethod, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jComboBoxMethod, gridBagConstraints);
        jComboBoxMethod.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerDeploy.class, "ACSD_Deployment_Method")); // NOI18N

        jLabelInstance.setLabelFor(jComboBoxInstance);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelInstance, NbBundle.getMessage(CustomizerDeploy.class, "LBL_CustDeploy_DeploymentInstance")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabelInstance, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jComboBoxInstance, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonManager, NbBundle.getMessage(CustomizerDeploy.class, "LBL_CustDeploy_Manage")); // NOI18N
        jButtonManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageDeployments(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        add(jButtonManager, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        customPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(customPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void manageDeployments(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageDeployments
        String newSelection = MobilityDeploymentManagerPanel.manageDeployment((String) jComboBoxMethod.getSelectedItem(), (String) jComboBoxInstance.getSelectedItem());//GEN-LAST:event_manageDeployments
        mdp = new MobilityDeploymentProperties();
        initGroupValues(useDefault);
        if (newSelection != null) jComboBoxInstance.setSelectedItem(newSelection);
    }                                  
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerRun.class, "ACSN_CustDeploy"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ACSD_CustDeploy"));
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        for ( DeploymentPlugin p : Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class)).allInstances() ){
            if (p instanceof CustomizerPanel){
                ((CustomizerPanel)p).initValues(props, configuration);
            }
        }
        this.vps = VisualPropertySupport.getDefault(props);
        pp=props;
        config=configuration;
        vps.register(defaultCheck, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return propertyGroup;
    }
    
    public void initGroupValues(final boolean useDefault) {
        this.useDefault = useDefault;
        vps.register(jComboBoxMethod, methodNames, DefaultPropertiesDescriptor.DEPLOYMENT_METHOD, useDefault);
        jLabelMethod.setEnabled(!useDefault);
        actionPerformed(null);
    }
    
    public void actionPerformed(final ActionEvent e) {
        final String method = (String)jComboBoxMethod.getSelectedItem();
        final DeploymentPlugin p = method == null ? null : plugins.get(method);
        if (e == null || e.getSource().equals(jComboBoxMethod))
        {
            boolean supportsInstances = p != null && p.getGlobalPropertyDefaultValues().size() > 0;
            vps.register(jComboBoxInstance, supportsInstances ? mdp.getInstanceList(p.getDeploymentMethodName()).toArray() : new String[0], DefaultPropertiesDescriptor.DEPLOYMENT_INSTANCE, useDefault);
            jLabelInstance.setEnabled(!useDefault&&supportsInstances);
            jComboBoxInstance.setEnabled(!useDefault&&supportsInstances);
            jButtonManager.setEnabled(!useDefault&&supportsInstances);            
        }
        if (e == null || e.getSource().equals(jComboBoxMethod) || e.getSource().equals(jComboBoxInstance)) {    
            cComp = p == null ? null : p.createProjectCustomizerPanel();
            if (cComp != null) 
            {
                if (cComp instanceof CustomizerPanel)
                    ((CustomizerPanel)cComp).initValues(pp,config);
                registerSubcomponents(cComp, p.getProjectPropertyDefaultValues().keySet());
            }
            //To avoid screen flickering
            customPanel.setVisible(false);
            customPanel.removeAll();
            if (cComp != null) customPanel.add(cComp);

            customPanel.setVisible(true);
            customPanel.repaint();
            customPanel.validate();
        }
    }
    
    private void registerSubcomponents(final Component c, final Set propertyNames) {
        final String prop = c.getName();
        if (prop != null && propertyNames.contains(prop)) {
            if (c instanceof JCheckBox) {
                vps.register((JCheckBox)c, prop, useDefault);
            } else if (c instanceof JRadioButton) {
                vps.register((JRadioButton)c, prop, useDefault);
            } else if (c instanceof JComboBox) {
                vps.register((JComboBox)c, null, prop, useDefault);
            } else if (c instanceof JSlider) {
                vps.register((JSlider)c, prop, useDefault);
            } else if (c instanceof JSpinner) {
                vps.register((JSpinner)c, prop, useDefault);
            } else if (c instanceof JTextComponent) {
                vps.register((JTextComponent)c, prop, useDefault);
            } else assert false : "Unknown component type for registration"; //NOI18N
        }
        if (c instanceof Container) {
            final Component sub[] = ((Container)c).getComponents();
            for (int i=0; i<sub.length; i++) registerSubcomponents(sub[i], propertyNames);
        }
        if (useDefault) c.setEnabled(false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customPanel;
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JButton jButtonManager;
    private javax.swing.JComboBox jComboBoxInstance;
    private javax.swing.JComboBox jComboBoxMethod;
    private javax.swing.JLabel jLabelInstance;
    private javax.swing.JLabel jLabelMethod;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
    
}
