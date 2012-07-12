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
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.RuleEditorListener;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.filters.FilterSubmenuAction;
import org.netbeans.modules.css.visual.filters.FiltersManager;
import org.netbeans.modules.css.visual.filters.RuleEditorFilters;
import org.netbeans.modules.css.visual.filters.SortActionSupport;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Rule editor panel is a {@link JPanel} component which can be embedded in 
 * the client's UI.
 * 
 * It can be controlled and observed via {@link RuleEditorPanelController} 
 * and {@link RuleEditorListener}.
 *
 * Open questions/todo-s:
 * -----------------------
 * 1) (P3) how to change the paint color of the property *keys*? (existing properties should
 *    be bolded, the unused in plain font.
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
@NbBundle.Messages(
        "titleLabel.text={0} properties"
)
public class RuleEditorPanel extends JPanel {

    private PropertySheet sheet;
    
    private Model model;
    private Rule rule;
    private Action[] actions;
    private RuleEditorFilters filters;
    private boolean showAllProperties, showCategories;
    private SortMode sortMode;
            
    private Collection<RuleEditorListener> LISTENERS
            = Collections.synchronizedCollection(new ArrayList<RuleEditorListener>());
    
    /**
     * Creates new form RuleEditorPanel
     */
    public RuleEditorPanel() {
        sortMode = SortMode.NATURAL;
        
        filters = new RuleEditorFilters( this );
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
        
        add(sheet, BorderLayout.CENTER);
        
        //add the filters panel
        northPanel.add(filters.getComponent(), BorderLayout.EAST);
        
        updateFiltersPresenters();
    }
    
    private void updateFiltersPresenters() {
        setShowCategories(filters.getInstance().isSelected(RuleEditorFilters.SHOW_CATEGORIES));
        setShowAllProperties(filters.getInstance().isSelected(RuleEditorFilters.SHOW_ALL_PROPERTIES));
    }

    private void resetSheetNode() {
        sheet.setNodes(new Node[]{new RuleNode(model, rule, showAllProperties, showCategories, sortMode)});
    }
    
    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        
        resetSheetNode();
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setShowAllProperties(boolean showAllProperties) {
        if(this.showAllProperties == showAllProperties) {
            return ; //no change
        }
        
        this.showAllProperties = showAllProperties;
        
        if(rule != null) {
            resetSheetNode();
        }
    }

    public void setShowCategories(boolean showCategories) {
        if(this.showCategories == showCategories) {
            return ; //no change
        }
        
        this.showCategories = showCategories;
        
        if(rule != null) {
            //re-set the node
             resetSheetNode();
        }
    }
    
    public void setModel(Model model) {
        this.model = model;
        this.rule = null;
    }
    
    public void setRule(Rule r) {
        if(model == null) {
            throw new IllegalStateException("you must call setModel(Model model) beforehand!"); //NOI18N
        }
        this.rule = r;
        
        resetSheetNode();
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
        sheet.setNodes(null);
        //TODO - show some 'no rule selected' message
    }
    
    /**
     * Registers an instance of {@link RuleEditorListener} to the component.
     * @param listener
     * @return true if the listeners list changed
     */
    public boolean addRuleEditorListener(RuleEditorListener listener) {
        return LISTENERS.add(listener);
    }
    /**
     * Unregisters an instance of {@link RuleEditorListener} from the component.
     * @param listener
     * @return true if the listeners list changed (listener removed)
     */
    public boolean removeRuleEditorListener(RuleEditorListener listener) {
        return LISTENERS.remove(listener);
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
        southPanel.add(addPropertyButton, java.awt.BorderLayout.WEST);

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPropertyButton;
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel southPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
