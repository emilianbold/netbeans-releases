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
 * DeviceAnywhereCustomizerPanel.java
 *
 * Created on April 27, 2007, 4:34 PM
 */

package org.netbeans.modules.deployment.deviceanywhere;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIDeviceWrapper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author  suchys
 */
public class DeviceAnywhereCustomizerPanel extends javax.swing.JPanel {
        
    private List<ApplicationAPIDeviceWrapper> devices;
    private DeviceAnywhereDeploymentPlugin.PropertyEvaluator evaluator;
    //private 
    /**
     * Creates new form DeviceAnywhereCustomizerPanel
     */
    DeviceAnywhereCustomizerPanel(DeviceAnywhereDeploymentPlugin.PropertyEvaluator evaluator) {
        initComponents();   
        //invisible components, only value holders
        allDevices.setVisible(false);
        selectedDevice.setVisible(false);
        selectedDeviceCareer.setVisible(false);
        add(allDevices);
        add(selectedDevice);
        add(selectedDeviceCareer);
        retriveButton.addActionListener(new DeviceListener());
        this.evaluator = evaluator;
    }
    
    @Override
    public void addNotify(){
        String input = allDevices.getText();      
        if (input.length() != 0){
            devicesComboBox.removeAllItems();
            devices = new ArrayList<ApplicationAPIDeviceWrapper>();
            //todo Is this good property parsing? Don't we have a better way to do it?
            //todo, need to remove asserts for dev builds
            try {
                StringTokenizer st = new StringTokenizer(input, ";"); //NOI18N
                while(st.hasMoreElements()){
                    String token = st.nextToken();
                    StringTokenizer item = new StringTokenizer(token, ","); //NOI18N
                    String s = item.nextToken();
                    //assert s != null : "Missing token for DeviceID"; //NOI18N
                    int deviceId = Integer.parseInt(s.substring(s.indexOf("=")+1)); //NOI18N
                    s = item.nextToken();
                    //assert s != null : "Missing token for DeviceName"; //NOI18N
                    String deviceName = s.substring(s.indexOf("=")+1); //NOI18N
                    s = item.nextToken();
                    //assert s != null : "Missing token for DeviceCareer"; //NOI18N
                    String deviceCareer = s.substring(s.indexOf("=")+1); //NOI18N
                    ApplicationAPIDeviceWrapper wrapper = new ApplicationAPIDeviceWrapper();
                    wrapper.setId(deviceId);
                    wrapper.setName(deviceName);
                    wrapper.setCarrier(deviceCareer);
                    devices.add(wrapper);
                    devicesComboBox.addItem(deviceName);
                }           
            } catch (Exception e){
            }
        }
        if (devices == null || devices.size() == 0){
            selectedDevice.setText("-1");
            devicesComboBox.addItem(NbBundle.getMessage (DeviceAnywhereCustomizerPanel.class, "MSG_NoDevice")); //NOI18N
        } else {
            String deviceId = selectedDevice.getText();
            for (ApplicationAPIDeviceWrapper elem : devices) {
                if (deviceId.equals(String.valueOf(elem.getId()))){
                    devicesComboBox.setSelectedItem(elem.getName());
                    break;
                }
            }
            if (devicesComboBox.getSelectedIndex() == -1){
                devicesComboBox.setSelectedIndex(0);
            }
        }
        retriveButton.setEnabled(true/*jTextFieldUser.getText().trim().length() != 0*/);                
        //jTextFieldUser.getDocument().addDocumentListener(dl);
        devicesComboBox.addItemListener(il);
        
        super.addNotify();
    }
    
