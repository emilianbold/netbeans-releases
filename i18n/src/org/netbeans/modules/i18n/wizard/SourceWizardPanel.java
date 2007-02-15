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
import java.beans.BeanInfo;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.netbeans.modules.i18n.SelectorUtils;

/**
 * First panel used in I18N (test) Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
final class SourceWizardPanel extends JPanel {

    /** Sources selected by user. */
    private final Map sourceMap = Util.createWizardSourceMap();
    
    /** This component panel wizard descriptor.
     * @see org.openide.WizardDescriptor.Panel 
     * @see Panel */
    private final Panel descPanel;

    /**
     * Panel role true (test wizard) false (i18n) wizard
     */
    private boolean testRole = false;
    
    /** Creates new form SourceChooserPanel.
     * @param it's panel wizard descriptor */
    private SourceWizardPanel(Panel descPanel, boolean testRole) {
        this.descPanel = descPanel;
        this.testRole = testRole;
        
        initComponents();        

        initAccessibility ();
        
        setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
        
        initList();
        
        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); // NOI18N
        
        if (testRole) {
            setName(Util.getString("TXT_SelecTestSources"));
        } else {
            setName(Util.getString("TXT_SelectSources"));                
        }        
    }
    

    /** Getter for <code>sources</code> property. */
    public Map getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>sources</code> property. */
    public void setSourceMap(Map sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }

    /**
     * Panel description depend of its container test or i18n role
     */
    private String getPanelDescription() {
        if (testRole == false)   {
            return Util.getString("MSG_SourcesPanel_desc");
        } else {
            return Util.getString("MSG_SourcesPanel_test_desc");
        }        
    }

    /**
     * Accessible panel description depends of its container test or i18n role
     */
    private String getAccessibleListDescription() {
        if (testRole == false)   {
            return Util.getString("ACSD_sourcesList");
        } else {
            return Util.getString("ACSD_sourcesList_test");
        }        
    }
    
    
    /** 
     * List content drives remove button enableness.
     */
    private void initList() {
        sourcesList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent evt) {
                    removeButton.setEnabled(!sourcesList.isSelectionEmpty());
                }
            }
        );
        
        removeButton.setEnabled(!sourcesList.isSelectionEmpty());
    }
    
    private void initAccessibility() {        
        getAccessibleContext().setAccessibleDescription(getPanelDescription());
        
        addButton.setToolTipText(Util.getString("CTL_AddSource_desc"));
        
        removeButton.setToolTipText(Util.getString("CTL_RemoveSource_desc"));
        
        sourcesList.getAccessibleContext().setAccessibleName(Util.getString("ACSN_sourcesList"));
        sourcesList.getAccessibleContext().setAccessibleDescription(getAccessibleListDescription());
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
        sourcesList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

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

        sourcesList.setCellRenderer(new DataObjectListCellRenderer());
        jScrollPane1.setViewportView(sourcesList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, NbBundle.getBundle(SourceWizardPanel.class).getString("CTL_AddSource")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getBundle(SourceWizardPanel.class).getString("CTL_RemoveSource")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(removeButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object[] selected = sourcesList.getSelectedValues();
        
        for(int i=0; i<selected.length; i++) {
            sourceMap.remove(selected[i]);
        }

        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed

        // take actual project from first data object

        Project prj = descPanel.getProject();
  
        // Selects source data objects which could be i18n-ized.
        try {
            Node[] selectedNodes= NodeOperation.getDefault().select(
                Util.getString("LBL_SelectSources"),
                Util.getString("LBL_Filesystems"),
                SelectorUtils.sourcesNode(prj, SelectorUtils.ALL_FILTER),
                new NodeAcceptor() {
                    public boolean acceptNodes(Node[] nodes) {
                        if(nodes == null || nodes.length == 0) {
                            return false;
                        }

                        for(int i=0; i<nodes.length; i++) {
                            // Has to be data object.
                            Object dataObject = nodes[i].getCookie(DataObject.class);
                            if (dataObject == null) {
                                return false;
                            }
                            // if it is folder and constains some our data object.
                            if (dataObject instanceof DataFolder) {
                                if (I18nUtil.containsAcceptedDataObject((DataFolder) dataObject)) {
                                    return true;
                                }
                            } else if (FactoryRegistry.hasFactory(dataObject.getClass())) {
                                // Has to have registered i18n factory for that data object class name.
                                return true;
                            }
                        }
                        
                        return false;
                    }                    
                }
            );
            
            for(int i=0; i<selectedNodes.length; i++) {
                DataObject dataObject = (DataObject)selectedNodes[i].getCookie(DataObject.class);

                if (dataObject instanceof DataFolder) {
                    // recursively add folder content
                    Iterator it = I18nUtil.getAcceptedDataObjects((DataFolder)dataObject).iterator();
                    while (it.hasNext()) {
                        Util.addSource(sourceMap, (DataObject)it.next());
                    }
                } else {
                    Util.addSource(sourceMap, dataObject);
                }
            }
            
            sourcesList.setListData(sourceMap.keySet().toArray());
           
            descPanel.fireStateChanged();
        } catch (UserCancelException uce) {
           // ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, uce);
             // nobody is interested in the message
        }
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JList sourcesList;
    // End of variables declaration//GEN-END:variables


    /** List cell rendrerer which uses data object as values. */
    public static class DataObjectListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
        JList list,
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus)    // the list and the cell have the focus
        {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            DataObject dataObject = (DataObject)value;

            if(dataObject != null) {
                ClassPath cp = ClassPath.getClassPath( dataObject.getPrimaryFile(), ClassPath.SOURCE );
                                
                label.setText(cp.getResourceName( dataObject.getPrimaryFile(), '.', false )); // NOI18N
                label.setIcon(new ImageIcon(dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                label.setText(""); // NOI18N
                label.setIcon(null);
            }

            return label;
        }
    }

    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>SourceChooserPanel</code>.
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel */
    public static class Panel extends I18nWizardDescriptor.Panel {

        /** Test wizard flag. */
        private final boolean testWizard;
        
        
        /** Constructor for i18n wizard. */
        public Panel() {
            this(false);
        }
        
        /** Constructor for specified i18n wizard. */
        public Panel(boolean testWizard) {
            this.testWizard = testWizard;
        }
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {                                    
            Component component = new SourceWizardPanel(this, testWizard);            
            
            return component;
        }

        /** Gets if panel is valid. Overrides superclass method. */
        public boolean isValid() {
            return !((SourceWizardPanel)getComponent()).getSourceMap().isEmpty();
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        public void readSettings(Object settings) {
	  super.readSettings(settings);
	  ((SourceWizardPanel)getComponent()).setSourceMap(getMap());
        }

        /** Stores settings at the end of panel show. Overrides superclass method. */
        public void storeSettings(Object settings) {
	  super.storeSettings(settings);
	    super.storeSettings(settings);
            // Update sources.
            getMap().clear();
            getMap().putAll(((SourceWizardPanel)getComponent()).getSourceMap());
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            if(testWizard)
                return new HelpCtx(I18nUtil.HELP_ID_TESTING);
            else
                return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
        }

    } // End of nested Panel class.
    
}
