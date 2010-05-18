/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.rest.configeditor.panels;

import java.awt.Component;
import javax.swing.JList;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatablePropertiesHolder;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import org.netbeans.modules.wsdlextensions.rest.RESTMethod;
import org.netbeans.modules.wsdlextensions.rest.configeditor.InboundProperties;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class InboundPropertiesPanel extends javax.swing.JPanel 
        implements ValidatablePropertiesHolder {

    private RESTMethod method;
    private ValidatableProperties properties;

    /** Creates new form InBoundPropertiesPanel */
    public InboundPropertiesPanel(RESTMethod method) {
        initComponents();
        this.method = method;

        addListeners();
    }

    private void addListeners() {
        FocusListener focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateDescriptionArea(e);
            }
        };

        txtConsumeTypes.addFocusListener(focusListener);
        cbHTTPListener.addFocusListener(focusListener);
        txtPath.addFocusListener(focusListener);
        txtProduceTypes.addFocusListener(focusListener);
        cbForwardAsAttachment.addFocusListener(focusListener);
        txtUserDefined.addFocusListener(focusListener);
    }

    private void updateDescriptionArea(FocusEvent evt){
        if (descriptionPanel == null) {
            return;
        }

        descriptionPanel.setText(""); // NOI18N

        JComponent source = (JComponent) evt.getSource();
        String tooltip = source.getToolTipText();

        String tooltipTitle = null;
        if (source == cbHTTPListener) {
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblHTTPListenerName.text"); // NOI18N
        } else if (source == txtPath){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblPath.text"); // NOI18N
        } else if (source == txtConsumeTypes) {
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblConsumeTypes.text"); // NOI18N
        } else if (source == txtProduceTypes){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblProduceTypes.text"); // NOI18N
        } else if (source == cbForwardAsAttachment){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblForwardAsAttachment.text"); // NOI18N
        } else if (source == txtUserDefined){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.lblUserDefined.text"); // NOI18N
        }

        if (tooltip != null && tooltipTitle != null) {
            String newLine = System.getProperty("line.separator"); // NOI18N
            String[] desc = new String[]{tooltipTitle + newLine + newLine, tooltip};
            descriptionPanel.setText(desc[0], desc[1]);
        }
    }

    public ValidatableProperties getValidatableProperties() {

        String value = (String) cbHTTPListener.getSelectedItem();
        properties.put(InboundProperties.HTTP_LISTENER_NAME_PROPERTY, value);

        properties.put(InboundProperties.METHOD_PROPERTY, method.getName().toUpperCase());

        value = txtPath.getText();
        properties.put(InboundProperties.PATH_PROPERTY, value);

        value = txtConsumeTypes.getText();
        properties.put(InboundProperties.CONSUME_TYPES_PROPERTY, value);

        value = txtProduceTypes.getText();
        properties.put(InboundProperties.PRODUCE_TYPES_PROPERTY, value);

        boolean bValue = cbForwardAsAttachment.isSelected();
        properties.put(InboundProperties.FORWARD_AS_ATTACHMENT_PROPERTY,
                bValue ? "true" : "false"); // NOI18N

        value = txtUserDefined.getText();
        try {
            // Note: cannot load user defined properties into EditableProperties
            // directly!
            Properties userDefinedProperties = new Properties();
            byte[] bytes = value.getBytes("UTF-8"); // NOI18N
            InputStream input = new ByteArrayInputStream(bytes);
            userDefinedProperties.load(input);
            for (Object key : userDefinedProperties.keySet()) {
                properties.put((String)key, userDefinedProperties.getProperty((String)key));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return properties;
    }

    public void setValidatableProperties(ValidatableProperties properties) {

        this.properties = properties;

        String value = properties.getProperty(InboundProperties.HTTP_LISTENER_NAME_PROPERTY);
        cbHTTPListener.setSelectedItem(value);

        value = properties.getProperty(InboundProperties.PATH_PROPERTY);
        txtPath.setText(value);

        value = properties.getProperty(InboundProperties.CONSUME_TYPES_PROPERTY);
        txtConsumeTypes.setText(value);

        value = properties.getProperty(InboundProperties.PRODUCE_TYPES_PROPERTY);
        txtProduceTypes.setText(value);

        value = properties.getProperty(InboundProperties.FORWARD_AS_ATTACHMENT_PROPERTY);
        cbForwardAsAttachment.setSelected(
                value != null && value.equalsIgnoreCase("true")); // NOI18N

        Set<String> userDefinedKeySet = new HashSet<String>();
        userDefinedKeySet.addAll(properties.keySet());
        userDefinedKeySet.removeAll(Arrays.asList(InboundProperties.PRE_DEFINED_PROPERTIES));
        value = ""; // NOI18N
        String newLine = System.getProperty("line.separator"); // NOI18N
        for (String key : userDefinedKeySet) {
            value += key + "=" + properties.get(key) + newLine; // NOI18N
        }
        txtUserDefined.setText(value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        lblPath = new javax.swing.JLabel();
        lblHTTPListenerName = new javax.swing.JLabel();
        lblConsumeTypes = new javax.swing.JLabel();
        lblProduceTypes = new javax.swing.JLabel();
        lblForwardAsAttachment = new javax.swing.JLabel();
        txtPath = new javax.swing.JTextField();
        txtConsumeTypes = new javax.swing.JTextField();
        txtProduceTypes = new javax.swing.JTextField();
        cbForwardAsAttachment = new javax.swing.JCheckBox();
        lblUserDefined = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtUserDefined = new javax.swing.JTextArea();
        cbHTTPListener = new javax.swing.JComboBox();
        descriptionPanel = new org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel();

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        lblPath.setDisplayedMnemonic('P');
        lblPath.setLabelFor(txtPath);
        lblPath.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblPath.text")); // NOI18N

        lblHTTPListenerName.setDisplayedMnemonic('H');
        lblHTTPListenerName.setLabelFor(cbHTTPListener);
        lblHTTPListenerName.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblHTTPListenerName.text")); // NOI18N

        lblConsumeTypes.setDisplayedMnemonic('C');
        lblConsumeTypes.setLabelFor(txtConsumeTypes);
        lblConsumeTypes.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblConsumeTypes.text")); // NOI18N

        lblProduceTypes.setDisplayedMnemonic('r');
        lblProduceTypes.setLabelFor(txtProduceTypes);
        lblProduceTypes.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblProduceTypes.text")); // NOI18N

        lblForwardAsAttachment.setDisplayedMnemonic('F');
        lblForwardAsAttachment.setLabelFor(cbForwardAsAttachment);
        lblForwardAsAttachment.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblForwardAsAttachment.text")); // NOI18N

        txtPath.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.txtPath.toolTipText")); // NOI18N

        txtConsumeTypes.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.txtConsumeTypes.toolTipText")); // NOI18N
        txtConsumeTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtConsumeTypesActionPerformed(evt);
            }
        });

        txtProduceTypes.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.txtProduceTypes.toolTipText")); // NOI18N

        cbForwardAsAttachment.setMnemonic('F');
        cbForwardAsAttachment.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.cbForwardAsAttachment.toolTipText")); // NOI18N

        lblUserDefined.setDisplayedMnemonic('D');
        lblUserDefined.setLabelFor(txtUserDefined);
        lblUserDefined.setText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.lblUserDefined.text")); // NOI18N

        txtUserDefined.setColumns(20);
        txtUserDefined.setRows(5);
        txtUserDefined.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.txtUserDefined.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(txtUserDefined);

        cbHTTPListener.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default-listener", "default-listener-ssl" }));
        cbHTTPListener.setToolTipText(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.cbHTTPListener.toolTipText")); // NOI18N
        cbHTTPListener.setRenderer(new HTTPListenerComboBoxCellRenderer());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblForwardAsAttachment)
                    .add(lblPath)
                    .add(lblHTTPListenerName)
                    .add(lblConsumeTypes)
                    .add(lblProduceTypes)
                    .add(lblUserDefined))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(txtProduceTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(txtConsumeTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(txtPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                    .add(cbForwardAsAttachment)
                    .add(cbHTTPListener, 0, 419, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPath)
                    .add(txtPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblHTTPListenerName)
                    .add(cbHTTPListener, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblConsumeTypes)
                    .add(txtConsumeTypes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProduceTypes)
                    .add(txtProduceTypes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblForwardAsAttachment)
                    .add(cbForwardAsAttachment))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblUserDefined)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                .addContainerGap())
        );

        jSplitPane1.setTopComponent(jPanel1);
        jSplitPane1.setBottomComponent(descriptionPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InboundPropertiesPanel.class, "InboundPropertiesPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void txtConsumeTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtConsumeTypesActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtConsumeTypesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbForwardAsAttachment;
    private javax.swing.JComboBox cbHTTPListener;
    private org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel descriptionPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblConsumeTypes;
    private javax.swing.JLabel lblForwardAsAttachment;
    private javax.swing.JLabel lblHTTPListenerName;
    private javax.swing.JLabel lblPath;
    private javax.swing.JLabel lblProduceTypes;
    private javax.swing.JLabel lblUserDefined;
    private javax.swing.JTextField txtConsumeTypes;
    private javax.swing.JTextField txtPath;
    private javax.swing.JTextField txtProduceTypes;
    private javax.swing.JTextArea txtUserDefined;
    // End of variables declaration//GEN-END:variables


    class HTTPListenerComboBoxCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if ("default-listener".equals(value)) { // NOI18N
                value = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.defaultHTTPListenerName"); // NOI18N
            } else if ("default-listener-ssl".equals(value)) { // NOI18N
                value = NbBundle.getMessage(getClass(),
                    "InboundPropertiesPanel.defaultHTTPSListenerName"); // NOI18N
            }
            return super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
        }

    }
}
