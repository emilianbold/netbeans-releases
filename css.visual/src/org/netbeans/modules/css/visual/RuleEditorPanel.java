/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.filters.FilterSubmenuAction;
import org.netbeans.modules.css.visual.filters.FiltersManager;
import org.netbeans.modules.css.visual.filters.FiltersSettings;
import org.netbeans.modules.css.visual.filters.RuleEditorFilters;
import org.netbeans.modules.css.visual.filters.SortActionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Rule editor panel is a {@link JPanel} component which can be embedded in 
 * the client's UI.
 * 
 * It can be controlled and observed via {@link RuleEditorPanelController} 
 * and {@link PropertyChangeListener}.
 *
 * Open questions/todo-s:
 * -----------------------
 * 
 * 2) (P4) related to #1 is how to listen on events happening over the sheet - implementing
 *    the mouse hover based "disable" action (maybe not necessary since doesn't make
 *    much sense for the rule editor).
 * 
 * 3) (P2) add own (propagate the filters) popup menu to the sheet
 * 
 * 4) (P4) (#EA) can property categories be programmatically collapsed/expanded?
 * 
 * 5) (P3) in the unsorted mode, can be the categories disabled? They seem to disappear only 
 *    in the "sort by alpha" mode
 * 
 * Enhancements:
 * --------------
 * A) if categorized view enabled, the category name containing a physical properties could be in bold font
 *    and the rest is collapsed (possibly configurable by a toolbar toggle)
 * 
 * @author marekfukala
 */
@NbBundle.Messages({
        "titleLabel.text={0} properties",
        "label.rule.error.tooltip=The selected rule contains error(s), the lister properties are read only",
        "label.add.property=Add Property"
})
public class RuleEditorPanel extends JPanel {

    private static final Icon ERROR_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/error-glyph.gif")); //NOI18N
    private static final JLabel ERROR_LABEL = new JLabel(ERROR_ICON);
    static {
        ERROR_LABEL.setToolTipText(Bundle.label_rule_error_tooltip());
    }
    
    private static final Color defaultPanelBackground = javax.swing.UIManager.getDefaults().getColor("Panel.background"); //NOI18N
    
    private PropertySheet sheet;
    
    private Model model;
    private Rule rule;
    private Action[] actions;
    private RuleEditorFilters filters;
    private boolean showAllProperties, showCategories;
    private SortMode sortMode;
    
    private RuleNode node;
    
    private PropertyChangeSupport CHANGE_SUPPORT = new PropertyChangeSupport(this);
    
    private boolean addPropertyMode;
    /**
     * Creates new form RuleEditorPanel
     */
    public RuleEditorPanel() {
        this(false);
    }
    
    private RuleEditorPanel(boolean addPropertyMode) {
        
        this.addPropertyMode = addPropertyMode;
        FiltersSettings filtersSettings = addPropertyMode
                ? new FiltersSettings(false, false, true)
                : new FiltersSettings();
        
        node = new RuleNode(this);
        
        sortMode = SortMode.NATURAL;
        
        filters = new RuleEditorFilters( this, filtersSettings );
        filters.getInstance().hookChangeListener(new FiltersManager.FilterChangeListener() {

            @Override
            public void filterStateChanged(ChangeEvent e) {
                updateFiltersPresenters();
            }
            
        });
        
        actions = new Action[] {            
            new SortActionSupport.NaturalSortAction( filters ),
            new SortActionSupport.AlphabeticalSortAction( filters ),
            null,
            new FilterSubmenuAction(filters)            
        };
        
        //init default components
        initComponents();
        
        addPropertyButton.setVisible(!addPropertyMode);
        
        titleLabel.setText(null);
        
        //add the property sheet to the center
        sheet = new PropertySheet();
        try {
            sheet.setSortingMode(PropertySheet.UNSORTED);
        } catch (PropertyVetoException ex) {
            //no-op
        }
        sheet.setPopupEnabled(false);
        sheet.setDescriptionAreaVisible(false);
        sheet.setNodes(new Node[]{node});
        
        add(sheet, BorderLayout.CENTER);
        
        northPanel.add(filters.getComponent(), BorderLayout.EAST);
        
        updateFiltersPresenters();
    }
    
    private void updateFiltersPresenters() {
        if(filters.getSettings().isShowCategoriesEnabled()) {
            setShowCategories(filters.getInstance().isSelected(RuleEditorFilters.SHOW_CATEGORIES));
        }
        if(filters.getSettings().isShowAllPropertiesEnabled()) {
            setShowAllProperties(filters.getInstance().isSelected(RuleEditorFilters.SHOW_ALL_PROPERTIES));
        }
    }

