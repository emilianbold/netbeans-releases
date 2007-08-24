/*
 * InputValuesJPanel.java
 *
 * Created on July 27, 2007, 2:04 PM
 */

package org.netbeans.modules.websvc.rest.component.palette;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.netbeans.modules.websvc.rest.support.Inflector;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.util.NbBundle;

/**
 *
 * @author  nam
 */
public class RestComponentSetupPanel extends javax.swing.JPanel {

    private Project project;
    private ParamTableModel tableModel;
    private Map<String, String> paramTypeMap;
    private Map<String, Object> paramValueMap;
    private boolean methodNameModified = false;

    /** Creates new form InputValuesJPanel */
    public RestComponentSetupPanel(String uriTemplate, String resourceName, Map<String, String> paramsTypes, Project project, boolean resourceExists) {
        initComponents();

        uriTemplateTF.setText(uriTemplate);
        updateMethodName();
        resourceNameTF.setText(resourceName);

        paramTypeMap = paramsTypes;
        this.project = project;
        tableModel = new ParamTableModel();
        paramTable.setModel(tableModel);
        tableModel.init();

        if (resourceExists) {
            paramLabel.setVisible(false);
            paramScrollPane.setVisible(false);
            messageLabel.setVisible(true);
        } else {
            messageLabel.setVisible(false);
        }
    }

    public String getUriTemplate() {
        return uriTemplateTF.getText().trim();
    }

    public String getMethodName() {
        return methodNameTF.getText().trim();
    }

    public String getResourceName() {
        return resourceNameTF.getText().trim();
    }

