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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.actions.AddPropertyAction;
import org.netbeans.modules.css.visual.actions.CreateRuleAction;
import org.netbeans.modules.css.visual.actions.DeleteRuleAction;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.filters.FilterSubmenuAction;
import org.netbeans.modules.css.visual.filters.FiltersManager;
import org.netbeans.modules.css.visual.filters.FiltersSettings;
import org.netbeans.modules.css.visual.filters.RuleEditorFilters;
import org.netbeans.modules.css.visual.filters.SortActionSupport;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

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
        "label.rule.error.tooltip=The selected rule contains error(s), the listed properties are read only"
})
public class RuleEditorPanel extends JPanel {
    
    private RequestProcessor RP = new RequestProcessor(CssCaretAwareSourceTask.class);

    
    public static final String RULE_EDITOR_LOGGER_NAME = "rule.editor"; //NOI18N
    
    private static final Logger LOG = Logger.getLogger(RULE_EDITOR_LOGGER_NAME);

    private static final Icon ERROR_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/error-glyph.gif")); //NOI18N
    private static final Icon APPLIED_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/database.gif")); //NOI18N
    
    private static final JLabel ERROR_LABEL = new JLabel(ERROR_ICON);
    private static final JLabel APPLIED_LABEL = new JLabel(APPLIED_ICON);
    static {
        ERROR_LABEL.setToolTipText(Bundle.label_rule_error_tooltip());
    }
    
//    private static final Color defaultPanelBackground = javax.swing.UIManager.getDefaults().getColor("Panel.background"); //NOI18N
    
    private PropertySheet sheet;
    
    private Model model;
    private Rule rule;
    
    private Action addPropertyAction;
    private Action addRuleAction;
    private Action removeRuleAction;
    
    private Action[] actions;
    private JPopupMenu popupMenu;
    
    private RuleEditorFilters filters;
    private boolean showAllProperties, showCategories;
    private SortMode sortMode;
    
    public RuleNode node;
    
    private PropertyChangeSupport CHANGE_SUPPORT = new PropertyChangeSupport(this);
    
    private boolean addPropertyMode;
    
