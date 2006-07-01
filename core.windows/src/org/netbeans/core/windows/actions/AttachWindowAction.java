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

package org.netbeans.core.windows.actions;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.awt.JMenuPlus;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;


/**
 * Action which allows to change layout of windows.
 *
 * @author   Peter Zavadsky
 */
public class AttachWindowAction extends AbstractAction 
implements Presenter.Menu {


    // For this class only.
    private static final String PROP_ACTION_CHANGED = "actionChanged"; // NOI18N
   
    private final PropertyChangeListener propListener;
    
    
    public AttachWindowAction() {
        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if(TopComponent.Registry.PROP_ACTIVATED.equals(propName)
                 || WindowManager.PROP_MODES.equals(propName)
                 || WindowManagerImpl.PROP_EDITOR_AREA_STATE.equals(propName)) {
                    updateState();
                }
            }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));
        
        WindowManagerImpl.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(propListener, WindowManagerImpl.getInstance()));
        
        updateState();
    }
    
    public void actionPerformed(ActionEvent evt) {}
    
    private void updateState() {
        TopComponent active = TopComponent.getRegistry().getActivated();
        Object param = active == null ? "" : active.getName(); // NOI18N
        putValue(Action.NAME, NbBundle.getMessage(MaximizeWindowAction.class, "CTL_AttachWindowAction", param));

        // XXX In separated state, the action should be present,
        // when achieved that, remove this kind of code.
        if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
            setEnabled(false);
        } else {
            setEnabled(active != null);
        }

        // So the presenter knows when to update itself.
        firePropertyChange(PROP_ACTION_CHANGED, null, null);
    }


    /** Implements <code>Presenter.Menu</code>. */
    public JMenuItem getMenuPresenter() {
        JMenuItem mi = new LazyPopup(this);
        org.openide.awt.Actions.connect(mi, this, false);
        return mi;
    }
    
    
    /** Menu item which will create its items lazilly when the popup will becomming visible.
     * Performance savings.*/
    private static class LazyPopup extends JMenuPlus implements PropertyChangeListener {
        
        /** Indicates whether menu items were created. */
        private boolean created = false;

        
        /** Constructor. */
        public LazyPopup(Action action) {
            setText((String)action.getValue(Action.NAME));
            
            action.addPropertyChangeListener(WeakListeners.propertyChange(this, action));
        }

        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            if(!created) {
                createMenuItems();
            }
            
            return super.getPopupMenu();
        }

        /** Creates items when actually needed. */
        private void createMenuItems() {
            created = true;
            removeAll();

            ModeImpl editorMode = (ModeImpl)WindowManagerImpl.getInstance().findMode("editor"); // NOI18N
            if(editorMode != null) {
                add(new ModeMenu(editorMode, NbBundle.getMessage(AttachWindowAction.class, "CTL_Documents"), true));
                add(new JSeparator());
            }

            Set modeSet = WindowManagerImpl.getInstance().getModes();
            
            for(Iterator it = modeSet.iterator(); it.hasNext(); ) {
                ModeImpl mode = (ModeImpl)it.next();
                
                if("editor".equals(mode.getName())) { // NOI18N
                    continue;
                }

                if(!mode.getOpenedTopComponents().isEmpty()) {
                    TopComponent tc = mode.getSelectedTopComponent();
                    if(tc != null) {
                        String name = tc.getName();
                        add(new ModeMenu(mode, name == null ? "null" : name, false)); // NOI18N
                    }
                }
            }
        }

        /** Implements <code>PropertyChangeListener</code>. */
        public void propertyChange(PropertyChangeEvent evt) {
            if(Action.NAME.equals(evt.getPropertyName())) {
                setText((String)evt.getNewValue());
            } else if(PROP_ACTION_CHANGED.equals(evt.getPropertyName())) { // NOI18N
                created = false;
            }
        }
    } // End of class LazyPopup.

    
    /** Sub menu for each mode. */
    private static class ModeMenu extends JMenu {
        public ModeMenu(ModeImpl mode, String name, boolean editor) {
            super(name);

            TopComponent active = TopComponent.getRegistry().getActivated();
            if(mode.canContain(active)) {
                if(!editor) {
                    add(new AttachAction(mode, null));
                    add(new JSeparator());
                }

                add(new AttachAction(mode, Constants.TOP));
                add(new AttachAction(mode, Constants.LEFT));
                add(new AttachAction(mode, Constants.BOTTOM));
                add(new AttachAction(mode, Constants.RIGHT));
            } else {
                setEnabled(false);
            }
        }
    } // End of ModeMenu class.
    

    /** Action which acutally performs the attaching of the component. */
    private static class AttachAction extends AbstractAction {
        private final ModeImpl mode;
        private final String side;
        
        public AttachAction(ModeImpl mode, String side) {
            this.mode = mode;
            this.side = side;
            
            String key;
            if(side == null) {
                key = "CTL_SideAsLastTab"; // NOI18N
                TopComponent active = TopComponent.getRegistry().getActivated();
                if(mode.getOpenedTopComponents().contains(active)) {
                    setEnabled(false);
                }
            } else if(side == Constants.TOP) {
                key = "CTL_SideTop"; // NOI18N
            } else if(side == Constants.LEFT) {
                key = "CTL_SideLeft"; // NOI18N
            } else if(side == Constants.BOTTOM) {
                key = "CTL_SideBottom"; // NOI18N
            } else if(side == Constants.RIGHT) {
                key = "CTL_SideRight"; // NOI18N
            } else {
                return;
            }
            
            putValue(Action.NAME, NbBundle.getMessage(AttachWindowAction.class, key));
        }
        
        public void actionPerformed(ActionEvent evt) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            ModeImpl activeMode = wm.getActiveMode();
            if(activeMode == null) {
                return;
            }

            TopComponent selected = activeMode.getSelectedTopComponent();
            if(selected == null || !mode.canContain(selected)) {
                return;
            }

            if(side == null) {
                mode.dockInto(selected);
                selected.open();
                selected.requestActive();
            } else {
                wm.attachTopComponentToSide(selected, mode, side);
            }
        }
    } // End of class AttachAction.

}