    public Map<String, Object> getInputParamValues() {
        if (paramValueMap == null) {
            paramValueMap = new HashMap<String, Object>();
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Object value = tableModel.getValueAt(row, 2);
                if (value != null) {
                    String param = (String) tableModel.getValueAt(row, 0);
                    paramValueMap.put(param, value);
                }
            }
        }
        return paramValueMap;
    }

    private class ParamTable extends JTable {

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 2) {
                String paramName = (String) tableModel.getValueAt(row, 0);
                String typeName = paramTypeMap.get(paramName);
                Class type = getType(typeName);
                if (Enum.class.isAssignableFrom(type)) {
                    JComboBox combo = new JComboBox(type.getEnumConstants());
                    return new DefaultCellEditor(combo);
                } else if ("boolean".equals(typeName) || Boolean.class.getName().equals(typeName)) {
                    JCheckBox cb = new JCheckBox();
                    cb.setHorizontalAlignment(JLabel.CENTER);
                    cb.setBorderPainted(true);
                    return new DefaultCellEditor(cb);
                } else if (paramName.toLowerCase().contains(Constants.PASSWORD)) {
                    return new DefaultCellEditor(new JPasswordField());
                }
            }
            return super.getCellEditor(row, column);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column == 2) {
                return new ParamCellRenderer();
            }
            return super.getCellRenderer(row, column);
        }
    }

    public Class getType(String typeName) {

        List<ClassPath> classPaths = SourceGroupSupport.gerClassPath(project);
        for (ClassPath cp : classPaths) {
            try {
                Class ret = Util.getPrimitiveType(typeName);
                if (ret != null) {
                    return ret;
                }
                ClassLoader cl = cp.getClassLoader(true);
                if (cl != null) {
                    return cl.loadClass(typeName);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(RestComponentSetupPanel.class.getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return String.class;
    }

    private class ParamCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 2) {
                throw new IllegalArgumentException("Wrong column number"); //NOI18N
            }

            Component ret = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String paramName = (String) tableModel.getValueAt(row, 0);
            String typeName = paramTypeMap.get(paramName);
            if (value == null) {
                return new JLabel(NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_NotSet"));
            } else if (value instanceof Boolean) {
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(JLabel.CENTER);
                cb.setBorderPainted(true);
                cb.setSelected((Boolean) value);
                return cb;
            } else if (paramName.contains(Constants.PASSWORD)) {
                return new JPasswordField((String) value);
            }
            return ret;
        }
    }

    private class ParamTableModel extends DefaultTableModel {

        ParamTableModel() {
            super(new Object[][]{{null, null, null}, {null, null, null}, {null, null, null}, {null, null, null}}, new String[]{NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_Name"), NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_Type"), NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_ConstantValue")});
        }
        Class[] types = new Class[]{java.lang.String.class, java.lang.String.class, java.lang.Object.class};
        boolean[] canEdit = new boolean[]{false, false, true};

        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }

        public void init() {
            List<String> params = new ArrayList(paramTypeMap.keySet());
            String userParam = null;
            String passwordParam = null;
            String licenseParam = null;
            for (String key : paramTypeMap.keySet()) {
                if (userParam == null && key.toLowerCase().indexOf("user") > -1) {
                    userParam = key;
                    params.remove(key);
                } else if (passwordParam == null && key.toLowerCase().contains(Constants.PASSWORD)) {
                    passwordParam = key;
                    params.remove(key);
                } else if (licenseParam == null && key.toLowerCase().indexOf("license") > -1) {
                    licenseParam = key;
                    params.remove(key);
                }
            }
            if (licenseParam != null) {
                params.add(0, licenseParam);
            }
            if (passwordParam != null) {
                params.add(0, passwordParam);
            }
            if (userParam != null) {
                params.add(0, userParam);
            }

            setRowCount(params.size());
            for (int row = 0; row < params.size(); row++) {
                String param = params.get(row);
                setValueAt(param, row, 0);
                setValueAt(paramTypeMap.get(param), row, 1);
            }
        }
    }

    private void updateMethodName() {
        if (!methodNameModified) {
            methodNameTF.setText(computeMethodName());
        }
    }

    private String computeMethodName() {
        return "get" + Inflector.getInstance().camelize(getUriTemplate()) + GenericResourceBean.RESOURCE_SUFFIX;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        paramLabel = new javax.swing.JLabel();
        paramScrollPane = new javax.swing.JScrollPane();
        paramTable = new ParamTable();
        uriTemplateLabel = new javax.swing.JLabel();
        uriTemplateTF = new javax.swing.JTextField();
        methodNameLabel = new javax.swing.JLabel();
        methodNameTF = new javax.swing.JTextField();
        subresourceLocatorLabel = new javax.swing.JLabel();
        subresourceLabel = new javax.swing.JLabel();
        resourceNameLabel = new javax.swing.JLabel();
        resourceNameTF = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        paramLabel.setLabelFor(paramTable);
        paramLabel.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_Parameters")); // NOI18N

        paramTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        paramScrollPane.setViewportView(paramTable);

        uriTemplateLabel.setLabelFor(uriTemplateTF);
        org.openide.awt.Mnemonics.setLocalizedText(uriTemplateLabel, org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_UriTemplate")); // NOI18N

        uriTemplateTF.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "RestComponentSetupPanel.uriTemplateTF.text")); // NOI18N
        uriTemplateTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                uriTemplateTFKeyReleased(evt);
            }
        });

        methodNameLabel.setLabelFor(methodNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(methodNameLabel, org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_MethodName")); // NOI18N

        methodNameTF.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "RestComponentSetupPanel.methodNameTF.text")); // NOI18N
        methodNameTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                methodNameTFKeyReleased(evt);
            }
        });

        subresourceLocatorLabel.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_SubresourceLocator")); // NOI18N

        subresourceLabel.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_Subresource")); // NOI18N

        resourceNameLabel.setLabelFor(resourceNameTF);
        org.openide.awt.Mnemonics.setLocalizedText(resourceNameLabel, org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "LBL_ResourceName")); // NOI18N

        resourceNameTF.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "RestComponentSetupPanel.resourceNameTF.text")); // NOI18N
        resourceNameTF.setEnabled(false);

        messageLabel.setText(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "MSG_AlreadyGenerated")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(subresourceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .add(subresourceLocatorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(methodNameLabel)
                            .add(uriTemplateLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(uriTemplateTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                            .add(methodNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(paramLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(resourceNameLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(resourceNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE))
                            .add(paramScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(subresourceLocatorLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(uriTemplateLabel)
                    .add(uriTemplateTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(methodNameLabel)
                    .add(methodNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(subresourceLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourceNameLabel)
                    .add(resourceNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paramLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(paramScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );

        paramLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RestComponentSetupPanel.class, "MSG_SetConstantValues")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void uriTemplateTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_uriTemplateTFKeyReleased
        // TODO add your handling code here:
        updateMethodName();
    }//GEN-LAST:event_uriTemplateTFKeyReleased

    private void methodNameTFKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_methodNameTFKeyReleased
        methodNameModified = true;
    }//GEN-LAST:event_methodNameTFKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel methodNameLabel;
    private javax.swing.JTextField methodNameTF;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JScrollPane paramScrollPane;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel resourceNameLabel;
    private javax.swing.JTextField resourceNameTF;
    private javax.swing.JLabel subresourceLabel;
    private javax.swing.JLabel subresourceLocatorLabel;
    private javax.swing.JLabel uriTemplateLabel;
    private javax.swing.JTextField uriTemplateTF;
    // End of variables declaration//GEN-END:variables
}