    private PropertyChangeListener MODEL_LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(Model.CHANGES_APPLIED_TO_DOCUMENT.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        northWestPanel.add(APPLIED_LABEL);    
                        northWestPanel.revalidate();
                        northWestPanel.repaint();
                    }
                });
                //re-set the css model as the CssCaretAwareSourceTask won't work 
                //if the modified file is not opened in editor
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        Model model = getModel();
                        if(model != null) {
                            Document doc = model.getLookup().lookup(Document.class);
                            if(doc != null) {
                                try {
                                    Source source = Source.create(doc);
                                    ParserManager.parse(Collections.singleton(source), new UserTask() {

                                        @Override
                                        public void run(ResultIterator resultIterator) throws Exception {
                                            resultIterator = WebUtils.getResultIterator(resultIterator, "text/css");
                                            if(resultIterator != null) {
                                                CssCslParserResult result = (CssCslParserResult)resultIterator.getParserResult();
                                                final Model model = result.getModel();
                                                SwingUtilities.invokeLater(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        LOG.info("Setting new model upon Model.applyChanges()");
                                                        setModel(model);
                                                    }
                                                    
                                                });
                                            }
                                        }
                                    });
                                } catch (ParseException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                    
                });
            }
        }
    };
    
    /**
     * Creates new form RuleEditorPanel
     */
    public RuleEditorPanel() {
        this(false);
    }
    
    public RuleEditorPanel(boolean addPropertyMode) {
        
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
        
        //initialize actions
        addPropertyAction = new AddPropertyAction(this);
        addRuleAction = new CreateRuleAction(this);
        removeRuleAction = new DeleteRuleAction(this);
        
        //keep actions status
        addRuleEditorListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(RuleEditorController.PropertyNames.MODEL_SET.name())) {
                    addRuleAction.setEnabled(evt.getNewValue() != null);
                } else if (evt.getPropertyName().equals(RuleEditorController.PropertyNames.RULE_SET.name())) {
                    addPropertyAction.setEnabled(evt.getNewValue() != null);
                    removeRuleAction.setEnabled(evt.getNewValue() != null);
                }
            }
        });
        
        actions = new Action[] {            
            addPropertyAction,
            addRuleAction,
            removeRuleAction,
            null,
            new SortActionSupport.NaturalSortAction( filters ),
            new SortActionSupport.AlphabeticalSortAction( filters ),
            null,
            new FilterSubmenuAction(filters)            
        };
        
        //custom popop for the whole panel
        //TODO possibly use some NB way, but I don't know it, no time for exploring now...
        JPopupMenu pm = new JPopupMenu();
        for(Action action : actions) {
            if(action != null) {
                if(action instanceof Presenter.Popup) {
                    pm.add(((Presenter.Popup)action).getPopupPresenter());
                } else {
                    pm.add(action);
                }
            } else {
                pm.addSeparator();
            }
        }
        
        setComponentPopupMenu(pm);
        
        //custom popup for the "menu icon"
        popupMenu = new JPopupMenu();
        popupMenu.add(addPropertyAction);
        popupMenu.add(addRuleAction);
        popupMenu.add(removeRuleAction);
                
        //init default components
        initComponents();
        
        if(!addPropertyMode) {
            northEastPanel.add(menuLabel, java.awt.BorderLayout.EAST);
            menuLabel.setComponentPopupMenu(popupMenu);
        }
        
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
        
        northEastPanel.add(filters.getComponent(), BorderLayout.WEST);
        
        updateFiltersPresenters();
        
    }
    
    public final void updateFiltersPresenters() {
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
    
    //runs in EDT
    public void setModel(final Model model) {
        LOG.log(Level.FINE, "setModel({0})", model);
        
        if(model == null) {
            throw new NullPointerException();
        }
        
        assert SwingUtilities.isEventDispatchThread();
        if(this.model == model) {
            LOG.log(Level.FINE, "no update - attempt to set the same model");
            return ; //no change
        }
        
        if(this.model != null) {
            this.model.removePropertyChangeListener(MODEL_LISTENER);
        }
        
        final Model oldModel = this.model;
        final Rule oldRule = this.rule;
        
        this.model = model;
        
        this.model.addPropertyChangeListener(MODEL_LISTENER);
        
        //remove the "applied changes mark"
        northWestPanel.remove(APPLIED_LABEL);
        northWestPanel.validate();
        northWestPanel.repaint();
        
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.MODEL_SET.name(), oldModel, this.model);
        
        if(this.rule != null) {
            //try to resolve the old rule from the previous model to corresponding
            //rule in the new model
            final AtomicReference<CharSequence> oldRuleId_ref = new AtomicReference<CharSequence>();
            oldModel.runReadTask(new Model.ModelTask() {
                @Override
                public void run(StyleSheet styleSheet) {
                    oldRuleId_ref.set(oldModel.getElementSource(oldRule.getSelectorsGroup()));
                }
            });
            final CharSequence oldRuleId = oldRuleId_ref.get();
            
            final AtomicReference<Rule> match_ref = new AtomicReference<Rule>();
            model.runReadTask(new Model.ModelTask() {

                @Override
                public void run(StyleSheet styleSheet) {
                    styleSheet.accept(new ModelVisitor.Adapter() {
                        @Override
                        public void visitRule(Rule rule) {
                            CharSequence ruleId = model.getElementSource(rule.getSelectorsGroup());
                            if(LexerUtils.equals(oldRuleId, ruleId, false, false)) {
                                //should be the same rule

                                //TODO - having some API for resolving old to new model elements between
                                //two model instances would be great. Something like ElementHandle.resolve
                                //TODO - the handles would be usefull as well as the elements shouldn't 
                                //be kept outside of the ModelTask-s.

                                LOG.log(Level.FINE, "found matching rule {0}", rule);
                                match_ref.set(rule);

                            }
                        }
                    });
                }
            });
            
            Rule match = match_ref.get();
            if(match == null) {
                setNoRuleState();
            } else {
                setRule(match_ref.get());
            }
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, match);
            
            
        } else {
            LOG.log(Level.FINE, "no rule was set before");
            //no rule was set - fire event anyway
            CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), oldRule, rule);
        }
        
        //do not fire change event since it is required
        //to call setRule(...) subsequently which will 
        //fire the change even
    }

    public Rule getRule() {
        return rule;
    }
    
    public void setRule(final Rule rule) {
        LOG.log(Level.FINE, "setRule({0})", rule);
        
        assert SwingUtilities.isEventDispatchThread();
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
            northWestPanel.add(ERROR_LABEL);
            addPropertyButton.setEnabled(false);
        } else {
            northWestPanel.remove(ERROR_LABEL);
            addPropertyButton.setEnabled(true);
        }
        northWestPanel.revalidate();
        
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
        LOG.log(Level.FINE, "setNoRuleState()");
        
        assert SwingUtilities.isEventDispatchThread();
        Rule old = this.rule;
        this.rule = null;
        CHANGE_SUPPORT.firePropertyChange(RuleEditorController.PropertyNames.RULE_SET.name(), old, null);
        
        titleLabel.setText(null);
        addPropertyButton.setEnabled(false);
        node.fireContextChanged();
    }
    
    
    public void setDeclarationInfo(Declaration declaration, DeclarationInfo declarationInfo) {
        node.fireDeclarationInfoChanged(declaration, declarationInfo);
    }
    
    /**
     * Registers an instance of {@link PropertyChangeListener} to the component.
     * @param listener
     */
    public final void addRuleEditorListener(PropertyChangeListener listener) {
        CHANGE_SUPPORT.addPropertyChangeListener(listener);
    }
    /**
     * Unregisters an instance of {@link PropertyChangeListener} from the component.
     * @param listener
     */
    public final void removeRuleEditorListener(PropertyChangeListener listener) {
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

        menuLabel = new javax.swing.JLabel();
        northPanel = new javax.swing.JPanel();
        northEastPanel = new javax.swing.JPanel();
        northWestPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        southPanel = new javax.swing.JPanel();
        addPropertyButton = new javax.swing.JButton();

        menuLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/menu.png"))); // NOI18N
        menuLabel.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.menuLabel.text")); // NOI18N
        menuLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 0, 0));
        menuLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuLabelMouseClicked(evt);
            }
        });

        setLayout(new java.awt.BorderLayout());

        northPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        northPanel.setLayout(new java.awt.BorderLayout());

        northEastPanel.setLayout(new java.awt.BorderLayout());
        northPanel.add(northEastPanel, java.awt.BorderLayout.EAST);

        northWestPanel.setLayout(new javax.swing.BoxLayout(northWestPanel, javax.swing.BoxLayout.LINE_AXIS));

        titleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        titleLabel.setMaximumSize(null);
        titleLabel.setPreferredSize(new java.awt.Dimension(100, 16));
        northWestPanel.add(titleLabel);

        northPanel.add(northWestPanel, java.awt.BorderLayout.CENTER);

        add(northPanel, java.awt.BorderLayout.NORTH);

        southPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        southPanel.setLayout(new java.awt.BorderLayout());

        addPropertyButton.setAction(addPropertyAction);
        addPropertyButton.setText(org.openide.util.NbBundle.getMessage(RuleEditorPanel.class, "RuleEditorPanel.addPropertyButton.text")); // NOI18N
        addPropertyButton.setEnabled(false);
        addPropertyButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        southPanel.add(addPropertyButton, java.awt.BorderLayout.WEST);

        add(southPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void menuLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuLabelMouseClicked
        //just invoke popup as if right-clicked
        popupMenu.show(menuLabel, 0,0);
    }//GEN-LAST:event_menuLabelMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPropertyButton;
    private javax.swing.JLabel menuLabel;
    private javax.swing.JPanel northEastPanel;
    private javax.swing.JPanel northPanel;
    private javax.swing.JPanel northWestPanel;
    private javax.swing.JPanel southPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