    public boolean isAddPropertyMode() {
        return addPropertyMode;
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(SortMode mode) {
        if(this.sortMode == mode) {
            return ; //no change
        }
        this.sortMode = mode;
        node.fireContextChanged();
    }

    public boolean isShowAllProperties() {
        return showAllProperties;
    }

    public void setShowAllProperties(boolean showAllProperties) {
        if(this.showAllProperties == showAllProperties) {
            return ; //no change
        }
        this.showAllProperties = showAllProperties;
        node.fireContextChanged();
    }

    public boolean isShowCategories() {
        return showCategories;
    }

    public void setShowCategories(boolean showCategories) {
        if(this.showCategories == showCategories) {
            return ; //no change
        }
        this.showCategories = showCategories;
        node.fireContextChanged();
    }

    public Model getModel() {
        return model;
    }
    
    public void setModel(Model model) {
        if(this.model == model) {
            return ; //no change
        }
        Model oldModel = this.model;
        Rule oldRule = this.rule;
        
        this.model = model;
        this.rule = null;
        
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.MODEL_SET.name(), oldModel, this.model);
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, this.rule);
        
        //do not fire change event since it is required
        //to call setRule(...) subsequently which will 
        //fire the change even
    }

    public Rule getRule() {
        return rule;
    }
    
    public void setRule(final Rule rule) {
        if(model == null) {
            throw new IllegalStateException("you must call setModel(Model model) beforehand!"); //NOI18N
        }
        if(this.rule == rule) {
            return; //no change
        }
        Rule old = this.rule;
        this.rule = rule;
        
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, this.rule);
        
        //check if the rule is valid
        if(!rule.isValid()) {
            northPanel.add(ERROR_LABEL, BorderLayout.WEST);
            //the component returns different RGB color that is really pained, at least on motif
            Color npc = northPanel.getBackground();
            Color bitMoreRed = new Color(
                Math.min(255, npc.getRed() + 6), 
                Math.max(0, npc.getGreen() - 3), 
                Math.max(0, npc.getBlue() - 3));
            northPanel.setBackground(bitMoreRed);
            
            addPropertyButton.setEnabled(false);
        } else {
            northPanel.remove(ERROR_LABEL);
            northPanel.setBackground(defaultPanelBackground);
            
            addPropertyButton.setEnabled(true);
        }
        
        node.fireContextChanged();
        
        final AtomicReference<String> ruleNameRef = new AtomicReference<String>();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet stylesheet) {
                ruleNameRef.set(model.getElementSource(rule.getSelectorsGroup()).toString());
            }
        });
        
        titleLabel.setText(Bundle.titleLabel_text(ruleNameRef.get()));
    }
    
    public void setNoRuleState() {
        this.rule = null;
        addPropertyButton.setEnabled(false);
        node.fireContextChanged();
    }
    
    /**
     * Registers an instance of {@link PropertyChangeListener} to the component.
     * @param listener
     */
    public void addRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }
    /**
     * Unregisters an instance of {@link PropertyChangeListener} from the component.
     * @param listener
     */
    public void removeRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.removePropertyChangeListener(listener);
    }
    
    public Action[] getActions() {
        return actions;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        northPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        southPanel = new javax.swing.JPanel();
        addPropertyButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        northPanel.setLayout(new java.awt.BorderLayout());
        northPanel.add(titleLabel, java.awt.BorderLayout.CENTER);

        add(northPanel, java.awt.BorderLayout.NORTH);

        southPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        southPanel.setLayout(new java.awt.BorderLayout());

        addPropertyButton.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.addPropertyButton.text")); // NOI18N
        addPropertyButton.setEnabled(false);
        addPropertyButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addPropertyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPropertyButtonActionPerformed(evt);
            }
        });
        southPanel.add(addPropertyButton, java.awt.BorderLayout.WEST);

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void addPropertyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPropertyButtonActionPerformed
        
        //use the default rule editor panel with some modifications
        final RuleEditorPanel addPropertyPanel = new RuleEditorPanel(true);
        addPropertyPanel.setModel(model);
        addPropertyPanel.setRule(rule);
        addPropertyPanel.setShowAllProperties(true);
        addPropertyPanel.setShowCategories(true);
        
        addPropertyPanel.updateFiltersPresenters();
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(
                new DialogDescriptor(addPropertyPanel, Bundle.label_add_property(), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if("OK".equals(e.getActionCommand())) { 
                    addPropertyPanel.node.applyModelChanges();
                }
            }
        }));
        
        dialog.setVisible(true);
        
    }//GEN-LAST:event_addPropertyButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPropertyButton;
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel southPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
