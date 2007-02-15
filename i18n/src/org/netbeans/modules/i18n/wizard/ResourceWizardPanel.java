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


package org.netbeans.modules.i18n.wizard;


import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.SelectorUtils;
import org.netbeans.modules.properties.UtilConvert; // PENDING

import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;


/**
 * Second panel of I18N Wizard.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see Panel
 */
final class ResourceWizardPanel extends JPanel {

    /** Local copy of i18n wizard data. */
    private final Map sourceMap = Util.createWizardSourceMap();

    /** Table model for resourcesTable. */
    private final ResourceTableModel tableModel = new ResourceTableModel();

    /** This component panel wizard descriptor.
     * @see org.openide.WizardDescriptor.Panel 
     * @see Panel */
    private final Panel descPanel;

    private final boolean testMode;
    
    /** Creates new form SourceChooserPanel. */
    private ResourceWizardPanel(Panel descPanel, boolean testMode) {
        this.descPanel = descPanel;
        this.testMode = testMode;
        
        initComponents();        
        
        initTable();
        
        initAccesibility();
    }

    
    /** Getter for <code>resources</code> property. */
    public Map getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>resources</code> property. */
    public void setSourceMap(Map sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        tableModel.fireTableDataChanged();
       
        descPanel.fireStateChanged();
    }
    
    private String getPanelDescription() {
        if (testMode == false) {
            return Util.getString("MSG_ResourcePanel_desc");
        } else {
            return Util.getString("MSG_ResourcePanel_test_desc");
        }
    }
    
