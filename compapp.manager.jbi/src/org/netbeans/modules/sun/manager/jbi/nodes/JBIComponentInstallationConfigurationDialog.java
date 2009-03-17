/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sun.manager.jbi.nodes;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.management.Attribute;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.nodes.property.SchemaBasedConfigPropertySupportFactory;
import org.netbeans.modules.sun.manager.jbi.nodes.property.PropertySheetOwner;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/**
 *
 * @author  jqian
 */
public class JBIComponentInstallationConfigurationDialog
        extends javax.swing.JDialog implements PropertySheetOwner {

    private String componentName;
    private JBIComponentConfigurationDescriptor rootDescriptor;
    private Properties defaultProperties;
    private Properties properties;
    private boolean cancelled = false;

    /** Creates new form JBIComponentInstallationConfigurationDialog */
    public JBIComponentInstallationConfigurationDialog(String componentName,
            JBIComponentConfigurationDescriptor rootDescriptor) {
        super((Frame) null, true);
        this.componentName = componentName;
        initComponents();

        this.rootDescriptor = rootDescriptor;
        initPropertyTable();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Properties getProperties() {
        return properties;
    }

    private void initPropertyTable() {

        final PropertySheetView propertySheetView = new PropertySheetView();
        jScrollPane1.setViewportView(propertySheetView);

        List<PropertySupport> propertySupports = new ArrayList<PropertySupport>();
        defaultProperties = new Properties();
        addPropertySupport(propertySupports, rootDescriptor, defaultProperties);
        
        properties = new Properties(defaultProperties);

        final Sheet sheet = new Sheet();
        Sheet.Set sheetSet = new Sheet.Set();
        sheetSet.setName("Component Configuration");
        if (propertySupports != null) {
            sheetSet.put(propertySupports.toArray(new PropertySupport[]{}));
            sheet.put(sheetSet);
        }

        final Node fakeNode = new AbstractNode(Children.LEAF) {

            @Override
            public Node.PropertySet[] getPropertySets() {
                return sheet.toArray();
            }
        };

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                propertySheetView.setNodes(new Node[]{fakeNode});
                jScrollPane1.validate();
            }
        });
    }

    private void addPropertySupport(List<PropertySupport> propertySupports,
            JBIComponentConfigurationDescriptor configDescriptor, 
            Properties properties) {

        if (configDescriptor instanceof JBIComponentConfigurationDescriptor.ApplicationConfiguration ||
                configDescriptor instanceof JBIComponentConfigurationDescriptor.ApplicationVariable) {
            // do nothing
        } else if (configDescriptor.isProperty()) {

            if (!configDescriptor.showDisplayAtInstallation()) {
                return;
            }

            String name = configDescriptor.getName();
            String value = configDescriptor.getDefaultValue();
            QName typeQName = configDescriptor.getTypeQName();

            if (value == null) {
                if (JBIComponentConfigurationDescriptor.XSD_STRING.equals(typeQName)) {
                    value = "";                    
                } else if (JBIComponentConfigurationDescriptor.XSD_BOOLEAN.equals(typeQName)) {
                    value = "false";                    
                } else if (JBIComponentConfigurationDescriptor.XSD_INT.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_BYTE.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_SHORT.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {
                    value = "0";   // FIXME: Int editor should accept empty value                 
                }
            }
            
            properties.setProperty(name, value);

            
            Object attrValue = null;
            if (JBIComponentConfigurationDescriptor.XSD_INT.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_BYTE.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_SHORT.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_POSITIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NEGATIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NON_POSITIVE_INTEGER.equals(typeQName) ||
                    JBIComponentConfigurationDescriptor.XSD_NON_NEGATIVE_INTEGER.equals(typeQName)) {
                attrValue = Integer.parseInt(value);
            } else if (JBIComponentConfigurationDescriptor.XSD_STRING.equals(typeQName)) {
                attrValue = value;
            } else if (JBIComponentConfigurationDescriptor.XSD_BOOLEAN.equals(typeQName)) {
                attrValue = Boolean.parseBoolean(value);
            } else {
                String newline = System.getProperty("line.separator"); // NOI18N
                throw new RuntimeException("The type for configuration property '" + 
                        name + "' is not supported: " + typeQName + newline +
                        "The supported types are: " + newline + 
                        Arrays.toString(JBIComponentConfigurationDescriptor.SUPPORTED_TYPES));
            }

            Attribute attr = new Attribute(name, attrValue);

            JBIComponentConfigurationMBeanAttributeInfo attrInfo =
                    new JBIComponentConfigurationMBeanAttributeInfo(
                    configDescriptor,
                    attrValue.getClass().getName(),
                    true, true, false);

            propertySupports.add(
                    SchemaBasedConfigPropertySupportFactory.getPropertySupport(
                    this, attr, attrInfo, componentName));

        } else { // PropertyGroup or root descriptor
            for (JBIComponentConfigurationDescriptor childDescriptor : 
                    configDescriptor.getChildren()) {
                addPropertySupport(propertySupports, childDescriptor, properties);
            }
        }
    }

    public Attribute setSheetProperty(String attrName, Object value) {
        properties.setProperty(attrName, value.toString());
        return new Attribute(attrName, value);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        installButton = new javax.swing.JButton();
        restoreDefaultButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(JBIComponentInstallationConfigurationDialog.class, "JBIComponentInstallationConfigurationDialog.title", new Object[] {componentName})); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JBIComponentInstallationConfigurationDialog.class, "JBIComponentInstallationConfigurationDialog.jLabel1.text")); // NOI18N

        installButton.setText(org.openide.util.NbBundle.getMessage(JBIComponentInstallationConfigurationDialog.class, "JBIComponentInstallationConfigurationDialog.installButton.text")); // NOI18N
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        restoreDefaultButton.setText(org.openide.util.NbBundle.getMessage(JBIComponentInstallationConfigurationDialog.class, "JBIComponentInstallationConfigurationDialog.restoreDefaultButton.text")); // NOI18N
        restoreDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreDefaultButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(JBIComponentInstallationConfigurationDialog.class, "JBIComponentInstallationConfigurationDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(installButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(restoreDefaultButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(11, 11, 11)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(restoreDefaultButton)
                    .add(installButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void restoreDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreDefaultButtonActionPerformed
        properties = new Properties(defaultProperties);
        initPropertyTable();
}//GEN-LAST:event_restoreDefaultButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelled = true;
        this.dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_installButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JBIComponentInstallationConfigurationDialog dialog =
                        new JBIComponentInstallationConfigurationDialog("FOO", null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton installButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton restoreDefaultButton;
    // End of variables declaration//GEN-END:variables
}
