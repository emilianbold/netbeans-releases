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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.NbBundle;

/** A single panel for a wizard - the GUI portion.
 *
 * @author vkraemer
 */
// TODO : force validation to key release instead of StateChange
public class CreateServerVisualPanel extends javax.swing.JPanel {

    /** The wizard panel descriptor associated with this GUI panel.
     * If you need to fire state changes or something similar, you can
     * use this handle to do so.
     */
//    private final AddInstancePortsDefPanel panel;
//    private final TargetServerData targetData;
    private static javax.swing.SpinnerNumberModel adminPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel instanceHttpPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel adminJmxPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel jmsPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel httpsPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbSslPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    private static javax.swing.SpinnerNumberModel orbMutualauthPortValue =
                new javax.swing.SpinnerNumberModel(0,0,65535,1);
    
    /** Create the wizard panel and set up some basic properties. */
    public CreateServerVisualPanel() { 
        PortSetter tmp = new PortSetter();
        instanceHttpPortValue.addChangeListener(tmp);
        adminJmxPortValue.addChangeListener(tmp);
        jmsPortValue.addChangeListener(tmp);
        orbPortValue.addChangeListener(tmp);
        httpsPortValue.addChangeListener(tmp);
        orbSslPortValue.addChangeListener(tmp);
        orbMutualauthPortValue.addChangeListener(tmp);
        adminPortValue.addChangeListener(tmp);
        initComponents();
        
        // XXX picking defaults isn't safe... but it is the best we have for the moment.
        int incr = (new Random()).nextInt(100)+1; //Integer. parseInt(targetData.getPort()) - 4848; // .rng.nextInt(100) + 1;
        adminPortValue.setValue(new Integer(4848+incr));
        instanceHttpPortValue.setValue(new Integer(8080+incr));
        adminJmxPortValue.setValue(new Integer(8686+incr));
        jmsPortValue.setValue(new Integer(7676+incr));
        orbPortValue.setValue(new Integer(3700+incr));
        httpsPortValue.setValue(new Integer(8181+incr));
        orbSslPortValue.setValue(new Integer(3820+incr));
        orbMutualauthPortValue.setValue(new Integer(3920+incr));
        
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(CreateServerVisualPanel.class, "TITLE_ServerPortProperties"));
        //msgLabel.setText(NbBundle.getMessage(CreateServerVisualPanel.class, "Msg_ValidPort"));
    }
    
    Number getAdminPort() {
        return adminPortValue.getNumber();
    }
    
    Number getInstanceHttpPort() {
        return instanceHttpPortValue.getNumber();
    }
    
    Number getAdminJmxPort() {
        return adminJmxPortValue.getNumber();
    }
    
    Number getJmsPort() {
        return jmsPortValue.getNumber();
    }
    
    Number getOrbPort() {
        return orbPortValue.getNumber();
    }
    
    Number getOrbSslPort() {
        return orbSslPortValue.getNumber();
    }
    
    Number getOrbMutualAuthPort() {
        return orbMutualauthPortValue.getNumber();
    }
    
    Number getHttpSslPort() {
        return httpsPortValue.getNumber();
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
    
    private class PortSetter implements javax.swing.event.ChangeListener {
        public void stateChanged(javax.swing.event.ChangeEvent ce) {
            fireChangeEvent();
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

        portConfPanel = new javax.swing.JPanel();
        adminJmxLbl = new javax.swing.JLabel();
        instancePortLbl = new javax.swing.JLabel();
        jmsPortLbl = new javax.swing.JLabel();
        orbListenerPortLbl = new javax.swing.JLabel();
        httpslPortLbl = new javax.swing.JLabel();
        orbSslPortLbl = new javax.swing.JLabel();
        orbMutualauthPortLbl = new javax.swing.JLabel();
        instanceHttpPort = new javax.swing.JSpinner();
        adminJmxPort = new javax.swing.JSpinner();
        jmsPort = new javax.swing.JSpinner();
        orbListenerPort = new javax.swing.JSpinner();
        httpsPort = new javax.swing.JSpinner();
        orbSslPort = new javax.swing.JSpinner();
        orbMutualauthPort = new javax.swing.JSpinner();
        adminPortLbl = new javax.swing.JLabel();
        adminPort = new javax.swing.JSpinner();
        spacingHack = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setFocusable(false);
        setLayout(new java.awt.GridBagLayout());

        portConfPanel.setLayout(new java.awt.GridBagLayout());

        adminJmxLbl.setLabelFor(adminJmxPort);
        org.openide.awt.Mnemonics.setLocalizedText(adminJmxLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_AdminJmxPort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(adminJmxLbl, gridBagConstraints);

        instancePortLbl.setLabelFor(instanceHttpPort);
        org.openide.awt.Mnemonics.setLocalizedText(instancePortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_InstancePort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(instancePortLbl, gridBagConstraints);

        jmsPortLbl.setLabelFor(jmsPort);
        org.openide.awt.Mnemonics.setLocalizedText(jmsPortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_JmsPort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(jmsPortLbl, gridBagConstraints);

        orbListenerPortLbl.setLabelFor(orbListenerPort);
        org.openide.awt.Mnemonics.setLocalizedText(orbListenerPortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbListener")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(orbListenerPortLbl, gridBagConstraints);

        httpslPortLbl.setLabelFor(httpsPort);
        org.openide.awt.Mnemonics.setLocalizedText(httpslPortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_HttpSslPort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(httpslPortLbl, gridBagConstraints);

        orbSslPortLbl.setLabelFor(orbSslPort);
        org.openide.awt.Mnemonics.setLocalizedText(orbSslPortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbSslPort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(orbSslPortLbl, gridBagConstraints);

        orbMutualauthPortLbl.setLabelFor(orbMutualauthPort);
        org.openide.awt.Mnemonics.setLocalizedText(orbMutualauthPortLbl, org.openide.util.NbBundle.getBundle(CreateServerVisualPanel.class).getString("LBL_OrbMutualauthPort")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 3);
        portConfPanel.add(orbMutualauthPortLbl, gridBagConstraints);

        instanceHttpPort.setModel(instanceHttpPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        portConfPanel.add(instanceHttpPort, gridBagConstraints);
        instanceHttpPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_httpPort")); // NOI18N
        instanceHttpPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_HTTP_PORT")); // NOI18N

        adminJmxPort.setModel(adminJmxPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        portConfPanel.add(adminJmxPort, gridBagConstraints);
        adminJmxPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_adminJmxPort")); // NOI18N
        adminJmxPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_ADMIN_JMX_PORT")); // NOI18N

        jmsPort.setModel(jmsPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        portConfPanel.add(jmsPort, gridBagConstraints);
        jmsPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_JMS_PORT")); // NOI18N
        jmsPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_JMS_PORT")); // NOI18N

        orbListenerPort.setModel(orbPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        portConfPanel.add(orbListenerPort, gridBagConstraints);
        orbListenerPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_ORB_PORT")); // NOI18N
        orbListenerPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_ORB_PORT")); // NOI18N

        httpsPort.setModel(httpsPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        portConfPanel.add(httpsPort, gridBagConstraints);
        httpsPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_HTTPS_PORT")); // NOI18N
        httpsPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_HTTPS_PORT")); // NOI18N

        orbSslPort.setModel(orbSslPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        portConfPanel.add(orbSslPort, gridBagConstraints);
        orbSslPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_ORB_SSL_PORT")); // NOI18N
        orbSslPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_ORB_SSL_PORT")); // NOI18N

        orbMutualauthPort.setModel(orbMutualauthPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        portConfPanel.add(orbMutualauthPort, gridBagConstraints);
        orbMutualauthPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_ORB_MUTUALAUTH_PORT")); // NOI18N
        orbMutualauthPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_ORB_MUTUALAUTH_PORT")); // NOI18N

        adminPortLbl.setLabelFor(adminPort);
        org.openide.awt.Mnemonics.setLocalizedText(adminPortLbl, org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "LBL_adminPortLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        portConfPanel.add(adminPortLbl, gridBagConstraints);

        adminPort.setModel(adminPortValue);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        portConfPanel.add(adminPort, gridBagConstraints);
        adminPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSN_adminPort")); // NOI18N
        adminPort.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateServerVisualPanel.class, "ACSD_AdminPort")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(portConfPanel, gridBagConstraints);

        spacingHack.setEnabled(false);
        spacingHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(spacingHack, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CreateServerVisualPanel_Desc")); // NOI18N
        jLabel1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        add(jLabel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName(bundle.getString("Step_ChooseUserDefinedLocalServer")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("AddUserDefinedLocalServerPanel_Desc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminJmxLbl;
    private javax.swing.JSpinner adminJmxPort;
    private javax.swing.JSpinner adminPort;
    private javax.swing.JLabel adminPortLbl;
    private javax.swing.JSpinner httpsPort;
    private javax.swing.JLabel httpslPortLbl;
    private javax.swing.JSpinner instanceHttpPort;
    private javax.swing.JLabel instancePortLbl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSpinner jmsPort;
    private javax.swing.JLabel jmsPortLbl;
    private javax.swing.JSpinner orbListenerPort;
    private javax.swing.JLabel orbListenerPortLbl;
    private javax.swing.JSpinner orbMutualauthPort;
    private javax.swing.JLabel orbMutualauthPortLbl;
    private javax.swing.JSpinner orbSslPort;
    private javax.swing.JLabel orbSslPortLbl;
    private javax.swing.JPanel portConfPanel;
    private javax.swing.JLabel spacingHack;
    // End of variables declaration//GEN-END:variables
        
}