    /** Inits table component. */
    private void initTable() {
        resourcesTable.setDefaultRenderer(DataObject.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                DataObject dataObject = (DataObject)value;

                if(dataObject != null) {                    
                    String name = "N/A";
                    if (column == 0) {
                        // name for the first column, from sources
                        ClassPath cp = ClassPath.getClassPath(dataObject.getPrimaryFile(), ClassPath.SOURCE );                    
                        name = cp.getResourceName( dataObject.getPrimaryFile(), '.', false );
                    } else {
                        // name for resource bundle, from execution,
                        // but the reference file must be the
                        // corresponding source
                        DataObject dob = (DataObject)tableModel.getValueAt(row, 0);
                        name = Util.getResourceName(dob.getPrimaryFile(), dataObject.getPrimaryFile(), '.', false);
                    }

                    label.setText(name); // NOI18N
                    label.setIcon(new ImageIcon(dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
                } else {
                    label.setText(""); // NOI18N
                    label.setIcon(null);
                }
                
                return label;
            }
        });

        Component cellSample = new DefaultTableCellRenderer()
                               .getTableCellRendererComponent(
                                    resourcesTable, //table
                                    "N/A",          //value             //NOI18N
                                    false,          //isSelected
                                    false,          //hasFocus
                                    0, 0);          //row, column
        int cellHeight = cellSample.getPreferredSize().height;
        int rowHeight = cellHeight + resourcesTable.getRowMargin();
        resourcesTable.setRowHeight(Math.max(16, rowHeight));

        resourcesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                addButton.setEnabled(!resourcesTable.getSelectionModel().isSelectionEmpty());
            }
        });
        
        addButton.setEnabled(!resourcesTable.getSelectionModel().isSelectionEmpty());
    }
    
    
    private void initAccesibility() {        
        addButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACS_CTL_SelectResource"));
        addAllButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACS_CTL_SelectResourceAll"));
        resourcesTable.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACSD_resourcesTable"));
        resourcesTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACSN_resourcesTable"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descTextArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        resourcesTable = new javax.swing.JTable();
        addAllButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        descTextArea.setColumns(20);
        descTextArea.setEditable(false);
        descTextArea.setLineWrap(true);
        descTextArea.setText(getPanelDescription());
        descTextArea.setWrapStyleWord(true);
        descTextArea.setDisabledTextColor(new JLabel().getForeground());
        descTextArea.setEnabled(false);
        descTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(descTextArea, gridBagConstraints);

        resourcesTable.setModel(tableModel);
        jScrollPane1.setViewportView(resourcesTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, NbBundle.getBundle(ResourceWizardPanel.class).getString("CTL_SelectResourceAll")); // NOI18N
        addAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(addAllButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, NbBundle.getBundle(ResourceWizardPanel.class).getString("CTL_SelectResource")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(addButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        DataObject resource = selectResource();
        
        if(resource == null)
            return;

        int[] selectedRows = resourcesTable.getSelectedRows();

        // Feed data.
        for(int i=0; i<selectedRows.length; i++) {
            DataObject dataObject = (DataObject)resourcesTable.getValueAt(selectedRows[i], 0);

            sourceMap.put(dataObject, new SourceData(resource));
            
            tableModel.fireTableCellUpdated(selectedRows[i], 1);
        }

        descPanel.fireStateChanged();
    }//GEN-LAST:event_addButtonActionPerformed

    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        DataObject resource = selectResource();
        
        if(resource == null)
            return;

        // Feed data.
        for(int i=0; i<resourcesTable.getRowCount(); i++) {
            DataObject dataObject = (DataObject)resourcesTable.getValueAt(i, 0);

            sourceMap.put(dataObject, new SourceData(resource));
            
            tableModel.fireTableCellUpdated(i, 1);
        }

        descPanel.fireStateChanged();
    }//GEN-LAST:event_addAllButtonActionPerformed

    /** Helper method. Gets user selected resource. */
    private DataObject selectResource() {
        Project prj = null;
        FileObject fo = null;
        Iterator it = sourceMap.keySet().iterator();
        if (it.hasNext()) {
            DataObject dobj = (DataObject) it.next();
            fo = dobj.getPrimaryFile();
            prj = FileOwnerQuery.getOwner(fo);
        }

	return SelectorUtils.selectBundle(prj, fo);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton addButton;
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable resourcesTable;
    // End of variables declaration//GEN-END:variables

    /** Table model for this class. */
    private class ResourceTableModel extends AbstractTableModel {
        
        /** Constructor. */
        public ResourceTableModel() {
        }
        
        
        /** Implements superclass abstract method. */
        public int getColumnCount() {
            return 2;
        }
        
        /** Implemenst superclass abstract method. */
        public int getRowCount() {
            return sourceMap.size();
        }
        
        /** Implements superclass abstract method. */
        public Object getValueAt(int rowIndex, int columnIndex) {

            if(columnIndex == 0) {
                return sourceMap.keySet().toArray()[rowIndex];
            } else { 
                SourceData value = (SourceData)sourceMap.values().toArray()[rowIndex];
                return value == null ? null : value.getResource();
            }
            
        }
        
        /** Overrides superclass method. 
         * @return DataObject.class */
        public Class getColumnClass(int columnIndex) {
            return DataObject.class;
        }

        /** Overrides superclass method. */
        public String getColumnName(int column) {
            if(column == 0)
                return NbBundle.getBundle(ResourceWizardPanel.class).getString("CTL_Source");
            else
                return NbBundle.getBundle(ResourceWizardPanel.class).getString("CTL_Resource");
        }
    } // End of ResourceTableModel inner class.
    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>ResourceChooserPanel</code>. 
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel */
    public static class Panel extends I18nWizardDescriptor.Panel implements I18nWizardDescriptor.ProgressMonitor {

        /** Cached component. */
        private transient ResourceWizardPanel resourcePanel;
        
        /** Indicates whether this panel is used in i18n test wizard or not. */
        private boolean testWizard;


        /** Constructs Panel for i18n wizard. */
        public Panel() {
            this(false);
        }

        /** Constructs panel for i18n wizard or i18n test wizard. */
        public Panel(boolean testWizard) {
            this.testWizard = testWizard;
        }
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {
            JPanel panel = new JPanel();

            // Accessibility
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ResourceWizardPanel.class).getString("ACS_ResourceWizardPanel"));                 
            
            panel.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); // NOI18N
            if(testWizard)
                panel.setName(NbBundle.getBundle(ResourceWizardPanel.class).getString("TXT_SelectTestResource"));
            else
                panel.setName(NbBundle.getBundle(ResourceWizardPanel.class).getString("TXT_SelectResource"));

            panel.setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
            
            panel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            panel.add(getUI(), constraints);            
            
            return panel;
        }

        /** Indicates if panel is valid. Overrides superclass method. */
        public boolean isValid() {
            return !getUI().getSourceMap().containsValue(null);
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        public void readSettings(Object settings) {
	    super.readSettings(settings);
            getUI().setSourceMap(getMap());
        }

        /** Stores settings at the end of panel show. Overrides superclass abstract method. */
        public void storeSettings(Object settings) {
	    super.storeSettings(settings);
            // Update sources.
            getMap().clear();
            getMap().putAll(getUI().getSourceMap());
        }
        
        /** Searches hard coded strings in sources and puts found hard coded string - i18n string pairs
         * into settings. Implements <code>ProgressMonitor</code> interface. */
        public void doLongTimeChanges() {
            // Replace panel.
            ProgressWizardPanel progressPanel = new ProgressWizardPanel(false);
            
            showProgressPanel(progressPanel);
            
            progressPanel.setMainText(NbBundle.getBundle(ResourceWizardPanel.class).getString("TXT_Loading"));
            progressPanel.setMainProgress(0);
            
            // Do search.
            Map sourceMap = getUI().getSourceMap();

            Iterator sourceIterator = sourceMap.keySet().iterator();

            // For each source perform the task.
            for(int i=0; sourceIterator.hasNext(); i++) {
                DataObject source = (DataObject)sourceIterator.next();

                ClassPath cp = ClassPath.getClassPath( source.getPrimaryFile(), ClassPath.SOURCE );                
                progressPanel.setMainText(NbBundle.getBundle(ResourceWizardPanel.class).getString("TXT_Loading") 
                    + " " + cp.getResourceName( source.getPrimaryFile(), '.', false )); // NOI18N


                // retrieve existing sourcedata -- will provide the resource for the new instance
                SourceData sourceData = (SourceData)sourceMap.get(source);
                
                // prepare new sourcedata
                // Get i18n support for this source.
                I18nSupport support;
                try {
                    support = FactoryRegistry.getFactory(source.getClass()).create(source);
                } catch(IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    // Remove source from settings.
                    sourceMap.remove(source);
                    continue;
                }
                sourceData = new SourceData(sourceData.getResource(), support);
                sourceMap.put(source, sourceData);
                
                cp = ClassPath.getClassPath( source.getPrimaryFile(), ClassPath.SOURCE );
                progressPanel.setMainText(NbBundle.getBundle(ResourceWizardPanel.class).getString("TXT_SearchingIn")
                    + " " + cp.getResourceName( source.getPrimaryFile(), '.', false ) ); // NOI18N
                
                // Get string map.
                Map stringMap = sourceData.getStringMap();

                HardCodedString[] foundStrings;
                
                if(testWizard) {
                    // Find all i18n-zied hard coded strings in the source.
                    foundStrings = support.getFinder().findAllI18nStrings();
                } else {
                    // Find all non-i18-ized hard coded strings in the source.
                    foundStrings = support.getFinder().findAllHardCodedStrings();
                }

                if(foundStrings == null) {
                    // Set empty map.
                    sourceData.setStringMap(new HashMap(0));
                    continue;
                }

                Map map = new HashMap(foundStrings.length); 

                // Put hard coded string - i18n pairs into map.
                for(int j=0; j<foundStrings.length; j++) {
                    if(testWizard && support.getResourceHolder().getValueForKey(UtilConvert.escapePropertiesSpecialChars(foundStrings[j].getText())) != null)
                        continue;
                        
                    map.put(foundStrings[j], support.getDefaultI18nString(foundStrings[j]));
                }

                progressPanel.setMainProgress((int)((i+1)/(float)sourceMap.size() * 100));

                sourceData.setStringMap(map);
            } // End of outer for.
        }
        
        /** Helper method. Places progress panel for monitoring search. */
        private void showProgressPanel(ProgressWizardPanel progressPanel) {
            ((Container)getComponent()).remove(getUI());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            ((Container)getComponent()).add(progressPanel, constraints);
            ((JComponent)getComponent()).revalidate();
            getComponent().repaint();
        }
        
        /** Resets panel back after monitoring search. Implements <code>ProgressMonitor</code> interface. */
        public void reset() {
            Container container = (Container)getComponent();
            
            if(!container.isAncestorOf(getUI())) {
                container.removeAll();
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.fill = GridBagConstraints.BOTH;
                container.add(getUI(), constraints);
            }
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            if(testWizard)
                return new HelpCtx(I18nUtil.HELP_ID_TESTING);
            else
                return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
        }
        
        private synchronized ResourceWizardPanel getUI() {
            if (resourcePanel == null) {
                resourcePanel = new ResourceWizardPanel(this, testWizard);
            }
            return resourcePanel;
        }

    } // End of nested Panel class.
    
}
