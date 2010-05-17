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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.mbeanwizard;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.*;

import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import org.netbeans.modules.jmx.MBeanNotification;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTable;


/**
 *
 * MBean notification panel class: Manages the notifications the user wants the
 * mbean to emit
 *
 */
public class MBeanNotificationPanel extends JPanel {
    private boolean DEBUG = false;
    
    private NotificationTable notificationTable;
    private MBeanNotificationTableModel notificationModel;
    private NotificationsWizardPanel wiz;
    
    private JCheckBox implNotifEmitCheckBox;
    private JCheckBox genDelegationCheckBox;
    private JCheckBox genSeqNbCheckBox;
    private JLabel notifTableLabel;
    private JButton notifAddJButton;
    private JButton notifRemJButton;
    
    private JPanel ancestorPanel = null;
    
    private ResourceBundle bundle;
    
    /**
     * MBean Notification Panel constructor
     * @param wiz a wizard panel
     */
    public MBeanNotificationPanel(NotificationsWizardPanel wiz) {
        super(new BorderLayout());
        this.wiz = wiz;
        
        this.ancestorPanel = this;
        
        bundle = NbBundle.getBundle(MBeanNotificationPanel.class);
        
        initComponents();
        
        // set names
        implNotifEmitCheckBox.setName("implNotifEmitCheckBox");
        genDelegationCheckBox.setName("genDelegationCheckBox");
        genSeqNbCheckBox.setName("genSeqNbCheckBox");
        
        String str = bundle.getString("LBL_Notification_Panel");// NOI18N
        setName(str);
        
        //init state
        updateState();
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    private void initJTable() {
        
        notificationModel = new MBeanNotificationTableModel();
        notificationTable = new NotificationTable(ancestorPanel, 
                notificationModel);
        notificationTable.setName("notificationTable");// NOI18N
    }
    
    void updateState() {
        boolean implSelected = implNotifEmitCheckBox.isSelected();
        genDelegationCheckBox.setEnabled(implSelected);
        genSeqNbCheckBox.setEnabled(implSelected);
        notifTableLabel.setEnabled(implSelected);
        notificationTable.setEnabled(implSelected);
        notifAddJButton.setEnabled(implSelected);
        if (implSelected && (notificationModel.getRowCount() > 0))
            notifRemJButton.setEnabled(true);
        else if (!implSelected) {
            notifRemJButton.setEnabled(false);
            if(notificationTable.isEditing())
                notificationTable.getCellEditor().stopCellEditing();
        }
    }
    
    private JPanel initSelectionPanel() {
        java.awt.GridBagConstraints gridBagConstraints;

        implNotifEmitCheckBox = new JCheckBox();
        genDelegationCheckBox = new JCheckBox();
        genSeqNbCheckBox = new JCheckBox();
        notifTableLabel = new JLabel();
        JPanel selectionPanel = new JPanel();
        
        selectionPanel.setLayout(new java.awt.GridBagLayout());
        
        implNotifEmitCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateState();
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        selectionPanel.add(implNotifEmitCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 21, 0, 0);
        selectionPanel.add(genDelegationCheckBox, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 21, 0, 0);
        selectionPanel.add(genSeqNbCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 5, 0);
        selectionPanel.add(notifTableLabel, gridBagConstraints);
        notifTableLabel.setLabelFor(notificationTable);
                
        return selectionPanel;
    }
    
    private void initComponents() {
        initJTable();
        JPanel selectionPanel = initSelectionPanel();
        
        Mnemonics.setLocalizedText(implNotifEmitCheckBox,
                bundle.getString("LBL_ImplementNotifEmitter"));//NOI18N
        Mnemonics.setLocalizedText(genDelegationCheckBox,
                bundle.getString("LBL_GenBroadcasterDelegation"));//NOI18N
        Mnemonics.setLocalizedText(genSeqNbCheckBox,
                bundle.getString("LBL_GenSeqNumberField"));//NOI18N
        Mnemonics.setLocalizedText(notifTableLabel,
                bundle.getString("LBL_Notifications"));//NOI18N
        
        // defines a scrolol panel for the JTabel
        JScrollPane notifJTableScrollPanel = new JScrollPane(notificationTable);
        
        notifAddJButton = new JButton();
        Mnemonics.setLocalizedText(notifAddJButton,
                bundle.getString("BUTTON_add_notification"));//NOI18N
        notifRemJButton = new JButton();
        Mnemonics.setLocalizedText(notifRemJButton,
                bundle.getString("BUTTON_rem_notification"));//NOI18N
        
        notifAddJButton.setName("notifAddJButton");// NOI18N
        notifRemJButton.setName("notifRemJButton");// NOI18N
        
        //remove button should first be disabled
        notifRemJButton.setEnabled(false);
        
        notifAddJButton.addActionListener(
                new AddTableRowListener(notificationTable, notificationModel,
                notifRemJButton));
        notifRemJButton.addActionListener(new RemTableRowListener(
                notificationTable, notificationModel, notifRemJButton));
        
        // adds the buttons to this panel
        JPanel notifJPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        notifJPanel.add(notifAddJButton);
        notifJPanel.add(notifRemJButton);
        
        add(selectionPanel, BorderLayout.NORTH);
        add(notifJTableScrollPanel, BorderLayout.CENTER);
        add(notifJPanel, BorderLayout.SOUTH);
        
        //Accessibility
        implNotifEmitCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_IMPL_NOTIF_EMITTER")); // NOI18N
        implNotifEmitCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_IMPL_NOTIF_EMITTER_DESCRIPTION")); // NOI18N
        genDelegationCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_GEN_DELEG_BROADCAST")); // NOI18N
        genDelegationCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_GEN_DELEG_BROADCAST_DESCRIPTION")); // NOI18N
        genSeqNbCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_GEN_SEQ_NUMBER")); // NOI18N
        genSeqNbCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_GEN_SEQ_NUMBER_DESCRIPTION")); // NOI18N
        notifRemJButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REMOVE_NOTIFICATION")); // NOI18N
        notifRemJButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REMOVE_NOTIFICATION_DESCRIPTION"));// NOI18N
        notifAddJButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_ADD_NOTIFICATION"));// NOI18N
        notifAddJButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_ADD_NOTIFICATION_DESCRIPTION"));// NOI18N
        notificationTable.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_NOTIFICATION_TABLE"));// NOI18N
        notificationTable.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_NOTIFICATION_TABLE_DESCRIPTION"));// NOI18N
        
    }
    
    /**
     * Returns the notification table model
     * @return MBeanNotificationTableModel
     */
    public MBeanNotificationTableModel getNotificationModel() {
        return notificationModel;
    }
    
    /**
     * Method which sets the default type value to the notification type
     * @param defaultTypeValue the default value to set
     */
    public void setDefaultTypeValue(String defaultTypeValue) {
        notificationModel.setDefaultTypeValue(defaultTypeValue);
    }
    
    /**
     * The inner class which defines the wizard panel
     * descriptor with user data
     */
    public static class NotificationsWizardPanel extends GenericWizardPanel
            implements org.openide.WizardDescriptor.FinishablePanel {
        private MBeanNotificationPanel panel = null;
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return finish true if the panel can be the last one and enables 
         * the finish button
         */
        public boolean isFinishPanel() { return true;}
        
        /**
         * Method returning the corresponding panel; here the 
         * MBeanNotificationPanel
         * @return Component the panel
         */
        public Component getComponent() { return getPanel(); }
        
        private MBeanNotificationPanel getPanel() {
            if (panel == null) {
                panel = new MBeanNotificationPanel(this);
            }
            
            return panel;
        }
        
        /**
         * Method which reads the in the model already contained data
         * @param settings an object containing the contents of the 
         * notification table
         */
        public void readSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            String mbeanName = (String)
            wiz.getProperty(WizardConstants.PROP_MBEAN_NAME);
            String mbeanPackageName = (String)
            wiz.getProperty(WizardConstants.PROP_MBEAN_PACKAGE_NAME);
            String defaultTypeValue = "";// NOI18N
            if ((mbeanPackageName != null) && (mbeanName != null)) {
                if (mbeanPackageName.equals("")) {// NOI18N
                    defaultTypeValue = mbeanName + "." + "type";// NOI18N
                } else {
                    defaultTypeValue = mbeanPackageName + "." + mbeanName + "."// NOI18N
                            + "type";// NOI18N
                }
            }
            defaultTypeValue = defaultTypeValue.toLowerCase();
            getPanel().setDefaultTypeValue(defaultTypeValue);
            
            // update state
            Boolean implNotifEmit = (Boolean) 
                wiz.getProperty(WizardConstants.PROP_IMPL_NOTIF_EMITTER);
            if (implNotifEmit == null)
                implNotifEmit = false;
            getPanel().setImplNotifEmit(implNotifEmit);
            Boolean genBroadDeleg = (Boolean) 
                wiz.getProperty(WizardConstants.PROP_GEN_BROADCAST_DELEGATION);
            if (genBroadDeleg == null)
                genBroadDeleg = false;
            getPanel().setGenBroadcasterDelegation(genBroadDeleg);
            Boolean genSeqNumber = (Boolean) 
                wiz.getProperty(WizardConstants.PROP_GEN_SEQ_NUMBER);
            if (genSeqNumber == null)
                genSeqNumber = false;
            getPanel().setGenSeqNb(genSeqNumber);
            getPanel().updateState();
        }
        
        /**
         * Method called to store information from the GUI into the wizard map
         * @param settings the object containing the data to store
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            //stores all values from the table in the model even with keyboard
            //navigation
            //the if block avoids the problem that the last entry is not taken
            //i.e we only do that if we are on the description TEXT field, not 
            //on the popup cell
            if (getPanel().notificationTable.getEditingColumn() == 
                    MBeanNotificationTableModel.IDX_NOTIF_DESCRIPTION)
                getPanel().notificationTable.editingStopped(
                        new ChangeEvent(this));
            
            wiz.putProperty(WizardConstants.PROP_IMPL_NOTIF_EMITTER, 
                    getPanel().isImplNotifEmit());
            wiz.putProperty(WizardConstants.PROP_GEN_BROADCAST_DELEGATION,
                    getPanel().genBroadcasterDelegation());
            wiz.putProperty(WizardConstants.PROP_GEN_SEQ_NUMBER,
                    getPanel().genSeqNb());
            
            // read the content of attributes table
            MBeanNotificationTableModel aModel = getPanel().notificationModel;
            int nbNotifs = aModel.size();
            
            wiz.putProperty(WizardConstants.PROP_NOTIF_NB, 
                    new Integer(nbNotifs).toString());
            
            for (int i = 0 ; i < nbNotifs ; i++) {
                //the current notification 
                MBeanNotification currentNotif = aModel.getNotification(i);
                wiz.putProperty(WizardConstants.PROP_NOTIF_CLASS + i,
                        currentNotif.getNotificationClass());
                
                wiz.putProperty(WizardConstants.PROP_NOTIF_DESCR + i,
                        currentNotif.getNotificationDescription());
                
                wiz.putProperty(WizardConstants.PROP_NOTIF_TYPE + i,
                        currentNotif.getNotificationTypeClasses());
            }
        }
        
        /**
         * Returns the current help context
         * @return HelpCtxt
         */
        public HelpCtx getHelp() {
            return new HelpCtx("jmx_instrumenting_app");// NOI18N
        }
    }

    public boolean isImplNotifEmit() {
        return implNotifEmitCheckBox.isSelected();
    }

    public boolean genBroadcasterDelegation() {
        return genDelegationCheckBox.isSelected();
    }

    public boolean genSeqNb() {
        return genSeqNbCheckBox.isSelected();
    }
    
    public void setImplNotifEmit(boolean state) {
        implNotifEmitCheckBox.setSelected(state);
    }

    public void setGenBroadcasterDelegation(boolean state) {
        genDelegationCheckBox.setSelected(state);
    }

    public void setGenSeqNb(boolean state) {
        genSeqNbCheckBox.setSelected(state);
    }
    
}
