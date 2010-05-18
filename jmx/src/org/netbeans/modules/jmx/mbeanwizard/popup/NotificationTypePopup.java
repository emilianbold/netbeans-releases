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

package org.netbeans.modules.jmx.mbeanwizard.popup;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.NotificationTypeTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.listener.AddTableRowListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.ClosePopupButtonListener;
import org.netbeans.modules.jmx.mbeanwizard.listener.RemTableRowListener;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.openide.util.NbBundle;
import org.netbeans.modules.jmx.mbeanwizard.table.NotificationTypePopupTable;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jmx.MBeanNotificationType;

/**
 * Class implementing the notification popup window
 *
 */
public class NotificationTypePopup extends AbstractPopup{
    
    private int editedRow;
    private MBeanNotificationTableModel notifTableModel;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel of the popup window; here the 
     * wizard
     * @param notifTableModel the table model of the notification table in 
     * the wizard
     * @param textField the textfield to fill with the information from 
     * the popup
     * @param editedRow the current edited row in the wizard notification table
     */
    public NotificationTypePopup(JPanel ancestorPanel, 
            MBeanNotificationTableModel notifTableModel,
            JTextField textField, int editedRow) {
        
        super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor());
        
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        
        this.notifTableModel = notifTableModel;
        this.textFieldToFill = textField;
        this.editedRow = editedRow;
        
        initJTable();
        initComponents();
        readSettings();
        
        //setDimensions(NbBundle.getMessage(NotificationTypePopup.class,"LBL_NotificationType_Popup"));// NOI18N
        setDimensions(bundle.getString("LBL_NotificationType_Popup"));// NOI18N
    }
    
    protected void initJTable() {
        
        popupTableModel = new NotificationTypeTableModel(
                notifTableModel.getDefaultTypeValue());
        popupTable = new NotificationTypePopupTable(popupTableModel);
        popupTable.setName("notifPopupTable");// NOI18N
    }
    
    protected void initComponents() {
        /*
        addJButton = instanciatePopupButton(NbBundle.getMessage(NotificationTypePopup.class,"LBL_Notification_addType"));// NOI18N
        removeJButton = instanciatePopupButton(NbBundle.getMessage(NotificationTypePopup.class,"LBL_Notification_remType"));// NOI18N
        closeJButton = instanciatePopupButton(NbBundle.getMessage(NotificationTypePopup.class,"LBL_Notification_close"));// NOI18N
        
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_REMOVE_NOTIFICATION_TYPE"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_REMOVE_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_ADD_NOTIFICATION_TYPE"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_ADD_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_CLOSE_NOTIFICATION_TYPE"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_CLOSE_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_NOTIFICATION_TYPE_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NotificationTypePopup.class,"ACCESS_NOTIFICATION_TYPE_TABLE_DESCRIPTION"));// NOI18N
        */
        addJButton = instanciatePopupButton(bundle.getString("LBL_Notification_addType"));// NOI18N
        removeJButton = instanciatePopupButton(bundle.getString("LBL_Notification_remType"));// NOI18N
        closeJButton = instanciatePopupButton(bundle.getString("LBL_Notification_close"));// NOI18N
        
        //Accessibility
        removeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_REMOVE_NOTIFICATION_TYPE"));// NOI18N
        removeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_REMOVE_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_ADD_NOTIFICATION_TYPE"));// NOI18N
        addJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_ADD_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_CLOSE_NOTIFICATION_TYPE"));// NOI18N
        closeJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_CLOSE_NOTIFICATION_TYPE_DESCRIPTION"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_NOTIFICATION_TYPE_TABLE"));// NOI18N
        popupTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_NOTIFICATION_TYPE_TABLE_DESCRIPTION"));// NOI18N
        
        
        
        addJButton.addActionListener(
            new AddTableRowListener(popupTable,popupTableModel,removeJButton));
        addJButton.setName("addNotifTypeJButton");// NOI18N
        removeJButton.addActionListener(
            new RemTableRowListener(popupTable,popupTableModel,removeJButton));
        removeJButton.setName("remNotifTypeJButton");// NOI18N
        closeJButton.addActionListener(new ClosePopupButtonListener(
                this,textFieldToFill));
        closeJButton.setName("closeNotifTypeJButton");// NOI18N
        
        definePanels(new JButton[] {addJButton,
                removeJButton,
                closeJButton
        },
                popupTable, 
                bundle.getString("LBL_NOTIF_TYPE_POPUP_TABLE")); // NOI18N
    }
    
    protected void readSettings() {
        if(notifTableModel.size() != 0) {
            //get the notification type list for the current notification
            List<MBeanNotificationType> notifTypes =
                notifTableModel.getNotification(editedRow).getNotificationTypeList();
            
            
            for (int i = 0; i < notifTypes.size(); i++) {
                popupTableModel.addRow();
                //copy the notification type i from the panel model
                //to the popup model for the current notification
                ((NotificationTypeTableModel)
                                popupTableModel).setNotificationType(i, 
                                                            notifTypes.get(i));
            }
            removeJButton.setEnabled(popupTableModel.getRowCount() > 0);
        }
    }
    
    /**
     * Inhertited from superclass
     */
    public String storeSettings() {
        
        //stores all values from the table in the model even with keyboard
        //navigation
        popupTable.editingStopped(new ChangeEvent(this));
        
        String notifTypeString = "";// NOI18N
        String notifTypeContent = "";// NOI18N
        ArrayList<MBeanNotificationType> mbnt = 
                new ArrayList<MBeanNotificationType>();
        
        for (int i = 0 ; i < popupTableModel.size(); i++) {
            //gets the current notification type in the popup
            MBeanNotificationType notifType =
                ((NotificationTypeTableModel)
                popupTableModel).getNotificationType(i);
            
            notifTypeContent = notifType.getNotificationType();
            
            if (notifTypeContent != "")// NOI18N
                notifTypeString += notifTypeContent;
            
            if (i < popupTableModel.size() -1)
                notifTypeString += ",";// NOI18N
            
            // fills the arraylist with the exceptions to store
            mbnt.add(notifType);
        }
        
        //copy back the notification types from the popup to the panel model
        notifTableModel.getNotification(editedRow).setNotificationTypeList(mbnt);
        
        return notifTypeString;
    }
}
