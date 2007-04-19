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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.DatasourceUIHelper;
import org.netbeans.modules.j2ee.common.EventRequestProcessor;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Action;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.AsynchronousAction;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Context;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Panel for adding data source reference.
 * @author Tomas Mysik
 */
public class DataSourceReferencePanel extends JPanel {
    public static final String IS_VALID = DataSourceReferencePanel.class.getName() + ".IS_VALID"; // NOI18N
    private final J2eeModuleProvider provider;
    private final Set<String> refNames;
    private final Set<Datasource> moduleDatasources;
    private final Set<Datasource> serverDatasources;
    private final boolean isDsApiSupportedByServerPlugin;
    
    /** Creates new form DataSourceReferencePanel */
    public DataSourceReferencePanel(J2eeModuleProvider provider, Set<String> refNames, Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources) {
        initComponents();
        this.provider = provider;
        this.refNames = refNames;
        this.moduleDatasources = moduleDatasources;
        this.serverDatasources = serverDatasources;
        isDsApiSupportedByServerPlugin = isDsApiSupportedByServerPlugin();
        
        registerListeners();
        
        setupErrorLabel();
        setupWarningLabel();
        setupAddButton();
        setupComboBoxes();
        handleComboBoxes();
        
        populate();
        
        verify();
    }
    
    /**
     * Get the name of the data source reference.
     * @return the reference name.
     */
    public String getReferenceName() {
        return dsReferenceText.getText().trim();
    }
    
    /**
     * Get the data source.
     * @return selected data source.
     */
    public Datasource getDataSource() {
        if (projectDsRadio.isSelected()) {
            return (Datasource) projectDsCombo.getSelectedItem();
        }
        return (Datasource) serverDsCombo.getSelectedItem();
    }
    
    public boolean copyDataSourceToProject() {
        if (projectDsRadio.isSelected()) {
            return false;
        }
        return dsCopyToProjectCheck.isSelected();
    }
    
    // TODO this method should be reviewed (handle 'Missing server' error)
    private boolean isDsApiSupportedByServerPlugin() {
        return (provider != null
                && provider.isDatasourceCreationSupported()
                && Util.isValidServerInstance(provider));
    }
    
