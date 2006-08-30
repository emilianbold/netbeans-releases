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
 * AbilitiesPanel.java
 *
 * Created on 19 May 2006, 17:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.project.ui;

import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.configurations.ProjectConfiguration;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.CustomizerAbilities;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 *
 * @author Lukas Waldmann
 */

public class AbilitiesPanel implements NavigatorPanel
{
    static class VAData implements NavigatorLookupHint
    {
        VAData()
        {
        }
        
        public String getContentType()
        {
            return "j2me/abilities";
        }
    }
    
    static class ABPanel extends JPanel
    {
        private static javax.swing.JScrollPane scrollPane;
        final private static EditableTableModel tableModel =  new EditableTableModel();
        final private static JTable table=new JTable(tableModel);
        private static ABPanel instance=null;
        private static VAData data = null;
        private static J2MEProjectProperties j2meProperties = null;
        private static VisualPropertySupport vps = null;
        private static J2MEProject project=null;
        private static ProjectConfiguration conf=null;
        
        private ABPanel()
        {
            initComponents();            
            scrollPane.setViewportView(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);                                    
            
            final TableCellEditor editor=table.getDefaultEditor(String.class);                        
            editor.addCellEditorListener(new CellEditorListener() {                    
                public void editingStopped(ChangeEvent e)
                {
                    final int row=table.getSelectedRow();
                    final int column=table.getSelectedColumn();
                    if (row < 0 || column <0 )
                        return;
                    final String key = tableModel.getValueAt(row, 0);
                    final String value = tableModel.getValueAt(row, 1);                    
                    final String newValue=(String)editor.getCellEditorValue();
                    if (!value.equals(newValue))         
                    {
                        tableModel.editRow(key,newValue);
                        j2meProperties.store();
                        // And save the project
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        }
                        catch ( IOException ex ) {
                            ErrorManager.getDefault().notify( ex );
                        }
                    }
                }

                public void editingCanceled(ChangeEvent e)
                {
                }               
            });
            
        }
        
        public static void setAbilities(final Lookup context)
        {
            if (context!=null)
            {
                data=context.lookup(VAData.class);
                if (data!=null)
                {
                    tableModel.removeListeners();
                    Node node=context.lookup(Node.class);
                    project=node.getLookup().lookup(J2MEProject.class);
                    conf=node.getLookup().lookup(ProjectConfiguration.class);
                    assert project != null;
                    j2meProperties = new J2MEProjectProperties( project,
                            project.getLookup().lookup(AntProjectHelper.class),
                            project.getLookup().lookup(ReferenceHelper.class),
                            project.getConfigurationHelper() );
                    
                    //Default configuration is a special case
                    final ProjectConfigurationsHelper pch=project.getLookup().lookup(ProjectConfigurationsHelper.class);
                    final ProjectConfiguration cf=conf.getName().equals(pch.getDefaultConfiguration().getName()) ? null : conf;
                    final boolean def=testForNonDefaultValue(j2meProperties,cf);
                    final boolean useDefault=def ^ true; //Negation
                    
                    tableModel.setEditable(def);
                    table.setBackground(javax.swing.UIManager.getDefaults().getColor(useDefault ? "Panel.background" : "Table.background")); //NOI18N

                    
                    final String pn=VisualPropertySupport.translatePropertyName(cf==null?null:cf.getName(),
                            DefaultPropertiesDescriptor.ABILITIES, useDefault);
                    
                    final Object values[]= new Object[] {j2meProperties.get(pn)};                    
                    tableModel.setDataDelegates(values);
                    
                    vps = VisualPropertySupport.getDefault(j2meProperties);                    
                    vps.register(tableModel,new String[] {pn},false);
                }
            }
        }
        
        private static boolean testForNonDefaultValue(final J2MEProjectProperties properties, final ProjectConfiguration conf)
        {
            if (conf == null || 
                    properties.get(VisualPropertySupport.prefixPropertyName(conf.getName(), DefaultPropertiesDescriptor.ABILITIES)) != null)
                return true;
            return false;
        }
        
        private void initComponents()
        {
            java.awt.GridBagConstraints gridBagConstraints;
            scrollPane = new javax.swing.JScrollPane();
            
            setLayout(new java.awt.GridBagLayout());
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.gridheight = 3;
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
            add(scrollPane, gridBagConstraints);
        }
        
        private static JComponent getInstance()
        {
            if (instance == null)
                instance = new ABPanel();
            return instance;
        }
    }
        
    public synchronized JComponent getComponent()
    {
        return ABPanel.getInstance();
    }
    
    public String getDisplayName()
    {
        return "Abilities";
    }
    
    public String getDisplayHint()
    {
        return "Abilities";
    }
    
    public synchronized void panelActivated(final Lookup context)
    {
    }
    
    public synchronized void panelDeactivated()
    {
    }
    
    public Lookup getLookup()
    {
        return null;
    }
    
    static class EditableTableModel extends CustomizerAbilities.StorableTableModel
    {
        private boolean editable=true;
        
        public boolean isCellEditable(@SuppressWarnings("unused")
		final int rowIndex, @SuppressWarnings("unused")
		final int columnIndex) {
            return columnIndex == 1 && editable;
        }
        
        void setEditable(final boolean edit)
        {
            editable=edit;
        }
        
        void removeListeners()
        {
            final Object list[]=listenerList.getListenerList();
            for (final Object l : list)
            {
                if ((l instanceof TableModelListener) && !(l instanceof JTable))
                    listenerList.remove(TableModelListener.class, (TableModelListener)l);
            }
        }
    }
}

