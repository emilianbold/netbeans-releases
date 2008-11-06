/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * ConfigurationsSelectionPanelGUI.java
 *
 * Created on 17. May 2005, 14:15
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.mobility.project.ui.customizer.NewConfigurationPanel;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.CategoryDescriptor;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.Descriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Adam Sotona
 */
public final class ConfigurationsSelectionPanelGUI extends JPanel implements ExplorerManager.Provider {
    private final ExplorerManager manager = new ExplorerManager();
    private final TreeTableView treeView;
    private final Set<String> bannedNames;
    private Set<ConfigurationTemplateDescriptor> selection = new HashSet();
    private HashSet<ChangeListener> listeners = new HashSet();
    
    /** Creates new form ConfigurationsSelectionPanelGUI */
    public ConfigurationsSelectionPanelGUI() {
        this(Collections.EMPTY_SET);
    }
    
    public ConfigurationsSelectionPanelGUI(Set<String> bannedNames) {
        this.bannedNames = bannedNames;
        initComponents();
        treeView = new TreeTableView();
        jLabel1.setLabelFor(treeView);
        treeView.setPopupAllowed(false);
        treeView.setRootVisible(false);
        treeView.setDefaultActionAllowed(false);
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeView.setProperties(new Property[]{ new SelectedProp (null, null)});
        templatesPanel.add(treeView, BorderLayout.CENTER);
        Node root = new AbstractNode (Children.create(new RootChildren(), true));
        root.setName(NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_CfgSelectionPanel_Templates")); //NOI18N
        manager.setRootContext(root);
        treeView.setPreferredSize(new Dimension(480, 350));
        treeView.setTreePreferredWidth(420);
        treeView.setTableColumnPreferredWidth(0, 60);
    }

    private final class RootChildren extends ChildFactory<ProjectConfigurationFactory> {
        @Override
        protected Node createNodeForKey(ProjectConfigurationFactory key) {
            ProjectConfigurationFactory.CategoryDescriptor cat = key.getRootCategory();
            return cat == null ? null : new CategoryNode (key.getRootCategory());
        }

        @Override
        protected boolean createKeys(List<ProjectConfigurationFactory> toPopulate) {
            toPopulate.addAll(Lookup.getDefault().lookupAll(ProjectConfigurationFactory.class));
            return true;
        }
    }

    private final class CategoryNode extends AbstractNode {
        public CategoryNode(final ProjectConfigurationFactory.CategoryDescriptor cat) {
            super (Children.create(new CategoryChildren(cat), true));
            setDisplayName(cat.getDisplayName());
        }
  
        @Override
        public Image getIcon(int type) {
            return NewConfigurationPanel.CLOSED_ICON == null ? super.getIcon(type) : NewConfigurationPanel.CLOSED_ICON;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return NewConfigurationPanel.OPENED_ICON == null ? super.getOpenedIcon(type) : NewConfigurationPanel.OPENED_ICON;
        }
  }

    private final class CategoryChildren extends ChildFactory<ProjectConfigurationFactory.Descriptor> {
        private final CategoryDescriptor cat;
        CategoryChildren(CategoryDescriptor cat) {
            this.cat = cat;
        }

        @Override
        protected Node createNodeForKey(Descriptor key) {
            if (key instanceof CategoryDescriptor) {
                return new CategoryNode ((CategoryDescriptor) key);
            } else if (key instanceof ConfigurationTemplateDescriptor) {
                ConfigurationTemplateDescriptor desc = (ConfigurationTemplateDescriptor) key;
                if (Utilities.isJavaIdentifier(desc.getCfgName())) {
                    return new TemplateNode (desc);
                }
            }
            return null;
        }

        @Override
        protected boolean createKeys(List<Descriptor> toPopulate) {
            toPopulate.addAll (cat.getChildren());
            return true;
        }

    }
    
    private final class TemplateNode extends AbstractNode {
        private ConfigurationTemplateDescriptor cfgTmp;
        public TemplateNode(ConfigurationTemplateDescriptor cfgTmp) {
            super(Children.LEAF);
            this.cfgTmp = cfgTmp;
            setDisplayName(cfgTmp.getDisplayName().equals(cfgTmp.getCfgName()) ? cfgTmp.getDisplayName() : NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_CfgSlePanel_TemplateNodePattern", cfgTmp.getDisplayName(), cfgTmp.getCfgName())); //NOI18N
        }
        
        @Override
        protected Sheet createSheet() {
            Sheet s = Sheet.createDefault();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            ss.put(new SelectedProp(cfgTmp, selection));
            return s;
        }
    }

    private final class SelectedProp extends PropertySupport.ReadWrite<Boolean> {
        private final ConfigurationTemplateDescriptor cfgTmp;
        private Set<ConfigurationTemplateDescriptor> selection;
        private SelectedProp (ConfigurationTemplateDescriptor cfgTmp, Set<ConfigurationTemplateDescriptor> selection) {
            this (cfgTmp, selection, NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_CfgSelectionPanel_Selection"));
        }
        private SelectedProp (ConfigurationTemplateDescriptor cfgTmp, Set<ConfigurationTemplateDescriptor> selection, String name) {
            super ("selection", Boolean.class, name, name);
            this.selection = selection;
            this.cfgTmp = cfgTmp;
        }

        public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
            return selection == null ? Boolean.TRUE : selection.contains(cfgTmp);
        }

        public void setValue(Boolean val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (val) {
                selection.add(cfgTmp);
            } else {
                selection.remove(cfgTmp);
            }
            fireChange();
        }
    }

    private final ChangeSupport change = new ChangeSupport(this);
    
    private void fireChange() {
        change.fireChange();
    }
    
    public void addChangeListener(ChangeListener l) {
        change.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        change.removeChangeListener(l);
    }
    
    public boolean valid() {
        //Note - changed from isValid() to avoid accidental override
        HashSet<String> names = new HashSet(bannedNames);
        for (ConfigurationTemplateDescriptor cfg : selection) {
            if (!names.add(cfg.getCfgName())) return false;
        }
        return true; 
    }
    
     public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public void setSelectedTemplates(Set<ConfigurationTemplateDescriptor> selected) {
        this.selection = selected;
    }
    
    public Set<ConfigurationTemplateDescriptor> getSelectedTemplates() {
        return selection;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        templatesPanel = new javax.swing.JPanel();

        setName(NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "TITLE_ConfigurationsSelection")); // NOI18N
        setPreferredSize(new java.awt.Dimension(560, 350));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(templatesPanel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "LBL_ConfigurationsSelection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jLabel1, gridBagConstraints);

        templatesPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        templatesPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(templatesPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "ACSN_CfgSelectionPanel_Selection")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigurationsSelectionPanelGUI.class, "ACSD_CfgSelectionPanel_Selection")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel templatesPanel;
    // End of variables declaration//GEN-END:variables
}
