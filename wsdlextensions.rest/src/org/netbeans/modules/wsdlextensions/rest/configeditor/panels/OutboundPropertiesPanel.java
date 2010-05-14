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
import javax.swing.JComponent;
import org.netbeans.modules.wsdlextensions.rest.RESTMethod;
import org.netbeans.modules.wsdlextensions.rest.configeditor.OutboundProperties;
import org.netbeans.modules.wsdlextensions.rest.configeditor.ValidatableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class OutboundPropertiesPanel extends javax.swing.JPanel
    implements ValidatablePropertiesHolder {

    private RESTMethod method;
    private ValidatableProperties properties;

    /** Creates new form OutboundPropertiesPanel */
    public OutboundPropertiesPanel(RESTMethod method) {
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

        txtURL.addFocusListener(focusListener);
        txtAcceptTypes.addFocusListener(focusListener);
        txtAcceptLanguages.addFocusListener(focusListener);
        txtContentType.addFocusListener(focusListener);
        txtHeaders.addFocusListener(focusListener);
        txtParams.addFocusListener(focusListener);
        comboParamStyle.addFocusListener(focusListener);
        txtBasicAuthUserName.addFocusListener(focusListener);
        txtBasicAuthPassword.addFocusListener(focusListener);
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
        if (source == txtURL) {
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblURL.text"); // NOI18N
        } else if (source == txtAcceptTypes){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblAcceptTypes.text"); // NOI18N
        } else if (source == txtAcceptLanguages) {
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblAcceptLanguages.text"); // NOI18N
        } else if (source == txtContentType){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblContentType.text"); // NOI18N
        } else if (source == txtHeaders){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblHeaders.text"); // NOI18N
        } else if (source == txtParams){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblParams.text"); // NOI18N
        } else if (source == comboParamStyle){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblParamStyle.text"); // NOI18N
        } else if (source == txtBasicAuthUserName){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblBasicAuthUserName.text"); // NOI18N
        } else if (source == txtBasicAuthPassword){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblBasicAuthPassword.text"); // NOI18N
        } else if (source == txtUserDefined){
            tooltipTitle = NbBundle.getMessage(getClass(),
                    "OutboundPropertiesPanel.lblUserDefined.text"); // NOI18N
        }

        if (tooltip != null && tooltipTitle != null) {
            String newLine = System.getProperty("line.separator"); // NOI18N
            String[] desc = new String[]{tooltipTitle + newLine + newLine, tooltip};
            descriptionPanel.setText(desc[0], desc[1]);
        }
    }

    public ValidatableProperties getValidatableProperties() {

        String value = txtURL.getText();
        properties.put(OutboundProperties.URL_PROPERTY,value);

        properties.put(OutboundProperties.METHOD_PROPERTY, method.getName().toUpperCase());

        value = txtAcceptTypes.getText();
        properties.put(OutboundProperties.ACCEPT_TYPES_PROPERTY, value);

        value = txtAcceptLanguages.getText();
        properties.put(OutboundProperties.ACCEPT_LANGUAGES_PROPERTY, value);

        value = txtContentType.getText();
        properties.put(OutboundProperties.CONTENT_TYPE_PROPERTY, value);

        value = txtHeaders.getText();
        properties.put(OutboundProperties.HEADERS_PROPERTY, value);

        value = (String) comboParamStyle.getSelectedItem();
        properties.put(OutboundProperties.PARAM_STYLE_PROPERTY, value);

        value = txtParams.getText();
        properties.put(OutboundProperties.PARAMS_PROPERTY, value);

        value = txtBasicAuthUserName.getText();
        properties.put(OutboundProperties.BASIC_AUTH_USER_NAME_PROPERTY, value);

        value = txtBasicAuthPassword.getText();
        properties.put(OutboundProperties.BASIC_AUTH_PASSWORD_PROPERTY, value);

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

        String value = properties.getProperty(OutboundProperties.URL_PROPERTY);
        txtURL.setText(value);

        value = properties.getProperty(OutboundProperties.ACCEPT_TYPES_PROPERTY);
        txtAcceptTypes.setText(value);

        value = properties.getProperty(OutboundProperties.ACCEPT_LANGUAGES_PROPERTY);
        txtAcceptLanguages.setText(value);

        value = properties.getProperty(OutboundProperties.CONTENT_TYPE_PROPERTY);
        txtContentType.setText(value);

        value = properties.getProperty(OutboundProperties.HEADERS_PROPERTY);
        txtHeaders.setText(value);

        value = properties.getProperty(OutboundProperties.PARAM_STYLE_PROPERTY);
        comboParamStyle.setSelectedItem(value);

        value = properties.getProperty(OutboundProperties.PARAMS_PROPERTY);
        txtParams.setText(value);

        value = properties.getProperty(OutboundProperties.BASIC_AUTH_USER_NAME_PROPERTY);
        txtBasicAuthUserName.setText(value);

        value = properties.getProperty(OutboundProperties.BASIC_AUTH_PASSWORD_PROPERTY);
        txtBasicAuthPassword.setText(value);

        Set<String> userDefinedKeySet = new HashSet<String>();
        userDefinedKeySet.addAll(properties.keySet());
        userDefinedKeySet.removeAll(Arrays.asList(OutboundProperties.PRE_DEFINED_PROPERTIES));
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
        lblURL = new javax.swing.JLabel();
        txtURL = new javax.swing.JTextField();
        lblAcceptTypes = new javax.swing.JLabel();
        lblAcceptLanguages = new javax.swing.JLabel();
        txtAcceptTypes = new javax.swing.JTextField();
        txtAcceptLanguages = new javax.swing.JTextField();
        lblContentType = new javax.swing.JLabel();
        txtContentType = new javax.swing.JTextField();
        lblHeaders = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtHeaders = new javax.swing.JTextArea();
        lblParamStyle = new javax.swing.JLabel();
        comboParamStyle = new javax.swing.JComboBox();
        lblParams = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtParams = new javax.swing.JTextArea();
        lblBasicAuthUserName = new javax.swing.JLabel();
        lblBasicAuthPassword = new javax.swing.JLabel();
        txtBasicAuthUserName = new javax.swing.JTextField();
        txtBasicAuthPassword = new javax.swing.JPasswordField();
        lblUserDefined = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtUserDefined = new javax.swing.JTextArea();
        descriptionPanel = new org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel();

        jSplitPane1.setDividerLocation(420);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        lblURL.setDisplayedMnemonic('U');
        lblURL.setLabelFor(txtURL);
        lblURL.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblURL.text")); // NOI18N

        txtURL.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtURL.toolTipText")); // NOI18N

        lblAcceptTypes.setDisplayedMnemonic('A');
        lblAcceptTypes.setLabelFor(txtAcceptTypes);
        lblAcceptTypes.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblAcceptTypes.text")); // NOI18N

        lblAcceptLanguages.setDisplayedMnemonic('L');
        lblAcceptLanguages.setLabelFor(txtAcceptLanguages);
        lblAcceptLanguages.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblAcceptLanguages.text")); // NOI18N

        txtAcceptTypes.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtAcceptTypes.toolTipText")); // NOI18N

        txtAcceptLanguages.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtAcceptLanguages.toolTipText")); // NOI18N
        txtAcceptLanguages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAcceptLanguagesActionPerformed(evt);
            }
        });

        lblContentType.setDisplayedMnemonic('C');
        lblContentType.setLabelFor(txtContentType);
        lblContentType.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblContentType.text")); // NOI18N

        txtContentType.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtContentType.toolTipText")); // NOI18N

        lblHeaders.setDisplayedMnemonic('H');
        lblHeaders.setLabelFor(txtHeaders);
        lblHeaders.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblHeaders.text")); // NOI18N

        txtHeaders.setColumns(20);
        txtHeaders.setRows(5);
        txtHeaders.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtHeaders.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(txtHeaders);

        lblParamStyle.setDisplayedMnemonic('P');
        lblParamStyle.setLabelFor(comboParamStyle);
        lblParamStyle.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblParamStyle.text")); // NOI18N

        comboParamStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Query", "Matrix" }));
        comboParamStyle.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtQueryStyle.toolTipText")); // NOI18N

        lblParams.setDisplayedMnemonic('r');
        lblParams.setLabelFor(txtParams);
        lblParams.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblParams.text")); // NOI18N

        txtParams.setColumns(20);
        txtParams.setRows(5);
        txtParams.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtParams.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(txtParams);

        lblBasicAuthUserName.setDisplayedMnemonic('N');
        lblBasicAuthUserName.setLabelFor(txtBasicAuthUserName);
        lblBasicAuthUserName.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblBasicAuthUserName.text")); // NOI18N

        lblBasicAuthPassword.setDisplayedMnemonic('w');
        lblBasicAuthPassword.setLabelFor(txtBasicAuthPassword);
        lblBasicAuthPassword.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblBasicAuthPassword.text")); // NOI18N

        txtBasicAuthUserName.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtBasicAuthUserName.toolTipText")); // NOI18N

        txtBasicAuthPassword.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtBasicAuthPassword.toolTipText")); // NOI18N

        lblUserDefined.setDisplayedMnemonic('D');
        lblUserDefined.setLabelFor(txtUserDefined);
        lblUserDefined.setText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.lblUserDefined.text")); // NOI18N

        txtUserDefined.setColumns(20);
        txtUserDefined.setRows(5);
        txtUserDefined.setToolTipText(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.txtUserDefined.toolTipText")); // NOI18N
        jScrollPane3.setViewportView(txtUserDefined);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblURL)
                    .add(lblAcceptTypes)
                    .add(lblAcceptLanguages)
                    .add(lblContentType)
                    .add(lblHeaders)
                    .add(lblParamStyle)
                    .add(lblParams)
                    .add(lblBasicAuthUserName)
                    .add(lblBasicAuthPassword)
                    .add(lblUserDefined))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtAcceptTypes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtAcceptLanguages, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtContentType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(comboParamStyle, 0, 429, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtBasicAuthUserName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                    .add(txtBasicAuthPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblURL)
                    .add(txtURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblAcceptTypes)
                    .add(txtAcceptTypes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtAcceptLanguages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAcceptLanguages))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblContentType)
                    .add(txtContentType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblHeaders)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblParamStyle)
                    .add(comboParamStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblParams)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblBasicAuthUserName)
                    .add(txtBasicAuthUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblBasicAuthPassword)
                    .add(txtBasicAuthPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(lblUserDefined)
                        .add(94, 94, 94))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jSplitPane1.setLeftComponent(jPanel1);
        jSplitPane1.setBottomComponent(descriptionPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 564, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OutboundPropertiesPanel.class, "OutboundPropertiesPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void txtAcceptLanguagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAcceptLanguagesActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_txtAcceptLanguagesActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comboParamStyle;
    private org.netbeans.modules.wsdlextensions.rest.configeditor.panels.DescriptionPanel descriptionPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lblAcceptLanguages;
    private javax.swing.JLabel lblAcceptTypes;
    private javax.swing.JLabel lblBasicAuthPassword;
    private javax.swing.JLabel lblBasicAuthUserName;
    private javax.swing.JLabel lblContentType;
    private javax.swing.JLabel lblHeaders;
    private javax.swing.JLabel lblParamStyle;
    private javax.swing.JLabel lblParams;
    private javax.swing.JLabel lblURL;
    private javax.swing.JLabel lblUserDefined;
    private javax.swing.JTextField txtAcceptLanguages;
    private javax.swing.JTextField txtAcceptTypes;
    private javax.swing.JPasswordField txtBasicAuthPassword;
    private javax.swing.JTextField txtBasicAuthUserName;
    private javax.swing.JTextField txtContentType;
    private javax.swing.JTextArea txtHeaders;
    private javax.swing.JTextArea txtParams;
    private javax.swing.JTextField txtURL;
    private javax.swing.JTextArea txtUserDefined;
    // End of variables declaration//GEN-END:variables

}
