/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;

public class PanelSupportedFrameworksVisual extends JPanel implements HelpCtx.Provider, TableModelListener, ListSelectionListener, ChangeListener {
    
    /** All available web extensions */
    public static int ALL_FRAMEWORKS = 0;
    
    /** Web extensions used in the project */
    public static int USED_FRAMEWORKS = 1;
    
    /** Web extensions which are not used in the project */
    public static int UNUSED_FRAMEWORKS = 2;
    
    private List ignoredFrameworks;
    private HashMap configPanels = new HashMap();
    private FrameworksTableModel model;
    private PanelSupportedFrameworks panel;
    
    /** Creates new form PanelInitProject
     * @param project the web project; if it is null, all available web extensions will be shown
     * @param filter one of the options <code>ALL_FRAMEWORKS</code>, <code>USED_FRAMEWORKS</code>, <code>UNUSED_FRAMEWORKS</code>
     * @param ignoredFrameworks the list of frameworks to be ignored when creating list; null is allowed
     */
    public PanelSupportedFrameworksVisual(PanelSupportedFrameworks panel, WebProject project, int filter, List ignoredFrameworks) {
        this.panel = panel;
        this.ignoredFrameworks = ignoredFrameworks;
        initComponents();

        model = new FrameworksTableModel();
        jTableFrameworks.setModel(model);
        createFrameworksList(project, filter);

        FrameworksTableCellRenderer renderer = new FrameworksTableCellRenderer();
        renderer.setBooleanRenderer(jTableFrameworks.getDefaultRenderer(Boolean.class));
        jTableFrameworks.setDefaultRenderer(WebFrameworkProvider.class, renderer);
        jTableFrameworks.setDefaultRenderer(Boolean.class, renderer);
        initTableVisualProperties(jTableFrameworks);

        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "ACS_NWP2_Frameworks_A11YDesc"));  // NOI18N        

        // Provide a name in the title bar.
        setName(NbBundle.getMessage(PanelConfigureProjectVisual.class, "LBL_NWP2_Frameworks")); //NOI18N
        putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelConfigureProjectVisual.class, "TXT_NewWebApp")); //NOI18N
    }

    private void initTableVisualProperties(JTable table) {
        table.getModel().addTableModelListener(this);
        
        table.setRowSelectionAllowed(true);
        table.getSelectionModel().addListSelectionListener(this);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.setTableHeader(null);
        
        table.setRowHeight(jTableFrameworks.getRowHeight() + 4);        
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));        
        // set the color of the table's JViewport
        table.getParent().setBackground(table.getBackground());
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        
        table.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private void createFrameworksList(WebProject project, int filter) {
        List frameworks = WebFrameworkSupport.getFrameworkProviders();
        configPanels.clear();
        
        if (project == null || filter == ALL_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                addFrameworkToModel((WebFrameworkProvider) frameworks.get(i));
                configPanels.put((WebFrameworkProvider) frameworks.get(i), ((WebFrameworkProvider) frameworks.get(i)).getConfigurationPanel(null));
            }
        } else if (project != null && filter == USED_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                if (framework.isInWebModule(project.getAPIWebModule())) {
                    addFrameworkToModel(framework);
                    configPanels.put(framework, framework.getConfigurationPanel(null));
                }
            }
        } else if (project != null && filter == UNUSED_FRAMEWORKS) {
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                if (!framework.isInWebModule(project.getAPIWebModule())) {
                    addFrameworkToModel(framework);
                    configPanels.put(framework, framework.getConfigurationPanel(null));
                }
            }
        }
        
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            FrameworkConfigurationPanel configPanel = (FrameworkConfigurationPanel) configPanels.get(item.getFramework());
            if (configPanel != null)
                configPanel.addChangeListener(this);
        }
    }
    
    private void addFrameworkToModel(WebFrameworkProvider framework) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        if (ignoredFrameworks == null || !ignoredFrameworks.contains(framework))
            model.addItem(new FrameworkModelItem(framework));
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFrameworks = new javax.swing.JTable();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelConfig = new javax.swing.JLabel();
        jPanelConfig = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 340));
        setRequestFocusEnabled(false);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "LBL_NWP2_Frameworks"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(32767, 70));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(22, 70));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(453, 70));
        jTableFrameworks.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableFrameworks);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jSeparator1, gridBagConstraints);

        jLabelConfig.setLabelFor(jPanelConfig);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(jLabelConfig, gridBagConstraints);

        jPanelConfig.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelConfig, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JPanel jPanelConfig;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTableFrameworks;
    // End of variables declaration//GEN-END:variables

    boolean valid(WizardDescriptor wizardDescriptor) {
        boolean retVal = true;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getItem(i).isSelected().booleanValue()) {
                retVal = false;
                break;
            }
        }
        //none framework selected, enable Finish
        if (retVal)
            return true;
        
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            FrameworkConfigurationPanel configPanel = (FrameworkConfigurationPanel) configPanels.get(item.getFramework());
            if (configPanel != null && !configPanel.isValid())
                return false;
        }
        
        return true;
    }

    void validate (WizardDescriptor settings) throws WizardValidationException {
//        projectLocationPanel.validate (d);
    }
    
    void read (WizardDescriptor settings) {
//        if ( bottomPanel != null ) {
//            bottomPanel.readSettings( settings );
//        }        
    }

    void store(WizardDescriptor settings) {
//        if ( bottomPanel != null ) {
//            bottomPanel.storeSettings( settings );
//        }
        
        settings.putProperty(WizardProperties.FRAMEWORKS, getSelectedFrameworks());    //NOI18N
    }

    public List getSelectedFrameworks() {
        List selectedFrameworks = new LinkedList();
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected() == Boolean.TRUE)
                selectedFrameworks.add(item.getFramework());
        }
        
        return selectedFrameworks;
    }
    
    public Component[] getConfigComponents() {
        return new Component[] {jSeparator1, jLabelConfig, jPanelConfig};
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PanelSupportedFrameworksVisual.class);
    }
    
    public void tableChanged(TableModelEvent e) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        FrameworkModelItem item = model.getItem(jTableFrameworks.getSelectedRow());
        WebFrameworkProvider framework = item.getFramework();
        setConfigPanel(framework, item);
    }
    
    public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        FrameworksTableModel model = (FrameworksTableModel) jTableFrameworks.getModel();
        FrameworkModelItem item = model.getItem(jTableFrameworks.getSelectedRow());
        WebFrameworkProvider framework = item.getFramework();
        setConfigPanel(framework, item);
    }
    
    private void setConfigPanel(WebFrameworkProvider framework, FrameworkModelItem item) {
        if (configPanels.get(framework) != null) {
            String message = MessageFormat.format(NbBundle.getMessage(PanelSupportedFrameworksVisual.class, "LBL_NWP2_ConfigureFramework"), new Object[] {framework.getName()}); //NOI18N
            jLabelConfig.setText(message);
//            jLabelConfig.setEnabled(item.isSelected().booleanValue());
            
            jPanelConfig.removeAll();

            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;

            jPanelConfig.add(((FrameworkConfigurationPanel) configPanels.get(framework)).getComponent(), gridBagConstraints);
            
            ((FrameworkConfigurationPanel) configPanels.get(framework)).enableComponents(item.isSelected().booleanValue());
            
            jPanelConfig.revalidate();
        } else {
            jLabelConfig.setText(""); //NOI18N
            jPanelConfig.removeAll();
            jPanelConfig.repaint();
            jPanelConfig.revalidate();
        }
        
        if (panel != null)
            panel.fireChangeEvent();
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if (panel != null)
            panel.fireChangeEvent();
    }
    
    public static class FrameworksTableCellRenderer extends DefaultTableCellRenderer {
        private TableCellRenderer booleanRenderer;
        
        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            if (value instanceof WebFrameworkProvider) {
                WebFrameworkProvider item = (WebFrameworkProvider) value;
                return super.getTableCellRendererComponent(table, item.getName(), isSelected, false, row, column);
            } else {
                if (value instanceof Boolean && booleanRenderer != null)
                    return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                else
                    return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        }
        
        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    /** 
     * Implements a TableModel.
     */
    public static final class FrameworksTableModel extends AbstractTableModel {
        private DefaultListModel model;
        
        public FrameworksTableModel() {
            model = new DefaultListModel();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public int getRowCount() {
            return model.size();
        }
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return Boolean.class;
            else
                return WebFrameworkProvider.class;
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 0);
        }
        
        public Object getValueAt(int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0: return item.isSelected();
                case 1: return item.getFramework();
            }
            return "";
        }
        
        public void setValueAt(Object value, int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0: item.setSelected((Boolean) value);break;
                case 1: item.setFramework((WebFrameworkProvider) value);break;
            }
            fireTableCellUpdated(row, column);
        }
        
        private FrameworkModelItem getItem(int index) {
            return (FrameworkModelItem) model.get(index);
        }
        
        public void addItem(FrameworkModelItem item){
            model.addElement(item);
        }
    }

    private final class FrameworkModelItem {
        private WebFrameworkProvider framework;
        private Boolean selected;
        
        /** Creates a new instance of BeanFormProperty */
        public FrameworkModelItem(WebFrameworkProvider framework) {
            this.setFramework(framework);
            setSelected(Boolean.FALSE);
        }

        public WebFrameworkProvider getFramework() {
            return framework;
        }

        public void setFramework(WebFrameworkProvider framework) {
            this.framework = framework;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

    }
}
