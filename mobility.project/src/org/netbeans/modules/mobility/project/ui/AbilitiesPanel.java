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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.GlobalAbilitiesCache;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.AddAttributePanel;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.AddAbilityPanel;
import org.netbeans.modules.mobility.project.ui.customizer.CustomizerAbilities;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 *
 * @author Lukas Waldmann
 */

public class AbilitiesPanel implements NavigatorPanel
{
    private static ABPanel instance=null;
    
    //Version for the hack
    /*
    static class ABHint implements NavigatorLookupHint
    {
        private String ext="";
        
        private synchronized void setExt()
        {
            ext="ext";
        }
        
        synchronized public String getContentType()
        {
            String ret= "j2me/abilities"+ext;
            ext="";
            return ret;
        }
    };
     **/
    
    static class ABHint implements NavigatorLookupHint
    {
        static String hint= "j2me/abilities";
        
        public String getContentType()
        {
            return hint;
        }
    };
    
        
    static ABHint hintInstance=new ABHint();
    
    private static String MULTIPLE_VALUES=NbBundle.getMessage(AbilitiesPanel.class,"LBL_MultipleValues");
    private static String REMOVE=NbBundle.getMessage(AbilitiesPanel.class,"LBL_RemoveAbility");
    private static String ADD=NbBundle.getMessage(AbilitiesPanel.class,"LBL_AddAbility");
    private static String COPY=NbBundle.getMessage(AbilitiesPanel.class,"LBL_CopyAbility");
    private static String PASTE=NbBundle.getMessage(AbilitiesPanel.class,"LBL_PasteAbility");
    
    static class ABPanel extends JPanel implements ExplorerManager.Provider
    {
        private static javax.swing.JScrollPane scrollPane;
        final private static EditableTableModel tableModel =  new EditableTableModel();
        final private static JTable table=new JTable(tableModel);    
        final private static Object[] emptyTable = new Object[] {new HashMap<String,String>()};
        private static J2MEProject project=null;
        private static Node[] selectedNodes=null;
        private static Node defaultConfig=null;
        private static Action[] actions = { new AddAction(), new RemoveAction(), new CopyAction(), new PasteAction() };
        private static ExplorerManager manager=new ExplorerManager();
        private static HashMap<String,String> copiedAbilities=new HashMap<String,String>();
        
        //To disable paste at the beginning
        static
        {
            actions[3].setEnabled(false);
        }
        
        static void addAbility(EditableProperties ep,AntProjectHelper helper, String key, String value)
        {
            //Save the change
            if (defaultConfig != null)
            {   
                String abilities=ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                //We don't want to overwite existing ability, it may happen just in case of PastAction not AddAction, 
                //which has internal check to avoid this
                if (!ab.containsKey(key))
                {
                    ab.put(key,value);
                    abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                    ep.put(DefaultPropertiesDescriptor.ABILITIES,abilities);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);   
                }
                else
                {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,"Ability "+key+" already exist in the configuration "+ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME
                            +". Its value will not be modified");
                }
            }

