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

/*
 * DeviceChooser.java
 *
 * Created on May 1, 2007, 12:09 PM
 */

package org.netbeans.modules.deployment.deviceanywhere;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPI;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIDeviceWrapper;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIGetLockedDevicesReturn;
import org.netbeans.modules.deployment.deviceanywhere.service.ApplicationAPIService;
import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  suchys
 */
public class DeviceChooser extends javax.swing.JPanel {
    
    protected ChangeListener changeListener;
    private List<ApplicationAPIDeviceWrapper> devices;
    
    /** Creates new form DeviceChooser */
    public DeviceChooser(String user, String password, ApplicationAPIDeviceWrapper selectedDevice, int selectedService) {
        initComponents();
        initView(user, password, selectedDevice, selectedService);
    }
    
    private void initView(final String user, final String password, final ApplicationAPIDeviceWrapper selectedDevice, final int selectedService){
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceList.setListData (getWarmupList ());
        deviceList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent evt) {
                if (changeListener != null){
                    changeListener.stateChanged (new ChangeEvent (evt));                        
                }
            }
        });
        // support for double click to finish dialog with selected class
        deviceList.addMouseListener (new MouseListener () {
            public void mouseClicked (MouseEvent e) {
                if (MouseUtils.isDoubleClick (e)) {
                    if (getSelectedDevice () != null) {
                        if (changeListener != null) {
                            changeListener.stateChanged (new ChangeEvent (e));
                        }
                    }
                }
            }
            public void mousePressed (MouseEvent e) {}
            public void mouseReleased (MouseEvent e) {}
            public void mouseEntered (MouseEvent e) {}
            public void mouseExited (MouseEvent e) {}
        });
//done
        RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                try {
                    ApplicationAPIService service = new ApplicationAPIService(selectedService);
                    ApplicationAPI port = service.getApplicationAPI();
                    final ApplicationAPIGetLockedDevicesReturn wrapper = port.getLockedDevices(user, password);
                    final int returnCode = wrapper.getReturnCode();
                    if (returnCode != 0){
                        SwingUtilities.invokeLater( new Runnable () {
                            public void run () {
                                String message = null;
                                if (returnCode == 1){
                                    message = NbBundle.getMessage (DeviceChooser.class, "MSG_InternalError"); // NOI18N
                                } else if (returnCode == 2){
                                    message = NbBundle.getMessage (DeviceChooser.class, "MSG_BadLogin"); // NOI18N
                                }
                                deviceList.setListData (new String[] { message }); 
                            }
                        });
                        return;
                    }
                    devices = wrapper.getDeviceWrappers().getDeviceWrappers();
                    //only for testing
//                    devices = new ArrayList<ApplicationAPIDeviceWrapper>();
//                    ApplicationAPIDeviceWrapper dev = new ApplicationAPIDeviceWrapper();
//                    dev.setId(22);
//                    dev.setName("Kachny");                  
//                    devices.add(dev);
//                    dev = new ApplicationAPIDeviceWrapper();
//                    dev.setId(11);
//                    dev.setName("Kachnicky");                  
//                    devices.add(dev);                  
                    //
                    if (devices.isEmpty ()) {
                        SwingUtilities.invokeLater( new Runnable () {
                            public void run () {
                                deviceList.setListData (new String[] { NbBundle.getMessage (DeviceChooser.class, "MSG_NoDevice") } ); // NOI18N
                            }
                        });
                        return;
                    } else {
                        final String[] result = new String [devices.size()];
                        int i = 0;
                        for (ApplicationAPIDeviceWrapper elem : devices) {
                            result[i++] = elem.getName();
                        }
                        //Arrays.sort (devices); //we must sort whole List<ApplicationAPIDeviceWrapper>
                        SwingUtilities.invokeLater(new Runnable () {
                            public void run () {
                                deviceList.setListData (result);
                                if(selectedDevice != null)
                                    deviceList.setSelectedValue(selectedDevice.getName(), true);
                                else    
                                    deviceList.setSelectedIndex (0);
                                return;
                            }
                        });
                    }
                } catch (Exception ex){
                    SwingUtilities.invokeLater( new Runnable () {
                        public void run () {
                            deviceList.setListData (new String[] { NbBundle.getMessage (DeviceChooser.class, "MSG_ConnectionError") } ); // NOI18N                    
                        }
                    });
                    if (ex instanceof ClassNotFoundException){
                        ErrorManager.getDefault().notify(ErrorManager.USER, ex);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                } 
            }
        });
        
//        if (dialogSubtitle != null) {
//            Mnemonics.setLocalizedText (jLabel1, dialogSubtitle);
//        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        deviceList = new javax.swing.JList();

        jLabel1.setLabelFor(deviceList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeviceChooser.class, "LBL_AvailableDevices")); // NOI18N

        jScrollPane1.setViewportView(deviceList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .add(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DeviceChooser.class, "ACSN_AvailableDevices")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DeviceChooser.class, "ACSD_AvailableDevices")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList deviceList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private Object[] getWarmupList () {
        return new Object[] {NbBundle.getMessage (DeviceChooser.class, "MSG_LoadingDevices")}; // NOI18N
    }

    public ApplicationAPIDeviceWrapper getSelectedDevice() {
        if (isValidDeviceName (deviceList.getSelectedValue ())) {
            return devices.get(deviceList.getSelectedIndex());
        } else {    
            return null;
        }
    }

    public List<ApplicationAPIDeviceWrapper> getLockedDevices() {
        return devices;
    }
    
    public synchronized void addChangeListener (ChangeListener l) {
        changeListener = l;
    }
    
    public synchronized void removeChangeListener (ChangeListener l) {
        changeListener = null;
    }

    private boolean isValidDeviceName(Object object) {
        return devices != null && !devices.isEmpty();
    }
    
}