    @Override
    public void removeNotify(){
        super.removeNotify();
        //jTextFieldUser.getDocument().removeDocumentListener(dl);
        devicesComboBox.removeItemListener(il);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        allDevices = new javax.swing.JTextField();
        selectedDevice = new javax.swing.JTextField();
        selectedDeviceCareer = new javax.swing.JTextField();
        inputPasswordPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        devicesComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        retriveButton = new javax.swing.JButton();

        allDevices.setName(DeviceAnywhereDeploymentPlugin.PROP_AVAILABLE_DEVICES);

        selectedDevice.setName(DeviceAnywhereDeploymentPlugin.PROP_DEVICE);

        selectedDeviceCareer.setName(DeviceAnywhereDeploymentPlugin.PROP_CAREER);

        jLabel1.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "MSG_InsertPass")); // NOI18N

        javax.swing.GroupLayout inputPasswordPanelLayout = new javax.swing.GroupLayout(inputPasswordPanel);
        inputPasswordPanel.setLayout(inputPasswordPanelLayout);
        inputPasswordPanelLayout.setHorizontalGroup(
            inputPasswordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPasswordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );
        inputPasswordPanelLayout.setVerticalGroup(
            inputPasswordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPasswordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputPasswordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSN_InsertPass")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSD_InsertPass")); // NOI18N

        jLabel5.setLabelFor(devicesComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "LBL_Device")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(retriveButton, org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "LBL_RetriveDevices")); // NOI18N
        retriveButton.setActionCommand(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "LBL_RetriveDevices")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(devicesComboBox, 0, 316, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(retriveButton)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(retriveButton)
                    .addComponent(jLabel5)
                    .addComponent(devicesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(173, Short.MAX_VALUE))
        );

        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSN_Device")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSD_Device")); // NOI18N
        retriveButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSN_RetriveDevices")); // NOI18N
        retriveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "ACSD_RetriveDevices")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField allDevices;
    private javax.swing.JComboBox devicesComboBox;
    private javax.swing.JPanel inputPasswordPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JButton retriveButton;
    private javax.swing.JTextField selectedDevice;
    private javax.swing.JTextField selectedDeviceCareer;
    // End of variables declaration//GEN-END:variables
    

    private class DeviceListener implements ActionListener /*, DocumentListener */ {
        
        protected final JButton okButton;
        
        DeviceListener() {
            this.okButton  = new JButton (NbBundle.getMessage (DeviceAnywhereCustomizerPanel.class, "LBL_ChooseOK")); //NOI18N
            this.okButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage (DeviceAnywhereCustomizerPanel.class, "ACSN_ChooseOK")); //NOI18N
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (DeviceAnywhereCustomizerPanel.class, "ACSD_ChooseOK")); //NOI18N
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */
        public void actionPerformed( ActionEvent e ) {
            
            String user = evaluator.evaluateGlobalProperty(
                    DeviceAnywhereDeploymentPlugin.PROP_USERID,
                    evaluator.evaluateProperty("deployment.instance")); //NOI18N

            if (user.trim().length() == 0){     
                NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine(
                        NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "MSG_InsertUsername"),  //NOI18N
                        NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "TITLE_InsertUsername")); //NOI18N
                DialogDisplayer.getDefault().notify(input);
                if ( input.getValue() == NotifyDescriptor.OK_OPTION){
                    user = input.getInputText();
                } else {
                    return;
                }                
            }

            String password = evaluator.evaluateGlobalProperty(
                    DeviceAnywhereDeploymentPlugin.PROP_PASSWORD, 
                    evaluator.evaluateProperty("deployment.instance")); //NOI18N

            if (password.trim().length() == 0){     
                DialogDescriptor input = new DialogDescriptor(
                        inputPasswordPanel,  //NOI18N
                        NbBundle.getMessage(DeviceAnywhereCustomizerPanel.class, "TITLE_InsertPass")); //NOI18N
                DialogDisplayer.getDefault().createDialog(input).setVisible(true);
                if ( input.getValue() == NotifyDescriptor.OK_OPTION){
                    password = new String(passwordField.getPassword());
                } else {
                    return;
                }                
            }
            
            final DeviceChooser panel = new DeviceChooser (user, password, null);
            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };
            
//            panel.setSelectedMainClass(mainClassTextField.getText());
            panel.addChangeListener (new ChangeListener () {
                public void stateChanged(ChangeEvent e) {
                    if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                        // click button and finish the dialog with selected class
                        okButton.doClick ();
                    } else {
                        okButton.setEnabled (panel.getSelectedDevice() != null);
                    }
                }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                    panel,
                    NbBundle.getMessage (DeviceAnywhereCustomizerPanel.class, "TITLE_ChooseDevice" ), //NOI18N
                    true,
                    options,
                    options[0],
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
                devicesComboBox.removeItemListener(il);
                devices = panel.getLockedDevices();
                devicesComboBox.removeAllItems();
                int i =0;
                StringBuffer sb = new StringBuffer();
                for (ApplicationAPIDeviceWrapper elem : devices) {
                    devicesComboBox.addItem(elem.getName());
                    sb.append("DeviceID-"); //NOI18N
                    sb.append(i);
                    sb.append("="); //NOI18N
                    sb.append(elem.getId());
                    sb.append(","); //NOI18N
                    sb.append("DeviceName-"); //NOI18N
                    sb.append(i);
                    sb.append("="); //NOI18N
                    sb.append(elem.getName());                    
                    sb.append(","); //NOI18N
                    sb.append("DeviceCareer-"); //NOI18N
                    sb.append(i);
                    sb.append("="); //NOI18N
                    sb.append(elem.getCarrier());                    
                    sb.append(";"); //NOI18N
                    i++;
                }
                ApplicationAPIDeviceWrapper selected= panel.getSelectedDevice();
                devicesComboBox.setSelectedItem(selected.getName());
                selectedDevice.setText(String.valueOf(selected.getId()));
                selectedDeviceCareer.setText(selected.getCarrier());
                allDevices.setText(sb.toString());
                devicesComboBox.addItemListener(il);
            }
            dlg.dispose();
        }
    }
    
    ItemListener il = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            if (devices == null)
                return;
            String deviceName = (String) devicesComboBox.getSelectedItem();
            for (ApplicationAPIDeviceWrapper elem : devices) {
                assert deviceName != null : "Device name must not be null here"; //NOI18N
                if (deviceName.equals(elem.getName())){
                    selectedDevice.setText(String.valueOf(elem.getId()));
                    selectedDeviceCareer.setText(elem.getCarrier());
                    return;
                }
            }
            assert true : "Cache does not contain selected device!"; //NOI18N
        }
     };     
     
     DocumentListener dl = new DocumentListener() {
         public void changedUpdate(DocumentEvent e) {
            // retriveButton.setEnabled(jTextFieldUser.getText().trim().length() != 0);
         }
         public void insertUpdate(DocumentEvent e) {
             changedUpdate(e);
         }
         public void removeUpdate(DocumentEvent e) {
             changedUpdate(e);
         }
     };
}
