/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.wizard;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Insets;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.PropertyPanel;

import org.openide.DialogDescriptor;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;


/**
 * <code>WizardDescriptor.Panel</code> used for to show found hard coded strings
 * for sepcified sources. It offers default key-value pairs and allows modify them.
 * These values will be used by actual i18n-zation of those sources.
 * It is the fourth and last panel of I18N Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
public class HardStringWizardPanel extends JPanel {

    /** Local copy of i18n wizard data. */
    private final Map sourceMap = new TreeMap(new SourceData.DataObjectComparator());

    /** Table model for <code>stringTable</code>. */
    private final AbstractTableModel tableModel = new HardCodedStringTableModel();
    
    
    /** Creates new form HardCodedStringsPanel */
    private HardStringWizardPanel() {
        initComponents();

        setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
        
        initTable();
        
        sourceCombo.setModel(new DefaultComboBoxModel(sourceMap.keySet().toArray()));
    }

    
    /** Getter for <code>resources</code> property. */
    public Map getSourceMap() {
        return sourceMap;
    } 
    
    /** Setter for <code>resources</code> property. */
    public void setSourceMap(Map sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        sourceCombo.setModel(new DefaultComboBoxModel(sourceMap.keySet().toArray()));
    }
    
    /** Gets string map for specified source data object. Utility method. */
    private Map getStringMap() {
        SourceData sourceData = (SourceData)sourceMap.get(sourceCombo.getSelectedItem());
        return sourceData == null ? null : sourceData.getStringMap();
    }

    /** Inits table component. */
    private void initTable() {
        hardStringTable.setDefaultRenderer(HardCodedString.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                HardCodedString hcString = (HardCodedString)value;

                if(hcString != null)
                    label.setText(hcString.getText());
                else
                    label.setText(""); // NOI18N

                return label;
            }
        });
        
        hardStringTable.setDefaultRenderer(I18nString.class, new DefaultTableCellRenderer() {
            private final JButton dotButton = new JButton("..."); // NOI18N
            
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

                I18nString i18nString = (I18nString)value;

                if(column == 3)
                    return dotButton;
                    
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if(i18nString != null) {
                    if(column == 1) {
                        label.setText(i18nString.getKey());
                    } else {
                        label.setText(i18nString.getValue());
                    }

                } else
                    label.setText(""); // NOI18N
                    
                
                return label;
            }
        });

        hardStringTable.setDefaultEditor(I18nString.class, new DefaultCellEditor(new JTextField()) {
            
            public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected,
                int row, int column) {

                I18nString i18nString = (I18nString)value;
                
                if(column == 1)
                    value = i18nString == null ? "" : i18nString.getKey(); // NOI18N
                else if(column == 2)
                    value = i18nString == null ? "" : i18nString.getValue(); // NOI18N
                else
                    value = ""; // NOI18N
                
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        });
        
        hardStringTable.getColumnModel().getColumn(3).setCellEditor(new CustomizeCellEditor());

        // PENDING: Setting the size of column with customize button editor.
        hardStringTable.getColumnModel().getColumn(3).setMaxWidth(30);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        sourceLabel = new javax.swing.JLabel();
        sourceCombo = new javax.swing.JComboBox();
        hardStringLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        hardStringTable = new javax.swing.JTable();
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        sourceLabel.setText(NbBundle.getBundle(HardStringWizardPanel.class).getString("LBL_Source"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceLabel, gridBagConstraints1);
        
        
        sourceCombo.setRenderer(new SourceWizardPanel.DataObjectListCellRenderer());
        sourceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceComboActionPerformed(evt);
            }
        }
        );
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        add(sourceCombo, gridBagConstraints1);
        
        
        hardStringLabel.setText(NbBundle.getBundle(HardStringWizardPanel.class).getString("LBL_FoundStrings"));
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(11, 0, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(hardStringLabel, gridBagConstraints1);
        
        
        jScrollPane2.setPreferredSize(new java.awt.Dimension(100, 100));
        
        hardStringTable.setModel(tableModel);
        jScrollPane2.setViewportView(hardStringTable);
        
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jScrollPane2, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void sourceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceComboActionPerformed
        tableModel.fireTableDataChanged();
    }//GEN-LAST:event_sourceComboActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JComboBox sourceCombo;
    private javax.swing.JLabel hardStringLabel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable hardStringTable;
    // End of variables declaration//GEN-END:variables

    /** Table model for this class. */
    private class HardCodedStringTableModel extends AbstractTableModel {
        
        /** Constructor. */
        public HardCodedStringTableModel() {
        }
        
        
        /** Implements superclass abstract method. */
        public int getColumnCount() {
            return 4;
        }
        
        /** Implemenst superclass abstract method. */
        public int getRowCount() {
            Map stringMap = getStringMap();
            return stringMap == null ? 0 : stringMap.size();
        }
        
        /** Implements superclass abstract method. */
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map stringMap = getStringMap();
            
            if(stringMap == null)
                return null; // NOI18N
            
            if(columnIndex == 0) {
                return stringMap.keySet().toArray()[rowIndex];
            } else {
                return stringMap.values().toArray()[rowIndex];
            }
        }
        
        /** Overrides superclass method.
         * @ return true for all columns but first */
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if(columnIndex > 0)
                return true;
            else
                return false;
        }
        
        /** Overrides superclass method. */
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if(columnIndex > 0) {
                I18nString i18nString = (I18nString)getStringMap().values().toArray()[rowIndex];
                
                if(columnIndex == 1)
                    i18nString.setKey(value.toString());
                else if(columnIndex == 2)
                    i18nString.setValue(value.toString());
            }
        }
        
        /** Overrides superclass method. 
         * @return DataObject.class */
        public Class getColumnClass(int columnIndex) {
            if(columnIndex == 0)
                return HardCodedString.class;
            else
                return I18nString.class;
        }

        /** Overrides superclass method. */
        public String getColumnName(int column) {
            if(column == 0)
                return NbBundle.getBundle(getClass()).getString("LBL_HardString");
            else if(column == 1)
                return NbBundle.getBundle(getClass()).getString("LBL_Key");
            else if(column == 2)
                return NbBundle.getBundle(getClass()).getString("LBL_Value");
            else 
                return ""; // NOI18N
        }
    } // End of ResourceTableModel nested class.


    /** Cell editor for the right most 'customize' column. It shows dialog 
     * constructed from <code>PropertyPanel</code> which provides actual custmization of the 
     * <code>I18nString</code> instance.
     * @see org.netbeans.modules.i18n.PropertyPanel
     */
    public static class CustomizeCellEditor extends AbstractCellEditor 
    implements TableCellEditor, ActionListener {

        /** <code>I18nString</code> instance to be edited by this editor. */
        private I18nString i18nString;
        
        /** Editor component, in our case <code>JButton</code>. */
        private JButton editorComponent;

        
        /** Constructor. */
        public CustomizeCellEditor() {
            editorComponent = new JButton("..."); // NOI18N
            
            editorComponent.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    PropertyPanel panel = new PropertyPanel();
                    panel.setI18nString(i18nString);

                    DialogDescriptor dd = new DialogDescriptor(panel,"Customize Property");
                    dd.setModal(true);
                    dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
                    dd.setOptions(new Object[] {DialogDescriptor.OK_OPTION});
                    dd.setAdditionalOptions(new Object[0]);
                    dd.setButtonListener(CustomizeCellEditor.this);

                    Dialog dialog = TopManager.getDefault().createDialog(dd);
                    dialog.setVisible(true);
                }
            });
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            i18nString = (I18nString)value;
            
            return editorComponent;
        }
        
        /** Implements <code>TableCellEditor</code> interface. */
        public Object getCellEditorValue() {
            return i18nString;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public boolean isCellEditable(EventObject anEvent) { 
            if(anEvent instanceof MouseEvent) { 
                // Counts needed to start editing.
                return ((MouseEvent)anEvent).getClickCount() >= 1;
            }
            return true;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public boolean shouldSelectCell(EventObject anEvent) { 
            return true; 
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public boolean stopCellEditing() {
            fireEditingStopped(); 
            return true;
        }

        /** Implements <code>TableCellEditor</code> interface. */
        public void cancelCellEditing() {
           fireEditingCanceled(); 
        }
        
        /** Implements <code>ActionListener</code> interface. */
        public void actionPerformed(ActionEvent evt) {
            stopCellEditing();
        }

    }
    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>HardCodedStringPanel</code>. 
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel*/
    public static class Panel extends I18nWizardDescriptor.Panel 
    implements WizardDescriptor.FinishPanel, I18nWizardDescriptor.ProgressMonitor {

        /** */
        private final HardStringWizardPanel hardStringPanel = new HardStringWizardPanel();


        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {
            JPanel panel = new JPanel();
            
            Integer index;
            
            if(I18nUtil.getOptions().isAdvancedWizard())
                index = new Integer(3);
            else
                index = new Integer(2);
            
            panel.putClientProperty("WizardPanel_contentSelectedIndex", index); // NOI18N
            panel.setName(NbBundle.getBundle(getClass()).getString("TXT_ModifyStrings"));
            
            panel.add(hardStringPanel, BorderLayout.CENTER);
            
            return panel;
        }

        /** Gets if panel is valid. Overrides superclass method. */
        public boolean isValid() {
            return true;
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        public void readSettings(Object settings) {
            hardStringPanel.setSourceMap((Map)settings);
        }

        /** Stores settings at the end of panel show. Overrides superclass method. */
        public void storeSettings(Object settings) {
            // Update sources.
            ((Map)settings).clear();
            ((Map)settings).putAll(hardStringPanel.getSourceMap());
        }
        
        /** Searches hard coded strings in sources and puts found hard coded string - i18n string pairs
         * into settings. Implements <code>ProgressMonitor</code> interface method. */
        public void doLongTimeChanges() {
            // Replace panel.
            final ProgressWizardPanel progressPanel = new ProgressWizardPanel(true);
            progressPanel.setMainText(NbBundle.getBundle(getClass()).getString("LBL_Internationalizing"));
            progressPanel.setMainProgress(0);
            
            ((Container)getComponent()).remove(hardStringPanel);
            ((Container)getComponent()).add(progressPanel, BorderLayout.CENTER);
            ((JComponent)getComponent()).revalidate();
            getComponent().repaint();

            // Do replacement job here.
            Map sourceMap = hardStringPanel.getSourceMap();

            Iterator sourceIterator = sourceMap.keySet().iterator();

            // For each source perform the task.
            for(int i=0; sourceIterator.hasNext(); i++) {
                Object source = sourceIterator.next();

                // Get source data.
                SourceData sourceData = (SourceData)sourceMap.get(source);

                // Get i18n support for this source.
                I18nSupport support = sourceData.getSupport();

                // Get string map.
                Map stringMap = sourceData.getStringMap();

                // Do actual replacement.
                Iterator it = stringMap.keySet().iterator();

                progressPanel.setSubText(NbBundle.getBundle(getClass()).getString("LBL_Source")+" "+((DataObject)source).getPrimaryFile().getPackageName('.'));

                for(int j=0; it.hasNext(); j++) {
                    HardCodedString hcString = (HardCodedString)it.next();
                    I18nString i18nString = (I18nString)stringMap.get(hcString);

                    // Put new property into bundle.
                    support.getResourceHolder().addProperty(i18nString.getKey(), i18nString.getValue(), i18nString.getComment());

                    // Replace string in source.
                    support.getReplacer().replace(hcString, i18nString);

                    progressPanel.setSubProgress((int)((j+1)/(float)stringMap.size() * 100));
                } // End of inner for.
                
                // Provide additional changes if there are some.
                if(support.hasAdditionalCustomizer()) {
                    support.performAdditionalChanges();
                }

                progressPanel.setMainProgress((int)((i+1)/(float)sourceMap.size() * 100));
            } // End of outer for.
        }
        
        /** Implements <code>ProgressMonitor</code> interface method. Does nothing. */
        public void reset() {
        }
        
        
    } // End of nested PanelDescriptor class.
}
