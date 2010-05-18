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

package org.netbeans.modules.jmx.actions.dialog;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.jmx.JavaModelHelper;
import org.netbeans.modules.jmx.MBeanNotification;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTable;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel which is used to ask notifications to user.
 * @author  tl156378
 */
public class AddNotifPanel extends javax.swing.JPanel {
    
    /** class to add registration of MBean */
    private JavaSource currentClass;
    
    private MBeanNotificationTableModel notificationModel;
    private NotificationTable notificationTable;
    
    private ResourceBundle bundle;
    
    private JButton btnOK;
    
    /**
     * Returns if the user has selected Generate Broadcaster delegation.
     */
    public boolean getGenBroadcastDeleg() {
        return genDelegationCheckBox.isSelected();
    }
    
    /**
     * Returns if the user has selected Generate private seqence number field.
     */
    public boolean getGenSeqNumber() {
        return genSeqNbCheckBox.isSelected();
    }
    
    /**
     * Returns all the specified notifications by user.
     * @return <CODE>MBeanNotification[]</CODE> specified notifications by user.
     */
    public MBeanNotification[] getNotifications() {
        MBeanNotification[] notifs = 
                new MBeanNotification[notificationModel.getRowCount()];
        for (int i = 0; i < notificationModel.getRowCount(); i++)
            notifs[i] = notificationModel.getNotification(i);
        return notifs;
    }
     
    /** 
     * Creates new form RemoveAttrPanel.
     * @param  node  node selected when the Register Mbean action was invoked
     */
    public AddNotifPanel(Node node) throws IOException {
        bundle = NbBundle.getBundle(AddNotifPanel.class);
        
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        
        currentClass = JavaModelHelper.getSource(fo);
        
        // init tags
        
        initComponents();
        String className = JavaModelHelper.getFullClassName(currentClass);
        notificationModel = new MBeanNotificationTableModel();
        notificationModel.setDefaultTypeValue(
                className.toLowerCase() + ".type"); // NOI18N
        notificationTable = new NotificationTable(this, notificationModel);
        notificationTable.setName("notificationTable"); // NOI18N
        notificationTable.setBorder(new javax.swing.border.EtchedBorder());
        jScrollPane1.setViewportView(notificationTable);
        notifTableLabel.setLabelFor(notificationTable);
        
        removeButton.setEnabled(false);
        addButton.addActionListener(
                new AddTableRowListener(notificationTable, notificationModel,
                removeButton));
        removeButton.addActionListener(new RemTableRowListener(
                notificationTable, notificationModel, removeButton));
        
        // init labels
        Mnemonics.setLocalizedText(genDelegationCheckBox,
                bundle.getString("LBL_GenBroadcasterDelegation")); // NOI18N
        Mnemonics.setLocalizedText(genSeqNbCheckBox,
                bundle.getString("LBL_GenSeqNumberField")); // NOI18N
        Mnemonics.setLocalizedText(notifTableLabel,
                bundle.getString("LBL_Notifications")); // NOI18N
        Mnemonics.setLocalizedText(addButton,
                bundle.getString("LBL_Button_AddNotification")); // NOI18N
        Mnemonics.setLocalizedText(removeButton,
                bundle.getString("LBL_Button_RemoveNotification")); // NOI18N
        
        // for accessibility
        genDelegationCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_GEN_DELEG_BROADCAST")); // NOI18N
        genDelegationCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_GEN_DELEG_BROADCAST_DESCRIPTION")); // NOI18N
        genSeqNbCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_GEN_SEQ_NUMBER")); // NOI18N
        genSeqNbCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_GEN_SEQ_NUMBER_DESCRIPTION")); // NOI18N
        notificationTable.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_NOTIFICATION_TABLE")); // NOI18N
        notificationTable.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_NOTIFICATION_TABLE_DESCRIPTION")); // NOI18N
        addButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_ADD_NOTIFICATION")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_ADD_NOTIFICATION_DESCRIPTION")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_REMOVE_NOTIFICATION")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_REMOVE_NOTIFICATION_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    private boolean isAcceptable() {
        return true;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if specified attributes are correct.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_AddNotifAction.Title"); // NOI18N
        
        btnOK = new JButton(bundle.getString("LBL_OK")); // NOI18N
        btnOK.setEnabled(isAcceptable());
        btnOK.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {btnOK, DialogDescriptor.CANCEL_OPTION},
                btnOK,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        new HelpCtx("jmx_mbean_update_notification"), // NOI18N
                        (ActionListener) null
                        ));
                
                if (returned == btnOK) {
                    return true;
                }
                return false;
    }
    
    /**
     * Returns the MBean class to add notifications.
     * @return <CODE>JavaSource</CODE> the MBean class
     */
    public JavaSource getMBeanClass() {
        return currentClass;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        buttonsPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        genDelegationCheckBox = new javax.swing.JCheckBox();
        genSeqNbCheckBox = new javax.swing.JCheckBox();
        notifTableLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 12, 12);
        add(jScrollPane1, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        leftPanel.add(removeButton, gridBagConstraints);

        addButton.setName("notifAddJButton");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        leftPanel.add(addButton, gridBagConstraints);

        buttonsPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(buttonsPanel, gridBagConstraints);

        genDelegationCheckBox.setName("genDelegationCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(genDelegationCheckBox, gridBagConstraints);

        genSeqNbCheckBox.setName("genSeqNbCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(genSeqNbCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 12);
        add(notifTableLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JCheckBox genDelegationCheckBox;
    private javax.swing.JCheckBox genSeqNbCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel notifTableLabel;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
}
