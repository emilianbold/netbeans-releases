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
 * AddAPIPanel.java
 *
 * Created on April 21, 2004, 2:12 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.*;
import java.lang.ref.SoftReference;
import java.io.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/**
 *
 * @author  dave
 */
public class AddAPIPanel extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    final private static String[] availableDefaultPermissions = {
        "javax.bluetooth.RemoteDevice.authorize", //NOI18N
        "javax.microedition.amms.control.camera.enableShutterFeedback", //NOI18N
        "javax.microedition.amms.control.tuner.setPreset", //NOI18N
        "javax.microedition.apdu.aid", //NOI18N
        "javax.microedition.apdu.sat", //NOI18N
        "javax.microedition.content.ContentHandler", //NOI18N
	"javax.microedition.io.Connector.bluetooth.client", //NOI18N
        "javax.microedition.io.Connector.bluetooth.server", //NOI18N
        "javax.microedition.io.Connector.cbs", //NOI18N
        "javax.microedition.io.Connector.comm", //NOI18N
        "javax.microedition.io.Connector.datagram", //NOI18N
        "javax.microedition.io.Connector.datagramreceiver", //NOI18N
        "javax.microedition.io.Connector.file.read", //NOI18N
        "javax.microedition.io.Connector.file.write", //NOI18N
        "javax.microedition.io.Connector.http", //NOI18N
        "javax.microedition.io.Connector.https", //NOI18N
        "javax.microedition.io.Connector.mms", //NOI18N
        "javax.microedition.io.Connector.obex.client", //NOI18N
        "javax.microedition.io.Connector.obex.client.tcp",
        "javax.microedition.io.Connector.obex.server", //NOI18N
        "javax.microedition.io.Connector.obex.server.tcp",
        "javax.microedition.io.Connector.serversocket", //NOI18N
        "javax.microedition.io.Connector.sip", //NOI18N
        "javax.microedition.io.Connector.sips", //NOI18N
        "javax.microedition.io.Connector.sms", //NOI18N
        "javax.microedition.io.Connector.socket", //NOI18N
        "javax.microedition.io.Connector.ssl", //NOI18N
        "javax.microedition.io.PushRegistry", //NOI18N
        "javax.microedition.jcrmi", //NOI18N
        "javax.microedition.location.LandmarkStore.category", //NOI18N
        "javax.microedition.location.LandmarkStore.management", //NOI18N
        "javax.microedition.location.LandmarkStore.read", //NOI18N
        "javax.microedition.location.LandmarkStore.write", //NOI18N
        "javax.microedition.location.Location", //NOI18N
        "javax.microedition.location.Orientation", //NOI18N
        "javax.microedition.location.ProximityListener", //NOI18N
        "javax.microedition.media.control.RecordControl", //NOI18N
        "javax.microedition.media.control.VideoControl.getSnapshot", //NOI18N
        "javax.microedition.payment.process", //NOI18N
        "javax.microedition.pim.ContactList.read", //NOI18N
        "javax.microedition.pim.ContactList.write", //NOI18N
        "javax.microedition.pim.EventList.read", //NOI18N
        "javax.microedition.pim.EventList.write", //NOI18N
        "javax.microedition.pim.ToDoList.read", //NOI18N
        "javax.microedition.pim.ToDoList.write", //NOI18N
        "javax.microedition.securityservice.CMSMessageSignatureService", //NOI18N
        "javax.wireless.messaging.cbs.receive", //NOI18N
        "javax.wireless.messaging.mms.receive", //NOI18N
        "javax.wireless.messaging.mms.send", //NOI18N
        "javax.wireless.messaging.sms.receive", //NOI18N
        "javax.wireless.messaging.sms.send" //NOI18N

    };
    
    private DialogDescriptor dd;
    private static SoftReference permissions;
    
    /** Creates new form AddAPIPanel */
    public AddAPIPanel(HashSet<String> set) {
        initComponents();
        initAccessibility();
        String[] availablePermissions = getPermissions();
        final Vector<String> v = new Vector<String>();
        for (int a = 0; a < availablePermissions.length; a ++)
            if (! set.contains(availablePermissions[a]))
                v.add(availablePermissions[a]);
        combo.setModel(new DefaultComboBoxModel(v));
        combo.getEditor().addActionListener(this);
        Component comp = combo.getEditor().getEditorComponent();
        if (comp instanceof JTextField)
            ((JTextField) comp).getDocument().addDocumentListener(this);
    }
    
    private static synchronized String[] getPermissions() {
        String[] result = null;
        if (permissions != null)
            result = (String[]) permissions.get();
        if (result == null) {
            result = loadPermissions();
            permissions = new SoftReference<String[]>(result);
        }
        return result;
    }
    
    private static String[] loadPermissions() {
        final FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("j2me/permissions.txt"); // NOI18N
        if (fo != null) {
            InputStream is = null;
            try {
                is = fo.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
                    String s;
                    final List<String> l = new ArrayList<String>();
                    while ((s = reader.readLine()) != null)
                        l.add(s);
                    return l.toArray(new String[l.size()]);
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return availableDefaultPermissions;
    }
    
    public String getAPIName() {
        return (String) combo.getEditor().getItem();
    }
    
    protected void setDialogDescriptor(final DialogDescriptor desc) {
        this.dd = desc;
        dd.setHelpCtx(new HelpCtx(AddAPIPanel.class));
        actionPerformed(null);
    }
    
    private boolean isValidClassName(final String s) {
        if (s.startsWith(".")  ||  s.endsWith(".")) //NOI18N
            return false;
        final StringTokenizer stk = new StringTokenizer(s, "."); //NOI18N
        while (stk.hasMoreTokens())
            if (!Utilities.isJavaIdentifier(stk.nextToken()))
                return false;
        return true;
    }
    
    public boolean isValid() {
        if (getAPIName().length() == 0  ||  !isValidClassName(getAPIName())) {
            errorPanel.setErrorBundleMessage("ERR_AddAPI_InvPackage");//NOI18N
            return false;
        }
        errorPanel.setErrorBundleMessage(null);
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        combo = new javax.swing.JComboBox();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(combo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AddAPIPanel.class, "LBL_AddAPI_API")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(jLabel1, gridBagConstraints);

        combo.setEditable(true);
        combo.setMaximumSize(new java.awt.Dimension(300, 32767));
        combo.setMinimumSize(new java.awt.Dimension(300, 23));
        combo.setPreferredSize(new java.awt.Dimension(250, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(combo, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddAPIPanel.class, "ACSN_AddAPI"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddAPIPanel.class, "ACSD_AddAPI"));
    }
    
    public void actionPerformed(@SuppressWarnings("unused")
	final java.awt.event.ActionEvent e) {
        dd.setValid(isValid());
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void insertUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    public void removeUpdate(@SuppressWarnings("unused")
	final javax.swing.event.DocumentEvent e) {
        actionPerformed(null);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox combo;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