    private void registerListeners() {
        // text field
        dsReferenceText.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verify();
            }
        });
        
        // radio buttons
        projectDsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
                handleComboBoxes();
            }
        });
        serverDsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
                handleComboBoxes();
            }
        });
        
        // combo boxes
        projectDsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
        serverDsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
    }
    
    private void setupComboBoxes() {
        projectDsCombo.setPrototypeDisplayValue(SelectDatabasePanel.PROTOTYPE_VALUE);
        projectDsCombo.setRenderer(DatasourceUIHelper.createDatasourceListCellRenderer());
        serverDsCombo.setRenderer(DatasourceUIHelper.createDatasourceListCellRenderer());
    }
    
    private void handleComboBoxes() {
        if (projectDsRadio.isSelected()) {
            projectDsCombo.setEnabled(true);
            serverDsCombo.setEnabled(false);
            dsCopyToProjectCheck.setEnabled(false);
        } else {
            projectDsCombo.setEnabled(false);
            serverDsCombo.setEnabled(true);
            dsCopyToProjectCheck.setEnabled(true);
        }
    }
    
    private void populate() {
        populateDataSources(moduleDatasources, projectDsCombo);
        populateDataSources(serverDatasources, serverDsCombo);
    }
    
    private static void populateDataSources(final Set<Datasource> datasources, final JComboBox comboBox) {
        assert datasources != null && comboBox != null;
        
        List<Datasource> sortedDatasources = new ArrayList<Datasource>(datasources);
        Collections.sort(sortedDatasources, DatasourceUIHelper.createDatasourceComparator());
        
        comboBox.removeAllItems();
        for (Datasource ds : sortedDatasources) {
            comboBox.addItem(ds);
        }
    }
    
    private void setupErrorLabel() {
        setError(null);
        errorLabel.setForeground(getErrorColor());
    }
    
    private Color getErrorColor() {
        Color errorColor = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (errorColor != null) {
            return errorColor;
        }
        return new Color(255, 0, 0);
    }
    
    private void setupWarningLabel() {
        if (!isDsApiSupportedByServerPlugin) {
            // DS API is not supported by the server plugin
            warningLabel.setForeground(getWarningColor());
            return;
        }
        warningLabel.setText("");
    }
    
    private Color getWarningColor() {
        Color warningColor = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (warningColor != null) {
            return warningColor;
        }
        return Color.DARK_GRAY;
    }
    
    private void setupAddButton() {
        if (!isDsApiSupportedByServerPlugin) {
            addButton.setEnabled(false);
        }
    }
    
    public void verify() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // reference name
        String refName = dsReferenceText.getText();
        if (refName == null || refName.trim().length() == 0) {
            setError("ERR_NO_REFNAME"); // NOI18N
            return false;
        } else {
            refName = refName.trim();
            if (refNames.contains(refName)) {
                setError("ERR_DUPLICATE_REFNAME"); // NOI18N
                return false;
            }
        }
        
        // data sources (radio + combo)
        if (dsGroup.getSelection() == null) {
            setError("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
            return false;
        } else if (projectDsRadio.isSelected()) {
            if (projectDsCombo.getItemCount() == 0
                    || projectDsCombo.getSelectedIndex() == -1) {
                setError("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
                return false;
            }
        } else if (serverDsRadio.isSelected()) {
            if (serverDsCombo.getItemCount() == 0
                    || serverDsCombo.getSelectedIndex() == -1) {
                setError("ERR_NO_DATASOURCE_SELECTED"); // NOI18N
                return false;
            }
        }
        
        // no errors
        setError(null);
        return true;
    }
    
    private void setError(String key) {
        if (key == null) {
            errorLabel.setText("");
            return;
        }
        errorLabel.setText(NbBundle.getMessage(DataSourceReferencePanel.class, key));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        dsGroup = new javax.swing.ButtonGroup();
        dsReferenceLabel = new javax.swing.JLabel();
        dsReferenceText = new javax.swing.JTextField();
        projectDsRadio = new javax.swing.JRadioButton();
        serverDsRadio = new javax.swing.JRadioButton();
        projectDsCombo = new javax.swing.JComboBox();
        serverDsCombo = new javax.swing.JComboBox();
        dsCopyToProjectCheck = new javax.swing.JCheckBox();
        addButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        warningLabel = new javax.swing.JLabel();

        dsReferenceLabel.setLabelFor(dsReferenceText);
        org.openide.awt.Mnemonics.setLocalizedText(dsReferenceLabel, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DsReferenceName")); // NOI18N

        dsGroup.add(projectDsRadio);
        projectDsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDsRadio, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_ProjectDs")); // NOI18N
        projectDsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectDsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        dsGroup.add(serverDsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDsRadio, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_ServerDs")); // NOI18N
        serverDsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverDsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(dsCopyToProjectCheck, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DsCopyToProject")); // NOI18N
        dsCopyToProjectCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dsCopyToProjectCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "ERR_NO_REFNAME")); // NOI18N
        errorLabel.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, org.openide.util.NbBundle.getMessage(DataSourceReferencePanel.class, "LBL_DSC_Warning")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(dsCopyToProjectCheck, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE))
                            .add(serverDsRadio)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(29, 29, 29)
                                .add(warningLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(projectDsRadio)
                                    .add(dsReferenceLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, serverDsCombo, 0, 331, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, dsReferenceText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, projectDsCombo, 0, 331, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dsReferenceLabel)
                    .add(dsReferenceText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectDsRadio)
                    .add(projectDsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(addButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warningLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverDsRadio)
                    .add(serverDsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dsCopyToProjectCheck)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Datasource datasource = handleDataSourceCustomizer();
        if (datasource != null) {
            moduleDatasources.add(datasource);
            populateDataSources(moduleDatasources, projectDsCombo);
            projectDsCombo.setSelectedItem(datasource);
        }
}//GEN-LAST:event_addButtonActionPerformed
    
    private Datasource handleDataSourceCustomizer() {
        
        Datasource datasource = null;
        Set<Datasource> datasources = new HashSet<Datasource>(moduleDatasources);
        datasources.addAll(serverDatasources);
        DatasourceComboBoxCustomizer dsc = new DatasourceComboBoxCustomizer(datasources);
        if (dsc.showDialog()) {
            datasource = createDataSource(dsc);
        }
        
        return datasource;
    }
    
    private Datasource createDataSource(DatasourceComboBoxCustomizer dsc) {
        // if provider is able to create it, we will use it
        if (isDsApiSupportedByServerPlugin) {
            return createServerDataSource(dsc);
        }
        return createProjectDataSource(dsc);
    }
    
    private Datasource createServerDataSource(DatasourceComboBoxCustomizer dsc) {
        final Datasource[] ds = new Datasource[1];

        // creating datasources asynchronously
        final String password = dsc.getPassword();
        final String jndiName = dsc.getJndiName();
        final String url = dsc.getUrl();
        final String username = dsc.getUsername();
        final String driverClassName = dsc.getDriverClassName();
        
        Action action = new AsynchronousAction() {
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(DatasourceUIHelper.class, "MSG_creatingDS");
                actionContext.getProgress().progress(msg);
                try {
                    ds[0] = provider.createDatasource(jndiName, url, username, password, driverClassName);
                } catch (DatasourceAlreadyExistsException daee) {
                    // it should not occur bcs it should be already handled in DatasourceCustomizer
                    StringBuilder sb = new StringBuilder();
                    for (Object conflict : daee.getDatasources()) {
                        sb.append(conflict.toString() + "\n"); // NOI18N
                    }
                    ErrorManager.getDefault().annotate(daee, NbBundle.getMessage(DatasourceUIHelper.class, "ERR_DsConflict", sb.toString()));
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, daee);
                } catch (ConfigurationException ce) {
                    ErrorManager.getDefault().notify(ce);
                }
            }

            public boolean isEnabled() {
                return password != null;
            }
        };
        
        // invoke action
        Collection<Action> actions = Collections.singleton(action);
        EventRequestProcessor eventRequestProcessor = new EventRequestProcessor();
        eventRequestProcessor.invoke(actions);
        
        return ds[0];
    }
    
    private Datasource createProjectDataSource(DatasourceComboBoxCustomizer dsc) {
        return new DatasourceImpl(
                        dsc.getJndiName(),
                        dsc.getUrl(),
                        dsc.getUsername(),
                        dsc.getPassword(),
                        dsc.getDriverClassName());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JCheckBox dsCopyToProjectCheck;
    private javax.swing.ButtonGroup dsGroup;
    private javax.swing.JLabel dsReferenceLabel;
    private javax.swing.JTextField dsReferenceText;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JComboBox projectDsCombo;
    private javax.swing.JRadioButton projectDsRadio;
    private javax.swing.JComboBox serverDsCombo;
    private javax.swing.JRadioButton serverDsRadio;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
    
    private static class DatasourceImpl implements Datasource {
        
        private final String jndiName;
        private final String url;
        private final String username;
        private final String password;
        private final String driverClassName;
        private String displayName;
        
        public DatasourceImpl(String jndiName, String url, String username, String password, String driverClassName) {
            this.jndiName = jndiName;
            this.url = url;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }
        
        public String getJndiName() {
            return jndiName;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getDriverClassName() {
            return driverClassName;
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = getJndiName() + " [" + getUrl() + "]"; // NOI18N
            }
            return displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }
}
