/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.actions;

import org.netbeans.spi.editor.AbstractEditorAction;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.EditorUtilities;
import org.openide.awt.Actions;
import org.openide.util.actions.Presenter;

/**
 * Action that represents a named editor action in main menu, popup menu
 * and editor toolbar.
 * <br/>
 * The actions are registered into "Editors/ActionPresenters" regardless
 * of the mime-type for which the actions get created.
 */
public final class PresenterEditorAction extends TextAction
        implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PropertyChangeListener, ChangeListener
{

    /**
     * Boolean action property displayed by the checkbox menu item.
     */
    private static final String SELECTED_KEY = "SwingSelectedKey"; // [TODO] Replace with "Action.SELECTED_KEY" on 1.6


    // -J-Dorg.netbeans.modules.editor.lib2.actions.PresenterEditorAction.level=FINEST
    private static final Logger LOG = Logger.getLogger(PresenterEditorAction.class.getName());

    private static final Map<PresenterEditorAction,String> presenterAction2Name
            = new WeakHashMap<PresenterEditorAction,String>();

    /**
     * Currently active editor component's editor kit reference.
     */
    private static Reference<SearchableEditorKit> activeEditorKitRef;

    private static ChangeListener kitChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent evt) {
            updateActions(null);
        }
    };

    private static SearchableEditorKit activeKit() {
        synchronized (PresenterEditorAction.class) {
            return (activeEditorKitRef != null) ? activeEditorKitRef.get() : null;
        }
    }

    private static void updateActions(SearchableEditorKit kit) {
        boolean changed = (activeEditorKitRef == null || kit != activeEditorKitRef.get());
        if (changed) {
            activeEditorKitRef = new WeakReference<SearchableEditorKit>(kit);
            for (Map.Entry<PresenterEditorAction, String> actionAndName : presenterAction2Name.entrySet()) {
                PresenterEditorAction presenterAction = actionAndName.getKey();
                String actionName = actionAndName.getValue();
                // Clear ref to old action
                presenterAction.clearDelegateActionRef();
                // Update to current delegate action (by using the given kit)
                presenterAction.delegateAction(kit, actionName);
            }
        }
    }

    private static final Action NULL_ACTION = new TextAction("null") {
        public void actionPerformed(ActionEvent evt) {
        }
    };

    private static final Reference<Action> NULL_ACTION_REF = new WeakReference<Action>(NULL_ACTION);

    static {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                    JTextComponent focusedTextComponent = (JTextComponent) evt.getNewValue();
                    TextUI ui = (focusedTextComponent != null) ? focusedTextComponent.getUI() : null;
                    EditorKit kit = (ui != null)
                            ? ui.getEditorKit(focusedTextComponent)
                            : EditorActionUtilities.getGlobalActionsKit();
                    if (kit != null) {
                        SearchableEditorKit searchableKit = EditorActionUtilities.getSearchableKit(kit);
                        updateActions(searchableKit);
                    }
                }
            }
        });
    }

    public static Action create(Map<String,?> attrs) {
        String actionName = (String)attrs.get(Action.NAME);
        if (actionName == null) {
            throw new IllegalArgumentException("Null Action.NAME attribute for attrs: " + attrs); // NOI18N
        }
        return new PresenterEditorAction(actionName, attrs);
    }

    /**
     * Corresponding action's reference.
     */
    private Reference<Action> delegateActionRef;

    private JMenuItem menuPresenter;

    private JMenuItem popupPresenter;

    private Component toolBarPresenter;

    private Map<String,?> attrs;

    public PresenterEditorAction(String actionName, Map<String,?> attrs) {
        super(actionName);
        this.attrs = attrs;
        presenterAction2Name.put(this, actionName);
    }

    public void actionPerformed(ActionEvent evt) {
        // Find the right action for the corresponding editor kit
        JTextComponent component = getTextComponent(evt);
        if (component != null) {
            TextUI ui = component.getUI();
            if (ui != null) {
                EditorKit kit = ui.getEditorKit(component);
                if (kit != null) {
                    String actionName = actionName();
                    Action action = EditorUtilities.getAction(kit, actionName);
                    if (action != null) {
                        action.actionPerformed(evt);
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Action '" + actionName + "' not found in editor kit " + kit + '\n'); // NOI18N
                        }
                    }
                }
            }
        }
    }

    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = createMenuItem(false);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getMenuPresenter() for action=" + actionName() + " returns " + menuPresenter); // NOI18N
        }
        return menuPresenter;
    }

    public JMenuItem getPopupPresenter() {
        if (popupPresenter == null) {
            popupPresenter = createMenuItem(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getPopupPresenter() for action=" + actionName() + " returns " + popupPresenter); // NOI18N
        }
        return popupPresenter;
    }

    public Component getToolbarPresenter() {
        if (toolBarPresenter == null) {
            toolBarPresenter = new JButton(this);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getToolbarPresenter() for action=" + actionName() + " returns " + toolBarPresenter); // NOI18N
        }
        return toolBarPresenter;
    }

    @Override
    public Object getValue(String key) {
        Object value = super.getValue(key);
        if (value == null) {
            if (!"instanceCreate".equals(key)) { // Return null for this key
                value = attrs.get(key);
                if (value == null) {
                    Action delegateAction = delegateAction();
                    if (delegateAction != null) {
                        value = delegateAction.getValue(key);
                    }
                }
            }
        }
        return value;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (SELECTED_KEY.equals(propertyName)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("propertyChange() of SELECTED_KEY for action " + actionName());
            }
            updateSelected();
        }
    }

    private void updateSelected() {
        if (isCheckBox()) {
            boolean selected = isSelected();
            if (menuPresenter instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)menuPresenter).setSelected(selected);
            }
            if (popupPresenter instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)popupPresenter).setSelected(selected);
            }
        }
    }

    public void stateChanged(ChangeEvent evt) {
        clearDelegateActionRef();
    }

    private boolean isSelected() {
        Action action = delegateAction();
        boolean selected = (action != null) && Boolean.TRUE.equals(action.getValue(SELECTED_KEY));
        return selected;
    }

    private JMenuItem createMenuItem(boolean isPopup) {
        final JMenuItem menuItem;
        if (isCheckBox()) {
            menuItem = new JCheckBoxMenuItem();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Create checkbox menu item for action " + actionName() + ", selected=" + isSelected());
            }
            menuItem.setSelected(isSelected());
            menuItem.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    boolean checkboxSelected = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
                    boolean actionSelected = isSelected();
                    if (checkboxSelected != actionSelected) {
                        Action delegateAction = delegateAction();
                        if (delegateAction != null) {
                            delegateAction.putValue(SELECTED_KEY, checkboxSelected);
                        }
                    }
                }
            });

        } else { // Regular menu item
            menuItem = new JMenuItem();
        }
        Actions.connect(menuItem, this, isPopup);
        return menuItem;
    }

    private boolean isCheckBox() {
        String presenterType = (String) getValue("PresenterType");
        return "CheckBox".equals(presenterType);
    }

    String actionName() {
        return (String) getValue(Action.NAME); // should be non-null (check by constructor)
    }

    Action delegateAction() {
        return delegateAction(null, null);
    }

    Action delegateAction(SearchableEditorKit searchableKit, String actionName) {
        synchronized (this) {
            if (delegateActionRef == null) {
                if (actionName == null) {
                    actionName = actionName();
                }
                if (searchableKit == null) {
                    EditorKit globalKit = EditorActionUtilities.getGlobalActionsKit();
                    searchableKit = (globalKit != null) ? EditorActionUtilities.getSearchableKit(globalKit) : null;
                    if (searchableKit == null) {
                        return null;
                    }
                }
                Action delegateAction = searchableKit.getAction(actionName);
                if (delegateAction != null) {
                    delegateActionRef = new WeakReference<Action>(delegateAction);
                    delegateAction.addPropertyChangeListener(this);
                    setEnabled(delegateAction.isEnabled());
                } else {
                    delegateActionRef = NULL_ACTION_REF;
                    setEnabled(false);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Action '" + actionName + "' not found in global editor kit " + searchableKit + '\n'); // NOI18N
                    }
                }
                updateSelected();
            }
            return (delegateActionRef != NULL_ACTION_REF)
                    ? delegateActionRef.get()
                    : null;
        }
    }

    private void clearDelegateActionRef() {
        synchronized (this) {
            if (delegateActionRef != null && delegateActionRef != NULL_ACTION_REF) {
                Action oldDelegateAction = delegateActionRef.get();
                if (oldDelegateAction != null) {
                    oldDelegateAction.removePropertyChangeListener(this);
                }
            }
            delegateActionRef = null;
        }
        
    }

}
