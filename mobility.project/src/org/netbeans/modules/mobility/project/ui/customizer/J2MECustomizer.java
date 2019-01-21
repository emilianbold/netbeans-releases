/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * J2MECustomizer.java
 *
 * Created on November 21, 2003, 5:34 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.spi.mobility.project.ui.customizer.ComposedCustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.HelpCtxCallback;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.HelpCtx;

/**
 *
 */
public class J2MECustomizer extends JPanel implements Runnable, HelpCtxCallback {
    
    private static final String REGISTRATION_FOLDER = "Customizer/org.netbeans.modules.kjava.j2meproject";//NOI18N
    private static final ProjectConfiguration ADD_CONFIGURATION = new ProjectConfiguration() {
        private final String name=NbBundle.getMessage(J2MECustomizer.class, "LBL_Customizer_AddConfiguration"); //NOI18N
        public String getDisplayName() {
            return name;
        }
    };
    
    public static final String ADD_CONFIG_DIALOG = "AddConfigDialog"; // NOI18N
    
    private J2MEProjectProperties props;
    private CategoryView categoryView;
    private DialogDescriptor descriptor;
    private String startPanel="";
    
    /** Creates new form J2MECustomizer */
    public J2MECustomizer( J2MEProjectProperties j2meProperties, String startPanel ) {
        this(j2meProperties);
        this.startPanel=startPanel;
        
    }
    
    
    /** Creates new form J2MECustomizer */
    public J2MECustomizer( J2MEProjectProperties j2meProperties ) {
        initComponents();
        configurationCombo.setRenderer(new ConfigurationCellRenderer());
        this.props = j2meProperties;
        
        categoryView = new CategoryView( createRootNode() );
        categoryPanel.add( categoryView, BorderLayout.CENTER);
        initAccessibility();
        setConfigurationCombo();
    }
    
    public void setDialogDescriptor(final DialogDescriptor dd) {
        this.descriptor = dd;
    }
    
    public void updateHelpCtx(final HelpCtx help) {
        if (descriptor != null) descriptor.setHelpCtx(help);
    }
    
    final public void setConfigurationCombo() {
        final Vector<ProjectConfiguration> configs = new Vector<ProjectConfiguration>(Arrays.asList(props.getConfigurations()));
        ProjectConfiguration sel = props.getActiveConfiguration();
        if (!configs.contains(sel)) {
            sel = configs.get(0);
            props.setActiveConfiguration(sel);
        }
        configs.add(ADD_CONFIGURATION);
        configurationCombo.setModel(new DefaultComboBoxModel(configs));
        configurationCombo.setSelectedItem(sel);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configurationPanel = new javax.swing.JPanel();
        jLabelConfig = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        jButtonEdit = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelCategory = new javax.swing.JLabel();
        categoryPanel = new javax.swing.JPanel();
        customizerPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(760, 500));
        setPreferredSize(new java.awt.Dimension(760, 530));
        setLayout(new java.awt.GridBagLayout());

        configurationPanel.setLayout(new java.awt.GridBagLayout());

