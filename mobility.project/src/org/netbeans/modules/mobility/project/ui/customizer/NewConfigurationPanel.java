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
 * NewConfigurationPanel.java
 *
 * Created on February 11, 2004, 2:44 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.ui.wizard.ConfigurationsSelectionPanelGUI;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.CategoryDescriptor;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.Descriptor;
import org.openide.DialogDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author  Adam Sotona
 */
public class NewConfigurationPanel extends JPanel implements DocumentListener, PropertyChangeListener, VetoableChangeListener, ExplorerManager.Provider {
    
    private DialogDescriptor dialogDescriptor;
    final private Collection<String> allNames;
    private final ExplorerManager manager = new ExplorerManager();
    private final BeanTreeView treeView;
    private String oldName;
    public static final Image CLOSED_ICON = findIcon("Nb.Explorer.Folder.icon", "Tree.closedIcon"); // NOI18N
    public static final Image OPENED_ICON = findIcon("Nb.Explorer.Folder.openedIcon", "Tree.openIcon"); // NOI18N
    
    /** Creates new form NewConfigurationPanel */
    public NewConfigurationPanel(Collection<String> allNames) {
        this.allNames = allNames;
        initComponents();
        initAccessibility();
        treeView = new BeanTreeView();
        jLabel2.setLabelFor(treeView);
        treeView.setPopupAllowed(false);
        treeView.setRootVisible(false);
        treeView.setDefaultActionAllowed(false);
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jPanel1.add(treeView, BorderLayout.CENTER);
        AbstractNode root = new AbstractNode(new Children.Keys<ProjectConfigurationFactory>(){
            {setKeys(Lookup.getDefault().lookupAll(ProjectConfigurationFactory.class));}
            protected Node[] createNodes(ProjectConfigurationFactory key) {
                return new Node[] {key instanceof ConfigurationTemplateDescriptor ? new TemplateNode((ConfigurationTemplateDescriptor)key) : new CategoryNode(key.getRootCategory())};
            }
        });
        root.setName(NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_CfgSelectionPanel_Templates")); //NOI18N
        manager.setRootContext(root);
        manager.addPropertyChangeListener(this);
//        manager.addVetoableChangeListener(this);
        try {
            manager.setSelectedNodes(new Node[] {root.getChildren().nodes().nextElement()});
        } catch (PropertyVetoException pve) {}
    }
    
    private class CategoryNode extends AbstractNode {
        public CategoryNode(final ProjectConfigurationFactory.CategoryDescriptor cat) {
            super(new Children.Keys<ProjectConfigurationFactory.Descriptor>(){
                {setKeys(cat.getChildren());}
                protected Node[] createNodes(Descriptor key) {
                    Node n = key instanceof CategoryDescriptor ? new CategoryNode((CategoryDescriptor)key) : key instanceof ConfigurationTemplateDescriptor ? new TemplateNode((ConfigurationTemplateDescriptor)key) : null;
                    return n == null ? null : new Node[] {n};
                }
            });
            setDisplayName(cat.getDisplayName());
        }

        public Image getIcon(int type) {
            return CLOSED_ICON == null ? super.getIcon(type) : CLOSED_ICON;
        }

        public Image getOpenedIcon(int type) {
            return OPENED_ICON == null ? super.getOpenedIcon(type) : OPENED_ICON;
        }
    }

    
    private static Image findIcon(String key1, String key2) {
        Image i = icon2image(key1);
        return i == null ? icon2image(key2) : i;
    }

    /** Gets an icon from UIManager and converts it to Image
     */
    private static Image icon2image(String key) {
        Object obj = UIManager.get(key);
        if (obj instanceof Image) {
            return (Image)obj;
        }
        
        if (obj instanceof Icon) {
            Icon icon = (Icon)obj;
            return Utilities.icon2Image(icon);
        }
        
        return null;
    }  
    
    private class TemplateNode extends AbstractNode {
        private ConfigurationTemplateDescriptor cfgTmp;
        public TemplateNode(ConfigurationTemplateDescriptor cfgTmp) {
            super(Children.LEAF, Lookups.singleton(cfgTmp));
            this.cfgTmp = cfgTmp;
            setDisplayName(cfgTmp.getDisplayName().equals(cfgTmp.getCfgName()) || cfgTmp.getCfgName().length() == 0 ? cfgTmp.getDisplayName() : NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_CfgSlePanel_TemplateNodePattern", cfgTmp.getDisplayName(), cfgTmp.getCfgName())); //NOI18N
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public String getName() {
        return jTextFieldName == null ? "" : jTextFieldName.getText(); //NOI18N
    }
    
    public ConfigurationTemplateDescriptor getTemplate() {
        Node[] nodes = manager.getSelectedNodes();
        if (nodes.length != 1) return null;
        return nodes[0].getLookup().lookup(ConfigurationTemplateDescriptor.class);
    }
    
    public void setDialogDescriptor(final DialogDescriptor dd) {
        assert dialogDescriptor == null : "Set the dialog descriptor only once!"; //NOI18N
        dialogDescriptor = dd;
        dd.setHelpCtx(new HelpCtx(NewConfigurationPanel.class));
        jTextFieldName.getDocument().addDocumentListener(this);
        changedUpdate(null);
        oldName = ""; //NOI18N
    }
    
    public boolean isValid() {
        final String name = jTextFieldName.getText();
        if (J2MEProjectUtils.ILEGAL_CONFIGURATION_NAMES.contains(name)) {
            errorPanel.setErrorBundleMessage("ERR_AddCfg_ReservedWord"); //NOI18N
            return false;
        }
        if (!Utilities.isJavaIdentifier(name)) {
            errorPanel.setErrorBundleMessage("ERR_AddCfg_MustBeJavaIdentifier"); //NOI18N
            return false;
        }
        if (allNames.contains(name)) {
            errorPanel.setErrorBundleMessage("ERR_AddCfg_NameExists"); //NOI18N
            return false;
        }
        if (getTemplate() == null) {
            errorPanel.setErrorBundleMessage("ERR_AddCfg_SelectTmp"); //NOI18N
            return false;
        };
        errorPanel.setErrorBundleMessage(null);
        return true;
    }
    
    public void changedUpdate(@SuppressWarnings("unused")
            final DocumentEvent e) {
        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(isValid());
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        changedUpdate(null);
        final String s = jTextFieldName.getText();
        if (s.length() == 0 || s.equals(oldName)) {
            Node[] nodes = manager.getSelectedNodes();
            if (nodes.length != 1) return;
            ConfigurationTemplateDescriptor desc = nodes[0].getLookup().lookup(ConfigurationTemplateDescriptor.class);
            if (desc == null) return;
            jTextFieldName.setText(desc.getCfgName());
            oldName = jTextFieldName.getText();
        }
    }
    
    public void vetoableChange(PropertyChangeEvent evt)throws PropertyVetoException {
        if (evt.getNewValue() instanceof Node[]) {
            Node[] n = (Node[])evt.getNewValue();
            if (n.length > 1) throw new PropertyVetoException("Mutiselection is not alloved", evt); //NOI18N
            if (n.length == 0) return;
            if (n[0].getLookup().lookup(ConfigurationTemplateDescriptor.class) == null) throw new PropertyVetoException("Only configuration templates selection allowed", evt); //NOI18N
        }
    }

    public void addNotify() {
        super.addNotify();
        jTextFieldName.requestFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setMinimumSize(new java.awt.Dimension(450, 200));
        setPreferredSize(new java.awt.Dimension(480, 350));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(NewConfigurationPanel.class, "LBL_NewConfigPanel_ConfigTemplate")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 12);
        add(jLabel2, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jPanel1, gridBagConstraints);

        jLabel1.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(NewConfigurationPanel.class, "LBL_NewConfigPanel_ConfigurationName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jTextFieldName, gridBagConstraints);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConfigurationPanel.class, "ACSD_NewCfg_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConfigurationPanel.class, "ACSN_NewConfigPanel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConfigurationPanel.class, "ACSD_NewConfigPanel"));
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}