            for (Node node : selectedNodes)
            {
                if (node != defaultConfig)
                {
                    ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                    String abilities = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.ABILITIES);
                    if (abilities == null)
                        // Let's take a default value if we inherit from default configuration
                        abilities = ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                    Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                    //if key is present it means default config is used and we don't need to added'
                    String oldVal=ab.get(key);
                    if (!ab.containsKey(key))
                    {
                        ab.put(key,value);
                        abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                        ep.put(J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.ABILITIES,abilities);
                    }
                    else
                    {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,"Ability "+key+" already exist in the configuration "+conf.getDisplayName()
                                +". Its value will not be modified");
                    }
                }
            }
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);   
        }
        
        static private class CopyAction extends AbstractAction {
            private CopyAction()
            {
                putValue(Action.NAME,COPY);
                putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke ("ctrl C"));
            }

            public void actionPerformed(ActionEvent e)
            {                
                final int rows[]=table.getSelectedRows();
                copiedAbilities=new HashMap<String,String>(rows.length);
                for (int row : rows)
                {
                    String key=tableModel.getValueAt(row,0);
                    String value=tableModel.getValueAt(row,1);
                    copiedAbilities.put(key,value);
                }
                actions[3].setEnabled(true);
            }
        }
        
        static private class PasteAction extends AbstractAction {
            private PasteAction()
            {
                putValue(Action.NAME,PASTE);
                putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke ("ctrl V"));
            }
                       
            public void actionPerformed(ActionEvent e)
            {
                ProjectManager.mutex().writeAccess(new Runnable() 
                {
                    public void run() 
                    {
                        final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                        final EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        
                        for (Entry<String,String> entry : copiedAbilities.entrySet())
                        {
                            addAbility(ep,helper,entry.getKey(),entry.getValue());
                            tableModel.addRow(entry.getKey(), entry.getValue());
                        }
                        try
                        {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (Exception ex)
                        {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                });

            }
        }
        
        static private class AddAction extends AbstractAction {

            private AddAction()
            {
                putValue(Action.NAME,ADD);
            }

            public void actionPerformed(ActionEvent e)
            {
                final AddAbilityPanel add = new AddAbilityPanel();
                HashSet<String> usedAbilities=new HashSet<String>();
                final AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                final EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                for (Node node : selectedNodes)
                {
                    ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                    String abilities = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.ABILITIES);
                    if (abilities == null)
                        // Let's take a default value if we inherit from default configuration
                        abilities = ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                    Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                    usedAbilities.addAll(ab.keySet());
                }
                final Vector<String> proposedAbilities = new Vector<String>(GlobalAbilitiesCache.getDefault().getAllAbilities());
                proposedAbilities.removeAll(usedAbilities);
                add.init(false, proposedAbilities, usedAbilities, null, null);
                final DialogDescriptor dd = new DialogDescriptor(
                    add, NbBundle.getMessage(CustomizerAbilities.class, "TITLE_AddAbility"), //NOI18N
                    true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(AddAttributePanel.class),
                    new ActionListener() 
                    {
                        public void actionPerformed(ActionEvent e) 
                        {
                            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                                final String key = add.getKey();
                                final String value=add.getValue();
                                GlobalAbilitiesCache.getDefault().addAbility(key);
                                int row = tableModel.addRow(key, value);
                                table.getSelectionModel().setSelectionInterval(row, row);
                                
                                ProjectManager.mutex().writeAccess(new Runnable() 
                                {
                                    public void run() 
                                    {
                                        addAbility(ep,helper,key,value);
                                        //Save the properties
                                        try
                                        {
                                            ProjectManager.getDefault().saveProject(project);
                                        } catch (Exception ex)
                                        {
                                            ErrorManager.getDefault().notify(ex);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    
                add.setDialogDescriptor(dd);
                final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.setVisible(true);
            }
        }
        
        static private class RemoveAction extends AbstractAction {

            private RemoveAction()
            {
                putValue(Action.NAME,REMOVE);
            }

            public void actionPerformed(ActionEvent e)
            {
                Object o=e.getSource();
                final int rows[]=table.getSelectedRows();
                if (project == null) return;
                
                ProjectManager.mutex().writeAccess(new Runnable() 
                {
                    public void run() 
                    {
                        for (int row : rows)
                        {
                            //getValueAt(rows[0],0) is always different, because we delete rows[0] in
                            //each iteration and rows[0] is actual line we want to delete
                            String key=tableModel.getValueAt(rows[0],0);                    
                            tableModel.removeRow(rows[0]);
                            AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                            EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            if (defaultConfig != null)
                            {   
                                String abilities=ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                                Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                                ab.remove(key);
                                abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                                ep.put(DefaultPropertiesDescriptor.ABILITIES,abilities);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);   
                            }

                            for (Node node : selectedNodes)
                            {
                                if (node != defaultConfig)
                                {
                                    ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                                    String abilities = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.ABILITIES);
                                    if (abilities == null)
                                        // Let's take a default value if we inherit from default configuration
                                        abilities = ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                                    Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                                    //if value is the same configuration inherits its values from the default configuration 
                                    //which was selected (and modified) as well so we don't need to change it
                                    //if the values are different configuration don't inherit abilities anymore and so we can store it
                                    if (ab.containsKey(key))
                                    {
                                        ab.remove(key);
                                        abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                                        ep.put(J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.ABILITIES,abilities);
                                    }
                                }
                            }
                            //Save the properties
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                        }
                        
                        try
                        {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (Exception ex)
                        {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                });
            }
        }
        
        private ABPanel()
        {
            initComponents();            
            scrollPane.setViewportView(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);                                    
            final TableCellEditor editor=table.getDefaultEditor(String.class);
            //Add the popup menu for abilities table
            final JPopupMenu pm=new JPopupMenu();
            for (Action act : actions)
                pm.add(act);            
            table.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e) {
                  showPopup(e);
                }

                public void mouseReleased(MouseEvent e) {
                  showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                  if (e.isPopupTrigger()) {
                    int row=table.rowAtPoint(e.getPoint());
                    int selRows[]=table.getSelectedRows();
                    if ((selRows.length>=2 && (row < selRows[0] || row > selRows[selRows.length-1])) || selRows.length<2)
                        table.setRowSelectionInterval(row,row);
                    pm.show(e.getComponent(), e.getX(), e.getY());
                  }
                }
            });
            
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
                        ProjectManager.mutex().writeAccess(new Runnable() 
                        {
                            public void run() 
                            {
                                AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                                EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                if (defaultConfig != null)
                                {
                                    String abilities=ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                                    Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                                    ab.put(key,newValue);
                                    abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                                    ep.put(DefaultPropertiesDescriptor.ABILITIES,abilities);
                                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);                                                                       
                                }                        

                                for (Node node : selectedNodes)
                                {
                                    if (node != defaultConfig)
                                    {
                                        ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                                        String abilities = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.ABILITIES);
                                        if (abilities == null)
                                            // Let's take a default value if we inherit from default configuration
                                            abilities = ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                                        Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                                        String defCf=ab.get(key);
                                        //if value is the same configuration inherits its values from the default configuration 
                                        //which was selected (and modified) as well so we don't need to change it
                                        //if the values are different configuration don't inherit abilities anymore and so we can store it
                                        if ((defCf==null && newValue !=null) || !defCf.equals(newValue))
                                        {
                                            ab.put(key,newValue);
                                            abilities=CommentingPreProcessor.encodeAbilitiesMap(ab);
                                            ep.put(J2MEProjectProperties.CONFIG_PREFIX+conf.getDisplayName()+"."+DefaultPropertiesDescriptor.ABILITIES,abilities);
                                        }
                                    }
                                }
                                //Save the properties
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
                                try
                                {
                                    ProjectManager.getDefault().saveProject(project);
                                } catch (Exception ex)
                                {
                                    ErrorManager.getDefault().notify(ex);
                                } 
                            }
                        });
                    }
                }

                public void editingCanceled(ChangeEvent e)
                {
                }               
            });
            
        }
        
        public Action[] getActions() {
            return actions;
        }

        
        public static void setAbilities(final Node[] nodes)
        {
            HashSet<String> abSet = new HashSet<String>();
            HashMap<String,String> abIntersection = new HashMap<String,String>();
            J2MEProject prj=null;            
            boolean setExt = false;
            defaultConfig=null;
            
            for (Node n : nodes)
            {
                Lookup context=n.getLookup();                
                if (context!=null)
                {
                    ABHint data=context.lookup(ABHint.class);
                    if (data!=null)
                    {
                        tableModel.removeListeners();
                        Node node=context.lookup(Node.class);
                        project=node.getLookup().lookup(J2MEProject.class);
                        ProjectConfiguration conf=node.getLookup().lookup(ProjectConfiguration.class);
                        assert project != null;
                        if (conf.getDisplayName().equals(ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME))
                            defaultConfig=n;

                        AntProjectHelper helper=project.getLookup().lookup(AntProjectHelper.class);
                        EditableProperties ep=helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String abilities = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + conf.getDisplayName() + "." + DefaultPropertiesDescriptor.ABILITIES);
                        if (abilities == null)
                            // Let's take a default value if we inherit from default configuration
                            abilities = ep.getProperty(DefaultPropertiesDescriptor.ABILITIES);
                        Map<String,String> ab=CommentingPreProcessor.decodeAbilitiesMap(abilities);
                        //change hinttype so only one instace is found
                        if (setExt)
                        {
                            //User has choosen multiple configurations from different projects - we don't support it
                            if (prj!=project)
                            {
                                defaultConfig=null;
                                tableModel.setDataDelegates(emptyTable);
                                return;
                            }
                                
                            //data.setExt();
                            abSet.retainAll(ab.keySet());
                            
                            //Aggregate
                            for (String key : abSet)
                            {
                                if (abIntersection.containsKey(key))
                                {
                                    String value1=abIntersection.get(key);
                                    String value2=ab.get(key);
                                    if ((value1 != null && !value1.equals(value2) || (value1==null && value2!=null)))
                                    {
                                        abIntersection.put(key,MULTIPLE_VALUES);
                                    }
                                }
                                else
                                    abIntersection.put(key,ab.get(key));
                            }
                        }
                        else
                        {
                            //first pass
                            prj=project;
                            setExt=true;
                            abSet.addAll(ab.keySet());
                            abIntersection.putAll(ab);
                        }
                    }
                }
            }
            abIntersection.keySet().retainAll(abSet);
            tableModel.setEditable(true);
            table.setBackground(javax.swing.UIManager.getDefaults().getColor("Table.background")); //NOI18N               
            tableModel.setDataDelegates(new Object[] {abIntersection});
            selectedNodes=nodes;
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

        public ExplorerManager getExplorerManager()
        {
            return manager;
        }
    }
        
    public synchronized JComponent getComponent()
    {
        return ABPanel.getInstance();
    }
    
    public String getDisplayName()
    {
        String cf="";
        if (ABPanel.selectedNodes != null)            
            cf =ABPanel.selectedNodes.length==1 ? " : " + ABPanel.selectedNodes[0].getLookup().lookup(ProjectConfiguration.class).getDisplayName() : 
                                                  " : " + NbBundle.getMessage(AbilitiesPanel.class,"LBL_MultipleConfigs");
        return "Abilities" + cf ;
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