        jLabelConfig.setLabelFor(configurationCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelConfig, NbBundle.getMessage(J2MECustomizer.class, "LBL_Customizer_Configuration")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        configurationPanel.add(jLabelConfig, gridBagConstraints);

        configurationCombo.setMinimumSize(new java.awt.Dimension(220, 24));
        configurationCombo.setPreferredSize(new java.awt.Dimension(220, 24));
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        configurationPanel.add(configurationCombo, gridBagConstraints);
        configurationCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MECustomizer.class, "ACSD_Customizer_Configuration")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonEdit, NbBundle.getMessage(J2MECustomizer.class, "LBL_Customizer_Edit")); // NOI18N
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 5, 0, 8);
        configurationPanel.add(jButtonEdit, gridBagConstraints);
        jButtonEdit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MECustomizer.class, "ACSD_Customizer_ManageCfgButton")); // NOI18N

        jSeparator2.setMinimumSize(new java.awt.Dimension(0, 5));
        jSeparator2.setPreferredSize(new java.awt.Dimension(0, 5));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 8, 0, 8);
        configurationPanel.add(jSeparator2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(configurationPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabelCategory, NbBundle.getMessage(J2MECustomizer.class, "LBL_Customizer_Category")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        add(jLabelCategory, gridBagConstraints);
        jLabelCategory.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MECustomizer.class, "ACSD_Customizer_Category")); // NOI18N

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setMinimumSize(new java.awt.Dimension(220, 24));
        categoryPanel.setPreferredSize(new java.awt.Dimension(220, 24));
        categoryPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 8, 8, 8);
        add(categoryPanel, gridBagConstraints);

        customizerPanel.setMinimumSize(new java.awt.Dimension(530, 10));
        customizerPanel.setPreferredSize(new java.awt.Dimension(530, 10));
        customizerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(customizerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2MECustomizer.class, "ACSN_Customizer"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MECustomizer.class, "ACSD_Customizer"));
        jLabelCategory.setLabelFor(categoryView.getBeanTreeView());
    }
    
    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        final CustomizerConfigManager ccm = new CustomizerConfigManager(props,null);
        DialogDisplayer.getDefault().notify(new DialogDescriptor(ccm, NbBundle.getMessage(J2MECustomizer.class, "Title_ProjectConfigurationManager"), true, new Object[]{NotifyDescriptor.CLOSED_OPTION}, NotifyDescriptor.CLOSED_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CustomizerConfigManager.class), null));    //NOI18N
        setConfigurationCombo();
    }//GEN-LAST:event_jButtonEditActionPerformed
    
    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        if (ADD_CONFIGURATION.equals(configurationCombo.getSelectedItem())) {
            RequestProcessor.getDefault().post(this);
        } else {
            props.setActiveConfiguration((ProjectConfiguration)configurationCombo.getSelectedItem());
            categoryView.updateCustomizerPanel();
        }
    }//GEN-LAST:event_configurationComboActionPerformed
    
    
    public void run() {
        
        if (!J2MECustomizer.this.isShowing()) {
            SwingUtilities.invokeLater(J2MECustomizer.this);
            return;
        }
        
        
        final NewConfigurationPanel p = new NewConfigurationPanel(props.getAllIdentifiers());
        final DialogDescriptor dd = new DialogDescriptor(p, NbBundle.getMessage(J2MECustomizer.class,
                "Title_Customizer_AddConfiguration"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
        p.setDialogDescriptor(dd);
        
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
            final ProjectConfiguration cfg = new ProjectConfiguration() {
                private final String name = p.getName();
                public String getDisplayName() {
                    return name;
                }
            };
            final ProjectConfiguration[] cfgs = props.getConfigurations();
            ProjectConfiguration[] ncfgs = new ProjectConfiguration[cfgs.length + 1];
            System.arraycopy(cfgs, 0, ncfgs, 0, cfgs.length);
            ncfgs[ncfgs.length - 1] = cfg;
            props.setConfigurations(ncfgs);
            VisualConfigSupport.createFromTemplate(props, p.getName(), p.getTemplate());
            props.setActiveConfiguration(cfg);
            setConfigurationCombo();
        } else {
            if (props.getActiveConfiguration() == null) {
                configurationCombo.setSelectedIndex(0);
            } else {
                configurationCombo.setSelectedItem(props.getActiveConfiguration());
            }
            categoryView.updateCustomizerPanel();
        }
    }
    
    
    
    public void addNotify() {
        super.addNotify();
        
        if (ADD_CONFIG_DIALOG.equals(startPanel)) {
            SwingUtilities.invokeLater(this);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JLabel jLabelCategory;
    private javax.swing.JLabel jLabelConfig;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration//GEN-END:variables
    
    // Private innerclasses ----------------------------------------------------
    
    private class CategoryView extends JPanel implements ExplorerManager.Provider, ChangeListener {
        
        final protected ExplorerManager manager;
        final private BeanTreeView btv;
        private HashMap<Node, Integer> tabSelections = new HashMap<Node, Integer>();
        
        CategoryView( Node rootNode ) {
            
            // See #36315
            manager = new ExplorerManager();
            
            setLayout( new BorderLayout() );
            
            Dimension size = new Dimension( 220, 4 );
            btv = new BeanTreeView();    // Add the BeanTreeView
            try {
                Field f = BeanTreeView.class.getDeclaredField("tree");//NOI18N
                f.setAccessible(true);
                jLabelCategory.setLabelFor((Component)f.get(btv));
            } catch (Exception e){}
            btv.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
            btv.setPopupAllowed( false );
            btv.setRootVisible( false );
            btv.setDefaultActionAllowed( false );
            btv.setMinimumSize( size );
            btv.setPreferredSize( size );
            btv.setMaximumSize( size );
            this.add( btv, BorderLayout.CENTER );
            //#154345 - with lazy children, we need to perform the selection
            //once the children are actually initialized
            rootNode.addNodeListener(new NL(rootNode));
            manager.setRootContext( rootNode );
            selectFirstNode();
            btv.expandAll();
            manager.addPropertyChangeListener( new ManagerChangeListener() );

            JLabel waitLabel = new JLabel (NbBundle.getMessage(CategoryView.class,
                    "MSG_LOADING")); //NOI18N
            waitLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            waitLabel.setVerticalTextPosition(SwingConstants.CENTER);
            customizerPanel.add(waitLabel, BorderLayout.CENTER);
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public BeanTreeView getBeanTreeView() {
            return btv;
        }
        
        @Override
        public void addNotify() {
            super.addNotify();
            btv.expandAll();
            
        }

        private final class NL extends NodeAdapter implements Runnable {
            private final Node listenedTo;
            private boolean done;
            NL (Node listenedTo) {
                this.listenedTo = listenedTo;
            }

            private void post() {
                done = false;
                RequestProcessor.getDefault().post(this, 150);
            }

            private void change() {
                done = true;
                listenedTo.removeNodeListener(this);
                selectFirstNode();
            }

            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                change();
            }

            public void run() {
                if (!done) {
                    if (!J2MECustomizer.this.isShowing()) {
                        return;
                    }
                    if (manager.getRootContext().getChildren().getNodesCount() > 1) {
                        change();
                    } else if (J2MECustomizer.this.isShowing()) {
                        post();
                    }
                }
            }

        }
        
        private void selectFirstNode() {
            
            final Children ch = manager.getRootContext().getChildren();
            if ( ch != null ) {
                final Node nodes[] = ch.getNodes();
                if ( nodes != null && nodes.length > 1 ) {
                    try {
                        manager.setSelectedNodes( new Node[] { nodes[1] } );
                    } catch ( PropertyVetoException e ) {
                        // No node will be selected
                    }
                }
            }
            
        }

        public void updateCustomizerPanel() {
            final Object cfg = configurationCombo.getSelectedItem();
            final String configuration = configurationCombo.getSelectedIndex() == 0 || cfg == null || !(cfg instanceof ProjectConfiguration) ? null : ((ProjectConfiguration)cfg).getDisplayName();
            final Node nodes[] = manager.getSelectedNodes();
            if ( nodes == null || nodes.length <= 0 ) {
                return;
            }
            final Node node = nodes[0];
            
            customizerPanel.removeAll();
            
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            //#207513
            if (df == null) { // no cookie
                return;
            }
            
            JComponent c = (JComponent)df.getPrimaryFile().getAttribute("customizerPanelClass");//NOI18N
            if (c != null) {
                updateHelpCtx(new HelpCtx(c.getClass()));
                c.setBorder(BorderFactory.createEmptyBorder(5, 8, 8, 8));
                customizerPanel.add(c, BorderLayout.CENTER );
                configurationCombo.setEnabled(c instanceof VisualPropertyGroup);
                if (c instanceof CustomizerPanel) {
                    ((CustomizerPanel)c).initValues(props, configuration);
                }
                if (c instanceof ComposedCustomizerPanel) {
                    ((ComposedCustomizerPanel)c).setHelpContextCallback(J2MECustomizer.this);
                }
            } else {
                DataObject dob[] = df.getChildren();
                JTabbedPane tab = new JTabbedPane();
                tab.setBorder(BorderFactory.createEmptyBorder(5, 8, 8, 8));
                boolean cfgSensitive = false;
                for (int i=0; i<dob.length; i++) {
                    FileObject fob = dob[i].getPrimaryFile();
                    String triggerName = (String)fob.getAttribute("triggerPropertyName"); //NOI18N
                    String triggerValue = triggerName == null ? null : (String)fob.getAttribute("triggerPropertyValue"); //NOI18N
                    cfgSensitive |= triggerValue != null;
                    String state = triggerValue == null ? null : evaluatePropertyRaw(configuration, triggerName);
                    if (triggerValue == null || triggerValue.equalsIgnoreCase(state)) {
                        c = (JComponent)fob.getAttribute("customizerPanelClass");//NOI18N
                        if (c != null) {
                            c.setBorder(BorderFactory.createEmptyBorder(5, 8, 8, 8));
                            tab.add(dob[i].getNodeDelegate().getDisplayName(), c);
                            cfgSensitive |= c instanceof VisualPropertyGroup;
                            if (c instanceof CustomizerPanel) {
                                ((CustomizerPanel)c).initValues(props, configuration);
                            }
                        }
                    }
                }
                configurationCombo.setEnabled(cfgSensitive);
                if (tab.getComponentCount() > 0) {  
                    updateHelpCtx(new HelpCtx(tab.getComponent(0).getClass()));
                    customizerPanel.add(tab.getComponentCount()==1 ? tab.getComponent(0) : tab, BorderLayout.CENTER );
                    tab.addChangeListener(CategoryView.this);
                    Integer i = tabSelections.get(node);
                    if (i != null && i < tab.getTabCount()) {
                        tab.setSelectedIndex(i);
                    }
                } else {
                    updateHelpCtx(null);
                }
            }
            customizerPanel.validate();
            customizerPanel.repaint();
        }
        
        
        private String evaluatePropertyRaw(String configuration, String name) {
            if (configuration != null) {
                String s = props.getPropertyRawValue(VisualPropertySupport.prefixPropertyName(configuration, name));
                if (s != null) return s;
            }
            return props.getPropertyRawValue(name);
        }
        
        /** Listens to selection change and shows the customizers as
         *  panels
         */
   
        public void stateChanged(ChangeEvent e) {
            JTabbedPane p = (JTabbedPane)e.getSource();
            Node nodes[] = manager.getSelectedNodes();
            if (nodes != null && nodes.length == 1) {
                tabSelections.put(nodes[0], p.getSelectedIndex());
            }
            updateHelpCtx(new HelpCtx(p.getSelectedComponent().getClass()));
        }
        
        private class ManagerChangeListener implements PropertyChangeListener {
            
            private ManagerChangeListener() {
                //Just to avoid creation of accessor class
            }
            
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getSource() != manager) {
                    return;
                }
                
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    updateCustomizerPanel();
                }
            }
        }
    }
    
    // Private methods ---------------------------------------------------------

    //!!!!!!!!!   HelpCtx !!!!!!!!!!!!!!!!
    private Node createRootNode() {
        DataFolder df = DataFolder.findFolder(FileUtil.getConfigFile(REGISTRATION_FOLDER));
        return new FNode(df.getNodeDelegate(), Children.create(new ConfigurationChildFactory(df), true));
    }

    private static class FNode extends FilterNode {
        public FNode(Node original, org.openide.nodes.Children children) {
            super(original, children);
        }
        
        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }
    }

    private static final class ConfigurationChildFactory extends ChildFactory<DataFolder> {
        private final DataFolder parent;
        private ConfigurationChildFactory(DataFolder parent) {
            this.parent = parent;
        }

        @Override
        protected boolean createKeys(List<DataFolder> toPopulate) {
            for (DataObject dob : parent.getChildren()) {
                if (dob instanceof DataFolder) {
                    toPopulate.add ((DataFolder) dob);
                }
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(DataFolder key) {
            return new FNode(key.getNodeDelegate(), Children.create(new ConfigurationChildFactory(key), true));
        }
    }
    
    private static class ConfigurationCellRenderer extends DefaultListCellRenderer {
        
        private ConfigurationCellRenderer() {
            //Just to avoid creation of accessor class
        }
        
        @Override
        public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
            super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            if (value instanceof ProjectConfiguration) {
                setText( ((ProjectConfiguration)value).getDisplayName());
            }
            return this;
        }
        
    }
}
