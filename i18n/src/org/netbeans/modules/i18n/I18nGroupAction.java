/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.netbeans.modules.i18n.wizard.I18nWizardAction;
import org.netbeans.modules.i18n.wizard.I18nTestWizardAction;

import org.openide.awt.Actions;
import org.openide.awt.JMenuPlus;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;


/**
 * Pseudo-action representing the Internationalization submenu.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 */
public final class I18nGroupAction extends SystemAction
                                   implements ContextAwareAction,
                                              Presenter.Menu,
                                              Presenter.Popup,
                                              Presenter.Toolbar {

    public I18nGroupAction() {
        
        /*
         * Caution! Calling putValue(...) triggers the initialize() method!
         */
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    /** Array of i18n actions. */
    protected static final SystemAction[] i18nActions = new SystemAction[] {
        SystemAction.get(I18nAction.class),
        SystemAction.get(InsertI18nStringAction.class),
        null,
        SystemAction.get(I18nWizardAction.class),
        SystemAction.get(I18nTestWizardAction.class),
    };
    
    /**
     * Does nothing, possibly except throwing an <code>AssertionError</code>.
     */
    public void actionPerformed(ActionEvent e) {
        assert false;
    }
    
    /** Gets localized name of action. Implements superclass abstract method. */
    public String getName() {
        return I18nUtil.getBundle().getString("LBL_I18nGroupActionName");
    }

    /** Gets icon resource. Overrides suprclass method. */
     protected String iconResource () {
         return "org/netbeans/modules/i18n/i18nAction.gif"; // NOI18N
     }

    /** Gets help context. Implements abstract superclass method. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(I18nUtil.HELP_ID_I18N);
    }
    
    /**
     */
    public JMenuItem getMenuPresenter() {
        return LazyPopup.createLazyPopup(true, this);
    }
    
    /**
     */
    public JMenuItem getPopupPresenter() {
        return LazyPopup.createLazyPopup(false, this);
    }

    /**
     */
    public Component getToolbarPresenter() {
        // In jdk1.3 could be used new JToolBar(getName()).
        JToolBar toolbar = new JToolBar (getName());

        for(int i=0; i<i18nActions.length; i++) {
            SystemAction action = i18nActions[i];

            if(action == null) {
                toolbar.addSeparator();
            } else if(action instanceof Presenter.Toolbar) {
                toolbar.add(((Presenter.Toolbar)action).getToolbarPresenter());
            }
        }

        return toolbar;
    }
    
    /**
     */
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAwareClass(this, actionContext);
    }
    
    
    /** Menu item which will create its items lazilly when the popup will becomming visible.
     * Performance savings.*/
    static class LazyPopup extends JMenuPlus {

        /** Icon. */
        private static Icon icon = null;
        
        /** Indicates if is part of menu, i.e. if should have icons. */
        private boolean isMenu;
        
        /** Indicates whether menu items were created. */
        private boolean created = false;

        
        /** Constructor. */
        private LazyPopup(boolean isMenu, SystemAction action) {
            Actions.setMenuText(this, action.getName(), isMenu);
            
            this.isMenu = isMenu;
            if (isMenu) {
                // mkleint doens't seem to be dynamic at all, just set it for main menu..
                createMenuItems();
            }
        }

        /** Creates <code>LazyPopup</code> menu item. */
        static JMenuItem createLazyPopup(boolean isMenu, SystemAction action) {
            return new LazyPopup(isMenu, action);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            if(!created)
                createMenuItems();
            
            return super.getPopupMenu();
        }

        /** Creates items when actually needed. */
        private void createMenuItems() {
            created = true;
            removeAll();

            for(int i=0; i<i18nActions.length; i++) {
                SystemAction action = i18nActions[i];

                if(action == null) {
                    addSeparator();
                } else if(!isMenu && action instanceof Presenter.Popup) {
                    add(((Presenter.Popup)action).getPopupPresenter());
                } else if(isMenu && action instanceof Presenter.Menu) {
                    add(((Presenter.Menu)action).getMenuPresenter());
                }
            }
        }
    } // End of class LazyPopup.
    
    /**
     * Context-aware variant of the <code>I18nGroupAction</code>.
     *
     * @see  org.openide.util.ContextAwareAction
     */
    private final class ContextAwareClass implements Action,
                                                     PropertyChangeListener,
                                                     Presenter.Menu,
                                                     Presenter.Popup,
                                                     Presenter.Toolbar {
        /* The class is final only for performance reasons. */
        
        /** */
        private final I18nGroupAction delegate;
        /** */
        private final Lookup context;
        /** */
        private final Action[] contextActions;
        /** */
        private List/*<PropertyChangeListener>*/ propListeners;
        /** is this action enabled? */
        private volatile Boolean enabled = null;        //unknown
        
        /**
         */
        ContextAwareClass(I18nGroupAction delegate, Lookup actionContext) {
            this.delegate = delegate;
            this.context = actionContext;
            this.contextActions = new Action[i18nActions.length];
            
            /*
             * Listen on the delegate's property changes so that we can
             * propagate them to this action:
             */
            delegate.addPropertyChangeListener(
                    WeakListeners.propertyChange(this, delegate));
            
            for (int i = 0; i < i18nActions.length; i++) {
                final SystemAction action = i18nActions[i];
                if (action == null) {
                    contextActions[i] = null;
                } else {
                    contextActions[i] = (action instanceof ContextAwareAction)
                                        ? ((ContextAwareAction) action)
                                          .createContextAwareInstance(context)
                                        : action;
                    
                    /*
                     * Listen on the property changes in order to detect
                     * that one of the I18n actions was enabled/disabled:
                     */
                    contextActions[i].addPropertyChangeListener(
                            WeakListeners.propertyChange(this,
                                                         contextActions[i]));
                }
            }
        }
        
        /**
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getSource() == delegate) {
                firePropertyChange(e.getPropertyName(),
                                   e.getOldValue(),
                                   e.getNewValue());
                return;
            }
            
            /* Source is one of the I18n actions. */
            final String propertyName = e.getPropertyName();
            if (PROP_ENABLED.equals(propertyName)) {
                updateEnabled();
            }
        }
        
        private synchronized void updateEnabled() {
            assert EventQueue.isDispatchThread();
            
            if (propListeners == null) {
                enabled = null;                 //unknown
                return;
            }
            
            Boolean wasEnabled = enabled;
            enabled = Boolean.valueOf(shouldBeEnabled());
            if (enabled != wasEnabled) {
                fireEnabledChanged();
            }
        }
        
        /**
         */
        private void fireEnabledChanged() {
            firePropertyChange(PROP_ENABLED, null, null);
        }
        
        /**
         * Determines whether the <em>Internationalize</em> submenu should
         * be enabled or not.
         *
         * @return  <code>true</code> if at least one I18n action displayed
         *          in the submenu should be enabled;
         *          <code>false</code> otherwise
         */
        private boolean shouldBeEnabled() {
            for (int i = 0; i < i18nActions.length; i++) {
                if ((contextActions[i] != null)
                        && contextActions[i].isEnabled()) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         */
        public boolean isEnabled() {
            assert EventQueue.isDispatchThread();
            
            if (enabled == null) {
                enabled = Boolean.valueOf(shouldBeEnabled());
            }
            return enabled.booleanValue();
        }
        
        /**
         * Has no effect.
         */
        public void setEnabled(boolean enabled) { }
        
        /**
         */
        public void actionPerformed(ActionEvent e) {
            delegate.actionPerformed(e);
        }
        
        /**
         */
        public JMenuItem getMenuPresenter() {
            return delegate.getMenuPresenter();
        }
        
        /**
         */
        public JMenuItem getPopupPresenter() {
            return delegate.getPopupPresenter();
        }
        
        /**
         */
        public Component getToolbarPresenter() {
            return delegate.getToolbarPresenter();
        }
        
        /**
         */
        public Object getValue(String name) {
            return delegate.getValue(name);
        }
        
        /**
         */
        public void putValue(String name, Object value) {
            delegate.putValue(name, value);
        }
        
        /**
         */
        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (propListeners == null) {
                propListeners = new ArrayList(4);
            }
            propListeners.add(l);
        }
        
        /**
         */
        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            if ((propListeners != null)
                    && propListeners.remove(l)
                    && propListeners.isEmpty()) {
                propListeners = null;
            }
        }
        
        /**
         */
        private void firePropertyChange(String propName,
                                        Object oldValue,
                                        Object newValue) {
            if ((newValue != null) && newValue.equals(oldValue)) {
                return;
            }
            
            if (propListeners != null) {
                final PropertyChangeEvent e = new PropertyChangeEvent(
                                                    this,
                                                    propName,
                                                    oldValue,
                                                    newValue);
                for (Iterator i = propListeners.iterator(); i.hasNext(); ) {
                    ((PropertyChangeListener) i.next()).propertyChange(e);
                }
            }
        }
    
    }

}
