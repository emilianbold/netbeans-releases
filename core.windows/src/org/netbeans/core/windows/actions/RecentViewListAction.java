/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.actions;

import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.KeyboardPopupSwitcher;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Invokes Recent View List
 *
 * @author  Marek Slama
 */
public final class RecentViewListAction extends AbstractAction
        implements PropertyChangeListener {
    
    /** Creates a new instance of RecentViewListAction */
    public RecentViewListAction() {
        putValue(NAME, NbBundle.getMessage(RecentViewListAction.class, "CTL_RecentViewListAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent[] documents = getRecentDocuments();
        
        if (documents.length < 2) {
            return;
        }
        
        if(!"immediately".equals(evt.getActionCommand()) && // NOI18N
                !(evt.getSource() instanceof javax.swing.JMenuItem)) {
            // #46800: fetch key directly from action command
            KeyStroke keyStroke = Utilities.stringToKey(evt.getActionCommand());
            
            if(keyStroke != null) {
                int triggerKey = keyStroke.getKeyCode();
                int reverseKey = KeyEvent.VK_SHIFT;
                int releaseKey = 0;
                
                int modifiers = keyStroke.getModifiers();
                if((InputEvent.CTRL_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_CONTROL;
                } else if((InputEvent.ALT_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_ALT;
                } else if((InputEvent.META_MASK & modifiers) != 0) {
                    releaseKey = InputEvent.META_MASK;
                }
                
                if(releaseKey != 0) {
                    if (!KeyboardPopupSwitcher.isShown()) {
                        Frame owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()
                        instanceof Frame ?
                            (Frame) KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()
                            : WindowManager.getDefault().getMainWindow();
                        KeyboardPopupSwitcher.selectItem(
                                createSwitcherItems(documents),
                                releaseKey, triggerKey);
                    }
                    return;
                }
            }
        }
        
        TopComponent tc = documents[1];
        // #37226 Unmaximized the other mode if needed.
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl) wm.findMode(tc);
        if(mode != null && mode != wm.getMaximizedMode()) {
            wm.setMaximizedMode(null);
        }
        
        tc.requestActive();
    }
    
    private SwitcherTableItem[] createSwitcherItems(TopComponent[] tcs) {
        SwitcherTableItem[] items = new SwitcherTableItem[tcs.length];
        for (int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            String name = tc.getDisplayName();
            if (name == null || name.trim().length() == 0) {
                name = tc.getName();
            }
            Image image = tc.getIcon();
            String description = tc.getToolTipText();
            ImageIcon imageIcon = (image != null ? new ImageIcon(image) : null);
            items[i] = new SwitcherTableItem(
                    new ActivableTC(tc),
                    name,
                    imageIcon,
                    false,
                    description != null ? description : name);
        }
        return items;
    }
    
    private class ActivableTC implements SwitcherTableItem.Activable {
        private TopComponent tc;
        private ActivableTC(TopComponent tc) {
            this.tc = tc;
        }
        public void activate() {
            if (tc != null) {
                tc.requestActive();
            }
        }
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    /** Only here for fix #41477:, called from layer.xml:
     * For KDE on unixes, Ctrl+TAB is occupied by OS,
     * so we also register Ctrl+BACk_QUOTE as recent view list action shortcut.
     * For other OS's, Ctrl+TAB is the only default, because we create link
     * not pointing to anything by returning null
     */
    public static String getStringRep4Unixes() {
        if (Utilities.isUnix()) {
            return "Actions/Window/org-netbeans-core-windows-actions-RecentViewListAction.instance"; //NOI18N
        }
        return null;
    }
    
    /**
     * Update enable state of this action.
     */
    private void updateEnabled() {
        setEnabled(isMoreThanOneDocOpened());
    }
    
    private boolean isMoreThanOneDocOpened() {
        for(Iterator it = WindowManagerImpl.getInstance().getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next(); {
                if (mode.getKind() == Constants.MODE_KIND_EDITOR)
                    return (mode.getOpenedTopComponents().size() > 1);
            }
        }
        return false;
    }
    
    private TopComponent[] getRecentDocuments() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] documents = wm.getRecentViewList();
        
        List docsList = new ArrayList();
        for (int i = 0; i < documents.length; i++) {
            TopComponent tc = documents[i];
            if (tc == null) {
                continue;
            }
            ModeImpl mode = (ModeImpl)wm.findMode(tc);
            if (mode == null) {
                continue;
            }
            
            if (mode.getKind() == Constants.MODE_KIND_EDITOR) {
                docsList.add(tc);
            }
        }
        return (TopComponent[])docsList.toArray(new TopComponent[0]);
    }
}

